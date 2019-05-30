package fast.common.gmdReplay.client;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.testng.Assert.assertEquals;

import java.util.ArrayList;

import org.junit.Before;
import org.junit.Test;

import fast.common.gmdReplay.client.GMDMessageError.ErrorDetails;

public class TestGMDMessageError {

	private GMDMessageError gmdMessageError;

	@Before
	public void setUp() {
		gmdMessageError = new GMDMessageError("A", ErrorType.Value.toString());
		gmdMessageError.addValueError("A", "B", "C");
		gmdMessageError.addMissingElement("B", "B");
		gmdMessageError.addUnexpectedElement("C", "C");
	}

	@Test
	public void testConstructorWithParameters() {
		assertNotNull(gmdMessageError);
	}

	@Test
	public void testGetSymbol() {
		String actual = gmdMessageError.getSymbol();
		assertEquals(actual, "A");
	}

	@Test
	public void testGetMsg_type() {
		String actual = gmdMessageError.getMsg_type();
		assertEquals(actual, "Value");
	}

	@Test
	public void testGetError() {
		ArrayList<ErrorDetails> errors = gmdMessageError.getErrors();
		assertNotNull(errors);
	}

	@Test
	public void testGetErrorDescription() {
		String errorDescription = gmdMessageError.getErrorDescription();
		assertTrue(errorDescription.contains("A"));
	}

	@Test
	public void testGetLabel_path() {
		int errNumber = gmdMessageError.getErrNumber();
		assertEquals(3, errNumber);
	}

	@Test
	public void testClear() {
		gmdMessageError.clear();
		int errNumber = gmdMessageError.getErrNumber();
		assertEquals(0, errNumber);
	}

	@Test
	public void testErrorDetails() {
		ArrayList<ErrorDetails> errors = gmdMessageError.getErrors();
		ErrorDetails errorDetails = errors.get(0);
		ErrorType type = errorDetails.getType();
		String label_path = errorDetails.getLabel_path();
		assertEquals("Value", type.toString());
		assertEquals("A", label_path);		
		
		
		ErrorDetails d = new ErrorDetails("A", "B", "C", ErrorType.Missing);
		int compareTo = errorDetails.compareTo(d);
		boolean equals = errorDetails.equals(d);
		assertEquals(compareTo, 0);
		assertEquals(equals, true);
	}
	
	@Test
	public void testCompareEquals() {
		ArrayList<ErrorDetails> errors = gmdMessageError.getErrors();
		ErrorDetails errorDetails = errors.get(0);
		ErrorDetails d = new ErrorDetails("A", "B", "C", ErrorType.Missing);
		int compareTo = errorDetails.compareTo(d);
		boolean equals = errorDetails.equals(d);
		assertEquals(compareTo, 0);
		assertEquals(equals, true);
	}
	
}
