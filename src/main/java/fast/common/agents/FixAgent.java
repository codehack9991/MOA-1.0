
package fast.common.agents;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Map;

import javax.xml.xpath.XPathExpressionException;

import fast.common.agents.messaging.IMessagingAgent;
import fast.common.context.FixStepResult;
import fast.common.context.MapMessageTemplateHelper;
import fast.common.context.ScenarioContext;
import fast.common.context.StepResult;
import fast.common.core.Configurator;
import fast.common.fix.FixHelper;
import fast.common.fix.MessageIncorrect;
import fast.common.logging.FastLogger;
import fast.common.replay.MessageMissing_ReplayException;
import fast.common.agents.messaging.MessagingException;
import quickfix.FieldNotFound;
import quickfix.InvalidMessage;
/**
 * The {@code FixAgent} class defines common methods to send and receive/verify messages for messaging agents
 * <p>Such messaging agent includes: {@link fast.common.agents.messaging.KafkaAgent},
 * {@link fast.common.agents.messaging.MessagingQueueAgent},
 * {@link fast.common.agents.messaging.TcpClientAgent},
 * {@link fast.common.agents.messaging.TibcoEmsAgent},
 * {@link fast.common.agents.messaging.TibcoRvAgent}, 
 * {@link fast.common.agents.messaging.UltraMessageAgent}</p>
 * <p>Details information for using an FixAgent can see: 
 * <p><a href="https://cedt-confluence.nam.nsroot.net/confluence/display/167813001/Message+Agent+Config+Settings">Examples</a></p>
 * 
 * @author QA Framework Team
 * @since 1.5
 */
public class FixAgent extends Agent implements IFixMessagingAgent {
	 private FastLogger _logger;
	 FixHelper _fixHelper;
	 final ArrayList<quickfix.Message> _receivedMessages = new ArrayList<quickfix.Message>();
	 Map _fieldMatchers;
	 String _sendExtraTags;
	 String _receiveExtraTags;
	 IMessagingAgent _messagingAgent;
	 
    /**
     * Constructs a new <tt>FixAgent</tt> with
     * default configuration file (config.yml) and custom configuration files to
     * fetch required parameters.
     * <p>The constructor initializes a messaging agent is really used to send and receive messages.</p>
     * @param   name a string for naming the creating FixAgent 
     * @param   agentParams a map to get the required parameters for creating a FixAgent 
     * @param   configurator a Configurator instance to provide configuration info for the actions of the FixAgent
     * 
     * @since 1.5
     */

	public FixAgent(String name, Map agentParams, Configurator configurator) throws Exception  {
		super(name, agentParams, configurator);
		 _logger = FastLogger.getLogger(String.format("%s:FixAgent", _name));
		 _fixHelper = new FixHelper(agentParams, configurator);
		 
		 _sendExtraTags = Configurator.getStringOr(_agentParams, "send_extra_tags", null);
		 _receiveExtraTags = Configurator.getStringOr(_agentParams, "receive_extra_tags", null);		
		 
		 String messaingAgentName = Configurator.getStringOr(_agentParams, "messaging_agent", null);		 
		 _messagingAgent = AgentsManager.getAgent(messaingAgentName);
	}
	/**
	 * Close a messaging agent
	 */
	@Override
	public void close() throws Exception {
		if(_messagingAgent != null){
			_messagingAgent.close();
		}		
	}
	/**
	 * Starts a messaging agent
	 */
	public void start() throws Exception {
		if(_messagingAgent == null){
			throw new NullPointerException("messaging agent is null");
		}
		if (!_messagingAgent.isStarted())
			_messagingAgent.start();
	}
	
