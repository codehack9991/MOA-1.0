package fast.common.context;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.powermock.api.mockito.PowerMockito.when;
import static org.powermock.api.mockito.PowerMockito.whenNew;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import quickfix.ConfigError;
import quickfix.InvalidMessage;
import quickfix.Message;

import com.esotericsoftware.yamlbeans.YamlReader;

import cucumber.api.DataTable;
import cucumber.api.Scenario;
import fast.common.core.Configurator;
import fast.common.core.MapMessageTemplate;
import fast.common.core.ValidationFailed;
import fast.common.fix.DataDictionary;
import fast.common.fix.FixHelper;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ Configurator.class })
@PowerMockIgnore({ "javax.management.*", "javax.crypto.*" })
public class TestMapMessageTemplateHelper {
	@Mock
	YamlReader yamlReader;

	@InjectMocks
	Configurator mockconfig;
	@Before
	public void setUp() throws Exception {
		Map settingsMap = new HashMap();
		Map fixMessageTemplates = new HashMap();
		fixMessageTemplates.put("ReportCancelPartial", "MsgType=NewSingle|57=CASH_COES_INSTANCE");
		fixMessageTemplates.put("RejctOrder", "RepType=ExecutionReport|65=EDF<<>>String<<>>CONTAIN");
		settingsMap.put("FixMessageTemplates", fixMessageTemplates);
		
		Map structuredFixMessageTemplates = new HashMap();
		
		Map customTemplate  = new HashMap();
		customTemplate.put( "SubTemplates", "ReportCancelPartial");
		customTemplate.put( "MsgType", "NewOrderSingle");
		customTemplate.put( "Currency", "$Currency");
		customTemplate.put( "Side", "BUY");
		structuredFixMessageTemplates.put("Client_RequestNew", customTemplate);
		settingsMap.put("StructuredFixMessageTemplates", structuredFixMessageTemplates);

		when((Map) yamlReader.read()).thenReturn(settingsMap);
		whenNew(YamlReader.class).withAnyArguments().thenReturn(yamlReader);
		mockconfig.release();
		mockconfig = Configurator.getInstance();
	}
	
	@Test
	public void populateTemplateMap_getRequiredFixTemplatesFromConfig() throws Exception{
		Map<String,MapMessageTemplate> fixTemplates= new HashMap<String, MapMessageTemplate>();
		MapMessageTemplateHelper.populateTemplateMap(mockconfig.getSettingsMap(), "FixMessageTemplates", null, fixTemplates, null);
		assertEquals("MsgType=NewSingle|57=CASH_COES_INSTANCE|",fixTemplates.get("ReportCancelPartial").getFieldValueString());
	}
	@Test
	public void populateTemplateMap_NoRequiredTemplatesFromConfig() throws Exception{
		Map<String,MapMessageTemplate> fixTemplates= new HashMap<String, MapMessageTemplate>();
		MapMessageTemplateHelper.populateTemplateMap(mockconfig.getSettingsMap(), "MessageTemplates", null, fixTemplates, null);
		assertTrue(fixTemplates.isEmpty());
	}
	@Test
	public void generateTemplate_dataDictionaryIsNotNull(){
		DataDictionary dataDictionary = mock(DataDictionary.class);
		when(dataDictionary.getFieldTag("SYM")).thenReturn(10033);
		when(dataDictionary.getFieldTag("BID")).thenReturn(7089);
		Map<String, MapMessageTemplate> existingtemplates = new HashMap<String, MapMessageTemplate>();			
		MapMessageTemplate template = MapMessageTemplateHelper.generateTemplate("Test", "SYM=C<<>>String<<>>Equal|BID=60", existingtemplates, dataDictionary);		
		assertEquals("10033=C|7089=60|", template.getFieldValueString());
		assertEquals("String", template.getValidateField("10033").get(MapMessageTemplateHelper.VALIDATE_DATATYPE));
	}
	@Test
	public void generateTemplate_intTags(){
		DataDictionary dataDictionary = mock(DataDictionary.class);
		Map<String, MapMessageTemplate> existingtemplates = new HashMap<String, MapMessageTemplate>();			
		MapMessageTemplate template = MapMessageTemplateHelper.generateTemplate("Test", "10033=C<<>>String<<>>Equal|7089=60", existingtemplates, dataDictionary);		
		assertEquals("10033=C|7089=60|", template.getFieldValueString());
		assertEquals("String", template.getValidateField("10033").get(MapMessageTemplateHelper.VALIDATE_DATATYPE));
	}
	@Test
	public void generateTemplate_exceptionWithoutEqualSymbol(){
		Map<String, MapMessageTemplate> existingtemplates = new HashMap<String, MapMessageTemplate>();			
		try{
			MapMessageTemplate template = MapMessageTemplateHelper.generateTemplate("Test", "10033=C<<>>String<<>>Equal|7089", existingtemplates, null);
		}catch(RuntimeException e){
			assertTrue(e.getMessage().contains("please check format with"));
		}
	}
	@Test
	public void generateTemplate_lastSymbolIsFixSpe(){
		Map<String, MapMessageTemplate> existingtemplates = new HashMap<String, MapMessageTemplate>();			
		MapMessageTemplate template = MapMessageTemplateHelper.generateTemplate("Test", "SYM=C|BID=60|", existingtemplates, null);		
		assertEquals("SYM=C|BID=60|", template.getFieldValueString());
	}
	@Test
	public void generateTemplate_existingtemplatesIsNull(){
		Map<String, MapMessageTemplate> existingtemplates = new HashMap<String, MapMessageTemplate>();			
		MapMessageTemplate template = MapMessageTemplateHelper.generateTemplate("Test", "SYM=C<<>>String<<>>Equal|BID=60", existingtemplates, null);		
		assertEquals("SYM=C|BID=60|", template.getFieldValueString());
		assertEquals("String", template.getValidateField("SYM").get(MapMessageTemplateHelper.VALIDATE_DATATYPE));
	}
	@Test
	public void generateTemplate_existingtemplatesIsNotNull(){
		Map<String,MapMessageTemplate> existingtemplates= new HashMap<String, MapMessageTemplate>();
		MapMessageTemplateHelper.populateTemplateMap(mockconfig.getSettingsMap(), "FixMessageTemplates", null, existingtemplates, null);
		MapMessageTemplate template = MapMessageTemplateHelper.generateTemplate("ReportCancelPartial", "SYM=C<<>>String<<>>Equal|BID=60", existingtemplates, null);		
		assertEquals("MsgType=NewSingle|57=CASH_COES_INSTANCE|SYM=C|BID=60|", template.getFieldValueString());
		assertEquals("String", template.getValidateField("SYM").get(MapMessageTemplateHelper.VALIDATE_DATATYPE));
	}
	
