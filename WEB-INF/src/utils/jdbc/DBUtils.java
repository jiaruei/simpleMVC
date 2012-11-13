package utils.jdbc;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.ResourceBundle;

import javax.management.RuntimeErrorException;

import org.apache.commons.beanutils.MethodUtils;
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
			log.error(e,e);
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

	public static <T> List<T> retrieveVOs(T bean) throws SQLException {

		String tableName = getTableName(bean);
		Map<String, Object> map = getNotBlankFields(bean);
		String[] columns = (String[]) map.keySet().toArray(new String[] {});

		StringBuilder sql = new StringBuilder("SELECT * FROM ").append(tableName);
		if (columns.length > 0) {
			sql.append(" WHERE ");
			for (int i = 0; i < columns.length; i++) {
				if (i > 0) {
					sql.append(" AND ");
				}
				sql.append(columns[i]).append(" = ? ");
			}
		}

		log.debug("SELECT SQL : " + sql.toString());

		Connection connection = null;
		PreparedStatement ps = null;
		List<T> list = new ArrayList<T>();

		try {
			connection = getConnection();
			ps = connection.prepareStatement(sql.toString());
			for (int i = 0; i < columns.length; i++) {
				Object[] values = (Object[]) map.get(columns[i]);
				ps.setObject(i + 1, values[1]);
			}

			ResultSet rs = ps.executeQuery();

			ResultSetMetaData metaData = rs.getMetaData();
			int columnCount = metaData.getColumnCount();
			while (rs.next()) {
				try {
					T vo = (T) bean.getClass().newInstance();
					for (int columnIndex = 0; columnIndex < columnCount; columnIndex++) {
						String column = metaData.getColumnName(columnIndex + 1);
						Object value = rs.getObject(columnIndex + 1);
						if (value != null) {
							String methodName = "set" + column.substring(0, 1).toUpperCase() + column.substring(1);
							MethodUtils.invokeMethod(vo, methodName, value);
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
			if (ps != null) {
				ps.close();
			}
			closeConnection(connection);
		}
		return list;
	}

	/**
	 * @param bean
	 * @return
	 * @throws SQLException
	 */
	public static int insertVO(Object bean) throws SQLException {

		String tableName = getTableName(bean);
		Map<String, Object> map = getNotBlankFields(bean);

		// create insert sql
		StringBuilder sql = new StringBuilder();
		StringBuilder sql1 = new StringBuilder();
		StringBuilder sql2 = new StringBuilder();
		sql1.append(" INSERT INTO ").append(tableName).append(" (");
		sql2.append("VALUES (");

		String[] columns = (String[]) map.keySet().toArray(new String[] {});
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
			for (int i = 0; i < columns.length; i++) {
				Object[] values = (Object[]) map.get(columns[i]);
				ps.setObject(i + 1, values[1]);
			}

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

			for (int i = 0; i < columns.length; i++) {
				Object[] values = (Object[]) map.get(columns[i]);
				ps.setObject(i + 1, values[1]);
			}

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

}
