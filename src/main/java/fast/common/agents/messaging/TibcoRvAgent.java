package fast.common.agents.messaging;

import com.tibco.tibrv.TibrvException;
import com.tibco.tibrv.TibrvListener;
import com.tibco.tibrv.TibrvMsg;
import com.tibco.tibrv.TibrvMsgCallback;
import com.tibco.tibrv.TibrvMsgField;
import com.tibco.tibrv.TibrvRvdTransport;

import fast.common.agents.Agent;
import fast.common.context.MapMessageTemplateHelper;
import fast.common.context.MessagingStepResult;
import fast.common.context.ScenarioContext;
import fast.common.context.TibcoRvStepResult;
import fast.common.core.Configurator;
import fast.common.core.MapMessageTemplate;
import fast.common.core.ValidationFailed;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import com.tibco.tibrv.Tibrv;
import fast.common.logging.FastLogger;
import quickfix.Field;
import quickfix.FieldNotFound;

/**
 * TIBCO RV reference is here https://docs.tibco.com/pub/rendezvous/8.4.0-february-2012/doc/pdf/tib_rv_java_reference.pdf
 */
public class TibcoRvAgent extends Agent implements IMessagingAgent, TibrvMsgCallback {
	public static final String TIBCORVMESSAGETEMPLATES_MAP_NAME = "TibcorvMessageTemplates";
	public static final String STRUCTUREDTIBCORVMESSAGETEMPLATES_MAP_NAME = "StructuredTibcorvMessageTemplates";
	
    private final FastLogger logger;
    private TibrvRvdTransport tibrvTransport;
    private String service;
    private String network;
    private String daemon;
    private String subject;
    private ArrayList<Object> receivedMessages = new ArrayList<Object>();
    private boolean isStarted = false;
    private HashMap<String, MapMessageTemplate> messageTemplates = new HashMap<String, MapMessageTemplate>();

    public TibcoRvAgent(){
    	logger = FastLogger.getLogger(String.format("%s:FixEmsAgent", _name));
    }
    
    public TibcoRvAgent(String name, Map agentParams, Configurator configurator) throws MessagingException {
        super(name, agentParams, configurator);
        logger = FastLogger.getLogger(String.format("%s:FixEmsAgent", _name));
        
        service = Configurator.getStringOr(_agentParams, "service", null);
    	network = Configurator.getStringOr(_agentParams, "network", null);
    	daemon = Configurator.getStringOr(_agentParams, "daemon", null);
        subject = Configurator.getStringOr(_agentParams, "subject", null);      
        
        MapMessageTemplateHelper.populateTemplateMap(configurator.getSettingsMap(), TIBCORVMESSAGETEMPLATES_MAP_NAME, null, messageTemplates, null);
        MapMessageTemplateHelper.populateTemplateMap(configurator.getSettingsMap(), STRUCTUREDTIBCORVMESSAGETEMPLATES_MAP_NAME, null, messageTemplates, null);

        start();
    }

    public void start() throws MessagingException {
        try {
            Tibrv.open(Tibrv.IMPL_NATIVE);
            tibrvTransport = new TibrvRvdTransport(service, network, daemon);
            tibrvTransport.setDescription("FAST Automation Client");
            new TibrvListener(Tibrv.defaultQueue(), this, tibrvTransport, subject, null);
            
        }
        catch(Exception ex) {
            tibrvTransport = null;
            logger.error(String.format("connect failure: %s", ex.toString()));
            throw new MessagingException(ex);
        }
        isStarted = true;
    }

    @Override
    public void close() throws Exception {
        tibrvTransport.destroy();
        tibrvTransport = null;
    }

	@Override
	public void send(Object message) throws MessagingException {		
        try {
        	TibrvMsg msg = null;   
        	if(message instanceof TibrvMsg){
        		msg = (TibrvMsg)message;
        	}
        	else {
        		msg = constructTibrvMsg(message);
        	}
        	
        	msg.setSendSubject(subject);
	        tibrvTransport.send(msg);
		} catch (TibrvException e) {
			throw new MessagingException(e);
		}		
	}
	
	public void send(ScenarioContext scenarioContext, String varName, String templateName, String userstr) throws MessagingException, TibrvException {
		String message = constuctFullUserStr(scenarioContext, templateName, userstr);		
		send(message);
		TibcoRvStepResult lastStepResult = new TibcoRvStepResult(message);
		scenarioContext.saveLastStepResult(lastStepResult, varName);
	}
	
	@Override
	public void onMsg(TibrvListener listener, TibrvMsg msg){	
		Object message = msg;		
		
		synchronized(receivedMessages){
			receivedMessages.add(message);
		}
	}

	@Override
	public MessagingStepResult receive() {
		ArrayList<Object> messages = null;
		synchronized(receivedMessages){
			messages = new ArrayList<Object>(receivedMessages);
			receivedMessages.clear();
		}
		return new MessagingStepResult(messages);
	}
	
	@Override
	public boolean isStarted() {
		return isStarted;
	}
	
