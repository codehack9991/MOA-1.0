package fast.common.jira;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.assertNull;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;


@RunWith(PowerMockRunner.class)
@PowerMockIgnore("javax.management.*")
public class TestHttpRequest {
	
	@Mock
	private CertificatedClient client;
	
	@Mock
	private ClientResponse response;
	
	@Mock
	private WebResource resource;
	
	@Mock
	private com.sun.jersey.api.client.WebResource.Builder builder;
	
	@InjectMocks
	private HttpRequest request=new HttpRequest();
	
	@Test
	public void constructor_withParams_fieldSetPoperly() {
		HttpRequest request = new HttpRequest("username", "password");
		assertNotNull(Whitebox.getInternalState(request, "client"));
	}

	@Test
	public void constructor_withNoParams_fieldSetPoperly() {
		HttpRequest request = new HttpRequest();
		assertNotNull(Whitebox.getInternalState(request, "client"));
	}

	@Test
	public void sendGetRequest_exceptionThrown() {
		HttpRequest request = new HttpRequest();
		try {
			request.sendGetRequest("http://localhost:8089/greeting");
		} catch (Exception ex) {
			assertNotEquals(RequestNotSucceedException.class.getName(), ex.getClass().getName());
		}
	}
	
	@Test
	public void sendGetRequest_passed(){
		when(response.getEntity(String.class)).thenReturn("{status:200}");
		when(response.getStatus()).thenReturn(200);
		when(builder.get(ClientResponse.class)).thenReturn(response);
		when(resource.accept("application/json")).thenReturn(builder);
		when(client.resource("http://localhost:8089/greeting")).thenReturn(resource);
		try {
			String result=request.sendGetRequest("http://localhost:8089/greeting");
			assertEquals(result, "{status:200}");
		} catch (RequestNotSucceedException e) {
			assertNull(e);
		}	
	}
	
	@Test
	public void sendGetRequest_notfound(){
		when(response.getEntity(String.class)).thenReturn("Not found");
		when(response.getStatus()).thenReturn(404);
		when(builder.get(ClientResponse.class)).thenReturn(response);
		when(resource.accept("application/json")).thenReturn(builder);
		when(client.resource("http://localhost:8089/greeting")).thenReturn(resource);
		try {
			request.sendGetRequest("http://localhost:8089/greeting");			
		} catch (RequestNotSucceedException e) {			
			assertEquals(String.format(RequestNotSucceedException.EXCEPTION_MESSAGE_PATTERN, response.getStatus(),"Not found"),
					"StatusCode:404.ErrorMessage:Not found");
		}	
	}

	@Test
	public void sendPostRequest_exceptionThrown() {
		HttpRequest request = new HttpRequest();
		try {
			request.sendPostRequest("http://localhost:8089/greeting", null);
		} catch (Exception ex) {
			assertNotEquals(RequestNotSucceedException.class.getName(), ex.getClass().getName());
		}
	}

	@Test
	public void sendPostRequest_passed(){
		when(response.getEntity(String.class)).thenReturn("{status:200}");
		when(response.getStatus()).thenReturn(200);
		when(builder.type("application/json")).thenReturn(builder);
		when(builder.post(ClientResponse.class,null)).thenReturn(response);
		when(resource.accept("application/json")).thenReturn(builder);
		when(client.resource("http://localhost:8089/greeting")).thenReturn(resource);
		try {
			String result=request.sendPostRequest("http://localhost:8089/greeting",null);
			assertEquals(result, "{status:200}");
		} catch (RequestNotSucceedException e) {
			assertNull(e);
		}	
	}
	
	@Test
	public void sendPutRequest_exceptionThrown() {
		HttpRequest request = new HttpRequest();
		try {
			request.sendPutRequest("http://localhost:8089/greeting", null);
		} catch (Exception ex) {
			assertNotEquals(RequestNotSucceedException.class.getName(), ex.getClass().getName());
		}
	}
	
	@Test
	public void sendPutRequest_passed(){
		when(response.getEntity(String.class)).thenReturn("{status:200}");
		when(response.getStatus()).thenReturn(200);
		when(builder.type("application/json")).thenReturn(builder);
		when(builder.put(ClientResponse.class,null)).thenReturn(response);
		when(resource.accept("application/json")).thenReturn(builder);
		when(client.resource("http://localhost:8089/greeting")).thenReturn(resource);
		try {
			String result=request.sendPutRequest("http://localhost:8089/greeting",null);
			assertEquals(result, "{status:200}");
		} catch (RequestNotSucceedException e) {
			assertNull(e);
		}	
	}

	@Test
	public void getResultFromResponse_returnNull() throws IllegalAccessException, IllegalArgumentException,
			InvocationTargetException, NoSuchMethodException, SecurityException {
		ClientResponse response = null;
		HttpRequest request = new HttpRequest();
		Method method = HttpRequest.class.getDeclaredMethod("getResultFromResponse", ClientResponse.class);
		method.setAccessible(true);
		assertNull(method.invoke(request, response));
	}
}
