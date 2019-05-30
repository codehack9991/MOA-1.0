package fast.common.glue;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import cucumber.api.Scenario;
import cucumber.api.java.After;
import cucumber.api.java.Before;
import cucumber.api.java.en.Then;
import fast.common.logging.FastLogger;
/**
 * The {@code DataValidationCommonStepDefs} class defines some basic methods for data validation
 * The basic methods include: greater, equal, contains...
 * @author QA Framework Team
 * @since 1.5
 */
public class DataValidationCommonStepDefs extends BaseCommonStepDefs {
	private static FastLogger logger = FastLogger.getLogger("DataValidationCommonStepDefs");

	@Before
	public void beforeScenario(Scenario scenario) throws Exception {
		super.beforeScenario(scenario);
	}

	@After
	public void afterScenario(Scenario scenario) throws Exception {
		super.afterScenario(scenario);
	}
	/**
	 * <p>Checks variable A whether contains variable B.
	 * 
	 * @param strA variable A
	 * @param strB variable B
	 * @return true if A contains B, false otherwise
	 * <p>Pattern :
     * <blockquote><pre>@Then("^\"([^\"]*)\" contains \"([^\"]*)\"$")</pre></blockquote>
	 * <p>Example : 
	 * <blockquote><pre>Then "@strA.Value" contains "@strB.Value"</pre></blockquote>
     * <blockquote><pre>Then "Welcome to FAST community" contains "FAST"</pre></blockquote>
     * @since 1.5
	 */
	@Then("^\"([^\"]*)\" contains \"([^\"]*)\"$")
	public boolean checkContains(String strA, String strB) throws Throwable {
		String processedValueA = getScenarioContext().processString(strA);
		String processedValueB = getScenarioContext().processString(strB);
		scenarioAndLogWrite("StringA:" + processedValueA + ", StringB:" + processedValueB);
		assertThat(processedValueA, containsString(processedValueB));
		return processedValueA.contains(processedValueB);
	}
	/**
	 * <p>Checks variable A whether does not contain variable B.
	 * 
	 * @param strA variable A
	 * @param strB variable B
	 * @return true if A does not contain B, false otherwise
	 * <p>Pattern :
     * <blockquote><pre>@Then("^\"([^\"]*)\" does not contains \"([^\"]*)\"$")</pre></blockquote>
	 * <p>Example : 
     * <blockquote><pre>Then "@strA.Value" does not contains "@strB.Value"</pre></blockquote>
     * @since 1.5
	 */
	@Then("^\"([^\"]*)\" does not contain \"([^\"]*)\"$")
	public boolean checkNotContains(String strA, String strB) throws Throwable {
		String processedValueA = getScenarioContext().processString(strA);
		String processedValueB = getScenarioContext().processString(strB);
		scenarioAndLogWrite("StringA:" + processedValueA + ", StringB:" + processedValueB);
		assertThat(processedValueA, not(containsString(processedValueB)));
		return !processedValueA.contains(processedValueB);
	}
	/**
	 * <p>Checks variable A whether equals to variable B.
	 * 
	 * @param strA a source variable
	 * @param strB a target variable
	 * @return true if A equals to B, false otherwise
	 * <p>Pattern :
     * <blockquote><pre>@Then("^\"([^\"]*)\" equals to \"([^\"]*)\"$")</pre></blockquote>
	 * <p>Example : 
     * <blockquote><pre>Then "@strA.Value" equals to "@strB.Value"</pre></blockquote>
     * @since 1.5
	 */
	@Then("^\"([^\"]*)\" equals to \"([^\"]*)\"$")
	public boolean checkEquals(String strA, String strB) {
		String processedValueA = getScenarioContext().processString(strA);
		String processedValueB = getScenarioContext().processString(strB);
		scenarioAndLogWrite("Actual:" + processedValueA + ", Expected:" + processedValueB);
		assertEquals(processedValueB, processedValueA);
		return processedValueA.equals(processedValueB);
	}
	/**
	 * <p>Checks variable A whether does not equal to variable B.
	 * 
	 * @param strA variable A
	 * @param strB variable B
	 * @return true if A is not equal to B, false otherwise
	 * <p>Pattern :
     * <blockquote><pre>@Then("^\"([^\"]*)\" does not equal to \"([^\"]*)\"$")</pre></blockquote>
	 * <p>Example : 
     * <blockquote><pre>Then "@strA.Value" does not equal to "@strB.Value"</pre></blockquote>
     * @since 1.5
	 */
	@Then("^\"([^\"]*)\" does not equal to \"([^\"]*)\"$")
	public boolean checkNotEquals(String strA, String strB) {
		String processedValueA = getScenarioContext().processString(strA);
		String processedValueB = getScenarioContext().processString(strB);
		scenarioAndLogWrite("Actual:" + processedValueA + ", Expected:" + processedValueB);
		assertNotEquals(processedValueB, processedValueA);
		return !processedValueA.equals(processedValueB);
	}
	/**
	 * <p>Checks a variable is empty
	 * 
	 * @param str the variable to check
	 * @return true if the variable is empty, false otherwise
	 * <p>Pattern :
     * <blockquote><pre>@Then("^\"([^\"]*)\" is null$")</pre></blockquote>
	 * <p>Example : 
     * <blockquote><pre>Then "@str.Value" is null</pre></blockquote>
     * @since 1.5
	 */
	@Then("^\"([^\"]*)\" is null$")
	public boolean checkNull(String str) {
		String processedValue = getScenarioContext().processString(str);
		scenarioAndLogWrite("Value:" + processedValue);
		assertTrue(processedValue.isEmpty());
		return processedValue.isEmpty();
	}
	/**
	 * <p>Checks variable A whether is greater than variable B.
	 * 
	 * @param strNumA variable A
	 * @param strNumB variable B
	 * @return true if Aã€€is greater, false otherwise
	 * <p>Pattern :
     * <blockquote><pre>@Then("^\"([^\"]*)\" is greater than \"([^\"]*)\"$")</pre></blockquote>
	 * <p>Example : 
     * <blockquote><pre>Then "@numA.Value" is greater than "@numB.Value"</pre></blockquote>
	 */
	@Then("^\"([^\"]*)\" is greater than \"([^\"]*)\"$")
	public boolean checkGreater(String strNumA, String strNumB) {
		Double numA = ConvertStringToDouble(strNumA);
		Double numB = ConvertStringToDouble(strNumB);
		assertTrue(numA > numB);
		return numA > numB;
	}
	/**
	 * <p>Checks variable A whether is less than variable B.
	 * 
	 * @param strNumA variable A
	 * @param strNumB variable B
	 * @return true if Aã€€is less, false otherwise
	 * <p>Pattern :
     * <blockquote><pre>@Then("^\"([^\"]*)\" is less than \"([^\"]*)\"$")</pre></blockquote>
	 * <p>Example : 
     * <blockquote><pre>Then "@numA.Value" is less than "@numB.Value"</pre></blockquote>
	 */
	@Then("^\"([^\"]*)\" is less than \"([^\"]*)\"$")
	public boolean checkLess(String strNumA, String strNumB) {
		Double numA = ConvertStringToDouble(strNumA);
		Double numB = ConvertStringToDouble(strNumB);
		assertTrue(numA < numB);
		return numA < numB;
	}

