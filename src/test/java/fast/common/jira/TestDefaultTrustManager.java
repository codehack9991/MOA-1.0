package fast.common.jira;

import static org.junit.Assert.assertNull;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import org.junit.Test;

public class TestDefaultTrustManager {

	@Test
	public void testDefaultTrustManager() throws CertificateException {
		DefaultTrustManager defaultTrustManager = new DefaultTrustManager();
		defaultTrustManager.checkClientTrusted(null, null);
		defaultTrustManager.checkServerTrusted(null, null);
		X509Certificate[] acceptedIssuers = defaultTrustManager.getAcceptedIssuers();
		assertNull(acceptedIssuers);
	}

}
