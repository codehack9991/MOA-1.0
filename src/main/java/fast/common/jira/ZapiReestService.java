package fast.common.jira;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import fast.common.cipher.AES;
import fast.common.jira.entities.Cycle;
import fast.common.jira.entities.PVItem;
import fast.common.jira.entities.Version;
import fast.common.jira.entities.VersionBoard;

public class ZapiRestService {
	private String url;
	private String userName;
	private String password;

	private Gson gson;
	private static final String ZAPI_URL = "/jira/rest/zapi/latest";
	private static final String JIRA_URL = "/jira/rest/api/2";
	private static final String JIRA_ISSUE_URL = "/issue";
	private static final String ZAPI_CYCLE_URL = "/cycle";
	private static final String ZAPI_TESTCASE_URL = "/test";
	private static final String ZAPI_UTIL_URL = "/util";
	private static final String ZAPI_EXECUTION_URL = "/execution";
	private static final String ZAPI_ATTACHMENT_URL = "/attachment";

	private static final String RESPONSE_JSON_TAG_RECORDSCOUNT = "recordsCount";

	private static final String REQUEST_TYPE_APPLICATION_JSON = "application/json";

	private static Logger logger = LogManager.getLogger(ZapiRestService.class);

	private HttpRequest client;

	public HttpRequest getClient() {
		return client;
	}

	public void setClient(HttpRequest client) {
		this.client = client;
	}

	public ZapiRestService(String url, String userName, String password) {
		this.url = url;
		this.userName = userName;
		this.password = password;

		GsonBuilder gsonBuilder = new GsonBuilder();
		gsonBuilder.registerTypeAdapter(Date.class, new JsonDeserializer<Date>() {
			DateFormat df = new SimpleDateFormat("dd/MMM/yy");

			@Override
			public Date deserialize(final JsonElement json, final Type tyoeOfT,
					final JsonDeserializationContext context) {
				try {
					String jsonStr = json.getAsString();
					if (jsonStr == null || jsonStr.trim().isEmpty()) {
						return null;
					}
					return df.parse(jsonStr);
				} catch (Exception e) {
					logger.error("Failed to parse date string " + json.getAsString(), e);
					return null;
				}
			}
		});
		gson = gsonBuilder.create();
	}
	
	public static ZapiRestService generateService(String url, String userName, String password){
		ZapiRestService service = new ZapiRestService(url, userName, password);
		service.connect();
		return service;
	}

	public void connect() {
		client = new HttpRequest(userName, password);
	}

	public List<Version> getVersionsByProjectId(String projectId) {
		List<Version> result = new ArrayList<>();

		try {
			String json = client.sendGetRequest(url + ZAPI_URL + "/util/versionBoard-list?projectId=" + projectId);
			VersionBoard board = gson.fromJson(json, VersionBoard.class);

			result.addAll(board.getUnreleasedVersions());
			result.addAll(board.getReleasedVersions());
		} catch (RequestNotSucceedException ex) {
			logger.error(String.format("failed to get version by projectId %s.%s", projectId, ex.getMessage()));
		}
		return result;
	}

	public Map<String, Cycle> getCyclesByProjectAndVersion(String projectId, String versionId) {
		HashMap<String, Cycle> result = new HashMap<>();

		try {
			String json = client
					.sendGetRequest(url + ZAPI_URL + "/cycle?projectId=" + projectId + "&versionId=" + versionId);
			JsonParser parser = new JsonParser();
			JsonElement element = parser.parse(json);
			if (element.isJsonObject()) {
				JsonObject cycleList = element.getAsJsonObject();
				cycleList.remove(RESPONSE_JSON_TAG_RECORDSCOUNT);
				cycleList.entrySet().forEach(e -> {
					String key = e.getKey();
					try {
						Cycle cycle = gson.fromJson(e.getValue().toString(), Cycle.class);
						result.put(key, cycle);
					} catch (Exception ex) {
						logger.error(ex.getMessage());
					}
				});
			}
		} catch (RequestNotSucceedException ex) {
			logger.error("failed to get cycles by projectId and versionId." + ex.getMessage());
		}

		return result;
	}

