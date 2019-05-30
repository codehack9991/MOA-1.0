package fast.common.context;

import static io.restassured.path.json.JsonPath.from;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


import fast.common.logging.FastLogger;
import io.restassured.response.Response;
import io.restassured.response.ResponseBody;
import io.restassured.http.Header;

public class WebApiResult extends StepResult {
	
	private int statusCode=0;
	private String statusLine="default";

	private String contentType="default";
	HashMap<String, String> headerMap = new HashMap<String, String>();
	private String responseBodyContent="default";
	private ResponseBody responseBody;
	private Map<String, String> cookies;
	private long responseTime=0;
	
	static FastLogger logger = FastLogger.getLogger("WebApiResult");

	enum DataType {
    	isString, isInt, isBoolean, isByte, isChar, isDouble, isFloat, isLong, isShort; 
    }
	
	public WebApiResult() {

	}

	public WebApiResult(int statusCode, String responseBodyContent) {
		this.statusCode = statusCode;
		this.responseBodyContent = responseBodyContent;
	}

	public WebApiResult(Response response) {
		
		//Extracting Status Line
		this.statusCode = response.getStatusCode();
		this.statusLine = response.getStatusLine();

		//Extracting Headers
		this.contentType = response.getContentType();
		
		//Extracting headers
     	List<Header> headers = response.headers().asList();
     	for(Header header : headers) {
			this.headerMap.put(header.getName(), header.getValue());				

     	}
		
		//Extract Time
		this.responseTime = response.getTime();
		
		//Extract Cookies
		this.cookies = response.getCookies();

		//Extract Body
		this.responseBody = response.getBody();
		this.responseBodyContent = response.asString();

	}

	/**
	 * get cookie value by key
	 * 
	 * @param key
	 *            relate to cookie value
	 * @return cookie value for key
	 */
	public String getCookieValue(String key) {
		if (cookies.containsKey(key)) {
			return cookies.get(key);
		}
		return null;
	}

	/**
	 * get response status code
	 * 
	 * @return response statusCode
	 */
	public int getStatusCode() {
		return statusCode;
	}

	/**
	 * get response body json as string
	 * 
	 * @return response body string
	 */
	public String getJsonResult() {
		if (responseBodyContent == null && responseBody != null) {
			responseBodyContent = responseBody.asString();
		}
		return responseBodyContent;
	}

	/**
	 * get json Object from jsonArray
	 * 
	 * @param index
	 *            of json Obeject array
	 * @return json Object
	 */
	public Object getArrayItem(int index) {
		String bodyStr;
		if (responseBody == null) {
			bodyStr = responseBodyContent;
		} else {
			bodyStr = responseBody.asString();
		}
		List<Object> items = from(bodyStr).getList("$");
		return items.get(index);
	}

	/**
	 * get field value as object from json string.
	 * 
	 * @param jsonString
	 *            source string
	 * @param path
	 *            target json object string
	 * @return json Object
	 */
	public Object getFieldValueFromJsonString(String jsonString, String path) {
		return from(jsonString).get(path);
	}

	@Override
	public String toString() {
		return this.getJsonResult();
	}

	@Override
	public String getFieldValue(String field) throws Throwable {
		if (this.responseBody != null) {
			return this.responseBody.path(field).toString();
		} else if (this.responseBodyContent != null && !this.responseBodyContent.isEmpty()) {
			return from(responseBodyContent).get(field).toString();
		} else {
			return null;
		}
	}

	/**
	 * get Map object field value from string response
	 * 
	 * @param jsonPath the target json path wanted to query
	 * @return Map object field value
	 */
	public Map<?, ?> getFieldValueAsMapFromStringResponse(String jsonPath) {
		if (this.responseBody != null) {
			Map<?, ?> jsonMap = this.responseBody.jsonPath().getMap(jsonPath);
			return jsonMap;
		} else {
			logger.info("responseBody is null.");
			return null;
		}
	}
	/**
	 * get List object field value from string response
	 * 
	 * @param jsonPath the target json path wanted to query
	 * @return List object field value
	 */
	public List<?> getFieldValueAsListFromStringResponse(String jsonPath) {
		if (this.responseBody != null) {
			List<?> jsonList = this.responseBody.jsonPath().getList(jsonPath);
			return jsonList;
		} else {
			logger.info("responseBody is null.");
			return null;
		}
	}
	/**
	 * get String object field value from string response
	 * 
	 * @param jsonPath the target json path wanted to query
	 * @return String object field value
	 */
	public String getFieldValueAsStringFromStringResponse(String jsonPath) {
		if (this.responseBody != null) {
			String jsonString = this.responseBody.jsonPath().getString(jsonPath);
			return jsonString;
		} else {
			logger.info("responseBody is null.");
			return null;
		}
	}

