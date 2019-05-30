package fast.common.glue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import cucumber.api.Scenario;
import fast.common.context.ScenarioContext;

import static org.junit.Assert.*;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

import java.util.Collection;



@RunWith(PowerMockRunner.class)
@PowerMockIgnore("javax.management.*")
@PrepareForTest(BaseCommonStepDefs.class)
public class TestDataValidationCommonStepDefs {
	
	private DataValidationCommonStepDefs dataValid;
	
	@Mock
	private BaseCommonStepDefs base;
	
	@Mock
	private ScenarioContext scenarioContext;
	
	private Scenario scenario;
	
	@Before
	public void setUp() throws Exception {
		dataValid = new DataValidationCommonStepDefs();
		scenario = new Scenario() {
			
			@Override
			public void write(String text) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public boolean isFailed() {
				// TODO Auto-generated method stub
				return false;
			}
			
			@Override
			public String getStatus() {
				// TODO Auto-generated method stub
				return null;
			}
			
			@Override
			public Collection<String> getSourceTagNames() {
				// TODO Auto-generated method stub
				return null;
			}
			
			@Override
			public String getName() {
				// TODO Auto-generated method stub
				return "invalidName";
			}
			
			@Override
			public String getId() {
				// TODO Auto-generated method stub
				return "invalidId";
			}
			
			@Override
			public void embed(byte[] data, String mimeType) {
				// TODO Auto-generated method stub
				
			}
		};
		PowerMockito.mockStatic(BaseCommonStepDefs.class);
		when(BaseCommonStepDefs.getScenarioContext()).thenReturn(scenarioContext);
	}
	
	@Test
	public void testBeforeScenario() throws Exception {
		dataValid.beforeScenario(scenario);
	}

	@Test
	public void testAfterScenario() throws Exception {
		dataValid.beforeScenario(scenario);
		dataValid.afterScenario(scenario);
	}
	
	@Test
	public void testCheckContains() throws Throwable{
		when(scenarioContext.processString("testStrA")).thenReturn("abc");
		when(scenarioContext.processString("testStrB")).thenReturn("a");
		assertTrue(dataValid.checkContains("testStrA", "testStrB"));
	}
	
	@Test
	public void testCheckNotContains() throws Throwable{
		when(scenarioContext.processString("testStrA")).thenReturn("abc");
		when(scenarioContext.processString("testStrB")).thenReturn("d");
		assertTrue(dataValid.checkNotContains("testStrA", "testStrB"));
	}
	
	
	@Test
	public void testCheckEquals() throws Throwable{
		when(scenarioContext.processString("testStrA")).thenReturn("abc");
		when(scenarioContext.processString("testStrB")).thenReturn("abc");
		assertTrue(dataValid.checkEquals("testStrA", "testStrB"));
	}
	
	@Test
	public void testCheckNotEquals() throws Throwable{
		when(scenarioContext.processString("testStrA")).thenReturn("abc");
		when(scenarioContext.processString("testStrB")).thenReturn("def");
		assertTrue(dataValid.checkNotEquals("testStrA", "testStrB"));
	}
	
	
	@Test
	public void testCheckNull() throws Throwable{
		when(scenarioContext.processString("testStrA")).thenReturn("");
		assertTrue(dataValid.checkNull("testStrA"));
	}
	
	@Test
	public void testCheckGreater(){
		when(scenarioContext.processString("testStrA")).thenReturn("100");
		when(scenarioContext.processString("testStrB")).thenReturn("99");
		assertTrue(dataValid.checkGreater("testStrA", "testStrB"));
	}
	
	@Test
	public void testCheckLess(){
		when(scenarioContext.processString("testStrA")).thenReturn("99");
		when(scenarioContext.processString("testStrB")).thenReturn("100");
		assertTrue(dataValid.checkLess("testStrA", "testStrB"));
	}
	
	@Test
	public void testCheckEarlier(){
		when(scenarioContext.processString("testStrA")).thenReturn("20180725-11:40:01");
		when(scenarioContext.processString("testStrB")).thenReturn("20180725-11:50:35");
		dataValid.checkEarlier("testStrA", "testStrB","yyyyMMdd-HH:mm:ss");
	}
	
	@Test
	public void testCheckLater(){
		when(scenarioContext.processString("testStrA")).thenReturn("20180725-11:50:35");
		when(scenarioContext.processString("testStrB")).thenReturn("20180725-11:40:01");
		dataValid.checkLater("testStrA", "testStrB","yyyyMMdd-HH:mm:ss");
	}
	
}
