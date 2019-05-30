package fast.common.context;

import java.util.ArrayList;


public class MessagingStepResult extends StepResult {
	private ArrayList<Object> _messages; 

	public MessagingStepResult() {		
	}
	
	public MessagingStepResult(ArrayList<Object> messages) {	
		_messages = messages;
	}
	
	public ArrayList<Object> getMessages(){
		return _messages;
	}

	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return "";
	}

	@Override
	public String getFieldValue(String field) throws Throwable {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ArrayList<String> getFieldsValues(String field) {
		// TODO Auto-generated method stub
		return null;
	}
}
