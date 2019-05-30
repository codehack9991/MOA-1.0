package fast.common.context.exception;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import fast.common.context.DataTable;

public class TestColIndexOutofRangeException {

	@Test
	public void constructor_initializeProperly() {
		ColIndexOutofRangeException ex = new ColIndexOutofRangeException(new DataTable(), 1);
		assertEquals("The max col index is -1, but 1 is given", ex.getMessage());
	}
}
