package fast.common.context;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Date;

import org.junit.Test;

public class TestEcmaScriptExposedMethods {

	@Test
	public void testGetEod() {
		EcmaScriptExposedMethods o = new EcmaScriptExposedMethods();
		assertNotNull(o);
		
		String eod = EcmaScriptExposedMethods.eod();
		assertNotNull(eod);
	}
	
	@Test
	public void testGetInHour() {
		String value = EcmaScriptExposedMethods.inhour();
		assertNotNull(value);
	}
	
	@Test
	public void testGetTimestamp() {
		String value = EcmaScriptExposedMethods.timestamp();
		assertNotNull(value);
	}
	
	@Test
	public void testGetUnique() {
		String value = EcmaScriptExposedMethods.unique();
		assertNotNull(value);
	}
	
	@Test
	public void testGetReadNum() {
		String actual = EcmaScriptExposedMethods.readNum();
		assertNotNull(actual);
	}

	@Test
	public void testGetSumUp() {
		int actual = EcmaScriptExposedMethods.sumUp("1+2+3");
		assertEquals(6, actual);
		actual = EcmaScriptExposedMethods.sumUp("1+0.2+3");
		assertEquals(-1, actual);
	}
	
	@Test
	public void testGetIsMgt() {
		String value = EcmaScriptExposedMethods.isgmt("1900");
		assertNotNull(value);
		value = EcmaScriptExposedMethods.isgmt("19000101-01:01:01.000");
		assertNotNull(value);
		value = EcmaScriptExposedMethods.isgmt("1900");
		assertNotNull(value);
	}

	@Test
	public void testGetThreeDecPlaces() {
		String value = EcmaScriptExposedMethods.threeDecPlaces("1.2345");
		assertNotNull(value);
		value = EcmaScriptExposedMethods.threeDecPlaces("12345");
		assertNotNull(value);
	}
	
	@Test
	public void testGetSumDouble() {
		double value = EcmaScriptExposedMethods.sumDouble("1.1+2.2");
		assertEquals(value, 3.3, 0.1);
		value = EcmaScriptExposedMethods.sumDouble("1.1+2.2.2");
		assertEquals(value, -1, 0.1);
	}
	
	@Test
	public void testAddSideBased() {
		String value = EcmaScriptExposedMethods.addSideBased("1.1,1,1,BUY");
		assertNotNull(value);
		value = EcmaScriptExposedMethods.addSideBased("1.1,1,1,SELL");
		assertNotNull(value);
	}
	
	@Test
	public void testTruncadd() {
		String value = EcmaScriptExposedMethods.truncadd("1.11,2,3.33");
		assertNotNull(value);
	}
	
	@Test
	public void testTrunc() {
		String value = EcmaScriptExposedMethods.trunc("1.1,1");
		assertNotNull(value);
	}
	
	@Test
	public void testChanged() {
		String value = EcmaScriptExposedMethods.changed("abc_abc");
		assertEquals("same", value);
		value = EcmaScriptExposedMethods.changed("aaa_abc");
		assertEquals("aaa", value);
	}
	
	@Test
	public void testNotZero() {
		String value = EcmaScriptExposedMethods.notzero("123");
		assertNotNull(value);
		value = EcmaScriptExposedMethods.notzero("0");
		assertNotNull(value);
	}
	
	@Test
	public void testGetClOrdIDForToday() {
		String value = EcmaScriptExposedMethods.GetClOrdIDForToday(10);
		assertNotNull(value);
	}
	
	@Test
	public void testAddSecondsToNow() {
		Date eod = EcmaScriptExposedMethods.addSecondsToNow(10);
		assertNotNull(eod);
	}
	
}
