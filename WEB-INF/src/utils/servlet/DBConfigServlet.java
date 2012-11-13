package utils.servlet;

import java.util.ResourceBundle;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

import org.apache.log4j.Logger;

import utils.jdbc.DBUtils;


public class DBConfigServlet extends HttpServlet{
	
	private Logger log = Logger.getLogger(this.getClass());
	
	public void init(ServletConfig config) throws ServletException {
		
		super.init(config);
		try {
			DBUtils.init(ResourceBundle.getBundle("jdbc"));
		} catch (Exception e) {
			e.printStackTrace();
			log.error("fail to load db config file  ... "+ e.getMessage());
		}
//		InputStream input = config.getServletContext().getResourceAsStream("/WEB-INF/classes/jdbc.properties");
//		try {
//			Properties p = new Properties();
//			p.load(input);
//			DBUtils.init(p);
//		} catch (Exception e) {
//			System.err.println(e.getMessage());
//			throw new ServletException(e);
//		}
		
	}
}
