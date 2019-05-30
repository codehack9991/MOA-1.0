package fast.common.htmlReport;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;

public class TestAssertionFailure {

	@Test
	public void testConstrutorWithoutParameter() {
		AssertionFailure assertionFailure = new AssertionFailure();
		assertNotNull(assertionFailure);
	}

	@Test
	public void testConstrutorWithOneParameter() {
		AssertionFailure assertionFailure = new AssertionFailure("Message");
		String message = assertionFailure.getMessage();
		String str = assertionFailure.toString();
		assertEquals("Message", message);
		assertNotNull(str);
		assertNotNull(assertionFailure);
	}
	
	@Test
	public void testConstrutorWithTwoParameters() {
		AssertionFailure assertionFailure = new AssertionFailure("Message", null);
		assertNotNull(assertionFailure);
	}

	
}
