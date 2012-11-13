package utils.web;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.google.gson.Gson;

public class JsonView implements View {

	private Logger log = Logger.getLogger(this.getClass());

	private String json = "{}";

	public JsonView(Object object) {
		this.json = new Gson().toJson(object);
	}

	@Override
	public void forward(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

		log.debug("JsonView : " + json);
		
		resp.setCharacterEncoding("UTF-8");
	    resp.setContentType("application/json");
	    PrintWriter writer = resp.getWriter();
        
        try {
        	writer.println(json);
        	writer.flush();
		} catch (Exception e) {
			log.error(e, e);
		}finally{
			writer.close();
		}
	}

}
