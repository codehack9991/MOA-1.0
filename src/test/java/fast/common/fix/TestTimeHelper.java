package fast.common.fix;

import static org.junit.Assert.assertTrue;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.TimeZone;

import org.junit.Test;

public class TestTimeHelper {
	@Test
	public void generateTsWithNanoseconds_getTimeWithNanosecondsFormat(){	

		String time = TimeHelper.generateTsWithNanoseconds();
		try{
			String timeZone = "UTC";
		    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd-HH:mm:ss.SSSSSSSSS").withZone(ZoneId.of(timeZone));
		    ZonedDateTime zdt = ZonedDateTime.parse(time, formatter);
		}catch(DateTimeParseException e){
			assertNull(e);
		}
	}
	@Test
	public void generateTsWithMicroseconds_getTimeWithMicrosecondsFormat(){	
		String time = TimeHelper.generateTsWithMicroseconds();
		try{
			String timeZone = "UTC";
		    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd-HH:mm:ss.SSSSSS").withZone(ZoneId.of(timeZone));
		    ZonedDateTime zdt = ZonedDateTime.parse(time, formatter);
		}catch(DateTimeParseException e){
			assertNull(e);
		}
	}
	
	@Test
	public void generateTsWithMilliseconds_getTimeMillisecondsFormat(){	
		String time = TimeHelper.generateTsWithMilliseconds();
		try{
			String timeZone = "UTC";
		    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd-HH:mm:ss.SSS").withZone(ZoneId.of(timeZone));
		    ZonedDateTime zdt = ZonedDateTime.parse(time, formatter);
		}catch(DateTimeParseException e){
			assertNull(e);
		}
	}
	@Test
	public void generateTsWithSeconds_getTimeSecondsFormat(){	
		String time = TimeHelper.generateTsWithSeconds();
		try{
			String timeZone = "UTC";
		    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd-HH:mm:ss").withZone(ZoneId.of(timeZone));
		    ZonedDateTime zdt = ZonedDateTime.parse(time, formatter);
		}catch(DateTimeParseException e){
			assertNull(e);
		}
	}
	
	@Test
	public void generateTsWithDate_getTimeDateFormat(){	
		String time = TimeHelper.generateTsWithDate();
		try{
			String timeZone = "UTC";
		    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd").withZone(ZoneId.of(timeZone));
		    LocalDate zdt = LocalDate.parse(time, formatter);
		}catch(DateTimeParseException e){
			assertNull(e);
		}
	}
	
	@Test
	public void generateTsWithTime_getTimeFormat(){
		String time = TimeHelper.generateTsWithTime();
		try{
			String timeZone = "UTC";
		    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss").withZone(ZoneId.of(timeZone));
		    LocalTime zdt = LocalTime.parse(time, formatter);
		}catch(DateTimeParseException e){
			assertNull(e);
		}
	}
	@Test
	public void generateTsFormat_withoutTimezone(){
		String stringFormatter="yyyyMMdd-HH:mm:ss.SSS";	
		String time = TimeHelper.generateTsFormat(stringFormatter,null);
		try{
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern(stringFormatter).withZone(ZoneId.of("UTC"));
			ZonedDateTime zdt = ZonedDateTime.parse(time, formatter);
			}catch(DateTimeParseException e){
				assertNull(e);
			}
	}
	@Test
	public void generateTsFormat_exceptionWithDifferentFormat(){	
		String stringFormatter="yyyyMMdd-HH:mm:ss.SSS";	
		String timeZone = "UTC";
		String time = TimeHelper.generateTsFormat(stringFormatter,timeZone);
		
		try{
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd-HH:mm:ss").withZone(ZoneId.of(timeZone));
		ZonedDateTime zdt = ZonedDateTime.parse(time, formatter);
		}catch(DateTimeParseException e){
			assertNotNull(e);
		}
	}
	
	@Test
	public void addMinutesToNow_addMinutes(){
		String stringFormatter="yyyyMMdd-HH:mm:ss.SSS";	
		String timeZone = "UTC";
		String actualStr=TimeHelper.addMinutesToNow(5, stringFormatter, timeZone);
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern(stringFormatter).withZone(ZoneId.of(timeZone));
		ZonedDateTime zdtAddMinutest = ZonedDateTime.parse(actualStr, formatter);
		Duration d = Duration.between( ZonedDateTime.now(ZoneId.of(timeZone)) , zdtAddMinutest );
		assertTrue(Math.abs(d.getSeconds()-300)<10);
	}

	@Test
	public void addMinutesToNow_addMinutesWithoutFormat(){		
		String stringFormatter="HH:mm:ss";	
		String actualStr=TimeHelper.addMinutesToNow(5, null, null);
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern(stringFormatter).withZone(ZoneId.of(TimeZone.getDefault().getID()));
		LocalTime zdtAddMinutest = LocalTime.parse(actualStr, formatter);
		Duration d = Duration.between( LocalTime.now(ZoneId.of(TimeZone.getDefault().getID())) , zdtAddMinutest );
		assertTrue(Math.abs(d.getSeconds()-300)<10);
	}
	
	
}
