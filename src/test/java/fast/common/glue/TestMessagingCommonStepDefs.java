package fast.common.glue;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import quickfix.Session;
import quickfix.SessionID;
import fast.common.agents.AgentsManager;
import fast.common.agents.FixTcpClientAgent;
import fast.common.agents.messaging.TibcoEmsAgent;
import fast.common.context.FixStepResult;
import fast.common.context.ScenarioContext;
import fast.common.core.Configurator;

@RunWith(PowerMockRunner.class)
@PowerMockIgnore("javax.management.*")
@PrepareForTest({ AgentsManager.class, quickfix.Session.class,BaseCommonStepDefs.class})
public class TestMessagingCommonStepDefs {
	private MessagingCommonStepDefs message;
	@Mock
	private AgentsManager agentsManager;
	@Mock
	private TibcoEmsAgent tibcoEmsAgent;

	@Before
	public void before() {
		message = new MessagingCommonStepDefs();
		PowerMockito.mockStatic(AgentsManager.class);
		when(AgentsManager.getInstance()).thenReturn(agentsManager);
	}

	@Test
	public void testSendBulkOfMessage() throws Exception {
		ArrayList<String> msgList = new ArrayList<>();
		msgList.add("message1");
		msgList.add("message2");
		msgList.add("message3");
		when(agentsManager.getOrCreateAgent("agentName")).thenReturn(tibcoEmsAgent);
		doNothing().when(tibcoEmsAgent).run("send", "message1");
		doNothing().when(tibcoEmsAgent).run("send", "message2");
		doNothing().when(tibcoEmsAgent).run("send", "message3");
		message.sendBulkOfMessage("agentName", 10, 1, msgList);
	}
	
	@Test
	public void sendMultiMessagesInTimeWindow_pass() throws Throwable{
		
		Map<Object,Object> agentParams=new HashMap<>();
		agentParams.put("send_extra_tags", "52=%generateTsWithMilliseconds()%|60=%generateTsWithNanoseconds()%|");
		agentParams.put("receive_extra_tags", "52=$Tag52Format|60=$Tag60Format");
		agentParams.put("ConnectionType", "initiator");
		agentParams.put("SenderCompID", "CLIENT");
		agentParams.put("TargetCompID", "VENUE");
		agentParams.put("SocketConnectHost", "localhost");
		agentParams.put("SocketConnectPort", "1234");
		agentParams.put("BeginString", "FIX.4.4");
		agentParams.put("data_dictionary", "quickfix_spec/FIX44.xml");
		PowerMockito.mockStatic(quickfix.Session.class);
		PowerMockito.when(Session.sendToTarget(any(quickfix.Message.class), any(SessionID.class))).thenReturn(true);
		FixTcpClientAgent fixTcpClientAgent = Mockito.spy(new FixTcpClientAgent("FixTcpClientAgent",agentParams,Configurator.getInstance()));
		doNothing().when(fixTcpClientAgent).start();
		when(agentsManager.getOrCreateAgent("FixTcpClientAgent")).thenReturn(fixTcpClientAgent);
		ScenarioContext scenarioContext = new ScenarioContext("FixAgent");
		PowerMockito.mockStatic(BaseCommonStepDefs.class);
		when(BaseCommonStepDefs.getScenarioContext()).thenReturn(scenarioContext);
		
		String agentName = "FixTcpClientAgent";
		int messageCount= 100;
		int timeWindow = 1;
		String varName = "Client_RequestNew";
		String msgTemplate = "RequestNew";
		String userstr = "11=123|38=1000|49=CLIENT|56=VENUE|54=2|";	
		message.sendMultiMessagesInTimeWindow(agentName, messageCount, timeWindow, varName, msgTemplate, userstr);
		FixStepResult result  = (FixStepResult)scenarioContext.getLastResultVariable();
		assertTrue(result.getFieldsValues("49").size()==100);
		assertTrue(result.getFieldsValues("49").get(0).equals("CLIENT"));
	}
}
