package fast.common.agents;

import static io.restassured.RestAssured.given;
import static io.restassured.RestAssured.preemptive;

import java.util.Map;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;

import fast.common.context.StepResult;
import fast.common.context.StepResult.Status;
import fast.common.core.Configurator;
import fast.common.context.WebApiResult;
import fast.common.logging.FastLogger;
import io.restassured.RestAssured;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

/**
 * The {@code WebApiAgent} class defines various basic http requests to interact
 * with web server using web api.
 * 
 * 
 * <p>
 * The basic requests: GET, POST, PUT, DELETE
 * </p>
 * 
 * <p>
 * Details information for using an WebApiAgent can see:
 * <p>
 
 */
public class WebApiAgent extends Agent{

	private RequestSpecification uncompletedRequest;
	
	/**
	 * Four request types:
	 * 
	 * GET, POST, PUT, DELETE
	 *
	 */
	public enum RequestType {
		GET, POST, PUT, DELETE
	}

	private FastLogger logger;
	private static final String APPLICATION_JSON = "application/json";
	
	/**
	 * Constructs a new <tt>WebApiAgent</tt> with
     * default configuration file (config.yml) and custom configuration files to
     * fetch required parameters.
     *
     * @param   name a string for naming the creating WebApiAgent 
     * @param   agentParams a map to get the required parameters for creating a WebApiAgent 
     * @param   configurator a Configurator instance to provide configuration info for the actions of the WebApiAgent
     * 
     * @since 1.7
	 * 
	 */
	public WebApiAgent(String name, Map<?,?> agentParams, Configurator configurator) {
		super(name, agentParams, configurator);
		logger = FastLogger.getLogger(String.format("%s:WebApiAgent", _name));
		
	}
	
	/**
	 * Constructs a new <tt>WebApiAgent</tt> with no parameter. Display a
	 * specified log information.
	 * 
	 * @since 1.5
	 * 
	 */
	public WebApiAgent() {
		super();
		logger = FastLogger.getLogger("WebApiAgent");
		logger.info("Initializing Web Api Agent...");
		RestAssured.replaceFiltersWith(ResponseLoggingFilter.responseLogger(), new RequestLoggingFilter());
		
	}

	/**
	 * Constructs a new <tt>WebApiAgent</tt> with uri. 
	 * 
	 * @param uri address used to send request
	 * 
	 * @since 1.7
	 */
	public WebApiAgent(String uri) {
		this();
		RestAssured.baseURI = uri;

	}
	
	/**
	 * Constructs a new <tt>WebApiAgent</tt> with username and password. 
	 * 
	 * @param username used to verify
	 * @param password used to verify
	 * 
	 * @since 1.7
	 */
	public WebApiAgent(String username, String password) {
		this();
		RestAssured.authentication = preemptive().basic(username, password);
		this.useRelaxedHTTPSValidation();
	}
	
	/**
	 * Constructs a new <tt>WebApiAgent</tt> with uri, username and password. 
	 * 
	 * @param uri address used to send request
	 * @param username used to verify
	 * @param password used to verify
	 * 
	 * @since 1.7
	 */
	public WebApiAgent(String uri, String username, String password) {
		this(username, password);
		RestAssured.baseURI = uri;
	}
	
	public void useRelaxedHTTPSValidation(){
		RestAssured.useRelaxedHTTPSValidation();
	}
	
	/**
	 * Create a WebApiAgent building the request part.
	 * 
	 * @return a request specification.
	 * @since 1.7
	 */
	public WebApiAgent generate() {
		if (uncompletedRequest == null) {
			uncompletedRequest = given();
		}
		return this;
	}

	/**
	 * Set content type to a send request.
	 * 
	 * @param contentType define request type
	 * @return a request specification with content type
	 * @since 1.7
	 */
	public WebApiAgent setContentType(String contentType) {
		generate();
		uncompletedRequest = uncompletedRequest.contentType(contentType);
		return this;
	}
	
	/**
	 * Set additional/custom headers to send in the request.
	 * 
	 * @param headers, define the additional/custom headers user want to send, apart from default headers.
	 * @return  a request specification with headers
	 * @since 1.7
	 */
	public WebApiAgent setHeaders(Map<String, String> headers) {
		generate();
		uncompletedRequest = uncompletedRequest.headers(headers);
		return this;
	}
	