	private void _sendMessage(quickfix.Message message) throws Exception {
        synchronized (this) {
            if (!_messagingAgent.isStarted()) {
                this.start();
            }

            String rawstr = message.toString();
            _logger.debug(String.format("SENDING userstr=[%s],%nrawstr=[%s]%n",
                    _fixHelper.convertRawstrToUserstr(rawstr), rawstr)); //_producer.getDestination().toString(),    
            _messagingAgent.send(rawstr);            
        }
    }
	/**
	 * <p>Converts template, user string and extra tags into a message</p>
	 * <p>Gets the template and extra tags from configuration file, the user string comes from parameter</p>
	 * @param scenarioContext 
	 * @param templateName is used to create a message
	 * @param varName a variable to save the transmitted message
	 * @param userstr is used to concatenate to msgTemplate for creating a message
	 * @throws Exception
	 * @since 1.5
	 * @see fast.common.glue.CommonStepDefs#sendFixMessage(String, String, String, String)
	 */
	public FixStepResult sendMessage(ScenarioContext scenarioContext, String templateName, String varName, String userstr) throws Exception, InvalidMessage {
        quickfix.Message msg = _fixHelper.convertUserstrToMessage(scenarioContext, templateName, userstr, _sendExtraTags);
        _sendMessage(msg);
        FixStepResult result = new FixStepResult(msg, _fixHelper);
        _fixHelper.saveResult(result, scenarioContext, templateName, varName);
        return result;
    }
	/**
	 * <p>Finds the best match message from the received messages with the template, user string and extra tags.</p>
	 * <p>Gets the template and extra tags from configuration file, the user string comes from parameter.</p>
	 * <p>The excepted message is converted from template, user string and extra tags.</p>
	 * <p>The received messages are received by messaging agents.</p>
	 * @param scenarioContext 
	 * @param templateName is used to create a message
	 * @param varName a variable to save the transmitted message
	 * @param userstr is used to concatenate to msgTemplate for creating a message
	 * @throws MessagingException, InterruptedException, XPathExpressionException, InvalidMessage, FieldNotFound
	 * @since 1.5
	 * @see fast.common.glue.CommonStepDefs#receiveFixMessage(String, String, String, String)
	 */
    public FixStepResult receiveMessage(ScenarioContext scenarioContext, String templateName, String varName, String userstr) throws MessagingException, InterruptedException, XPathExpressionException, InvalidMessage, FieldNotFound {
    	synchronized (_receivedMessages) {
            try {
            	ArrayList<Object> rawMessages = _messagingAgent.receive().getMessages();
            	for(int i=0; i< rawMessages.size(); i++){
            		String message=(String)rawMessages.get(i);
            		quickfix.Message qf_msg = _fixHelper.convertFulluserstrToMessage(null, MapMessageTemplateHelper.setMessageFieldSep(message)); // it will now consider dictionary for repeating groups
                   _receivedMessages.add(qf_msg);
            	}                
            } catch (InvalidMessage invalidMessage) {
                throw invalidMessage;
            }
        }

        quickfix.Message msg = _fixHelper.convertUserstrToMessage(scenarioContext, templateName, userstr, _receiveExtraTags);
        String rawstr = msg.toString();
        _logger.debug(String.format("CHECK RECEIVED userstr=[%s],%nrawstr=[%s]%n",
                _fixHelper.convertRawstrToUserstr(rawstr), rawstr));

        return _fixHelper.receiveMessageAndSaveResult(_receivedMessages, scenarioContext, templateName, varName, msg);
    }

    @Override
	public FixStepResult receiveAndVerifyMessage(ScenarioContext scenarioContext, String templateName, String varName,
			String userStr) throws Throwable {
    	synchronized (_receivedMessages) {
            try {
            	ArrayList<Object> rawMessages = _messagingAgent.receive().getMessages();
            	for(int i=0; i< rawMessages.size(); i++){
            		String message=(String)rawMessages.get(i);
            		quickfix.Message quickMessage = _fixHelper.convertFulluserstrToMessage(null, MapMessageTemplateHelper.setMessageFieldSep(message)); // it will now consider dictionary for repeating groups
                    _receivedMessages.add(quickMessage);
            	}                
            } catch (InvalidMessage invalidMessage) {
                throw invalidMessage;
            }
        }

        return _fixHelper.receiveAndVerifyMessage(_receivedMessages, scenarioContext, templateName, varName, userStr,_receiveExtraTags);
	}
    
