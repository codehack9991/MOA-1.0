package fast.common.jira;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import fast.common.jira.entities.Cycle;
import fast.common.jira.entities.VersionBoard;

@RunWith(PowerMockRunner.class)
@PowerMockIgnore("javax.management.*")
public class TestZapiRestService {

	@InjectMocks
	private ZapiRestService zapiRestService = new ZapiRestService("url", "user", "pass");

	@Mock
	private HttpRequest client;

	@Mock
	private VersionBoard board;

	@Test
	public void test() throws Exception {
		assertEquals("", "");
		try {
			HttpRequest request = mock(HttpRequest.class);
			when(request.sendPostRequest(any(String.class), any(String.class))).thenReturn("");

			ZapiRestService mockObj = mock(ZapiRestService.class);
			mockObj.setClient(request);
			mockObj.createNewCycle("", "", "");
		} catch (JiraTransactionException ex) {

		}
	}

	@Test
	public void testGetClient() {
		zapiRestService.setClient(null);
		HttpRequest client = zapiRestService.getClient();
		assertNull(client);
		assertNotNull(zapiRestService);
	}

	@Test
	public void constructor_initializeCorrectly() {
		String url = "any url";
		String userName = "any user";
		String password = "any password";
		ZapiRestService service = new ZapiRestService(url, userName, password);

		assertEquals(Whitebox.getInternalState(service, "url"), url);
		assertEquals(Whitebox.getInternalState(service, "userName"), userName);
		assertEquals(Whitebox.getInternalState(service, "password"), password);
		assertNotNull(Whitebox.getInternalState(service, "gson"));
	}

	@Test
	public void connect_clientCreated() {
		ZapiRestService service = new ZapiRestService("url", "userName", "password");
		service.connect();
		assertNotNull(service.getClient());
	}

	@Test
	public void getClient_null() {
		ZapiRestService service = new ZapiRestService("url", "userName", "password");
		assertNull(service.getClient());
	}

	@Test
	public void setClient_getClientReturnNotNull() {
		ZapiRestService service = new ZapiRestService("url", "userName", "password");
		service.setClient(new HttpRequest());
		assertNotNull(service.getClient());
	}

	@Test
	public void getVersionsByProjectId_passed() throws RequestNotSucceedException {	
		String json="{\"unreleasedVersions\": [{\"value\": \"-1\",\"archived\": false,\"label\": \"Unscheduled\"}],\"releasedVersions\": [{\"value\": \"-1\",\"archived\": false,\"label\": \"Unscheduled\"}]}";
		when(board.getUnreleasedVersions()).thenReturn(new ArrayList<>());
		when(board.getReleasedVersions()).thenReturn(new ArrayList<>());
		when(client.sendGetRequest(any(String.class))).thenReturn(json);

		assertEquals(zapiRestService.getVersionsByProjectId("projectid").size(), 2);
	}

	@Test
	public void getVersionsByProjectId_exceptionThrown() {

		try {
			when(board.getUnreleasedVersions()).thenReturn(new ArrayList<>());
			when(board.getReleasedVersions()).thenReturn(new ArrayList<>());
			when(client.sendGetRequest(any(String.class))).thenThrow(new RequestNotSucceedException());
			zapiRestService.getVersionsByProjectId("projectid");
		} catch (Exception e) {
			assertNull(e);
		}
	}

	@Test
	public void getCyclesByProjectAndVersion_passed_noresult() throws RequestNotSucceedException {
		when(client.sendGetRequest(any(String.class))).thenReturn("");
		Map<String, Cycle> result = zapiRestService.getCyclesByProjectAndVersion("", "");
		assertEquals(result.keySet().size(), 0);
	}

	@Test
	public void getCyclesByProjectAndVersion_exceptionThrown() {
		try {
			when(client.sendGetRequest(any(String.class))).thenThrow(new RequestNotSucceedException());
			Map<String, Cycle> result = zapiRestService.getCyclesByProjectAndVersion("", "");
			assertEquals(result.keySet().size(), 0);
		} catch (Exception e) {
			assertNull(e);
		}
	}

