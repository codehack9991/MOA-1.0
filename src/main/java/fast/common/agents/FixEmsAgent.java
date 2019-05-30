package fast.common.agents;

import javax.jms.*;
import javax.jms.Message;
import javax.jms.Session;
import javax.xml.xpath.XPathExpressionException;






import org.apache.commons.lang3.NotImplementedException;

import com.tibco.tibjms.TibjmsConnectionFactory;

import fast.common.context.ScenarioContext;
import fast.common.context.StepResult;
import fast.common.fix.MessageIncorrect;
import fast.common.logging.FastLogger;
import fast.common.core.Configurator;
import fast.common.fix.FixHelper;
import fast.common.context.FixStepResult;
import fast.common.replay.MessageMissing_ReplayException;
import quickfix.*;

import java.util.ArrayList;
import java.util.Map;
/**
 * The {@code FixEmsAgent} class defines common methods to send and receive/verify messages based on Tibco Ems.
 * @see {@link fast.common.agents.messaging.TibcoEmsAgent},
 * <p>Details information for using an FixEmsAgent can see:</p> 
 * <p><a href="https://cedt-confluence.nam.nsroot.net/confluence/display/167813001/Message+Agent+Config+Settings">Examples</a></p>
 * 
 * @author QA Framework Team
 * @since 1.5
 */
public class FixEmsAgent extends Agent implements javax.jms.MessageListener, IFixMessagingAgent {
	public static final String CONFIG_HOST = "host";
	public static final String CONFIG_USER = "user";
	public static final String CONFIG_PASSWORD = "password";
    FixHelper _fixHelper;
    final ArrayList<quickfix.Message> _receivedMessages = new ArrayList<>();
    Map _fieldMatchers;
    private FastLogger _logger;

    private String _sendHost;
    private String _sendUser;
    private String _sendPassword;
    private String _sendTopicName;

    private String _receiveHost;
    private String _receiveUser;
    private String _receivePassword;
    private ArrayList<String> _receiveTopicNames;

    String _sendExtraTags;
    String _receiveExtraTags;
    private boolean isStarted = false;

    /**
     * Constructs a new <tt>FixEmsAgent</tt> with
     * default configuration file (config.yml) and custom configuration files to
     * fetch required parameters.
 
     * @param   name a string for naming the creating FixEmsAgent 
     * @param   agentParams a map to get the required parameters for creating a FixEmsAgent 
     * @param   configurator a Configurator instance to provide configuration info for the actions of the FixEmsAgent
     * 
     * @since 1.5
     */
    public FixEmsAgent(String name, Map agentParams, Configurator configurator) throws ConfigError  {
        super(name, agentParams, configurator);
        _logger = FastLogger.getLogger(String.format("%s:FixEmsAgent", _name));

        _fixHelper = new FixHelper(agentParams, configurator);

        //start(); we will now start it explicitly or in send/receive

        
        String commonHost = Configurator.getStringOr(_agentParams, CONFIG_HOST, null);
        String commonUser = Configurator.getStringOr(_agentParams, CONFIG_USER, null);
        String commonPassword = Configurator.getStringOr(_agentParams, CONFIG_PASSWORD, null);
        String commonSendTopic = Configurator.getStringOr(_agentParams, "send_topic", null);
        String commonReceiveTopic = Configurator.getStringOr(_agentParams, "receive_topic", null);
        String commonSendExtraTags = Configurator.getStringOr(_agentParams, "send_extra_tags", null);

        _sendHost = commonHost;
        _sendUser = commonUser;
        _sendPassword = commonPassword;
        _sendTopicName = commonSendTopic;
        _sendExtraTags = commonSendExtraTags;

        Object sendObj = _agentParams.get("send");
        if(sendObj != null) {
            Map sendMap = (Map)sendObj;
            _sendHost = Configurator.getStringOr(sendMap, CONFIG_HOST, _sendHost);
            _sendUser = Configurator.getStringOr(sendMap, CONFIG_USER, _sendUser);
            _sendPassword = Configurator.getStringOr(sendMap, CONFIG_PASSWORD, _sendPassword);
            _sendTopicName = Configurator.getStringOr(sendMap, "topic", _sendTopicName);
            _sendExtraTags = Configurator.getStringOr(sendMap, "extra_tags", _sendExtraTags);
        }



        _receiveHost = commonHost;
        _receiveUser = commonUser;
        _receivePassword = commonPassword;
        _receiveTopicNames = new ArrayList<> ();
        if(commonReceiveTopic != null) {
            _receiveTopicNames.add(commonReceiveTopic);
        }
        _receiveExtraTags = null;

        Object receiveObj = _agentParams.get("receive");
        if(receiveObj != null) {
            Map receiveMap = (Map)receiveObj;

            _receiveHost = Configurator.getStringOr(receiveMap, CONFIG_HOST, _receiveHost);
            _receiveUser = Configurator.getStringOr(receiveMap, CONFIG_USER, _receiveUser);
            _receivePassword = Configurator.getStringOr(receiveMap, CONFIG_PASSWORD, _receivePassword);
            _receiveTopicNames = Configurator.getArrayListOr(receiveMap, "topics", _receiveTopicNames);
            _receiveExtraTags = Configurator.getStringOr(receiveMap, "extra_tags", _receiveExtraTags);
        }


    }

