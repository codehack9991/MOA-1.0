package fast.common.agents.messaging;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.ArgumentMatchers.anyObject;
import static org.mockito.Mockito.spy;

import java.util.HashMap;
import java.util.Map;

import fast.common.core.Configurator;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import fast.common.context.MessagingStepResult;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.reflect.Whitebox;

public class TestUmbAgent {

	private UmbAgent umbAgent =null;
	private Configurator c = null;
	private Map<String, String> agentParams =new HashMap<String, String>();

	@Before
	public void setUp() throws Exception  {

		agentParams.put("Pub_Client_Key", "147273-AMS.PUB.TEST-1");
		agentParams.put("Sub_Client_Key", "147273-AMS.SUB.TEST-2");

		c =Configurator.getInstance();
		c.getSettingsMap().put("UmbAgent", agentParams);

	}

	@After
	public void close() throws Exception {
		if(umbAgent!=null){
			umbAgent.close();
		}
	}

	@Test
	public void constructor_withDefaultValue() {

		umbAgent = new UmbAgent("UmbAgent",agentParams,c);
		String subscriberType = Whitebox.getInternalState(umbAgent, "subscriberType");
		assertEquals(subscriberType, "Individual");

	}

	@Test
	public void constructor_withConfigValue() {

		agentParams.put("Is_Durable", "false");
		agentParams.put("Env", "DEV");
		agentParams.put("Region", "NAM");
		agentParams.put("Subscriber_Type", "Batch");

		c.getSettingsMap().put("UmbAgent", agentParams);
		umbAgent = new UmbAgent("UmbAgent",agentParams,c);

		String subscriberType = Whitebox.getInternalState(umbAgent, "subscriberType");
		assertEquals(subscriberType, "Batch");

	}
	
	@Test
	public void testSendAndReceive_Individual() throws Exception {
		agentParams.put("Subscriber_Type", "Individual");

		c.getSettingsMap().put("UmbAgent", agentParams);
		umbAgent = spy(new UmbAgent("UmbAgent",agentParams,c));

		PowerMockito.doNothing().when(umbAgent).startSubscriber();
		PowerMockito.doNothing().when(umbAgent).startPublisher();
		PowerMockito.doNothing().when(umbAgent).send(anyObject());

		umbAgent.start();
		umbAgent.send("Test Message - Individual");
		Thread.sleep(3000);
		MessagingStepResult result = umbAgent.receive();
		assertEquals(0,result.getMessages().size());
	}
	
	@Test
	public void testSendAndReceive_Batch() throws Exception {
		agentParams.put("Subscriber_Type", "Batch");

		c.getSettingsMap().put("UmbAgent", agentParams);
		umbAgent = spy(new UmbAgent("UmbAgent",agentParams,c));

		PowerMockito.doNothing().when(umbAgent).startSubscriber();
		PowerMockito.doNothing().when(umbAgent).startPublisher();
		PowerMockito.doNothing().when(umbAgent).send(anyObject());

		umbAgent.start();
		umbAgent.send("Test Message - Batch");
		Thread.sleep(3000);
		MessagingStepResult result = umbAgent.receive();
		assertEquals(0,result.getMessages().size());
	}

	@Test
	public void testSendAndReceive_Default() throws Exception {
		agentParams.put("Subscriber_Type", "Default");

		c.getSettingsMap().put("UmbAgent", agentParams);
		umbAgent = spy(new UmbAgent("UmbAgent",agentParams,c));

		PowerMockito.doNothing().when(umbAgent).startSubscriber();
		PowerMockito.doNothing().when(umbAgent).startPublisher();
		PowerMockito.doNothing().when(umbAgent).send(anyObject());

		umbAgent.start();
		umbAgent.send("Test Message - Default");
		Thread.sleep(3000);
		MessagingStepResult result = umbAgent.receive();
		assertEquals(0,result.getMessages().size());
	}

	@Test
	public void isStarted_false(){

		c.getSettingsMap().put("UmbAgent", agentParams);
		umbAgent = new UmbAgent("UmbAgent",agentParams,c);
		assertFalse(umbAgent.isStarted());

	}
}
