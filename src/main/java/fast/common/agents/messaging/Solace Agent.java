package fast.common.agents.messaging;

import com.solacesystems.jcsmp.*;
import fast.common.agents.Agent;
import fast.common.context.MessagingStepResult;
import fast.common.core.Configurator;
import fast.common.logging.FastLogger;

import java.util.ArrayList;
import java.util.Map;

/**
 * The {@code SolaceAgent} class defines automation of connecting Solace server and do some actions.
 *
 *
 * <p>The basic actions includes: start/shutdown, send messages, receive messages ...</p>
 *
 * <p>Details information for using a SolaceAgent can see: </p>
 *  <a href="https://cedt-confluence.nam.nsroot.net/confluence/display/167813001/SolaceAgent+Automation+Example"> <p>Examples</p></a>
 *
 * @author QA Framework Team
 * @since 1.8
 */
public class SolaceAgent extends Agent implements IMessagingAgent{

    private static final String CONFIG_HOST = "Host";
    private static final String CONFIG_VPN_NAME = "VPN_Name";
    private static final String CONFIG_CLIENT_USER_NAME = "Client_User_Name";
    private static final String CONFIG_CLIENT_PASS_WORD = "Client_Password";
    private static final String CONFIG_SOLACE_MESSAGING_TYPE = "Solace_Messaging_Type";
    private static final String CONFIG_PUB_TOPIC_OR_QUEUE_NAME = "Pub_Topic_Or_Queue_Name";
    private static final String CONFIG_SUB_TOPIC_OR_QUEUE_NAME = "Sub_Topic_Or_Queue_Name";

    private String host;
    private String vpnName;
    private String userName;
    private String passWord;
    private String messagingType = "Topic";
    private String pubTopicOrQueueName;
    private String subTopicOrQueueName;

    private FastLogger logger;
    private JCSMPSession publishSession = null;
    private JCSMPSession subscribeSession = null;
    private Queue pubQueue;
    private Queue subQueue;
    private Topic pubTopic;
    private Topic subTopic;
    private XMLMessageProducer publisher = null;
    private XMLMessageConsumer topicSubscriber = null;
    private FlowReceiver queueSubscriber = null;
    private boolean isStarted = false;
    final ArrayList<Object> receivedMessages = new ArrayList<>();

    /**
     * Constructs a new <tt>SolaceAgent</tt> with
     * default configuration file (config.yml) and custom configuration files to
     * fetch required parameters.
     *
     * @param   name a string for naming the creating SolaceAgent
     * @param   agentParams a map to get the required parameters for creating a SolaceAgent
     * @param   configurator a Configurator instance to provide configuration info for the actions of the SolaceAgent
     *
     * @since 1.8
     */
    public SolaceAgent(String name, Map agentParams, Configurator configurator) {

        super(name, agentParams, configurator);
        logger = FastLogger.getLogger(String.format("%s:SolaceAgent", _name));

        host = Configurator.getStringOr(agentParams, CONFIG_HOST, null);
        vpnName = Configurator.getStringOr(agentParams, CONFIG_VPN_NAME, null);
        userName = Configurator.getStringOr(agentParams, CONFIG_CLIENT_USER_NAME, null);
        passWord = Configurator.getStringOr(agentParams, CONFIG_CLIENT_PASS_WORD, null);
        pubTopicOrQueueName = Configurator.getStringOr(agentParams, CONFIG_PUB_TOPIC_OR_QUEUE_NAME, null);
        subTopicOrQueueName = Configurator.getStringOr(agentParams, CONFIG_SUB_TOPIC_OR_QUEUE_NAME, null);

        if (agentParams.containsKey(CONFIG_SOLACE_MESSAGING_TYPE)) {
            messagingType = agentParams.get(CONFIG_SOLACE_MESSAGING_TYPE).toString().toLowerCase();
        }

    }

    /**
     * Create a subscribe session or publish session
     * @throws InvalidPropertiesException
     * @since 1.8
     */
    private JCSMPSession createSession() throws InvalidPropertiesException {
        final JCSMPProperties properties = new JCSMPProperties();
        properties.setProperty(JCSMPProperties.HOST, host);     // host:port
        properties.setProperty(JCSMPProperties.USERNAME, userName); // client-username
        properties.setProperty(JCSMPProperties.PASSWORD, passWord); // client-password
        properties.setProperty(JCSMPProperties.VPN_NAME, vpnName); // message-vpn
        properties.setProperty(JCSMPProperties.SSL_VALIDATE_CERTIFICATE, false);
        return JCSMPFactory.onlyInstance().createSession(properties);
    }

