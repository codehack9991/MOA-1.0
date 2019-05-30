package fast.common.context.exception;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class TestColumnNotExistException {
	@Test
	public void constructor_initializeProperly() {
		ColumnNotExistsException ex = new ColumnNotExistsException("Col");
		assertEquals("column Col doesn't exist in datatable", ex.getMessage());
	}
}
