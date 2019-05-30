package fast.common.agents.messaging;

import java.util.ArrayList;
import java.util.Map;

import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.jms.Topic;

import org.apache.hadoop.classification.InterfaceAudience.Private;

import com.tibco.tibjms.TibjmsConnectionFactory;

import fast.common.agents.Agent;
import fast.common.context.MessagingStepResult;
import fast.common.core.Configurator;
import fast.common.logging.FastLogger;

public class TibcoEmsAgent extends Agent implements javax.jms.MessageListener, IMessagingAgent{
	private FastLogger _logger;
	final ArrayList<String> _receivedMessages = new ArrayList<>();
	
	private String _sendHost;
    private String _sendUser;
    private String _sendPassword;
    private String _sendTopicName;

    private String _receiveHost;
    private String _receiveUser;
    private String _receivePassword;
    private ArrayList<String> _receiveTopicNames;
    
    Connection _sendConnection;
    Connection _receiveConnection;

    Session _sendSession; // we need to keep it to create text messages
    MessageProducer _producer; // we may have one producer and may have many consumers
    
    private boolean isStarted = false;
	public TibcoEmsAgent(String name, Map agentParams, Configurator configurator) {
		super(name, agentParams, configurator);
		_logger = FastLogger.getLogger(String.format("%s:FixEmsAgent", _name));
		// TODO Auto-generated constructor stub
		
		String commonHost = Configurator.getStringOr(_agentParams, "host", null);
        String commonUser = Configurator.getStringOr(_agentParams, "user", null);
        String commonPassword = Configurator.getStringOr(_agentParams, "password", null);
        String commonSendTopic = Configurator.getStringOr(_agentParams, "send_topic", null);
        String commonReceiveTopic = Configurator.getStringOr(_agentParams, "receive_topic", null);        

        _sendHost = commonHost;
        _sendUser = commonUser;
        _sendPassword = commonPassword;
        _sendTopicName = commonSendTopic;        

        Object sendObj = _agentParams.get("send");
        if(sendObj != null) {
            Map sendMap = (Map)sendObj;
            _sendHost = Configurator.getStringOr(sendMap, "host", _sendHost);
            _sendUser = Configurator.getStringOr(sendMap, "user", _sendUser);
            _sendPassword = Configurator.getStringOr(sendMap, "password", _sendPassword);
            _sendTopicName = Configurator.getStringOr(sendMap, "topic", _sendTopicName);            
        }



        _receiveHost = commonHost;
        _receiveUser = commonUser;
        _receivePassword = commonPassword;
        _receiveTopicNames = new ArrayList<String> ();
        if(commonReceiveTopic != null) {
            _receiveTopicNames.add(commonReceiveTopic);
        }

        Object receiveObj = _agentParams.get("receive");
        if(receiveObj != null) {
            Map receiveMap = (Map)receiveObj;

            _receiveHost = Configurator.getStringOr(receiveMap, "host", _receiveHost);
            _receiveUser = Configurator.getStringOr(receiveMap, "user", _receiveUser);
            _receivePassword = Configurator.getStringOr(receiveMap, "password", _receivePassword);
            _receiveTopicNames = Configurator.getArrayListOr(receiveMap, "topics", _receiveTopicNames);            
        }
	}
	
	@Override
    public void start() throws MessagingException {
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


                if (_receiveTopicNames.size() > 0) {
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
                throw new MessagingException(ex);
            }
        }
        isStarted = true;
    }

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
	        isStarted = false;
	}

	@Override
	public void send(Object message) throws MessagingException {
		if (!isStarted()) {
            this.start();
        }
		try{		
			Message msg = null;
			if(message instanceof String){
				msg = _sendSession.createTextMessage();
		        ((TextMessage)msg).setText((String)message);       
			}
			else {
				throw new MessagingException("Non-Text message is not supported for now");
			}
	        
	        _producer.send(msg);
		}
		catch(JMSException ex){
			throw new MessagingException(ex);
		}
	}

	@Override
	public MessagingStepResult receive() throws MessagingException {
		if(_receiveConnection == null) {
            throw new RuntimeException("agent is not configured for RECEIVE-based action");
        }
		
		ArrayList<Object> result;
		synchronized (_receivedMessages) {
			result = new ArrayList<Object>(_receivedMessages);
			_receivedMessages.clear();
		}
		return new MessagingStepResult(result);
	}

	@Override
	public void onMessage(Message message) {
		// TODO Auto-generated method stub
		String rawstr = null;
        String topic = null;
        try {
            rawstr = ((TextMessage)message).getText();
            topic = message.getJMSDestination().toString();
            
            _logger.debug(String.format("%s: received rawstr=[%s]%n",
                    topic, rawstr));
            
            synchronized (_receivedMessages) {            
                _receivedMessages.add(rawstr);
            }
        } catch (JMSException e) {
            _logger.error(String.format("onMessage(): raised exception: %s", e.toString()));
        }
	}

	@Override
	public boolean isStarted() {
		return isStarted;
	}
	
	/*@Override
    public void flushBuffersToLog(boolean isSingleTread) {
        synchronized (_receivedMessages) {
            _logger.info(String.format("Agent had %d received messages in buffer:", _receivedMessages.size()));

            int i = 1;
            for(String msg: _receivedMessages) {
                _logger.info(String.format("Message #%d: %s", i, msg.toString()));
                i++;
            }
        }
    }*/
}
