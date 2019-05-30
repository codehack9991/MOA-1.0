package fast.common.fix;

import static org.junit.Assert.*;
import static org.powermock.api.mockito.PowerMockito.when;
import static org.powermock.api.mockito.PowerMockito.whenNew;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.xpath.XPathExpressionException;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import com.esotericsoftware.yamlbeans.YamlReader;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import fast.common.agents.AgentsManager;
import fast.common.agents.IFixMessagingAgent;
import fast.common.context.EvalScope;
import fast.common.context.FixStepResult;
import fast.common.context.ScenarioContext;
import fast.common.context.StepResult;
import fast.common.core.Configurator;
import fast.common.core.MapMessageTemplate;
import fast.common.core.ValidationFailed;
import fast.common.replay.MessageMissing_ReplayException;
import quickfix.ConfigError;
import quickfix.FieldConvertError;
import quickfix.FieldNotFound;
import quickfix.InvalidMessage;
import quickfix.Message;
import quickfix.SessionSettings;


@RunWith(PowerMockRunner.class)
@PrepareForTest({ Configurator.class })
@PowerMockIgnore({ "javax.management.*", "javax.crypto.*" })
public class TestFixHelper {
	
	@Mock
	YamlReader yamlReader;

	@InjectMocks
	Configurator mockconfig;
	
	Map settingsMap = new HashMap();
	Map paramsMap = new HashMap();

	FixHelper fixHelper=null;		
	String rawMsg =null;
	ScenarioContext scenarioContext =null;
	Message actualMessage = null;
	Message receiveMessage1=null;
	Message receiveMessage2 =null;
	ArrayList<Message> receivedMessages =null;
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
		Map fixMessageTemplates = new HashMap();
		fixMessageTemplates.put("ReportCancelPartial", "MsgType=NewSingle|57=CASH_COES_INSTANCE");
		fixMessageTemplates.put("RejctOrder", "RepType=ExecutionReport");
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
		
		paramsMap.put("data_dictionary", System.getProperty("user.dir")+"/src/test/resources/fast/common/fix/resources/FIX42.xml");

		fixHelper = new FixHelper(paramsMap,mockconfig);
		rawMsg= "8=FIX.4.29=249235=NewSingle49=COMET50=CASH_COES_INSTANCE10143=ORT52=20180608-16:41:06.2348054=510962=LIST10964=18130363atu10965=7011019=N11346=0.011347=0.010372=A100232=50310890=0.09871=181303624e410=000";
		actualMessage = fixHelper.createFixMessage(rawMsg);

		String rawMsg1 = "8=FIX.4.29=249235=849=COMET50=CASH_COES_INSTANCE10143=ORT52=20180608-16:41:06.2348054=410962=LIST10964=18130363atu38=1001.037=37859295411=1/20181210-110310965=7011019=N11346=0.011347=1.010372=A100232=50310890=0.09871=181303624e410=000";
		receiveMessage1 = fixHelper.createFixMessage(rawMsg1);
		String rawMsg2 = "8=FIX.4.29=249235=NewSingle49=COMET50=CASH_COES_INSTANCE10143=ORT52=20180608-16:41:06.2348054=557=CASH_COES_INSTANCE10962=LIST10964=18130363atu38=1000.037=18923607411=2/20181210-110310965=7011019=N11346=0.011347=2.010372=A100232=50310890=0.09871=181303624e410=000";
		receiveMessage2 = fixHelper.createFixMessage(rawMsg2);
		receivedMessages = new ArrayList<Message>();
		receivedMessages.add(receiveMessage1);
		receivedMessages.add(receiveMessage2);
		
		
		scenarioContext = new ScenarioContext("test");
		EvalScope evalScope = new EvalScope();
		Whitebox.setInternalState(scenarioContext, "_evalScope", evalScope);
		StepResult lastStepResult=null;
		Whitebox.setInternalState(scenarioContext, "_lastStepResult", lastStepResult);
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void convertUserstrToFulluserstr_exceptedRequiredTemplate() throws Exception {		
		String userString= "62=test|10771=APP.L";
		String resultString = Whitebox.invokeMethod(fixHelper, "convertUserstrToFulluserstr", "Client_RequestNew",userString,null);
		assertTrue(resultString.contains("MsgType=NewSingle|57=CASH_COES_INSTANCE"));
		assertTrue(!resultString.contains("RepType=ExecutionReport"));
		
	}

