package cn.smartjavaai.face.vector.core;

import cn.smartjavaai.face.entity.FaceSearchParams;
import cn.smartjavaai.face.enums.IdStrategy;
import cn.smartjavaai.face.utils.FaceUtils;
import cn.smartjavaai.face.vector.config.MilvusConfig;
import cn.smartjavaai.face.vector.constant.VectorDBConstants;
import cn.smartjavaai.face.vector.entity.FaceVector;
import cn.smartjavaai.common.entity.face.FaceSearchResult;
import cn.smartjavaai.face.vector.exception.VectorDBException;
import io.milvus.client.MilvusServiceClient;
import io.milvus.grpc.*;
import io.milvus.param.*;
import io.milvus.param.collection.*;
import io.milvus.param.dml.*;
import io.milvus.param.index.CreateIndexParam;
import io.milvus.response.DescCollResponseWrapper;
import io.milvus.response.QueryResultsWrapper;
import io.milvus.response.SearchResultsWrapper;
import io.milvus.v2.service.collection.request.DescribeCollectionReq;
import io.milvus.v2.service.collection.response.DescribeCollectionResp;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Milvus向量数据库客户端实现
 * @author dwj
 */
@Slf4j
public class MilvusClient implements VectorDBClient {

    private final MilvusConfig config;
    private MilvusServiceClient serviceClient;

    private String collectionName;

    /**
     * 是否初始化完毕
     */
    private boolean isInit;

    public MilvusClient(MilvusConfig config) {
        this.config = config;
    }

    @Override
    public void initialize() {
        try {
            ConnectParam.Builder builder = ConnectParam.newBuilder()
                    .withHost(config.getHost())
                    .withPort(config.getPort());
            if (StringUtils.isNotBlank(config.getUsername()) && StringUtils.isNotBlank(config.getPassword())) {
                builder.withAuthorization(config.getUsername(), config.getPassword());
            }
            ConnectParam connectParam = builder.build();
            serviceClient = new MilvusServiceClient(connectParam);
            collectionName = StringUtils.isNotBlank(config.getCollectionName()) ? config.getCollectionName() : VectorDBConstants.Defaults.DEFAULT_COLLECTION_NAME;
            createCollection(collectionName, config.getDimension());

            boolean isAutoID = isAutoID(collectionName);
            if(isAutoID && config.getIdStrategy() != IdStrategy.AUTO){
                throw new VectorDBException("ID策略与当前Collection不匹配");
            }
            if(!isAutoID && config.getIdStrategy() == IdStrategy.AUTO){
                throw new VectorDBException("ID策略与当前Collection不匹配");
            }
            if(config.isUseMemoryCache()){
                // 加载集合到内存
                loadFaceFeatures();
            }
            isInit = true;
        } catch (Exception e) {
            throw new VectorDBException("初始化Milvus客户端失败", e);
        }
    }

