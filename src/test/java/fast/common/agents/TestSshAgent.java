package fast.common.agents;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.TimeoutException;

import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;

import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;

import fast.common.context.CommonStepResult;
import fast.common.core.Configurator;

@RunWith(PowerMockRunner.class)
@PowerMockIgnore("javax.net.ssl.*")
public class TestSshAgent {
	
	@Mock
	private StringBuilder stringBuilder;
	
	@Mock
	private OutputStream outputStream;
	
	@Mock
	private InputStream inputStream;
	
	@Mock
	private Channel channel;
	
	@Mock
	private Properties properties;
	
	@Mock
	private Session session;
	
	@Mock
	private JSch jSch;
	
	@Rule
	public ExpectedException throwns = ExpectedException.none();

	private SshAgent sshagent =null;
	private Configurator c = null;
	private Map<String, String> agentParams =new HashMap();

	@Before
	public void setUp() throws Exception {
		agentParams.put("hostName", "localhost");
		agentParams.put("username", "remote-username");
		agentParams.put("port", "22999");
		agentParams.put("password", "remote-password");
        c =Configurator.getInstance();
		sshagent = new SshAgent("UnixAgent",agentParams,c);
		
	}
	
	@Test
	public void constructor_setParametersContainsTimeout()  {
		agentParams.put("runCommandTimeout", "6");
		sshagent = new SshAgent("UnixAgent",agentParams,c);
		assertEquals( sshagent.runCommandTimeout,6);
	}
	
	@Test
	public void readOutput_returnExceptedValue() throws IOException  {
		InputStream stubInputStream = IOUtils.toInputStream("This is for test", "UTF-8");
		String result = sshagent.readOutput(stubInputStream);
		assertEquals(result, "This is for test");
	}

	@Test
	public void readOutput_exceptedBreak() throws IOException  {
		when(inputStream.available()).thenReturn(6);
		when(inputStream.read(Mockito.any(byte[].class), Mockito.any(int.class), Mockito.any(int.class))).thenReturn(-1);
		String result = sshagent.readOutput(inputStream);
		assertEquals(0,result.length());
	}
	@Test
	public void waitForResponse_exceptTimeOutException() throws TimeoutException, IOException  {
		when(inputStream.available()).thenReturn(-6);
		throwns.expect(TimeoutException.class);
		throwns.expectMessage("Timed out when reading command output");
		sshagent.waitForResponse(inputStream);
//		try {
//			sshagent.waitForResponse(inputStream);
//		} catch (TimeoutException timeoutException) {
//			assertSame(timeoutException.getMessage(),
//					"Timed out when reading command output");
//		}
	}
	
	@Test
	public void waitForResponse_pass() throws IOException, TimeoutException {
		when(inputStream.available()).thenReturn(6);
		sshagent.waitForResponse(inputStream);
		assertTrue(inputStream.available()>0);
	}
	
	@Test	
	@PrepareForTest( { SshAgent.class})
	public void waitForResponse_InterruptedException() throws Exception {
		PowerMockito.spy(Thread.class);
		PowerMockito.doThrow(new InterruptedException()).when(Thread.class);
		Thread.sleep(Mockito.anyLong());
		agentParams.put("runCommandTimeout", "1");
		sshagent = new SshAgent("UnixAgent",agentParams,c);
        when(inputStream.available()).thenReturn(-6);
        try{
        	sshagent.waitForResponse(inputStream);
        }catch(TimeoutException e){
        	assertTrue(Thread.interrupted());
        }
        assertFalse(Thread.interrupted());
	}
	
	@Test
	public void testSendCommand() throws Exception{
		PowerMockito.whenNew(JSch.class).withNoArguments().thenReturn(jSch);
		when(jSch.getSession("remote-username","localhost",Integer.parseInt("22999"))).thenReturn(session);
		PowerMockito.whenNew(Properties.class).withNoArguments().thenReturn(properties);
		when(properties.put("StrictHostKeyChecking", "no")).thenReturn(properties);
		when(session.openChannel("shell")).thenReturn(channel);
		when(channel.getOutputStream()).thenReturn(outputStream);
		when(channel.getInputStream()).thenReturn(inputStream);
		when(inputStream.available()).thenReturn(2);
		byte[] tmp = new byte[1024];
		when(inputStream.read(tmp, 0, 1024)).thenReturn(1);
		StringBuilder responseMessageBuilder = Mockito.mock(StringBuilder.class);
		PowerMockito.whenNew(StringBuilder.class).withNoArguments().thenReturn(responseMessageBuilder);
		String str = "test";
		when(responseMessageBuilder.toString()).thenReturn(str);
		when(stringBuilder.append(str)).thenReturn(stringBuilder);
		when(responseMessageBuilder.append(str)).thenReturn(responseMessageBuilder);
		when(responseMessageBuilder.delete(0, responseMessageBuilder.length())).thenReturn(responseMessageBuilder);
		CommonStepResult commonStepResult = Mockito.mock(CommonStepResult.class);
		PowerMockito.whenNew(CommonStepResult.class).withNoArguments().thenReturn(commonStepResult);
		throwns.expect(NullPointerException.class);
		sshagent.sendCommand("testCommand");
	}

}
