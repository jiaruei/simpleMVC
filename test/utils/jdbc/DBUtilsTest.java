package utils.jdbc;

import static org.junit.Assert.fail;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import domain.Camel;

public class DBUtilsTest {

	@BeforeClass
	public static void loadPros() {

		ResourceBundle config = ResourceBundle.getBundle("jdbc");
		try {
			DBUtils.init(config);
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}

	@Before
	public void clearCamelData() {

		try {
			Camel camel = new Camel();
			DBUtils.deleteVO(camel);
			List<Camel> camelList = DBUtils.retrieveVOs(camel);
			Assert.assertEquals(0, camelList.size());
		} catch (SQLException e) {
			fail(e.getMessage());
		}

		Camel camel1 = new Camel();
		camel1.setCol1("A");
		camel1.setCol2("Chinese");
		camel1.setCol3(new BigDecimal("15.36"));
		camel1.setCol4(new Date());

		Camel camel2 = new Camel();
		camel2.setCol1("B");
		camel2.setCol2("English");
		camel2.setCol3(new BigDecimal("99.32"));
		camel2.setCol4(new Date());

		try {
			DBUtils.insertVO(camel1);
			DBUtils.insertVO(camel2);
		} catch (SQLException e) {
			fail(e.getMessage());
		}
	}

	@Test
	public void retrieveMaps() {

		String sql = "select col2,col3 from Camel where col1=':col1' ";
		Map<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put("col1", "B");

		try {

			List<Map<String, Object>> list = DBUtils.retrieveMaps(sql, paramMap);
			Assert.assertEquals(1, list.size());

			Map<String, Object> map = list.get(0);

			Assert.assertEquals(2, map.keySet().size());
			Assert.assertNull(map.get("col1"));
			Assert.assertEquals("English", map.get("col2"));
			Assert.assertEquals(new BigDecimal("99.32"), map.get("col3"));
			Assert.assertNull(map.get("col4"));
			
		} catch (SQLException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}

	@Ignore
	@Test
	public void testRetrieveVOs() {
		fail("Not yet implemented");
	}

	@Ignore
	@Test
	public void testInsertVO() {
		fail("Not yet implemented");
	}

}
