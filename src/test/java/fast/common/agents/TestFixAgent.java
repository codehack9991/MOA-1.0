package fast.common.agents;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import fast.common.agents.messaging.MessagingException;
import fast.common.agents.messaging.TibcoEmsAgent;
import fast.common.context.FixStepResult;
import fast.common.context.ScenarioContext;
import fast.common.core.Configurator;
import quickfix.FieldNotFound;
import quickfix.InvalidMessage;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;
import static org.testng.Assert.assertTrue;

import org.powermock.api.mockito.PowerMockito;
//import org.powermock.core.classloader.annotations.PrepareForTest;
//import org.powermock.modules.junit4.PowerMockRunner;
//
//import com.citi.cet.automation.framework.reporter.json.StepResult;









import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.HashMap;
import java.util.Map;

import javax.xml.xpath.XPathExpressionException;


@RunWith(PowerMockRunner.class)
@PowerMockIgnore("javax.management.*")
@PrepareForTest({ AgentsManager.class})
public class TestFixAgent {
	FixAgent _fixAgent;
	
	@Before
	public void setUp() throws Exception{
		/*TibcoEmsAgent mockMessagingAgent = mock(TibcoEmsAgent.class);
		ArrayList<String> messages = new ArrayList<String>();
		messages.add("11=123|35=8");
		try {
			doNothing().when(mockMessagingAgent).send(any(String.class));
			when(mockMessagingAgent.receive()).thenReturn(messages);
		} catch (MessagingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		HashMap<String, String> agentParams = new HashMap<String, String>();
		agentParams.put("send_extra_tags", "");
		agentParams.put("receive_extra_tags", "");
		agentParams.put("messagingAgent", "mockMissingAgent");
		String dataDictionary = System.getProperty("user.dir") + "/config/quickfix_spec/FIX44.xml";
		agentParams.put("data_dictionary", dataDictionary);
		
		Configurator mockConfigurator = mock(Configurator.class);	
		when(mockConfigurator.getFilenameOr(agentParams, "data_dictionary", null)).thenReturn(dataDictionary);
		
		PowerMockito.mockStatic(AgentsManager.class);
		when(AgentsManager.getAgent(anyString())).thenReturn(mockMessagingAgent);
				
		_fixAgent = new FixAgent("testFixAgent", agentParams, mockConfigurator);*/
	}

	/*@Test -- To Do: it's difficult to create unit test for sendMessage() due to deep coupling among FixAgent, FixHelper, ScenarioContext and Configurator
	public void testSendMessage() throws XPathExpressionException, MessagingException, InvalidMessage {
		ScenarioContext mockScenarioContext = mock(ScenarioContext.class);
		
		_fixAgent.sendMessage(mockScenarioContext, null, "@sendVar", "35=D|38=1000|40=MARKET|59=DAY|55=ABC|54=BUY");
	}*/
	
	@Test
	public void testReceiveMessage() throws XPathExpressionException, MessagingException, InterruptedException, InvalidMessage, FieldNotFound {
		/*ScenarioContext mockScenarioContext = mock(ScenarioContext.class);
		_fixAgent.receiveMessage(mockScenarioContext, null, null, "11=123");*/
	}
	@Test
	public void sendMultiMessagesInTimeWindow_pass() throws Throwable{
	
		Map<String, String> agentParams =new HashMap();
		agentParams.put("class_name", "fast.common.agents.FixAgent");
		agentParams.put("data_dictionary", "quickfix_spec/FIX44.xml");
		agentParams.put("send_extra_tags", "BeginString=FIX.4.2|52=%generateTsWithMicroseconds()%");
		agentParams.put("messaging_agent", "TibcoEmsAgent_Ares");
		Configurator c =Configurator.getInstance();
		c.getSettingsMap().put("FixAgent", agentParams);
		AgentsManager agentsManager = mock(AgentsManager.class);
	    PowerMockito.mockStatic(AgentsManager.class);
		Map<String, Object> tibcoEmsAgentParams =new HashMap();
		tibcoEmsAgentParams.put("class_name", "fast.common.agents.messaging.TibcoEmsAgent");	    
		tibcoEmsAgentParams.put("TibcoEmsAgent_Ares", tibcoEmsAgentParams);
		
		TibcoEmsAgent tibcoEmsAgent =  mock(TibcoEmsAgent.class);
		doNothing().when(tibcoEmsAgent).send(any(Object.class));
		when(AgentsManager.getInstance()).thenReturn(agentsManager);
		when(agentsManager.getAgent("TibcoEmsAgent_Ares")).thenReturn(tibcoEmsAgent);
		FixAgent fixAgent = new FixAgent("FixAgent",agentParams,c);
		ScenarioContext scenarioContext= new ScenarioContext("fixSendMultipleMessages");
		String userstr = "11=123|38=1000|49=SENDER|56=RECEIVER|54=2|";
		FixStepResult sendResults =	fixAgent.sendMultiMessagesInTimeWindow(scenarioContext, 100, 1, "ClientNew", userstr);
		assertTrue(sendResults.getFieldsValues("49").size()==100);
		assertTrue(sendResults.getFieldsValues("49").get(0).equals("SENDER"));
	}
}
