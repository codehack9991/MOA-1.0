package fast.common.gmdReplay.pserver;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;

public class TestGMDCertificationClient {
	
	@Test
	public void GMDCertificationClient_ConstructorWithOneParameter() {
		GMDDummyServer server = null;
		GMDCertificationClient client = new GMDCertificationClient(server);
		assertNotNull(client);
	}

	@Test
	public void GMDCertificationClient_ConstructorWithfOURParameters() {
		GMDDummyServer server = null;
		String dumpFile = "";
		int bufferSize = 1000;
		long delay = 500;
		GMDCertificationClient client = new GMDCertificationClient(server, dumpFile, bufferSize, delay);
		assertNotNull(client);
	}	
	
}