	@Test
	public void getCyclesByProjectAndVersion_passed_correctResult() throws RequestNotSucceedException {
		String json = "{\"-1\": {\"totalExecutions\": 0," + "\"endDate\": \"\"," + "\"description\": \"\","
				+ "\"totalExecuted\": 0," + "\"started\": \"\"," + "\"expand\": \"executionSummaries\","
				+ "\"projectKey\": \"C167813\"," + "\"versionId\": 28504," + "\"environment\": \"\","
				+ "\"build\": \"\"," + "\"ended\": \"\"," + "\"name\": \"Ad hoc\"," + "\"modifiedBy\": \"\","
				+ "\"projectId\": 15201," + "\"startDate\": \"\","
				+ "\"executionSummaries\": { \"executionSummary\": [] }}," + "\"recordsCount\": 1}";

		when(client.sendGetRequest(any(String.class))).thenReturn(json);
		Map<String, Cycle> result = zapiRestService.getCyclesByProjectAndVersion("", "");
		assertNotEquals(result.keySet().size(), 0);
	}

	@Test
	public void createNewCycle_passed() throws RequestNotSucceedException, JiraTransactionException {
		String json = "{ \"id\":10000}";
		when(client.sendPostRequest(any(String.class), any(String.class))).thenReturn(json);
		String result = zapiRestService.createNewCycle("", "", "");
		assertEquals(result, "10000");
	}

	@Test
	public void createNewCycle_missingid_exceptionThrown() {
		try {
			String json = "{ \"message\":10000}";
			when(client.sendPostRequest(any(String.class), any(String.class))).thenReturn(json);
			zapiRestService.createNewCycle("", "", "");
		} catch (Exception ex) {
			assertEquals(ex.getClass().getName(), JiraTransactionException.class.getName());
		}
	}

	@Test
	public void createNewCycle_invalidjson_exceptionThrown() {
		try {
			String json = "XXXX";
			when(client.sendPostRequest(any(String.class), any(String.class))).thenReturn(json);
			zapiRestService.createNewCycle("", "", "");
		} catch (Exception ex) {
			assertEquals(ex.getClass().getName(), JiraTransactionException.class.getName());
		}
	}

	@Test
	public void getAllProjects_passed() throws RequestNotSucceedException {
		// https://cedt-icg-jira.nam.nsroot.net/jira/rest/zapi/latest/util/project-list
		String json = "{\"options\":[{\"value\":\"18102\",\"label\":\"161157 - Global Debt Manager\"}]}";
		when(client.sendGetRequest(any(String.class))).thenReturn(json);
		assertEquals(zapiRestService.getAllProjects().size(), 1);
	}

	@Test
	public void getAllProjects_invalidjson() throws RequestNotSucceedException {
		String json = "xxx";
		when(client.sendGetRequest(any(String.class))).thenReturn(json);
		assertEquals(zapiRestService.getAllProjects().size(), 0);
	}
	
	@Test
	public void getVersionsByPrjId_passed() throws RequestNotSucceedException {
		// https://cedt-icg-jira.nam.nsroot.net/jira/rest/zapi/latest/util/versionBoard-list?projectId=
		String json="{\"unreleasedVersions\": [{\"value\": \"-1\",\"archived\": false,\"label\": \"Unscheduled\"}],\"releasedVersions\": [{\"value\": \"-1\",\"archived\": false,\"label\": \"Unscheduled\"}]}";
		
		when(client.sendGetRequest(any(String.class))).thenReturn(json);
		
		assertEquals(zapiRestService.getVersionsByPrjId("").size(), 2);
		
	}
	
	@Test 
	public void getCyclesByPrjIdAndVerId_passed() throws RequestNotSucceedException{
		String json = "{\"-1\": {\"totalExecutions\": 0," + "\"endDate\": \"\"," + "\"description\": \"\","
				+ "\"totalExecuted\": 0," + "\"started\": \"\"," + "\"expand\": \"executionSummaries\","
				+ "\"projectKey\": \"C167813\"," + "\"versionId\": 28504," + "\"environment\": \"\","
				+ "\"build\": \"\"," + "\"ended\": \"\"," + "\"name\": \"Ad hoc\"," + "\"modifiedBy\": \"\","
				+ "\"projectId\": 15201," + "\"startDate\": \"\","
				+ "\"executionSummaries\": { \"executionSummary\": [] }}," + "\"recordsCount\": 1}";

		when(client.sendGetRequest(any(String.class))).thenReturn(json);
		
		assertEquals(zapiRestService.getCyclesByPrjIdAndVerId("", "").size(), 1);
	}
	
