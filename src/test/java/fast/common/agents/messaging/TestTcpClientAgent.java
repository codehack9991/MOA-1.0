package fast.common.agents.messaging;

import static org.junit.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

import javax.enterprise.inject.New;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.internal.matchers.Any;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import fast.common.context.MessagingStepResult;
import fast.common.core.Configurator;
import fast.common.logging.FastLogger;

import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doNothing;


@RunWith(PowerMockRunner.class)
@PowerMockIgnore({ "javax.management.*", "javax.crypto.*" })
public class TestTcpClientAgent {

	@Mock
	private InputStream in;
	@Mock
	private OutputStream out;
	
	@Mock
	private Socket client;
	
	@InjectMocks
	private TcpClientAgent tcpClientAgent;
	
	public TestTcpClientAgent() throws Exception{
		tcpClientAgent=new TcpClientAgent("localhost",8080,"TcpClientAgent", null,Configurator.getInstance(),FastLogger.getLogger(TestTcpClientAgent.class.getName()));
	}
	
	@Test
	public void  constructor_fieldsSet() throws Exception{
		
		Map<String,Object> agentParams=new HashMap<>();
		agentParams.put(UltraMessageAgent.CONFIG_SERVER, "server");
		agentParams.put(UltraMessageAgent.CONFIG_PORT, 0);
		FastLogger logger=FastLogger.getLogger(TestTcpClientAgent.class.getName());
		
		TcpClientAgent agent=new TcpClientAgent("TcpClientAgent",agentParams, Configurator.getInstance());
		
		assertEquals("server", Whitebox.getInternalState(agent, "server"));
		assertEquals("0", Whitebox.getInternalState(agent, "port").toString());
		assertNotNull(Whitebox.getInternalState(agent, "logger"));
		assertNotNull(Whitebox.getInternalState(agent, "receivedMessages"));
		
		agent=new TcpClientAgent("server",0,"TcpClientAgent", agentParams, Configurator.getInstance(),logger);
		
		assertEquals("server", Whitebox.getInternalState(agent, "server"));
		assertEquals("0", Whitebox.getInternalState(agent, "port").toString());
		assertNotNull(Whitebox.getInternalState(agent, "logger"));
		assertNotNull(Whitebox.getInternalState(agent, "receivedMessages"));
	}
	
	@Test
	public void isConnected_passed_true(){
		when(client.isConnected()).thenReturn(true);
		assertTrue(tcpClientAgent.isConnected());
	}
	
	@Test
	public void isConnected_passed_false() throws Exception{
		TcpClientAgent agent=new TcpClientAgent("server",0,"TcpClientAgent", null, Configurator.getInstance(),FastLogger.getLogger(TestTcpClientAgent.class.getName()));
		assertFalse(agent.isConnected());
	}
	
	@Test
	public void isStarted_passed_true(){
		when(client.isConnected()).thenReturn(true);
		assertTrue(tcpClientAgent.isStarted());
	}
	
	@Test
	public void isStarted_passed_false() throws Exception{
		TcpClientAgent agent=new TcpClientAgent("server",0,"TcpClientAgent", null, Configurator.getInstance(),FastLogger.getLogger(TestTcpClientAgent.class.getName()));
		assertFalse(agent.isStarted());
	}
	
	@Test
	public void start_passed_alreadystarted() throws MessagingException{
		when(client.isConnected()).thenReturn(true);
		tcpClientAgent.start();
	}
	
	@Test
	public void start_failed_servernull(){
		boolean  exceptionThrown=false;
		try(TcpClientAgent agent=new TcpClientAgent(null,0,"TcpClientAgent", null, Configurator.getInstance(),FastLogger.getLogger(TestTcpClientAgent.class.getName()));) {			
			agent.start();
		} catch (Exception e) {
			exceptionThrown=true;
			assertEquals(MessagingException.class.getName(), e.getClass().getName());
		}
				assertTrue(exceptionThrown);		
	}
	
	@Test
	public void start_failed_connectfail() throws Exception{
		try{
		when(client.isConnected()).thenReturn(false);
		PowerMockito.whenNew(Socket.class).withArguments("localhost", 8080).thenReturn(client);//not work
		tcpClientAgent.start();
		}catch(Exception ex)
		{
			assertEquals(MessagingException.class.getName(), ex.getClass().getName());
		}
	}
		
	@Test
	public void close_passed(){
		try{
			tcpClientAgent.close();
		}catch(Exception ex){
			assertNull(ex);
		}
	}
	
	@Test
	public void send_passed() throws MessagingException{
		when(client.isConnected()).thenReturn(true);
		tcpClientAgent.send("any");
	}
	
	@Test
	public void send_failed(){
		boolean  exceptionthrown=false;
		try{
			when(client.isConnected()).thenReturn(false);
			tcpClientAgent.send("any");
		}catch(Exception ex){
			exceptionthrown=true;
			assertEquals(MessagingException.class.getName(), ex.getClass().getName());
		}
		assertTrue(exceptionthrown);
	}
	
	@Test
	public void receive_passed() throws MessagingException{
		when(client.isConnected()).thenReturn(true);
		MessagingStepResult result=tcpClientAgent.receive();
		assertEquals(0,result.getMessages().size());
	}
	
	@Test
	public void receive_failed()  {
		boolean  exceptionthrown=false;
		try{
			when(client.isConnected()).thenReturn(false);
			tcpClientAgent.receive();
		}catch(Exception ex){
			exceptionthrown=true;
			assertEquals(MessagingException.class.getName(), ex.getClass().getName());
		}
		assertTrue(exceptionthrown);
	}
	
	
}


