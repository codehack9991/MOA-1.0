package fast.common.context;

import static org.mockito.Mockito.when;
import static org.mockito.Mockito.mock;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import com.google.gson.Gson;
import fast.common.context.WebApiResult.DataType;
import io.restassured.http.Header;
import io.restassured.http.Headers;
import io.restassured.path.json.JsonPath;
import io.restassured.path.xml.XmlPath;
import io.restassured.response.Response;
import io.restassured.response.ResponseBody;

@RunWith(MockitoJUnitRunner.class)
public class TestWebApiResult {
	private JsonPath jsonPath = null;
	private XmlPath xmlPath = null;
	private Response response = null;
	private WebApiResult webApiResult = null;
	@SuppressWarnings("rawtypes")
	private ResponseBody responseBody = null;
	private Headers header = null;
	
	@Before
	public void setup(){
		jsonPath = mock(JsonPath.class);
		xmlPath = mock(XmlPath.class);
		response = mock(Response.class);
		responseBody = mock(ResponseBody.class);
		header = mock(Headers.class);
		
		List<Header> headers = new ArrayList<Header>();
		headers.add(new Header("Content-length","17"));
		headers.add(new Header("Date","2018-21-11 11:40:00 IST"));
		headers.add(new Header("content-type", "application/json"));
		
		Map<String,String> cookies = new HashMap<>();
		cookies.put("s_fid", "0FFB406D1D1464D6-3569F2FE5427595E");
		
		when(response.getStatusCode()).thenReturn(200);
		when(response.getStatusLine()).thenReturn("HTTP/1.1 200 OK");
		when(response.getContentType()).thenReturn("application/json; charset=utf-8");
		when(response.getCookies()).thenReturn(cookies);
		when(response.headers()).thenReturn(header);
		when(header.asList()).thenReturn(headers);
		when(response.getTime()).thenReturn((long) 4081988);
		when(response.asString()).thenReturn("response as string");
		when(response.getBody()).thenReturn(responseBody);
		when(responseBody.jsonPath()).thenReturn(jsonPath);
		webApiResult = new WebApiResult(response);
	}
	
	@Test
	public void getFieldValueAsString_FromStringResponse(){
		when(jsonPath.getString("invalidJsonPath")).thenReturn("str");
		assertEquals("str", (webApiResult.getFieldValueAsStringFromStringResponse("invalidJsonPath")));
	}
	
	@Test
	public void getFieldValueAsString_FromStringResponse_nullResponseBody(){
		when(response.getBody()).thenReturn(null);
		webApiResult = new WebApiResult(response);
		assertNull(webApiResult.getFieldValueAsStringFromStringResponse("invalidJsonPath"));
	}
	
	@Test
	public void getFieldValueAsList_FromStringResponse(){
		when(jsonPath.getList("invalidJsonPath")).thenReturn(new ArrayList<>());
		assertEquals(new ArrayList<>(), (webApiResult.getFieldValueAsListFromStringResponse("invalidJsonPath")));
	}
	
	@Test
	public void getFieldValueAsList_FromStringResponse_nullResponseBody(){
		when(response.getBody()).thenReturn(null);
		webApiResult = new WebApiResult(response);
		assertNull(webApiResult.getFieldValueAsListFromStringResponse("invalidJsonPath"));
	}
	
	@Test
	public void getFieldValueAsMap_FromStringResponse(){
		when(jsonPath.getMap("invalidJsonPath")).thenReturn(new HashMap<>());
		assertEquals(new HashMap<>(), (webApiResult.getFieldValueAsMapFromStringResponse("invalidJsonPath")));
	}
	
	@Test
	public void getFieldValueAsMap_FromStringResponse_nullResponseBody(){
		when(response.getBody()).thenReturn(null);
		webApiResult = new WebApiResult(response);
		assertNull(webApiResult.getFieldValueAsMapFromStringResponse("invalidJsonPath"));
	}
	
	@Test
	public void getFieldValueAsString_FromJsonObjectResponse(){
		when(jsonPath.getString("invalidJsonPath")).thenReturn("str");
		assertEquals("str", (webApiResult.getFieldValueAsStringFromJsonObjectResponse("invalidJsonPath")));
	}
	
