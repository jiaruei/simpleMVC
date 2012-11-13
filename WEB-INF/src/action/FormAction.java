package action;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import utils.jdbc.DBUtils;
import utils.web.JsonView;
import utils.web.PageView;
import utils.web.View;
import domain.Camel;

public class FormAction {

	private Logger log = Logger.getLogger(this.getClass());
	
	public View ajaxCamel(HttpServletRequest req, HttpServletResponse resp) {
		
		String name = req.getParameter("name");
		
		List<Camel> camelList = Collections.EMPTY_LIST;
		try {
			camelList = DBUtils.retrieveVOs(new Camel());
			Camel bean = new Camel();
			bean.setCol1(name);
			bean.setCol2("小魚");
			camelList.add(bean);
		} catch (SQLException e) {
			log.error(e, e);
		}

		return new JsonView(camelList);
	}

	public View initData(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

		try {
			DBUtils.beginTransaction();

			DBUtils.deleteVO(new Camel());

			Camel camel1 = new Camel();
			camel1.setCol1("1");
			camel1.setCol2("中文");
			camel1.setCol3(new BigDecimal(12.8));

			Camel camel2 = new Camel();
			camel2.setCol1("2");
			camel2.setCol2("英文");
			camel2.setCol3(new BigDecimal(886922.8));

			DBUtils.insertVO(camel1);
			DBUtils.insertVO(camel2);

			DBUtils.commitTransaction();

		} catch (SQLException e) {
			log.error(e, e);
			DBUtils.rollbackTransaction();
		}
		try {
			req.setAttribute("camelList", DBUtils.retrieveVOs(new Camel())) ;
		} catch (SQLException e) {
			log.error(e, e);
		}
		return new PageView("abc/def.jsp");

	}

}