	/**
	 * get Map object field value from JsonObject response
	 * 
	 * @param jsonPath the target json path wanted to query
	 * @return Map object field value
	 */
	public Map<?, ?> getFieldValueAsMapFromJsonObjectResponse(String jsonPath) {
		if (this.responseBody != null) {
			Map<?, ?> jsonMap = this.responseBody.jsonPath().getMap(jsonPath);
			return jsonMap;
		} else {
			logger.info("responseBody is null.");
			return null;
		}
	}
	/**
	 * get List object field value from JsonObject response
	 * 
	 * @param jsonPath the target json path wanted to query
	 * @return List object field value
	 */
	public List<?> getFieldValueAsListFromJsonObjectResponse(String jsonPath) {
		if (this.responseBody != null) {
			List<?> jsonList = this.responseBody.jsonPath().getList(jsonPath);
			return jsonList;
		} else {
			logger.info("responseBody is null.");
			return null;
		}
	}
	/**
	 * get String object field value from JsonObject response
	 * 
	 * @param jsonPath the target json path wanted to query
	 * @return String object field value
	 */
	public String getFieldValueAsStringFromJsonObjectResponse(String jsonPath) {
		if (this.responseBody != null) {
			String jsonString = this.responseBody.jsonPath().getString(jsonPath);
			return jsonString;
		} else {
			logger.info("responseBody is null.");
			return null;
		}
	}

	/**
	 * get Map object field value from JsonArray response
	 * 
	 * @param jsonPath the target json path wanted to query
	 * @param targetMapValue the target Map value you wanted to get
	 * @return Map object field value
	 */
	public Map<?, ?> getFieldValueAsMapFromJsonArrayResponse(String jsonPath, String targetMapValue) {
		if (this.responseBody != null) {
			List<?> resultLists = getFieldValueAsListFromJsonArrayResponse(jsonPath);
			Map<?, ?> jsonTargetMap = null;
			for (int i = 0; i < resultLists.size(); i++) {
				if (resultLists.get(i) instanceof Map) {
					if (((Map<?, ?>) resultLists.get(i)).containsValue(targetMapValue)) {
						jsonTargetMap = (Map<?, ?>) resultLists.get(i);
						logger.info("Get the map result from response successfully!");
					}
				} else if (resultLists.get(i) instanceof String) {
					logger.info("The " + "NO." + i + " result is not a Map but a String: " + resultLists);
				} else if (resultLists.get(i) instanceof List) {
					logger.info(
							"The " + "NO." + i + " result is not a Map but a List: " + resultLists.get(i).toString());
				}
			}
			return jsonTargetMap;
		} else {
			logger.info("responseBody is null.");
			return null;
		}
	}
	/**
	 * get List object field value from JsonArray response
	 * 
	 * @param jsonPath the target json path wanted to query
	 * @return List object field value
	 */
	public List<?> getFieldValueAsListFromJsonArrayResponse(String jsonPath) {
		if (this.responseBody != null) {
			List<?> jsonList = this.responseBody.jsonPath().getList(jsonPath);
			return jsonList;
		} else {
			logger.info("responseBody is null.");
			return null;
		}
	}
	/**
	 * get String object field value from JsonArray response
	 *
	 * @param jsonPath the target json path wanted to query
	 * @return String object field value
	 */
	public String getFieldValueAsStringFromJsonArrayResponse(String jsonPath) {
		if (this.responseBody != null) {
			String jsonString = this.responseBody.jsonPath().getString(jsonPath);
			return jsonString;
		} else {
			logger.info("responseBody is null.");
			return null;
		}
	}

