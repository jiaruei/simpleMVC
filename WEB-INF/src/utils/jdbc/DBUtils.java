package utils.jdbc;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.ResourceBundle;

import org.apache.commons.beanutils.MethodUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.log4j.Logger;

import com.mchange.v2.c3p0.ComboPooledDataSource;

public class DBUtils {

	private static Logger log = Logger.getLogger(DBUtils.class);

	private static ComboPooledDataSource cpds = null;

	public static final ThreadLocal<Connection> userThreadLocal = new ThreadLocal<Connection>();

	public static void init(Properties config) throws Exception {

		if (cpds == null) {
			cpds = new ComboPooledDataSource();
			cpds.setDriverClass(config.getProperty("jdbc.driverClassName"));
			cpds.setJdbcUrl(config.getProperty("jdbc.url"));
			cpds.setUser(config.getProperty("jdbc.username"));
			cpds.setPassword(config.getProperty("jdbc.password"));
			cpds.setMinPoolSize(NumberUtils.toInt(config.getProperty("jdbc.MinPoolSize"), 5));
			cpds.setAcquireIncrement(NumberUtils.toInt(config.getProperty("jdbc.setAcquireIncrement"), 5));
			cpds.setMaxPoolSize(NumberUtils.toInt(config.getProperty("jdbc.MaxPoolSize"), 20));
		}
	}

	public static void init(ResourceBundle config) throws Exception {

		if (cpds == null) {
			Properties p = new Properties();
			for (String key : config.keySet()) {
				p.setProperty(key, config.getString(key));
			}
			init(p);
		}
	}

	public static void beginTransaction() {

		if (userThreadLocal.get() != null) {
			userThreadLocal.set(null);
		}
		log.debug("Starting new database transaction in this thread.");

		Connection currentConnection;
		try {
			currentConnection = cpds.getConnection();
			currentConnection.setAutoCommit(false);
			userThreadLocal.set(currentConnection);
		} catch (SQLException e) {
			log.error(e, e);
			throw new RuntimeException(e);
		}
	}

	public static void commitTransaction() {

		if (userThreadLocal.get() != null) {
			Connection currentConnection = userThreadLocal.get();
			try {
				currentConnection.commit();
				log.debug("commit database transaction in this thread.");
			} catch (SQLException e) {
				log.error(e, e);
				throw new RuntimeException(e);
			} finally {
				userThreadLocal.set(null);
				try {
					currentConnection.rollback();
					currentConnection.close();
				} catch (SQLException e) {
					log.error(e, e);
					throw new RuntimeException(e);
				}
			}
		}
	}

	public static void rollbackTransaction() {

		if (userThreadLocal.get() != null) {
			Connection currentConnection = userThreadLocal.get();
			try {
				currentConnection.rollback();
				log.debug("rollback database transaction in this thread.");
			} catch (SQLException e) {
				log.error(e, e);
				throw new RuntimeException(e);
			} finally {
				userThreadLocal.set(null);
				try {
					currentConnection.close();
				} catch (SQLException e) {
					log.error(e, e);
					throw new RuntimeException(e);
				}
			}
		}
	}

	public static Connection getConnection() throws SQLException {

		if (userThreadLocal.get() != null) {
			return userThreadLocal.get();
		} else {
			return cpds.getConnection();
		}
	}

	private static Map<String, Object> getNotBlankFields(Object bean) {

		Field[] fields = bean.getClass().getDeclaredFields();
		Map<String, Object> map = new LinkedHashMap<String, Object>();

		try {
			for (Field field : fields) {
				String column = field.getName();
				String dataType = field.getType().getSimpleName();

				String methodName = "get" + column.substring(0, 1).toUpperCase() + column.substring(1);
				Object value = MethodUtils.invokeMethod(bean, methodName, null);
				if (value != null) {
					map.put(column, new Object[] { dataType, value });
				}
			}
		} catch (Exception e) {
			log.error(e, e);
			throw new RuntimeException(e);
		}
		return map;
	}

	private static String getTableName(Object bean) {
		return bean.getClass().getSimpleName();
	}
	
	
	
