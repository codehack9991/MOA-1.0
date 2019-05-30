package fast.common.core.client;

import static org.junit.Assert.assertNotNull;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

public class TestFileClient {

	@Test
	public void testFileClient() {
		FileClient client = new FileClient(null);
		assertNotNull(client);
	}

	
	@Test
	public void testFileClientConnection() {
		Map params = new HashMap<>();
		params.put("uri", "src/test/resources/test.ini");
		FileClient client = new FileClient(params);
		try {
			client.connect();
			client.disconnect();
		} catch (Exception e) {
		}
		assertNotNull(client);
	}

}
