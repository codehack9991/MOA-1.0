package fast.common.agents.messaging;

import com.solacesystems.jcsmp.*;
import fast.common.context.MessagingStepResult;
import fast.common.core.Configurator;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.powermock.api.mockito.PowerMockito.*;

@RunWith(PowerMockRunner.class)
@PrepareForTest({JCSMPFactory.class})
@PowerMockIgnore({ "javax.management.*", "javax.crypto.*" })
public class TestSolaceAgent {

    @Mock
    JCSMPSession jcsmpSession;

    @Mock
    XMLMessageProducer publisher;

    @Mock
    XMLMessageConsumer topicSubscriber;

    @Mock
    FlowReceiver flowReceiver;

    @Mock
    TextMessage textMessage;

    private Configurator c = null;
    private Map<String, String> agentParams =new HashMap<String, String>();
    private SolaceAgent solaceAgent = null;

    @Before
    public void setUp() throws Exception {

        JCSMPFactory jcsmpFactory = mock(JCSMPFactory.class);
        PowerMockito.mockStatic(JCSMPFactory.class);
        when(JCSMPFactory.onlyInstance()).thenReturn(jcsmpFactory);
        when(jcsmpFactory.createSession(any(JCSMPProperties.class))).thenReturn(jcsmpSession);
        when(jcsmpFactory.createMessage(any())).thenReturn(textMessage);
        doNothing().when(textMessage).setText(anyString());
        when(jcsmpSession.getMessageProducer(any(JCSMPStreamingPublishEventHandler.class))).thenReturn(publisher);
        when(jcsmpSession.createFlow(any(XMLMessageListener.class),any(ConsumerFlowProperties.class),any(EndpointProperties.class))).thenReturn(flowReceiver);
        when(jcsmpSession.getMessageConsumer(any(XMLMessageListener.class))).thenReturn(topicSubscriber);
        doNothing().when(jcsmpSession).provision(any(Endpoint.class),any(EndpointProperties.class),anyLong());
        doNothing().when(publisher).send(any(XMLMessage.class),any(Destination.class));
        doNothing().when(topicSubscriber).start();
        doNothing().when(flowReceiver).start();

        agentParams.put("Host", "tcps://dev-us-sol-rio1.nam.nsroot.net:55443");
        agentParams.put("VPN_Name", "RIO_DEV_NAM_NJ_TRD_EQ_1");
        agentParams.put("Client_User_Name", "dev_gma_sub");
        agentParams.put("Client_Password", "test");
        agentParams.put("Pub_Topic_Or_Queue_Name", "US/BOTRD/GMADC/001");
        agentParams.put("Sub_Topic_Or_Queue_Name", "US/BOTRD/GMADC/001");

        c =Configurator.getInstance();
        c.getSettingsMap().put("SolaceAgent", agentParams);

    }

    @After
    public void tearDown() throws Exception {
        if(solaceAgent != null){
            solaceAgent.close();
        }
    }

    @Test
    public void sendAndReceive_Topic() throws Exception {
        solaceAgent = new SolaceAgent("SolaceAgent", agentParams, c);
        solaceAgent.start();
        solaceAgent.send("Test Message - Topic");
        MessagingStepResult result = solaceAgent.receive();
    }

    @Test
    public void sendAndReceive_Queue() throws Exception{
        agentParams.put("Solace_Messaging_Type", "Queue");
        c.getSettingsMap().put("SolaceAgent", agentParams);
        solaceAgent = new SolaceAgent("SolaceAgent", agentParams, c);
        solaceAgent.start();
        solaceAgent.send("Test Message - Queue");
        MessagingStepResult result = solaceAgent.receive();
    }

    @Test
    public void start_istarted() throws Exception{

        solaceAgent = new SolaceAgent("SolaceAgent", agentParams, c);
        solaceAgent.start();
        solaceAgent.start();
    }

    @Test
    public void start_noTopic() throws Exception{
        agentParams.remove("Pub_Topic_Or_Queue_Name");
        agentParams.remove("Sub_Topic_Or_Queue_Name");
        c.getSettingsMap().put("SolaceAgent", agentParams);
        solaceAgent = new SolaceAgent("SolaceAgent", agentParams, c);
        try {
            solaceAgent.start();
        } catch (IllegalArgumentException e){
            assertEquals("Configuration Error: Missing configuration for both Pub_Topic_Or_Queue_Name and Sub_Topic_Or_Queue_Name", e.getMessage());
        }

    }

    @Test
    public void start_noMsgType() throws Exception{
        agentParams.put("Solace_Messaging_Type", "NO");
        c.getSettingsMap().put("SolaceAgent", agentParams);
        solaceAgent = new SolaceAgent("SolaceAgent", agentParams, c);
        try {
            solaceAgent.start();
        } catch (IllegalArgumentException e){
            assertEquals("Messaging Type Configuration Error:no", e.getMessage());
        }

    }

    @Test
    public void isStarted() {
        solaceAgent = new SolaceAgent("SolaceAgent", agentParams, c);
        assertEquals(false, solaceAgent.isStarted());
    }
}
