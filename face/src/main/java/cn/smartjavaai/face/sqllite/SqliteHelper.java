package cn.smartjavaai.face.sqllite;

import cn.hutool.core.io.resource.ResourceUtil;
import lombok.extern.slf4j.Slf4j;
import org.sqlite.SQLiteConfig;
import org.sqlite.SQLiteDataSource;

import javax.sql.DataSource;
import java.io.File;
import java.lang.reflect.Field;
import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * sqlite帮助类
 * @author dwj
 */
@Slf4j
public class SqliteHelper {

    private static final ConcurrentHashMap<String, SqliteHelper> INSTANCES = new ConcurrentHashMap<>();
    private static final int MAX_CONNECTIONS = 10; // 最大连接数

    private final String dbFilePath;
    private final DataSource dataSource;

    /**
     * 获取SqliteHelper实例（单例模式）
     * @param dbFilePath sqlite db 文件路径
     * @return SqliteHelper实例
     * @throws SQLException SQL异常
     * @throws ClassNotFoundException 类未找到异常
     */
    public static SqliteHelper getInstance(String dbFilePath) throws SQLException, ClassNotFoundException {
        return INSTANCES.computeIfAbsent(dbFilePath, path -> {
            try {
                return new SqliteHelper(path);
            } catch (Exception e) {
                log.error("创建SqliteHelper实例失败", e);
                throw new RuntimeException("创建SqliteHelper实例失败", e);
            }
        });
    }

    /**
     * 私有构造函数
     * @param dbFilePath sqlite db 文件路径
     * @throws ClassNotFoundException 类未找到异常
     * @throws SQLException SQL异常
     */
    private SqliteHelper(String dbFilePath) throws ClassNotFoundException, SQLException {
        this.dbFilePath = dbFilePath;
        createDatabaseIfNotExists();

        // 初始化连接池
        SQLiteDataSource sqLiteDataSource = new SQLiteDataSource();
        sqLiteDataSource.setUrl("jdbc:sqlite:" + dbFilePath);

        // 配置SQLite连接
        SQLiteConfig sqLiteConfig = new SQLiteConfig();
        sqLiteConfig.setSharedCache(true);
        sqLiteConfig.enableLoadExtension(true);
        sqLiteConfig.setBusyTimeout(5000); // 5秒超时
        sqLiteDataSource.setConfig(sqLiteConfig);

        this.dataSource = sqLiteDataSource;
    }