	@Test
	public void convertUserstrToFulluserstr_exceptedExtraTags()throws Exception {		
		String userString= "62=test|10771=APP.L";
		String resultString = Whitebox.invokeMethod(fixHelper, "convertUserstrToFulluserstr", "Client_RequestNew",userString,"11=123");
		assertTrue(resultString.contains("11=123"));		
	}
	@Test
	public void getSendingTimeStr_exceptedTimeFormatter(){
		String time= FixHelper.getSendingTimeStr();
		Pattern DATE_PATTERN = Pattern.compile("[0-9]{4}(0[1-9]|1[0-2])(0[1-9]|[1-2][0-9]|3[0-1])-(2[0-3]|[01][0-9]):([0-5][0-9]):([0-5][0-9])");
		Matcher m=DATE_PATTERN.matcher(time); 
		assertTrue(m.matches());
	}
	
	
	@Test
	public void getTransactTimeStr_exceptedTimeFormatter(){
		String time= FixHelper.getTransactTimeStr();
		Pattern DATE_PATTERN = Pattern.compile("[0-9]{4}(0[1-9]|1[0-2])(0[1-9]|[1-2][0-9]|3[0-1])-(2[0-3]|[01][0-9]):([0-5][0-9]):([0-5][0-9])\\.\\d{3}");
		Matcher m=DATE_PATTERN.matcher(time); 
		assertTrue(m.matches());
	}
	
	@Test
	public void generateClOrdID_exceptedCustomerFormatter(){
		String clOrdID= FixHelper.generateClOrdID();
		Pattern DATE_PATTERN = Pattern.compile("\\d+/[0-9]{4}(0[1-9]|1[0-2])(0[1-9]|[1-2][0-9]|3[0-1])-(2[0-3]|[01][0-9]):([0-5][0-9]):([0-5][0-9])\\.\\d{3}");
		Matcher m=DATE_PATTERN.matcher(clOrdID); 
		assertTrue(m.matches());
	}
	
	@Test
	public void getNextNumber_generatedNumber(){
		int number = FixHelper.getNextNumber();
		assertTrue(number>0);
	}
	
	@Test
	public void convertFulluserstrToMessage_getAnAppMessage() throws XPathExpressionException, InvalidMessage, FieldNotFound{
		String fulluserstr = "8=FIX.4.2|35=RIO|9=2492|49=COMET|50=CASH_COES_INSTANCE10|143=ORT|";
		Message msg=fixHelper.convertFulluserstrToMessage(null, fulluserstr);
		assertTrue(msg.isApp());
		String msgType=msg.getHeader().getString(35);
		assertEquals(msgType,"RIO");
	}
	@Test
	public void convertFulluserstrToMessage_runTimeException() throws XPathExpressionException, InvalidMessage{
		String fulluserstr = "8=FIX.4.2|35=RIO|9=2492|37|49=COMET|50=CASH_COES_INSTANCE10|143=ORT|";
		try{
			Message msg=fixHelper.convertFulluserstrToMessage(null, fulluserstr);
		}catch (RuntimeException e) {
			assertTrue(e.getMessage().contains("symbol '=' not found in tag-value pair '37' in message"));
		}
	}
	@Test
	public void convertFulluserstrToMessage_passWithRepeattags() throws XPathExpressionException, InvalidMessage{
		String fulluserstr = "8=FIX.4.2|35=RIO|9=2492|37=123344|37=ABC|49=COMET|50=CASH_COES_INSTANCE10|143=ORT|";
		Message msg = fixHelper.convertFulluserstrToMessage(null, fulluserstr);
		assertTrue(msg.toString().contains("37=ABC"));
		assertTrue(!msg.toString().contains("37=123344"));
	}
	@Test
	public void convertFulluserstrToMessage_passWithRepeatEmptytags() throws XPathExpressionException, InvalidMessage{
		String fulluserstr = "8=FIX.4.2|35=RIO|9=2492|37=123344|37=|49=COMET|50=CASH_COES_INSTANCE10|143=ORT|";
		Message msg = fixHelper.convertFulluserstrToMessage(null, fulluserstr);
		assertTrue(!msg.toString().contains("37="));
	}
	@Test
	public void convertUserstrToMessage_getAnAppMessage() throws XPathExpressionException, InvalidMessage, FieldNotFound{
		String template = "ReportCancelPartial";
		String userString = "10202=USCASH|49=Cfore";
		String extraTags = "11=123";
		Message msg=fixHelper.convertUserstrToMessage(null, template,userString, extraTags);
		assertTrue(msg.isApp());
		String msgType=msg.getHeader().getString(35);
		assertEquals(msgType,"NewSingle");
	}