	@Test
	public void getFieldValueAsString_FromJsonObjectResponse_nullResponseBody(){
		when(response.getBody()).thenReturn(null);
		webApiResult = new WebApiResult(response);
		assertNull(webApiResult.getFieldValueAsStringFromJsonObjectResponse("invalidJsonPath"));
	}
	
	@Test
	public void getFieldValueAsList_FromJsonObjectResponse(){
		when(jsonPath.getList("invalidJsonPath")).thenReturn(new ArrayList<>());
		assertEquals(new ArrayList<>(), (webApiResult.getFieldValueAsListFromJsonObjectResponse("invalidJsonPath")));
	}
	
	@Test
	public void getFieldValueAsList_FromJsonObjectResponse_nullResponseBody(){
		when(response.getBody()).thenReturn(null);
		webApiResult = new WebApiResult(response);
		assertNull(webApiResult.getFieldValueAsListFromJsonObjectResponse("invalidJsonPath"));
	}
	
	@Test
	public void getFieldValueAsMap_FromJsonObjectResponse(){
		when(jsonPath.getMap("invalidJsonPath")).thenReturn(new HashMap<>());
		assertEquals(new HashMap<>(), (webApiResult.getFieldValueAsMapFromJsonObjectResponse("invalidJsonPath")));
	}
	
	@Test
	public void getFieldValueAsMap_FromJsonObjectResponse_nullResponseBody(){
		when(response.getBody()).thenReturn(null);
		webApiResult = new WebApiResult(response);
		assertNull(webApiResult.getFieldValueAsMapFromJsonObjectResponse("invalidJsonPath"));
	}
	
	@Test
	public void getFieldValueAsString_FromJsonArrayResponse(){
		when(jsonPath.getString("invalidJsonPath")).thenReturn("str");
		assertEquals("str", (webApiResult.getFieldValueAsStringFromJsonArrayResponse("invalidJsonPath")));
	}
	
	@Test
	public void getFieldValueAsString_FromJsonArrayResponse_nullResponseBody(){
		when(response.getBody()).thenReturn(null);
		webApiResult = new WebApiResult(response);
		assertNull(webApiResult.getFieldValueAsStringFromJsonArrayResponse("invalidJsonPath"));
	}
	
	@Test
	public void getFieldValueAsList_FromJsonArrayResponse(){
		when(jsonPath.getList("invalidJsonPath")).thenReturn(new ArrayList<>());
		assertEquals(new ArrayList<>(), (webApiResult.getFieldValueAsListFromJsonArrayResponse("invalidJsonPath")));
	}
	
	@Test
	public void getFieldValueAsList_FromJsonArrayResponse_nullResponseBody(){
		when(response.getBody()).thenReturn(null);
		webApiResult = new WebApiResult(response);
		assertNull(webApiResult.getFieldValueAsListFromJsonArrayResponse("invalidJsonPath"));
	}
	
	@Test
	public void getFieldValueAsMap_FromJsonArrayResponse_returnNull(){
		when(jsonPath.getList("invalidJsonPath")).thenReturn(new ArrayList<>());
		assertEquals(null, (webApiResult.getFieldValueAsMapFromJsonArrayResponse("invalidJsonPath", "invalidTargetMap")));
	}
	
	@Test
	public void getFieldValueAsMap_FromJsonArrayResponse_returnMap(){
		List<Object> list = new ArrayList<>();
		Map<String, Object> jsonobj=new HashMap<>();
		jsonobj.put("address", "moon");
		list.add(jsonobj);
		when(jsonPath.getList("invalidJsonPath")).thenReturn(list);
		assertEquals(jsonobj, (webApiResult.getFieldValueAsMapFromJsonArrayResponse("invalidJsonPath", "moon")));
	}
	
	@Test
	public void getFieldValueAsMap_FromJsonArrayResponse_notMapNotStringNotList(){
		List<Object> list = new ArrayList<>();
		list.add(100);
		when(jsonPath.getList("invalidJsonPath")).thenReturn(list);
		assertEquals(null, (webApiResult.getFieldValueAsMapFromJsonArrayResponse("invalidJsonPath", "moon")));
	}
	