	public String createNewCycle(String prjId, String verId, String cycName)
			throws RequestNotSucceedException, JiraTransactionException {
		String requrl = this.url + ZAPI_URL + ZAPI_CYCLE_URL;
		String requestBody = "{"
				+ String.format("\"name\":\"%s\",\"projectId\":%s,\"versionId\":\"%s\"", cycName, prjId, verId) + "}";

		String resultJson = client.sendPostRequest(requrl, requestBody);

		JsonParser jp = new JsonParser();
		JsonElement element = jp.parse(resultJson);
		if (element.isJsonObject()) {
			JsonObject newCycle = element.getAsJsonObject();
			if (newCycle.has("id")) {
				return newCycle.get("id").getAsString();
			}
		}
		throw new JiraTransactionException(resultJson);
	}

//	public Cycle getCycleById(String cycleId) {
//
//		try {
//			String json = client.sendGetRequest(url + ZAPI_URL + "/cycle/" + cycleId);
//			return gson.fromJson(json, Cycle.class);
//		} catch (Exception ex) {
//			logger.error(String.format("failed to get cycle by id %s. %s", cycleId, ex.getMessage()));
//			return null;
//		}
//	}

	public List<PVItem> getAllProjects() throws RequestNotSucceedException {
		String requrl = this.url + ZAPI_URL + ZAPI_UTIL_URL + "/project-list";
		String repStr = client.sendGetRequest(requrl);

		List<PVItem> result = new ArrayList<>();

		JsonParser parser = new JsonParser();
		JsonElement element = parser.parse(repStr);

		if (element.isJsonObject()) {
			JsonObject jsonObj = null;
			jsonObj = element.getAsJsonObject();
			JsonArray prjsList = (JsonArray) jsonObj.get("options");

			prjsList.forEach(e -> {
				JsonObject entry = (JsonObject) e;
				result.add(gson.fromJson(entry.toString(), PVItem.class));
			});

		}
		return result;
	}

	public List<PVItem> getVersionsByPrjId(String prjid) throws RequestNotSucceedException {
		String requrl = this.url + ZAPI_URL + ZAPI_UTIL_URL + "/versionBoard-list?projectId=" + prjid;
		String repStr = client.sendGetRequest(requrl);

		JsonParser parser = new JsonParser();
		JsonElement ele = parser.parse(repStr);

		List<PVItem> result = new ArrayList<>();

		if (ele.isJsonObject()) {
			JsonObject jo = null;
			jo = ele.getAsJsonObject();
			JsonArray unreleased = (JsonArray) jo.get("unreleasedVersions");
			JsonArray released = (JsonArray) jo.get("releasedVersions");

			unreleased.forEach(x -> {
				JsonObject entry = (JsonObject) x;
				result.add(gson.fromJson(entry.toString(), PVItem.class));
			});
			released.forEach(x -> {
				JsonObject entry = (JsonObject) x;
				result.add(gson.fromJson(entry.toString(), PVItem.class));
			});
		}

		return result;
	}

	public List<Cycle> getCyclesByPrjIdAndVerId(String prjid, String verid) throws RequestNotSucceedException {
		String requrl = this.url + ZAPI_URL + ZAPI_CYCLE_URL + "?projectId=" + prjid + "&versionId=" + verid;
		String json = client.sendGetRequest(requrl);
		JsonParser parser = new JsonParser();
		JsonElement element = parser.parse(json);

		List<Cycle> result = new ArrayList<>();

		if (element.isJsonNull()) {
			return result;
		} else if (element.isJsonObject()) {
			JsonObject cycleList = element.getAsJsonObject();
			cycleList.remove(RESPONSE_JSON_TAG_RECORDSCOUNT);
			cycleList.entrySet().forEach(e -> {
				String key = e.getKey();
				try {
					Cycle cycle = gson.fromJson(e.getValue().toString(), Cycle.class);
					cycle.setId(key);
					result.add(cycle);
				} catch (Exception ex) {
					logger.error(ex.getMessage());
				}
			});
		}
		return result;
	}

