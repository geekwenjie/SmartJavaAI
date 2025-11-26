package cn.smartjavaai.face.sqllite;


import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * RowMapper
 * @author dwj
 * @param <T>
 */
public interface RowMapper<T> {
    public abstract T mapRow(ResultSet rs, int index) throws SQLException;
}
