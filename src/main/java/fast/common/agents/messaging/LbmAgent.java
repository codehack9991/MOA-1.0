package fast.common.agents.messaging;

import java.util.ArrayList;
import java.util.Map;

import com.latencybusters.lbm.LBM;
import com.latencybusters.lbm.LBMContext;
import com.latencybusters.lbm.LBMException;
import com.latencybusters.lbm.LBMSource;
import com.latencybusters.lbm.LBMSourceAttributes;
import com.latencybusters.lbm.LBMTopic;
import com.latencybusters.lbm.LBMMessage;
import com.latencybusters.lbm.LBMReceiverAttributes;
import com.latencybusters.lbm.LBMReceiverCallback;
import com.latencybusters.lbm.LBMReceiver;
import fast.common.agents.Agent;
import fast.common.context.MessagingStepResult;
import fast.common.core.Configurator;
import fast.common.logging.FastLogger;

/**
 * The {@code LbmAgent} class defines automation of connecting 29West server and do some actions.
 *
 *
 * <p>The basic actions includes: start/close, send messages, receive messages ...</p>
 *
 * <p>Details information for using a LbmAgent can see: </p>
 *  <a href="https://cedt-confluence.nam.nsroot.net/confluence/display/167813001/LbmAgent+Automation+Config"> <p>Examples</p></a>
 *
 * @author QA Framework Team
 * @since 1.9
 */
public class LbmAgent extends Agent implements IMessagingAgent{

    private static final String SEND_TOPIC = "Send_Topic";
    private static final String RECEIVE_TOPIC = "Receive_Topic";
    private static final String LICENSE_PATH = "License_Path";
    private static final String CONFIG_PATH = "Config_Path";

    private LBMContext myContext = null;
    private LBMSource mySource = null;
    private LBMReceiver myReceiver = null;
    private String sendTopicName;
    private String receiveTopicName;
    private String licensePath = "config/lbm/lbm_license.txt";
    private String configPath = "config/lbm/lbm.cfg";
    private boolean isStarted = false;
    private final ArrayList<Object> receivedMessages = new ArrayList<>();

    private FastLogger logger;

    /**
     * Constructs a new <tt>LbmAgent</tt> with
     * default configuration file (config.yml) and custom configuration files to
     * fetch required parameters.
     *
     * @param   name a string for naming the creating LbmAgent
     * @param   agentParams a map to get the required parameters for creating a LbmAgent
     * @param   configurator a Configurator instance to provide configuration info for the actions of the LbmAgent
     *
     * @since 1.9
     */
    public LbmAgent(String name, Map agentParams, Configurator configurator) {

        super(name, agentParams, configurator);

        logger = FastLogger.getLogger(String.format("%s:LbmAgent", _name));

        sendTopicName = Configurator.getStringOr(agentParams, SEND_TOPIC, null);
        receiveTopicName = Configurator.getStringOr(agentParams, RECEIVE_TOPIC, null);

        if (agentParams.containsKey(LICENSE_PATH)) {
            licensePath = agentParams.get(LICENSE_PATH).toString();
        }
        if (agentParams.containsKey(CONFIG_PATH)) {
            configPath = agentParams.get(CONFIG_PATH).toString();
        }
    }

    /**
     * Initialize the Lbmagent, including set license, set configuration, init sender and receiver
     * @throws LBMException LBMException
     * @since 1.9
     */
    public void init() throws LBMException {

        LBM.setLicenseFile(licensePath);
        LBM.setConfiguration(configPath);

        createAndSetLBMContext();
        setSourceTopic();
        setReceiverTopicAndReceiverCallback();

    }

    /**
     * create a context object.  A context is an environment in which LBM functions.
     * @since 1.9
     */
    private void createAndSetLBMContext(){
        try{

            myContext = new LBMContext();
            logger.info("LBM Context created");
        }
        catch(LBMException lbmExp){
            logger.info("Error creating LBMContext ctx" + lbmExp.getMessage());
        }
    }