	@Test
	public void convertRawstrToUserstr_exceptedField(){
		String userStr =fixHelper.convertRawstrToUserstr(rawMsg);
		assertTrue(userStr.contains("MsgType=NewSingle"));
		assertTrue(userStr.contains("|"));
	}

	@Test
	public void checkMessageContainsUserstr_noException() throws InvalidMessage {
		String userStr =fixHelper.convertRawstrToUserstr(rawMsg);
		try {
			fixHelper.checkMessageContainsUserstr(null, actualMessage, userStr);
		} catch (XPathExpressionException e) {
			assertTrue(e.getMessage()==null);
		} catch (FieldNotFound e) {
			assertTrue(e.getMessage()==null);
		}
	}
	
	@Test
	public void checkMessageContainsUserstr_messageIncorrectException() throws InvalidMessage {
		String userStr =fixHelper.convertRawstrToUserstr(rawMsg+"123=5");
		try {
			fixHelper.checkMessageContainsUserstr(null, actualMessage, userStr);
		} catch (XPathExpressionException e) {
			assertTrue(e.getMessage()==null);
		} catch (MessageIncorrect e) {
			String exception = e.getMessage();
			assertTrue(exception.contains("(123) is missing: expected value: '5'"));
		} catch (FieldNotFound e) {
			assertTrue(e.getMessage()==null);
		}
	}
	@Test
	public void checkMessageNotContainsUserstr_noException() throws InvalidMessage{
		String userStr ="25,333,109999";
		try {
			fixHelper.checkMessageNotContainsUserstr(null, actualMessage, userStr);
		} catch (XPathExpressionException e) {
			assertTrue(e.getMessage()==null);
		}
	}
	