	@Test
	public void getFieldValueAsMap_FromJsonArrayResponse_notMapKeyValue(){
		List<Object> list = new ArrayList<>();
		Map<String, Object> jsonobj=new HashMap<>();
		jsonobj.put("address", "sun");
		list.add(jsonobj);
		when(jsonPath.getList("invalidJsonPath")).thenReturn(list);
		assertEquals(null, (webApiResult.getFieldValueAsMapFromJsonArrayResponse("invalidJsonPath", "moon")));
	}
	
	@Test
	public void getFieldValueAsMap_FromJsonArrayResponse_containString(){
		List<Object> list = new ArrayList<>();
		String str = "testString";
		list.add(str);
		when(jsonPath.getList("invalidJsonPath")).thenReturn(list);
		assertEquals(null, (webApiResult.getFieldValueAsMapFromJsonArrayResponse("invalidJsonPath", "moon")));
	}
	
	@Test
	public void getFieldValueAsMap_FromJsonArrayResponse_containList(){
		List<Object> list = new ArrayList<>();
		List<String> testList = new ArrayList<>();
		testList.add("testString");
		list.add(testList);
		when(jsonPath.getList("invalidJsonPath")).thenReturn(list);
		assertEquals(null, (webApiResult.getFieldValueAsMapFromJsonArrayResponse("invalidJsonPath", "moon")));
	}
	
	@Test
	public void getFieldValueAsMap_FromJsonArrayResponse_nullResponseBody(){
		when(response.getBody()).thenReturn(null);
		webApiResult = new WebApiResult(response);
		assertNull(webApiResult.getFieldValueAsMapFromJsonArrayResponse("invalidJsonPath", "invalidTargetMap"));
	}
	
	@Test
	public void getSpecific_DataString_FromResponse(){
		when(jsonPath.getString("invalidJsonPath")).thenReturn("test string success");
		assertEquals("test string success", webApiResult.getSpecificDataFromResponse("invalidJsonPath", DataType.isString));
	}
	
	@Test
	public void getSpecific_DataInt_FromResponse(){
		when(jsonPath.getInt("invalidJsonPath")).thenReturn(100);
		assertEquals(100, webApiResult.getSpecificDataFromResponse("invalidJsonPath", DataType.isInt));
	}
	
	@Test
	public void getSpecific_DataBoolean_FromResponse(){
		when(jsonPath.getBoolean("invalidJsonPath")).thenReturn(true);
		assertEquals(true, webApiResult.getSpecificDataFromResponse("invalidJsonPath", DataType.isBoolean));
	}
	
	@Test
	public void getSpecific_DataByte_FromResponse(){
		when(jsonPath.getByte("invalidJsonPath")).thenReturn(new Byte((byte) 100));
		assertEquals(new Byte((byte) 100), webApiResult.getSpecificDataFromResponse("invalidJsonPath", DataType.isByte));
	}
	
	@Test
	public void getSpecific_DataChar_FromResponse(){
		when(jsonPath.getChar("invalidJsonPath")).thenReturn(new Character((char) 100));
		assertEquals(new Character((char) 100), webApiResult.getSpecificDataFromResponse("invalidJsonPath", DataType.isChar));
	}
	
	@Test
	public void getSpecific_DataDouble_FromResponse(){
		when(jsonPath.getDouble("invalidJsonPath")).thenReturn(new Double((double) 100));
		assertEquals(new Double((double) 100), webApiResult.getSpecificDataFromResponse("invalidJsonPath", DataType.isDouble));
	}
	
	@Test
	public void getSpecific_DataFloat_FromResponse(){
		when(jsonPath.getFloat("invalidJsonPath")).thenReturn(new Float((float) 100));
		assertEquals(new Float((float) 100), webApiResult.getSpecificDataFromResponse("invalidJsonPath", DataType.isFloat));
	}
	
	@Test
	public void getSpecific_DataLong_FromResponse(){
		when(jsonPath.getLong("invalidJsonPath")).thenReturn(new Long((long) 100));
		assertEquals(new Long((long) 100), webApiResult.getSpecificDataFromResponse("invalidJsonPath", DataType.isLong));
	}
	
	@Test
	public void getSpecific_DataShort_FromResponse(){
		when(jsonPath.getShort("invalidJsonPath")).thenReturn(new Short((short) 100));
		assertEquals(new Short((short) 100), webApiResult.getSpecificDataFromResponse("invalidJsonPath", DataType.isShort));
	}
	