	/**
	 * get specific data from response.
	 * 
	 * @param jsonPath the target json path wanted to query
	 * @param dataType the Type value wanted to get
	 * @return query result such as String, Int, Boolean and so on
	 */
	public Object getSpecificDataFromResponse(String jsonPath, DataType dataType) {
        Object result = null;
        switch(dataType) {
        default:
        case isString: 
        	result = this.responseBody.jsonPath().getString(jsonPath);
        	break;
        case isInt:
        	result = this.responseBody.jsonPath().getInt(jsonPath);
        	break;
        case isBoolean:
        	result = this.responseBody.jsonPath().getBoolean(jsonPath);
        	break;
        case isByte:
        	result = this.responseBody.jsonPath().getByte(jsonPath);
        	break;
        case isChar:
        	result = this.responseBody.jsonPath().getChar(jsonPath);
        	break;
        case isDouble:
        	result = this.responseBody.jsonPath().getDouble(jsonPath);
        	break;
        case isFloat:
        	result = this.responseBody.jsonPath().getFloat(jsonPath);
        	break;
        case isLong:
        	result = this.responseBody.jsonPath().getLong(jsonPath);
        	break;
        case isShort:
        	result = this.responseBody.jsonPath().getShort(jsonPath);
        	break;
//        default :
//        	result = this.responseBody.jsonPath().getString(jsonPath);
        }                
        return result;
    }
	
	public String getStatusLine() {
		return statusLine;
	}

	public String getContentType() {
		return contentType;
	}

	public HashMap<String, String> getHeaderMap() {
		return headerMap;
	}

	public String getResponseBodyContent() {
		return responseBodyContent;
	}

	public ResponseBody getResponseBody() {
		return responseBody;
	}

	public Map<String, String> getCookies() {
		return cookies;
	}

	public long getResponseTime() {
		return responseTime;
	}
	
	/**
	 * get Map object field value from string response
	 * 
	 * @param xmlPath the target xml path wanted to query
	 * @return Map object field value
	 */
	public Map<?, ?> getFieldValueAsMapFromXmlResponse(String xmlPath) {
		if (this.responseBody != null) {
			Map<?, ?> xmlMap = this.responseBody.xmlPath().getMap(xmlPath);
			return xmlMap;
		} else {
			logger.info("responseBody is null.");
			return null;
		}
	}
	/**
	 * get List object field value from string response
	 * 
	 * @param xmlPath the target xml path wanted to query
	 * @return List object field value
	 */
	public List<?> getFieldValueAsListFromXmlResponse(String xmlPath) {
		if (this.responseBody != null) {
			List<?> xmlList = this.responseBody.xmlPath().getList(xmlPath);
			return xmlList;
		} else {
			logger.info("responseBody is null.");
			return null;
		}
	}
	/**
	 * get String object field value from string response
	 * 
	 * @param xmlPath the target xml path wanted to query
	 * @return String object field value
	 */
	public String getFieldValueAsStringFromXmlResponse(String xmlPath) {
		if (this.responseBody != null) {
			String xmlString = this.responseBody.xmlPath().getString(xmlPath);
			return xmlString;
		} else {
			logger.info("responseBody is null.");
			return null;
		}
	}
	/**
	 * get specific data from response.
	 * 
	 * @param xmlPath the target xml path wanted to query
	 * @param dataType the Type value wanted to get
	 * @return query result such as String, Int, Boolean and so on
	 */
	public Object getSpecificDataFromXmlResponse(String xmlPath, DataType dataType) {
        Object result = null;
        switch(dataType) {
        default:
        case isString: 
        	result = this.responseBody.xmlPath().getString(xmlPath);
        	break;
        case isInt:
        	result = this.responseBody.xmlPath().getInt(xmlPath);
        	break;
        case isBoolean:
        	result = this.responseBody.xmlPath().getBoolean(xmlPath);
        	break;
        case isByte:
        	result = this.responseBody.xmlPath().getByte(xmlPath);
        	break;
        case isChar:
        	result = this.responseBody.xmlPath().getChar(xmlPath);
        	break;
        case isDouble:
        	result = this.responseBody.xmlPath().getDouble(xmlPath);
        	break;
        case isFloat:
        	result = this.responseBody.xmlPath().getFloat(xmlPath);
        	break;
        case isLong:
        	result = this.responseBody.xmlPath().getLong(xmlPath);
        	break;
        case isShort:
        	result = this.responseBody.xmlPath().getShort(xmlPath);
        	break;
        }                
        return result;
    }
	

	@Override
	public ArrayList<String> getFieldsValues(String field) {
		return null;
	}
}
