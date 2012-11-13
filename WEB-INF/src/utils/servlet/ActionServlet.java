package utils.servlet;

import java.io.IOException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import utils.action.Action;

public class ActionServlet extends HttpServlet {

	private Logger log = Logger.getLogger(this.getClass());

	private static final String defaultPackage = "action";
	
	private static final String defaultJspFolder = "/WEB-INF/jsp";


	protected void forward(HttpServletRequest req, HttpServletResponse resp, String url) throws ServletException, IOException {

		String path = defaultJspFolder + "/" + url;
		req.getRequestDispatcher(path).forward(req, resp);
	}
	
	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		doPost(req, resp);

	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

		try {
			
			String actionClassName = getActionClassName(req);
			
			log.info("loading actionClassName : " + actionClassName);
			
			Action action = (Action) Class.forName(actionClassName).newInstance();
			String url = action.doAction(req, resp);
			forward(req, resp, url);
		} catch (Exception e) {
			System.err.println(e);
			throw new ServletException(e);
		}
	}

	private String getActionClassName(HttpServletRequest req) {

		String[] split = req.getRequestURI().split("/");
		String simpleActionName = StringUtils.remove(split[split.length - 1], ".do");
		simpleActionName = simpleActionName.substring(0, 1).toUpperCase() + simpleActionName.substring(1);
		return defaultPackage + "." + simpleActionName;
	}

}