	@Test
	public void getFieldValue_withResponseBody() throws Throwable{
		when(responseBody.path("invalidField")).thenReturn(100);
		assertEquals("100",webApiResult.getFieldValue("invalidField"));
	}
	
	@Test
	public void getFieldValue_nullResponseBody_withResponseBodyContent() throws Throwable{
		Gson gson=new Gson();
		Map<String, Object> jsonobj=new HashMap<>();
		Map<String, Object> subjsonobj=new HashMap<>();
		jsonobj.put("id", 1);
		jsonobj.put("name", "x");
		subjsonobj.put("name", "y");
		jsonobj.put("child", subjsonobj);
		when(response.getBody()).thenReturn(null);
		when(response.asString()).thenReturn(gson.toJson(jsonobj));
		webApiResult = new WebApiResult(response);
		assertEquals("y", webApiResult.getFieldValue("child.name"));
	}
	
	@Test
	public void getFieldValue_nullResponseBody_responseBodyContentButEmpty() throws Throwable{
		when(response.getBody()).thenReturn(null);
		when(response.asString()).thenReturn("");
		webApiResult = new WebApiResult(response);
		assertNull(webApiResult.getFieldValue("invalidField"));
	}
	
	@Test
	public void getFieldValue_nullResponseBody_nullResponseBodyContent() throws Throwable{
		when(response.getBody()).thenReturn(null);
		when(response.asString()).thenReturn(null);
		webApiResult = new WebApiResult(response);
		assertNull(webApiResult.getFieldValue("invalidField"));
	}
	
	@Test
	public void testConstruct_WebApiResult_noParams(){
		WebApiResult webResult=new WebApiResult();
		assertEquals(0, webResult.getStatusCode());
	}
	
	@Test
	public void testConstruct_WebApiResult(){
		Gson gson=new Gson();
		Map<String, Object> jsonobj=new HashMap<>();
		Map<String, Object> subjsonobj=new HashMap<>();
		jsonobj.put("id", 1);
		jsonobj.put("name", "x");
		subjsonobj.put("name", "y");
		jsonobj.put("child", subjsonobj);
		WebApiResult webResult=new WebApiResult(200, gson.toJson(jsonobj));
		assertEquals(200, webResult.getStatusCode());
	}
	
	@Test
	public void testGetFieldValueFromJsonString(){
		Gson gson=new Gson();
		List<Map<String, Object>> list=new ArrayList<>();
		Map<String, Object> jsonobj=new HashMap<>();
		jsonobj.put("address", "moon");
		list.add(jsonobj);
		assertEquals("[moon]", webApiResult.getFieldValueFromJsonString(gson.toJson(list), "address").toString());
	}
	
	@Test
	public void getArrayItem_nullResponseBody(){
		Gson gson=new Gson();
		List<Map<String, Object>> list=new ArrayList<>();
		Map<String, Object> jsonobj=new HashMap<>();
		Map<String, Object> subjsonobj=new HashMap<>();
		jsonobj.put("id", 1);
		jsonobj.put("name", "x");
		subjsonobj.put("name", "y");
		jsonobj.put("child", subjsonobj);
		list.add(jsonobj);
		jsonobj=new HashMap<>();
		jsonobj.put("address", "moon");
		list.add(jsonobj);
		when(response.getBody()).thenReturn(null);
		when(response.asString()).thenReturn(gson.toJson(list));
		webApiResult = new WebApiResult(response);
		assertEquals("{address=moon}", webApiResult.getArrayItem(1).toString());
	}
	
	@Test
	public void getArrayItem_withResponseBody(){
		Gson gson=new Gson();
		List<Map<String, Object>> list=new ArrayList<>();
		Map<String, Object> jsonobj=new HashMap<>();
		Map<String, Object> subjsonobj=new HashMap<>();
		jsonobj.put("id", 1);
		jsonobj.put("name", "x");
		subjsonobj.put("name", "y");
		jsonobj.put("child", subjsonobj);
		list.add(jsonobj);
		jsonobj=new HashMap<>();
		jsonobj.put("address", "moon");
		list.add(jsonobj);
		when(responseBody.asString()).thenReturn(gson.toJson(list));
		assertEquals("{address=moon}", webApiResult.getArrayItem(1).toString());
	}
	
	@Test
	public void getFieldsValues(){
		assertNull(webApiResult.getFieldsValues("invalidField"));
	}
	