	@Test
	public void generateTemplate_userStringIsArrayList(){
		Map<String,MapMessageTemplate> existingtemplates= new HashMap<String, MapMessageTemplate>();
		MapMessageTemplateHelper.populateTemplateMap(mockconfig.getSettingsMap(), "FixMessageTemplates", null, existingtemplates, null);
		ArrayList<Map<String, String>> arraylist = new ArrayList<Map<String, String>> ();
		Map<String, String> field1 = new HashMap();
		field1.put("SYM", "C<<>>String<<>>Equal");
		Map<String, String> field2 = new HashMap();
		field2.put("BID", "60");
		arraylist.add(field1);
		arraylist.add(field2);
		MapMessageTemplate template = MapMessageTemplateHelper.generateTemplate("ReportCancelPartial", arraylist, existingtemplates, null);		
		assertEquals("MsgType=NewSingle|57=CASH_COES_INSTANCE|SYM=C|BID=60|", template.getFieldValueString());
		assertEquals("String", template.getValidateField("SYM").get(MapMessageTemplateHelper.VALIDATE_DATATYPE));
	}

	@Test
	public void generateTemplate_userStringIsMap(){
		Map<String,MapMessageTemplate> existingtemplates= new HashMap<String, MapMessageTemplate>();
		MapMessageTemplateHelper.populateTemplateMap(mockconfig.getSettingsMap(), "FixMessageTemplates", null, existingtemplates, null);
		Map<String, String> filedMap = new HashMap();
		filedMap.put("SYM", "C<<>>String<<>>Equal");
		filedMap.put("BID", "60");
		MapMessageTemplate template = MapMessageTemplateHelper.generateTemplate("ReportCancelPartial", filedMap, existingtemplates, null);		
		assertEquals("MsgType=NewSingle|57=CASH_COES_INSTANCE|SYM=C|BID=60|", template.getFieldValueString());
		assertEquals("String", template.getValidateField("SYM").get(MapMessageTemplateHelper.VALIDATE_DATATYPE));
	}
	
