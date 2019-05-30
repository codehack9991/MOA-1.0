package fast.common.agents.messaging;

import java.util.ArrayList;
import java.util.Map;

import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.QueueConnectionFactory;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.apache.activemq.ActiveMQConnectionFactory;

import com.tibco.tibjms.TibjmsQueueConnectionFactory;

import fast.common.agents.Agent;
import fast.common.context.MessagingStepResult;
import fast.common.core.Configurator;
import fast.common.logging.FastLogger;
import quickfix.ConfigError;

public class MessagingQueueAgent extends Agent implements javax.jms.MessageListener, IMessagingAgent {
	private FastLogger logger;
	final ArrayList<String> receivedMessages = new ArrayList<>();

	private String sendHost;
	private String sendUser;
	private String sendPassword;
	private String sendQueueName;
	private String sendQueueType;
	private String receiveHost;
	private String receiveUser;
	private String receivePassword;
	private String receiveQueueName;
	private String receiveQueueType;

	private Connection sendConnection;
	private Connection receiveConnection;
	private Session sendSession;
	private Session receiveSession;
	private MessageProducer producer;
	private MessageConsumer consumer;
	private boolean isStarted = false;

	public MessagingQueueAgent(String name, Map<?, ?> agentParams, Configurator configurator) throws ConfigError {
		super(name, agentParams, configurator);
		logger = FastLogger.getLogger(String.format("%s:MessagingQueueAgent", name));
		String host = Configurator.getStringOr(agentParams, "host", null);
		String user = Configurator.getStringOr(agentParams, "user", null);
		String password = Configurator.getStringOr(agentParams, "password", null);
		String send_queue_name = Configurator.getStringOr(agentParams, "send_queue_name", null);
		String receive_queue_name = Configurator.getStringOr(agentParams, "receive_queue_name", null);
		String send_queue_type = Configurator.getStringOr(agentParams, "send_queue_type", null);
		String receive_queue_type = Configurator.getStringOr(agentParams, "receive_queue_type", null);

		sendHost = host;
		sendUser = user;
		sendPassword = password;
		sendQueueName = send_queue_name;
		sendQueueType = send_queue_type;
		
		Object sendObj = agentParams.get("send");
		if (sendObj != null) {
			Map<?, ?> sendMap = (Map<?, ?>) sendObj;
			sendHost = Configurator.getStringOr(sendMap, "host", sendHost);
			sendUser = Configurator.getStringOr(sendMap, "user", sendUser);
			sendPassword = Configurator.getStringOr(sendMap, "password", sendPassword);
			sendQueueName = Configurator.getStringOr(sendMap, "queuename", sendQueueName);
			sendQueueType = Configurator.getStringOr(sendMap, "queuetype", sendQueueType);
		}

		receiveHost = host;
		receiveUser = user;
		receivePassword = password;
		receiveQueueName = receive_queue_name;
		receiveQueueType = receive_queue_type;

		Object receiveObj = agentParams.get("receive");
		if (receiveObj != null) {
			Map<?, ?> receiveMap = (Map<?, ?>) receiveObj;
			receiveHost = Configurator.getStringOr(receiveMap, "host", receiveHost);
			receiveUser = Configurator.getStringOr(receiveMap, "user", receiveUser);
			receivePassword = Configurator.getStringOr(receiveMap, "password", receivePassword);
			receiveQueueName = Configurator.getStringOr(receiveMap, "queuename", receiveQueueName);
			receiveQueueType = Configurator.getStringOr(receiveMap, "queuetype", receiveQueueType);

		}
	}

	@Override
	public void send(Object message) throws MessagingException {
		if (!isStarted()) {
            this.start();
        }
		try {
			Message msg = null;
			if (message instanceof String) {
				msg = sendSession.createTextMessage();
				((TextMessage) msg).setText((String) message);
			} else {
				throw new MessagingException("Non-Text message is not supported for now");
			}
			producer.send(msg);
		} catch (JMSException ex) {
			throw new MessagingException(ex);
		}

	}

	@Override
	public MessagingStepResult receive() throws MessagingException {
		if (receiveConnection == null) {
			throw new RuntimeException("agent is not configured for RECEIVE-based action");
		}
		ArrayList<Object> result;
		synchronized (receivedMessages) {
			result = new ArrayList<Object>(receivedMessages);
			receivedMessages.clear();
		}
		return new MessagingStepResult(result);
	}

	@Override
	public void start() throws MessagingException {
		synchronized (this) {
			try {
				if (sendQueueName != null) {
					QueueConnectionFactory factory = getConnectionFactory(sendQueueType,sendHost);
					if (factory == null)
						return;
					sendConnection = factory.createConnection(sendUser, sendPassword);
					sendSession = sendConnection.createSession(false, Session.AUTO_ACKNOWLEDGE);
					Queue sendQueue = sendSession.createQueue(sendQueueName);
					logger.info(String.format("host: '%s', user: '%s', password: '%s', queue: '%s'", sendHost, sendUser,
							sendPassword, sendQueueName));
					producer = sendSession.createProducer(sendQueue);
					sendConnection.start();
				}
				if (receiveQueueName!= null) {
					QueueConnectionFactory factory = getConnectionFactory(receiveQueueType,receiveHost);
					if (factory == null)
						return;
					receiveConnection = factory.createConnection(receiveUser, receivePassword);
					receiveSession = receiveConnection.createSession(false, Session.AUTO_ACKNOWLEDGE);
					Queue receiveQueue = receiveSession.createQueue(receiveQueueName);
					consumer = receiveSession.createConsumer(receiveQueue);
					consumer.setMessageListener(this);
					receiveConnection.start();
				}

				if ((receiveConnection == null) && (sendConnection == null)) {
					throw new RuntimeException(
							"Agent is not configured neither for SEND nor for RECEIVE-based actions - which must be a configuration error because such agent is useless. Please check your configuration.");
				}

			} catch (Exception ex) {
				receiveConnection=null;
				sendConnection=null;
				logger.error(String.format("connect failure: %s", ex.toString()));
				throw new MessagingException(ex);
			}
		}
		isStarted = true;
	}

	@Override
	public void close() throws Exception {
		if (sendConnection != null) {
			sendConnection.close();
		}
		if (receiveConnection != null) {
			receiveConnection.close();
		}
		synchronized (receivedMessages) {
			receivedMessages.clear();
		}
        isStarted = false;
	}

	@Override
	public void onMessage(Message message) {
		String rawstr = null;
		String queueName = null;

		try {
			rawstr = ((TextMessage) message).getText();
			queueName = message.getJMSDestination().toString();
			logger.debug(String.format("%s: received rawstr=[%s]%n", queueName, rawstr));
			synchronized (receivedMessages) {
				receivedMessages.add(rawstr);
			}
		} catch (JMSException e) {
			logger.error(String.format("onMessage(): raised exception: %s", e.toString()));
		}

	}
   
	private QueueConnectionFactory getConnectionFactory(String queueType,String host){
		switch (queueType.toUpperCase()) {
		case "TIBCOEMS":
			return new TibjmsQueueConnectionFactory(host);
		case "ACTIVEMQ":
			return new ActiveMQConnectionFactory(host);
		default:
			break;
		}
		
		return null;
		
	}

	@Override
	public boolean isStarted() {
		return isStarted;
	}

}