    /**
     * Create a new or get a exist subscribe session
     * @throws JCSMPException
     * @since 1.8
     */
    private synchronized JCSMPSession getSubscribeSession() throws JCSMPException {
        if(subscribeSession == null) {
            subscribeSession = createSession();
            provisioning(subscribeSession,subTopicOrQueueName, false);
        }
        return subscribeSession;
    }

    /**
     * Create a new or get a exist publish session
     * @throws JCSMPException
     * @since 1.8
     */
    private synchronized JCSMPSession getPublishSession() throws JCSMPException {
        if(publishSession == null) {
            publishSession = createSession();
            provisioning(publishSession, pubTopicOrQueueName,true);
        }
        return publishSession;
    }

    /**
     * provisioning a topic or queue for transport
     * @param session when provisioning queue need session
     * @param topicOrQueueName give the topic or queue name
     * @throws JCSMPException
     * @since 1.8
     */
    private void provisioning(JCSMPSession session, String topicOrQueueName, boolean isPublish) throws JCSMPException {
        switch (messagingType.toUpperCase()){
            case "TOPIC":
                provisioningTopic(topicOrQueueName, isPublish);
                break;
            case "QUEUE":
                provisioningQueue(session, topicOrQueueName, isPublish);
                break;
            default:
                throw new IllegalArgumentException("Messaging Type Configuration Error:"+ messagingType);
        }
    }

    /**
     * provisioning a topic
     * @param topicName give the topic name
     * @since 1.8
     */
    private void provisioningTopic(String topicName, Boolean isPublish) throws JCSMPException {
        logger.info("Attempting to provision the topic '"+ topicName +"' on the appliance.");
        if(isPublish){
            pubTopic = JCSMPFactory.onlyInstance().createTopic(topicName);
        } else {
            subTopic = JCSMPFactory.onlyInstance().createTopic(topicName);
            subscribeSession.addSubscription(subTopic);
        }
    }

    /**
     * provisioning a queue
     * @param session give the session name
     * @param queueName give the queue name
     * @since 1.8
     */
    private void provisioningQueue(JCSMPSession session, String queueName, Boolean isPublish) throws JCSMPException {
        logger.info("Attempting to provision the queue '"+ queueName +"' on the appliance.");
        final EndpointProperties endpointProps = new EndpointProperties();
        // set queue permissions to "consume" and access-type to "exclusive"
        endpointProps.setPermission(EndpointProperties.PERMISSION_CONSUME);
        endpointProps.setAccessType(EndpointProperties.ACCESSTYPE_EXCLUSIVE);

        if(isPublish){
            // create the queue object locally
            pubQueue = JCSMPFactory.onlyInstance().createQueue(queueName);
            // Actually provision it, and do not fail if it already exists
            session.provision(pubQueue, endpointProps, JCSMPSession.FLAG_IGNORE_ALREADY_EXISTS);
        }else{
            // create the queue object locally
            subQueue = JCSMPFactory.onlyInstance().createQueue(queueName);
            // Actually provision it, and do not fail if it already exists
            session.provision(subQueue, endpointProps, JCSMPSession.FLAG_IGNORE_ALREADY_EXISTS);
        }

    }

    /**
     * create publisher for solace agent
     * @since 1.8
     */
    private XMLMessageProducer createPublisher() throws JCSMPException {
        /** Anonymous inner-class for handling publishing events */
        return publishSession.getMessageProducer(new JCSMPStreamingPublishEventHandler() {
            @Override
            public void responseReceived(String messageID) {
                logger.info("Producer received response for msg: " + messageID);
            }
            @Override
            public void handleError(String messageID, JCSMPException e, long timestamp) {
                logger.error("Producer received error for msg: "+ messageID + " - " + e);
            }
        });
    }

    /**
     * get or create a new publisher for solace agent
     * @since 1.8
     */
    private XMLMessageProducer getPublisher() throws Exception {
        if(!isStarted){
            start();
        }
        if(publisher == null){
            publisher = createPublisher();
        }
        return publisher;
    }

    /**
     * the received messages are stored in a map receivedMessages
     * @since 1.8
     */
    private void onReceiveHandler(BytesXMLMessage msg){
        if (msg instanceof TextMessage) {
            receivedMessages.add(((TextMessage) msg).getText());
            logger.info("TextMessage received: " + ((TextMessage)msg).getText());
        } else {
            receivedMessages.add(msg);
            logger.info("Message received.");
        }
    }