	@Test
	public void parseFieldValue_parseFieldWithSubTemplate(){
		String elementKey="SubTemplates";
		String elementValue="RejctOrder|ReportCancelPartial";
		StringBuilder fieldValueString = new StringBuilder();
		Map<String,MapMessageTemplate> existingtemplates= new HashMap<String, MapMessageTemplate>();	
		MapMessageTemplateHelper.populateTemplateMap(mockconfig.getSettingsMap(), "FixMessageTemplates", null, existingtemplates, null);
		Map<String, Map<String, String>> validateField = new LinkedHashMap<String, Map<String, String>>();
		MapMessageTemplate template = new MapMessageTemplate("test", validateField);
		MapMessageTemplateHelper.parseFieldValue(elementKey, elementValue, template,fieldValueString, existingtemplates, null);
		assertEquals("RepType=ExecutionReport|65=EDF|MsgType=NewSingle|57=CASH_COES_INSTANCE|",fieldValueString.toString());
		assertEquals("CONTAIN", template.getValidateField("65").get(MapMessageTemplateHelper.VALIDATE_OPERATOR));

	}
	
	@Test
	public void parseFieldValue_dataDictionaryIsNotNull(){
		String elementKey="RepType";
		String elementValue="ExecutionReport";
		StringBuilder fieldValueString = new StringBuilder();
		DataDictionary dataDictionary = mock(DataDictionary.class);
		when(dataDictionary.getFieldTag("RepType")).thenReturn(36);
		MapMessageTemplateHelper.parseFieldValue(elementKey, elementValue, null,fieldValueString, null, dataDictionary);
		assertEquals("36=ExecutionReport|",fieldValueString.toString());

	}
	
	@Test
	public void parseFieldValue_intTags(){
		String elementKey="36";
		String elementValue="ExecutionReport";
		StringBuilder fieldValueString = new StringBuilder();
		DataDictionary dataDictionary = mock(DataDictionary.class);
		MapMessageTemplateHelper.parseFieldValue(elementKey, elementValue, null,fieldValueString, null, dataDictionary);
		assertEquals("36=ExecutionReport|",fieldValueString.toString());

	}
	
	@Test
	public void convertFieldObject_string() throws ValidationFailed{
		Object result = MapMessageTemplateHelper.convertFieldObject("ABC","string");
		assertTrue(result instanceof  String);
	}
	
	@Test
	public void convertFieldObject_number() throws ValidationFailed{
		Object result = MapMessageTemplateHelper.convertFieldObject("1234","NUMBER");
		assertTrue(result instanceof  Double);
	}
	
	@Test
	public void convertFieldObject_datetime() throws ValidationFailed{
		Object result = MapMessageTemplateHelper.convertFieldObject("20190103-17:34:49","datetime");
		assertTrue(result instanceof  ZonedDateTime);
	}
	@Test
	public void convertFieldObject_exceptionWithInvalidType() throws ValidationFailed{
		try{
			Object result = MapMessageTemplateHelper.convertFieldObject("20190103-17:34:49","text");
		}catch(ValidationFailed e){
			assertTrue(e.getMessage().contains("Invalid data type"));
		}
	}
	
	@Test
	public void operatorEqual_stringEqual(){
		Object actual="ABC";
		Object excepted="ABC";
		assertTrue(MapMessageTemplateHelper.operatorEqual(actual, excepted));
	}
	@Test
	public void operatorEqual_stringNotEqual(){
		Object actual="ABC";
		Object excepted="BCD";
		assertFalse(MapMessageTemplateHelper.operatorEqual(actual, excepted));
	}
	
	@Test
	public void operatorEqual_doubleEqual() throws ValidationFailed{
		Object actual = MapMessageTemplateHelper.convertFieldObject("12.34","NUMBER");
		Object excepted = MapMessageTemplateHelper.convertFieldObject("12.34","NUMBER");
		assertTrue(MapMessageTemplateHelper.operatorEqual(actual, excepted));
	}
	@Test
	public void operatorEqual_doubleNotEqual() throws ValidationFailed{
		Object actual = MapMessageTemplateHelper.convertFieldObject("12.34","NUMBER");
		Object excepted = MapMessageTemplateHelper.convertFieldObject("22.34","NUMBER");
		assertFalse(MapMessageTemplateHelper.operatorEqual(actual, excepted));
	}
	