    @Override
    public void createCollection(String collectionName, int dimension) {
        try {
            if (hasCollection(collectionName)) {
                log.debug("集合已存在:{}", collectionName);
                return;
            }
            // 创建集合字段
            FieldType idField = null;

            switch (config.getIdStrategy()){
                case AUTO://自动生成ID
                    idField = FieldType.newBuilder()
                            .withName(VectorDBConstants.FieldNames.ID_FIELD)
                            .withDataType(DataType.Int64)
                            .withPrimaryKey(true)
                            .withAutoID(true)
                            .build();
                    break;
                case CUSTOM://自定义ID
                    idField = FieldType.newBuilder()
                            .withName(VectorDBConstants.FieldNames.ID_FIELD)
                            .withDataType(DataType.VarChar)
                            .withMaxLength(VectorDBConstants.Defaults.DEFAULT_ID_MAX_LENGTH)
                            .withPrimaryKey(true)
                            .withAutoID(false)
                            .build();
                    break;
            }

            FieldType vectorField = FieldType.newBuilder()
                    .withName(VectorDBConstants.FieldNames.VECTOR_FIELD)
                    .withDataType(DataType.FloatVector)
                    .withDimension(dimension)
                    .build();

            FieldType metadataField = FieldType.newBuilder()
                    .withName(VectorDBConstants.FieldNames.METADATA_FIELD)
                    .withDataType(DataType.VarChar)
                    .withMaxLength(VectorDBConstants.Defaults.DEFAULT_METADATA_MAX_LENGTH)
                    .withNullable(true)//允许为空值
                    .build();

            // 创建集合参数
            CreateCollectionParam createCollectionParam = CreateCollectionParam.newBuilder()
                    .withCollectionName(collectionName)
                    .withDescription("人脸特征向量集合")
                    .addFieldType(idField)
                    .addFieldType(vectorField)
                    .addFieldType(metadataField)
                    .build();

            R<RpcStatus> response = serviceClient.createCollection(createCollectionParam);

            if (response.getStatus() != R.Status.Success.getCode()) {
                throw new VectorDBException("Milvus 创建集合失败：" + response.getMessage());
            }

            log.debug("创建集合成功");

            // 创建索引
            IndexType indexType = IndexType.IVF_FLAT;
            if (config.getIndexType() != null) {
                indexType = config.getIndexType();
            }

            CreateIndexParam indexParam = CreateIndexParam.newBuilder()
                    .withCollectionName(collectionName)
                    .withFieldName(VectorDBConstants.FieldNames.VECTOR_FIELD)
                    .withIndexType(indexType)
                    .withMetricType(config.getMetricType())
                    .withExtraParam(String.format("{\"nlist\":%d}", config.getNlist()))
                    .withSyncMode(Boolean.TRUE)//调用方法后等待 Milvus 执行完成
                    .build();

            R<RpcStatus> createIndexResponse = serviceClient.createIndex(indexParam);

            if (createIndexResponse.getStatus() != R.Status.Success.getCode()) {
                throw new VectorDBException("Milvus 创建索引失败：" + createIndexResponse.getMessage());
            }

            log.debug("创建索引成功");


        } catch (Exception e) {
            throw new VectorDBException("创建Milvus集合失败", e);
        }
    }

    /**
     * 判断是否为自增长ID
     * @param collectionName
     * @return
     */
    private boolean isAutoID(String collectionName) {
        R<DescribeCollectionResponse> response = serviceClient.describeCollection(
                DescribeCollectionParam.newBuilder()
                        .withCollectionName(collectionName)
                        .build()
        );
        DescCollResponseWrapper wrapper = new DescCollResponseWrapper(response.getData());
        return wrapper.getPrimaryField().isAutoID();
    }

    @Override
    public void dropCollection(String collectionName) {
        try {
            if (!isInit){
                throw new VectorDBException("Milvus未初始化完毕");
            }
            if (hasCollection(collectionName)) {
                R<RpcStatus> response = serviceClient.dropCollection(DropCollectionParam.newBuilder()
                        .withCollectionName(collectionName)
                        .build());
                if (response.getStatus() != R.Status.Success.getCode()) {
                    throw new VectorDBException("Milvus 删除集合失败：" + response.getMessage());
                }
                isInit = false;
            }
        } catch (Exception e) {
            throw new VectorDBException("删除Milvus集合失败", e);
        }
    }

    @Override
    public boolean hasCollection(String collectionName) {

        try {
            R<Boolean> response = serviceClient.hasCollection(
                    HasCollectionParam.newBuilder()
                            .withCollectionName(collectionName)
                            .build()
            );

            if (response.getStatus() != R.Status.Success.getCode()) {
                throw new VectorDBException("Milvus 查询失败：" + response.getMessage());
            }
            return Boolean.TRUE.equals(response.getData());
        } catch (Exception e) {
            throw new VectorDBException("检查Milvus集合是否保存失败", e);
        }
    }