	/**
	 * Set path params to a send request uri.
	 * 
	 * @param pathParams define request uri path params
	 * @return a request specification with pathParams
	 * @since 1.7
	 */
	public WebApiAgent setPathParams(Map<String, Object> pathParams) {
		generate();
		uncompletedRequest = uncompletedRequest.pathParams(pathParams);
		return this;
	}
	
	/**
	 * Set body to a send request.
	 * 
	 * @param body define request body
	 * @return a request specification with body
	 * @since 1.7
	 */
	public WebApiAgent setBody(Object body) {
		generate();
		uncompletedRequest = uncompletedRequest.body(body);
		return this;
	}
	
	/**
	 * Set params type to a send request.
	 * 
	 * @param params define request params
	 * @return a request specification with params
	 * @since 1.7
	 */
	public WebApiAgent setParams(Map<String, Object> params) {
		generate();
		uncompletedRequest = uncompletedRequest.params(params);
		return this;
	}

	/**
	 * Send request and get response to store in StepResult.
	 * 
	 * @param type including "GET POST PUT DELETE"
	 * @param uri define request address
	 * @return result store response into stepResult
	 * @since 1.7 
	 * 
	 * @see fast.common.glue.CommonStepDefs#sendRequest(String, String, String, String)
	 */
	public StepResult sendRequest(RequestType type, String uri) {
		generate();
		WebApiResult result = null;
		try {
			Response response = null;
			switch (type) {
			case GET:
				response = uncompletedRequest.get(uri);
				break;
			case POST:
				response = uncompletedRequest.post(uri);
				break;
			case PUT:
				response = uncompletedRequest.put(uri);
				break;
			case DELETE:
				response = uncompletedRequest.delete(uri);
				break;
			}			
			result=new WebApiResult(response);
			result.setStatus(Status.Passed);
		} catch (Exception ex) {
			result=new WebApiResult();
			result.setStatus(Status.Failed);
			result.setFailedMessage(ex.getMessage());
		}finally{
			uncompletedRequest = null;
		}
		return result;
	}

	/**
	 * Send rest request and get response to store in StepResult.
	 * 
	 * @param type including "GET POST PUT DELETE"
	 * @param uri define request address
	 * @param pathParams define uri address path params
	 * @param params define request params
	 * @param body define request body
	 * @return result store response into stepResult
	 * @since 1.7 
	 * 
	 * @see fast.common.glue.CommonStepDefs#sendRestRequest(String, String, String, String, String, String, String)
	 */
	public StepResult sendRestRequest(RequestType type, String uri, Map<String, Object> pathParams,
			Map<String, Object> params, Map<String, Object> body) {

		this.generate().setContentType(APPLICATION_JSON);
		if (pathParams != null)
			this.setPathParams(pathParams);
		if (params != null)
			this.setParams(params);
		if (body != null)
			this.setBody(body);

		return this.sendRequest(type, uri);
	}

	/**
	 * 
	 * Sends http request to get responses from web server.
	 * 
	 * @param requestType
	 *            four types: GET, POST, PUT, DELETE
	 * @param uri
	 *            the address of web server to send request
	 * @param jsonObject
	 *            the request body provides with fields and values
	 * @return the WebApiResult stores contains status code and response.
	 *         <p>
	 *         Example: <blockquote>
	 * 
	 *         <pre>
	 *         String uri = "http://rest.byo.sd-ed06-5eaa.nam.nsroot.net/project?name=FAST";
	 *         </pre>
	 * 
	 *         </blockquote> <blockquote>
	 * 
	 *         <pre>
	 *         WebApiResult response = new WebApiAgent().sendRestRequest(RequestType.GET, uri, null);
	 *         </pre>
	 * 
	 *         </blockquote>
	 * @since 1.5
	 * @deprecated  As of release 1.7, replaced by {@link fast.commom.agents.WebApiAgent#sendRestRequest(RequestType, String, Map<String, Object>,Map<String, Object>, Map<String, Object>)}
	 */
	@Deprecated
	public WebApiResult sendRestRequest(RequestType requestType, String uri, JSONObject jsonObject) {
		logger.info(String.format("Request URI: %s. Rquest Type: %s", uri, requestType));
		Map<String, Object> processedJsonObject = null;
		if(jsonObject != null){
			processedJsonObject = JSONObject.parseObject(jsonObject.toJSONString(), new TypeReference<Map<String, Object>>(){});
		}
		return (WebApiResult) this.sendRestRequest(requestType,uri,null,null,processedJsonObject);
	}

	@Override
	public void close() throws Exception {
		logger.info("WebApi Agent closes");
	}
	
}
