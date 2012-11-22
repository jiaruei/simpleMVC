package utils.date;

import java.text.ParseException;
import java.util.Date;

import org.apache.commons.lang.time.DateFormatUtils;
import org.apache.commons.lang.time.DateUtils;
import org.apache.log4j.Logger;

public class DateHelper {

	private static Logger log = Logger.getLogger(DateHelper.class);
	
	/**
	 * 
	 * @param y2kDate
	 * @return
	 */
	public static Date y2kStrToDate(String y2kDate) {

		try {
			return DateUtils.parseDate(y2kDate, new String[] { "yyyy-MM-dd" });
		} catch (ParseException e) {
			log.error(e,e);
			throw new RuntimeException(e);
		}
	}
	
	public static String toY2kStr(Date date){
		return DateFormatUtils.format(date, "yyyy-MM-dd");
	}

	public static String y2kTodayStr(){
		return DateFormatUtils.format(new Date(), "yyyy-MM-dd");
	}
	
	public static Date rocStrToDate(String rocDate) {
		
		int length = rocDate.length();
		try {
			return DateUtils.parseDate(rocDate, new String[] { "yyyy-MM-dd" });
		} catch (ParseException e) {
			log.error(e,e);
			throw new RuntimeException(e);
		}
	}
	
	
}