	@Test
	public void operatorEqual_zonedDateTimeEqual() throws ValidationFailed{
		Object actual = MapMessageTemplateHelper.convertFieldObject("20190103-17:34:49","datetime");
		Object excepted = MapMessageTemplateHelper.convertFieldObject("20190103-17:34:49","datetime");
		assertTrue(MapMessageTemplateHelper.operatorEqual(actual, excepted));
	}
	@Test
	public void operatorEqual_zonedDateTimeNotEqual() throws ValidationFailed{
		Object actual = MapMessageTemplateHelper.convertFieldObject("20190103-17:34:49","datetime");
		Object excepted = MapMessageTemplateHelper.convertFieldObject("20190102-17:34:49","datetime");
		assertFalse(MapMessageTemplateHelper.operatorEqual(actual, excepted));
	}
	@Test
	public void operatorGreater_zonedDateTimeGreater() throws ValidationFailed{
		Object actual = MapMessageTemplateHelper.convertFieldObject("20190103-17:34:49","datetime");
		Object excepted = MapMessageTemplateHelper.convertFieldObject("20190102-17:34:49","datetime");
		assertTrue(MapMessageTemplateHelper.operatorGreater(actual, excepted));
	}
	@Test
	public void operatorGreater_zonedDateTimeNotGreater() throws ValidationFailed{
		Object actual = MapMessageTemplateHelper.convertFieldObject("20190101-17:34:49","datetime");
		Object excepted = MapMessageTemplateHelper.convertFieldObject("20190102-17:34:49","datetime");
		assertFalse(MapMessageTemplateHelper.operatorGreater(actual, excepted));
	}
	@Test
	public void operatorGreater_doubleGreater() throws ValidationFailed{
		Object actual = MapMessageTemplateHelper.convertFieldObject("32.34","NUMBER");
		Object excepted = MapMessageTemplateHelper.convertFieldObject("22.34","NUMBER");
		assertTrue(MapMessageTemplateHelper.operatorGreater(actual, excepted));
	}
	@Test
	public void operatorGreater_doubleNotGreater() throws ValidationFailed{
		Object actual = MapMessageTemplateHelper.convertFieldObject("12.34","NUMBER");
		Object excepted = MapMessageTemplateHelper.convertFieldObject("22.34","NUMBER");
		assertFalse(MapMessageTemplateHelper.operatorGreater(actual, excepted));
	}
	@Test
	public void operatorGreater_exceptionWithInvalidType() throws ValidationFailed{
		try{
			Object result = MapMessageTemplateHelper.operatorGreater("AC","text");
		}catch(ValidationFailed e){
			assertTrue(e.getMessage().contains("Invalid operator for datatype String"));
		}
	}
	@Test
	public void validateWithField_exceptionWithoutActualValue() throws ValidationFailed{
		String exceptedValue="ABCD";
		String dataType="string";
		String operator="contain";
		try{
			assertTrue(MapMessageTemplateHelper.validateWithField( null,  exceptedValue,  dataType,  operator));
		}catch(ValidationFailed e){
			assertEquals("Actual Value is null",e.getMessage());
		}
	}
	@Test
	public void validateWithField_stringContain() throws ValidationFailed{
		String actualValue="ABCDE";
		String exceptedValue="ABCD";
		String dataType="string";
		String operator="contain";
		assertTrue(MapMessageTemplateHelper.validateWithField( actualValue,  exceptedValue,  dataType,  operator));
	}
	@Test
	public void validateWithField_stringEqual() throws ValidationFailed{
		String actualValue="ABCD";
		String exceptedValue="ABCD";
		String dataType="string";
		String operator="equal";
		assertTrue(MapMessageTemplateHelper.validateWithField( actualValue,  exceptedValue,  dataType,  operator));
	}
	@Test
	public void validateWithField_stringNonEqual() throws ValidationFailed{
		String actualValue="ABCD";
		String exceptedValue="ABCDE";
		String dataType="string";
		String operator="nonequal";
		assertTrue(MapMessageTemplateHelper.validateWithField( actualValue,  exceptedValue,  dataType,  operator));
	}
	@Test
	public void validateWithField_stringNotNonEqual() throws ValidationFailed{
		String actualValue="ABCD";
		String exceptedValue="ABCD";
		String dataType="string";
		String operator="nonequal";
		assertFalse(MapMessageTemplateHelper.validateWithField( actualValue,  exceptedValue,  dataType,  operator));
	}
	@Test
	public void validateWithField_exceptionWithContain() throws ValidationFailed{
		String actualValue="12.35";
		String exceptedValue="12.3";
		String dataType="number";
		String operator="contain";
		try{
			MapMessageTemplateHelper.validateWithField( actualValue,  exceptedValue,  dataType,  operator);
		}catch(ValidationFailed e){
			assertTrue(e.getMessage().contains("Invalid operator"));
		}
	}
	@Test
	public void validateWithField_numberGreater() throws ValidationFailed{
		String actualValue="12.3";
		String exceptedValue="12.2";
		String dataType="number";
		String operator="Greater";
		assertTrue(MapMessageTemplateHelper.validateWithField( actualValue,  exceptedValue,  dataType,  operator));
	}
	
