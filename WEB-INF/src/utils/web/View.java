package utils.web;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public interface View {
	
	public void forward(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException;
}
