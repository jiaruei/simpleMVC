package utils.jdbc;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public interface SqlOperator {
	
	public void beginTransaction();

	public void commitTransaction();

	public void rollbackTransaction();
	
	public <T> int insertVo(T bean) throws SQLException;

	public <T> List<T> retrieveVos(T bean) throws SQLException;

	public <T> int deleteVo(T bean) throws SQLException;

	public List<Map<String, Object>> retrieveMaps(String sql) throws SQLException;

	public List<Map<String, Object>> retrieveMaps(String sql, Object[] values) throws SQLException;

	public List<Map<String, Object>> retrieveMaps(String namedParameterSql, Map<String, Object> valuesMap) throws SQLException;

	public int executeUpdate(String sql) throws SQLException;

	public int executeUpdate(String sql, Object[] values) throws SQLException;

	public int executeUpdate(String namedParameterSql, Map<String, Object> valuesMap) throws SQLException;
}
