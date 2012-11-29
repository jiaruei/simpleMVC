package utils.jdbc;

import java.sql.Connection;
import java.util.List;
import java.util.Properties;
import java.util.ResourceBundle;

import org.apache.commons.lang.math.NumberUtils;
import org.apache.log4j.Logger;

import com.mchange.v2.c3p0.ComboPooledDataSource;

public class DBUtils2 {

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

		if (pooledDataSource == null) {
			throw new IllegalArgumentException("initial datasource config fail ...");
		}
		return pooledDataSource;
	}


	protected static <T> String createSelectSql(String tableName,List<String> fields) {

		// create sql
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT * FROM ").append(tableName);

		if (fields.size() > 0) {
			sb.append(" WHERE ");
			for (int i = 0; i < fields.size(); i++) {
				if (i > 0) {
					sb.append(" AND ");
				}
				sb.append(fields.get(i)).append(" = ").append(" ? ");
			}
		}

		return sb.toString();
	}

	protected static <T> String createInsertSql(String tableName,List<String> fields) {
		
		// create sql
		StringBuilder sql1 = new StringBuilder();
		StringBuilder sql2 = new StringBuilder();

		sql1.append(" INSERT INTO ").append(tableName).append(" (");
		sql2.append("VALUES (");

		for (int i = 0; i < fields.size(); i++) {
			String column = fields.get(i);
			sql1.append(column);
			sql2.append("?");
			if (i < fields.size() - 1) {
				sql1.append(",");
				sql2.append(",");
			}
		}
		sql1.append(")");
		sql2.append(")");
		
		return sql1.append(sql2).toString();

	}

	protected static <T> String createDeleteSql(String tableName,List<String> fields) {
		
		// create sql
		StringBuilder sb = new StringBuilder("DELETE FROM ").append(tableName);

		if (fields.size() > 0) {
			sb.append(" WHERE ");
			for (int i = 0; i < fields.size(); i++) {
				if (i > 0) {
					sb.append(fields.get(i)).append(" = ").append(" ? ");
				}
			}
		}
		return sb.toString();
	}

}
