package fast.common.context.exception;

import fast.common.context.DataTable;

public class RowIndexOutofRangeException extends Exception {
	
	private static final long serialVersionUID = -2852429685803407718L;

	public RowIndexOutofRangeException(DataTable dataTable,int rowIndex){
		super(String.format("The max row index is %d, but %d is given",	dataTable.getRowCount()-1,rowIndex ));
	}
}
