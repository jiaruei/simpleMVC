package utils.jdbc;

import java.util.List;
import java.util.Map;

public interface SqlOperator {
	
	public <T> int insertVo(T bean);

	public <T> List<T> retrieveVos(T bean);

	public <T> int deleteVo(T bean);

	public List<Map<String, Object>> retrieveMaps(String sql);
	
	public List<Map<String, Object>> retrieveMaps(String sql, Object[] values);
	
	public List<Map<String, Object>> retrieveMaps(String namedParameterSql, Map<String, Object> valuesMap);
	
	public int executeUpdate(String sql);
	
	public int executeUpdate(String sql, Object[] values);

	public int executeUpdate(String namedParameterSql, Map<String, Object> valuesMap);
}