    @Override
    public String insert(FaceVector faceVector) {
        try {
            if (!isInit){
                throw new VectorDBException("Milvus未初始化完毕");
            }
            //验证
            if(faceVector == null){
                throw new VectorDBException("插入数据失败：faceVector不能为空");
            }
            if(faceVector.getVector() == null || faceVector.getVector().length == 0){
                throw new VectorDBException("插入数据失败：vector不能为空");
            }

            //自定义ID
            if(config.getIdStrategy() == IdStrategy.CUSTOM){
                if(StringUtils.isBlank(faceVector.getId())){
                    throw new VectorDBException("插入数据失败：ID生成策略-自定义ID，id不能为空");
                }
            }
            // 转 float[] 为 List<Float>
            List<Float> vectorList = new ArrayList<>();
            for (float v : faceVector.getVector()) {
                vectorList.add(v);
            }

            List<List<Float>> vectors = Collections.singletonList(vectorList);
            //List<String> metadataList = Collections.singletonList(faceVector.getMetadata());

            List<String> metadataList = Optional.ofNullable(faceVector.getMetadata())
                    .map(Collections::singletonList)
                    .orElse(Collections.emptyList());
            List<InsertParam.Field> fields = null;
            switch (config.getIdStrategy()){
                case AUTO://自动生成ID
                    fields = Arrays.asList(
                            InsertParam.Field.builder().name(VectorDBConstants.FieldNames.VECTOR_FIELD).values(vectors).build(),
                            InsertParam.Field.builder().name(VectorDBConstants.FieldNames.METADATA_FIELD).values(metadataList).build()
                    );
                    break;
                case CUSTOM://自定义ID
                    List<String> ids = Collections.singletonList(faceVector.getId());
                    fields = Arrays.asList(
                            InsertParam.Field.builder().name(VectorDBConstants.FieldNames.ID_FIELD).values(ids).build(),
                            InsertParam.Field.builder().name(VectorDBConstants.FieldNames.VECTOR_FIELD).values(vectors).build(),
                            InsertParam.Field.builder().name(VectorDBConstants.FieldNames.METADATA_FIELD).values(metadataList).build()
                    );
                    break;
            }

            InsertParam insertParam = InsertParam.newBuilder()
                    .withCollectionName(collectionName)
                    .withFields(fields)
                    .build();

            R<MutationResult> response = serviceClient.insert(insertParam);
            if (response.getStatus() != R.Status.Success.getCode()) {
                throw new VectorDBException("插入失败: " + response.getMessage());
            }

            List<Long> autoIds = response.getData().getIDs().getIntId().getDataList();


            return config.getIdStrategy()  == IdStrategy.AUTO ? String.valueOf(autoIds.get(0)) : faceVector.getId();
        } catch (Exception e) {
            throw new VectorDBException("插入Milvus向量失败", e);
        }
    }

    @Override
    public void upsert(FaceVector faceVector) {
        try {
            if (!isInit){
                throw new VectorDBException("Milvus未初始化完毕");
            }
            if(config.getIdStrategy() == IdStrategy.AUTO){
                throw new VectorDBException("idStrategy为AUTO时,不支持更新操作");
            }
            //验证
            if(faceVector == null){
                throw new VectorDBException("更新数据失败：faceVector不能为空");
            }
            if(faceVector.getVector() == null || faceVector.getVector().length == 0){
                throw new VectorDBException("更新数据失败：vector不能为空");
            }
            if(StringUtils.isBlank(faceVector.getId())){
                throw new VectorDBException("更新数据失败：id不能为空");
            }

            // 转换向量为 List<Float>
            List<Float> vectorList = new ArrayList<>();
            for (float v : faceVector.getVector()) {
                vectorList.add(v);
            }

            List<List<Float>> vectors = Collections.singletonList(vectorList);
            List<String> metadataList = Optional.ofNullable(faceVector.getMetadata())
                    .map(Collections::singletonList)
                    .orElse(Collections.emptyList());

            // 准备字段列表
            List<UpsertParam.Field> fields = new ArrayList<>();

            fields.add(UpsertParam.Field.builder()
                    .name(VectorDBConstants.FieldNames.ID_FIELD)
                    .values(Collections.singletonList(faceVector.getId()))
                    .build());

            // 添加向量和元数据字段
            fields.add(UpsertParam.Field.builder()
                    .name(VectorDBConstants.FieldNames.VECTOR_FIELD)
                    .values(vectors)
                    .build());

            fields.add(UpsertParam.Field.builder()
                    .name(VectorDBConstants.FieldNames.METADATA_FIELD)
                    .values(metadataList)
                    .build());

            // 构建Upsert参数
            UpsertParam upsertParam = UpsertParam.newBuilder()
                    .withCollectionName(collectionName)
                    .withFields(fields)
                    .build();

            // 执行Upsert操作
            R<MutationResult> response = serviceClient.upsert(upsertParam);
            if (response.getStatus() != R.Status.Success.getCode()) {
                throw new VectorDBException("Upsert操作失败: " + response.getMessage());
            }
        } catch (Exception e) {
            throw new VectorDBException("Milvus Upsert操作失败", e);
        }
    }



