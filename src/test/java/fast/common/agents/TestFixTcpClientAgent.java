package fast.common.agents;

import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import quickfix.ConfigError;
import quickfix.Session;
import quickfix.SessionID;
import quickfix.SessionSettings;
import fast.common.context.FixStepResult;
import fast.common.context.ScenarioContext;
import fast.common.core.Configurator;
import fast.common.fix.FixHelper;
import static org.mockito.Mockito.*;
import static org.testng.Assert.assertTrue;


@RunWith(PowerMockRunner.class)
@PrepareForTest({quickfix.Session.class,quickfix.SocketInitiator.class}) 
@PowerMockIgnore({ "javax.management.*", "javax.crypto.*" })
public class TestFixTcpClientAgent {
	
	
	FixTcpClientAgent  fixTcpClientAgent ;
	Map<Object,Object> agentParams;
	
	@Before
	public void setup() throws Exception{
		agentParams=new HashMap<>();
		agentParams.put("send_extra_tags", "52=%generateTsWithMilliseconds()%|60=%generateTsWithNanoseconds()%|");
		agentParams.put("receive_extra_tags", "52=$Tag52Format|60=$Tag60Format");
		agentParams.put("SenderCompID", "CLIENT");
		agentParams.put("TargetCompID", "VENUE");
		agentParams.put("SocketConnectHost", "localhost");
		agentParams.put("SocketConnectPort", "1234");
		agentParams.put("BeginString", "FIX.4.4");
		agentParams.put("data_dictionary", "quickfix_spec/FIX44.xml");
		agentParams.put("ConnectionType", "initiator");

	}
	@Test
	public void consructor_fieldsSetProperply() throws Exception{
		Configurator c =Configurator.getInstance();
		c.getSettingsMap().put("FixTcpClientAgent", agentParams);
		fixTcpClientAgent = new FixTcpClientAgent("FixTcpClientAgent",agentParams,c);
	}
	@Test
	public void start_withInitialError() throws Throwable{
		agentParams.put("HeartBtInt", "10");
		agentParams.put("StartTime", "00:00:00");
		agentParams.put("EndTime", "00:00:00");
		Configurator c =Configurator.getInstance();
		c.getSettingsMap().put("FixTcpClientAgent", agentParams);
		fixTcpClientAgent = Mockito.spy(new FixTcpClientAgent("FixTcpClientAgent",agentParams,c));
		try{
		    fixTcpClientAgent.start();
		}catch(quickfix.ConfigError e){
			assertTrue(e.getMessage().contains("error during session initialization"));
		}
	}
	
	@Test
	public void close_InitiatorNull() throws Throwable{
		fixTcpClientAgent = Mockito.spy(new FixTcpClientAgent("FixTcpClientAgent",agentParams,Configurator.getInstance()));
		doNothing().when(fixTcpClientAgent).start();
		fixTcpClientAgent.close();
	}
	@Test
	public void _sendMessage_pass() throws Throwable{
		fixTcpClientAgent = Mockito.spy(new FixTcpClientAgent("FixTcpClientAgent",agentParams,Configurator.getInstance()));
		doNothing().when(fixTcpClientAgent).start();
		PowerMockito.mockStatic(quickfix.Session.class);
		PowerMockito.when(Session.sendToTarget(any(quickfix.Message.class), any(SessionID.class))).thenReturn(true);
		
		FixHelper fixHelper = new FixHelper(agentParams,Configurator.getInstance());
		String rawMsg = "8=FIX.4.29=249235=NewSingle49=COMET50=CASH_COES_INSTANCE10143=ORT52=20180608-16:41:06.2348054=557=CASH_COES_INSTANCE10962=LIST10964=18130363atu38=1000.037=18923607411=2/20181210-110310965=7011019=N11346=0.011347=2.010372=A100232=50310890=0.09871=181303624e410=000";
		quickfix.Message msg = fixHelper.createFixMessage(rawMsg);		
		fixTcpClientAgent._sendMessage(msg);
	}
	

	@Test
	public void isStarted_exceptedValueFalse() throws ConfigError, InterruptedException, Exception{
	    fixTcpClientAgent = Mockito.spy(new FixTcpClientAgent("FixTcpClientAgent",agentParams,Configurator.getInstance()));
		assertTrue(!fixTcpClientAgent.isStarted());
	}
	@Test
	public void sendMultiMessagesInTimeWindow_pass() throws Throwable{
	    fixTcpClientAgent = Mockito.spy(new FixTcpClientAgent("FixTcpClientAgent",agentParams,Configurator.getInstance()));
		String userstr = "11=123|38=1000|49=SENDER|56=RECEIVER|54=2|";
		doNothing().when(fixTcpClientAgent).start();
		PowerMockito.mockStatic(quickfix.Session.class);
		PowerMockito.when(Session.sendToTarget(any(quickfix.Message.class), any(SessionID.class))).thenReturn(true);
		ScenarioContext scenarioContext = new ScenarioContext("FixAgent");
		FixStepResult sendResults = fixTcpClientAgent.sendMultiMessagesInTimeWindow(scenarioContext, 10, 1, "ClientNEW", userstr);
		assertTrue(sendResults.getFieldsValues("49").size()==10);
		assertTrue(sendResults.getFieldsValues("49").get(0).equals("SENDER"));
		
	}
}
