package fast.common.jira.entities;

import static org.testng.Assert.assertNull;

import org.junit.Before;
import org.junit.Test;

public class TestForPVItem {

	private PVItem pvItem;

	@Before
	public void Before() {
		pvItem = new PVItem();
	}
	
	@Test
	public void testForGetValue() {
		assertNull(pvItem.getValue());
	}
	
	
	@Test
	public void testForGetLabel() {
		assertNull(pvItem.getLabel());
	}
	
	@Test
	public void testForGetHasAccessToSoftware() {
		assertNull(pvItem.getHasAccessToSoftware());
	}
	
	@Test
	public void testForGetType() {
		assertNull(pvItem.getType());
	}
	
}
