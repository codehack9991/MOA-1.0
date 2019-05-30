package fast.common.agents.messaging;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import fast.common.agents.Agent;
import fast.common.context.MessagingStepResult;
import fast.common.core.Configurator;
import fast.common.logging.FastLogger;

public class UltraMessageAgent extends Agent implements IMessagingAgent {

	protected static final String DEFAULT_29WEST_SERVER = "168.72.170.69";
	protected static final int DEFAULT_29WEST_PORT = 62005;

	protected static final String CMD_CONFIG = "config";
	protected static final String CMD_TOPIC = "topic";
	protected static final String CMD_LISTENSTART = "lstart";
	protected static final String CMD_LISTENSTOP = "lstop";
	protected static final String CMD_PUBLISH = "publish";
	protected static final String CMD_CLOSE = "close";
	protected static final String CMD_HEARTBEAT = "heartbeat";
	protected static final String CONFIG_SERVER = "server";
	protected static final String CONFIG_PORT = "port";
	protected static final String CONFIG_CONFIG = "config";
	protected static final String CONFIG_SENDTOPIC = "send_topic";
	protected static final String CONFIG_RECEIVETOPICS = "receive_topics";
	protected static final String FIX_HEARTBEAT = "35=0";	

	private final FastLogger logger;

	private String server;
	private int port;
	private String config;

	private List<UltraMessageTopic> receiveTopics;
	private UltraMessageTopic sendTopic;

	private String sendTopicName;

	private List<String> receiveTopicNames;

	private TcpClientAgent client;

	public String getName() {
		return this._name;
	}

	@SuppressWarnings("unchecked")
	public Map<String, Object> getAgentParams() {
		return this._agentParams;
	}

	public Configurator getConfigurator() {
		return this._configurator;
	}

	public FastLogger getLogger() {
		return this.logger;
	}
	
	protected static String getMessageSeparator(){
		return System.getProperty("line.separator");
	}
	
	public UltraMessageAgent(){
		logger = FastLogger.getLogger(UltraMessageAgent.class.getName());
	}

	public UltraMessageAgent(String name, Map<String, Object> agentParams, Configurator configurator) {
		super(name, agentParams, configurator);
		logger = FastLogger.getLogger(String.format("%s:UltraMessageAgent", _name));
		this.server = Configurator.getStringOr(_agentParams, CONFIG_SERVER, null);
		this.port = Configurator.getInt(_agentParams, CONFIG_PORT);
		this.sendTopicName = Configurator.getStringOr(_agentParams, CONFIG_SENDTOPIC, null);
		this.receiveTopicNames = Configurator.getArrayListOr(_agentParams, CONFIG_RECEIVETOPICS, null);
		this.config = Configurator.getStringOr(_agentParams, CONFIG_CONFIG, null);

		if (receiveTopicNames == null || receiveTopicNames.isEmpty()) {
			this.receiveTopicNames = new ArrayList<>();
			String receiveTopic = Configurator.getStringOr(_agentParams, CONFIG_RECEIVETOPICS, null);
			if (receiveTopic != null && !receiveTopic.isEmpty()) {
				this.receiveTopicNames.add(receiveTopic);
			}
		}

		server = (server == null || server.isEmpty() ? DEFAULT_29WEST_SERVER : this.server);
		port = (port <= 0 ? DEFAULT_29WEST_PORT : this.port);
	}

	public boolean isConnected() {
		try {
			this.client.send(UltraMessageAgent.CMD_HEARTBEAT);
			this.client.send(UltraMessageAgent.getMessageSeparator());
			return true;
		} catch (Exception ex) {
			return false;
		}
	}

	@Override
	public void send(Object message) throws MessagingException {
		if (this.sendTopic == null) {
			logger.warn("Agent is not started or configured a send topic.");
			return;
		}
		try {
			this.sendTopic.publish((String) message);
		} catch (Exception ex) {
			throw new MessagingException(ex);
		}
	}

	@Override
	public MessagingStepResult receive() throws MessagingException {
		if (this.receiveTopics == null) {
			throw new MessagingException("agent is not started or configured for RECEIVE-based action");
		}

		ArrayList<Object> result = new ArrayList<>();
		for (UltraMessageTopic topic : this.receiveTopics) {
			result.addAll(topic.getReceivedMessages().getMessages());
		}
		return new MessagingStepResult(result);
	}

	@Override
	public void start() throws MessagingException {
		synchronized (this) {
			try {
				if (this.isConnected()) {
					return;
				}

				this.createConnection();

				if (this.sendTopicName != null && !this.sendTopicName.isEmpty()) {
					sendTopic = new UltraMessageTopic(this.server, this.port, this.sendTopicName, this);
					sendTopic.listenStart();
				}

				if (this.receiveTopicNames != null && !this.receiveTopicNames.isEmpty()) {
					UltraMessageTopic topic = null;
					this.receiveTopics = new ArrayList<>();
					for (String topicname : this.receiveTopicNames) {
						topic = topicname.equals(sendTopicName) ? sendTopic
								: new UltraMessageTopic(this.server, this.port, topicname, this);
						this.receiveTopics.add(topic);
					}
					for (UltraMessageTopic t : this.receiveTopics) {
						t.listenStart();
					}
				}

				if ((sendTopic == null) && (receiveTopics == null)) {
					throw new MessagingException(
							"Agent is not configured neither for SEND nor for RECEIVE-based actions - "
									+ "which must be a configuration error because such agent is useless. Please check your configuration.");
				}
			} catch (Exception ex) {
				sendTopic = null;
				receiveTopics = null;
				logger.error(String.format("connect failure: %s", ex.toString()));
				this.close();

				throw new MessagingException(ex);
			}
		}
	}

