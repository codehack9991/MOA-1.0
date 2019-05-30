package fast.common.jira;

import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

public class HttpRequest {
	private CertificatedClient client;
	private static final String REQUEST_TYPE_APPLICATION_JSON = "application/json";

	public HttpRequest() {
		client = new CertificatedClient();
	}

	public HttpRequest(String userName, String password) {
		client = new CertificatedClient(userName, password);
	}

	public String sendGetRequest(String url) throws RequestNotSucceedException {

		WebResource resource = client.resource(url);

		ClientResponse response = resource.accept(REQUEST_TYPE_APPLICATION_JSON).get(ClientResponse.class);

		return this.getResultFromResponse(response);
	}

	public String sendPutRequest(String url, String requestBody) throws RequestNotSucceedException {

		WebResource resource = client.resource(url);

		ClientResponse response = resource.accept(REQUEST_TYPE_APPLICATION_JSON).type(REQUEST_TYPE_APPLICATION_JSON)
				.put(ClientResponse.class, requestBody);

		return this.getResultFromResponse(response);
	}

	public String sendPostRequest(String url, String requestBody) throws RequestNotSucceedException {

		WebResource resource = client.resource(url);

		ClientResponse response = resource.accept(REQUEST_TYPE_APPLICATION_JSON).type(REQUEST_TYPE_APPLICATION_JSON)
				.post(ClientResponse.class, requestBody);

		return this.getResultFromResponse(response);
	}

	private String getResultFromResponse(ClientResponse response) throws RequestNotSucceedException {
		if (response == null) {
			return null;
		}
		String result = response.getEntity(String.class);
		if (response.getStatus() != 200) {
			throw new RequestNotSucceedException(
					String.format(RequestNotSucceedException.EXCEPTION_MESSAGE_PATTERN, response.getStatus(), result));
		}

		return result;
	}
}