	@Test
	public void validateWithField_numberNotGreater() throws ValidationFailed{
		String actualValue="12.1";
		String exceptedValue="12.2";
		String dataType="number";
		String operator="Greater";
		assertFalse(MapMessageTemplateHelper.validateWithField( actualValue,  exceptedValue,  dataType,  operator));
	}
	
	
	@Test
	public void validateWithField_numberGreaterOrEqualExceptedGreater() throws ValidationFailed{
		String actualValue="12.3";
		String exceptedValue="12.2";
		String dataType="number";
		String operator="GreaterorEqual";
		assertTrue(MapMessageTemplateHelper.validateWithField( actualValue,  exceptedValue,  dataType,  operator));
	}
	@Test
	public void validateWithField_numberGreaterOrEqualExceptedEqual() throws ValidationFailed{
		String actualValue="12.3";
		String exceptedValue="12.3";
		String dataType="number";
		String operator="GreaterorEqual";
		assertTrue(MapMessageTemplateHelper.validateWithField( actualValue,  exceptedValue,  dataType,  operator));
	}
	@Test
	public void validateWithField_numberNotGreaterOrEqual() throws ValidationFailed{
		String actualValue="12.1";
		String exceptedValue="12.2";
		String dataType="number";
		String operator="GreaterorEqual";
		assertFalse(MapMessageTemplateHelper.validateWithField( actualValue,  exceptedValue,  dataType,  operator));
	}
	@Test
	public void validateWithField_datetimeLess() throws ValidationFailed{
		String actualValue="20190103-17:34:49";
		String exceptedValue="20190104-17:34:49";
		String dataType="datetime";
		String operator="less";
		assertTrue(MapMessageTemplateHelper.validateWithField( actualValue,  exceptedValue,  dataType,  operator));
	}
	@Test
	public void validateWithField_datetimeNotLess() throws ValidationFailed{
		String actualValue="20190104-17:34:49";
		String exceptedValue="20190103-17:34:49";
		String dataType="datetime";
		String operator="less";
		assertFalse(MapMessageTemplateHelper.validateWithField( actualValue,  exceptedValue,  dataType,  operator));
	}
	@Test
	public void validateWithField_datetimeLessorEqualExceptedEqual() throws ValidationFailed{
		String actualValue="20190103-17:34:49";
		String exceptedValue="20190103-17:34:49";
		String dataType="datetime";
		String operator="lessorequal";
		assertTrue(MapMessageTemplateHelper.validateWithField( actualValue,  exceptedValue,  dataType,  operator));
	}
	@Test
	public void validateWithField_datetimeLessorEqualExceptedLess() throws ValidationFailed{
		String actualValue="20190102-17:34:49";
		String exceptedValue="20190103-17:34:49";
		String dataType="datetime";
		String operator="lessorequal";
		assertTrue(MapMessageTemplateHelper.validateWithField( actualValue,  exceptedValue,  dataType,  operator));
	}
	@Test
	public void validateWithField_datetimeNotLessorEqual() throws ValidationFailed{
		String actualValue="20190104-17:34:49";
		String exceptedValue="20190103-17:34:49";
		String dataType="datetime";
		String operator="lessorequal";
		assertFalse(MapMessageTemplateHelper.validateWithField( actualValue,  exceptedValue,  dataType,  operator));
	}
	
