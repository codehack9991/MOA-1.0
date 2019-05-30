package fast.common.jira;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class TestCertificatedClient {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testConstructorWithoutParameter() {
		CertificatedClient certificatedClient = new CertificatedClient();
		assertNotNull(certificatedClient);
	}
	
	@Test
	public void testConstructorWithParameters() {
		CertificatedClient certificatedClient = new CertificatedClient("user", "pass");
		assertNotNull(certificatedClient);
	}
	
	@Test
	public void testConstructorWithParameters2() {
		CertificatedClient certificatedClient = new CertificatedClient("user", "3b e6 38 4e 76 3b a7 c2 13 d9 f1 d5 52 99 eb 3b");
		assertNotNull(certificatedClient);
	}

}
