package utils.web;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

public class PageView implements View {
	
	private Logger log = Logger.getLogger(this.getClass());
	
	private static final String defaultJspFolder = "/WEB-INF/jsp";

	private String url;

	public PageView(String url) {
		this.url = url;
	}

	private String getUrl() {
		return url;
	}

	@Override
	public void forward(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		
		String path = defaultJspFolder + "/" + getUrl();
		log.debug("pageView forward "+ path);

		req.getRequestDispatcher(path).forward(req, resp);
	}

}
