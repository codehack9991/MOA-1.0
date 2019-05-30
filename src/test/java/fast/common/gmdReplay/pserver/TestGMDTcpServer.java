package fast.common.gmdReplay.pserver;

import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

import org.junit.Test;

public class TestGMDTcpServer {

	@Test
	public void testConstructor() {
		GMDTcpServer server = new GMDTcpServer(10086);
		assertNotNull(server);
	}
	
	@Test
	public void testSendMsgToAllClient() {
		GMDTcpServer server = new GMDTcpServer(10086);
		Socket e = null;
		try {
			e = new Socket("", 10086);
		} catch (UnknownHostException e1) {
			e1.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		server.sessionList.add(e);
		server.sendMsgToAllClient(new byte[0]);
		assertNotNull(server);
	}
	
	@Test
	public void testRegisterMBeans() {
		GMDTcpServer server = new GMDTcpServer(10086);
		GMDDummyServer server1 = null;
		String dumpFile = "";
		int bufferSize = 1000;
		long delay = 500;
		GMDCertificationClient client = new GMDCertificationClient(server1, dumpFile, bufferSize, delay);
		server.registerMBeans(client);
		assertNotNull(server);
	}
	
}