	private TibrvMsg constructTibrvMsg(Object message) throws TibrvException{
		TibrvMsg msg = new TibrvMsg();
    	if(message instanceof String){
    		//Sample string message: BID1=20|BID2=20.1|ASK=21        		
    		String[] fields = ((String)message).split("\\" + MapMessageTemplateHelper.MESSAGE_FIELD_SEP);
    		for(String fieldStr : fields){
    			String[] keyValue = fieldStr.split("=");
    			String key = keyValue[0].trim();

    			if(msg.getField(key) != null){
    				msg.removeField(key);
    			}
    			msg.add(key, keyValue[1].trim());
    		}
    	} 
    	else {
    		try {
        		HashMap<String, Object> data = (HashMap<String, Object>)message;
        		for(Entry<String, Object> entry: data.entrySet()){
        			msg.add(entry.getKey(), entry.getValue());
        		}
			} catch (Exception e) {
				logger.error(String.format("Failed to extract data from input hash map, error: %s", e.getMessage()));
				throw e;
			}
    	}
		
		return msg;
	}
	
	public static String convertTibrvMsgToString(TibrvMsg msg){
		StringBuilder builder = new StringBuilder();
		try{
			for(int i = 0; i < msg.getNumFields(); i++){
				TibrvMsgField field = msg.getFieldByIndex(i);
				builder.append(String.format("%s=%s%s", field.name, field.data.toString(), MapMessageTemplateHelper.MESSAGE_FIELD_SEP));
			}
			if(builder.length() > 0){
				builder.replace(builder.lastIndexOf(MapMessageTemplateHelper.MESSAGE_FIELD_SEP), builder.length(), "");
			}
		}
		catch(Exception ex){
			builder.append(ex.getMessage());
		}
		
		return builder.toString();
	}
	
	public String constuctFullUserStr(ScenarioContext scenarioContext, String templateName, String userstr){
		MapMessageTemplate userMessage = MapMessageTemplateHelper.generateTemplate(templateName, userstr, messageTemplates, null);
		return scenarioContext.processString(userMessage.getFieldValueString());
	}
	
	public TibcoRvStepResult receive(ScenarioContext scenarioContext, String templateName, String userstr) throws ValidationFailed{
		MapMessageTemplate selectionCondition = MapMessageTemplateHelper.generateTemplate(templateName, userstr, messageTemplates, null);
		MapMessageTemplate processedSelectionCondition = MapMessageTemplateHelper.processTemplate(scenarioContext, selectionCondition);
		ArrayList<Object> messages = null;
		synchronized(receivedMessages){
			messages = new ArrayList<Object>(receivedMessages);
			receivedMessages.clear();
		}
				
		for(Object obj : messages){
			TibrvMsg msg = (TibrvMsg)obj;
			if(validateMessage(msg, processedSelectionCondition)){
				return new TibcoRvStepResult(msg);
			}
		}
		
		throw new ValidationFailed("No received message match the given condition");
	}		
	
	public String validate(ScenarioContext scenarioContext, String templateName, String userstr, TibcoRvStepResult result) throws ValidationFailed, TibrvException{
		MapMessageTemplate expected = MapMessageTemplateHelper.generateTemplate(templateName, userstr, messageTemplates, null);
		MapMessageTemplate processedExpected = MapMessageTemplateHelper.processTemplate(scenarioContext, expected);		

		StringBuilder logBuilder = new StringBuilder();		
		logBuilder.append(String.format("Actual:%s%nExpected:%s%n", result.toString(), convertTibrvMsgToString(constructTibrvMsg(processedExpected.getFieldValueString()))));

		StringBuilder diffBuilder = new StringBuilder();
		validateMessage(result, processedExpected, diffBuilder);

		if (diffBuilder.length() > 0) {
			throw new ValidationFailed(String.format("Validation Failed:%n%sDifferences:%n%s", logBuilder.toString(), diffBuilder.toString()));
		}

		return logBuilder.toString();
	}
	
	public void validateMessage(TibcoRvStepResult actualMessage, MapMessageTemplate condition, StringBuilder diffBuilder) throws ValidationFailed {
		for(Entry<String, Map<String, String>> entry : condition.getAllValidateFields().entrySet()){
			try {
				if(!MapMessageTemplateHelper.validateWithField(actualMessage.getFieldValue(entry.getKey()), entry.getValue().get(MapMessageTemplateHelper.VALIDATE_VALUE)
						, entry.getValue().get(MapMessageTemplateHelper.VALIDATE_DATATYPE), entry.getValue().get(MapMessageTemplateHelper.VALIDATE_OPERATOR))){
					diffBuilder.append(entry.getKey() + "=(" + actualMessage.getFieldValue(entry.getKey()) + ")(" + entry.getValue().get(MapMessageTemplateHelper.VALIDATE_VALUE) + ")(" + 
							entry.getValue().get(MapMessageTemplateHelper.VALIDATE_OPERATOR).toUpperCase() + ")\n");
				}
			} catch (Throwable e) {
				throw new ValidationFailed(e.getMessage());
			}
		}		
	}
	
	public boolean validateMessage(TibrvMsg actualMessage, MapMessageTemplate condition) {
		for(Entry<String, Map<String, String>> entry : condition.getAllValidateFields().entrySet()){
			try {
				if(!MapMessageTemplateHelper.validateWithField(actualMessage.get(entry.getKey()).toString(), entry.getValue().get(MapMessageTemplateHelper.VALIDATE_VALUE)
						, entry.getValue().get(MapMessageTemplateHelper.VALIDATE_DATATYPE), entry.getValue().get(MapMessageTemplateHelper.VALIDATE_OPERATOR))){
					return false;
				}
			} catch (Exception e) {
				return false;
			}
		}		
		
		return true;
	}
}
