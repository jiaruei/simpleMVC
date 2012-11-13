package utils.web;

import java.io.IOException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.beanutils.MethodUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

public class ActionServlet extends HttpServlet {

	private Logger log = Logger.getLogger(this.getClass());

	private static final String defaultPackage = "action";

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

			String[] actionClassInfo = getActionClassInfo(req);
			log.info("loading actionClassName : " + actionClassInfo[0]);

			Object action = Class.forName(defaultPackage + "." + actionClassInfo[0]).newInstance();
			String methodName = actionClassInfo[1];
			View view = (View) MethodUtils.invokeMethod(action, methodName, new Object[] { req, resp });
			view.forward(req, resp);
		} catch (Exception e) {
			log.error(e, e);
			throw new ServletException(e);
		}
	}

	private String[] getActionClassInfo(HttpServletRequest req) {

		String requestURI = req.getRequestURI();
		String[] arr = StringUtils.substringBefore(requestURI, ".do").split("/");

		String simpleName = null;
		String methodName = null;

		try {
			simpleName = arr[arr.length - 2];
			simpleName = simpleName.substring(0, 1).toUpperCase() + simpleName.substring(1);
			methodName = arr[arr.length - 1];

		} catch (Exception e) {
			log.error(e, e);
			throw new IllegalArgumentException(e);
		}
		return new String[] { simpleName, methodName };
	}

	// @Override
	// protected void doPost(HttpServletRequest req, HttpServletResponse resp)
	// throws ServletException, IOException {
	//
	// try {
	//
	// String actionClassName = getActionClassName(req);
	//
	// log.info("loading actionClassName : " + actionClassName);
	//
	// Action action = (Action) Class.forName(actionClassName).newInstance();
	// View view = action.doAction(req, resp);
	// view.forward(req, resp);
	// } catch (Exception e) {
	// log.error(e,e);
	// throw new ServletException(e);
	// }
	// }

	// private String getActionClassName(HttpServletRequest req) {
	//
	// String[] split = req.getRequestURI().split("/");
	// String simpleActionName = StringUtils.remove(split[split.length - 1],
	// ".do");
	// simpleActionName = simpleActionName.substring(0, 1).toUpperCase() +
	// simpleActionName.substring(1);
	// return defaultPackage + "." + simpleActionName;
	// }

}