	@Test
	public void checkMessageNotContainsUserstr_runtimeException() throws InvalidMessage{
		String userStr ="9871";
		try {
			fixHelper.checkMessageNotContainsUserstr(null, actualMessage, userStr);
		} catch (XPathExpressionException e) {
			assertTrue(e.getMessage()==null);
		}catch (RuntimeException e) {
			String exception = e.getMessage();
			assertTrue(exception.contains("tag 9871 should not be present in the message"));		}
	}
	@Test
	public void setOrderBook_jsonException() throws Throwable{
	   IFixMessagingAgent fixMsgAgent = Mockito.mock(IFixMessagingAgent.class);
	   String raw= "8=FIX.4.29=249235=NewSingle11=12345649=COMET50=CASH_COES_INSTANCE10143=ORT52=20180608-16:41:06.2348054=510962=LIST10964=18130363atu10965=7011019=N11346=0.011347=0.010372=A100232=50310890=0.09871=181303624e410=000";
	   Message actualMessage = fixHelper.createFixMessage(raw);
	   FixStepResult stepResult= new FixStepResult(actualMessage, fixHelper);
	   when(fixMsgAgent.sendMessage(scenarioContext, "ReportCancelPartial_Clear_Buy", null, "11=123456|49=COMET|50=CASH_COES_INSTANCE10|143=ORT|52=20180608-16:41:06.234|8054=5|10962=LIST|10964=18130363atu|10965=70|11019=N|11346=0.0|11347=0.0|10372=A|100232=503|10890=0.0|9871=181303624e4|10=000|")).thenReturn(stepResult); 
	   when(fixMsgAgent.sendMessage(scenarioContext, "ReportCancelPartial_Clear_Sell", null, "11=123456|49=COMET|50=CASH_COES_INSTANCE10|143=ORT|52=20180608-16:41:06.234|8054=5|10962=LIST|10964=18130363atu|10965=70|11019=N|11346=0.0|11347=0.0|10372=A|100232=503|10890=0.0|9871=181303624e4|10=000|")).thenReturn(stepResult); 
	   String userStr  = "11=123456|49=COMET|50=CASH_COES_INSTANCE10|143=ORT|52=20180608-16:41:06.234|8054=5|10962=LIST|10964=18130363atu|10965=70|11019=N|11346=0.0|11347=0.0|10372=A|100232=503|10890=0.0|9871=181303624e4|10=000|";
	   ObjectMapper objectMapper = Mockito.mock(ObjectMapper.class);
	   whenNew(ObjectMapper.class).withAnyArguments().thenReturn(objectMapper);
	   HashMap<String, List<Integer>> book = new HashMap<String, List<Integer>>();
	   book.put("true", new ArrayList<Integer>(Arrays.asList(0, 4, 8, 9, 12)));
	   when(objectMapper.readValue("true", new TypeReference<HashMap<String, List<Integer>>>() {})).thenReturn(book); 
	   try{
	   fixHelper.setOrderBook(scenarioContext, fixMsgAgent, "ReportCancelPartial", "true", userStr);
	   }catch (JsonMappingException e) {
			String exception = e.getMessage();		
	   }
	}
	@Test
	public void getMessageFieldValue_returnExceptedValue() throws XPathExpressionException, FieldNotFound, InvalidMessage{
		String msgType = fixHelper.getMessageFieldValue("35", actualMessage);
		String tagvalue = fixHelper.getMessageFieldValue("9871", actualMessage);
		assertEquals("NewSingle", msgType);
		assertEquals("181303624e4", tagvalue);

	}
	@Test
	public void compareMessage_exceptionWithCheckAdditionalTags() throws XPathExpressionException, InvalidMessage, FieldNotFound{
		String str1  = "8=FIX.4.2|35=RIO|9=2492|49=COMET|50=CASH_COES_INSTANCE10|143=ORT|11=123|37=456";
		String str2  = "8=FIX.4.2|35=RIO|9=2492|49=COMET|50=CASH_COES_INSTANCE10|143=ORT|11=123";
		quickfix.Message actualMessage = fixHelper.convertFulluserstrToMessage(null, str1);
		quickfix.Message expectedMessage = fixHelper.convertFulluserstrToMessage(null, str2);
		try{
			fixHelper.compareMessage(actualMessage, expectedMessage, true);
		}
		catch(MessageIncorrect e){
			assertTrue(e.getMessage().contains("Tag '37' is additional tag in actual message"));
		}
	}
	@Test
	public void compareMessage_exceptionWithWorngFateFormat() throws XPathExpressionException, InvalidMessage, FieldNotFound{
		String str1  = "8=FIX.4.2|35=RIO|9=2492|49=COMET|50=CASH_COES_INSTANCE10|143=ORT|11=123|52=20180608-16:41:06.234|";
		String str2  = "8=FIX.4.2|35=RIO|9=2492|49=COMET|50=CASH_COES_INSTANCE10|143=ORT|11=123|52=/(\\d+)-(\\d+)-(\\d+)-(\\d+)/|";
		quickfix.Message actualMessage = fixHelper.convertFulluserstrToMessage(null, str1);
		quickfix.Message expectedMessage = fixHelper.convertFulluserstrToMessage(null, str2);
		try {
			fixHelper.compareMessage(actualMessage, expectedMessage, false);
		} catch (MessageIncorrect e) {
			assertTrue(e.getMessage().contains("Tag SendingTime (52) has incorrect format: expected format '(\\d+)-(\\d+)-(\\d+)-(\\d+)' vs actual value '20180608-16:41:06.234'"));
		}
	}
	@Test
	public void findMessage_getExceptedMessageWithTag35() throws InvalidMessage, FieldNotFound, XPathExpressionException{
		String expectedMsg = "8=FIX.4.2|35=NewSingle|8054=5";
		Message expectedMessage=fixHelper.convertFulluserstrToMessage(null, expectedMsg);
		Message found = fixHelper.findMessage(receivedMessages, expectedMessage);
		assertEquals(receiveMessage2.toString(), found.toString());
		
	}

