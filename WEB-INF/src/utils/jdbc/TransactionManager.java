package utils.jdbc;

import java.sql.Connection;
import java.sql.SQLException;

import org.apache.log4j.Logger;

public class TransactionManager {
	
	private static Logger log = Logger.getLogger(TransactionManager.class);
	
	public static final ThreadLocal<Connection> connectionThreadLocal = new ThreadLocal<Connection>();

	public static void beginTransaction() {
		
		if(connectionThreadLocal.get() != null){
			connectionThreadLocal.set(null);
		}
		try {
			Connection currentConnection = DBUtils.getConnection();
			currentConnection.setAutoCommit(false);
			connectionThreadLocal.set(currentConnection);
			log.debug("start database transaction in this thread.");
		} catch (SQLException e) {
			log.error(e,e);
			throw new RuntimeException(e);
		}
	}
	
	public static void commitTransaction() {

		if (connectionThreadLocal.get() != null) {
			Connection currentConnection = connectionThreadLocal.get();
			try {
				currentConnection.commit();
				currentConnection.close();
				connectionThreadLocal.set(null);
				log.debug("commit database transaction in this thread.");
			} catch (SQLException e) {
				log.error(e,e);
				throw new RuntimeException(e);
			}
		} else {
			throw new RuntimeException("error operator ,miss beginTransaction operator ");
		}
	}
	
	public static void rollbackTransaction()  {
		
		if (connectionThreadLocal.get() != null) {
			Connection currentConnection = connectionThreadLocal.get();
			try {
				currentConnection.rollback();
				connectionThreadLocal.set(null);
				log.debug("rollback transaction in this thread.");
			} catch (SQLException e) {
				log.error(e,e);
				throw new RuntimeException(e);
			}
		}
	}
	
}
