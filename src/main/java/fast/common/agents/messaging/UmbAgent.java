package fast.common.agents.messaging;

import java.util.ArrayList;
import java.util.Map;

import com.citi.rio.registry.infra.Environment;
import com.citi.rio.registry.infra.Region;
import com.citi.rio.umb.UmbClientFactory;
import com.citi.rio.umb.exception.UmbException;
import com.citi.rio.umb.payload.Payload;
import com.citi.rio.umb.payload.Payload.Key;
import com.citi.rio.umb.publish.UmbPublisher;
import com.citi.rio.umb.subscribe.UmbMessageHandler;
import com.citi.rio.umb.subscribe.UmbMessageHandlerFactory;
import com.citi.rio.umb.subscribe.UmbPayloadHandler;
import com.citi.rio.umb.subscribe.UmbSubscriber;
import com.citi.rio.umb.subscribe.exception.MessageHandlerCreationException;
import com.citi.rio.umb.subscribe.payload.BatchPayload;
import com.citi.rio.umb.subscribe.payload.IndividualPayload;

import fast.common.agents.Agent;
import fast.common.context.MessagingStepResult;
import fast.common.core.Configurator;
import fast.common.logging.FastLogger;

/**
 * The {@code UmbAgent} class defines automation of connecting UMB server and do some actions.
 * 
 * 
 * <p>The basic actions includes: start/shutdown subscriber or publisher, send messages, receive messages ...</p>
 * 
 * <p>Details information for using a UmbAgent can see: </p>
 *  <a href="https://cedt-confluence.nam.nsroot.net/confluence/display/167813001/UmbAgent+Automation+Config"> <p>Examples</p></a>
 * 
 * @author QA Framework Team
 * @since 1.7
 */
public class UmbAgent extends Agent implements IMessagingAgent{
	
    private static final String PROPERTY_IS_DURABLE = "rio.umb.transport.jms.is_durable";
    
    private static final String CONFIG_IS_DURABLE = "Is_Durable";
    private static final String CONFIG_ENV = "Env";
    private static final String CONFIG_REGION = "Region";
    private static final String CONFIG_SUBSCRIBER_TYPE = "Subscriber_Type";
    private static final String CONFIG_PUB_CLIENT_KEY = "Pub_Client_Key";
    private static final String CONFIG_SUB_CLIENT_KEY = "Sub_Client_Key";
    
    private String isDurable = "false";
    private Environment env = Environment.DEV;
    private Region region = Region.NAM;
    private String subscriberType = "Individual";
    private String pubClientKey;
    private String subClientKey;
    
    private FastLogger logger;
    private UmbSubscriber subscriber;
    private UmbPublisher publisher;
    private boolean isStarted = false;
    final ArrayList<Object> receivedMessages = new ArrayList<>();
    