	@Test
	public void findMessage_getExceptedMessageWithTag11() throws InvalidMessage, FieldNotFound, XPathExpressionException{
		String expectedMsg = "8=FIX.4.2|143=ORT|11=2/20181210-1103|10962=LIST";
		Message expectedMessage=fixHelper.convertFulluserstrToMessage(null, expectedMsg);
		Message found = fixHelper.findMessage(receivedMessages, expectedMessage);
		assertEquals(receiveMessage2.toString(), found.toString());
		
	}
	
	@Test
	public void findMessage_getExceptedMessageWithTag37() throws InvalidMessage, FieldNotFound, XPathExpressionException{
		String expectedMsg = "8=FIX.4.2|143=ORT|37=189236074|10962=LIST";
		Message expectedMessage=fixHelper.convertFulluserstrToMessage(null, expectedMsg);
		Message found = fixHelper.findMessage(receivedMessages, expectedMessage);
		assertEquals(receiveMessage2.toString(), found.toString());
		
	}
	
	@Test
	public void findMessage_getExceptedMessageWithTag38() throws InvalidMessage, FieldNotFound, XPathExpressionException{
		String expectedMsg = "8=FIX.4.2|143=ORT|38=1000.0|10962=LIST";
		Message expectedMessage=fixHelper.convertFulluserstrToMessage(null, expectedMsg);
		Message found = fixHelper.findMessage(receivedMessages, expectedMessage);
		assertEquals(receiveMessage2.toString(), found.toString());
		
	}
	
	@Test
	public void findMessage_noFoundMessageException() throws InvalidMessage, FieldNotFound, XPathExpressionException{		
		String expectedMsg = "8=FIX.4.2|143=ABC|10962=LIST";
		Message expectedMessage=fixHelper.convertFulluserstrToMessage(null, expectedMsg);
		try{
			Message found = fixHelper.findMessage(receivedMessages, expectedMessage);
		}catch (MessageIncorrect e) {
			String exception = e.getMessage();
			assertTrue(exception.contains("Tag TargetLocationID (143) has incorrect value: expected value 'ABC' vs actual value 'ORT"));
		 }
		}
	
	@Test
	public void saveResult_saveResultUnderTempleteAndVariable() throws Exception{
		FixStepResult fixStepResult = new FixStepResult(actualMessage,fixHelper);
		ScenarioContext scenarioContext= new ScenarioContext("test");
		EvalScope evalScope = new EvalScope();
		Whitebox.setInternalState(scenarioContext, "_evalScope", evalScope);
		StepResult lastStepResult=null;
		Whitebox.setInternalState(scenarioContext, "_lastStepResult", lastStepResult);
		fixHelper.saveResult(fixStepResult, scenarioContext, "templateName", "@SaveResult");
		assertEquals(scenarioContext.getVariable("@templateName").toString(),scenarioContext.getVariable("@SaveResult").toString());
	}
	
	@Test
	public void saveResult_saveResultWithoutTemplete() throws Throwable{
		FixStepResult fixStepResult = new FixStepResult(actualMessage,fixHelper);
		fixHelper.saveResult(fixStepResult, scenarioContext, null, "@SaveResult");
		assertEquals(scenarioContext.getVariable("@SaveResult").getFieldValue("10962"),"LIST");
	}
	
	@Test
	public void receiveAndVerifyMessage_foundExceptedMessage() throws XPathExpressionException, FieldNotFound, ValidationFailed, InvalidMessage, InterruptedException{
		String template = "ReportCancelPartial";
		String userString = "11=2/20181210-1103<<>>String<<>>Equal|10965=70<<>>String<<>>Equal";
		String extraTags = "10372=A<<>>String<<>>CONTAIN";
		FixStepResult fixStepResult =fixHelper.receiveAndVerifyMessage(receivedMessages, scenarioContext, template,"@SaveReceive", userString,extraTags);
		assertEquals(fixStepResult.getFieldValue("10962"),"LIST");

	}
	