    public void setOrderBook(ScenarioContext scenarioContext, String templateSetName, String orderBook, String userStr) throws Throwable {
        _fixHelper.setOrderBook(scenarioContext, this, templateSetName, orderBook, userStr);
    }
    /**
	 * Sends a raw fix message
	 * @param rawMsg convert this raw fix string to fix message then send to target server
	 * @throws Throwable
	 * @since 1.5
     * @see fast.common.glue.CommonStepDefs#sendRawFixMessage(String, String, String)
     */
	@Override
	public FixStepResult sendRawMessage(String rawMsg) throws Throwable {
		quickfix.Message msg = _fixHelper.createFixMessage(rawMsg);
        _sendMessage(msg);
        FixStepResult result = new FixStepResult(msg, _fixHelper);
        return result;
	}
	/**
	 * Checks the messaging agent does not receive the specified message
	 * @param scenarioContext call process string method to process tag value
	 * @param msgName a msgTemplate, is used to create the excepted message
	 * @param userstr is used to concatenate to msgTemplate for creating the excepted message
	 * @throws Throwable
	 * @since 1.5
	 * @see fast.common.glue.CommonStepDefs#notReceiveFixMessage(String, String, String, String)
	 */
	@Override
	public void notReceiveMessage(ScenarioContext scenarioContext, String msgName, String userstr) throws Throwable {
		if(!_messagingAgent.isStarted()) {
            throw new RuntimeException("agent is not configured for RECEIVE-based action");
        }

        quickfix.Message msg = _fixHelper.convertUserstrToMessage(scenarioContext, msgName, userstr, null);// DONE: we shall not use extra tags here - in NOT RECEIVE command
        String rawstr = msg.toString();
        _logger.debug(String.format("CHECK NOT RECEIVED userstr=[%s],%nrawstr=[%s]%n",
                _fixHelper.convertRawstrToUserstr(rawstr), rawstr));

        try {
            quickfix.Message foundMessage = _fixHelper.checkReceived(_receivedMessages, msg);
        }
        catch (MessageIncorrect e) {
            // as we expected
            return;
        }
        catch (MessageMissing_ReplayException e) {
            // as we expected
            return;
        }

        throw new RuntimeException("We received message when we didn't expect it");
	}
	/**
	 *  Converts a raw fix string to a fix message
	 *  @param rawMsg fix string to convert
	 *  @throws InvalidMessage
	 *  @since 1.5
	 *  @see fast.common.glue.CommonStepDefs#setToFixMessage(String, String, String)
	 */
	@Override
	public StepResult createFixMessage(String rawMsg) throws InvalidMessage {
		quickfix.Message msg = _fixHelper.createFixMessage(rawMsg);
        FixStepResult result = new FixStepResult(msg, _fixHelper);
        return result;
	}
	/**
	 * Writes received messages to log
	 */
	@Override
    public void flushBuffersToLog(boolean isSingleTread) {
        synchronized (_receivedMessages) {
            _logger.info(String.format("Agent had %d received messages in buffer:", _receivedMessages.size()));

            int i = 1;
            for(quickfix.Message msg: _receivedMessages) {
                _logger.debug(String.format("Message #%d: %s", i, msg.toString()));
                i++;
            }
        }
    }

	@Override
	public boolean isStarted() {
		return _messagingAgent == null ? false : _messagingAgent.isStarted();
	}
	@Override
	public FixStepResult sendMultiMessagesInTimeWindow(ScenarioContext scenarioContext,
			int messageCount, int timeWindow, String msgTemplate, String userstr) throws Exception{
		ArrayList<quickfix.Message> multipleMsg = new ArrayList<>();
		for (int i = 0; i < messageCount; i++) {
			quickfix.Message msg = _fixHelper.convertUserstrToMessage(
					scenarioContext, msgTemplate, userstr, _sendExtraTags);
			multipleMsg.add(msg);
		}
		LocalDateTime startTime = LocalDateTime.now();
		long timeWait = (timeWindow*1000-2*messageCount)/messageCount;
		for (quickfix.Message s : multipleMsg) {
			_sendMessage(s);
			if(timeWait>0)Thread.sleep(timeWait);
		}
		LocalDateTime endTime = LocalDateTime.now();
		Duration duration = Duration.between(startTime, endTime);
		_logger.debug("Totally takes " + duration.toMillis() +" milliseconds !!!");
		return new FixStepResult(multipleMsg, _fixHelper);	
	}	
}

