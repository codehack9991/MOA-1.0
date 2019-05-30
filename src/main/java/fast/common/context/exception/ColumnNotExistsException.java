package fast.common.context.exception;

public class ColumnNotExistsException extends Exception {
	
	private static final long serialVersionUID = -5880093783866339111L;

	public ColumnNotExistsException(String columnName){
		super(String.format("column %s doesn't exist in datatable", columnName));
	}
}
