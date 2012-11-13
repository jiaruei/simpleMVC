package action;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import utils.web.PageView;
import utils.web.View;

public class FirstAction {

	public View index(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

		return new PageView("abc/abc.jsp");

	}

}
