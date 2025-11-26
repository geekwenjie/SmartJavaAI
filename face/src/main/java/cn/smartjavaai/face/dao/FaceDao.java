package cn.smartjavaai.face.dao;

import cn.smartjavaai.face.sqllite.RowMapper;
import cn.smartjavaai.face.sqllite.SqliteHelper;
import cn.smartjavaai.face.utils.VectorUtils;
import cn.smartjavaai.face.vector.entity.FaceVector;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.collections4.CollectionUtils;

import java.lang.reflect.InvocationTargetException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 人脸库持久层
 * @author dwj
 */
@Slf4j
public class FaceDao {

    private static final String FACE_TABLE_NAME = "face";

    private static final String SCHEMA_RESOURCE = "db/schema.sql";

    private static final ConcurrentHashMap<String, FaceDao> INSTANCES = new ConcurrentHashMap<>();

    private final String dbFilePath;

    /**
     * 获取FaceDao实例（单例模式）
     * @param dbFilePath 数据库文件路径
     * @return FaceDao实例
     */
    public static FaceDao getInstance(String dbFilePath) {
        return INSTANCES.computeIfAbsent(dbFilePath, path -> new FaceDao(path));
    }

    /**
     * 私有构造函数
     * @param dbFilePath 数据库文件路径
     */
    private FaceDao(String dbFilePath) {
        this.dbFilePath = dbFilePath;
        try {
            SqliteHelper sqliteHelper = SqliteHelper.getInstance(dbFilePath);
            //自动创建数据库+表
            sqliteHelper.initializeDatabase(FACE_TABLE_NAME, SCHEMA_RESOURCE);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 插入或更新人脸向量
     * @param faceVector 人脸向量
     * @throws SQLException SQL异常
     * @throws ClassNotFoundException 类未找到异常
     */
    public void insertOrUpdate(FaceVector faceVector) throws SQLException, ClassNotFoundException {
        SqliteHelper sqliteHelper = SqliteHelper.getInstance(dbFilePath);
        Map<String, Object> params = new HashMap<>();
        params.put("id", faceVector.getId());
        params.put("vector", VectorUtils.toByteArray(faceVector.getVector()));
        params.put("metadata", faceVector.getMetadata());
        sqliteHelper.executeInsertOrUpdate(FACE_TABLE_NAME, params);
    }

    /**
     * 使用index查询key
     * @param id 人脸ID
     * @return 人脸向量
     * @throws SQLException SQL异常
     * @throws ClassNotFoundException 类未找到异常
     */
    public FaceVector findById(String id) throws SQLException, ClassNotFoundException {
        SqliteHelper sqliteHelper = SqliteHelper.getInstance(dbFilePath);
        String sql = "select \"id\",\"vector\",\"metadata\" from " + FACE_TABLE_NAME + " where \"id\" = '" + id + "'";
        List<FaceVector> faceVectors = sqliteHelper.executeQuery(sql, new RowMapper<FaceVector>() {
            @Override
            public FaceVector mapRow(ResultSet rs, int id) throws SQLException {
                FaceVector face = new FaceVector();
                face.setId(rs.getString("id"));
                face.setVector(VectorUtils.toFloatArray(rs.getBytes("vector")));
                face.setMetadata(rs.getString("metadata"));
                return face;
            }
        });
        return CollectionUtils.isNotEmpty(faceVectors) ? faceVectors.get(0) : null;
    }

    /**
     * 删除全部
     * @return 删除的行数
     * @throws SQLException SQL异常
     * @throws ClassNotFoundException 类未找到异常
     */
    public long deleteAll() throws SQLException, ClassNotFoundException {
        SqliteHelper sqliteHelper = SqliteHelper.getInstance(dbFilePath);
        long rows = sqliteHelper.executeUpdate("delete from " + FACE_TABLE_NAME);
        return rows;
    }

    /**
     * 使用id数组查询
     * @param ids ID数组
     * @return 人脸向量列表
     * @throws SQLException SQL异常
     * @throws ClassNotFoundException 类未找到异常
     */
    public List<FaceVector> findByIds(String... ids) throws SQLException, ClassNotFoundException {
        // 使用 Stream API
        String inKeys = Arrays.stream(ids)
                .map(s -> "'" + s + "'")
                .reduce((s1, s2) -> s1 + "," + s2)
                .orElse("");
        String sql = "select \"id\",\"vector\",\"metadata\" from " + FACE_TABLE_NAME + " where \"id\" in (" + inKeys + ")";
        log.debug("sql：{}", sql);
        SqliteHelper sqliteHelper = SqliteHelper.getInstance(dbFilePath);
        List<FaceVector> faceVectors = sqliteHelper.executeQuery(sql, new RowMapper<FaceVector>() {
            @Override
            public FaceVector mapRow(ResultSet rs, int id) throws SQLException {
                FaceVector face = new FaceVector();
                face.setId(rs.getString("id"));
                face.setVector(VectorUtils.toFloatArray(rs.getBytes("vector")));
                face.setMetadata(rs.getString("metadata"));
                return face;
            }
        });
        return faceVectors;
    }

    /**
     * 删除人脸
     * @param ids ID数组
     * @return 是否成功
     * @throws SQLException SQL异常
     * @throws ClassNotFoundException 类未找到异常
     */
    public boolean deleteFace(String... ids) throws SQLException, ClassNotFoundException {
        String inKeys = Arrays.stream(ids)
                .map(s -> "'" + s + "'")
                .reduce((s1, s2) -> s1 + "," + s2)
                .orElse("");

        SqliteHelper sqliteHelper = SqliteHelper.getInstance(dbFilePath);
        String sql = "delete from " + FACE_TABLE_NAME + " where \"id\" in (" + inKeys + ")";
        int rows = sqliteHelper.executeUpdate(sql);
        log.debug("删除了{}行数据", rows);
        return rows == ids.length;
    }

    /**
     * 分页查询人脸
     * @param pageNo 页码
     * @param pageSize 每页大小
     * @return 人脸向量列表
     * @throws SQLException SQL异常
     * @throws ClassNotFoundException 类未找到异常
     */
    public List<FaceVector> findFace(int pageNo, int pageSize) throws SQLException, ClassNotFoundException {
        long offset = (pageNo - 1) * pageSize;
        String sql = "select \"id\",\"vector\",\"metadata\" from " + FACE_TABLE_NAME +
                " limit " + offset + "," + pageSize;
        SqliteHelper sqliteHelper = SqliteHelper.getInstance(dbFilePath);
        return sqliteHelper.executeQuery(sql, new RowMapper<FaceVector>() {
            @Override
            public FaceVector mapRow(ResultSet rs, int id) throws SQLException {
                FaceVector face = new FaceVector();
                face.setId(rs.getString("id"));
                face.setVector(VectorUtils.toFloatArray(rs.getBytes("vector")));
                face.setMetadata(rs.getString("metadata"));
                return face;
            }
        });
    }

    /**
     * 关闭所有实例
     */
    public static void closeAll() {
        INSTANCES.clear();
        log.debug("所有FaceDao实例已关闭");
    }
}
