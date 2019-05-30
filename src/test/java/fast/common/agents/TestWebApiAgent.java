package fast.common.agents;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import com.alibaba.fastjson.JSONObject;

import fast.common.agents.WebApiAgent.RequestType;
import fast.common.context.StepResult;
import fast.common.context.StepResult.Status;
import fast.common.core.Configurator;
import fast.common.context.WebApiResult;
import io.restassured.RestAssured;
import io.restassured.authentication.NoAuthScheme;
import io.restassured.authentication.PreemptiveAuthProvider;
import io.restassured.http.Header;
import io.restassured.http.Headers;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;


@RunWith(PowerMockRunner.class)
@PowerMockIgnore("javax.net.ssl.*")
@PrepareForTest(RestAssured.class)
public class TestWebApiAgent {
	@Mock
	private RequestSpecification uncompletedRequest;
	
	@Mock
	private Response response;
	
	@Mock
	private Headers headers;
	
	@Mock
	private PreemptiveAuthProvider preemptiveAuthProvider;
	
	@InjectMocks
	private WebApiAgent agent = new WebApiAgent();
	
	@Before
	public void setup(){		
		List<Header> headerList = new ArrayList<Header>();
		when(headers.asList()).thenReturn(headerList);
		when(response.headers()).thenReturn(headers);			
	}
	
	@Test
	public void construct_configuration_webApiAgent() throws Exception{
		String name = "nonMeaningName";
		Map<?,?> agentParams = null;
		Configurator configurator = Configurator.getInstance();
		WebApiAgent agent = new WebApiAgent(name, agentParams, configurator);
		assertEquals("nonMeaningName",Whitebox.getInternalState(agent,"_name"));
	}
	
	@Test
	public void construct_webApiAgentWithUri(){
		String uri = "nonMeaningUri";
		WebApiAgent agent = new WebApiAgent(uri);
		assertEquals(uri,RestAssured.baseURI);
	}
	
	@Test
	public void construct_Authentication_webApiAgent(){
		String username = "nonMeaningUsername";
		String password = "nonMeaningPassword";
		PowerMockito.mockStatic(RestAssured.class);
		when(RestAssured.preemptive()).thenReturn(preemptiveAuthProvider);
		NoAuthScheme noAuthScheme = new NoAuthScheme();
		when(preemptiveAuthProvider.basic(username, password)).thenReturn(noAuthScheme);
		WebApiAgent agent = new WebApiAgent(username,password);
		assertEquals(noAuthScheme, RestAssured.authentication);
	}
	
	@Test
	public void construct_Authentication_webApiAgentWithUri(){
		String uri = "nonMeaningUri";
		String username = "nonMeaningUsername";
		String password = "nonMeaningPassword";
		WebApiAgent agent = new WebApiAgent(uri,username,password);
		assertEquals(uri,RestAssured.baseURI);
	}
	
	@Test
	public void sendRestRequest_allParams(){
		String uri = "nonMeaningUri";
		Map<String, Object> pathParams = new HashMap<>();
		pathParams.put("invalidKey", "invalidValue");
		Map<String, Object> params = new HashMap<>();
		params.put("invalidKey", "invalidValue");
		Map<String, Object> body = new HashMap<>();
		body.put("invalidKey", "invalidValue");
		WebApiResult result= (WebApiResult) agent.sendRestRequest(RequestType.GET, uri, pathParams,params,body);
		assertEquals(result.getStatus(), Status.Failed);
	}
	
	@Test
	public void sendRestRequest_nullBody(){
		String uri = "nonMeaningUri";
		Map<String, Object> pathParams = new HashMap<>();
		pathParams.put("invalidKey", "invalidValue");
		Map<String, Object> params = new HashMap<>();
		params.put("invalidKey", "invalidValue");
		Map<String, Object> body = null;
		WebApiResult result= (WebApiResult) agent.sendRestRequest(RequestType.GET, uri, pathParams,params,body);
		assertEquals(result.getStatus(), Status.Failed);
	}
	
	@Test
	public void sendRestRequest_deprecated(){
		String uri = "nonMeaningUri";
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("invalidKey", "invalidValue");
		@SuppressWarnings("deprecation")
		WebApiResult result= (WebApiResult) agent.sendRestRequest(RequestType.GET, uri, jsonObject);
		assertEquals(result.getStatus(), Status.Failed);
	}
	
