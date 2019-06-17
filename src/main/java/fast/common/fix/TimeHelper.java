package fast.common.fix;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.TimeZone;

/**
 
 */
public class TimeHelper {
	public static DateTimeFormatter nanosecondsTimeFormatter;
	public static DateTimeFormatter microsecondsTimeFormatter;
	public static DateTimeFormatter millisecondsTimeFormatter;
	public static DateTimeFormatter secondsTimeFormatter;
	public static DateTimeFormatter dateFormatter;
	public static DateTimeFormatter timeFormatter;
	
	private static final String TIMEZONE_LOCAL="LOCAL";

	private TimeHelper(){
	}
	
	static {
		nanosecondsTimeFormatter = DateTimeFormatter.ofPattern("yyyyMMdd-HH:mm:ss.SSSSSSSSS").withZone(ZoneId.of("UTC"));
		microsecondsTimeFormatter = DateTimeFormatter.ofPattern("yyyyMMdd-HH:mm:ss.SSSSSS").withZone(ZoneId.of("UTC"));
		millisecondsTimeFormatter = DateTimeFormatter.ofPattern("yyyyMMdd-HH:mm:ss.SSS").withZone(ZoneId.of("UTC"));
		secondsTimeFormatter = DateTimeFormatter.ofPattern("yyyyMMdd-HH:mm:ss").withZone(ZoneId.of("UTC"));
		dateFormatter = DateTimeFormatter.ofPattern("yyyyMMdd").withZone(ZoneId.of("UTC"));
		timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss").withZone(ZoneId.of("UTC"));
	}

	private static ZonedDateTime getNow() {
		return ZonedDateTime.now(ZoneId.of("UTC"));
	}

	public static String generateTsWithNanoseconds() {
		ZonedDateTime ts = getNow();
		return ts.format(nanosecondsTimeFormatter);
	}

	public static String generateTsWithMicroseconds() {
		ZonedDateTime ts = getNow();
		return  ts.format(microsecondsTimeFormatter);
	}

	public static String generateTsWithMilliseconds() {
		ZonedDateTime ts = getNow();
		return ts.format(millisecondsTimeFormatter);
	}

	public static String generateTsWithSeconds() {
		ZonedDateTime ts = getNow();
		return ts.format(secondsTimeFormatter);
	}

	public static String generateTsWithDate() {
		ZonedDateTime ts = getNow();
		return ts.format(dateFormatter);		
	}
	
	public static String generateTsWithTime(){
		ZonedDateTime ts= getNow();
		return ts.format(timeFormatter);
	}

	public static String generateTsFormat(String format, String tz) {
		if(tz==null || tz.isEmpty() || tz.equalsIgnoreCase(TIMEZONE_LOCAL)){
			tz= TimeZone.getDefault().getID();
		}
		
		DateTimeFormatter df = DateTimeFormatter.ofPattern(format).withZone(ZoneId.of(tz));
		ZonedDateTime ts = getNow();
		return ts.format(df);
	}

	public static String addMinutesToNow(Integer minutes,String format, String tz) {
		if(tz==null || tz.isEmpty() || tz.equalsIgnoreCase(TIMEZONE_LOCAL)){
			tz= TimeZone.getDefault().getID();
		}
		if (format==null || format.isEmpty()) {
			format="HH:mm:ss";
		}
		DateTimeFormatter df = DateTimeFormatter.ofPattern(format).withZone(ZoneId.of(tz));
		ZonedDateTime ts = getNow().plusMinutes(minutes);
		return ts.format(df);		
	}
}