	@Test 
	public void getCyclesByPrjIdAndVerId_passed_invalidJson() throws RequestNotSucceedException{
		String json = "null";

		when(client.sendGetRequest(any(String.class))).thenReturn(json);
		
		assertEquals(zapiRestService.getCyclesByPrjIdAndVerId("", "").size(), 0);
	}
	
	@Test 
	public void getCyclesByPrjIdAndVerId_passed_invalidCycle() throws RequestNotSucceedException{
		String json = "{\"-1\":\"xxx\"," + "\"recordsCount\": 1}";

		when(client.sendGetRequest(any(String.class))).thenReturn(json);
		
		assertEquals(zapiRestService.getCyclesByPrjIdAndVerId("", "").size(), 0);
	}
	
	@Test
	public void getCyclesByVerId_passed() throws RequestNotSucceedException{
		String json = "{\"-1\": {\"totalExecutions\": 0," + "\"endDate\": \"\"," + "\"description\": \"\","
				+ "\"totalExecuted\": 0," + "\"started\": \"\"," + "\"expand\": \"executionSummaries\","
				+ "\"projectKey\": \"C167813\"," + "\"versionId\": 28504," + "\"environment\": \"\","
				+ "\"build\": \"\"," + "\"ended\": \"\"," + "\"name\": \"Ad hoc\"," + "\"modifiedBy\": \"\","
				+ "\"projectId\": 15201," + "\"startDate\": \"\","
				+ "\"executionSummaries\": { \"executionSummary\": [] }}," + "\"recordsCount\": 1}";

		when(client.sendGetRequest(any(String.class))).thenReturn(json);
		
		assertEquals(zapiRestService.getCyclesByVerId( "").size(), 1);
	}
	
	@Test 
	public void getCyclesByVerId_passed_invalidJson() throws RequestNotSucceedException{
		String json = "null";

		when(client.sendGetRequest(any(String.class))).thenReturn(json);
		
		assertEquals(zapiRestService.getCyclesByVerId("").size(), 0);
	}
	
	@Test 
	public void getCyclesByVerId_passed_invalidCycle() throws RequestNotSucceedException{
		String json = "{\"-1\":\"xxx\"," + "\"recordsCount\": 1}";

		when(client.sendGetRequest(any(String.class))).thenReturn(json);
		
		assertEquals(zapiRestService.getCyclesByVerId("").size(), 0);
	}
	
	@Test
	public void getIssueIdByIssueKey_passed() throws RequestNotSucceedException{
		// https://cedt-icg-jira.nam.nsroot.net/jira/rest/api/2/issue/C167813-326
		String json ="{\"id\":\"959620\"}";
		
		when(client.sendGetRequest(any(String.class))).thenReturn(json);
		assertEquals(zapiRestService.getIssueIdByIssueKey(""), "959620");
	}
	
	@Test
	public void getIssueIdByIssueKey_invalidJson_passed() throws RequestNotSucceedException{
		// https://cedt-icg-jira.nam.nsroot.net/jira/rest/api/2/issue/C167813-326
		String json ="null";
		
		when(client.sendGetRequest(any(String.class))).thenReturn(json);
		assertNull(zapiRestService.getIssueIdByIssueKey(""));
	}
	
	@Test
	public void createNewExecution_passed() throws RequestNotSucceedException, KeyManagementException, NoSuchAlgorithmException, UnsupportedOperationException, IOException{
		String json="{\"13377\":{\"id\":13377}}";
		when(client.sendPostRequest(any(String.class), any(String.class))).thenReturn(json);
		
		assertEquals(zapiRestService.createNewExecution("", "", "", "", ""), "13377");
	}
	
	@Test
	public void updateExecutionInfo_passed(){
		String json="";
		try {
			when(client.sendPutRequest(any(String.class), any(String.class))).thenReturn(json);
			zapiRestService.updateExecutionInfo("", "");
		} catch (RequestNotSucceedException e) {
			assertNull(e);
		}		
	}
}