	public static int updateSQL(String fakeSql, Map<String, Object> paramMap) throws SQLException {
		
		// parse fakeSQL
		Object[] sqlWithParams = parseCustomSqlWithParams(fakeSql);
		String sql = (String) sqlWithParams[0];
		List<String> params = (List<String>) sqlWithParams[1];
		
		Connection connection = null;
		PreparedStatement pre = null;
		ResultSet rs = null;

		try {
			connection = getConnection();
			pre = connection.prepareStatement(sql);
		}catch(SQLException e){
			log.error(e,e);
			throw e;
		}finally{
			if(!pre.isClosed()){
				pre.close();
			}
			closeConnection(connection);
		}
		return 0;
	}
	public static List<Map<String, Object>> retrieveMaps(String fakeSql, Map<String, Object> paramMap) throws SQLException {

		Object[] sqlWithParams = parseCustomSqlWithParams(fakeSql);
		String sql = (String) sqlWithParams[0];
		List<String> params = (List<String>) sqlWithParams[1];
		List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();

		Connection connection = null;
		PreparedStatement pre = null;
		ResultSet rs = null;

		try {
			connection = getConnection();
			pre = connection.prepareStatement(sql);

			for (int i = 0; i < params.size(); i++) {
				pre.setObject(i + 1, paramMap.get(params.get(i)));
			}
			rs = pre.executeQuery();

			ResultSetMetaData metaData = rs.getMetaData();
			int columnCount = metaData.getColumnCount();

			while (rs.next()) {
				Map<String, Object> map = new HashMap<String, Object>();
				for (int i = 0; i < columnCount; i++) {
					String columnName = metaData.getColumnName(i + 1);
					Object columnValue = rs.getObject(i + 1);
					
					if (columnValue != null) {
						if (columnValue instanceof java.sql.Date) {
							java.sql.Date utilDate = (java.sql.Date) columnValue;
							columnValue = new java.util.Date(utilDate.getTime());
						}
					}
					map.put(columnName, columnValue);
				}
				result.add(map);
			}
			connection.close();
		} catch (SQLException e) {
			log.error(e, e);
			throw e;
		} finally {
			
			if(rs.isClosed()){
				rs.close();				
			}
			if(pre.isClosed()){
				pre.close();				
			}
			closeConnection(connection);
		}

		return result;
	}

	public static <T> List<T> retrieveVOs(T bean) throws SQLException {

		String tableName = getTableName(bean);
		Map<String, Object> map = getNotBlankFields(bean);
		String[] columns = (String[]) map.keySet().toArray(new String[] {});

		StringBuilder sql = new StringBuilder("SELECT * FROM ").append(tableName);
		// append condition 
		if (columns.length > 0) {
			sql.append(" WHERE ");
			for (int i = 0; i < columns.length; i++) {
				if (i > 0) {
					sql.append(" AND ");
				}
				sql.append(columns[i]).append(" = ").append(" ? ");
			}
		}

		log.debug("SELECT SQL : " + sql.toString());

		Connection connection = null;
		PreparedStatement pre = null;
		ResultSet rs = null;
		
		List<T> list = new ArrayList<T>();

		try {
			connection = getConnection();
			pre = connection.prepareStatement(sql.toString());
			pre.clearParameters();
			setParameters(map, columns, pre);

			rs = pre.executeQuery();
			ResultSetMetaData metaData = rs.getMetaData();
			int columnCount = metaData.getColumnCount();

			while (rs.next()) {
				try {
					T vo = (T) bean.getClass().newInstance();
					Field[] fields = bean.getClass().getDeclaredFields();

					for (int i = 0; i < columnCount; i++) {
						String columnName = metaData.getColumnName(i + 1);
						Object columnValue = rs.getObject(i + 1);
						// make sure column has value
						if (columnValue != null) {
							if (columnValue instanceof java.sql.Date) {
								java.sql.Date utilDate = (java.sql.Date) columnValue;
								columnValue = new java.util.Date(utilDate.getTime());
							}
							// make sure column name and VO filed have same name
							String fieldName = getSetterMethod(fields, columnName);

							if (StringUtils.isNotBlank(fieldName)) {
								// inject value to the vo 
								MethodUtils.invokeMethod(vo, fieldName, columnValue);
							}
						}
					}
					
					list.add(vo);
				} catch (Exception e) {
					e.printStackTrace();
					log.error(e, e);
					throw new SQLException(e);
				}
			}

		} catch (SQLException e) {
			log.error(e, e);
			throw e;
		} finally {
			if(rs.isClosed()){
				rs.close();				
			}
			if(pre.isClosed()){
				pre.close();				
			}
			closeConnection(connection);
		}
		return list;
	}
	

