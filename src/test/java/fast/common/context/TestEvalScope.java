package fast.common.context;

import static org.junit.Assert.assertEquals;
import static org.testng.Assert.assertEquals;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import fast.common.cipher.AES;


public class TestEvalScope {

	EvalScope evalScope;
	
	@Rule
	public ExpectedException throwns = ExpectedException.none();

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
		evalScope = new EvalScope();
		AES.SetSecretKey("1234567812345678");
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testProcessPassword() throws Throwable {

		String password = evalScope.processString("3b e6 38 4e 76 3b a7 c2 13 d9 f1 d5 52 99 eb 3b ");
		assertEquals("passcode", password);

		String stringWithSinglePassword = evalScope
				.processString("abc;#3b e6 38 4e 76 3b a7 c2 13 d9 f1 d5 52 99 eb 3b ;xyz");
		assertEquals("abc;passcode;xyz", stringWithSinglePassword);

		String stringWithMultiPassword = evalScope.processString(
				"abc;#3b e6 38 4e 76 3b a7 c2 13 d9 f1 d5 52 99 eb 3b ;xyz#3b e6 38 4e 76 3b a7 c2 13 d9 f1 d5 52 99 eb 3b ");
		assertEquals("abc;passcode;xyzpasscode", stringWithMultiPassword);

		// string
		String processString = evalScope.processString("CheckBox_uploadToDashboard");
		assertEquals("CheckBox_uploadToDashboard", processString);

	}

	@Test
	public void testProcessVariables() throws Throwable {
		CommonStepResult stepResult = new CommonStepResult();
		stepResult.setFieldValue("variablestest");
		evalScope.saveVar("@Variables", stepResult);
		assertEquals("variablestest", evalScope.processString("@Variables.Value"));

	}

	@Test
	public void testProcessParams() throws Throwable {
		Map<String, ArrayList<String>> _threadParams = new HashMap<>();
		ArrayList<String> paramValues = new ArrayList<>();
		paramValues.add("2");
		_threadParams.put("params", paramValues);
		evalScope.setThreadParams(_threadParams, 1);
		assertEquals("2", evalScope.processString("$params"));

	}

	@Test
	public void testProcessScripts() throws Throwable {
		assertEquals("8", evalScope.processString("%3+5%"));
		
		assertEquals("8.0", evalScope.processString("%3 + Mod(55, 10)%"));
	}

	@Test
	public void testComplexeString() throws Throwable {
		assertEquals("123@citi.com", evalScope.processString("[[123@citi.com]]"));

		CommonStepResult stepResult = new CommonStepResult();
		stepResult.setFieldValue("tony");
		evalScope.saveVar("@Variable", stepResult);
		assertEquals("tony", evalScope.processString("@Variable.Value"));
		
		assertEquals("This value tony 123@citi.com",
				evalScope.processString("This value @Variable.Value [[123@citi.com]]"));
		assertEquals("This value tony 123@citi.com",
				evalScope.processString("This value @Variable.Value [[123@citi]].com"));
	}
	
	@Test
	public void testProcessString_VariableValueContainsReservedCharacter() throws Throwable {
		CommonStepResult stepResult = new CommonStepResult();
		stepResult.setFieldValue("123@citi.com");
		evalScope.saveVar("@Variable", stepResult);
		assertEquals("123@citi.com", evalScope.processString("@Variable.Value"));
		
		assertEquals("The email address is 123@citi.com",
				evalScope.processString("The email address is @Variable.Value"));
		
		assertEquals("The email address is 123@citi.com",
				evalScope.processString("%'The email address is ' + '@Variable.Value'%"));
		
		assertEquals("The email address is @Variable.Value",
				evalScope.processString("The email address is [[@Variable.Value]]"));
	}
	
	@Test
	public void testProcessStringReturnNull(){
		assertEquals(null,evalScope.processString(null));
	}

	@Test
	public void testPattern(){
		String s3 = "sgf[[xxx]]]aslghl[[sdsf[[wegs]]]]245[[sef]]456";
		String patt = "\\[\\[([^\\[\\]]+|\\[\\[([^\\[\\]]+)*\\]\\])*\\]\\]";
		Pattern patt1 = Pattern.compile(patt);
		Matcher m1 = patt1.matcher(s3);
		int start = 0;
		int end = 0;
		while (m1.find()) {
			end = s3.indexOf(m1.group());
			System.out.println(s3.substring(start, end));
			start = end + m1.group().length();
			System.out.println(m1.group().substring(2,m1.group().length()-2));
		}
	}
	
	@Test
	public void testSpecialCharater() throws Throwable {

		CommonStepResult stepResult = new CommonStepResult();
		stepResult.setFieldValue("hello@tony");
		evalScope.saveVar("@Variable", stepResult);
	
		assertEquals("hello@tony", evalScope.processString("@Variable.Value"));

		assertEquals("This value hello@tony 123@citi.com", evalScope.processString("This value @Variable.Value [[123@citi.com]]"));
	}
	
	@Test
	public void testGetPrivateKeyFilePath() throws Exception{
		evalScope.setPrivateKeyFilePath("testPath");
		assertEquals("testPath",evalScope.getPrivateKeyFilePath());
	}
	
	@Test
	public void updateParamsWithExpection() throws Exception{
		throwns.expect(Exception.class);
		throwns.expectMessage("Param or ThreadParam 'test' is not defined and due to this cannot be updated");
		evalScope.updateParam("test", new Object());
	}

}
