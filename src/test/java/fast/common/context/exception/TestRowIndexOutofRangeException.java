package fast.common.context.exception;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import fast.common.context.DataTable;

public class TestRowIndexOutofRangeException {

	@Test
	public void constructor_initializeProperly() {
		RowIndexOutofRangeException ex = new RowIndexOutofRangeException(new DataTable(), 1);
		assertEquals("The max row index is -1, but 1 is given", ex.getMessage());
	}
}