    @Override
    public List<String> insertBatch(List<FaceVector> faceVectors) {
        try {
            if (!isInit){
                throw new VectorDBException("Milvus未初始化完毕");
            }
            List<String> ids = faceVectors.stream()
                    .map(FaceVector::getId)
                    .collect(Collectors.toList());


            List<List<Float>> vectors = new ArrayList<>();
            for (FaceVector fv : faceVectors) {
                List<Float> list = new ArrayList<>();
                for (float f : fv.getVector()) {
                    list.add(f);
                }
                vectors.add(list);
            }

            List<String> metadataList = faceVectors.stream()
                    .map(FaceVector::getMetadata)
                    .collect(Collectors.toList());

            InsertParam insertParam = InsertParam.newBuilder()
                    .withCollectionName(collectionName)
                    .withFields(Arrays.asList(
                            InsertParam.Field.builder().name(VectorDBConstants.FieldNames.ID_FIELD).values(ids).build(),
                            InsertParam.Field.builder().name(VectorDBConstants.FieldNames.VECTOR_FIELD).values(vectors).build(),
                            InsertParam.Field.builder().name(VectorDBConstants.FieldNames.METADATA_FIELD).values(metadataList).build()
                    ))
                    .build();

            R<MutationResult> insertResult = serviceClient.insert(insertParam);
            return ids;
        } catch (Exception e) {
            throw new VectorDBException("批量插入Milvus向量失败", e);
        }
    }

    @Override
    public void delete(String id) {
        try {
            if (!isInit){
                throw new VectorDBException("Milvus未初始化完毕");
            }
            String expr = String.format("%s == \"%s\"", VectorDBConstants.FieldNames.ID_FIELD, id);
            DeleteParam deleteParam = DeleteParam.newBuilder()
                    .withCollectionName(collectionName)
                    .withExpr(expr)
                    .build();

            R<MutationResult> response = serviceClient.delete(deleteParam);
            if (response.getStatus() != R.Status.Success.getCode()) {
                throw new VectorDBException("删除操作失败: " + response.getMessage());
            }
        } catch (Exception e) {
            throw new VectorDBException("删除Milvus向量失败", e);
        }
    }

    @Override
    public void deleteBatch(List<String> ids) {
        try {
            if (!isInit){
                throw new VectorDBException("Milvus未初始化完毕");
            }
            // 构建IN表达式: id in ["id1", "id2", ...]
            StringBuilder expr = new StringBuilder(VectorDBConstants.FieldNames.ID_FIELD + " in [");
            for (int i = 0; i < ids.size(); i++) {
                if(config.getIdStrategy() == IdStrategy.AUTO){
                    expr.append(ids.get(i)); // 不加引号
                }else{
                    expr.append("'" + ids.get(i) + "'"); // 不加引号
                }
                if (i < ids.size() - 1) {
                    expr.append(", ");
                }
            }



            expr.append("]");
            DeleteParam deleteParam = DeleteParam.newBuilder()
                    .withCollectionName(collectionName)
                    .withExpr(expr.toString())
                    .build();

            R<MutationResult> response = serviceClient.delete(deleteParam);

            if (response.getStatus() != R.Status.Success.getCode()) {
                throw new VectorDBException("删除操作失败: " + response.getMessage());
            }
        } catch (Exception e) {
            throw new VectorDBException("批量删除Milvus向量失败", e);
        }
    }