    /**
     * set topic for sender
     * @since 1.9
     */
    private void setSourceTopic() throws LBMException{

        LBMTopic myTopic = new LBMTopic(myContext, this.sendTopicName, new LBMSourceAttributes());
        logger.info("Send Topic name is ...." + this.receiveTopicName);

        /*
         * Create the source object and bind it to a topic.  Sources must be
         * associated with a context.
         */
        mySource = new LBMSource(myContext, myTopic);

        /*
         * Need to wait for receivers to find us before first send.  There are
         * other ways to accomplish this, but sleep is easy.  See https://communities.informatica.com/infakb/faq/5/Pages/80061.aspx
         * for details.
         */
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            logger.error(e.getMessage());
            Thread.currentThread().interrupt();
        }
        logger.info("Sender init complete.");
    }

    /**
     * set topic for receiver and add callback method
     * @since 1.9
     */
    private void setReceiverTopicAndReceiverCallback() throws LBMException{

        LBMTopic myTopic = new LBMTopic(myContext, this.receiveTopicName, new LBMReceiverAttributes());

        logger.info("Receive Topic name is ...." + this.receiveTopicName);

        /*
         * Add a callback function to our new receiver.  This function is
         * called each time our receiver gets a message.
         * */
        LbmAgent.ReceiverCallback receiverCallback = new LbmAgent.ReceiverCallback();

        /*
         * Create the receiver object and bind it to a topic.  Receivers must be
         * associated with a context.
         */
        myReceiver = new LBMReceiver(myContext, myTopic, receiverCallback, null);
        logger.info("Receiver init complete.");
    }

    /**
     * Use sender to Send String messages
     * @param message the string message to send
     * @throws MessagingException MessagingException
     * @since 1.9
     */
    private void sendMessage(String message) throws MessagingException {
        /*
         * Send a message to the "Greetings" topic.  The flags make sure the
         * call to lbm_src_send doesn't return until the message is sent.
         */
        logger.info("Publishing message to topic " + this.sendTopicName + ".....");
        try {
            mySource.send(message.getBytes(), message.length(), LBM.MSG_FLUSH | LBM.SRC_BLOCK);
            logger.info("Message published .....");
        } catch (LBMException e) {
            logger.error(e.getMessage());
            throw new MessagingException(e);
        }
    }

    /**
     * Send messages
     * @param message the object message to send
     * @throws MessagingException MessagingException
     * @since 1.9
     */
    @Override
    public void send(Object message) throws MessagingException {

        if(message instanceof String){
            sendMessage((String)message);
        }else{
            logger.error("message type not supported.");
            throw new IllegalArgumentException("message type not supported.");
        }

    }

    /**
     * Receive messages
     * @return MessagingStepResult received messages
     * @since 1.9
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
     * Start a Lbmagent and init
     * @throws Exception Excpetion
     */
    @Override
    public void start() throws Exception {
        if(isStarted){
            logger.info("Skip - Already started.");
            return;
        }
        try{
            init();
            logger.info("LbmAgent starts successfully.");
            isStarted = true;
        }catch (Exception e){
            logger.error(e.getMessage());
            throw e;
        }
    }

    /**
     * Close a Lbmagent
     */
    @Override
    public void close() throws Exception {
        try{
            if (mySource != null) {
                mySource.close();
                mySource = null;
            }
            if(myReceiver != null){
                myReceiver.close();
                myReceiver = null;
            }
            if(myContext != null){
                myContext.close();
                myContext = null;
            }
        }catch(Exception ex){
            logger.error(ex.getMessage());
            throw ex;
        }finally{
            synchronized (receivedMessages) {
                receivedMessages.clear();
            }
            isStarted = false;
        }
    }

    /*
     * LBM passes received messages to the application by means of a callback.
     * I.e. the LBM context thread reads the network socket, performs its
     * higher-level protocol functions, and then calls an application-level
     * function that was set up during initialization.  This callback function
     * has some limitations placed upon it.  It must execute very quickly
     * any potentially blocking calls it might make will interfere with the
     *  proper execution of the LBM context thread.
     *
     * LBM receiver callbacks in Java are merely classes that implement the LBMReceiverCallback interface
     */
    private class ReceiverCallback implements LBMReceiverCallback {

        public int onReceive(Object cbArgs, LBMMessage theMessage) {
            /*
             * There are several different events that can cause the receiver
             * callback to be called. Decode the event that caused this.
             */

            if (theMessage == null) {
                logger.info("Message is null....");
                return -1;
            }

            switch (theMessage.type()) {

                case LBM.MSG_REQUEST:
                case LBM.MSG_DATA:
                    try {
                        receivedMessages.add(new String(theMessage.data()));
                        logger.info("Received message: " + theMessage.dataLength() + " bytes on topic "+ theMessage.topicName()+". The message is " + new String(theMessage.data()) + "'");

                    } catch (Exception e) {
                        logger.info("Exception .");
                        System.exit(1);
                    }
                    break;

                case LBM.MSG_BOS:
                    logger.info("[" + theMessage.topicName() + "][" + theMessage.source() + "], Beginning of Transport Session");
                    break;

                case LBM.MSG_EOS:
                    logger.info("[" + theMessage.topicName() + "][" + theMessage.source() + "], End of Transport Session");
                    break;

                default:
                    break;
            }

            theMessage.dispose();
            /*
             * Return 0 if there were no errors. Returning a non-zero value will
             * cause LBM to log a generic error message.
             */
            return 0;
        }

    }

}
