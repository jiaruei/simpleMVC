package utils.jdbc;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import org.apache.log4j.Logger;

public class JdbcTemplate {
	
	private static Logger log = Logger.getLogger(JdbcTemplate.class);
	
	public JdbcTemplate() {
	}

	public static void initialConfig(String rootConfigFileName){
	
		ResourceBundle config = ResourceBundle.getBundle(rootConfigFileName);
		try {
			DBUtils.init(config);
		} catch (Exception e) {
			log.error(e,e);
			throw new RuntimeException(e);
		}
	}
	
	public static <T> List<T> retrieveVos(T bean) throws SQLException{
		return DBUtils.retrieveVOos(bean);
	}

	public static <T> int insertVo(T bean) throws SQLException{
		return DBUtils.insertVo(bean);
	}

	public static <T> int deleteVo(T bean) throws SQLException{
		return DBUtils.deleteVo(bean);
	}

	
	public static List<Map<String, Object>> retrieveMaps(String sql) throws SQLException {
		return DBUtils.retrieveMaps(sql);
	}
	
	public static List<Map<String, Object>> retrieveMaps(String sql, Object[] values) throws SQLException {
		return DBUtils.retrieveMaps(sql, values);
	}

	public static List<Map<String, Object>> retrieveMaps(String fakeSql, Map<String, Object> paramMap) throws SQLException {		
		return DBUtils.retrieveMaps(fakeSql, paramMap);
	}

}
