package cn.smartjavaai.face.sqllite;


import java.sql.ResultSet;

/**
 * ResultSetExtractor
 * @author dwj
 * @param <T>
 */
public interface ResultSetExtractor<T> {

    public abstract T extractData(ResultSet rs);

}