	@Test
	public void getStatusCode_validCodeReturned(){
		assertEquals(200, webApiResult.getStatusCode());
	}
	
	@Test
	public void getStatusLine_correctValueReturned(){
		assertEquals("HTTP/1.1 200 OK", webApiResult.getStatusLine());
	}
	
	@Test
	public void getContentType_expectedValueReturned(){
		assertEquals("application/json; charset=utf-8", webApiResult.getContentType());
	}
	
	@Test
	public void getResponseTime_expectedValueReturned(){
		assertEquals(4081988, webApiResult.getResponseTime());
	}
	
	@Test
	public void getHeaderMap_expectedValueReturned(){
		assertEquals("17", webApiResult.getHeaderMap().get("Content-length"));
		assertEquals("2018-21-11 11:40:00 IST", webApiResult.getHeaderMap().get("Date"));
	}
	
	@Test
	public void getCookies_expectedValueReturned(){
		Map<String,String> cookies = new HashMap<>();
		cookies.put("s_fid", "0FFB406D1D1464D6-3569F2FE5427595E");
		assertEquals(cookies, webApiResult.getCookies());
	}
	
	@Test
	public void getCookies_expectedSpecifyValueReturned(){
		assertEquals("0FFB406D1D1464D6-3569F2FE5427595E", webApiResult.getCookieValue("s_fid"));
	}
	
	@Test
	public void getCookies_noKeyValueReturned(){
		assertEquals(null, webApiResult.getCookieValue("noKey"));
	}
	
	@Test
	public void getJsonResult_responseBody_responseBodyContent(){
		assertEquals("response as string",webApiResult.getJsonResult());
	}
	
	@Test
	public void getJsonResult_nullResponseBodyContent_responseBody(){
		when(response.asString()).thenReturn(null);
		when(responseBody.asString()).thenReturn("responseBodyContent is null but responseBody is not null");
		webApiResult = new WebApiResult(response);
		assertEquals("responseBodyContent is null but responseBody is not null",webApiResult.getJsonResult());
	}
	
	@Test
	public void getJsonResult_nullResponseBody_responseBodyConten(){
		when(response.getBody()).thenReturn(null);
		webApiResult = new WebApiResult(response);
		assertEquals("response as string",webApiResult.getJsonResult());
	}
	
	@Test
	public void getJsonResult_nullResponseBodyContent_nullResponseBody(){
		when(response.getBody()).thenReturn(null);
		when(response.asString()).thenReturn(null);
		webApiResult = new WebApiResult(response);
		assertEquals(null,webApiResult.getJsonResult());
	}
	
	@Test
	public void getResponseBodyContent(){
		assertEquals("response as string",webApiResult.getResponseBodyContent());
	}
	
	@Test
	public void getResponseBody(){
		assertEquals(responseBody,webApiResult.getResponseBody());
	}
	
	@Test
	public void testToString(){
		assertEquals("response as string",webApiResult.toString());
	}
	
	@Test
	public void getXmlFieldValueAsString_FromXmlResponse(){
		when(responseBody.xmlPath()).thenReturn(xmlPath);
		when(xmlPath.getString("invalidXmlPath")).thenReturn("xmlStr");
		assertEquals("xmlStr",webApiResult.getFieldValueAsStringFromXmlResponse("invalidXmlPath"));
	}
	
	@Test
	public void getXmlFieldValueAsString_FromXmlResponse_nullResponseBody(){
		when(response.getBody()).thenReturn(null);
		webApiResult = new WebApiResult(response);
		assertNull(webApiResult.getFieldValueAsStringFromXmlResponse("invalidXmlPath"));
	}
	
	@Test
	public void getXmlFieldValueAsList_FromXmlResponse(){
		when(responseBody.xmlPath()).thenReturn(xmlPath);
		when(xmlPath.getList("invalidXmlPath")).thenReturn(new ArrayList<>());
		assertEquals(new ArrayList<>(),webApiResult.getFieldValueAsListFromXmlResponse("invalidXmlPath"));
	}
	
	@Test
	public void getXmlFieldValueAsList_FromXmlResponse_nullResponseBody(){
		when(response.getBody()).thenReturn(null);
		webApiResult = new WebApiResult(response);
		assertNull(webApiResult.getFieldValueAsListFromXmlResponse("invalidXmlPath"));
	}
	