	@Test
	public void receiveAndVerifyMessage_receiveMessageEmptyException() throws XPathExpressionException, FieldNotFound, ValidationFailed, InvalidMessage, InterruptedException{
		String template = "ReportCancelPartial";
		String userString = "11=2/20181210-1103<<>>String<<>>Equal|10965=70<<>>String<<>>Equal";
		String extraTags = "10372=A<<>>String<<>>CONTAIN";
		ArrayList<Message> testReceivedMessages = new ArrayList<Message>();
		try {
			FixStepResult fixStepResult = fixHelper.receiveAndVerifyMessage(
					testReceivedMessages, scenarioContext, template,
					"@SaveReceive", userString, extraTags);
		} catch (ValidationFailed e) {
			assertTrue(e.getMessage().contains(
					"Searched 0 messages in the buffer:"));
		}
	}
	
	@Test
	public void receiveAndVerifyMessage_noFounMessageException() throws XPathExpressionException, FieldNotFound, ValidationFailed, InvalidMessage, InterruptedException{
		String template = "ReportCancelPartial";
		String userString = "11=3/20181210-1103<<>>String<<>>Equal|10965=70<<>>String<<>>Equal";
		String extraTags = "10372=A<<>>String<<>>Equal";
		try {
			FixStepResult fixStepResult = fixHelper.receiveAndVerifyMessage(
					receivedMessages, scenarioContext, template,
					"@SaveReceive", userString, extraTags);
		} catch (ValidationFailed e) {
			assertTrue(e.getMessage().contains(
					"ERROR: Missing message matching pattern"));
		}
	}
	
	@Test
	public void receiveMessageAndSaveResult_foundAndSaveResult() throws Throwable{
		String expectedMsg = "8=FIX.4.2|143=ORT|38=1000.0|10962=LIST";
		Message expectedMessage=fixHelper.convertFulluserstrToMessage(null, expectedMsg);
		FixStepResult fixStepResult = fixHelper.receiveMessageAndSaveResult(receivedMessages, scenarioContext, "anyone", "@ReceiveSave", expectedMessage);
		assertEquals(scenarioContext.getVariable("@ReceiveSave").getFieldValue("10962"),"LIST");
		assertEquals(scenarioContext.getVariable("@ReceiveSave").toString(),fixStepResult.getActualMessage());
	}
	@Test
	public void receiveMessageAndSaveResult_noFoundMessage() throws Throwable{
		String expectedMsg = "8=FIX.4.2|143=ABC|38=1000.0|10962=LIST";
		Message expectedMessage=fixHelper.convertFulluserstrToMessage(null, expectedMsg);
		try{
		FixStepResult fixStepResult = fixHelper.receiveMessageAndSaveResult(receivedMessages, scenarioContext, "anyone", "@ReceiveSave", expectedMessage);
		}catch (MessageIncorrect e) {
			String exception = e.getMessage();
			assertTrue(exception.contains("Tag TargetLocationID (143) has incorrect value: expected value 'ABC' vs actual value 'ORT"));
		 }
	}
	@Test
	public void checkReceived_failOnAdditionalTags() throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException, InvalidMessage, InterruptedException, FieldNotFound, XPathExpressionException{
		Whitebox.setInternalState(fixHelper, "failOnAdditionalTags", true);
		String expectedMsg = "8=FIX.4.2|143=ORT|11=2/20181210-1103|10962=LIST";
		Message exceptedMessage=fixHelper.convertFulluserstrToMessage(null, expectedMsg);
		try{
        Message msg =  fixHelper.checkReceived(receivedMessages, exceptedMessage);
		}catch (MessageIncorrect e) {
			String exception = e.getMessage();
			assertTrue(exception.contains("Tag '49' is additional tag in actual message"));
			assertTrue(exception.contains("Tag '8054' is additional tag in actual message"));
		 }	
	}
	
