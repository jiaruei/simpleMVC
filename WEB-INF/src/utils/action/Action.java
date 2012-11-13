package utils.action;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public abstract class Action {

	private static final String defaultJspFolder = "/WEB-INF/jsp";

	public abstract String doAction(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException;

	protected void forward(HttpServletRequest req, HttpServletResponse resp, String url) throws ServletException, IOException {

		String path = defaultJspFolder + "/" + url;
		req.getRequestDispatcher(path).forward(req, resp);
	}
}