	private static String getSetterMethod(Field[] fields, String columnName) {

		for (Field field : fields) {
			String fieldName = field.getName();
			if ((fieldName.equalsIgnoreCase(columnName))) {
				return "set" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
			}
		}

		return null;
	}

	/**
	 * @param bean
	 * @return
	 * @throws SQLException
	 */
	public static int insertVO(Object bean) throws SQLException {

		String tableName = getTableName(bean);
		Map<String, Object> map = getNotBlankFields(bean);

		// create insert SQL
		String[] columns = (String[]) map.keySet().toArray(new String[] {});
		if(columns.length == 0){
			throw new IllegalArgumentException("value of bean fields should't all empty ");
		}
		StringBuilder sql = new StringBuilder();
		StringBuilder sql1 = new StringBuilder();
		StringBuilder sql2 = new StringBuilder();
		sql1.append(" INSERT INTO ").append(tableName).append(" (");
		sql2.append("VALUES (");

		for (int i = 0; i < columns.length; i++) {
			String column = columns[i];
			sql1.append(column);
			sql2.append("?");
			if (i < columns.length - 1) {
				sql1.append(",");
				sql2.append(",");
			}
		}
		sql1.append(")");
		sql2.append(")");
		sql.append(sql1).append(sql2);

		log.debug("INSERT SQL : " + sql.toString());

		Connection connection = null;
		PreparedStatement ps = null;
		int size = 0;
		try {
			connection = getConnection();
			ps = connection.prepareStatement(sql.toString());
			ps.clearParameters();
			setParameters(map, columns, ps);

			size = ps.executeUpdate();
		} catch (SQLException e) {
			log.error(e, e);
			throw e;
		} finally {
			if (ps != null) {
				ps.close();
			}
			closeConnection(connection);
		}
		return size;
	}

	private static void setParameters(Map<String, Object> map, String[] columns, PreparedStatement ps) throws SQLException {

		for (int i = 0; i < columns.length; i++) {
			Object value = ((Object[]) map.get(columns[i]))[1];

			// translate javaBean Date field (java.util.Date) to SQL Date type
			// (java.sql.Date) parameter
			if (value instanceof java.util.Date) {
				java.util.Date utilDate = (java.util.Date) value;
				value = new java.sql.Date(utilDate.getTime());
			}
			ps.setObject(i + 1, value);
		}
	}

	public static int deleteVO(Object bean) throws SQLException {

		String tableName = getTableName(bean);

		StringBuilder sql = new StringBuilder("DELETE FROM ").append(tableName);

		Connection connection = null;
		PreparedStatement ps = null;

		Map<String, Object> map = getNotBlankFields(bean);
		String[] columns = (String[]) map.keySet().toArray(new String[] {});

		if (columns.length > 0) {
			sql.append(" WHERE ");
			for (int i = 0; i < columns.length; i++) {
				if (i > 0) {
					sql.append(" AND ");
				}
				sql.append(columns[i]).append(" = ? ");
			}
		}
		log.debug("DELETE SQL : " + sql.toString());

		try {
			connection = getConnection();
			ps = connection.prepareStatement(sql.toString());
			ps.clearParameters();
			setParameters(map, columns, ps);

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

	private static void closeConnection(Connection connection) throws SQLException {

		if (userThreadLocal.get() == null) {
			if (connection != null) {
				connection.close();
			}
		}
	}

	private static Object[] parseCustomSqlWithParams(String fakeSql) {

		if (StringUtils.isBlank(fakeSql)) {
			throw new IllegalArgumentException("can't parse empty sql ");
		}

		String sql = fakeSql;
		List<String> keyList = new ArrayList<String>();

		// get key parameter ex: ':column' -> column
		while (fakeSql.length() != 0) {
			fakeSql = StringUtils.substringAfter(fakeSql.trim(), "':");
			// make sure prefix ':
			if (fakeSql.length() > 0) {
				String key = StringUtils.substringBefore(fakeSql, "'");
				keyList.add(key);
				int position = StringUtils.indexOf(fakeSql, "'");
				if (position == -1) {
					throw new RuntimeException("語法不正確 少 結尾符號 ' ");
				}
				fakeSql = StringUtils.substring(fakeSql, position + 1);
			}
		}

		for (String key : keyList) {
			String replace = "':" + key + "'";
			sql = StringUtils.replace(sql, replace, "?");
		}
		log.debug("parse SQL : " + sql);

		return new Object[] { sql, keyList };
	}

}