    Connection _sendConnection;
    Connection _receiveConnection;

    Session _sendSession; // we need to keep it to create text messages
    MessageProducer _producer; // we may have one producer and may have many consumers


    /**
     * Creates a connection to destination and prepares a session for sending messages by producer or receiving messages by consumer 
     * <p>Consumer sets listener to accept messages once messages arrive</P>
     */
    @Override
    public void start() throws JMSException {
        synchronized (this) {
            try {
                if (_sendTopicName != null) {
                    TibjmsConnectionFactory connectionFactory = new TibjmsConnectionFactory(_sendHost);
                    _sendConnection = connectionFactory.createConnection(_sendUser, _sendPassword);
                    _sendSession = _sendConnection.createSession(false, Session.AUTO_ACKNOWLEDGE);
                    Topic sendTopic = _sendSession.createTopic(_sendTopicName);
					_logger.info("host: '%s', user: '%s', password: '%s', topic: '%s'".format(_sendHost, _sendUser, _sendPassword, _sendTopicName));
                    _producer = _sendSession.createProducer(sendTopic);
                    _sendConnection.start();
                }


                if (!_receiveTopicNames.isEmpty()) {
                    TibjmsConnectionFactory connectionFactory = new TibjmsConnectionFactory(_receiveHost);
                    _receiveConnection = connectionFactory.createConnection(_receiveUser, _receivePassword);
                    Session receiveSession = _receiveConnection.createSession(false, Session.AUTO_ACKNOWLEDGE);

                    for (Object topicObj : _receiveTopicNames) {
                        String receiveTopicName = topicObj.toString();
                        // TODO: add resolving of topic names (string parsing - if it is needed for any projects)

                        Topic receiveTopic = receiveSession.createTopic(receiveTopicName);
                        MessageConsumer consumer = receiveSession.createConsumer(receiveTopic);
                        consumer.setMessageListener(this);
                    }

                    _receiveConnection.start();
                }

                if ((_receiveConnection == null) && (_sendConnection == null)) {
                    throw new RuntimeException("Agent is not configured neither for SEND nor for RECEIVE-based actions - which must be a configuration error because such agent is useless. Please check your configuration.");
                }

            } catch (Exception ex) {
                _receiveConnection = null;
                _sendConnection = null;
                _logger.error("connect failure: %s".format(ex.toString()));
                throw ex;
            }
        }
        isStarted = true;
    }


    private void _sendMessage(quickfix.Message message) throws JMSException {
        synchronized (this) {
            if (_sendConnection == null) {
                this.start();
            }

            String rawstr = message.toString();

            _logger.debug(String.format("SENDING userstr=[%s],%nrawstr=[%s]%n",
                    _fixHelper.convertRawstrToUserstr(rawstr), rawstr)); //_producer.getDestination().toString(),
            TextMessage msg = _sendSession.createTextMessage();
            msg.setText(rawstr);
            _producer.send(msg);
        }
    }
    /**
	 * @see fast.common.glue.CommonStepDefs#sendFixMessage(String, String, String, String)
	 * @see fast.common.agents.FixAgent#sendMessage(ScenarioContext, String, String, String)
	 */
    public FixStepResult sendMessage(ScenarioContext scenarioContext, String templateName, String varName, String userstr) throws JMSException, XPathExpressionException, InvalidMessage {
        quickfix.Message msg = _fixHelper.convertUserstrToMessage(scenarioContext, templateName, userstr, _sendExtraTags);
        _sendMessage(msg);
        FixStepResult result = new FixStepResult(msg, _fixHelper);
        _fixHelper.saveResult(result, scenarioContext, templateName, varName);
        return result;
    }
    /**
	 * @see fast.common.glue.CommonStepDefs#receiveFixMessage(String, String, String, String)
	 * @see fast.common.agents.FixAgent#receiveMessage(ScenarioContext, String, String, String)
	 */
    public FixStepResult receiveMessage(ScenarioContext scenarioContext, String templateName, String varName, String userstr) throws InterruptedException, XPathExpressionException, InvalidMessage, FieldNotFound {
        if(_receiveConnection == null) {
            throw new RuntimeException("agent is not configured for RECEIVE-based action");
        }

        quickfix.Message msg = _fixHelper.convertUserstrToMessage(scenarioContext, templateName, userstr, _receiveExtraTags);
        String rawstr = msg.toString();
        _logger.debug(String.format("CHECK RECEIVED userstr=[%s],%nrawstr=[%s]%n",
                _fixHelper.convertRawstrToUserstr(rawstr), rawstr));

        return _fixHelper.receiveMessageAndSaveResult(_receivedMessages, scenarioContext, templateName, varName, msg);
    }
    
