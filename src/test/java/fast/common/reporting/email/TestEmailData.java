package fast.common.reporting.email;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

public class TestEmailData {

	private static final String FEATURE_NAME = "Feature1";
	private EmailData emailData;

	@Before
	public void setUp() throws Exception {
		emailData = new EmailData(FEATURE_NAME);
	}

	@Test
	public void testfeatureNameGetterSetter() {
		String featureName = emailData.getFeatureName();
		assertEquals(FEATURE_NAME, featureName);
		String newFeatureName = "Feature2";
		emailData.setFeatureName(newFeatureName);
		String actual = emailData.getFeatureName();
		assertEquals(newFeatureName, actual);
	}

	@Test
	public void testScenarioResultGetterSetter() {
		String result = "passed";
		emailData.setScenarioResult(result);
		String actual = emailData.getScenarioResult();
		assertEquals(result, actual);
	}

	@Test
	public void testScenarioNumGetterSetter() {
		int num = 123;
		emailData.setScenarioNum(num);
		int actual = emailData.getScenarioNum();
		assertEquals(num, actual);
	}

	@Test
	public void testPassedScenarioGetterSetter() {
		int passedScenarios = 10;
		emailData.setPassedScenario(passedScenarios);
		int actual = emailData.getPassedScenario();
		assertEquals(passedScenarios, actual);
	}

	@Test
	public void testFailedScenarioGetterSetter() {
		int faildScenario = 0;
		emailData.setFaildScenario(faildScenario);
		int actual = emailData.getFaildScenario();
		assertEquals(faildScenario, actual);
	}

}