    @Override
    public List<FaceSearchResult> search(float[] queryVector, FaceSearchParams faceSearchParams) {
        try {
            if (!isInit){
                throw new VectorDBException("Milvus未初始化完毕");
            }
            // 1. 包装查询向量
            List<List<Float>> vectors = new ArrayList<>();
            List<Float> floatList = new ArrayList<>();
            for (float f : queryVector) {
                floatList.add(f);
            }
            vectors.add(floatList);

            // 2. 构造搜索参数
            SearchParam searchParam = SearchParam.newBuilder()
                    .withCollectionName(collectionName)
                    .withVectorFieldName(VectorDBConstants.FieldNames.VECTOR_FIELD)
                    .withTopK(faceSearchParams.getTopK())
                    .withMetricType(config.getMetricType())
                    .withOutFields(Arrays.asList(VectorDBConstants.FieldNames.ID_FIELD, VectorDBConstants.FieldNames.METADATA_FIELD))
                    .withVectors(vectors)
                    .withParams("{\"nprobe\": 10}")//和nlist有关
                    .build();
            R<SearchResults> resp = serviceClient.search(searchParam);
            if (resp.getStatus() != R.Status.Success.getCode()) {
                throw new VectorDBException("Milvus 查询失败: " + resp.getMessage());
            }

            SearchResults results = resp.getData();
            SearchResultsWrapper wrapper = new SearchResultsWrapper(results.getResults());

            // 3. 获取字段数据（FieldData）
            List<SearchResultsWrapper.IDScore> scores = wrapper.getIDScore(0); // 默认只有一条 query 向量

            List<FaceSearchResult> finalResults = new ArrayList<>();
            for (int i = 0; i < scores.size(); i++) {
                SearchResultsWrapper.IDScore score = scores.get(i);
                float similarity = score.getScore();
                if (faceSearchParams.getNormalizeSimilarity()) {
                    // 将分数转换为相似度
                    similarity = FaceUtils.convertScoreToSimilarity(config.getMetricType().name(), score.getScore());
                }
                if (similarity >= faceSearchParams.getThreshold()) {
                    // 获取 Metadata
                    String metadata = wrapper.getFieldData(VectorDBConstants.FieldNames.METADATA_FIELD, 0).get(i).toString();
                    // 获取 ID
                    String id = wrapper.getFieldData(VectorDBConstants.FieldNames.ID_FIELD, 0).get(i).toString();
                    finalResults.add(new FaceSearchResult(id, similarity, metadata));
                }
            }
            return finalResults;

        } catch (Exception e) {
            throw new VectorDBException("搜索 Milvus 向量失败", e);
        }
    }

    @Override
    public long count(String collectionName) {
        try {
            if (serviceClient == null){
                throw new VectorDBException("Milvus未初始化完毕");
            }
            R<QueryResults> response = serviceClient.query(
                    QueryParam.newBuilder()
                            .withCollectionName(collectionName)
                            .withOutFields(Collections.singletonList("count(*)"))
                            .build()
            );

            if (response.getStatus() != R.Status.Success.getCode()) {
                throw new VectorDBException("Milvus 查询失败，msg: " + response.getMessage());
            }

            List<FieldData> fields = response.getData().getFieldsDataList();
            if (fields.isEmpty()) {
                throw new VectorDBException("Milvus 返回空字段数据");
            }

            FieldData countField = fields.get(0);
            List<Long> countValues = countField.getScalars().getLongData().getDataList();
            if (countValues.isEmpty()) {
                throw new VectorDBException("Milvus count(*) 返回为空");
            }

            return countValues.get(0); // count(*) 查询的结果
        } catch (Exception e) {
            throw new VectorDBException("获取 Milvus 集合数量失败", e);
        }
    }

    @Override
    public void close() {
        if (serviceClient != null) {
            serviceClient.close();
            isInit = false;
        }
    }