    /**
     * 获取数据库连接
     * @return 数据库连接
     * @throws SQLException SQL异常
     */
    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    /**
     * 执行sql查询
     * @param sql sql select 语句
     * @param rse 结果集处理类对象
     * @return 查询结果
     * @throws SQLException SQL异常
     */
    public <T> T executeQuery(String sql, ResultSetExtractor<T> rse) throws SQLException {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            return rse.extractData(rs);
        }
    }

    /**
     * 执行select查询，返回结果列表
     * @param sql sql select 语句
     * @param rm 结果集的行数据处理类对象
     * @return 查询结果列表
     * @throws SQLException SQL异常
     */
    public <T> List<T> executeQuery(String sql, RowMapper<T> rm) throws SQLException {
        List<T> rsList = new ArrayList<>();
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                rsList.add(rm.mapRow(rs, rs.getRow()));
            }
            return rsList;
        }
    }

    /**
     * 简单查询某个字段
     * @param sql SQL查询语句
     * @return 查询结果
     * @throws SQLException SQL异常
     */
    public String executeQuery(String sql) throws SQLException {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getString(1);
            }
            return null;
        }
    }

    /**
     * 执行数据库更新sql语句
     * @param sql SQL更新语句
     * @return 更新行数
     * @throws SQLException SQL异常
     */
    public int executeUpdate(String sql) throws SQLException {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            return stmt.executeUpdate(sql);
        }
    }

    /**
     * 执行多个sql更新语句
     * @param sqls SQL更新语句数组
     * @throws SQLException SQL异常
     */
    public void executeUpdate(String... sqls) throws SQLException {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            for (String sql : sqls) {
                stmt.executeUpdate(sql);
            }
        }
    }

    /**
     * 执行数据库更新 sql List
     * @param sqls sql列表
     * @throws SQLException SQL异常
     */
    public void executeUpdate(List<String> sqls) throws SQLException {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            for (String sql : sqls) {
                stmt.executeUpdate(sql);
            }
        }
    }

    /**
     * 执行select查询，返回结果列表
     * @param sql sql select 语句
     * @param clazz 实体泛型
     * @return 实体集合
     * @throws SQLException 异常信息
     * @throws IllegalAccessException 非法访问异常
     * @throws InstantiationException 实例化异常
     */
    public <T> List<T> executeQueryList(String sql, Class<T> clazz) throws SQLException, IllegalAccessException, InstantiationException {
        List<T> rsList = new ArrayList<>();
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                T t = clazz.newInstance();
                for (Field field : t.getClass().getDeclaredFields()) {
                    field.setAccessible(true);
                    field.set(t, rs.getObject(field.getName()));
                }
                rsList.add(t);
            }
            return rsList;
        }
    }

    /**
     * 执行sql查询,适用单条结果集
     * @param sql sql select 语句
     * @param clazz 结果集处理类对象
     * @return 查询结果
     * @throws SQLException SQL异常
     * @throws IllegalAccessException 非法访问异常
     * @throws InstantiationException 实例化异常
     */
    public <T> T executeQuery(String sql, Class<T> clazz) throws SQLException, IllegalAccessException, InstantiationException {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                T t = clazz.newInstance();
                for (Field field : t.getClass().getDeclaredFields()) {
                    field.setAccessible(true);
                    field.set(t, rs.getObject(field.getName()));
                }
                return t;
            }
            return null;
        }
    }

    /**
     * 执行数据库更新sql语句
     * @param tableName 表名
     * @param param key-value键值对,key:表中字段名,value:值
     * @return 更新行数
     * @throws SQLException SQL异常
     */
    public int executeInsertOrUpdate(String tableName, Map<String, Object> param) throws SQLException {
        try (Connection conn = getConnection()) {
            // 保证字段和值顺序一致
            List<String> keys = new ArrayList<>(param.keySet());

            StringBuilder sql = new StringBuilder();
            sql.append("INSERT OR REPLACE INTO ");
            sql.append(tableName);
            sql.append(" (");

            for (String key : keys) {
                sql.append(key).append(",");
            }
            sql.deleteCharAt(sql.length() - 1);
            sql.append(") VALUES (");
            for (int i = 0; i < keys.size(); i++) {
                sql.append("?,");
            }
            sql.deleteCharAt(sql.length() - 1);
            sql.append(");");

            log.debug("sql: {}", sql.toString());

            try (PreparedStatement pstmt = conn.prepareStatement(sql.toString())) {
                for (int i = 0; i < keys.size(); i++) {
                    Object value = param.get(keys.get(i));
                    if (value instanceof byte[]) {
                        pstmt.setBytes(i + 1, (byte[]) value);
                    } else {
                        pstmt.setObject(i + 1, value);
                    }
                }
                return pstmt.executeUpdate();
            }
        }
    }


    /**
     * 使用预编译语句执行更新
     * @param sql SQL语句
     * @param args 参数
     * @return 更新行数
     * @throws SQLException SQL异常
     */
    public int executeUpdate(String sql, Object[] args) throws SQLException {
        try (Connection conn = getConnection()) {
            if (args == null || args.length == 0) {
                try (Statement stmt = conn.createStatement()) {
                    return stmt.executeUpdate(sql);
                }
            } else {
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    for (int i = 0; i < args.length; i++) {
                        stmt.setObject(i + 1, args[i]);
                    }
                    return stmt.executeUpdate();
                }
            }
        }
    }

    /**
     * 创建数据库文件（如果不存在）
     */
    private void createDatabaseIfNotExists() {
        File dbFile = new File(dbFilePath);
        if (!dbFile.exists()) {
            try {
                // 创建数据库文件
                dbFile.getParentFile().mkdirs(); // 创建父目录
                dbFile.createNewFile();
                log.debug("Created new SQLite database file: {}", dbFilePath);
            } catch (Exception e) {
                log.error("Failed to create database file: {}", dbFilePath, e);
                throw new RuntimeException("Database file creation failed", e);
            }
        }
    }

    /**
     * 初始化数据库表结构
     */
    public void initializeDatabase(String tableName, String schemaResourcePath) throws SQLException {
        // 检查表是否存在
        if (isTableExists(tableName)) {
            log.debug("Database table already exists");
            return;
        }

        log.debug("Creating database tables...");

        // 使用 Hutool 读取 SQL 资源文件
        List<String> sqlStatements = readSqlResource(schemaResourcePath);

        // 执行所有 SQL 语句
        for (String sql : sqlStatements) {
            if (!sql.trim().isEmpty()) {
                executeUpdate(sql);
            }
        }
        log.debug("Database tables created successfully");
    }

    /**
     * 检查表是否存在
     */
    private boolean isTableExists(String tableName) throws SQLException {
        try (Connection conn = getConnection()) {
            ResultSet rs = conn.getMetaData().getTables(null, null, tableName, null);
            return rs.next();
        } catch (SQLException e) {
            log.warn("Error checking table existence: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 使用 Hutool 读取 SQL 资源文件并分割为语句列表
     */
    private List<String> readSqlResource(String resourcePath) {
        try {
            // 读取整个资源文件内容
            String content = ResourceUtil.readUtf8Str(resourcePath);

            // 分割 SQL 语句（按分号分割）
            return Arrays.stream(content.split(";"))
                    .map(String::trim)
                    .filter(sql -> !sql.isEmpty())
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Failed to read SQL resource: {}", resourcePath, e);
            throw new RuntimeException("SQL resource read error", e);
        }
    }

    /**
     * 关闭所有连接池
     */
    public static void closeAll() {
        INSTANCES.clear();
        log.debug("所有SqliteHelper实例已关闭");
    }
}
