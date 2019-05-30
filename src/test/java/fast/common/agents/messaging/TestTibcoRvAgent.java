package fast.common.agents.messaging;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;

import com.aventstack.extentreports.gherkin.model.Scenario;
import com.tibco.tibrv.Tibrv;
import com.tibco.tibrv.TibrvException;
import com.tibco.tibrv.TibrvListener;
import com.tibco.tibrv.TibrvMsg;
import com.tibco.tibrv.TibrvQueue;
import com.tibco.tibrv.TibrvRvdTransport;

import fast.common.context.MessagingStepResult;
import fast.common.context.ScenarioContext;
import fast.common.context.TibcoRvStepResult;
import fast.common.core.Configurator;
import fast.common.core.ValidationFailed;
import io.restassured.RestAssured;

@RunWith(MockitoJUnitRunner.class)
//@PowerMockIgnore("javax.net.ssl.*")
//@PrepareForTest({Tibrv.class, TibrvRvdTransport.class, TibrvListener.class})
public class TestTibcoRvAgent {
	private Map agentParams;
	
	@Mock
	private TibrvRvdTransport tibrvTransport;
	
	@InjectMocks
	private TibcoRvAgent agent = new TibcoRvAgent();

	@Before
	public void setUp() {
		agentParams = new HashMap<String, Object>();
		agentParams.put("service", "service");
		agentParams.put("network", "network");
		agentParams.put("daemon", "daemon");
		agentParams.put("subject", "subject");	
	}	
	
//	@Test
//	public void constructWithStart_success() throws MessagingException, Exception{
//		PowerMockito.mockStatic(Tibrv.class);
//		PowerMockito.doNothing().when(Tibrv.class);	
//		Tibrv.open(Tibrv.IMPL_NATIVE);
//		TibrvRvdTransport transport = mock(TibrvRvdTransport.class);
//		PowerMockito.whenNew(TibrvRvdTransport.class).withArguments("service", "network", "daemon").thenReturn(transport);
//		TibrvListener listener = mock(TibrvListener.class);
//		PowerMockito.whenNew(TibrvListener.class).withArguments(any(TibrvQueue.class), any(TibcoRvAgent.class), any(TibrvRvdTransport.class), anyString(), any(Object.class)).thenReturn(listener);
//				
//		agent = new TibcoRvAgent("testAgent", agentParams, Configurator.getInstance());
//	}

	@Test
	public void constructWithStart_fail(){
		try {
			TibcoRvAgent myAgent = new TibcoRvAgent("testAgent", agentParams, Configurator.getInstance());
			fail("Expect exception");
		} catch (Exception ex) {
			assertEquals(MessagingException.class, ex.getClass());
		}
	}
	
	@Test
	public void isStart_false(){
		assertFalse(agent.isStarted());
	}
	
	@Test
	public void close_success() throws Exception{		
		doNothing().when(tibrvTransport).destroy();
		agent.close();
		try{
			agent.close();
			fail("expect exception");
		}catch(Exception ex){
			assertEquals(NullPointerException.class, ex.getClass());
		}
	}
	
	@Test
	public void sendTibrvMsg_success() throws Exception{		
		TibrvMsg msg = new TibrvMsg();
		msg.add("SYMBOL", "C");
		msg.add("PRICE", 50);
		msg.add("SIDE", "BUY");
		doNothing().when(tibrvTransport).send(any(TibrvMsg.class));
		agent.send(msg);		
	}
	
	@Test
	public void sendStringMsg_success() throws Exception{
		doNothing().when(tibrvTransport).send(any(TibrvMsg.class));
		agent.send("SYM=C|PRICE=50|SIDE=BUY");		
	}
	
	@Test
	public void sendMapMsg_success() throws Exception{
		HashMap<String, Object> fields = new HashMap<String, Object>();
		fields.put("SYMBOL", "C");
		fields.put("PRICE", 50);
		fields.put("SIDE", "BUY");
		doNothing().when(tibrvTransport).send(any(TibrvMsg.class));
		agent.send(fields);		
	}
	
	@Test
	public void send_ExceptionThrown(){		
		try{	
			agent.send(null);
			fail("expect exception is thrown");
		}catch(Exception ex){
			assertEquals(NullPointerException.class, ex.getClass());
		}	
	}
	
	@Test
	public void receive() throws TibrvException{	
		TibrvMsg msg = new TibrvMsg();
		msg.add("SYMBOL", "C");
		msg.add("PRICE", 50);
		msg.add("SIDE", "BUY");
		
		agent.onMsg(null, msg);
		MessagingStepResult result = agent.receive();	
		assertEquals("C", ((TibrvMsg)result.getMessages().get(0)).get("SYMBOL"));
	}
	
	@Test
	public void receiveWithSecenarioContext_success() throws Throwable{	
		TibrvMsg msg = new TibrvMsg();
		msg.add("SYMBOL", "C");
		msg.add("PRICE", 50);
		msg.add("SIDE", "BUY");		
		agent.onMsg(null, msg);
		
		Scenario scenario = mock(Scenario.class);
		ScenarioContext context = new ScenarioContext(scenario);

		TibcoRvStepResult result = agent.receive(context, "", "");	
		assertEquals("C", result.getFieldValue("SYMBOL"));
	}
	
	@Test
	public void convertTibrvMsgToString_normalMessage() throws TibrvException {
		TibrvMsg msg = new TibrvMsg();
		msg.add("SYMBOL", "C");
		msg.add("PRICE", 50);
		msg.add("SIDE", "BUY");
		assertEquals("SYMBOL=C|PRICE=50|SIDE=BUY", TibcoRvAgent.convertTibrvMsgToString(msg));
	}
	
	@Test
	public void convertTibrvMsgToString_emptyMessage() throws TibrvException {
		TibrvMsg msg = new TibrvMsg();
		assertEquals("", TibcoRvAgent.convertTibrvMsgToString(msg));
	}
	
	@Test
	public void convertTibrvMsgToString_exceptionCaught() throws TibrvException {
		TibrvMsg msg = new TibrvMsg();
		msg.add("SYMBOL", "C");
		msg.add("PRICE", 50);
		msg.add("SIDE", "BUY");
		TibrvMsg msgSpy = spy(msg);
		when(msgSpy.getNumFields()).thenReturn(4);
		assertTrue(TibcoRvAgent.convertTibrvMsgToString(msgSpy).endsWith("Field index out of range"));
	}
}
