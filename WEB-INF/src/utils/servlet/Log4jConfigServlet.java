package utils.servlet;

import java.util.Properties;
import java.util.ResourceBundle;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

public class Log4jConfigServlet extends HttpServlet{
	
	private Logger log = Logger.getLogger(this.getClass());
	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		
		ResourceBundle bundle = ResourceBundle.getBundle("log4j");
		Properties properties = new Properties();
		for(String key : bundle.keySet()){
			properties.setProperty(key, bundle.getString(key));
		}
		PropertyConfigurator.configure(properties);
		
		log.info("loading log4j properties ...");
	}
}