	@Test
	public void tableToUserstr_converToUserString(){
		List<List<String>> infoInTheRaw = Arrays.asList( Arrays.asList("35", "10044"), Arrays.asList("8", "SBDHJ") ); 
	    DataTable dataTable = DataTable.create(infoInTheRaw);
	    String result = MapMessageTemplateHelper.tableToUserstr(dataTable);
	    assertEquals("35=10044|8=SBDHJ|", result);
	}
	@Test
	@PrepareForTest(Scenario.class)
	public void processTemplate_getExceptedTemplate() throws Exception {
		Scenario mockScenario = mock(Scenario.class);
		ScenarioContext scenarioContext = new ScenarioContext(mockScenario);
		
		CommonStepResult stepResult = new CommonStepResult();
		stepResult.setFieldValue("C");
		scenarioContext.saveLastStepResult(stepResult, "@sym");
		
		Map<String, MapMessageTemplate> existingtemplates = new HashMap<String, MapMessageTemplate>();		
		MapMessageTemplate template = MapMessageTemplateHelper.generateTemplate("Test", "SYM=@sym.Value<<>>String<<>>Equal|BID=60", existingtemplates, null);
		MapMessageTemplate processedTemplate = MapMessageTemplateHelper.processTemplate(scenarioContext, template);
		assertEquals("SYM=C|BID=60|", processedTemplate.getFieldValueString());
		assertEquals("String", processedTemplate.getValidateField("SYM").get(MapMessageTemplateHelper.VALIDATE_DATATYPE));
	}
	@Test
	public void setMessageFieldSep_convertSOHToSep(){
		String rawString= "8=FIX.4.29=249235=NewSingle49=COMET50=CASH_COES_INSTANCE10143=ORT10=1029";
		String message = MapMessageTemplateHelper.setMessageFieldSep(rawString);
		assertEquals("8=FIX.4.2|9=2492|35=NewSingle|49=COMET|50=CASH_COES_INSTANCE10|143=ORT|10=1029|", message);
	}
	@Test
	public void setMessageFieldSep_convertAToSep(){
		String rawString= "8=FIX.4.2^A9=2492^A35=NewSingle^A49=COMET^A50=CASH_COES_INSTANCE10^A143=ORT^A10=1029";
		String message = MapMessageTemplateHelper.setMessageFieldSep(rawString);
		assertEquals("8=FIX.4.2|9=2492|35=NewSingle|49=COMET|50=CASH_COES_INSTANCE10|143=ORT|10=1029|", message);
	}
	@Test
	public void setMessageFieldSep_convertWaveLineToSep(){
		String rawString= "8=FIX.4.2~9=2492~35=NewSingle~49=COMET~50=CASH_COES_INSTANCE10~143=ORT~10=1029";
		String message = MapMessageTemplateHelper.setMessageFieldSep(rawString);
		assertEquals("8=FIX.4.2|9=2492|35=NewSingle|49=COMET|50=CASH_COES_INSTANCE10|143=ORT|10=1029|", message);
	}
	@Test
	public void validateFieldByDefault_stringEqual() throws ValidationFailed{
		int tag = 1033;
		String actualValue = "DHJK";
		String expectedValue= "JSL";
		StringBuilder diffBuilder = new StringBuilder();
		MapMessageTemplateHelper.validateFieldByDefault(tag, actualValue, expectedValue, diffBuilder);
		assertTrue(diffBuilder.toString().contains("1033=(DHJK)(JSL)(EQUAL)"));
	}
	@Test
	public void validateFieldByDefault_stringMatchPattern() throws ValidationFailed{
		int tag = 1033;
		String actualValue = "DHJK";
		String expectedValue= "/(\\d+)/";
		StringBuilder diffBuilder = new StringBuilder();
		MapMessageTemplateHelper.validateFieldByDefault(tag, actualValue, expectedValue, diffBuilder);
		assertTrue(diffBuilder.toString().contains("1033=(DHJK)(/(\\d+)/)(EQUAL)"));
	}
	@Test
	public void getExceptedVerifyMessage() throws ConfigError, InvalidMessage{
		Map paramsMap = new HashMap();
		paramsMap.put("data_dictionary", System.getProperty("user.dir")+"/src/test/resources/fast/common/fix/resources/FIX42.xml");
		FixHelper fixHelper = new FixHelper(paramsMap,mockconfig);
		String rawMsg= "8=FIX.4.29=249235=NewSingle49=COMET50=CASH_COES_INSTANCE10143=ORT1028=12341021=DKJ1035=C10=000";
		Message actualMessage = fixHelper.createFixMessage(rawMsg);
		Map<String, MapMessageTemplate> existingtemplates = new HashMap<String, MapMessageTemplate>();			
		MapMessageTemplate template = MapMessageTemplateHelper.generateTemplate("Test", "1035=C<<>>String<<>>Equal|BID=60", existingtemplates, null);		
		StringBuilder excepted = new StringBuilder();
		MapMessageTemplateHelper.getExceptedVerifyMessage(actualMessage,template,excepted);
		assertTrue(excepted.toString().contains("1021=DKJ|1028=1234|1035(Equal)C|"));
	}
	@After
	public void checkMockito() {
		org.mockito.Mockito.validateMockitoUsage(); 
	}

}
