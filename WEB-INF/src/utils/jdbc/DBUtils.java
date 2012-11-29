package utils.jdbc;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.ResourceBundle;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.log4j.Logger;

import com.mchange.v2.c3p0.ComboPooledDataSource;

public class DBUtils {

	private static Logger log = Logger.getLogger(DBUtils.class);

	private static ComboPooledDataSource pooledDataSource = null;

	public static final ThreadLocal<Connection> userThreadLocal = new ThreadLocal<Connection>();

	public static void init(Properties config) throws Exception {
		
		if (pooledDataSource == null) {
			pooledDataSource = new ComboPooledDataSource();
			pooledDataSource.setDriverClass(config.getProperty("jdbc.driverClassName"));
			pooledDataSource.setJdbcUrl(config.getProperty("jdbc.url"));
			pooledDataSource.setUser(config.getProperty("jdbc.username"));
			pooledDataSource.setPassword(config.getProperty("jdbc.password"));
			pooledDataSource.setMinPoolSize(NumberUtils.toInt(config.getProperty("jdbc.MinPoolSize"), 5));
			pooledDataSource.setAcquireIncrement(NumberUtils.toInt(config.getProperty("jdbc.setAcquireIncrement"), 5));
			pooledDataSource.setMaxPoolSize(NumberUtils.toInt(config.getProperty("jdbc.MaxPoolSize"), 20));
		}
	}
	
	public static void init(ResourceBundle config) throws Exception {
		
		if (pooledDataSource == null) {
			Properties p = new Properties();
			for (String key : config.keySet()) {
				p.setProperty(key, config.getString(key));
			}
			init(p);
		}
	}
	
	protected static ComboPooledDataSource getPooledDataSource() {
		
		if(pooledDataSource == null){
			throw new IllegalArgumentException("initial datasource config fail ...");
		}
		return pooledDataSource;
	}
	
	protected static Connection getConnection() throws SQLException {

		if (userThreadLocal.get() != null) {
			return userThreadLocal.get();
		} else {
			return pooledDataSource.getConnection();
		}
	}

	/**
	 * ­Y¥]§t¤@³s¦ê operator «h ¤£ close connection
	 * 
	 * @param connection
	 * @throws SQLException
	 */
	protected static void closeConnection(Connection connection) throws SQLException {

		if (userThreadLocal.get() == null) {
			if (connection != null) {
				connection.close();
			}
		}
	}

	private static Map<String, Object> getNotBlankFields(Object bean) {

		Field[] fields = bean.getClass().getDeclaredFields();
		Map<String, Object> map = new LinkedHashMap<String, Object>();

		try {
			for (Field field : fields) {
				String fieldName = field.getName();
				Object value = PropertyUtils.getProperty(bean, fieldName);
				if (value != null) {
					map.put(fieldName, value);
				}
			}
		} catch (Exception e) {
			log.error(e, e);
			throw new RuntimeException(e);
		}
		return map;
	}

	private static java.sql.Date toSqlDate(java.util.Date date) {
		return new java.sql.Date(date.getTime());
	}

	private static java.util.Date toUtilDate(java.sql.Date date) {
		return new java.util.Date(date.getTime());
	}

	private static List<String> getFieldNameList(Object bean) {

		List<String> result = new ArrayList<String>();
		Field[] fields = bean.getClass().getDeclaredFields();
		for (Field field : fields) {
			result.add(field.getName());
		}
		return result;
	}

	private static boolean isUtilDateType(Object bean, String fieldName) {

		Field[] fields = bean.getClass().getDeclaredFields();
		for (Field field : fields) {
			if (field.getName().equals(fieldName)) {
				String typeName = field.getType().getName();
				return (typeName.equals("java.util.Date"));
			}
		}
		return false;
	}

	private static void setParameters(Map<String, Object> fieldMap, PreparedStatement ps) throws SQLException {

		ps.clearParameters();
		String[] columns = (String[]) fieldMap.keySet().toArray(new String[] {});
		for (int i = 0; i < columns.length; i++) {
			Object value = fieldMap.get(columns[i]);

			// translate java.util.Date to java.sql.Date
			if (value instanceof java.util.Date) {
				value = toSqlDate((java.util.Date) value);
			}
			ps.setObject(i + 1, value);
		}
	}