	public List<Cycle> getCyclesByVerId(String verid) throws RequestNotSucceedException {
		String requrl = this.url + ZAPI_URL + ZAPI_CYCLE_URL + "?versionId=" + verid;
		String json = client.sendGetRequest(requrl);
		JsonParser parser = new JsonParser();
		JsonElement element = parser.parse(json);

		List<Cycle> result = new ArrayList<>();

		if (element.isJsonNull()) {
			return result;
		} else if (element.isJsonObject()) {
			JsonObject cycleList = element.getAsJsonObject();
			cycleList.remove(RESPONSE_JSON_TAG_RECORDSCOUNT);
			cycleList.entrySet().forEach(e -> {
				String key = e.getKey();
				try {
					Cycle cycle = gson.fromJson(e.getValue().toString(), Cycle.class);
					cycle.setId(key);
					result.add(cycle);
				} catch (Exception ex) {
					logger.error(ex.getMessage());
				}
			});
		}
		return result;
	}

	public String getIssueIdByIssueKey(String issueKey) throws RequestNotSucceedException {
		String requrl = this.url + JIRA_URL + JIRA_ISSUE_URL + "/" + issueKey;

		String json = client.sendGetRequest(requrl);

		JsonParser parser = new JsonParser();
		JsonElement element = parser.parse(json);

		String result = null;

		if (element.isJsonObject()) {
			JsonObject jsonObject = element.getAsJsonObject();

			result = jsonObject.get("id").getAsString();
		}
		return result;
	}

	public String createNewExecution(String prjid, String verid, String cycid, String issueid, String assingee)
			throws KeyManagementException, NoSuchAlgorithmException, UnsupportedOperationException,
			RequestNotSucceedException, IOException {
		String reqBody = String.format(
				"{\"cycleId\":\"%s\",\"issueId\":\"%s\",\"projectId\":\"%s\",\"versionId\":\"%s\",\"assigneeType\":\"assignee\",\"assignee\":\"%s\",\"folderId\":null}",
				cycid, issueid, prjid, verid, assingee);
		String requrl = this.url + ZAPI_URL + ZAPI_EXECUTION_URL;

		String reqstr = client.sendPostRequest(requrl, reqBody);
		String result = null;

		@SuppressWarnings("unchecked")
		Map<String, Object> map = gson.fromJson(reqstr, Map.class);
		result = map.keySet().toArray()[0].toString();

		return result;
	}

	public String updateExecutionInfo(String executionId, String status) throws RequestNotSucceedException {
		String reqBody = "{\"status\":\"" + status + "\"}";
		String requrl = this.url + ZAPI_URL + ZAPI_EXECUTION_URL + "/" + executionId + "/execute";

		return client.sendPutRequest(requrl, reqBody);
	}

	public String uploadAttachementToExecution(String executionId, List<String> files)
			throws KeyManagementException, NoSuchAlgorithmException, RequestNotSucceedException, IOException {
		if (files == null || files.isEmpty()) {
			return "no file";
		}

		String requrl = this.url + ZAPI_URL + ZAPI_ATTACHMENT_URL + "?entityId=" + executionId
				+ "&entityType=execution";

		return this.postFileViaHttpClient(requrl, files);
	}

