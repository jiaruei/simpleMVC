package utils.web;

import java.io.InputStream;
import java.util.Properties;
import java.util.ResourceBundle;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.commons.lang.StringUtils;

import utils.jdbc.DBUtils;

public class DataSourceConfigListener implements ServletContextListener {

	@Override
	public void contextDestroyed(ServletContextEvent event) {
		
	}

	@Override
	public void contextInitialized(ServletContextEvent event) {
		
		ServletContext servletContext = event.getServletContext();
		String location = servletContext.getInitParameter("dataSourceConfigLocation");
		
		if(StringUtils.isBlank(location)){
			try {
				DBUtils.init(ResourceBundle.getBundle("jdbc"));
			} catch (Exception e) {
				e.printStackTrace();
				servletContext.log("fail Initializing  default datasource ... ");
			}			
			servletContext.log("default Initializing  datasource ... ");
		}else{
			InputStream is = event.getServletContext().getResourceAsStream(location);
			Properties properties = new Properties();
			
			try {
				properties.load(is);
				DBUtils.init(properties);
			} catch (Exception e) {
				throw new IllegalArgumentException("Invalid 'dataSourceConfigLocation' parameter: " + e.getMessage());
			}
			
			servletContext.log("Initializing dataSourceConfigLocation from [" + location + "] ... ");
		}
	}

}