    /**
     * get or create a new topic subscriber
     * @since 1.8
     */
    private XMLMessageConsumer getTopicSubscriber() throws Exception {
        if(topicSubscriber == null){
            /** Anonymous inner-class for MessageListener
             *  This demonstrates the async threaded message callback */
            topicSubscriber = subscribeSession.getMessageConsumer(new XMLMessageListener() {
                @Override
                public void onReceive(BytesXMLMessage msg) {
                    onReceiveHandler(msg);
                }

                @Override
                public void onException(JCSMPException e) {
                    logger.error("Consumer received exception: " + e);
                }
            });
        }
        return topicSubscriber;
    }

    /**
     * get or create a new queue subscriber
     * @since 1.8
     */
    private FlowReceiver getQueueSubscriber() throws Exception {
        if(queueSubscriber == null){
        // Create a Flow be able to bind to and consume messages from the Queue.
            final ConsumerFlowProperties flowProp = new ConsumerFlowProperties();
            flowProp.setEndpoint(subQueue);
            flowProp.setAckMode(JCSMPProperties.SUPPORTED_MESSAGE_ACK_CLIENT);

            EndpointProperties endpointProps = new EndpointProperties();
            endpointProps.setAccessType(EndpointProperties.ACCESSTYPE_EXCLUSIVE);

            queueSubscriber = subscribeSession.createFlow(new XMLMessageListener() {
                @Override
                public void onReceive(BytesXMLMessage msg) {
                    onReceiveHandler(msg);

                    // When the ack mode is set to SUPPORTED_MESSAGE_ACK_CLIENT,
                    // guaranteed delivery messages are acknowledged after
                    // processing
                    msg.ackMessage();
                }

                @Override
                public void onException(JCSMPException e) {
                    logger.error("Consumer received exception: " + e);
                }
            }, flowProp, endpointProps);
        }
        return queueSubscriber;
    }

    /**
     * send message
     * @param message the object message to send
     * @since 1.8
     */
    @Override
    public void send(Object message) {
        TextMessage msg = JCSMPFactory.onlyInstance().createMessage(TextMessage.class);
        if(message instanceof String){
            msg.setText((String)message);
        }else{
            logger.error("message type not supported.");
        }
        try{
            switch (messagingType.toUpperCase()){
                case "TOPIC":
                    getPublisher().send(msg,pubTopic);
                    break;
                case "QUEUE":
                    getPublisher().send(msg,pubQueue);
                    break;
                default:
                    throw new IllegalArgumentException("Messaging Type Configuration Error:"+ messagingType);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * receive messages
     * @since 1.8
     */
    @Override
    public MessagingStepResult receive() {
        ArrayList<Object> result;

        synchronized (receivedMessages) {
            result = new ArrayList<>(receivedMessages);
            receivedMessages.clear();
        }
        logger.info("Received " +result.size() + " messages !");
        return new MessagingStepResult(result);
    }

    /**
     * @return agent is whether started
     */
    @Override
    public boolean isStarted() {
        return isStarted;
    }

    /**
     * Starts a solace agent: connect session and start subscriber
     * @throws Exception
     */
    @Override
    public void start() throws Exception {
        if(isStarted){
            logger.info("Skip - Already started.");
            return;
        }
        if(subTopicOrQueueName == null && pubTopicOrQueueName == null){
            throw new IllegalArgumentException("Configuration Error: Missing configuration for both Pub_Topic_Or_Queue_Name and Sub_Topic_Or_Queue_Name");
        }
        if(pubTopicOrQueueName != null){
            getPublishSession().connect();
        }
        if(subTopicOrQueueName != null){
            getSubscribeSession().connect();
        }
        switch (messagingType.toUpperCase()){
            case "TOPIC":
                getTopicSubscriber().start();
                break;
            case "QUEUE":
                getQueueSubscriber().start();
                break;
            default:
                throw new IllegalArgumentException("Messaging Type Configuration Error:"+ messagingType);
        }
        isStarted = true;
    }

    /**
     * Close a solace agent
     */
    @Override
    public void close() throws Exception {
    	try{
            if(publisher != null){
            	publisher.close();
            	publisher = null;
            }      
            if(topicSubscriber != null){
            	topicSubscriber.close();
            	topicSubscriber = null;
            }               
            if(queueSubscriber != null){
            	queueSubscriber.close();
            	queueSubscriber = null;
            }  
            if(publishSession != null){
            	publishSession.closeSession();
            	publishSession = null;
            }              
            if(subscribeSession != null){
            	subscribeSession.closeSession();
            	subscribeSession = null;
            }               
    	}catch(Exception ex){
    		logger.info(ex.getMessage());
    		throw ex;
    	}finally{
    		synchronized (receivedMessages) {
	            receivedMessages.clear();
	        }
    		isStarted = false;
    	}   
    }
}
