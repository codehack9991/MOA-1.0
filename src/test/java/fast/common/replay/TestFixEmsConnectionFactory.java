package fast.common.replay;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

public class TestFixEmsConnectionFactory {

	private FixEmsConnectionFactory factory;
	private FixEmsConnectionFactory factory2;
	private HashMap<String, ReplayConnection> _connections;
	private HashMap<String, ReplayConnection> _connections2;
	private Map param3 = new HashMap<>();

	@Before
	public void setUp() throws Exception {
		Map param = new HashMap<>();
		Map param2 = new HashMap<>();

		Map brokers = new HashMap<>();
		brokers.put("broker", null);
		Map topicsMapping = new HashMap<>();
		topicsMapping.put("<default_send>", "msg");
		
		Map topicsMapping2 = new HashMap<>();
		topicsMapping2.put("<default_send>", "msg");
		topicsMapping2.put("connection", "broker:connection");

		param.put("Brokers", brokers);
		param.put("TopicsMapping", topicsMapping);
		param.put("connection", null);

		param2.put("Brokers", brokers);
		param2.put("TopicsMapping", topicsMapping2);

		_connections = new HashMap<>();
		_connections2 = new HashMap<>();
		param3.put("data_dictionary", "FIX42.xml");
		param3.put("TopicsMapping", topicsMapping);
		param3.put("Brokers", brokers);
		ReplayConnection objConn = new FixEmsReplayConnection(null, null, param3, "fast/common/fix/resources/",
				"connection", false);
		ReplayConnection objConn2 = new FixEmsReplayConnection(null, null, param3, "fast/common/fix/resources/",
				"connection", true);
		_connections.put("connection", objConn);
		_connections2.put("connection", objConn2);

		factory = new FixEmsConnectionFactory(null, "name", param, "");
		factory2 = new FixEmsConnectionFactory(null, "name", param2, "");
	}

	@Test
	public void testConstructor() {
		FixEmsConnectionFactory fixEmsConnectionFactory = new FixEmsConnectionFactory(null, "name", null, "");
		assertNotNull(fixEmsConnectionFactory);
	}

	@Test
	public void testGetConnectionName() throws Exception {

		try {
			factory.getConnection(null, "TEST");
		} catch (Exception e) {
		}

		String connectionName = factory.getConnectionName("connection", "SEND");
		assertNotNull(connectionName);
	}

	@Test
	public void testCreateConnectionWithInvalidAction() {

		ReplayConnection conn = null;
		try {
			conn = factory.createConnection("connection", "");
		} catch (Exception e) {
		}
		assertNull(conn);
	}

	@Test
	public void testCreateConnectionWithInvalidConnection() throws Exception {
		ReplayConnection conn = null;
		try {
			conn = factory2.createConnection("connection", "SEND");
		} catch (Exception e) {
		}
		assertNull(conn);
	}

	@Test
	public void testCreateConnectionShouldReturnNull() throws Exception {
		ReplayConnection conn = null;
		try {
			conn = factory.createConnection("connection", "SEND");
		} catch (Exception e) {
		}
		assertNull(conn);
	}

	@Test
	public void testCreateConnectionForSendingShouldReturnNull() throws Exception {
		ReplayConnection conn = null;
		try {
			conn = factory2.createConnection("connection", "SEND");
		} catch (Exception e) {
		}
		assertNull(conn);
	}

	@Test
	public void testCreateConnectionWithFactoryShouldReturnNull() throws Exception {
		ReplayConnection conn = null;

		try {
			conn = factory.createConnection("connection", "CHECK_RECEIVE");
		} catch (Exception e) {
		}
		assertNull(conn);
	}

	@Test
	public void testCreateConnectionWithFactory2ShouldReturnNull() throws Exception {
		ReplayConnection conn = null;
		try {
			conn = factory2.createConnection("connection", "CHECK_RECEIVE");
		} catch (Exception e) {
		}
		assertNull(conn);
	}

	@Test
	public void testCreateConnectionShouldReturnConnection() throws Exception {
		ReplayConnection conn = null;
		try {
			factory2._connections = _connections;
			conn = factory2.createConnection("connection", "CHECK_RECEIVE");
		} catch (Exception e) {
		}
		assertNotNull(conn);
	}	
}
