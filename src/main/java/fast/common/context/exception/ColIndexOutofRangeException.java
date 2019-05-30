package fast.common.context.exception;

import fast.common.context.DataTable;

public class ColIndexOutofRangeException extends Exception {

	private static final long serialVersionUID = -8343648929135266131L;

	public ColIndexOutofRangeException(DataTable dataTable,int colIndex){
		super(String.format("The max col index is %d, but %d is given",	dataTable.getColumnCount()-1,colIndex ));
	}
	
}