	private Double ConvertStringToDouble(String str) {
		String processedValue = getScenarioContext().processString(str);
		scenarioAndLogWrite("Number is:" + processedValue);
		Double number = Double.parseDouble(processedValue);
		return number;
	}
	/**
	 * <p>Checks time variable A whether is earlier than time variable B
	 * 
     * @param strA variable A
	 * @param strB variable B
	 * @param stringFormatter time format
	 * @return true or false, true if Aã€€is earlier, false otherwise
	 * <p>Pattern :
     * <blockquote><pre>@Then("^\"([^\"]*)\" is earlier than \"([^\"]*)\" with time format \"([^\"]*)\"$")</pre></blockquote>
	 * <p>Example : 
     * <blockquote><pre>Then "20180725-11:40:01" is earlier than "20180725-11:50:35" with time format "yyyyMMdd-HH:mm:ss"</pre></blockquote>
	 */
	@Then("^\"([^\"]*)\" is earlier than \"([^\"]*)\" with time format \"([^\"]*)\"$")
	public void checkEarlier(String strA, String strB, String stringFormatter) {
		ZonedDateTime zDateTimeA = converStringToTime(strA, stringFormatter);
		ZonedDateTime zDateTimeB = converStringToTime(strB, stringFormatter);
		assertTrue(zDateTimeA.compareTo(zDateTimeB) < 0);
	}
	/**
	 * <p>Checks time variable A whether is later than time variable B
	 * 
     * @param strA variable A
	 * @param strB variable B
	 * @param stringFormatter time format
	 * @return true or false, true if Aã€€is later, false otherwise
	 * <p>Pattern :
     * <blockquote><pre>@Then("^\"([^\"]*)\" is later than \"([^\"]*)\" with time format \"([^\"]*)\"$")</pre></blockquote>
	 * <p>Example : 
     * <blockquote><pre>Then "20180725-11:40:35.123" is later than "0180725-11:40:01.123" with time format "yyyyMMdd-HH:mm:ss.SSS"</pre></blockquote>
	 */
	@Then("^\"([^\"]*)\" is later than \"([^\"]*)\" with time format \"([^\"]*)\"$")
	public void checkLater(String strA, String strB, String stringFormatter) {
		ZonedDateTime zDateTimeA = converStringToTime(strA, stringFormatter);
		ZonedDateTime zDateTimeB = converStringToTime(strB, stringFormatter);
		assertTrue(zDateTimeA.compareTo(zDateTimeB) > 0);
	}
	
	private ZonedDateTime converStringToTime(String str, String stringFormatter) {
		String processedValue = getScenarioContext().processString(str);
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern(stringFormatter).withZone(ZoneId.of("UTC"));
		scenarioAndLogWrite("Time is:" + processedValue);
		return ZonedDateTime.parse(processedValue, formatter);
	}

}
