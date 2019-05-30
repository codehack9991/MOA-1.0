package fast.common.glue;

import static org.junit.Assert.*;

import java.util.Collection;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import cucumber.api.Scenario;
import fast.common.context.ScenarioContextManager;

import fast.common.logging.FastLogger;

public class TestBaseCommonStepDefs {

	private Scenario scenario;

	@Before
	public void setUp() throws Exception {
		scenario = new Scenario() {

			private String log = "failed";		

			@Override
			public void write(String text) {
				log = text;
			}

			@Override
			public boolean isFailed() {
				return log.contains("failed");
			}

			@Override
			public String getStatus() {
				return null;
			}

			@Override
			public Collection<String> getSourceTagNames() {
				return null;
			}

			@Override
			public String getName() {
				return "scenario";
			}

			@Override
			public String getId() {
				return "123";
			}

			@Override
			public void embed(byte[] data, String mimeType) {

			}
		};
	}

	@After
	public void tearDown() throws Exception {
		ScenarioContextManager.getInstance().close();
	}

	@Test
	public void testBeforeScenario() throws Exception {
		BaseCommonStepDefs stepDefs = new BaseCommonStepDefs();
		stepDefs.beforeScenario(scenario);
		boolean started = stepDefs.runtimeInfoManager.isScenarioStarted(scenario);
		assertTrue(started);
	}

	@Test
	public void testAfterScenario() throws Exception {
		BaseCommonStepDefs stepDefs = new BaseCommonStepDefs();
		stepDefs.beforeScenario(scenario);
		stepDefs.afterScenario(scenario);
		boolean ended = stepDefs.runtimeInfoManager.isScenarioEnded(scenario);
		assertTrue(ended);
	}

	@Test
	public void testScenarioAndLogWrite() throws Exception {
		BaseCommonStepDefs stepDefs = new BaseCommonStepDefs();
		stepDefs.beforeScenario(scenario);
		String log = "test passed";
		stepDefs.scenarioAndLogWrite(log);
		boolean failed = scenario.isFailed();
		assertFalse(failed);
	}
	
	@Test
	public void testScenarioOutlineLoggerContextIsSet() throws Exception {
		FastLogger logger = FastLogger.getLogger("testScenarioOutlineLoggerContextIsSet");
		BaseCommonStepDefs stepDefs = new BaseCommonStepDefs();
		scenario = new Scenario() {

			private String log = "failed";		

			@Override
			public void write(String text) {
				log = text;
			}

			@Override
			public boolean isFailed() {
				return log.contains("failed");
			}

			@Override
			public String getStatus() {
				return null;
			}

			@Override
			public Collection<String> getSourceTagNames() {
				return null;
			}

			@Override
			public String getName() {
				return "scenario";
			}

			@Override
			public String getId() {
				return "feature;scenario;;2";
			}

			@Override
			public void embed(byte[] data, String mimeType) {

			}
		};
		stepDefs.beforeScenario(scenario);
		assertEquals("Feature name is not correct", "feature", logger.getThreadContextValue("featureName"));
		assertEquals("Scenario name is not correct", "scenario", logger.getThreadContextValue("scenarioName"));
		assertEquals("Test name is not correct", "feature_scenario_2", logger.getThreadContextValue("testName"));
	}
	
	@Test
	public void testScenarioLoggerContextIsSet() throws Exception {
		FastLogger logger = FastLogger.getLogger("testScenarioOutlineLoggerContextIsSet");
		BaseCommonStepDefs stepDefs = new BaseCommonStepDefs();
		scenario = new Scenario() {

			private String log = "failed";		

			@Override
			public void write(String text) {
				log = text;
			}

			@Override
			public boolean isFailed() {
				return log.contains("failed");
			}

			@Override
			public String getStatus() {
				return null;
			}

			@Override
			public Collection<String> getSourceTagNames() {
				return null;
			}

			@Override
			public String getName() {
				return "scenario";
			}

			@Override
			public String getId() {
				return "feature;scenario";
			}

			@Override
			public void embed(byte[] data, String mimeType) {

			}
		};
		stepDefs.beforeScenario(scenario);
		assertEquals("Feature name is not correct", "feature", logger.getThreadContextValue("featureName"));
		assertEquals("Scenario name is not correct", "scenario", logger.getThreadContextValue("scenarioName"));
		assertEquals("Test name is not correct", "feature_scenario", logger.getThreadContextValue("testName"));
		assertEquals("Short Test name is not correct", "feature_scenario", logger.getThreadContextValue("testName"));
	}
	
	@Test
	public void testScenarioLoggerContextIsSetWithLongName() throws Exception {
		FastLogger logger = FastLogger.getLogger("testScenarioOutlineLoggerContextIsSet");
		BaseCommonStepDefs stepDefs = new BaseCommonStepDefs();
		scenario = new Scenario() {

			private String log = "failed";		

			@Override
			public void write(String text) {
				log = text;
			}

			@Override
			public boolean isFailed() {
				return log.contains("failed");
			}

			@Override
			public String getStatus() {
				return null;
			}

			@Override
			public Collection<String> getSourceTagNames() {
				return null;
			}

			@Override
			public String getName() {
				return "Test scenario for long name";
			}

			@Override
			public String getId() {
				return "Feature for long name;Test scenario for long name";
			}

			@Override
			public void embed(byte[] data, String mimeType) {

			}
		};
		stepDefs.beforeScenario(scenario);
		assertEquals("Feature name is not correct", "Feature for long name", logger.getThreadContextValue("featureName"));
		assertEquals("Scenario name is not correct", "Test scenario for long name", logger.getThreadContextValue("scenarioName"));
		assertEquals("Test name is not correct", "Feature_for_long_name_Test_scenario_for_long_name", logger.getThreadContextValue("testName"));
		assertEquals("Short Test name is not correct", "Feature_fo_Test_scena", logger.getThreadContextValue("shortTestName"));
	}
}