    /**
     * Constructs a new <tt>UmbAgent</tt> with
     * default configuration file (config.yml) and custom configuration files to
     * fetch required parameters.
     *
     * @param   name a string for naming the creating UmbAgent 
     * @param   agentParams a map to get the required parameters for creating a UmbAgent 
     * @param   configurator a Configurator instance to provide configuration info for the actions of the UmbAgent
     * 
     * @since 1.7
     */
	public UmbAgent(String name, Map agentParams, Configurator configurator) {
		
		super(name, agentParams, configurator);
		logger = FastLogger.getLogger(String.format("%s:UmbAgent", _name));
		
		pubClientKey = Configurator.getStringOr(agentParams, CONFIG_PUB_CLIENT_KEY, null);
		subClientKey = Configurator.getStringOr(agentParams, CONFIG_SUB_CLIENT_KEY, null);
		
		if (agentParams.containsKey(CONFIG_IS_DURABLE)) {
			isDurable = agentParams.get(CONFIG_IS_DURABLE).toString().toLowerCase();
		}
		if (agentParams.containsKey(CONFIG_ENV)) {
			env = Environment.valueOf(agentParams.get(CONFIG_ENV).toString().toUpperCase());
		}
		if (agentParams.containsKey(CONFIG_REGION)) {
			region = Region.valueOf(agentParams.get(CONFIG_REGION).toString().toUpperCase());
		}
		if (agentParams.containsKey(CONFIG_SUBSCRIBER_TYPE)) {
			subscriberType = agentParams.get(CONFIG_SUBSCRIBER_TYPE).toString();
		}
		
	}
    /**
	 * Initialize a subscriber for umb agent
	 * @param subscriberType one subscriber type [Individual|Batch|Default] to initialize
	 * @throws IllegalArgumentException
	 * @since 1.7
     */
    private void initSubscriber(String subscriberType){
    	
    	logger.info("Initialize subscriber with type: " +subscriberType);
    	
    	switch (subscriberType.toUpperCase()) {
		case "INDIVIDUAL":
			subscriber = getIndividualSubscriber();
			break;
		case "BATCH":
			subscriber = getBatchSubscriber();
			break;
		case "DEFAULT":
			subscriber = getDefaultSubscriber();
			break;
		default:
			throw new IllegalArgumentException("Invalid SubscriberTpye! Please choose [Individual|Batch|Default]");
		}
    	
    }
	/**
	 * @return a individual subscriber
	 */
    private UmbSubscriber getIndividualSubscriber(){
    	
    	UmbClientFactory ucf = new UmbClientFactory(env, region);
    	return	ucf.createSubscriber(subClientKey, new UmbMessageHandlerFactory<IndividualPayload<String>>() {
                    @Override
                    public UmbMessageHandler<IndividualPayload<String>> create(final Map<Key, Object> metaData)
                        throws MessageHandlerCreationException {
                        return new UmbPayloadHandler<String, IndividualPayload<String>>() {
                            @Override
                            public void onPayload(final IndividualPayload<String> payload) {
                            	
                            	receivedMessages.add(payload.getContent());
                            	logger.info("Received message: " + payload.getContent());

                                payload.commit();
                            }
                            @Override
                            public void onApplicationError(final IndividualPayload<String> payload,
                                final RuntimeException error) {

                            	logger.error("Appliction error: " + error.getMessage());
                                subscriber.shutdown();
                            }
                            @Override
                            public void onConnectionError(final Exception error)
                            {
                                // Handle Connection Error.
                            	logger.error("Connection error: " + error.getMessage());
                                subscriber.shutdown();
                            }
                        };
                    }
                });
    	
    }
	/**
	 * @return a batch subscriber
	 */
    private UmbSubscriber getBatchSubscriber(){
    	
    	UmbClientFactory ucf = new UmbClientFactory(env, region);
    	return	ucf.createSubscriber(subClientKey, new UmbMessageHandlerFactory<BatchPayload<String>>() {
                    @Override
                    public UmbMessageHandler<BatchPayload<String>> create(final Map<Key, Object> metaData)
                        throws MessageHandlerCreationException {
                        return new UmbPayloadHandler<String, BatchPayload<String>>() {
                            @Override
                            public void onPayload(final BatchPayload<String> payload) {

                            	receivedMessages.add(payload.getContent());
                            	logger.info("Received message: " + payload.getContent());

                                payload.commitAll();
                            }
                            @Override
                            public void onApplicationError(final BatchPayload<String> payload,
                                final RuntimeException error) {

                            	logger.error("Appliction error: " + error.getMessage());
                                subscriber.shutdown();
                            }
                            @Override
                            public void onConnectionError(final Exception error)
                            {
                                // Handle Connection Error.
                            	logger.error("Connection error: " + error.getMessage());
                                subscriber.shutdown();
                            }
                        };
                    }
                });
    	
    }
	/**
	 * @return a default subscriber
	 */
    private UmbSubscriber getDefaultSubscriber(){
    	
    	UmbClientFactory ucf = new UmbClientFactory(env, region);
    	return	ucf.createSubscriber(subClientKey, new UmbMessageHandlerFactory<Payload<String>>() {
                    @Override
                    public UmbMessageHandler<Payload<String>> create(final Map<Key, Object> metaData)
                        throws MessageHandlerCreationException {
                        return new UmbPayloadHandler<String, Payload<String>>() {
                            @Override
                            public void onPayload(final Payload<String> payload) {
                            	receivedMessages.add(payload.getContent());
                            	logger.info("Received message: " + payload.getContent());

                            }
                            @Override
                            public void onApplicationError(final Payload<String> payload,
                                final RuntimeException error) {

                            	logger.error("Appliction error: " + error.getMessage());
                                subscriber.shutdown();
                            }
                            @Override
                            public void onConnectionError(final Exception error)
                            {
                                // Handle Connection Error.
                            	logger.error("Connection error: " + error.getMessage());
                                subscriber.shutdown();
                            }
                        };
                    }
                });
    	
    }
	/**
	 * Start subscriber
	 */
    public synchronized void startSubscriber(){
    	
    	if(subClientKey == null){
    		throw new UmbException("Agent is not configured for \"Sub_Client_Key\". Please check your configuration.");
    	}
    	
    	if(subscriber == null){

			System.setProperty(PROPERTY_IS_DURABLE, isDurable); // Must set this property as false at localhost              
			initSubscriber(subscriberType);
    		
    	}
        
    }
	/**
	 * Shutdown subscriber
	 */
    public synchronized void shutdownSubscriber(){
    	
		if (subscriber != null) {
			
			subscriber.shutdown();
			subscriber = null;			
			
			synchronized (receivedMessages) {
				receivedMessages.clear();
			}
			
		}
		
    }
	/**
	 * Start publisher
	 */
    public synchronized void startPublisher(){
    	
    	if(pubClientKey == null){
    		throw new UmbException("Agent is not configured for \"Pub_Client_Key\". Please check your configuration.");
    	}
    	
    	if(publisher == null){    		
    		
    		UmbClientFactory umbClientFactory = new UmbClientFactory(env, region);
            publisher = umbClientFactory.createPublisher(pubClientKey);    		
            
    	}
        
    }
	/**
	 * Shutdown publisher
	 */
    public synchronized void shutdownPublisher(){
    	
    	if(publisher != null){
    		
			publisher.shutdown();
			publisher = null;
    		
    	}
    	  
    }
	/**
	 * @return agent is whether started
	 */
	@Override
	public boolean isStarted() {
		
		return isStarted;
		
	}
	/**
	 * Starts a messaging agent
	 * @throws UmbException
	 */
	@Override
	public void start() throws Exception {
		
		if(isStarted){
			logger.info("Skip - Already started.");
			return;
		}
		if(subClientKey == null && pubClientKey ==null){
			throw new UmbException("Agent is not configured neither for SEND nor for RECEIVE-based actions - which must be a configuration error because such agent is useless. Please check your configuration.");
		}
		if(subClientKey != null){
			startSubscriber();
		}
		if(pubClientKey != null){
			startPublisher();
		}
		logger.info("UmbAgent starts successfully.");
		isStarted = true;
		
	}
    /**
	 * Send messages
	 * @param message the object message to send
	 * @throws UmbException
	 * @since 1.7
     */
	@Override
	public void send(Object message) throws MessagingException {
		
        try{
            publisher.publish(message);
            logger.info("Published message: "+ message);
        }
        catch (UmbException e) {
        	logger.error("Appliction error: " + e);
            throw e;
        }
		
	}
    /**
	 * Receive messages
	 * @return MessagingStepResult received messages
	 * @since 1.7
     */
	@Override
	public MessagingStepResult receive() throws MessagingException {
		
		ArrayList<Object> result;
		
		synchronized (receivedMessages) {
			result = new ArrayList<>(receivedMessages);
			receivedMessages.clear();
		}
		logger.info("Received " +result.size() + " messages !");
		return new MessagingStepResult(result);
		
	}
	/**
	 * Close a umb agent
	 */
	@Override
	public void close() throws Exception {
		
		shutdownPublisher();
		shutdownSubscriber();
		logger.info("UmbAgent closed successfully.");
		isStarted = false;

	}

}