    @Override
    public FaceVector getFaceInfoById(String id) {
        try {
            if (!isInit) {
                throw new VectorDBException("Milvus未初始化完毕");
            }
            String expr = VectorDBConstants.FieldNames.ID_FIELD + " == '" + id + "'";
            if(config.getIdStrategy() == IdStrategy.AUTO){
                expr = VectorDBConstants.FieldNames.ID_FIELD + " == " + id;
            }
            // 5. 执行查询
            R<QueryResults> response = serviceClient.query(
                    QueryParam.newBuilder()
                            .withCollectionName(collectionName)
                            .withExpr(expr)
                            .withOutFields(Arrays.asList(VectorDBConstants.FieldNames.ID_FIELD, VectorDBConstants.FieldNames.VECTOR_FIELD, VectorDBConstants.FieldNames.METADATA_FIELD))
                            .build()
            );

            // 处理响应
            if (response.getStatus() != R.Status.Success.getCode()) {
                throw new VectorDBException("查询失败: " + response.getMessage());
            }

            QueryResultsWrapper wrapper = new QueryResultsWrapper(response.getData());
            List<QueryResultsWrapper.RowRecord> records = wrapper.getRowRecords();
            if (records.isEmpty()) {
                return null;
            }
            // 提取第一条记录
            QueryResultsWrapper.RowRecord row = records.get(0);
            Object vectorObj = row.get(VectorDBConstants.FieldNames.VECTOR_FIELD);
            float[] vector = null;
            if (vectorObj instanceof List<?>) {
                // Milvus SDK通常返回List<Float>，转成float[]
                List<Float> vectorList = (List<Float>) vectorObj;
                vector = new float[vectorList.size()];
                for (int i = 0; i < vectorList.size(); i++) {
                    vector[i] = vectorList.get(i);
                }
            }
            return new FaceVector(id, vector, (String) row.get(VectorDBConstants.FieldNames.METADATA_FIELD));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<FaceVector> listFaces(long pageNum, long pageSize) {
        try {
            if (!isInit) {
                throw new VectorDBException("Milvus未初始化完毕");
            }
            if (pageNum < 1 || pageSize < 1) {
                throw new IllegalArgumentException("pageNum和pageSize必须大于0");
            }
            long offset = (pageNum - 1) * pageSize;
            // 构造查询参数，使用offset和limit实现分页
            QueryParam queryParam = QueryParam.newBuilder()
                    .withCollectionName(collectionName)
                    .withOutFields(Arrays.asList(
                            VectorDBConstants.FieldNames.ID_FIELD,
                            VectorDBConstants.FieldNames.VECTOR_FIELD,
                            VectorDBConstants.FieldNames.METADATA_FIELD))
                    .withOffset(offset)
                    .withLimit(pageSize)
                    .build();

            R<QueryResults> response = serviceClient.query(queryParam);

            if (response.getStatus() != R.Status.Success.getCode()) {
                throw new VectorDBException("分页查询失败: " + response.getMessage());
            }

            QueryResultsWrapper wrapper = new QueryResultsWrapper(response.getData());
            List<QueryResultsWrapper.RowRecord> records = wrapper.getRowRecords();
            if (records.isEmpty()) {
                return Collections.emptyList();
            }

            List<FaceVector> result = new ArrayList<>();
            for (QueryResultsWrapper.RowRecord row : records) {
                String id = (String) row.get(VectorDBConstants.FieldNames.ID_FIELD);
                Object vectorObj = row.get(VectorDBConstants.FieldNames.VECTOR_FIELD);
                float[] vector = null;
                if (vectorObj instanceof List<?>) {
                    List<Float> vectorList = (List<Float>) vectorObj;
                    vector = new float[vectorList.size()];
                    for (int i = 0; i < vectorList.size(); i++) {
                        vector[i] = vectorList.get(i);
                    }
                }
                String metadata = (String) row.get(VectorDBConstants.FieldNames.METADATA_FIELD);
                result.add(new FaceVector(id, vector, metadata));
            }

            return result;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void releaseCollection(String collectionName) {
        if (!isInit){
            throw new VectorDBException("Milvus未初始化完毕");
        }
        R<RpcStatus> response = serviceClient.releaseCollection(ReleaseCollectionParam.newBuilder()
                .withCollectionName(collectionName)
                .build());

        if (response.getStatus() != R.Status.Success.getCode()) {
            throw new VectorDBException("Milvus releaseCollection失败，msg: " + response.getMessage());
        }
    }


    @Override
    public void loadFaceFeatures() {
        // 加载集合到内存
        R<RpcStatus> loadResponse = serviceClient.loadCollection(LoadCollectionParam.newBuilder()
                .withCollectionName(collectionName)
                .build());
        if (loadResponse.getStatus() != R.Status.Success.getCode()) {
            throw new VectorDBException("Milvus 加载集合到内存失败：" + loadResponse.getMessage());
        }
        long count = count(collectionName);
        log.debug("加载集合到内存成功,人脸数量：{}", count);
    }

    @Override
    public void releaseFaceFeatures() {
        releaseCollection(collectionName);
    }
}
