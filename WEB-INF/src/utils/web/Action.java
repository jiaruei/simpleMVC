package utils.web;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Deprecated
public interface Action {

	public View doAction(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException;

}