	@SuppressWarnings("unchecked")
	private void createConnection() throws MessagingException {

		client = new TcpClientAgent(this.server, this.port, this._name, this._agentParams, this._configurator, this.logger);
		client.start();

		if (!this.isConnected()) {
			throw new MessagingException(
					"Agent is not configured correctly for neither server or port. Please check the config file.");
		}

		if (config == null || config.isEmpty()) {
			throw new MessagingException(
					"Agent can not be configured for 'config' param which is necessary. Please check your configuration.");
		}
		try {
			client.send(UltraMessageAgent.CMD_CONFIG + config);
			client.send(UltraMessageAgent.getMessageSeparator());
		} catch (Exception ex) {
			throw new MessagingException("Agent can not complete command set-config." + ex.getMessage());
		}
	}

	@Override
	public void close() {
		try {
			if (this.client != null) {
				this.client.close();
			}
			if (this.sendTopic != null) {
				this.sendTopic.close();
			}
			if (this.receiveTopics != null) {
				for (UltraMessageTopic topic : this.receiveTopics) {
					topic.close();
				}
			}
		} catch (Exception ex) {
			logger.info(ex.getMessage());
		}

	}

	@Override
	public boolean isStarted() {
		return this.isConnected();
	}

}

class UltraMessageTopic {
	private String name;

	private boolean isListening;

	private TcpClientAgent client;

	private UltraMessageAgent agent;

	public UltraMessageTopic(String server, int port, String topic, UltraMessageAgent agent) {

		this.agent = agent;
		this.name = "";
		if (topic == null || topic.isEmpty() || server == null || server.isEmpty() || port <= 0) {
			return;
		}
		try {
			client = new TcpClientAgent(server, port, agent.getName(), agent.getAgentParams(), agent.getConfigurator(),
					agent.getLogger());
			client.start();
			client.send(UltraMessageAgent.CMD_TOPIC + topic);
			client.send(UltraMessageAgent.getMessageSeparator());
			this.name = topic;
		} catch (MessagingException exception) {
			this.agent.getLogger()
					.error(String.format("failed to create topic %s, error:%s", topic, exception.getMessage()));
		}
		if (this.name.isEmpty()) {
			close();
		}
		isListening = false;
	}

	public void listenStart() {
		if (this.client == null || this.isListening)
			return;
		try {
			this.client.send(UltraMessageAgent.CMD_LISTENSTART);
			this.client.send(UltraMessageAgent.getMessageSeparator());
			isListening = true;
		} catch (Exception exception) {
			this.agent.getLogger().error(String.format("starting listener failed: %s", exception.toString()));
		}
	}

	public void listenStop() {
		if (this.client == null || !isListening)
			return;
		try {
			this.client.send(UltraMessageAgent.CMD_LISTENSTOP);
			this.client.send(UltraMessageAgent.getMessageSeparator());
			isListening = false;
		} catch (Exception exception) {
			this.agent.getLogger().error(String.format("stopping listener failed: %s", exception.toString()));
		}
	}

	public void close() {
		this.listenStop();
		if (this.client != null) {
			try {
				this.client.send(UltraMessageAgent.CMD_CLOSE);
				this.client.close();
			} catch (Exception e) {
				this.agent.getLogger().info(e.getMessage());
			}
			this.client = null;
		}
		this.name = null;
	}

	public MessagingStepResult getReceivedMessages() {
		try {
			MessagingStepResult origin = client.receive();
			ArrayList<Object> result = new ArrayList<>();
			if (origin.getMessages() != null) {
				for (Object message : origin.getMessages()) {
					result.addAll(processMessage(message));
				}
			}
			return new MessagingStepResult(result);
		} catch (Exception exception) {
			this.agent.getLogger().error(this.name + " failed: %s" + exception.toString());
			return new MessagingStepResult();
		}
	}

	private ArrayList<Object> processMessage(Object message) {
		String[] lines = message.toString().split(UltraMessageAgent.getMessageSeparator());
		ArrayList<Object> availableLines = new ArrayList<>();
		for (String line : lines) {
			if (line.isEmpty() || line.trim().equals(UltraMessageAgent.FIX_HEARTBEAT)) {
				continue;
			}
			if (!line.endsWith("\001")) {
				line += "\001";
			}
			availableLines.add(line);
		}
		return availableLines;
	}

	public void publish(String message) throws MessagingException {
		if (message == "")
			return;
		if (this.client == null)
			return;
		if (!this.client.isConnected())
			return;
		this.client.send(UltraMessageAgent.CMD_PUBLISH + message);
		this.client.send(UltraMessageAgent.getMessageSeparator());
	}
}
