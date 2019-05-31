package fast.common.replay;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class TestFixTcpConnectionFactory {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testFixTcpConnectionFactory() throws Exception {
		FixTcpConnectionFactory factory = new FixTcpConnectionFactory(null, "name", null, null);
		factory.getConnection("connection", "action");
		assertNotNull(factory);
	}

	@Test
	public void testFixTcpConnectionFactoryCreateConnection() throws Exception {
		FixTcpConnectionFactory factory = new FixTcpConnectionFactory(null, "name", null, null);
		ReplayConnection conn = factory.createConnection("connection", "SEND");
		assertNotNull(conn);
	}

}
