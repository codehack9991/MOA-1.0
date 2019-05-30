package fast.common.context;

import java.util.ArrayList;

import com.tibco.tibrv.TibrvException;
import com.tibco.tibrv.TibrvMsg;

import fast.common.agents.messaging.TibcoRvAgent;
import fast.common.logging.FastLogger;

public class TibcoRvStepResult extends StepResult {
	static FastLogger logger = FastLogger.getLogger("TibcoRvStepResult");	
	private ArrayList<TibrvMsg> messages = new ArrayList<>();
	
	public TibcoRvStepResult(String message) throws TibrvException {	
		TibrvMsg tibrvMsg = new TibrvMsg();
		String[] fields = message.split("\\" + MapMessageTemplateHelper.MESSAGE_FIELD_SEP);
		for(String fieldStr : fields){
			String[] keyValue = fieldStr.split("=");
			tibrvMsg.add(keyValue[0].trim(), keyValue[1].trim());
		}
		messages.add(tibrvMsg);
	}
	
	public TibcoRvStepResult(TibrvMsg message){
		messages.add(message);
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		try{
			for(TibrvMsg msg : messages){				
				builder.append(TibcoRvAgent.convertTibrvMsgToString(msg));
				builder.append("\n");
			}
		}
		catch(Exception ex){
			builder.append(ex.getMessage());
		}
		
		return builder.toString();
	}

	@Override
	public String getFieldValue(String field) throws Throwable {
		if(messages.get(0).getField(field) != null){
			return messages.get(0).get(field).toString();	
		}
		else{
			return null;
		}
	}

	@Override
	public ArrayList<String> getFieldsValues(String field){
		ArrayList<String> values = new ArrayList<>();	
		try {
			if (!messages.isEmpty() && messages.get(0).getField(field) != null) {
				for (TibrvMsg msg : messages) {
					values.add(msg.get(field).toString());
				}
			}
		} catch (Exception e) {
			logger.error(String.format("Failed to get value for field %s from Tibrv Msg due to error %s", field,
					e.getMessage()));
		}
		
		return values;
	}
}
