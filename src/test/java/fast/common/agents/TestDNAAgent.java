package fast.common.agents;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Matchers.anyString;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import com.citi.dna.client.Connection;
import com.citi.dna.client.Service;
import com.citi.dna.comm.Message;
import com.citi.dna.comm.Transport;
import com.citi.dna.comm.TransportFactory;
import com.citi.dna.discovery.DiscoveryClient;
import com.citi.dna.util.c.Dict;

import fast.common.context.DNAStepResult;
import fast.common.core.Configurator;

@RunWith(PowerMockRunner.class)
@PowerMockIgnore("javax.management.*")
@PrepareForTest(TransportFactory.class)
public class TestDNAAgent {
	
	private AgentDNA dnaAgent;
	private String name;
	private Configurator configurator;
	private Map agentParams;
	
	
	@Mock
	private TransportFactory transportFactory;
	
	@Mock
	private Transport transport;
	
	
	@Before
	public void setup() throws Exception{
		name = "nonMeaningName";
		configurator = Configurator.getInstance();
		agentParams = new HashMap<>();
		agentParams.put("Transport.ID", "transportID");
		agentParams.put("Transport.CLASSNAME", "transportClassname");
		agentParams.put("host", "host");
		agentParams.put("user", "user");
		agentParams.put("password", "password");
		agentParams.put("connectionFactory", "connectionFactory");
		agentParams.put("ENV", "environment");
		agentParams.put(AgentDNA.PARAM_REUSE_SERVICE, "true");
		
		Properties props = new Properties();
        props.put(Transport.ID, "Transport.ID"); //AgentDNA
        props.put(Transport.CLASSNAME, "Transport.CLASSNAME");
        props.put("provider", "host");
        props.put("connectionFactory", "connectionFactory");
        props.put("username", "user");
        props.put("password", "password");
        props.put("ENV", "environment");
		PowerMockito.mockStatic(TransportFactory.class);
		when(TransportFactory.getInstance()).thenReturn(transportFactory);
		when(transportFactory.getTransport(props)).thenReturn(transport);
		dnaAgent = new AgentDNA(name, agentParams, configurator);
	}
	
	@Test
	public void constructDNAAgent() throws Exception {
		assertEquals("nonMeaningName", Whitebox.getInternalState(dnaAgent,"_name"));
		assertEquals(true, Whitebox.getInternalState(dnaAgent,"reuseService"));
		assertNotNull(Whitebox.getInternalState(dnaAgent,"servicesInUse"));
	}
	
	@Test
	public void getService_pass() throws Exception {
		Map<String, Service> mockServices = new HashMap<>();
		Service mockService = mock(Service.class);		
		when(mockService.isConnected()).thenReturn(true);	
		mockServices.put("MockService", mockService);
		Whitebox.setInternalState(dnaAgent, "servicesInUse", mockServices);
		assertEquals(mockService, Whitebox.invokeMethod(dnaAgent, "getService", "MockService"));
	}
	
	@Test
	public void sendQuery_returnNullResult() throws Throwable {
		Map<String, Service> mockServices = new HashMap<>();
		Service mockService = mock(Service.class);		
		when(mockService.isConnected()).thenReturn(true);
		when(mockService.exec("KDB Query")).thenReturn(null);
		mockServices.put("MockService", mockService);
		Whitebox.setInternalState(dnaAgent, "servicesInUse", mockServices);
		assertEquals("", dnaAgent.sendQuery("MockService", "KDB Query").toString());
	}
	
	@Test
	public void sendQuery_returnStringResult() throws Throwable {
		Map<String, Service> mockServices = new HashMap<>();
		Service mockService = mock(Service.class);		
		when(mockService.isConnected()).thenReturn(true);
		when(mockService.exec("KDB Query")).thenReturn("Result");
		mockServices.put("MockService", mockService);
		Whitebox.setInternalState(dnaAgent, "servicesInUse", mockServices);
		assertEquals("Result", dnaAgent.sendQuery("MockService", "KDB Query").toString());
	}
	
	@Test
	public void executeFunction_returnNullResult() throws Throwable {
		Object[] arg1 = {"JNJ"};
		Object[] arg2 = {10.0};
		Object[] arg3 = {11.0};
		String[] arg4 = {"xx12345"};
		Object[][] functionArgs = {{arg1, arg2, arg3, arg4}};
		
		Map<String, Service> mockServices = new HashMap<>();
		Service mockService = mock(Service.class);		
		when(mockService.isConnected()).thenReturn(true);
		when(mockService.exec("FunctionName", functionArgs)).thenReturn(null);
		mockServices.put("MockService", mockService);
		Whitebox.setInternalState(dnaAgent, "servicesInUse", mockServices);
		
		assertEquals("", dnaAgent.executeFunction("MockService", "FunctionName", functionArgs).toString());
	}
	
	@Test
	public void executeFunction_returnStringResult() throws Throwable {
		Object[] arg1 = {"JNJ"};
		Object[] arg2 = {10.0};
		Object[] arg3 = {11.0};
		String[] arg4 = {"xx12345"};
		Object[][] functionArgs = {{arg1, arg2, arg3, arg4}};
		
		Map<String, Service> mockServices = new HashMap<>();
		Service mockService = mock(Service.class);		
		when(mockService.isConnected()).thenReturn(true);
		when(mockService.exec("FunctionName", functionArgs)).thenReturn("Result");
		mockServices.put("MockService", mockService);
		Whitebox.setInternalState(dnaAgent, "servicesInUse", mockServices);
		
		assertEquals("Result", dnaAgent.executeFunction("MockService", "FunctionName", functionArgs).toString());
	}
	
	@Test
	public void close_pass() throws Exception{
		Map<String, Service> mockServices = new HashMap<>();
		Service mockService = mock(Service.class);
		when(mockService.isConnected()).thenReturn(true);		
		Connection mockConnection = mock(Connection.class);
		doNothing().when(mockConnection).close();
		when(mockService.getConnection()).thenReturn(mockConnection);
		mockServices.put("MockService", mockService);
		Whitebox.setInternalState(dnaAgent, "servicesInUse", mockServices);
		
		dnaAgent.close();
	}
		
}
