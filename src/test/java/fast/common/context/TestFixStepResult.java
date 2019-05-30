package fast.common.context;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.mockito.Mockito.when;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.doNothing;

import org.powermock.reflect.Whitebox;

import fast.common.fix.FixHelper;
import quickfix.Message;

@RunWith(MockitoJUnitRunner.class)
public class TestFixStepResult {
	
	private FixStepResult fixStepResult = null;
	private quickfix.Message actualMessage = null;
	private ArrayList<quickfix.Message> actualMessageList;
	
	@Mock
	FixHelper fixHelper;

	@Before
	public void setup(){
		
	}
	
	@After
	public void tearDown(){
		
	}
	
	@Test
	public void constructWithMessageAndFixHelper(){
		fixStepResult = new FixStepResult(actualMessage, fixHelper);
		assertEquals(fixHelper,Whitebox.getInternalState(fixStepResult,"_fixHelper"));
	}
	
	@Test
	public void constructWithMessageListAndFixHelper(){
		fixStepResult = new FixStepResult(actualMessageList, fixHelper);
		assertEquals(fixHelper,Whitebox.getInternalState(fixStepResult,"_fixHelper"));
	}
	
	@Test
	public void testGetFieldValue() throws Exception{
		fixStepResult = new FixStepResult(actualMessage, fixHelper);
		when(fixHelper.getMessageFieldValue("testField", actualMessage)).thenReturn("test get field value");
		assertEquals("test get field value",fixStepResult.getFieldValue("testField"));
	}
	
	@Test
	public void testGetFieldsValues() throws Exception{
		actualMessageList = new ArrayList<>();
		Message test = new Message();
		actualMessageList.add(test);
		ArrayList<String> testList = new ArrayList<>();
		testList.add("testList");
		when(fixHelper.getMessageFieldValue(anyString(),any())).thenReturn("testList");
		fixStepResult = new FixStepResult(actualMessageList, fixHelper);
		assertEquals(testList,fixStepResult.getFieldsValues("test get fields values"));
	}
	
	@Test
	public void testValidate() throws Exception{
		fixStepResult = new FixStepResult(actualMessage, fixHelper);
		when(fixHelper.validate(any(), anyString(), anyString(), anyString())).thenReturn("test validate");
		assertEquals("test validate", fixStepResult.validate(null, "varName", "template", "userstr"));
	}
	
	@Test
	public void testGetActualMessage(){
		actualMessage = new Message();
		fixStepResult = new FixStepResult(actualMessage, fixHelper);
		assertEquals("9=0|10=167|",fixStepResult.getActualMessage());
	}
	
	@Test
	public void testContainsWithScenarioContextAndUserstr() throws Exception{
		doNothing().when(fixHelper).checkMessageContainsUserstr(any(), any(), anyString());
		fixStepResult = new FixStepResult(actualMessage, fixHelper);
		fixStepResult.contains(null,"userstr");
	}
	
	@Test
	public void testContainsWithUserstr() throws Exception{
		doNothing().when(fixHelper).checkMessageContainsUserstr(any(), any(), anyString());
		fixStepResult = new FixStepResult(actualMessage, fixHelper);
		fixStepResult.contains("userstr");
	}
	
	@Test
	public void testNotContains() throws Throwable{
		doNothing().when(fixHelper).checkMessageNotContainsUserstr(any(), any(), anyString());
		fixStepResult = new FixStepResult(actualMessage, fixHelper);
		fixStepResult.not_contains("userstr");
	}
	
	@Test
	public void testToString(){
		fixStepResult = new FixStepResult(actualMessage, fixHelper);
		assertEquals("<Empty FixStepResult!>",fixStepResult.toString());
	}
	
	@Test
	public void testToStringActualMessageNoNull(){
		actualMessage = new Message();
		fixStepResult = new FixStepResult(actualMessage, fixHelper);
		assertEquals("9=0|10=167|",fixStepResult.toString());
	}
	
	@Test
	public void testToStringActualMessageListNoNull(){
		actualMessageList = new ArrayList<>();
		Message test = new Message();
		actualMessageList.add(test);
		ArrayList<String> testList = new ArrayList<>();
		testList.add("testList");
		fixStepResult = new FixStepResult(actualMessageList, fixHelper);
		assertEquals("9=0|10=167|\r\n",fixStepResult.toString());
	}
	
}