    public FixStepResult receiveAndVerifyMessage(ScenarioContext scenarioContext, String templateName, String varName, String userStr) throws Throwable {
    	quickfix.Message msg = _fixHelper.convertUserstrToMessage(scenarioContext, templateName, userStr, _receiveExtraTags);
        String rawstr = msg.toString();
        _logger.debug(String.format("CHECK RECEIVED rawstr=[%s]%n",rawstr));
        quickfix.Message foundMessage = _fixHelper.checkReceived(_receivedMessages, msg);
        _fixHelper.compareMessage(foundMessage, msg, true);
        FixStepResult result = new FixStepResult(foundMessage, _fixHelper);
        _fixHelper.saveResult(result, scenarioContext, templateName, varName);
        return result;
        
    }
    /**
	 * @see fast.common.glue.CommonStepDefs#notReceiveFixMessage(String, String, String, String)
	 * @see fast.common.agents.FixAgent#notReceiveMessage(ScenarioContext, String, String)
	 */
    public void notReceiveMessage(ScenarioContext scenarioContext, String msgName, String userstr) throws Exception {
        if(_receiveConnection == null) {
            throw new RuntimeException("agent is not configured for RECEIVE-based action");
        }

        quickfix.Message msg = _fixHelper.convertUserstrToMessage(scenarioContext, msgName, userstr, null);// DONE: we shall not use extra tags here - in NOT RECEIVE command
        String rawstr = msg.toString();
        _logger.debug(String.format("CHECK NOT RECEIVED userstr=[%s],%nrawstr=[%s]%n",
                _fixHelper.convertRawstrToUserstr(rawstr), rawstr));

        try {
             _fixHelper.checkReceived(_receivedMessages, msg);
        }
        catch (MessageIncorrect | MessageMissing_ReplayException e) {
            // as we expected
            return;
        }

        throw new RuntimeException("We received message when we didn't expect it");
    }

    public void setOrderBook(ScenarioContext scenarioContext, String templateSetName, String orderBook, String userStr) throws Throwable {
        _fixHelper.setOrderBook(scenarioContext, this, templateSetName, orderBook, userStr);
    }
    /**
     * Accepts messages once discover the destination of the messages is the received topic
     * <p>Stores all received messages into an array list</p> 
     */
    @Override
    public void onMessage(Message message) {
        String dedup_rawstr = null;
        String topic = null;
        try {
            String rawstr = ((TextMessage)message).getText();
            topic = message.getJMSDestination().toString();

            _logger.debug(String.format("%s: received userstr=[%s],%nrawstr=[%s]%n",
                    topic, _fixHelper.convertRawstrToUserstr(rawstr), rawstr));
            dedup_rawstr = _fixHelper.removeDuplicateNonRepeatingGroupTags(rawstr); // it should now not have duplicate tags outside of repeating groups!
        } catch (JMSException e) {
            _logger.error(String.format("onMessage(): raised exception: %s", e.toString()));
        }



        synchronized (_receivedMessages) {
            try {
                quickfix.Message qf_msg = _fixHelper.createFixMessage(dedup_rawstr); // it will now consider dictionary for repeating groups
                _receivedMessages.add(qf_msg);
            } catch (InvalidMessage invalidMessage) {
                _logger.error(invalidMessage.getMessage());
            }
        }
    }
    /**
     * Closes the connection
     */
    @Override
    public void close() throws Exception {
      
        if(_sendConnection != null) {
            _sendConnection.close();
        }
        if(_receiveConnection != null) {
            _receiveConnection.close();
        }

        synchronized (_receivedMessages) {
            _receivedMessages.clear();
        }
    }
    /**
     * Prints the logs
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
    /**
	 * @see fast.common.agents.FixAgent#createFixMessage(String)
	 */
    public StepResult createFixMessage(String rawMsg) throws InvalidMessage { // for debugging
        quickfix.Message msg = _fixHelper.createFixMessage(rawMsg);
        return new FixStepResult(msg, _fixHelper);
    }
    /**
	 * @see fast.common.glue.CommonStepDefs#sendRawFixMessage(String, String, String)
	 * @see fast.common.agents.FixAgent#sendRawMessage(String)
	 */
    public FixStepResult sendRawMessage(String rawMsg) throws Throwable { // for debugging
        quickfix.Message msg = _fixHelper.createFixMessage(rawMsg);
        _sendMessage(msg);
        return new FixStepResult(msg, _fixHelper);
    }

	/**
	 * Checks whether the agent is started
	 */
	@Override
	public boolean isStarted() {
		return isStarted;
	}


	@Override
	public FixStepResult sendMultiMessagesInTimeWindow(ScenarioContext scenarioContext,
			int messageCount, int timeWindow, String msgTemplate,String userstr) {
		 throw new NotImplementedException("FixEmsAgent is not supported to send multiple message in time window now!");	
	}
}
