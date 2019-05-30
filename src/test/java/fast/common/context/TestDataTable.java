package fast.common.context;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import fast.common.context.exception.ColIndexOutofRangeException;
import fast.common.context.exception.ColumnNotExistsException;
import fast.common.context.exception.RowIndexOutofRangeException;

public class TestDataTable {

	private final static String NONEXISTENT_ROW="NO";
	private final static String COLUMN_NAME_MALIU="MaLiu";	
	
	DataTable dataTable;
	DataTable emptyTable;

	@Before
	public void setUp() {
		List<String> theader = new ArrayList<String>();
		theader.add("name");
		theader.add("age");
		theader.add("phone");

		List<List<Object>> tdata = new ArrayList<List<Object>>();
		List<Object> row = new ArrayList<Object>();

		row.add("LiYi");
		row.add("10");
		row.add("4567891");

		tdata.add(row);
		row = new ArrayList<Object>();

		row.add("LiuEr");
		row.add("11");
		row.add("234567");

		tdata.add(row);
		row = new ArrayList<Object>();

		row.add("ZhangSan");
		row.add("10");
		row.add("123456");

		tdata.add(row);
		row = new ArrayList<Object>();

		row.add("LiSi");
		row.add("11");
		row.add("234567");

		tdata.add(row);
		row = new ArrayList<Object>();

		row.add("WangWu");
		row.add("15");
		row.add("345678");

		tdata.add(row);

		dataTable = new DataTable(theader, tdata);
		emptyTable = new DataTable();
	}

	@Test
	public void copyEmptyTable_getTableWithHeader() {
		assertEquals(3, dataTable.copyEmptyTable().getHeader().size());
	}

	@Test
	public void getHeader_passed() {
		assertEquals(3, dataTable.getHeader().size());
		assertEquals(0, emptyTable.getHeader().size());
	}

	@Test
	public void getData_passed() {
		assertEquals(5, dataTable.getData().size());
		assertEquals(0, emptyTable.getData().size());
	}

	@Test
	public void isEmpty_passed() {
		assertFalse(dataTable.isEmpty());
		assertTrue(emptyTable.isEmpty());
	}

	@Test
	public void getRowCount_passed() {
		assertEquals(5, dataTable.getRowCount());
		assertEquals(0, emptyTable.getRowCount());
	}

	@Test
	public void getColumnCount_passed() {
		assertEquals(3, dataTable.getColumnCount());
		assertEquals(0, emptyTable.getColumnCount());
	}

	@Test
	public void getRow_specifiedRowGot() throws RowIndexOutofRangeException {
		List<Object> row = dataTable.getRow(0);
		assertEquals("LiYi", row.get(0));
	}	

	@Test
	public void getRow_exceptionThrown() {
		try {
			List<Object> row = dataTable.getRow(5);
			assertNotNull(row);
		} catch (Exception ex) {
			assertEquals(ex.getClass().getName(), RowIndexOutofRangeException.class.getName());
		}
	}

	@Test
	public void getColumn_specifiedColumnGot() throws ColIndexOutofRangeException, ColumnNotExistsException {
		List<Object> column = dataTable.getColumn(0);
		assertEquals("LiYi", column.get(0));
		column = dataTable.getColumn("name");
		assertEquals("LiYi", column.get(0));
	}

	@Test
	public void getColumn_exceptionThrown() {
		try {
			List<Object> column = dataTable.getColumn(3);
			assertNotNull(column);
		} catch (Exception ex) {
			assertEquals(ex.getClass().getName(), ColIndexOutofRangeException.class.getName());
		}
		try {
			List<Object> column = dataTable.getColumn(NONEXISTENT_ROW);
			assertNotNull(column);
		} catch (Exception ex) {
			assertEquals(ex.getClass().getName(), ColumnNotExistsException.class.getName());
		}
	}

	@Test
	public void getCellValue_correctValueGot()
			throws RowIndexOutofRangeException, ColIndexOutofRangeException, ColumnNotExistsException {
		String cell = dataTable.getCellValue(1, 1).toString();
		assertEquals("11", cell);
		cell = dataTable.getCellValue(1, "name").toString();
		assertEquals("LiuEr", cell);
	}

	@Test
	public void getCellValue_exceptionThrown() {
		try {
			String cell = dataTable.getCellValue(1, 3).toString();
			assertNull(cell);
		} catch (Exception ex) {
			assertEquals(ex.getClass().getName(), ColIndexOutofRangeException.class.getName());
		}
		try {
			String cell = dataTable.getCellValue(1, NONEXISTENT_ROW).toString();
			assertNull(cell);
		} catch (Exception ex) {
			assertEquals(ex.getClass().getName(), ColumnNotExistsException.class.getName());
		}
		try {
			String cell = dataTable.getCellValue(5, "name").toString();
			assertNull(cell);
		} catch (Exception ex) {
			assertEquals(ex.getClass().getName(), RowIndexOutofRangeException.class.getName());
		}
	}

	@Test
	public void setCellValue_setProperly()
			throws RowIndexOutofRangeException, ColIndexOutofRangeException, ColumnNotExistsException {
		dataTable.setCellValue(0, 0, COLUMN_NAME_MALIU);
		assertEquals(COLUMN_NAME_MALIU, dataTable.getCellValue(0, 0));
		dataTable.setCellValue(0, "age", "3");
		assertEquals("3", dataTable.getCellValue(0, "age"));
	}

	@Test
	public void setCellValue_exceptionThrown() {
		try {
			dataTable.setCellValue(0, 5, COLUMN_NAME_MALIU);
			assertNotNull(null);
		} catch (Exception ex) {
			assertEquals(ex.getClass().getName(), ColIndexOutofRangeException.class.getName());
		}
		try {
			dataTable.setCellValue(0, NONEXISTENT_ROW, "3");
			assertNotNull(null);
		} catch (Exception ex) {
			assertEquals(ex.getClass().getName(), ColumnNotExistsException.class.getName());
		}
		try {
			dataTable.setCellValue(5, NONEXISTENT_ROW, "3");
			assertNotNull(null);
		} catch (Exception ex) {
			assertEquals(ex.getClass().getName(), RowIndexOutofRangeException.class.getName());
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Test
	public void addRow_passed() {
		List<Object> row = new ArrayList();
		row.add("GouDan");
		row.add("1");
		row.add("357496465");

		dataTable.addRow(row);
		assertEquals(6, dataTable.getRowCount());
	}
}
