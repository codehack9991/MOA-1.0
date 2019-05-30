package fast.common.agents;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Vector;
import java.io.File;

import org.apache.commons.io.IOUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import java.io.InputStreamReader;
import java.net.ConnectException;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpATTRS;
import com.jcraft.jsch.ChannelSftp.LsEntry;

import fast.common.core.Configurator;

@RunWith(PowerMockRunner.class)
@PowerMockIgnore("javax.net.ssl.*")
public class TestSftpAgent {

	@Mock
	ChannelSftp sftpChannel;

	@Mock
	ChannelSftp channel;

	@Mock
	Properties properties;

	@Mock
	Session session;

	@Mock
	JSch jSch;

	@Mock
	InputStreamReader inputStreamReader;
	
	static Map<String, Object> agentParams;
	static Configurator c ;
	static{
		try {
			c = Configurator.getInstance();
		} catch (Exception e) {
			e.printStackTrace();
		}
		agentParams = new HashMap();
		agentParams.put("host", "localhost");
		agentParams.put("username", "remote-username");
		agentParams.put("port", 22);
		agentParams.put("password", "remote-password");
		c.getSettingsMap().put("sftpAgent", agentParams);
	}

	@InjectMocks
	SftpAgent agent=new SftpAgent("sftpAgent", agentParams, c);

	
	@Rule
	public ExpectedException throwns = ExpectedException.none();

	@Before
	public void setUp() throws Exception {
		when(jSch.getSession(Mockito.any(String.class),Mockito.any(String.class),Mockito.any(Integer.class))).thenReturn(session);
		doNothing().when(session).setPassword(Mockito.any(String.class));
		doNothing().when(session).setConfig(Mockito.any(Properties.class));
		doNothing().when(session).connect();
		when(session.openChannel("sftp")).thenReturn(channel);
		doNothing().when(channel).connect();	
	}

	@After
	public void tearDown() throws Exception {
		agent.close();
	}
	
	@Test
	public void connectUsingAgentParams_pass() throws Exception {
		Whitebox.setInternalState(agent, "_agentParams", agentParams);
		Whitebox.invokeMethod(agent,"connectUsingAgentParams");
	}
	
	@Test
	public void connect_pass() throws Exception {
		Whitebox.invokeMethod(agent,"connect",	"remote-username", "localhost", 22, "remote-password");
	}
	
	@Test
	public void connect_JSchException() throws Exception {
		
		 Map<String, Object> realagentParams = new HashMap();
		 realagentParams.put("host", "localhost");
		 realagentParams.put("username", "remote-username");
		 realagentParams.put("port", 12345);
		 realagentParams.put("password", "remote-password");
		 c.getSettingsMap().put("readlsftpAgent", realagentParams);

		SftpAgent realAgent=new SftpAgent("readlsftpAgent", realagentParams, c);
		try{
		   realAgent.connect("remote-username", "localhost", 12345, "remote-password");
			throwns.expect(NullPointerException.class);
		}catch(JSchException | ConnectException e){
			assertTrue(e.getMessage().contains("Connection refused"));
		}
	}
	
	@Test
	public void getFileContentFromRemote_pass() throws Exception {
		InputStream stubInputStream = IOUtils.toInputStream("This is for test", "UTF-8");
		when(channel.get("/remote/file/path")).thenReturn(stubInputStream);
		StringBuilder stringBuilder = Mockito.mock(StringBuilder.class);
		PowerMockito.whenNew(StringBuilder.class).withNoArguments().thenReturn(stringBuilder);
		BufferedReader bufferedReader = Mockito.mock(BufferedReader.class);
		PowerMockito.whenNew(BufferedReader.class).withAnyArguments().thenReturn(bufferedReader);
		
		Whitebox.setInternalState(agent, "_agentParams", agentParams);
		Whitebox.invokeMethod(agent,"connectUsingAgentParams");
		String result = Whitebox.invokeMethod(agent,"getFileContentFromRemote","/remote/file/path");
		assertTrue(result.contains("This is for test"));
	}
	