	@Test
	public void checkReceived_noFoundMessage() throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException, InvalidMessage, InterruptedException, FieldNotFound, XPathExpressionException{
		String expectedMsg = "8=FIX.4.2|143=ORT|11=4/20181210-1103|37=123|10962=LIST";
		Message exceptedMessage=fixHelper.convertFulluserstrToMessage(null, expectedMsg);
		try{
        Message msg =  fixHelper.checkReceived(receivedMessages, exceptedMessage);
		}catch (MessageMissing_ReplayException e) {
			String exception = e.getMessage();
			assertTrue(exception.contains("Identity:[11=4/20181210-1103|37=123]"));
		 }
	}
	@Test
	public void updateFilePathInDictionary_updateDictionary() throws FieldConvertError, ConfigError{
	  paramsMap.put("DataDictionary", System.getProperty("user.dir")+"/src/test/resources/fast/common/fix/resources/FIX42.xml");
      quickfix.Dictionary d = new quickfix.Dictionary("AllSettings", paramsMap);
      String config_folder = mockconfig.getConfigFolder();
      FixHelper.updateFilePathInDictionary(d, config_folder); 
      assertTrue(d.getString("DataDictionary").contains("FIX42.xml"));
	}
	@Test
	public void checkReceived_findExceptedMessage() throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException, InvalidMessage, InterruptedException, FieldNotFound, XPathExpressionException{
		Whitebox.setInternalState(fixHelper, "failOnAdditionalTags", true);
		String expectedMsg = "8=FIX.4.2|143=ORT|11=4/20181210-1103|10962=LIST";
		Message exceptedMessage=fixHelper.convertFulluserstrToMessage(null, expectedMsg);
		receivedMessages.add(exceptedMessage);
        Message msg =  fixHelper.checkReceived(receivedMessages, exceptedMessage);
        assertTrue(msg.toString().contains("8=FIX.4.29=38143=ORT11=4/20181210-110310962=LIST"));
	}
	@Test
	public void createFixMessage_noFoundtag35Exception() throws InvalidMessage{
		String expectedMsg = "8=FIX.4.2143=ABC38=1000.010962=LIST";
		try{
		fixHelper.createFixMessage(expectedMsg);
		}catch (InvalidMessage e) {
			String exception = e.getMessage();
			assertTrue(exception.contains("Field [35] was not found in message"));
		 }
	}
	@Test
	public void createFixMessage_noFoundEndSOHException() throws InvalidMessage{
		String expectedMsg = "8=FIX.4.235=NewSingle143=ABC38=1000.010962=LIST";
		try{
		fixHelper.createFixMessage(expectedMsg);
		}catch (InvalidMessage e) {
			String exception = e.getMessage();
			assertTrue(exception.contains("SOH not found at end of field"));
		 }
	}
	@Test
	public void createFixMessage_exceptedOneValueForRepeatTags() throws InvalidMessage{
		String expectedMsg = "8=FIX.4.235=RIO143=ABC143=ORT38=1000.010962=LIST";
		Message msg = fixHelper.createFixMessage(expectedMsg);
		assertTrue(msg.toString().contains("143=ORT"));
		assertTrue(!msg.toString().contains("143=ABC"));
	}
	
	@Test
	public void removeDuplicateNonRepeatingGroupTags_passWithoutDupTAg(){
		String removeRawMsg = fixHelper.removeDuplicateNonRepeatingGroupTags(rawMsg);
		assertEquals(removeRawMsg, rawMsg);
	}
	@Test
	public void removeDuplicateNonRepeatingGroupTags_passWithDupTAg() throws Exception{
		Set<String> set = new HashSet<>(Arrays.asList("52"));
		Whitebox.setInternalState(fixHelper, "_dupTagsToRemoveInEms", set);
		String rawMsgWithDuptag= "8=FIX.4.29=249235=NewSingle49=COMET50=CASH_COES_INSTANCE10143=ORT52=20180708-16:41:06.11152=20180608-16:41:06.2348054=510962=LIST10964=18130363atu10965=7011019=N11346=0.011347=0.010372=A100232=50310890=0.09871=181303624e410=000";
		String removeRawMsg = fixHelper.removeDuplicateNonRepeatingGroupTags(rawMsgWithDuptag);
		assertTrue(removeRawMsg.contains("52=20180708-16:41:06.111"));
		assertTrue(!removeRawMsg.contains("52=20180608-16:41:06.234"));
	}
	
	
	@Test
	public void removeDuplicateNonRepeatingGroupTags_runtimeExceptionWithEmptytag(){
		Set<String> set = new HashSet<>(Arrays.asList("52"));
		Whitebox.setInternalState(fixHelper, "_dupTagsToRemoveInEms", set);
		String rawMsgWithDuptag= "8=FIX.4.29=249235=NewSingle49=COMET50=CASH_COES_INSTANCE10143=ORT52=20180708-16:41:06.11152=20180608-16:41:06.2348054=510962=LIST10964=18130363atu10965=7011019=N11346=0.011347=0.010372=A100232=50310890=0.09871=181303624e4117=10=000";
		try{
		String removeRawMsg = fixHelper.removeDuplicateNonRepeatingGroupTags(rawMsgWithDuptag);
		}catch(RuntimeException e){
			assertTrue(e.getMessage().contains("empty tag: '117=' in"));
		}
	
	}
	@Test
	public void validate_passWithTypeAndOperator() throws XPathExpressionException, InvalidMessage, FieldNotFound, ValidationFailed{
		String result = "8=FIX.4.2|35=RIO|9=2492|49=COMET|57=CASH_COES_INSTANCE|143=ORT|";
		String userString= "143=ORT<<>>String<<>>contain|49=COMET<<>>String<<>>Equal";
		String logs = fixHelper.validate(null, result, "ReportCancelPartial", userString);
		assertTrue(logs.contains("Actual:8=FIX.4.2|9=46|35=RIO|49=COMET|57=CASH_COES_INSTANCE|143=ORT|10=132|"));
		assertTrue(logs.contains("Excepted:35=NewSingle|49=COMET|57=CASH_COES_INSTANCE|143=ORT"));
	}
	
