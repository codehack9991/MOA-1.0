package fast.common.jira.entities;

import static org.mockito.Matchers.any;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

import org.junit.Before;
import org.junit.Test;

public class TestForVersion {

	private Version version;

	@Before
	public void Before() {
		version = new Version();
	}
	
	@Test
	public void testForSetGetValue() {		
		version.setValue("version");		
		assertEquals("version", version.getValue());
	}
	
	@Test
	public void testForSetGetArchived() {
		version.setArchived(true);		
		assertEquals(true, version.getArchived());
	}
	
	@Test
	public void testForSetGetLabel() {
		version.setLabel("label");		
		assertEquals("label", version.getLabel());
	}
}
