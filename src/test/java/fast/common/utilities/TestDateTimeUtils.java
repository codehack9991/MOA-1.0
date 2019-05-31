package fast.common.utilities;

import static org.junit.Assert.assertEquals;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import fast.common.context.DateTimeDifferStepResult;


public class TestDateTimeUtils {
	
	DateTimeDifferStepResult dateTimeDifferStepResult = new DateTimeDifferStepResult();
	
	@Before
	public void setUp() throws Exception {
	}
		
	@After
	public void tearDown() throws Exception {
			
	}
	
	@Test
	public void testGetDifferDateTimeAttributesWithForamt_HHMMSS() throws Throwable {
		String endVarName = "10:05:30";
		String startVarName = "08:04:15";
		dateTimeDifferStepResult = DateTimeUtils.getDifferDateTimeAttributes(endVarName, startVarName, "HH:mm:ss");
		assertEquals("2",dateTimeDifferStepResult.getFieldValue("hour"));
		assertEquals("1",dateTimeDifferStepResult.getFieldValue("min"));
		assertEquals("15",dateTimeDifferStepResult.getFieldValue("sec"));
	}
	
	@Test
	public void testGetDifferDateTimeAttributesWithFormat_YYYYMMDDHHMMSS() throws Throwable {
		String endVarName = "2019-03-04 00:05:30";
		String startVarName = "2019-03-01 08:04:15";
		dateTimeDifferStepResult = DateTimeUtils.getDifferDateTimeAttributes(endVarName, startVarName, "yyyy-MM-dd HH:mm:ss");
		assertEquals("2",dateTimeDifferStepResult.getFieldValue("day"));
		assertEquals("16",dateTimeDifferStepResult.getFieldValue("hour"));
		assertEquals("1",dateTimeDifferStepResult.getFieldValue("min"));
		assertEquals("15",dateTimeDifferStepResult.getFieldValue("sec"));
	}
	
	@Test
	public void testGetDifferDateTimeAttributesWithFormat_EEEMMMddHHMMSSZZZYYYY() throws Throwable {
		String endVarName = "Fri Dec 14 19:50:23 CST 2013";
		String startVarName = "Tue May 14 09:10:53 CST 2013";
		dateTimeDifferStepResult = DateTimeUtils.getDifferDateTimeAttributes(endVarName, startVarName, "EEE MMM dd HH:mm:ss zzz yyyy");
		assertEquals("214",dateTimeDifferStepResult.getFieldValue("day"));
		assertEquals("10",dateTimeDifferStepResult.getFieldValue("hour"));
		assertEquals("39",dateTimeDifferStepResult.getFieldValue("min"));
		assertEquals("30",dateTimeDifferStepResult.getFieldValue("sec"));
	}
	
} 