	private static void setParameters(List<Object> valueList, PreparedStatement ps) throws SQLException {

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

	/**
	 * @param fakeSql
	 * @param paramMap 
	 * @return 1. sql 2. parameter keys
	 */
	private static Object[] parseCustomSqlWithParams(String fakeSql, Map<String, Object> paramMap) {

		if (StringUtils.isBlank(fakeSql)) {
			throw new IllegalArgumentException("parameter can't empty ");
		}

		String sql = fakeSql;
		List<String> keyList = new ArrayList<String>();
		List<Object> valueList = new ArrayList<Object>();

		// get key parameter ex: ':column' -> column
		while (fakeSql.length() != 0) {
			fakeSql = StringUtils.substringAfter(fakeSql.trim(), "':");
			// make sure prefix ':
			if (fakeSql.length() > 0) {
				String key = StringUtils.substringBefore(fakeSql, "'");
				keyList.add(key);
				int position = StringUtils.indexOf(fakeSql, "'");
				if (position == -1) {
					throw new RuntimeException("»yªk¤£¥¿½T ¤Ö µ²§À²Å¸¹ ' ");
				}
				fakeSql = StringUtils.substring(fakeSql, position + 1);
			}
		}

		for (String key : keyList) {
			String replace = "':" + key + "'";
			sql = StringUtils.replace(sql, replace, "?");
			valueList.add(paramMap.get(key));
		}
		
		log.debug("parse SQL : " + sql);

		return new Object[] { sql, valueList };
	}

	private static Map<String, Object> createFieldMap(List<String> params, Map<String, Object> paramMap) {

		Map<String, Object> map = new LinkedHashMap<String, Object>();
		for (String key : params) {
			map.put(key, paramMap.get(key));
		}
		return map;
	}

	private static List<Object> createValueList(List<String> params, Map<String, Object> paramMap) {
		
		return createValueList(params.toArray(new String[]{}), paramMap);
	}

	private static List<Object> createValueList(String[] fields, Map<String, Object> fieldMap) {
		
		List<Object> valueList = new ArrayList<Object>();
		for (String parameter : fields) {
			valueList.add(fieldMap.get(parameter));
		}
		return valueList;
	}
	
	
	
	protected static <T> String createSelectSql(T bean) {

		StringBuilder sb = new StringBuilder();

		String tableName = bean.getClass().getSimpleName();
		Map<String, Object> fieldsMap = getNotBlankFields(bean);
		String[] fields = (String[]) fieldsMap.keySet().toArray(new String[] {});

		// create sql
		sb.append("SELECT * FROM ").append(tableName);
		if (fields.length > 0) {
			sb.append(" WHERE ");
			for (int i = 0; i < fields.length; i++) {
				if (i > 0) {
					sb.append(" AND ");
				}
				sb.append(fields[i]).append(" = ").append(" ? ");
			}
		}

		return sb.toString();
	}
	
	protected static <T> List<T> retrieveVos(T bean) throws SQLException {

		List<T> beanList = new ArrayList<T>();

		StringBuilder sb = new StringBuilder();
		String tableName = bean.getClass().getSimpleName();
		sb.append("SELECT * FROM ").append(tableName);

		Map<String, Object> fieldMap = getNotBlankFields(bean);
		String[] fields = (String[]) fieldMap.keySet().toArray(new String[] {});
		if (fields.length > 0) {
			sb.append(" WHERE ");
			for (int i = 0; i < fields.length; i++) {
				if (i > 0) {
					sb.append(" AND ");
				}
				sb.append(fields[i]).append(" = ").append(" ? ");
			}
		}

		String sql = sb.toString();

		log.debug(" SQL : " + sql);

		Connection connection = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		ResultSetMetaData metaData = null;

		try {
			connection = getConnection();
			ps = connection.prepareStatement(sql);
			setParameters(createValueList(fields, fieldMap), ps);

			rs = ps.executeQuery();
			metaData = rs.getMetaData();
			int columnCount = metaData.getColumnCount();

			while (rs.next()) {
				T vo = (T) bean.getClass().newInstance();
				for (int i = 0; i < columnCount; i++) {
					String columnName = metaData.getColumnName(i + 1);
					Object columnValue = rs.getObject(i + 1);
					// find filedName and columnName match
					if (columnValue != null && getFieldNameList(bean).contains(columnName)) {
						if (columnValue instanceof java.sql.Date && isUtilDateType(bean, columnName)) {
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

	protected static <T> int deleteVo(T bean) throws SQLException {

		String tableName = bean.getClass().getSimpleName();
		StringBuilder sb = new StringBuilder("DELETE FROM ").append(tableName);
		Map<String, Object> fieldMap = getNotBlankFields(bean);
		String[] fields = (String[]) fieldMap.keySet().toArray(new String[] {});
		if (fields.length > 0) {
			sb.append(" WHERE ");
			for (int i = 0; i < fields.length; i++) {
				if (i > 0) {
					sb.append(fields[i]).append(" = ").append(" ? ");
				}
			}
		}

		String sql = sb.toString();
		log.debug(" SQL : " + sql);

		Connection connection = null;
		PreparedStatement ps = null;
		int count = 0;
		try {
			connection = getConnection();
			ps = connection.prepareStatement(sql);
			setParameters(createValueList(fields, fieldMap), ps);
			count = ps.executeUpdate();
		} catch (Exception e) {
			log.error(e, e);
			throw new SQLException(e);
		} finally {
			if (ps != null) {
				ps.close();
			}
			closeConnection(connection);
		}
		return count;
	}

	protected static <T> int insertVo(T bean) throws SQLException {

		String tableName = bean.getClass().getSimpleName();
		Map<String, Object> fieldMap = getNotBlankFields(bean);
		String[] fields = (String[]) fieldMap.keySet().toArray(new String[] {});

		if (fields.length == 0) {
			throw new IllegalArgumentException("field value of bean should't all empty ");
		}

		StringBuilder sql1 = new StringBuilder();
		StringBuilder sql2 = new StringBuilder();

		sql1.append(" INSERT INTO ").append(tableName).append(" (");
		sql2.append("VALUES (");

		for (int i = 0; i < fields.length; i++) {
			String column = fields[i];
			sql1.append(column);
			sql2.append("?");
			if (i < fields.length - 1) {
				sql1.append(",");
				sql2.append(",");
			}
		}
		sql1.append(")");
		sql2.append(")");
		String sql = sql1.append(sql2).toString();

		log.debug(" SQL : " + sql.toString());

		Connection connection = null;
		PreparedStatement ps = null;

		try {
			connection = getConnection();
			ps = connection.prepareStatement(sql.toString());
			setParameters(createValueList(fields, fieldMap), ps);
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

	protected static List<Map<String, Object>> retrieveMaps(String sql) throws SQLException {
		return executeSqlToMaps(sql, null);
	}

	protected static List<Map<String, Object>> retrieveMaps(String sql, Object[] values) throws SQLException {

		return executeSqlToMaps(sql, Arrays.asList(values));
	} 

	protected static List<Map<String, Object>> retrieveMaps(String namedParameterSql, Map<String, Object> paramMap) throws SQLException {

		Object[] sqlWithParams = parseCustomSqlWithParams(namedParameterSql,paramMap);
		String sql = (String) sqlWithParams[0];
		List<Object> valueList = (List<Object>) sqlWithParams[1];
		return executeSqlToMaps(sql, valueList);
	}

	private static List<Map<String, Object>> executeSqlToMaps(String sql, List<Object> valueList) throws SQLException {

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

	protected static int executeUpdate(String sql) throws SQLException {

		return executeSqlUpdate(sql,null);
	}
	
	protected static int executeUpdate(String sql, Object[] values) throws SQLException {
		
		return executeSqlUpdate(sql,Arrays.asList(values));
	}
	
	protected static int executeUpdate(String namedParameterSql, Map<String, Object> paramMap) throws SQLException {
		
		Object[] sqlWithParams = parseCustomSqlWithParams(namedParameterSql,paramMap);
		String sql = (String) sqlWithParams[0];
		List<Object> valueList = (List<Object>) sqlWithParams[1];
		
		return executeSqlUpdate(sql,valueList);
	}

	private static int executeSqlUpdate(String sql, List<Object> valueList) throws SQLException {
		
		Connection connection = null;
		PreparedStatement ps = null;
		int count = 0;
		try {
			connection = getConnection();
			ps = connection.prepareStatement(sql);
			setParameters(valueList, ps);
			count = ps.executeUpdate();
		} catch (SQLException e) {
			log.error(e, e);
			throw e;
		} finally {
			if (ps != null) {
				ps.close();
			}
			closeConnection(connection);
		}
		return count;
	}

	

}
