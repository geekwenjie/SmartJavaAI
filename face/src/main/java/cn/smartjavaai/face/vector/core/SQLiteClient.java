package cn.smartjavaai.face.vector.core;

import cn.hutool.core.util.IdUtil;
import cn.smartjavaai.common.config.Config;
import cn.smartjavaai.common.utils.SimilarityUtil;
import cn.smartjavaai.face.dao.FaceDao;
import cn.smartjavaai.face.entity.FaceSearchParams;
import cn.smartjavaai.face.vector.config.SQLiteConfig;
import cn.smartjavaai.face.vector.entity.FaceVector;
import cn.smartjavaai.common.entity.face.FaceSearchResult;
import cn.smartjavaai.face.vector.exception.VectorDBException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

@Slf4j
public class SQLiteClient implements VectorDBClient {

    private final FaceDao faceDao;
    //private final List<FaceVector> memoryIndex = new CopyOnWriteArrayList<>();
    private final ConcurrentHashMap<String, FaceVector> memoryIndex = new ConcurrentHashMap<>();
    private int featureDimension; // 维度

    private final ExecutorService executor = Executors.newFixedThreadPool(4);

    private SQLiteConfig config;

    /**
     * 是否初始化完毕
     */
    private boolean isInit;

    public SQLiteClient(SQLiteConfig config) {
        this.config = config;
        String dbPath = config.getDbPath();
        //如果未指定db路径，则使用默认路径
        if(StringUtils.isBlank(config.getDbPath())){
            dbPath = Config.getCachePath() + File.separator + "face.db";
            log.debug("使用默认SQLite人脸库路径: {}", dbPath);
        }
        this.faceDao = FaceDao.getInstance(dbPath);
    }


    @Override
    public void initialize() {
        try {
            // 加载所有特征到内存
            loadAllFeaturesToMemory();
            isInit = true;
            log.debug("SQLiteVectorDB initialized with {} faces", memoryIndex.size());
        } catch (Exception e) {
            throw new VectorDBException("初始化失败", e);
        }
    }

    // 以下方法保持接口兼容但忽略collectionName参数
    @Override
    public void createCollection(String collectionName, int dimension) {
        this.featureDimension = dimension;
        log.debug("特征维度设置为: {}", dimension);
    }

    @Override
    public void dropCollection(String collectionName) {
        if (!isInit){
            throw new VectorDBException("人脸库未加载完毕");
        }
        clearAllData();
        log.warn("所有数据已被清空");
    }

    @Override
    public boolean hasCollection(String collectionName) {
        throw new UnsupportedOperationException("Sqlite 不支持此操作");
    }

    @Override
    public String insert(FaceVector faceVector) {
        if (!isInit){
            throw new VectorDBException("人脸库未加载完毕");
        }
        return insertBatch(Collections.singletonList(faceVector)).get(0);
    }

    @Override
    public void upsert(FaceVector faceVector) {
//        if (faceVector.getId() != null) {
//            delete(faceVector.getId());
//        }
        insert(faceVector);
    }

    @Override
    public List<String> insertBatch(List<FaceVector> faceVectors) {
        if (!isInit){
            throw new VectorDBException("人脸库未加载完毕");
        }
        List<String> ids = new ArrayList<>();
        try {
            for (FaceVector faceVector : faceVectors) {
                String id = faceVector.getId() != null ?
                        faceVector.getId() : IdUtil.simpleUUID();
                faceVector.setId(id);
                // 保存到数据库
                faceDao.insertOrUpdate(faceVector);
                // 添加到内存索引
                addToMemoryIndex(faceVector);
                ids.add(id);
            }
            log.debug("插入了 {} 个人脸向量", faceVectors.size());
            return ids;
        } catch (Exception e) {
            throw new VectorDBException("批量插入失败", e);
        }
    }

    @Override
    public void delete(String id) {
        if (!isInit){
            throw new VectorDBException("人脸库未加载完毕");
        }
        deleteBatch(Collections.singletonList(id));
    }

