package fast.common.reporting;

import static org.junit.Assert.*;

import java.util.Collection;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import cucumber.api.Scenario;

public class TestRuntimeInfoManager {

	private Scenario scenario;

	@Before
	public void setUp() throws Exception {

		scenario = new Scenario() {
			@Override
			public void write(String text) {
			}

			@Override
			public boolean isFailed() {
				return false;
			}

			@Override
			public String getStatus() {
				return "passed";
			}

			@Override
			public Collection<String> getSourceTagNames() {
				return null;
			}

			@Override
			public String getName() {
				return "TestScenario";
			}

			@Override
			public String getId() {
				return "0001";
			}

			@Override
			public void embed(byte[] data, String mimeType) {
			}
		};

	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testGetInstance() {
		RuntimeInfoManager instance = RuntimeInfoManager.INSTANCE;

		assertNotNull(instance);
	}

	@Test
	public void testInstanceIsSingleton() {
		RuntimeInfoManager expected = RuntimeInfoManager.INSTANCE;
		RuntimeInfoManager actual = RuntimeInfoManager.INSTANCE;
		assertEquals(expected, actual);
	}

	@Test
	public void testGetScenarioRuntimeInfoNum() {
		RuntimeInfoManager instance = RuntimeInfoManager.INSTANCE;
		int num = instance.getScenarioRuntimeInfoNum();
		assertNotEquals(0, num);
	}

	@Test
	public void isScenarioStarted_DefaultValueShouldBeFalse() {
		RuntimeInfoManager instance = RuntimeInfoManager.INSTANCE;
		boolean started = instance.isScenarioStarted(scenario);
		assertFalse(started);
	}

	@Test
	public void isScenarioEnded_DefaultValueShouldBeTrue() {
		RuntimeInfoManager instance = RuntimeInfoManager.INSTANCE;
		boolean ended = instance.isScenarioEnded(scenario);
		assertTrue(ended);
	}

	@Test
	public void testSaveReportToFile() throws Exception {
		RuntimeInfoManager instance = RuntimeInfoManager.INSTANCE;
		instance.notifyScenarioStart(scenario, null);
		boolean scenarioStarted = instance.isScenarioStarted(scenario);
		;
		assertTrue(scenarioStarted);
		instance.notifyScenarioEnd(scenario, null);
		boolean scenarioEnded = instance.isScenarioEnded(scenario);
		assertTrue(scenarioEnded);
		instance.saveReportToFile(scenario);
	}

}
