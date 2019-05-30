package fast.common.jira;


import java.security.SecureRandom;
import java.security.cert.X509Certificate;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.filter.HTTPBasicAuthFilter;
import fast.common.cipher.*;

/**
 * Certificated HTTP client
 * @author lc37141
 *
 */
public class CertificatedClient extends Client{
	
	/**
	 * Constructor of Certificated Client Class
	 * @param username
	 * @param passcode
	 */
	public CertificatedClient(String username, String passcode) {
		this();
		String password=AES.isCipherFormat(passcode) ? AES.decode(passcode) : passcode;
		this.addFilter(new HTTPBasicAuthFilter(username, password));
	}
	
	public CertificatedClient(){
		try {

			TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
				public X509Certificate[] getAcceptedIssuers() {
					return null;
				}

				public void checkClientTrusted(X509Certificate[] certs, String authType) {
				}

				public void checkServerTrusted(X509Certificate[] certs, String authType) {
				}
			} };

			SSLContext sc = SSLContext.getInstance("TLS");
			sc.init(null, trustAllCerts, new SecureRandom());
			HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
		} catch (Exception e) {

		}
		create();
	}
	
}
