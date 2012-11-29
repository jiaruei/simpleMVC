package utils.jdbc;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import junit.framework.Assert;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import utils.date.DateHelper;
import domain.Camel;

public class DBUtilsTest {

	private static Logger log = Logger.getLogger(DBUtilsTest.class);

	@BeforeClass
	public static void loadPros() {

		ResourceBundle config = ResourceBundle.getBundle("jdbc");
		try {
			DBUtils.init(config);
			deleteCamel();
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}

	public static void deleteCamel() {
		try {
			Camel camel = new Camel();
			DBUtils.deleteVo(camel);
			List<Camel> camelList = DBUtils.retrieveVos(camel);
			Assert.assertEquals(0, camelList.size());
		} catch (SQLException e) {
			fail(e.getMessage());
		}
	}

	@Before
	public void clearCamelBefor() {

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
			DBUtils.insertVo(camel1);
			DBUtils.insertVo(camel2);
		} catch (SQLException e) {
			fail(e.getMessage());
		}
	}

	@After
	public void clearCamelAfter() {
		deleteCamel();
	}

	@Test
	public void retrieveMaps() {

		String sql = "select 2 as col1,col2,col3 ,col4 from Camel where col1=':col1' and col4=':col4'";
		Map<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put("col1", "B");
		paramMap.put("col4", new Date());

		try {
			List<Map<String, Object>> list = DBUtils.retrieveMaps(sql, paramMap);
			Assert.assertEquals(1, list.size());

			Map<String, Object> map = list.get(0);

			assertEquals(4, map.keySet().size());
			assertEquals(2, map.get("col1"));
			assertEquals("English", map.get("col2"));
			assertEquals(new BigDecimal("99.32"), map.get("col3"));
			assertEquals(DateHelper.y2kTodayStr(), DateHelper.toY2kStr((Date) map.get("col4")));

		} catch (SQLException e) {
			fail(e.getMessage());
		}
	}

	@Test
	public void deleteSql() {

		Camel bean = new Camel();
		try {
			assertEquals(2, DBUtils.retrieveVos(bean).size());
		} catch (SQLException e) {
			fail(e.getMessage());
		}
		String sql = "delete from Camel  where col1=':col1'";
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("col1", "A");

		try {
			int deleteCount = DBUtils.executeUpdate(sql, params);
			assertEquals(1, deleteCount);
		} catch (SQLException e) {
			fail(e.getMessage());
		}

		try {
			assertEquals(1, DBUtils.retrieveVos(bean).size());
		} catch (SQLException e) {
			fail(e.getMessage());
		}
	}
	
	@Test
	public void insertSql() {

		String sql = "insert into Camel (col1,col2,col3) values (':col1',':col2',':col3')";
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("col1", "C");
		params.put("col2", "Third");
		params.put("col3", new BigDecimal("99.3"));
		
		try {
			DBUtils.executeUpdate(sql, params);
		} catch (SQLException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
		
		try {
			Map<String, Object> map = DBUtils.retrieveMaps("select count(*) AS total from Camel").get(0);
			long total = ((Long) map.get("total")).longValue();
			assertEquals(3, total);
		} catch (SQLException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}
	@Test
	public void updateSql() {

		String sql = "update Camel set col1=':col1',col2=':col2' where col1=':col3'";
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("col1", "C");
		params.put("col2", "TWD");
		params.put("col3", "A");

		try {
			int updateCount = DBUtils.executeUpdate(sql, params);
			assertEquals(1, updateCount);
		} catch (SQLException e) {
			fail(e.getMessage());
		}
		Camel bean = new Camel();
		bean.setCol1("C");
		try {
			List<Camel> list = DBUtils.retrieveVos(bean);
			assertEquals(1, list.size());
			bean = list.get(0);

			assertEquals("C", bean.getCol1());
			assertEquals("TWD", bean.getCol2());
		} catch (SQLException e) {
			fail(e.getMessage());
		}

		bean = new Camel();
		bean.setCol1("A");
		try {
			List<Camel> list = DBUtils.retrieveVos(bean);
			assertEquals(0, list.size());
		} catch (SQLException e) {
			fail(e.getMessage());
		}
	}

	@Test
	public void insertRetrieveVO() {

		Camel bean = new Camel();

		List<Camel> list = null;
		try {
			list = DBUtils.retrieveVos(bean);
			assertEquals(2, list.size());

			Camel camel = list.get(0);
			assertEquals("A", camel.getCol1());
			assertEquals("Chinese", camel.getCol2());
			assertEquals(new BigDecimal("15.36"), camel.getCol3());
			assertEquals(DateHelper.toY2kStr(camel.getCol4()), DateHelper.y2kTodayStr());
		} catch (SQLException e) {
			fail(e.getMessage());
		}
	}

}
