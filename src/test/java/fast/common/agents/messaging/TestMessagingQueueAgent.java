package fast.common.agents.messaging;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import quickfix.ConfigError;
import fast.common.core.Configurator;

@RunWith(PowerMockRunner.class)
public class TestMessagingQueueAgent {
	
	private MessagingQueueAgent messagingQueueAgent =null;
	private Configurator c = null;
	private Map<String, String> agentParams =new HashMap();

	@Before
	public void setUp() throws Exception  {
		agentParams.put("host", "localhost");
		agentParams.put("user", "remote-username");
		agentParams.put("password", "remote-password");
		agentParams.put("send_queue_name", "sendQueueName");
		agentParams.put("receive_queue_name", "receiveQueueName");
		agentParams.put("send_queue_type", "ACTIVEMQ");
		agentParams.put("receive_queue_type", "TIBCOEMS");
        c =Configurator.getInstance();
		c.getSettingsMap().put("MessagingQueueAgent", agentParams);
        messagingQueueAgent = new MessagingQueueAgent("MessagingQueueAgent",agentParams,c);
		
	}
	@Test
	public void constructor_setParametersContainsSendbody() throws ConfigError {
		Map<String, String> sendMap =new HashMap();
		sendMap.put("host", "123");
		sendMap.put("user", "456");
		sendMap.put("password", "789");
		sendMap.put("queuename", "1");
		sendMap.put("queuetype", "ACTIVEMQ");
		Map obj= (Map) c.getSettingsMap().get("MessagingQueueAgent");
		obj.put("send", sendMap);
        messagingQueueAgent = new MessagingQueueAgent("MessagingQueueAgent",agentParams,c);
        String sendQueueName = Whitebox.getInternalState(messagingQueueAgent, "sendQueueName");
        assertEquals(sendQueueName, "1");

	}
	@Test
	public void constructor_setParametersContainsReceivebody() throws ConfigError {
		Map<String, String> receiveMap =new HashMap();
		receiveMap.put("host", "ABC");
		receiveMap.put("user", "DEF");
		receiveMap.put("password", "GHI");
		receiveMap.put("queuename", "A");
		receiveMap.put("queuetype", "TIBCOEMS");
		Map obj= (Map) c.getSettingsMap().get("MessagingQueueAgent");
		obj.put("receive", receiveMap);
        messagingQueueAgent = new MessagingQueueAgent("MessagingQueueAgent",agentParams,c);
		String receiveQueueName= Whitebox.getInternalState(messagingQueueAgent, "receiveQueueName");
		assertEquals(receiveQueueName,"A");

	}
	

	
	@Test
	public void isStarted_false(){
		assertFalse(messagingQueueAgent.isStarted());
	}
	
}
