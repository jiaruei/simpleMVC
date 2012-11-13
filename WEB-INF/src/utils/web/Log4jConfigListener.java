package utils.web;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.ResourceBundle;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.PropertyConfigurator;

public class Log4jConfigListener implements ServletContextListener {

	@Override
	public void contextDestroyed(ServletContextEvent event) {
		
	}

	@Override
	public void contextInitialized(ServletContextEvent event) {
		ServletContext servletContext = event.getServletContext();
		String location = servletContext.getInitParameter("log4jConfigLocation");
		
		if(StringUtils.isBlank(location)){
			
			ResourceBundle bundle = ResourceBundle.getBundle("log4j");
			Properties properties = new Properties();
			for(String key : bundle.keySet()){
				properties.setProperty(key, bundle.getString(key));
			}
			PropertyConfigurator.configure(properties);
			
			servletContext.log("default Initializing  log4j ... ");
			
		}else{
			
			InputStream is = event.getServletContext().getResourceAsStream(location);
			Properties properties = new Properties();
			
			try {
				properties.load(is);
				PropertyConfigurator.configure(properties);
			} catch (IOException e) {
				throw new IllegalArgumentException("Invalid 'log4jConfigLocation' parameter: " + e.getMessage());
			}
			
			servletContext.log("Initializing log4j from [" + location + "] ...");
		}
	}

}
