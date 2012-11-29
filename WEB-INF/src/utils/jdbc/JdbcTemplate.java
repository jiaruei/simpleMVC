package utils.jdbc;

import java.beans.PropertyVetoException;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.mchange.v2.c3p0.ComboPooledDataSource;

public class JdbcTemplate implements SqlOperator {

	private static Logger log = Logger.getLogger(JdbcTemplate.class);

	private static final ThreadLocal<Connection> connectionThreadLocal = new ThreadLocal<Connection>();

	private ComboPooledDataSource pooledDataSource;

	public JdbcTemplate() {
		pooledDataSource = DBUtils.getPooledDataSource();
	}

	public JdbcTemplate(String driverClass, String url, String userId, String password) {

		pooledDataSource = new ComboPooledDataSource();
		try {
			pooledDataSource.setDriverClass(driverClass);
			pooledDataSource.setJdbcUrl(url);
			pooledDataSource.setUser(userId);
			pooledDataSource.setPassword(password);
			pooledDataSource.setMinPoolSize(5);
			pooledDataSource.setAcquireIncrement(5);
			pooledDataSource.setMaxPoolSize(30);
		} catch (PropertyVetoException e) {
			log.error(e, e);
			throw new RuntimeException(e);
		}
	}

	private void setParameters(List<Object> valueList, PreparedStatement ps) throws SQLException {

		ps.clearParameters();

		if (valueList != null && !valueList.isEmpty()) {
			for (int i = 0; i < valueList.size(); i++) {
				Object value = valueList.get(i);
				// translate java.util.Date to java.sql.Date
				if (value instanceof java.util.Date) {
					value = toSqlDate((java.util.Date) value);
				}
				ps.setObject(i + 1, value);
			}
		}
	}

	private Connection getConnection() throws SQLException {

		if (connectionThreadLocal.get() != null) {
			return connectionThreadLocal.get();
		} else {
			return pooledDataSource.getConnection();
		}
	}

	private void closeConnection(Connection connection) throws SQLException {

		if (connectionThreadLocal.get() == null) {
			if (connection != null) {
				connection.close();
			}
		}
	}

	private <T> boolean isJavaUtilDateType(T bean, String fieldName) {

		Field[] fields = bean.getClass().getDeclaredFields();
		for (Field field : fields) {
			if (field.getName().equals(fieldName)) {
				String typeName = field.getType().getName();
				return (typeName.equals("java.util.Date"));
			}
		}
		return false;
	}

	private static java.sql.Date toSqlDate(java.util.Date date) {
		return new java.sql.Date(date.getTime());
	}

	private static java.util.Date toUtilDate(java.sql.Date date) {
		return new java.util.Date(date.getTime());
	}

	private List<Map<String, Object>> executeSqlToMaps(String sql, List<Object> valueList) throws SQLException {

		List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();

		Connection connection = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		ResultSetMetaData metaData = null;

		try {
			connection = getConnection();
			ps = connection.prepareStatement(sql);
			setParameters(valueList, ps);
			rs = ps.executeQuery();

			metaData = rs.getMetaData();
			int columnCount = metaData.getColumnCount();

			while (rs.next()) {
				Map<String, Object> map = new HashMap<String, Object>();

				for (int i = 0; i < columnCount; i++) {
					String columnName = metaData.getColumnName(i + 1);
					Object columnValue = rs.getObject(i + 1);

					if (columnValue != null && columnValue instanceof java.sql.Date) {
						columnValue = toUtilDate((java.sql.Date) columnValue);
					}

					map.put(columnName, columnValue);
				}
				result.add(map);
			}
		} catch (SQLException e) {
			log.error(e, e);
			throw e;
		} finally {
			if (rs != null) {
				rs.close();
			}
			if (ps != null) {
				ps.close();
			}
			closeConnection(connection);
		}

		return result;
	}

	@Override
	public <T> int insertVo(T bean) throws SQLException {

		List<String> allFields = new ArrayList<String>();
		List<String> notBlankFields = new ArrayList<String>();
		List<Object> notBlankValueList = new ArrayList<Object>();

		Field[] fields = bean.getClass().getDeclaredFields();
		try {
			for (Field field : fields) {
				String fieldName = field.getName();
				allFields.add(fieldName);
				Object value = PropertyUtils.getProperty(bean, fieldName);
				if (value != null && StringUtils.isNotBlank(ObjectUtils.toString(value))) {
					notBlankFields.add(fieldName);
					notBlankValueList.add(value);
				}
			}
		} catch (Exception e) {
			log.error(e, e);
			throw new RuntimeException(e);
		}

		if (notBlankFields.size() == 0) {
			throw new IllegalArgumentException("value of bean field should't all blank ");
		}

		String sql = DBUtils2.createInsertSql(bean.getClass().getSimpleName(), notBlankFields);

		log.debug(sql);

		Connection connection = null;
		PreparedStatement ps = null;

		try {
			connection = getConnection();
			ps = connection.prepareStatement(sql.toString());
			setParameters(notBlankValueList, ps);
			return ps.executeUpdate();

		} catch (SQLException e) {
			log.error(e, e);
			throw e;
		} finally {
			if (ps != null) {
				ps.close();
			}
			closeConnection(connection);
		}
	}

