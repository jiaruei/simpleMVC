package utils.jdbc;

import java.sql.SQLException;
import java.util.List;

import org.apache.log4j.Logger;

public abstract class AbstractJdbcTemplate {
	
private static Logger log = Logger.getLogger(JdbcTemplate.class);
	
	
	public static <T> int insertVo(T bean) throws SQLException{
		return DBUtils.insertVo(bean);
	}

	public static <T> int deleteVo(T bean) throws SQLException{
		return DBUtils.deleteVo(bean);
	}

	public static <T> List<T> retrieveVos(T bean) throws SQLException{
		return DBUtils.retrieveVOos(bean);
	}
}
