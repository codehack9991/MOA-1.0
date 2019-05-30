package fast.common.context;

import java.util.ArrayList;
import java.util.List;

import fast.common.context.exception.ColIndexOutofRangeException;
import fast.common.context.exception.ColumnNotExistsException;
import fast.common.context.exception.RowIndexOutofRangeException;

public class DataTable {

	private List<String> header;
	private List<List<Object>> data;

	public DataTable() {
		header = new ArrayList<>();
		data = new ArrayList<>();
	}

	/**
     * Constructs a new <tt>DataTable</tt> with header and data
     * @param   list of header
     * @param   list of data 
     * @since 1.9
     */
	public DataTable(List<String> header, List<List<Object>> data) {
		this.header = header == null ? new ArrayList<>() : header;
		this.data = data == null ? new ArrayList<>() : data;
	}

	/**
     * get a table with header and empty data
     * @return DataTable
	 * @since 1.9
     */
	public DataTable copyEmptyTable() {
		List<String> copyHeader = new ArrayList<String>();
		copyHeader.addAll(this.header);
		return new DataTable(copyHeader, new ArrayList<>());
	}

	/**
     * get table's header
     * @return List<String>
	 * @since 1.9
     */
	public List<String> getHeader() {
		return this.header;
	}

	/**
     * get table's data
     * @return List<List<Object>>
	 * @since 1.9
     */
	public List<List<Object>> getData() {
		return this.data;
	}

	/**
     * get a boolean flag to indicate whether table is empty
     * @return boolean
	 * @since 1.9
     */
	public boolean isEmpty() {
		return this.data.isEmpty();
	}

	/**
     * get row count of table
     * @return int
	 * @since 1.9
     */
	public int getRowCount() {
		return data.size();
	}
	
	/**
     * get column of table
     * @return int
	 * @since 1.9
     */
	public int getColumnCount() {
		return this.header.size();
	}

	/**
     * get single row data by row index from table
     * @param row index
     * @return List<Object>
     * @throws RowIndexOutofRangeException
	 * @since 1.9
     */
	public List<Object> getRow(int rowIndex) throws RowIndexOutofRangeException {
		this.checkRowIndex(rowIndex);
		return this.data.get(rowIndex);
	}

	/**
     * get single column data by column index from table
     * @param column index
     * @return List<Object>
     * @throws ColIndexOutofRangeException
	 * @since 1.9
     */
	public List<Object> getColumn(int colIndex) throws ColIndexOutofRangeException {
		this.checkColIndex(colIndex);

		List<Object> result = new ArrayList<>();

		for (List<Object> row : this.data) {
			result.add(row.get(colIndex));
		}

		return result;
	}

	/**
     * get single column by column name from table
     * @param column name
     * @return List<Object>
     * @throws ColIndexOutofRangeException, ColumnNotExistsException
	 * @since 1.9
     */
	public List<Object> getColumn(String colName) throws ColIndexOutofRangeException, ColumnNotExistsException {
		return this.getColumn(this.checkColName(colName));
	}

	/**
     * get single cell value by row index and column index from table
     * @param row index
     * @param column index
     * @return Object
     * @throws RowIndexOutofRangeException, ColIndexOutofRangeException
	 * @since 1.9
     */
	public Object getCellValue(int rowIndex, int colIndex)
			throws RowIndexOutofRangeException, ColIndexOutofRangeException {
		if (data.isEmpty()) {
			return null;
		}

		checkRowIndex(rowIndex);
		List<Object> row = data.get(rowIndex);
		if (row.isEmpty()) {
			return null;
		}
		checkColIndex(colIndex);
		return row.get(colIndex);
	}

	/**
     * get single cell value by row index and column name from table
     * @param row index
     * @param column name
     * @return Object
     * @throws RowIndexOutofRangeException, ColIndexOutofRangeException, ColumnNotExistsException
	 * @since 1.9
     */
	public Object getCellValue(int rowIndex, String colName)
			throws RowIndexOutofRangeException, ColIndexOutofRangeException, ColumnNotExistsException {
		return this.getCellValue(rowIndex, this.checkColName(colName));
	}
	
	/**
     * set single cell value by row index and column index to table
     * @param row index
     * @param column index
     * @param value
     * @throws RowIndexOutofRangeException, ColIndexOutofRangeException
	 * @since 1.9
     */
	public void setCellValue(int rowIndex, int colIndex, Object value)
			throws RowIndexOutofRangeException, ColIndexOutofRangeException {
		this.checkRowIndex(rowIndex);
		this.checkColIndex(colIndex);
		this.data.get(rowIndex).set(colIndex, value);
	}

	/**
     * set single cell value by row index and column name to table
     * @param row index
     * @param column name
     * @param value
     * @throws RowIndexOutofRangeException, ColumnNotExistsException
	 * @since 1.9
     */
	public void setCellValue(int rowIndex, String colName, Object value)
			throws RowIndexOutofRangeException, ColumnNotExistsException {
		this.checkRowIndex(rowIndex);
		this.data.get(rowIndex).set(this.checkColName(colName), value);
	}

	/**
     * add a row to table
     * @param row data
	 * @since 1.9
     */
	public void addRow(List<Object> row) {
		this.data.add(row);
	}

	private int checkColName(String colName) throws ColumnNotExistsException {
		int colIndex = header.indexOf(colName);
		if (colIndex == -1) {
			throw new ColumnNotExistsException(colName);
		}
		return colIndex;
	}

	private void checkRowIndex(int rowIndex) throws RowIndexOutofRangeException {
		if (rowIndex >= this.getRowCount() || rowIndex < 0)
			throw new RowIndexOutofRangeException(this, rowIndex);
	}

	private void checkColIndex(int colIndex) throws ColIndexOutofRangeException {
		if (colIndex >= this.getColumnCount() || colIndex < 0)
			throw new ColIndexOutofRangeException(this, colIndex);
	}
}