	@Override
	public <T> List<T> retrieveVos(T bean) throws SQLException {

		List<T> beanList = new ArrayList<T>();

		List<String> allFields = new ArrayList<String>();
		List<String> notBlankFields = new ArrayList<String>();
		List<Object> notBlankValueList = new ArrayList<Object>();

		Field[] fields = bean.getClass().getDeclaredFields();
		try {
			for (Field field : fields) {
				String fieldName = field.getName();
				allFields.add(fieldName);
				Object value = PropertyUtils.getProperty(bean, fieldName);
				if (value != null && StringUtils.isNotBlank(ObjectUtils.toString(value))) {
					notBlankFields.add(fieldName);
					notBlankValueList.add(value);
				}
			}
		} catch (Exception e) {
			log.error(e, e);
			throw new RuntimeException(e);
		}

		String sql = DBUtils2.createSelectSql(bean.getClass().getSimpleName(), notBlankFields);

		log.debug(sql);

		Connection connection = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		ResultSetMetaData metaData = null;

		try {
			connection = getConnection();
			ps = connection.prepareStatement(sql);
			setParameters(notBlankValueList, ps);
			rs = ps.executeQuery();
			metaData = rs.getMetaData();

			while (rs.next()) {
				T vo = (T) bean.getClass().newInstance();
				for (int i = 0; i < metaData.getColumnCount(); i++) {
					String columnName = metaData.getColumnName(i + 1);
					Object columnValue = rs.getObject(i + 1);
					// find filedName and columnName match
					if (columnValue != null && allFields.contains(columnName)) {
						// if column Value is java.sql.Date and VO mapping field
						// type is java.util.Date
						if (columnValue instanceof java.sql.Date && isJavaUtilDateType(bean, columnName)) {
							columnValue = toUtilDate((java.sql.Date) columnValue);
						}
						PropertyUtils.setProperty(vo, columnName, columnValue);
					}
				}
				beanList.add(vo);
			}

		} catch (Exception e) {
			log.error(e, e);
			throw new SQLException(e);
		} finally {
			if (rs != null) {
				rs.close();
			}
			if (ps != null) {
				ps.close();
			}
			closeConnection(connection);
		}

		return beanList;
	}

	@Override
	public <T> int deleteVo(T bean) throws SQLException {

		List<String> allFields = new ArrayList<String>();
		List<String> notBlankFields = new ArrayList<String>();
		List<Object> notBlankValueList = new ArrayList<Object>();

		Field[] fields = bean.getClass().getDeclaredFields();
		try {
			for (Field field : fields) {
				String fieldName = field.getName();
				allFields.add(fieldName);
				Object value = PropertyUtils.getProperty(bean, fieldName);
				if (value != null && StringUtils.isNotBlank(ObjectUtils.toString(value))) {
					notBlankFields.add(fieldName);
					notBlankValueList.add(value);
				}
			}
		} catch (Exception e) {
			log.error(e, e);
			throw new RuntimeException(e);
		}

		// if (notBlankFields.size() == 0) {
		// throw new
		// IllegalArgumentException("value of bean field should't all blank ");
		// }

		String sql = DBUtils2.createDeleteSql(bean.getClass().getSimpleName(), notBlankFields);

		log.debug(sql);

		Connection connection = null;
		PreparedStatement ps = null;

		try {
			connection = getConnection();
			ps = connection.prepareStatement(sql.toString());
			setParameters(notBlankValueList, ps);
			return ps.executeUpdate();

		} catch (SQLException e) {
			log.error(e, e);
			throw e;
		} finally {
			if (ps != null) {
				ps.close();
			}
			closeConnection(connection);
		}
	}

	@Override
	public List<Map<String, Object>> retrieveMaps(String sql) throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Map<String, Object>> retrieveMaps(String sql, Object[] values) throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Map<String, Object>> retrieveMaps(String namedParameterSql, Map<String, Object> valuesMap) throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int executeUpdate(String sql) throws SQLException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int executeUpdate(String sql, Object[] values) throws SQLException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int executeUpdate(String namedParameterSql, Map<String, Object> valuesMap) throws SQLException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void beginTransaction() {
		// TODO Auto-generated method stub

	}

	@Override
	public void commitTransaction() {
		// TODO Auto-generated method stub

	}

	@Override
	public void rollbackTransaction() {
		// TODO Auto-generated method stub

	}

}
