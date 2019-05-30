package fast.common.agents.messaging;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.modules.junit4.PowerMockRunner;

import com.aventstack.extentreports.gherkin.model.When;

import fast.common.context.MessagingStepResult;
import fast.common.core.Configurator;
import fast.common.logging.FastLogger;

import static org.mockito.ArgumentMatchers.any;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


@RunWith(PowerMockRunner.class)
@PowerMockIgnore({ "javax.management.*", "javax.crypto.*" })
public class TestUltraMessageTopic {
	
	@Mock
	private TcpClientAgent client;
	
	@Mock
	private UltraMessageAgent agent;
	
	@InjectMocks
	private UltraMessageTopic ultraMessageTopic;
	
	public TestUltraMessageTopic() throws Exception{
		Map<String,Object> agentParams=new HashMap<>();
		agentParams.put(UltraMessageAgent.CONFIG_SERVER, "server");
		agentParams.put(UltraMessageAgent.CONFIG_PORT, 0);
		agentParams.put(UltraMessageAgent.CONFIG_SENDTOPIC, "topic");
		agentParams.put(UltraMessageAgent.CONFIG_RECEIVETOPICS, "topic");
		agentParams.put(UltraMessageAgent.CONFIG_CONFIG, null);
		UltraMessageAgent agent=Mockito.mock(UltraMessageAgent.class);
		when(agent.getName()).thenReturn("Agent");
		when(agent.getAgentParams()).thenReturn(agentParams);
		when(agent.getConfigurator()).thenReturn( Configurator.getInstance());
		when(agent.getLogger()).thenReturn( FastLogger.getLogger(TestUltraMessageTopic.class.getName()));
		
		ultraMessageTopic=new UltraMessageTopic("localhost", 8080,"topic",agent);
	}
	
	
	@Test
	public void listenStart_passed() throws MessagingException{
		
		doNothing().when(client).send(any(String.class));
		ultraMessageTopic.listenStart();
	}
	
	@Test
	public void getReceivedMessage_passed() throws MessagingException{
		ArrayList<Object> messages=new ArrayList<>();
		messages.add("message");
		
		when(client.receive()).thenReturn(new MessagingStepResult(messages));
		
		MessagingStepResult result=ultraMessageTopic.getReceivedMessages();
		
		assertEquals(result.getMessages().size(), 1);
	}
	
	@Test
	public void publish_passed() throws MessagingException{
		doNothing().when(client).send(any(Object.class));
		when(client.isConnected()).thenReturn(true);
		ultraMessageTopic.publish("message");
	}
	
	@Test
	public void publish_conditionnotsatisfied() throws Exception{			
		doNothing().when(client).send(any(Object.class));
		when(client.isConnected()).thenReturn(true);
		ultraMessageTopic.publish("");
		
		doNothing().when(client).send(any(Object.class));
		when(client.isConnected()).thenReturn(false);
		ultraMessageTopic.publish("message");
		
		Map<String,Object> agentParams=new HashMap<>();
		agentParams.put(UltraMessageAgent.CONFIG_SERVER, "server");
		agentParams.put(UltraMessageAgent.CONFIG_PORT, 0);
		agentParams.put(UltraMessageAgent.CONFIG_SENDTOPIC, "topic");
		agentParams.put(UltraMessageAgent.CONFIG_RECEIVETOPICS, "topic");
		agentParams.put(UltraMessageAgent.CONFIG_CONFIG, null);
		UltraMessageAgent agent=Mockito.mock(UltraMessageAgent.class);
		when(agent.getName()).thenReturn("Agent");
		when(agent.getAgentParams()).thenReturn(agentParams);
		when(agent.getConfigurator()).thenReturn( Configurator.getInstance());
		when(agent.getLogger()).thenReturn( FastLogger.getLogger(TestUltraMessageTopic.class.getName()));
		
		UltraMessageTopic topic=new UltraMessageTopic("localhost", 8080,"topic",agent);
		topic.publish("message");
	}
	
}
