package fast.common.jira;

import static org.junit.Assert.assertNotNull;

import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.net.URI;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import org.junit.Test;

public class TestAuthenticator {
	private ClientRequestContext requestContext = new ClientRequestContext() {
		
		@Override
		public void setUri(URI uri) {
		}
		
		@Override
		public void setProperty(String name, Object object) {
		}
		
		@Override
		public void setMethod(String method) {
		}
		
		@Override
		public void setEntityStream(OutputStream outputStream) {
		}
		
		@Override
		public void setEntity(Object entity, Annotation[] annotations, MediaType mediaType) {
		}
		
		@Override
		public void setEntity(Object entity) {
		}
		
		@Override
		public void removeProperty(String name) {
		}
		
		@Override
		public boolean hasEntity() {
			return false;
		}
		
		@Override
		public URI getUri() {
			return null;
		}
		
		@Override
		public MultivaluedMap<String, String> getStringHeaders() {
			return null;
		}
		
		@Override
		public Collection<String> getPropertyNames() {
			return null;
		}
		
		@Override
		public Object getProperty(String name) {
			return null;
		}
		
		@Override
		public String getMethod() {
			return null;
		}
		
		@Override
		public MediaType getMediaType() {
			return null;
		}
		
		@Override
		public Locale getLanguage() {
			return null;
		}
		
		@Override
		public MultivaluedMap<String, Object> getHeaders() {
			return null;
		}
		
		@Override
		public String getHeaderString(String name) {
			return null;
		}
		
		@Override
		public Type getEntityType() {
			return null;
		}
		
		@Override
		public OutputStream getEntityStream() {
			return null;
		}
		
		@Override
		public Class<?> getEntityClass() {
			return null;
		}
		
		@Override
		public Annotation[] getEntityAnnotations() {
			return null;
		}
		
		@Override
		public Object getEntity() {
			return null;
		}
		
		@Override
		public Date getDate() {
			return null;
		}
		
		@Override
		public Map<String, Cookie> getCookies() {
			return null;
		}
		
		@Override
		public Configuration getConfiguration() {
			return null;
		}
		
		@Override
		public Client getClient() {
			return null;
		}
		
		@Override
		public List<MediaType> getAcceptableMediaTypes() {
			return null;
		}
		
		@Override
		public List<Locale> getAcceptableLanguages() {
			return null;
		}
		
		@Override
		public void abortWith(Response response) {
		}
	};

	@Test
	public void testAuthenticator() {
		Authenticator authenticator = new Authenticator("","");
		try {
			authenticator.filter(requestContext);
		} catch (Exception e) {
		}
		assertNotNull(authenticator);
	}

}