	@Test
	public void validate_passWithTypeAndOperatorAndTag8() throws XPathExpressionException, InvalidMessage, FieldNotFound, ValidationFailed{
		String result = "8=FIX.4.2|35=RIO|9=2492|49=COMET|57=CASH_COES_INSTANCE|143=ORT|";
		String userString= "8=FIX.4.2|143=ORT<<>>String<<>>contain|49=COMET<<>>String<<>>Equal";
		String logs = fixHelper.validate(null, result, "ReportCancelPartial", userString);
		assertTrue(logs.contains("Actual:8=FIX.4.2|9=46|35=RIO|49=COMET|57=CASH_COES_INSTANCE|143=ORT|10=132|"));
		assertTrue(logs.contains("Excepted:35=NewSingle|49=COMET|57=CASH_COES_INSTANCE|143=ORT"));
	}
	
	@Test
	public void validate_excepteionWithTypeAndOperator() throws XPathExpressionException, InvalidMessage, FieldNotFound, ValidationFailed{
		String result = "8=FIX.4.2|35=RIO|9=2492|49=COMET|57=CASH_COES_INSTANCE|143=ORT|";
		String userString= "143=test<<>>String<<>>contain|49=PTE<<>>String<<>>Equal";
		try{
			String logs = fixHelper.validate(null, result, "ReportCancelPartial", userString);
		} catch (ValidationFailed e) {
			assertTrue(e.getMessage().contains(
					"49=(COMET)(PTE)(EQUAL)"));
			assertTrue(e.getMessage().contains(
					"143=(ORT)(test)(CONTAIN)"));
		}
	}
	
	@Test
	public void validateMessageParts_withDefault() throws XPathExpressionException, InvalidMessage, ClassNotFoundException, FieldNotFound, ValidationFailed, NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException{

    	final Field fieldA = fast.common.fix.FixHelper.class.getDeclaredField( "validateByEqual" );
        fieldA.setAccessible( true );
        fieldA.setBoolean(false,true);
		StringBuilder diffBuilder= new StringBuilder();	
		String str1  = "8=FIX.4.2|35=RIO|9=2492|49=COMET|50=CASH_COES_INSTANCE10|143=ORT|11=123|37=345";
		String str2  = "8=FIX.4.2|35=RIO|9=2492|49=COMET|50=CASH_COES_INSTANCE10|143=ORT|11=123|37=456";
		quickfix.Message actualMessage = fixHelper.convertFulluserstrToMessage(null, str1);
		quickfix.Message expectedMessage = fixHelper.convertFulluserstrToMessage(null, str2);
		MapMessageTemplate mapMessageTemplate = Mockito.mock(MapMessageTemplate.class);
		when(mapMessageTemplate.getValidateField(Mockito.any(String.class))).thenReturn(null);
		FixHelper.validateMessageParts(actualMessage, expectedMessage, mapMessageTemplate, diffBuilder);
		assertTrue(diffBuilder.toString().contains("37=(345)(456)(EQUAL)"));
	}
	
	
}
