package fast.common.agents.messaging;

import fast.common.context.MessagingStepResult;
import fast.common.core.Configurator;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.powermock.reflect.Whitebox;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;
import static org.mockito.Mockito.spy;
import static org.powermock.api.mockito.PowerMockito.doNothing;

public class TestLbmAgent {

    private LbmAgent lbmAgent;
    private Configurator c = null;
    private Map<String, String> agentParams =new HashMap<>();

    @Before
    public void setUp() throws Exception {

        agentParams.put("Send_Topic", "Test_Topic");
        agentParams.put("Receive_Topic", "Test_Topic");
        c =Configurator.getInstance();
        c.getSettingsMap().put("LbmAgent", agentParams);
        lbmAgent = spy(new LbmAgent("LbmAgent",agentParams,c));

        doNothing().when(lbmAgent).init();

    }

    @After
    public void tearDown() throws Exception {
        lbmAgent.close();
    }

    @Test
    public void constructor_withConfigValue() {

        String topicName = Whitebox.getInternalState(lbmAgent, "sendTopicName");
        assertEquals(topicName, "Test_Topic");

    }

    @Test(expected = IllegalArgumentException.class)
    public void sendNotString() throws MessagingException {

        String[] send_msg = {"1","2"};
        lbmAgent.send(send_msg);

    }

    @Test()
    public void receive() {

        MessagingStepResult stepResult = lbmAgent.receive();
        assertEquals(0,stepResult.getMessages().size());

    }

    @Test
    public void isStarted() throws Exception {
        lbmAgent.start();
        assertEquals(true, lbmAgent.isStarted());
    }
}