	private String postViaHttpClient(String url, String requestBody) throws RequestNotSucceedException,
			NoSuchAlgorithmException, UnsupportedOperationException, IOException, KeyManagementException {
		String auth = this.userName + ":"
				+ (AES.isCipherFormat(this.password) ? AES.decode(this.password) : this.password);
		String encodedAuth = java.util.Base64.getEncoder().encodeToString(auth.getBytes(Charset.forName("UTF-8")));
		String authHeader = "Basic " + encodedAuth;

		SSLContext sc = SSLContext.getInstance("TLS");

		sc.init(null, getAllTrustedCerts(), new SecureRandom());

		HostnameVerifier allowAllHosts = new NoopHostnameVerifier();

		SSLConnectionSocketFactory connectionFactory = new SSLConnectionSocketFactory(sc, allowAllHosts);

		CloseableHttpClient httpclient = HttpClients.custom().setSSLSocketFactory(connectionFactory).build();
		HttpPost httpPost = new HttpPost(url);
		httpPost.setHeader(HttpHeaders.AUTHORIZATION, authHeader);
		httpPost.setHeader("content-type", REQUEST_TYPE_APPLICATION_JSON);

		StringEntity stringEntity = new StringEntity(requestBody);
		httpPost.setEntity(stringEntity);

		CloseableHttpResponse response = httpclient.execute(httpPost);
		int statusCode = response.getStatusLine().getStatusCode();

		String result = inputStreamToString(response.getEntity().getContent());
		if (statusCode != 200) {
			throw new RequestNotSucceedException("StatusCode:" + statusCode + ".ErrorMessage:" + result);
		}

		return result;
	}

	private String postFileViaHttpClient(String url, List<String> files)
			throws RequestNotSucceedException, NoSuchAlgorithmException, KeyManagementException, IOException {
		String auth = this.userName + ":"
				+ (AES.isCipherFormat(this.password) ? AES.decode(this.password) : this.password);
		String encodedAuth = java.util.Base64.getEncoder().encodeToString(auth.getBytes(Charset.forName("UTF-8")));
		String authHeader = "Basic " + encodedAuth;

		SSLContext sc = SSLContext.getInstance("TLS");

		sc.init(null, getAllTrustedCerts(), new SecureRandom());

		HostnameVerifier allowAllHosts = new NoopHostnameVerifier();

		SSLConnectionSocketFactory connectionFactory = new SSLConnectionSocketFactory(sc, allowAllHosts);

		CloseableHttpClient httpclient = HttpClients.custom().setSSLSocketFactory(connectionFactory).build();
		HttpPost httpPost = new HttpPost(url);
		httpPost.setHeader(HttpHeaders.AUTHORIZATION, authHeader);

		MultipartEntityBuilder builder = MultipartEntityBuilder.create();

		builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);

		files.forEach(x -> {
			File file = new File(x);

			builder.addBinaryBody("file", file, ContentType.DEFAULT_TEXT, file.getName());

		});

		HttpEntity entity = builder.build();
		httpPost.setEntity(entity);

		CloseableHttpResponse response = httpclient.execute(httpPost);
		int statusCode = response.getStatusLine().getStatusCode();
		String result = inputStreamToString(response.getEntity().getContent());
		if (statusCode != 200) {
			throw new RequestNotSucceedException(
					String.format(RequestNotSucceedException.EXCEPTION_MESSAGE_PATTERN, statusCode, result));
		}

		return result;
	}

	private TrustManager[] getAllTrustedCerts() {
		return new TrustManager[] { new X509TrustManager() {
			public X509Certificate[] getAcceptedIssuers() {
				return new X509Certificate[0];
			}

			public void checkClientTrusted(X509Certificate[] certs, String authType) {
				//
			}

			public void checkServerTrusted(X509Certificate[] certs, String authType) {
				//
			}
		} };
	}

	private String inputStreamToString(InputStream is) throws IOException {

		String line = "";
		StringBuilder total = new StringBuilder();

		// Wrap a BufferedReader around the InputStream
		BufferedReader rd = new BufferedReader(new InputStreamReader(is));

		try {
			// Read response until the end
			while ((line = rd.readLine()) != null) {
				total.append(line);
			}
		} catch (IOException e) {
			logger.error(e.getMessage());
			throw e;
		}

		// Return full string
		return total.toString();
	}
}