	@Test
	public void getXmlFieldValueAsMap_FromXmlResponse(){
		when(responseBody.xmlPath()).thenReturn(xmlPath);
		when(xmlPath.getMap("invalidXmlPath")).thenReturn(new HashMap<>());
		assertEquals(new HashMap<>(),webApiResult.getFieldValueAsMapFromXmlResponse("invalidXmlPath"));
	}
	
	@Test
	public void getXmlFieldValueAsMap_FromXmlResponse_nullResponseBody(){
		when(response.getBody()).thenReturn(null);
		webApiResult = new WebApiResult(response);
		assertNull(webApiResult.getFieldValueAsMapFromXmlResponse("invalidXmlPath"));
	}
	
	@Test
	public void getSpecific_DataString_FromXmlResponse(){
		when(responseBody.xmlPath()).thenReturn(xmlPath);
		when(xmlPath.getString("invalidXmlPath")).thenReturn("test string success");
		assertEquals("test string success", webApiResult.getSpecificDataFromXmlResponse("invalidXmlPath", DataType.isString));
	}
	
	@Test
	public void getSpecific_DataInt_FromXmlResponse(){
		when(responseBody.xmlPath()).thenReturn(xmlPath);
		when(xmlPath.getInt("invalidXmlPath")).thenReturn(100);
		assertEquals(100, webApiResult.getSpecificDataFromXmlResponse("invalidXmlPath", DataType.isInt));
	}
	
	@Test
	public void getSpecific_DataBoolean_FromXmlResponse(){
		when(responseBody.xmlPath()).thenReturn(xmlPath);
		when(xmlPath.getBoolean("invalidXmlPath")).thenReturn(true);
		assertEquals(true, webApiResult.getSpecificDataFromXmlResponse("invalidXmlPath", DataType.isBoolean));
	}
	
	@Test
	public void getSpecific_DataByte_FromXmlResponse(){
		when(responseBody.xmlPath()).thenReturn(xmlPath);
		when(xmlPath.getByte("invalidXmlPath")).thenReturn(new Byte((byte) 100));
		assertEquals(new Byte((byte) 100), webApiResult.getSpecificDataFromXmlResponse("invalidXmlPath", DataType.isByte));
	}
	
	@Test
	public void getSpecific_DataChar_FromXmlResponse(){
		when(responseBody.xmlPath()).thenReturn(xmlPath);
		when(xmlPath.getChar("invalidXmlPath")).thenReturn(new Character((char) 100));
		assertEquals(new Character((char) 100), webApiResult.getSpecificDataFromXmlResponse("invalidXmlPath", DataType.isChar));
	}
	
	@Test
	public void getSpecific_DataDouble_FromXmlResponse(){
		when(responseBody.xmlPath()).thenReturn(xmlPath);
		when(xmlPath.getDouble("invalidXmlPath")).thenReturn(new Double((double) 100));
		assertEquals(new Double((double) 100), webApiResult.getSpecificDataFromXmlResponse("invalidXmlPath", DataType.isDouble));
	}
	
	@Test
	public void getSpecific_DataFloat_FromXmlResponse(){
		when(responseBody.xmlPath()).thenReturn(xmlPath);
		when(xmlPath.getFloat("invalidXmlPath")).thenReturn(new Float((float) 100));
		assertEquals(new Float((float) 100), webApiResult.getSpecificDataFromXmlResponse("invalidXmlPath", DataType.isFloat));
	}
	
	@Test
	public void getSpecific_DataLong_FromXmlResponse(){
		when(responseBody.xmlPath()).thenReturn(xmlPath);
		when(xmlPath.getLong("invalidXmlPath")).thenReturn(new Long((long) 100));
		assertEquals(new Long((long) 100), webApiResult.getSpecificDataFromXmlResponse("invalidXmlPath", DataType.isLong));
	}
	
	@Test
	public void getSpecific_DataShort_FromXmlResponse(){
		when(responseBody.xmlPath()).thenReturn(xmlPath);
		when(xmlPath.getShort("invalidXmlPath")).thenReturn(new Short((short) 100));
		assertEquals(new Short((short) 100), webApiResult.getSpecificDataFromXmlResponse("invalidXmlPath", DataType.isShort));
	}
}