	@Test
	public void sendRestRequest_deprecated_nullJsonObject(){
		String uri = "nonMeaningUri";
		JSONObject jsonObject = null;
		@SuppressWarnings("deprecation")
		WebApiResult result= (WebApiResult) agent.sendRestRequest(RequestType.GET, uri, jsonObject);
		assertEquals(result.getStatus(), Status.Failed);
	}

	@Test
	public void sendRequest_withInvalidUrl_failed(){
		String invalidUrl = "invalidUrl";
		String errorMessage = "Invalid URL";
		when(uncompletedRequest.get(invalidUrl)).thenThrow(new RuntimeException(errorMessage));
		WebApiResult result= (WebApiResult) agent.generate().sendRequest(RequestType.GET,invalidUrl);

		assertEquals(result.getStatus(), Status.Failed);
		assertEquals(result.getFailedMessage(), errorMessage);
	}
	@Test
	public void sendRequest_null_Failed() throws Throwable{	
		String testurl = "anything";
		when(uncompletedRequest.get(anyString())).thenReturn(response);
	    StepResult result= agent.sendRequest(null,testurl);
		assertEquals(Status.Failed, result.getStatus());
	}
	@Test
	public void sendRequest_get_passed() throws Throwable{	
		when(uncompletedRequest.get(anyString())).thenReturn(response);
		
		StepResult result=agent.sendRequest(RequestType.GET, anyString());
		assertEquals(Status.Passed, result.getStatus());
	}
	
	@Test
	public void sendRequest_put_passed() throws Throwable{			
		when(uncompletedRequest.put(anyString())).thenReturn(response);
		
		StepResult result=agent.sendRequest(RequestType.PUT, anyString());
		assertEquals(Status.Passed, result.getStatus());
	}
	
	@Test
	public void sendRequest_post_passed() throws Throwable{			
		when(uncompletedRequest.post(anyString())).thenReturn(response);
		
		StepResult result=agent.sendRequest(RequestType.POST, anyString());
		assertEquals(Status.Passed, result.getStatus());
	}
	
	@Test
	public void sendRequest_delete_passed() throws Throwable{			
		when(uncompletedRequest.delete(anyString())).thenReturn(response);
		
		StepResult result=agent.sendRequest(RequestType.DELETE, anyString());
		assertEquals(Status.Passed, result.getStatus());
	}
	

	@SuppressWarnings("unchecked")
	@Test
	public void setHeaders_headersAreSet() throws Throwable{
		when(uncompletedRequest.headers(any(Map.class))).thenReturn(uncompletedRequest);		
		
		HashMap<String, String> requestHeaders = new HashMap<String, String>();
		requestHeaders.put("Accept", "application/json");
		assertEquals(agent, agent.setHeaders(requestHeaders));		
	}
	
	@Test
	public void TestForGenerate(){		
		assertNotNull(agent.generate());
	}
	
	@SuppressWarnings("resource")
	@Test
	public void TestForContentType(){		
		assertNotNull((new WebApiAgent()).setContentType("application/json"));
	}
	
	@SuppressWarnings("resource")
	@Test
	public void TestForSetHeaders(){		
		assertNotNull((new WebApiAgent()).setHeaders(new HashMap<>()));
	}

	@SuppressWarnings("resource")
	@Test
	public void TestForSetPathParams(){		
		assertNotNull((new WebApiAgent()).setPathParams(new HashMap<>()));
	}
	
	@SuppressWarnings("resource")
	@Test
	public void TestForParams(){		
		assertNotNull((new WebApiAgent()).setParams(new HashMap<>()));
	}
	
	@SuppressWarnings("resource")
	@Test
	public void TestForSetBody(){		
		assertNotNull((new WebApiAgent()).setBody(new HashMap<>()));
	}
	
	@Test
	public void useRelaxedHTTPSValidation_noExceptionThrown(){
		(new WebApiAgent()).useRelaxedHTTPSValidation();
	}
	
	@Test
	public void TestForClose() throws Exception{
		agent.close();
	}
}