	@Test
	public void checkFileExist_pass() throws Exception{
		SftpATTRS fileAttributes = Mockito.mock(SftpATTRS.class);
		when(channel.lstat("/remote/file/path")).thenReturn(fileAttributes);
		Whitebox.setInternalState(agent, "_agentParams", agentParams);
		Whitebox.invokeMethod(agent,"connectUsingAgentParams");
	    boolean result = Whitebox.invokeMethod(agent,"checkFileExist","/remote/file/path");
	    assertTrue(result);
	}
	
	
	@Test
	@PrepareForTest({SftpAgent.class,File.class})
	public void getFileFromRemote_pass() throws Exception{
		File file = PowerMockito.mock(File.class);
		SftpAgent agentSpy =  PowerMockito.spy(agent);
		PowerMockito.doReturn(true).when(agentSpy).checkFileExist("/remote/file/path");
		PowerMockito.whenNew(File.class).withArguments("C://file//path").thenReturn(file);
		when(file.exists()).thenReturn(true);
		PowerMockito.doNothing().when(channel).get("/remote/file/path","C://file//path");
		Whitebox.setInternalState(agentSpy, "_agentParams", agentParams);
		Whitebox.invokeMethod(agentSpy,"connectUsingAgentParams");
		boolean result =agentSpy.getFileFromRemote("/remote/file/path","C://file//path");
		assertTrue(result);
	}


	@Test
	public void getFileFromRemote_noExistFIleLocal() throws Exception{
		SftpAgent agentSpy =  Mockito.spy(agent);
		Mockito.doReturn(true).when(agentSpy).checkFileExist("/remote/file/path");
		PowerMockito.doNothing().when(channel).get("/remote/file/path","any");
		Whitebox.setInternalState(agentSpy, "_agentParams", agentParams);
		Whitebox.invokeMethod(agentSpy,"connectUsingAgentParams");
		boolean result =agentSpy.getFileFromRemote("/remote/file/path","any");
		assertFalse(result);
	}
	
	@Test
	public void getDirectoryContent_pass() throws Exception{
		Vector<LsEntry> filelist = new Vector<> ();
		LsEntry le1 = Mockito.mock(LsEntry.class);
		when(le1.getLongname()).thenReturn("test1");
		LsEntry le2 = Mockito.mock(LsEntry.class);
		when(le2.getLongname()).thenReturn("test2");
		filelist.add(le1);
		filelist.add(le2);
		when(channel.ls("/remote/file/path")).thenReturn(filelist);
		Whitebox.setInternalState(agent, "_agentParams", agentParams);
		Whitebox.invokeMethod(agent,"connectUsingAgentParams");
		Vector<LsEntry> resultfilelist= Whitebox.invokeMethod(agent,"getDirectoryContent","/remote/file/path");
		assertTrue(resultfilelist.size()==2);
	}
	
	@Test
	public void getDirectoryContent_resultIsEmpty() throws Exception{
		Vector<LsEntry> filelist = new Vector<> ();
		when(channel.ls("/remote/file/path")).thenReturn(filelist);
		Whitebox.setInternalState(agent, "_agentParams", agentParams);
		Whitebox.invokeMethod(agent,"connectUsingAgentParams");
		Vector<LsEntry> resultfilelist= Whitebox.invokeMethod(agent,"getDirectoryContent","/remote/file/path");
		assertTrue(resultfilelist.isEmpty());
	}
	
	@Test
	public void isSessionMaintained_ExcepteDefaultFalse() throws Exception{
		boolean result =  Whitebox.invokeMethod(agent,"isSessionMaintained");
		assertFalse(result);
	}
	
	@Test
	public void isConnected_ExcepteDefaultFalse() throws Exception{
		boolean result =  Whitebox.invokeMethod(agent,"isConnected");
		assertFalse(result);
	}
	@Test
	public void setSessionMaintained_SetIsSessionMaintainedTrue() throws Exception{
		Whitebox.invokeMethod(agent,"setSessionMaintained",true);
		boolean result =  Whitebox.invokeMethod(agent,"isSessionMaintained");
		assertTrue(result);
	}
	@Test
	public void disconnect_pass() throws Exception{
		when(channel.isConnected()).thenReturn(true);
		when(session.isConnected()).thenReturn(true);
		Whitebox.setInternalState(agent, "_agentParams", agentParams);
		Whitebox.invokeMethod(agent,"connectUsingAgentParams");
		boolean connect =  Whitebox.invokeMethod(agent,"isConnected");
		assertTrue(connect);
		Whitebox.invokeMethod(agent,"disconnect");
		boolean disconnect =  Whitebox.invokeMethod(agent,"isConnected");
		assertFalse(disconnect);
	}

}