    @Override
    public void deleteBatch(List<String> ids) {
        if (!isInit){
            throw new VectorDBException("人脸库未加载完毕");
        }
        try {
            // 从数据库中删除
            boolean isSuccess = faceDao.deleteFace(ids.toArray(new String[0]));
            // 从内存中删除
            ids.forEach(memoryIndex::remove);
            if(!isSuccess){
                throw new VectorDBException("删除失败");
            }
        } catch (Exception e) {
            throw new VectorDBException("批量删除失败", e);
        }
    }

    @Override
    public List<FaceSearchResult> search(float[] queryVector, FaceSearchParams faceSearchParams) {
        if (!isInit){
            throw new VectorDBException("人脸库未加载完毕");
        }
        if (memoryIndex.isEmpty()) {
            return Collections.emptyList();
        }
        // 并行计算相似度
        List<CompletableFuture<FaceSearchResult>> futures = memoryIndex.values().stream()
                .map(vector -> CompletableFuture.supplyAsync(() -> {
                    float similarity = SimilarityUtil.calculate(queryVector, vector.getVector(), config.getSimilarityType(), faceSearchParams.getNormalizeSimilarity());
                    return similarity >= faceSearchParams.getThreshold() ?
                            new FaceSearchResult(vector.getId(), similarity, vector.getMetadata()) :
                            null;
                }, executor))
                .collect(Collectors.toList());

        // 收集结果并过滤null
        List<FaceSearchResult> allResults = futures.stream()
                .map(CompletableFuture::join)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        // 获取TopK结果
        return allResults.stream()
                .sorted(Comparator.comparingDouble(FaceSearchResult::getSimilarity).reversed())
                .limit(faceSearchParams.getTopK())
                .collect(Collectors.toList());
    }

    @Override
    public long count(String collectionName) {
        return memoryIndex.size();
    }

    @Override
    public void close() {
        executor.shutdown();
        try {
            if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    @Override
    public FaceVector getFaceInfoById(String id) {
        if (!isInit) {
            throw new VectorDBException("人脸库未加载完毕");
        }
        // 先从内存缓存中获取
        FaceVector faceVector = memoryIndex.get(id);
        if (faceVector == null) {
            // 如果内存中没有，则从数据库查询
            try {
                faceVector = faceDao.findById(id);
            } catch (SQLException | ClassNotFoundException e) {
                throw new VectorDBException("SQLite查询异常", e);
            }
        }
        return faceVector;
    }

    @Override
    public List<FaceVector> listFaces(long pageNum, long pageSize) {
        if (!isInit) {
            throw new VectorDBException("人脸库未加载完毕");
        }

        if (pageNum < 1 || pageSize < 1) {
            throw new IllegalArgumentException("pageNum和pageSize必须大于0");
        }

        // 从数据库中查询指定分页的数据
        try {
            return faceDao.findFace((int)pageNum, (int)pageSize);
        } catch (Exception e) {
            throw new VectorDBException("分页查询失败", e);
        }
    }

    // ============= 私有辅助方法 =============

    private void loadAllFeaturesToMemory() {
        try {
            int pageSize = 1000;
            int page = 1;
            while (true) {
                List<FaceVector> batch = faceDao.findFace(page, pageSize);
                if (CollectionUtils.isEmpty(batch)) {
                    break;
                }
                for (FaceVector vector : batch) {
                    addToMemoryIndex(vector);
                }
                page++;
            }
            log.debug("从数据库加载了 {} 个特征向量到内存", memoryIndex.size());
        } catch (Exception e) {
            throw new VectorDBException("加载特征到内存失败", e);
        }
    }

    private void addToMemoryIndex(FaceVector faceVector) {
        memoryIndex.put(faceVector.getId(), faceVector);
    }

    private void clearAllData() {
        if (!isInit){
            throw new VectorDBException("人脸库未加载完毕");
        }
        try {
            faceDao.deleteAll();
            memoryIndex.clear();
        } catch (Exception e) {
            log.error("清空数据库失败", e);
        }
    }

    @Override
    public void loadFaceFeatures() {
        // 加载所有特征到内存
        loadAllFeaturesToMemory();
        isInit = true;
        log.debug("SQLiteVectorDB load success {} faces", memoryIndex.size());
    }

    @Override
    public void releaseFaceFeatures() {
        if (!isInit){
            throw new VectorDBException("人脸库未加载完毕");
        }
        memoryIndex.clear();
        isInit = false;
    }
}
