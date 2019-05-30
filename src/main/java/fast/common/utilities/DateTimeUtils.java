package fast.common.utilities;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;


import fast.common.context.DateTimeDifferStepResult;
import fast.common.logging.FastLogger;

public class DateTimeUtils {
	
	public static FastLogger logger = FastLogger.getLogger("DateTimeUtilsLogger");
	
	private DateTimeUtils(){
		
	}
	
	private static Map<String,String> extractDifferDateTimeAttributes(String endTime, String startTime, String dateTimeFormat) throws ParseException {
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat(dateTimeFormat);
		Map differTimeAttributes = new HashMap<String,String>();
		Date endDate = null;
		Date startDate = null;
		endDate = simpleDateFormat.parse(endTime);
		startDate = simpleDateFormat.parse(startTime);
		long tickDifferTime=endDate.getTime()-startDate.getTime();
		long day=tickDifferTime/(24*60*60*1000);
		long hour=(tickDifferTime/(60*60*1000)-day*24);
		long min=((tickDifferTime/(60*1000))-day*24*60-hour*60);
		long sec=(tickDifferTime/1000-day*24*60*60-hour*60*60-min*60);
		differTimeAttributes.put("day", String.valueOf(day));
		differTimeAttributes.put("hour", String.valueOf(hour));
		differTimeAttributes.put("min", String.valueOf(min));
		differTimeAttributes.put("sec", String.valueOf(sec));
		return differTimeAttributes;
	}
	
	public static DateTimeDifferStepResult getDifferDateTimeAttributes(String endTime, String startTime, String dateTimeFormat) throws ParseException {
		DateTimeDifferStepResult dateTimeStepResult = new DateTimeDifferStepResult();
		Map<String, String> differTimeMap = extractDifferDateTimeAttributes(endTime,startTime,dateTimeFormat);
		dateTimeStepResult.setResult(differTimeMap);
		return dateTimeStepResult;
	}
	
}
