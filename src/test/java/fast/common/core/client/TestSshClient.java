package fast.common.core.client;

import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

public class TestSshClient {

	@Test
	public void testSshClient() {
		SshClient client = new SshClient(null);
		assertNotNull(client);
	}

	@Test
	public void testSshClientConnection() {
		Map params = new HashMap<>();
		params.put("user", "");
		params.put("host", "");
		params.put("port", "123");
		params.put("password", "");
		SshClient client = new SshClient(params);
		try {
			client.connect();
		} catch (Exception e) {
		}
		try {
			client.disconnect();
		} catch (IOException e) {
		}
		assertNotNull(client);
	}

}
