package fast.common.utilities;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.testng.Assert.assertEquals;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

public class TestBusinessDayInfo {

	@Test
	public void testForAddBusinesDays() {
		Calendar calendar = Calendar.getInstance();
		calendar.set(2018, 6, 4);
		Date date = calendar.getTime();
		BusinessDaysInfo businessDaysInfo = new BusinessDaysInfo();
		Assert.assertEquals("07/05/2018", businessDaysInfo.addBusinessDays(date, 1));
	}

	@Test
	public void testCopyBusinesDaysInfo() {
		BusinessDaysInfo businessDaysInfo = new BusinessDaysInfo();
		Object copy = businessDaysInfo.copy();
		assertEquals(copy, businessDaysInfo);
	}

	@Test
	public void testCheckBusinesDaysInfoEquals() {
		BusinessDaysInfo businessDaysInfo = new BusinessDaysInfo();
		Object copy = businessDaysInfo.copy();
		boolean result = copy.equals(businessDaysInfo);
		assertTrue(result);
		result = copy.equals(null);
		assertFalse(result);
		result = copy.equals("");
		assertFalse(result);
		assertEquals(copy, businessDaysInfo);
	}

	@Test
	public void testBusinesDayInfoGetHolidaysList() {

		BusinessDaysInfo businessDaysInfo = new BusinessDaysInfo();
		List<String> holidaysList = businessDaysInfo.getHolidaysList();
		assertNotNull(holidaysList);
	}

	@Test
	public void testBusinesDayInfoAddBusinessDays() {

		BusinessDaysInfo businessDaysInfo = new BusinessDaysInfo();
		businessDaysInfo.addBusinessDays(null, 0);
		businessDaysInfo.addBusinessDays(new Date(), 1);
		businessDaysInfo.addBusinessDays(new Date(), 2);
		businessDaysInfo.addBusinessDays(new Date(), 3);
		businessDaysInfo.addBusinessDays(new Date(), 4);
		businessDaysInfo.addBusinessDays(new Date(), 5);
		businessDaysInfo.addBusinessDays(new Date(), 6);
		businessDaysInfo.addBusinessDays(new Date(), 7);
		businessDaysInfo.addBusinessDays(new Date(), -1);

		List<String> holidaysList = businessDaysInfo.getHolidaysList();
		assertNotNull(holidaysList);
	}

	@Test
	public void testBusinesDayInfoLoadHolidaysList() {

		BusinessDaysInfo businessDaysInfo = new BusinessDaysInfo();
		String holidays = "1/2/1900,1/3/1900";
		try {
			businessDaysInfo.loadHolidaysList(holidays);
			List<String> holidaysList = businessDaysInfo.getHolidaysList();
			int hashCode = businessDaysInfo.hashCode();
			assertNotNull(hashCode);
			assertNotNull(holidaysList);
		} catch (ParseException e) {
		}
		try {
			businessDaysInfo.loadHolidaysList("1,1");
		} catch (ParseException e) {
		}

	}

	@Test
	public void testBusinesDayInfoLoadHolidaysListWithInvalidString() {
		BusinessDaysInfo businessDaysInfo = new BusinessDaysInfo();
		try {
			businessDaysInfo.loadHolidaysList("1,1");
		} catch (ParseException e) {
		}
		int size = businessDaysInfo.getHolidaysList().size();
		assertEquals(size, 0);
	}

}
