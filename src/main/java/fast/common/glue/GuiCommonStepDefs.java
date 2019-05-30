package fast.common.glue;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

import cucumber.api.Scenario;
import cucumber.api.java.After;
import cucumber.api.java.Before;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import fast.common.agents.AgentsManager;
import fast.common.context.StepResult;
import fast.common.context.UiaStepResult;
import fast.common.logging.FastLogger;
/**
 * The {@code GuiCommonStepDefs} class defines some common actions for Desktop GUI and Web Browser GUI agents, which can be used in different scenarios.
 * @author QA Framework Team
 * @since 1.5
 */
public class GuiCommonStepDefs extends BaseCommonStepDefs{
	private static FastLogger logger = FastLogger.getLogger("GuiCommonStepDefs");
	
	@Before
	public void beforeScenario(Scenario scenario) throws Exception {
		super.beforeScenario(scenario);
	}

	@After
	public void afterScenario(Scenario scenario) throws Exception {
		super.afterScenario(scenario);
	}
	
	/**
	 * <p>Sets the time out duration for the corresponding agent.
	 * 
     * @param  agentName name of agent on which to run the step
     * @param  timeout timeout duration in seconds
     * <p>Pattern :
     * <blockquote><pre>@When("^(\\w+) set timeout (\\d+) seconds$")</pre></blockquote>
     * <p>Example : 
     * <blockquote><pre>When WebAgent set timeout 5 seconds</pre></blockquote>
     * @see fast.common.agents.WebBrowserAgent#setTimeout(int) 
     * @see fast.common.agents.UiaAgent#setTimeout(int)
     * @since 1.5
     */
	@When("^(\\w+) set timeout (\\d+) seconds$")
	public void setTimeout(String agentName, int timeout) throws Throwable {
		AgentsManager.getInstance().getOrCreateAgent(agentName).run("setTimeout", timeout);
	}


	/**
	 * <p>Opens the specified web page with the given url, specially for the web agent.
	 * <p>This method is only available on web browser agent now</p>
     * @param  agentName the name of agent on which to run the step
     * @param  url the address of the web page to open
     * <p>Pattern :
     * <blockquote><pre>@When("^(\\w+) open \"([^\"]*)\" url$")</pre></blockquote>
     * <p>Example : 
     * <blockquote><pre>When WebAgent open "https://www.wikipedia.org" url</pre></blockquote>
     * 
     * @see fast.common.agents.WebBrowserAgent#openUrl(String)
     * @since 1.5
     */

	@When("^(\\w+) open \"([^\"]*)\" url$")
	public void openUrl(String agentName, String url) throws Throwable {
		String processedUrl = getScenarioContext().processString(url);
		AgentsManager.getInstance().getOrCreateAgent(agentName).run("openUrl", processedUrl);
	}
	
	/**
	 * <p>Gets url of the current web page, specially for the web agent.
	 * <p>This method is only available on web browser agent now</p>
     * @param  agentName the name of agent on which to run the step
     * @return the address of the current page
     *           
     * <p>Pattern :
     * <blockquote><pre>@When("^(\\w+) get current url$")</pre></blockquote>
     * <p>Example : 
     * <blockquote><pre>Then WebAgent get current url</pre></blockquote>
     * @see fast.common.agents.WebBrowserAgent#getCurrentUrl()
     * @since 1.5
     */

	@Then("^(\\w+) get current url$")
    public String getCurrentUrl(String agentName) throws Throwable {       
        StepResult result = AgentsManager.getInstance().getOrCreateAgent(agentName).runWithResult("getCurrentUrl");
		String url = result.getFieldValue(StepResult.DEFAULT_FIELD_VALUE);
		logger.info("Current Url: [" + url + "]");
		return url;
    }

	/**
	 * <p>Sets the current web page
	 * <p>This method is only available on web browser agent now</p>
     * @param  agentName the name of agent on which to run the step
     * @param  pageName the name of the page to set as current page, referenced in web repository.
     *           
     * <p>Pattern :
     * <blockquote><pre>@Then("^(\\w+) (am|is) on ([\\w\\.]+)")</pre></blockquote>
     * <p>Example : 
     * <blockquote><pre>Then WebAgent is on SearchPage</pre></blockquote>
     * 
     * @since 1.5
     * @see fast.common.agents.WebBrowserAgent#onPage(String)
     */
	@Then("^(\\w+) (am|is) on ([\\w\\.]+)")
	public void onPage(String agentName, String verbNotUsed, String pageName) throws Throwable {
		String processedPageName = getScenarioContext().processString(pageName);
		AgentsManager.getInstance().getOrCreateAgent(agentName).run("onPage", processedPageName);
	}


	/**
	 * <p>Checks whether the specified control can be found in the current runtime  
	 * <p>This method is only available on web browser agent now</p>
     * @param  agentName the name of agent on which to run the step
     * @param  controlName the name of the control to check, referenced in the GUI repository.
     * <p>Pattern:
     * <blockquote><pre>@Then("^(\\w+) see ([\\w\\.]+)")</pre></blockquote>
     * <p>Example : 
     * <blockquote><pre>Then WebAgent see Button_Add</pre></blockquote>
     * 
     * @since 1.5
     * @see fast.common.agents.WebBrowserAgent#seeControl(String)
     */
	@Then("^(\\w+) see ([\\w\\.]+)")
	public void seeControl(String agentName, String controlName) throws Throwable {
		String processedControlName = getScenarioContext().processString(controlName);
		AgentsManager.getInstance().getOrCreateAgent(agentName).run("seeControl", processedControlName);
	}


	/**
	 * <p>Left click on the specified control 
	 * 
     * @param  agentName the name of agent on which to run the step
     * @param  controlName the name of the control to click on, referenced in the GUI repository.
     * <p>Pattern :
     * <blockquote><pre>@When("^(\\w+) click on ([\\w\\.]+)")</pre></blockquote>
     * <p>Example : 
     * <blockquote><pre>When DesktopAgent click on Calculator_Button_Add</pre></blockquote>
     * 
     * @since 1.5
     * @see fast.common.agents.WebBrowserAgent#clickControl(String)
     * @see fast.common.agents.UiaAgent#clickControl(String)
     */
	@When("^(\\w+) click on ([\\w\\.]+)")
	public void clickControl(String agentName, String controlName) throws Throwable {
		String processedControlName = getScenarioContext().processString(controlName);
		AgentsManager.getInstance().getOrCreateAgent(agentName).run("clickControl", processedControlName);		
	}

	/**
	 * <p>Types the given text into the specified control
	 * 
     * @param  agentName the name of agent on which to run the step
     * @param  text the text to type
     * @param  controlName the name of the control to type text into, referenced in the GUI repository.
     *           
     * <p>Pattern :
     * <blockquote><pre>@When("^(\\w+) type \"([^\"]*)\" into ([\\w\\.]+)")</pre></blockquote>
     * <p>Example : 
     * <blockquote><pre>And WebAgent type "Cucumber" into SearchTextbox</pre></blockquote>
     * 
     * @since 1.5
     * @see fast.common.agents.WebBrowserAgent#typeTextIntoControl(String, String)
     * @see fast.common.agents.UiaAgent#typeTextIntoControl(String, String)
     * @see fast.common.agents.LeanftAgent#typeTextIntoControl(String, String)
     */
	@When("^(\\w+) type \"([^\"]*)\" into ([\\w\\.]+)")
	public void typeTextIntoControl(String agentName, String text, String controlName) throws Throwable {
		String processedText = getScenarioContext().processString(text);
		String processedControlName = getScenarioContext().processString(controlName);
		AgentsManager.getInstance().getOrCreateAgent(agentName).run("typeTextIntoControl", processedText,
				processedControlName);
	}
	/**
	 * <p>Types the given date into the specified control
	 * <p>This method is only available on UIA Agent now</p>
     * @param  agentName the name of agent on which to run the step
     * @param  date the text to type
     * @param  controlName the name of the control to type text into, referenced in the GUI repository.
     *           
     * <p>Pattern :
     * <blockquote><pre>@When("^(\\w+) set date \"([^\"]*)\" into ([\\w\\.]+)")</pre></blockquote>
     * <p>Example : 
     * <blockquote><pre>And UIAagent set date "01/03/2020" into datebox</pre></blockquote>
     * 
     * @since 1.7
     * @see fast.common.agents.UiaAgent#setDateIntoControl(String, String)
     */
	@When("^(\\w+) set date \"([^\"]*)\" into ([\\w\\.]+)")
	public void setDateIntoControl(String agentName, String date, String controlName) throws Throwable {
		String processedDate = getScenarioContext().processString(date);
		String processedControlName = getScenarioContext().processString(controlName);
		AgentsManager.getInstance().getOrCreateAgent(agentName).run("setDateIntoControl", processedDate,
				processedControlName);
	} 

	/**
	 * <p>Checks whether the given text is visible in the specified control
	 * 
     * @param  agentName the name of agent on which to run the step
     * @param  text the text to check
     * @param  controlName the name of the control on which to check the text, referenced in the GUI repository.
     *           
     * <p>Pattern :
     * <blockquote><pre>@When("^(\\w+) see \"([^\"]*)\" in ([\\w\\.]+)")</pre></blockquote>
     * <p>Example : 
     * <blockquote><pre>When DesktopAgent see "5" in Calculator_Text_Result</pre></blockquote>
     * 
     * @since 1.5
     * @see fast.common.agents.WebBrowserAgent#seeTextInControl(String, String)
     * @see fast.common.agents.UiaAgent#seeTextInControl(String, String)
     * @see fast.common.agents.LeanftAgent#seeTextInControl(String, String)
     */
	@When("^(\\w+) see \"([^\"]*)\" in ([\\w\\.]+)")
	public void seeTextInControl(String agentName, String text, String controlName) throws Throwable {
		String processedText = getScenarioContext().processString(text);
		String processedControlName = getScenarioContext().processString(controlName);
		AgentsManager.getInstance().getOrCreateAgent(agentName).run("seeTextInControl", processedText,
				processedControlName);
	}


	/**
	 * <p>Launches the specified application with the given path
     * @param  agentName the name of agent on which to run the step
     * @param  appPath the path of the application to launch
     *          
     * <p>Pattern :
     * <blockquote><pre>@Given("^(\\w+) launch desktop GUI \"([^\"]*)\"")</pre></blockquote>
     * <p>Example : 
     * <blockquote><pre>Given DesktopAgent launch desktop GUI "C:\windows\system32\calc.exe"</pre></blockquote>
     * 
     * @since 1.5
     * @see fast.common.agents.UiaAgent#launchGui(String)
     * @see fast.common.agents.LeanftAgent#launchGui(String)
     */
	@Given("^(\\w+) launch desktop GUI \"([^\"]*)\"")
	public void launchDesktopGUI(String agentName, String appPath) throws Throwable {
		AgentsManager.getInstance().getOrCreateAgent(agentName).run("launchGui", appPath);

	}
	@Given("Start Leanft Agent")
	public void startLeanftAgent() throws Throwable {
		AgentsManager.getInstance().getOrCreateAgent("LeanftAgent").run("startLeanftAgent");

	}
	
	/**
	 * <p>Presses the specified hot key during the runtime
	 * 
	 * @param agentName the name of agent on which to run the step
	 * @param keyName the name of the hot key to press
	 * 
	 * <p>Hot Key supported for now :
	 * <blockquote><pre>F1, F2, F3, ..., F12</pre></blockquote>
	 * <blockquote><pre>SHIFT, CONTROL, ALT, LEFT_ALT, RIGHT_ALT, RETURN</pre></blockquote>
	 * <blockquote><pre>RIGHT, BACKSPACE, LEFT, ESCAPE, TAB, HOME, END, UP</pre></blockquote>
	 * <blockquote><pre>DOWN, INSERT, DELETE, CAPS, PAGEUP, PAGEDOWN, PRINT</pre></blockquote>
	 * <blockquote><pre>PRINTSCREEN, SPACE, NUMLOCK, SCROLL, LWIN, RWIN</pre></blockquote>
	 * <blockquote><pre>CONTROL+A, CONTROL+SHIFT, CONTROL+C, CONTROL+F, CONTROL+ALT</pre></blockquote>

	 * <p>Pattern :
     * <blockquote><pre>@When("^(\\w+) press \"([^\"]*)\"$")</pre></blockquote>
     * <p>Example : 
     * <blockquote><pre>When DesktopAgent press "F6"</pre></blockquote>
     * 
     * @since 1.5
     * @see fast.common.agents.WebBrowserAgent#pressHotKey(String)
     * @see fast.common.agents.UiaAgent#pressHotKey(String)
     */
	@When("^(\\w+) press \"([^\"]*)\"$")
	public void pressHotKey(String agentName, String keyName) throws Throwable {
		AgentsManager.getInstance().getOrCreateAgent(agentName).run("pressHotKey", keyName);
	}
	
	/**
	 * <p>Presses the specified hot key on control during the runtime
	 * 
	 * @param agentName the name of agent on which to run the step
	 * @param keyName the name of the hot key to press
	 * @param controlName the name of the control to press keys on
	 * 
	 * <p>Pattern :
     * <blockquote><pre>@When("^(\\w+) press \"([^\"]*)\" on ([\\w\\.]+)$")</pre></blockquote>
     * <p>Example : 
     * <blockquote><pre>When WebAgent press "ENTER" on searchInput</pre></blockquote>
     * 
     * @since 1.7
     * @see fast.common.agents.WebBrowserAgent#pressHotKeyOnControl(String, String)
     */
	@When("^(\\w+) press \"([^\"]*)\" on ([\\w\\.]+)$")
	public void pressHotKeyOnControl(String agentName, String keyName, String controlName) throws Throwable {
		AgentsManager.getInstance().getOrCreateAgent(agentName).run("pressHotKeyOnControl", keyName, controlName);
	}
	
	/**
	 * <p>Presses the specified hot key by leanft agent
	 * 
     * @param  agentName the name of agent on which to run the step
     * @param  controlName the name of the hot key to press
     * <p>Pattern :
     * <blockquote><pre>@Then("^(\\w+) send key \"([^\"]*)\" to ([\\w\\.]+)")</pre></blockquote>
     * <p>Example : 
     * <blockquote><pre>Then LeanftAgent send key "RETURN" to Calculator.Text_Result</pre></blockquote>
     * 
     * @since 1.5
     * @see fast.common.agents.LeanftAgent#pressLftKey(String, String)
     */
	@When("^(\\w+) send key \"([^\"]*)\" to ([\\w\\.]+)")
	public void pressLftKey(String agentName, String keyName, String controlName) throws Throwable {
		AgentsManager.getInstance().getOrCreateAgent(agentName).run("pressLftKey", controlName, keyName);
	}
	/**
	 * <p>Lets the running agent to focus on the specified control
	 * <p>This method is only available on Uia agent now</p> 
     * @param  agentName the name of agent on which to run the step
     * @param  controlName the name of the control on which to focus, referenced in the GUI repository.
     *           
     * <p>Pattern :
     * <blockquote><pre>@When("^(\\w+) focus on ([\\w\\.]+)")</pre></blockquote>
     * <p>Example : 
     * <blockquote><pre>When DesktopAgent focus on RazorWindow</pre></blockquote>
     * 
     * @since 1.5
     * @see fast.common.agents.UiaAgent#focusControl(String)
     */
	@When("^(\\w+) focus on ([\\w\\.]+)")
	public void focusControl(String agentName, String controlName) throws Throwable {
		AgentsManager.getInstance().getOrCreateAgent(agentName).run("focusControl", controlName);
	}


	/**
	 * <p>Left click on the specified control without focusing on it
	 * <p>This method is only available on Uia agent now</p>
     * @param  agentName the name of agent on which to run the step
     * @param  controlName the name of the control to click on, referenced in the GUI repository.
     *           
     * <p>Pattern : 
     * <blockquote><pre>@When("^(\\w+) click unfocus on ([\\w\\.]+)")</pre></blockquote>
     * <p>Example : 
     * <blockquote><pre>When DesktopAgent click unfocus on Calculator_Button_Add</pre></blockquote>
     * 
     * @since 1.5
     * @see fast.common.agents.UiaAgent#clickControlUnfocus(String)
     */
	@When("^(\\w+) click unfocus on ([\\w\\.]+)")
	public void clickOnControlUnfocus(String agentName, String controlName) throws Throwable {
		AgentsManager.getInstance().getOrCreateAgent(agentName).run("clickControlUnfocus", controlName);
	}


	/**
	 * <p>Double click on the specified control 
     * @param  agentName the name of agent on which to run the step
     * @param  controlName the name of the control to double click on, referenced in the GUI repository.
     *           
     * <p>Pattern :
     * <blockquote><pre>@When("^(\\w+) double click on ([\\w\\.]+)")</pre></blockquote>
     * <p>Example : 
     * <blockquote><pre>When DesktopAgent double click on Calculator_Button_Add</pre></blockquote>
     * 
     * @since 1.5
     * @see fast.common.agents.WebBrowserAgent#doubleClickControl(String)
     * @see fast.common.agents.UiaAgent#doubleClickControl(String)
     * @see fast.common.agents.LeanftAgent#doubleClickControl(String)
     */
	@When("^(\\w+) double click on ([\\w\\.]+)")
	public void doubleClickOnControl(String agentName, String controlName) throws Throwable {
		AgentsManager.getInstance().getOrCreateAgent(agentName).run("doubleClickControl", controlName);
	}


	/**
	 * <p>Right click on the specified control 
     * @param  agentName the name of agent on which to run the step
     * @param  controlName the name of the control to right click on, referenced in the GUI repository.
     *           
     * <p>Pattern :
     * <blockquote><pre>@When("^(\\w+) right click on ([\\w\\.]+)")</pre></blockquote>
     * <p>Example : 
     * <blockquote><pre>When DesktopAgent right click on Calculator_Button_Add</pre></blockquote>
     * 
     * @since 1.5
     * @see fast.common.agents.WebBrowserAgent#rightClickControl(String)
     * @see fast.common.agents.UiaAgent#rightClickControl(String)
     * @see fast.common.agents.LeanftAgent#rightClickControl(String)
     */
	@When("^(\\w+) right click on ([\\w\\.]+)")
	public void rightClickOnControl(String agentName, String controlName) throws Throwable {
		AgentsManager.getInstance().getOrCreateAgent(agentName).run("rightClickControl", controlName);
	}


	/**
	 * <p>Selects an item by the given item value from the specified control
     * @param  agentName the name of agent on which to run the step
     * @param  itemName the value of the item to select
     * @param  controlName the name of the control to select item, referenced in the GUI repository.
     *           
     * <p>Pattern :
     * <blockquote><pre>@When("^(\\w+) select \"([^\"]*)\" from ([\\w\\.]+)")</pre></blockquote>
     * <p>Example : 
     * <blockquote><pre>When DesktopAgent select "USD" from List_Currency</pre></blockquote>
     * 
     * @since 1.5
     * @see fast.common.agents.WebBrowserAgent#selectItem(String, String)
     * @see fast.common.agents.UiaAgent#selectItem(String, String)
     * @see fast.common.agents.LeanftAgent#selectItem(String, String)
     */
	@When("^(\\w+) select \"([^\"]*)\" from ([\\w\\.]+)")
	public void selectItem(String agentName, String itemName, String controlName) throws Throwable {
		AgentsManager.getInstance().getOrCreateAgent(agentName).run("selectItem", controlName, itemName);
	}
	
	/**
	 * <p>Selects an item by the given item value from the specified control
	 * <p>This method is only available on WebBrowserAgent now</p>
     * @param  agentName the name of agent on which to run the step
     * @param  controlName the name of the control to deselect items, referenced in the GUI repository.
     *           
     * <p>Pattern :
     * <blockquote><pre>@When("^(\\w+) deselect all items from ([\\w\\.]+)")</pre></blockquote>
     * <p>Example : 
     * <blockquote><pre>When WebAgent deselect all items from DefinitionCMEAppSelect</pre></blockquote>
     * 
     * @since 1.7
     * @see fast.common.agents.WebBrowserAgent#deselectAllItems(String)
     */
	@When("^(\\w+) deselect all items from ([\\w\\.]+)")
	public void deselectAllItems(String agentName, String controlName) throws Throwable {
		AgentsManager.getInstance().getOrCreateAgent(agentName).run("deselectAllItems", controlName);
	}

	@Then("^(\\w+) get first selected value from ([\\w\\.]+)")
	public String getFirstSelectedValue(String agentName, String controlName) throws Throwable {
		StepResult result = AgentsManager.getInstance().getOrCreateAgent(agentName).runWithResult("getFirstSelectedValue", controlName);
		return result.getFieldValue(StepResult.DEFAULT_FIELD_VALUE);
	}
	
	/**
	 * <p>Sets the status of the specified check box with the flag given,
	 * flag = "true", select the check box; flag = "false", unselect the check box.
	 * 
     * @param  agentName the name of agent on which to run the step
     * @param  controlName the name of the control(check box) to set the status, referenced in the GUI repository
     * @param  flag  "true" or "false"
     * 
     * <p>Pattern :
     * <blockquote><pre>@When("^(\\w+) check box ([\\w\\.]+) with flag (\\w+)")</pre></blockquote>
     * <p>Example : 
     * <blockquote><pre>When DesktopAgent check box CheckBox_uploadToDashboard with flag "true"</pre></blockquote>
     * 
     * @since 1.5
     * @see fast.common.agents.WebBrowserAgent#checkBox(String, String)
     * @see fast.common.agents.UiaAgent#checkBox(String, String)
     * @see fast.common.agents.LeanftAgent#checkBox(String, String)
     * @see #checkCheckBox(String, String)
     * @see #uncheckCheckBox(String, String)
     */
	@When("^(\\w+) check box ([\\w\\.]+) with flag (\\w+)")
	public void checkBox(String agentName, String controlName, String flag) throws Throwable {
		String processedControlName = getScenarioContext().processString(controlName);
		AgentsManager.getInstance().getOrCreateAgent(agentName).run("checkBox", processedControlName, flag);
	}


	@When("^(\\w+) check on ([\\w\\.]*)")
	public void checkCheckBox(String agentName, String controlName) throws Throwable {
		String processedControlName = getScenarioContext().processString(controlName);
 		AgentsManager.getInstance().getOrCreateAgent(agentName).run("checkBox", processedControlName, "true");
	}
	
	@When("^(\\w+) uncheck on ([\\w\\.]*)")
	public void uncheckCheckBox(String agentName, String controlName) throws Throwable {
		String processedControlName = getScenarioContext().processString(controlName);
		AgentsManager.getInstance().getOrCreateAgent(agentName).run("checkBox", processedControlName, "false");
	}

	/**
	 * <p>Sets the status of the specified radio button with the flag given,
	 * flag = "true", select the radio button; flag = "false", unselect the check box.
	 * <p>This method is only available on UIA agent now</p>
     * @param  agentName the name of agent on which to run the step
     * @param  controlName the name of the control(check box) to set the status, referenced in the GUI repository
     * @param  flag  "true" or "false"
     * 
     * <p>Pattern :
     * <blockquote><pre>@When("^(\\w+) select radio button ([\\w\\.]+) with flag (\\w+)")</pre></blockquote>
     * <p>Example : 
     * <blockquote><pre>When DesktopAgent select radio button radionbutton_uploadToDashboard with flag "true"</pre></blockquote>
     * 
     * @since 1.7
     * @see fast.common.agents.UiaAgent#selectRadioButton(String, String)
     */
	@When("^(\\w+) select radio button ([\\w\\.]+) with flag (\\w+)")
	public void selectRadioButton(String agentName, String controlName, String flag) throws Throwable {
		String processedControlName = getScenarioContext().processString(controlName);
		AgentsManager.getInstance().getOrCreateAgent(agentName).run("selectRadioButton", processedControlName, flag);
	}
	/**
	 * Get a row index from a table by a specified cell value and the name|index of the column contains the cell value
	 * @param agentName  the name of agent on which to run the step
	 * @param controlName the name of the control 
	 * @param columnName the name of the column for UiaAgent, the index of the column for WebBrowserAgent
	 * @param columnValue the specified cell value
	 * @return row index
	 * @throws Throwable
	 * @since 1.5
	 * @see #getWebRowIndex(String, String, String, String, String)
	 * @see fast.common.agents.WebBrowserAgent#getRowIndex(String, String, String)
     * @see fast.common.agents.UiaAgent#getRowIndex(String, String, String)
	 */
	public String getRowIndex(String agentName, String controlName, String columnName, String columnValue)
			throws Throwable {
		StepResult result = AgentsManager.getInstance().getOrCreateAgent(agentName).runWithResult("getRowIndex",
				controlName, columnName, columnValue);
		return result.getFieldValue(StepResult.DEFAULT_FIELD_VALUE);
	}

	/**
	 * <p>Right clicks on the specified cell in the data grid
	 * <p>This method is only available on Uia agent now</p>
     * @param  agentName the name of agent on which to run the step
     * @param  cellPosition the index of the cell to click in the data grid, in the form of "RowIndex,ColumnIndex"
     * @param  controlName the name of the data grid in which to locate the cell, referenced in the GUI repository.
     * @since 1.5
     * @see fast.common.agents.UiaAgent#rightClickGridCell(String, String)
     */
	public void rightClickGridCell(String agentName, String cellPosition, String controlName) throws Throwable {
		AgentsManager.getInstance().getOrCreateAgent(agentName).runWithResult("rightClickGridCell", controlName,
				cellPosition);
	}
	/**
	 * <p>Set Value on the specified cell in the data grid
	 * <p>This method is only available on Uia agent now</p>
     * @param  agentName the name of agent on which to run the step
     * @param  value the value to set in the cell
     * @param  cellPosition the index of the cell to click in the data grid, in the form of "RowIndex,ColumnIndex"
     * @param  controlName the name of the data grid in which to locate the cell, referenced in the GUI repository.
     * <p>Pattern :
     * <blockquote><pre>@When("^(\\w+) set value \"([^\"]*)\" to cell \"([^\"]*)\" in data grid ([\\w\\.]+)")</pre></blockquote>
     * <p>Example : 
     * <blockquote><pre>When DesktopAgent set value "Test" to cell "0,0" in data grid Razor_Table</pre></blockquote>
     *
     * @since 1.7
     * @see fast.common.agents.UiaAgent#SetValueGridCell(String, String,String)
     */
	@When("^(\\w+) set value \"([^\"]*)\" to cell \"([^\"]*)\" in data grid ([\\w\\.]+)")
	public void SetValueGridCell(String agentName,String value, String cellPosition, String controlName) throws Throwable {
		String processedValue = getScenarioContext().processString(value);
		AgentsManager.getInstance().getOrCreateAgent(agentName).runWithResult("SetValueGridCell", controlName, processedValue,
				cellPosition);
	} 

	/**
	 * <p>Reads the text from the specified control and then save it into a variable 
	 * 
     * @param  agentName the name of agent on which to run the step
     * @param  controlName the name of the control to read text, referenced in the GUI repository
     * @param  varName the name of the variable which stores the result read from the control
     * 
     * <p>Pattern :
     * <blockquote><pre>@Then("^(\\w+) read text from (@?\\w+) into (@\\w+)$")</pre></blockquote>
     * <p>Example : 
     * <blockquote><pre>When DesktopAgent read text from Table_Head into @result</pre></blockquote>
     * @since 1.5
     * @see fast.common.agents.WebBrowserAgent#readTextOnControl(String)
     * @see fast.common.agents.UiaAgent#readTextOnControl(String)
     * @see fast.common.agents.LeanftAgent#readTextOnControl(String)
     */

	@Then("^(\\w+) read text from (@?\\w+) into (@\\w+)$")
	public void readTextOnControl(String agentName, String controlName, String varName) throws Throwable {
		String processControlName = (controlName.contains("@")) ? getScenarioContext()
				.processString(controlName + ".Value") : controlName;
		StepResult result = AgentsManager.getInstance().getOrCreateAgent(agentName).runWithResult("readTextOnControl",
				processControlName);
		assertNotNull(result);
		getScenarioContext().saveLastStepResult(result, varName);
		scenarioAndLogWrite(String.format("Save text %s into variable %s", result.getFieldValue(StepResult.DEFAULT_FIELD_VALUE), varName));
	}
	
	/**
	 * <p>Define specified control with specified value 
	 * <p>This method is only available on Web agent now</p> 
     * @param  agentName the name of agent on which to run the step
     * @param  controlName the name of the control to read text, referenced in the GUI repository
     * @param  value specified value to set xpath for the control 
     * @param  varName the name of the variable which stores the result read from the control
     * 
     * <p>Pattern :
     * <blockquote><pre>@When("^(\\w+) define (\\w+) with location value \"([^\"]*)\" into (@\\w+)$")</pre></blockquote>
     * <p>Example : 
     * <blockquote><pre>When WebAgent define control Table_Head with location value "header" into @Table_Head</pre></blockquote>
     * <blockquote><pre>When WebAgent read text from @Table_Head into @result</pre></blockquote>
     * <p>The xpath of control Table_Head contains keyword "<LocationVariable>"
     * <blockquote><pre> Table_Head: /html/body/div[3]/section/div[1]/div/div/div[2]/menu-group-item[1]/div/div[2]/div/ul/li/div/div[1]/div[1]/span[contains(text(),'<LocationVariable>')]</pre></blockquote>
     * @since 1.7
     * @see fast.common.agents.WebBrowserAgent#defineSpecialValue(String, String)
     */
	@When("^(\\w+) define control (\\w+) with location value \"([^\"]*)\" into (@\\w+)$")
	public void defineLocationVariable(String agentName, String controlName, String value,String varName) throws Exception{
		StepResult result = AgentsManager.getInstance().getOrCreateAgent(agentName).runWithResult("defineLocationVariable",
				controlName,value);
		getScenarioContext().saveLastStepResult(result, varName);

	}
	/**
	 * <p>Reads the name from the specified control and then save it into a variable 
	 * <p>This method is only available on Uia agent now</p>
     * @param  agentName the name of agent on which to run the step
     * @param  controlName the name of the control to read its name, referenced in the GUI repository.
     * @param  varName the name of the variable which stores the result read from the control
     * 
     * <p>Pattern :
     * <blockquote><pre>@Then("^(\\w+) read name from (\\w+) into (@\\w+)$")</pre></blockquote>
     * <p>Example : 
     * <blockquote><pre>When DesktopAgent read name from Table_Head into @result</pre></blockquote>
     * 
     * @since 1.5
     * @see fast.common.agents.UiaAgent#readNameOnControl(String)
     */
	@Then("^(\\w+) read name from (\\w+) into (@\\w+)$")
    public void readNameOnControl(String agentName, String controlName, String varName) throws Throwable {
           StepResult result = AgentsManager.getInstance().getOrCreateAgent(agentName).runWithResult("readNameOnControl",
                        controlName);
           assertNotNull(result);
           getScenarioContext().saveLastStepResult(result, varName);
           scenarioAndLogWrite(String.format("Save text %s into variable %s", result.getFieldValue(StepResult.DEFAULT_FIELD_VALUE), varName));
    }

	/**
	 * <p>Reads the substring with the given prefix and length,
	 * from the text showed on the specified control and then save it into a variable 
	 * <p>This method is only available on Uia agent now</p>
     * @param  agentName he name of agent on which to run the step
     * @param  prefix the prefix of the substring
     * @param  length the length of the substring
     * @param  controlName the name of the control to read the substring, referenced in the GUI repository
     * @param  varName the name of the variable which stores the result read from the control
     * 
     * <p>Pattern :
     * <blockquote><pre>@Then("^(\\w+) read substring with prefix \"([^\"]*)\" and length (\\d+) from text on (\\w+) into (@\\w+)$")</pre></blockquote>
     * <p>Example : 
     * <blockquote><pre>Then DesktopAgent read substring with prefix "Error" and length 10 from text on Table_Content into @result</pre></blockquote>
     * 
     * @since 1.5
     * @see fast.common.agents.UiaAgent#readNameOnControl(String)
     */
	@Then("^(\\w+) read substring with prefix \"([^\"]*)\" and length (\\d+) from text on (\\w+) into (@\\w+)$")
	public void readTextSubstringOnControl(String agentName, String prefix, int length, String controlName,
			String varName) throws Throwable {
		StepResult result = AgentsManager.getInstance().getOrCreateAgent(agentName)
				.runWithResult("readTextSubstringOnControl", controlName, prefix, length);
		assertNotNull(result);
		getScenarioContext().saveLastStepResult(result, varName);

		scenarioAndLogWrite(String.format("read substring \"%s\" from control %s", result.getFieldValue(StepResult.DEFAULT_FIELD_VALUE), controlName));
	}


	/**
	 * <p>Clicks on the specified cell in the data grid
	 * <p>This method is only available on Uia agent now</p>
     * @param  agentName the name of agent on which to run the step
     * @param  cellPosition the index of the cell to click in the data grid, in the form of "RowIndex,ColumnIndex"
     * @param  controlName the name of the data grid in which to locate the cell, referenced in the GUI repository.
     *           
     * <p>Pattern :
     * <blockquote><pre>@When("^(\\w+) click cell \"([^\"]*)\" in data grid ([\\w\\.]+)")</pre></blockquote>
     * <p>Example : 
     * <blockquote><pre>When DesktopAgent click cell "0,0" in data grid Razor_Table</pre></blockquote>
     * 
     * @since 1.5
     * @see fast.common.agents.UiaAgent#clickGridCell(String, String)
     */
	@When("^(\\w+) click cell \"([^\"]*)\" in data grid ([\\w\\.]+)")
	public void clickGridCell(String agentName, String cellPosition, String controlName) throws Throwable {
		AgentsManager.getInstance().getOrCreateAgent(agentName).runWithResult("clickGridCell", controlName,
				cellPosition);
	}


    /**
	 * <p>Clicks on the specified point from its control
     * @param  agentName the name of agent on which to run the step
     * @param  pointOffsetX the X offset of the point
     * @param  pointOffsetY the Y offset of the point
     * @param  controlName the name of the control form which to locate the point by its offset, referenced in the GUI repository.
     *           
     * <p>Pattern :
     * <blockquote><pre>@When("^(\\w+) click point (\\d+),(\\d+) in ([\\w\\.]+)")</pre></blockquote>
     * <p>Example : 
     * <blockquote><pre>When DesktopAgent click point 500,400 in Razor_Table</pre></blockquote>
     * 
     * @since 1.5
     * @see fast.common.agents.UiaAgent#clickControlPoint(String, String, String)
     * @see fast.common.agents.LeanftAgent#clickControlPoint(String, String, String)
     */
	@When("^(\\w+) click point (\\d+),(\\d+) in ([\\w\\.]+)")
	public void clickControlPoint(String agentName, String pointOffsetX, String pointOffsetY, String controlName)
			throws Throwable {
		AgentsManager.getInstance().getOrCreateAgent(agentName).runWithResult("clickControlPoint", controlName,
				pointOffsetX, pointOffsetY);
	}


	/**
	 * <p>Right clicks on the specified point from its control
	 * <p>This method is only available on Uia agent now</p>
     * @param  agentName the name of agent on which to run the step
     * @param  pointOffsetX the X offset of the point
     * @param  pointOffsetY the Y offset of the point
     * @param  controlName the name of the control form which to locate the point by its offset, referenced in the GUI repository.
     *           
     * <p>Pattern :
     * <blockquote><pre>@When("^(\\w+) right click point (\\d+),(\\d+) in ([\\w\\.]+)")</pre></blockquote>
     * <p>Example : 
     * <blockquote><pre>When DesktopAgent right click point 500,400 in Razor_Table</pre></blockquote>
     * 
     * @since 1.5
     * @see fast.common.agents.UiaAgent#rightClickControlPoint(String, String, String)
     */
	@When("^(\\w+) right click point (\\d+),(\\d+) in ([\\w\\.]+)")
	public void rightClickControlPoint(String agentName, String pointOffsetX, String pointOffsetY, String controlName)
			throws Throwable {
		AgentsManager.getInstance().getOrCreateAgent(agentName).runWithResult("rightClickControlPoint", controlName,
				pointOffsetX, pointOffsetY);
	}


	/**
	 * <p>Reads the text from the specified cell in a data grid and then save it into a variable 
	 * <p>This method is only available on Uia agent now</p>
     * @param  agentName the name of agent on which to run the step
     * @param  cellPosition the index of the cell to read text in the data grid, in the form of "RowIndex,ColumnIndex"
     * @param  controlName the name of the data grid to read text, referenced in the GUI repository
     * @param  varName the name of the variable which stores the result read from the cell
     * <p>Pattern :
     * <blockquote><pre>@When("^(\\w+) read cell \"([^\"]*)\" in (\\w+) into (@\\w+)$")</pre></blockquote>
     * <p>Example : 
     * <blockquote><pre>When DesktopAgent read cell "0,0" in Razor_Table into @result</pre></blockquote>
     * 
     * @since 1.5
     * @see fast.common.agents.UiaAgent#readGridCell(String, String)
     */
	@When("^(\\w+) read cell \"([^\"]*)\" in ([\\w\\.]+) into (@\\w+)$")
	public void readGridCell(String agentName, String cellPosition, String controlName, String varName)
			throws Throwable {
		StepResult result = AgentsManager.getInstance().getOrCreateAgent(agentName).runWithResult("readGridCell",
				controlName, cellPosition);
		assertNotNull(result);
		getScenarioContext().saveLastStepResult(result, varName);
		scenarioAndLogWrite(String.format("Save cell value \"%s\" into variable %s", result.getFieldValue(StepResult.DEFAULT_FIELD_VALUE), varName));
	}
	
	/**
	 * <p>Closes the corresponding window for leanft agent
	 * 
     * @param  agentName the name of agent on which to run the step
     * @param  controlName the name of window to close
     * <p>Pattern :
     * <blockquote><pre>@Then("^(\\w+) close window ([\\w\\.]+)$")</pre></blockquote>
     * <p>Example : 
     * <blockquote><pre>Then LeanftAgent close window CalculatorWindow</pre></blockquote>
     * 
     * @since 1.5
     * @see fast.common.agents.LeanftAgent#closeLftControl(String)
     */
	@Then("^(\\w+) close window ([\\w\\.]+)$")
	public void closeLftControl(String agentName, String controlName) throws Exception {
		AgentsManager.getInstance().getOrCreateAgent(agentName).run("closeLftControl", controlName);
	}
	/**
	 * Click alert button
	 * @param agentName the name of agent on which to run the step
	 * @param alertButtonName the name of alert button, referenced in the GUI repository.
	 * @throws Exception
	 * @since 1.5
	 * @see fast.common.agents.WebBrowserAgent#checkClickAlert(String)
	 */
	public void checkClickAlert(String agentName, String alertButtonName) throws Exception {
		AgentsManager.getInstance().getOrCreateAgent(agentName).run("checkClickAlert", alertButtonName);
	}

	/**
	 * Checks whether a control exists in current page
	 * @param agentName the name of agent on which to run the step
	 * @param controlName the control to check
	 * @return true or false
	 * @throws Throwable
	 * @since 1.5
	 * @see fast.common.agents.WebBrowserAgent#CheckWhetherExist(String)
	 * @see fast.common.agents.UiaAgent#CheckWhetherExist(String)
	 * @see fast.common.agents.LeanftAgent#CheckWhetherExist(String)
	 */
	@Then("^(\\w+) check control ([\\w\\.]+) exist$")
	public boolean checkElementExist(String agentName, String controlName) throws Throwable {
		boolean bl = false;
		String processedControlName = getScenarioContext().processString(controlName);
		StepResult result = AgentsManager.getInstance().getOrCreateAgent(agentName).runWithResult("CheckWhetherExist",
				processedControlName);
		bl = Boolean.parseBoolean(result.getFieldValue(StepResult.DEFAULT_FIELD_VALUE));
		logger.info("------------------------------" + bl);
		return bl;
	}
	/**
	 * Checks whether a control can be seen in current page
	 * <p>This method is only available on Uia agent now</p>
	 * @param agentName the name of agent on which to run the step
	 * @param controlName the control to check
	 * @return true or false
	 * @throws Throwable
	 * @since 1.5
	 * @see fast.common.agents.UiaAgent#CheckWhetherVisible(String)
	 */
	public boolean checkElementVisible(String agentName, String controlName) throws Throwable {
		boolean bl = false;
		String processedControlName = getScenarioContext().processString(controlName);
		StepResult result = AgentsManager.getInstance().getOrCreateAgent(agentName).runWithResult("CheckWhetherVisible",
				processedControlName);
		bl = Boolean.parseBoolean(result.getFieldValue(StepResult.DEFAULT_FIELD_VALUE));
		logger.info("------------------------------" + bl);
		return bl;

	}
	/**
	 * Checks whether a control is enable in current page
	 * <p>This method is only available on Uia agent now</p>
	 * @param agentName the name of agent on which to run the step
	 * @param controlName the control to check
	 * @return true or false
	 * @throws Throwable
	 * @since 1.5
	 * @see fast.common.agents.UiaAgent#checkControlEnabled(String)
	 */
	public boolean checkControlEnabled(String agentName, String controlName) throws Throwable {
		boolean bl = false;
		String processedControlName = getScenarioContext().processString(controlName);
		StepResult result = AgentsManager.getInstance().getOrCreateAgent(agentName).runWithResult("checkControlEnabled",
				processedControlName);
		bl = Boolean.parseBoolean(result.getFieldValue(StepResult.DEFAULT_FIELD_VALUE));
		logger.info("------------------------------" + bl);
		assertTrue(bl);
		return bl;
	}
	/**
	 * Counts the number of specified web elements with same Xpath or css selector
	 * <p>This method is only available on web browser agent now</p>
	 * @param agentName the name of agent on which to run the step
	 * @param controlName the name of controls to count
	 * @return number of the web elements
	 * @throws Throwable
	 * @since 1.5
	 * @see fast.common.agents.WebBrowserAgent#counterElement(String)
	 */
	public int counterElement(String agentName, String controlName) throws Throwable {
		int num;
		String processedControlName = getScenarioContext().processString(controlName);
		StepResult result = AgentsManager.getInstance().getOrCreateAgent(agentName).runWithResult("counterElement",
				processedControlName);
		num = Integer.parseInt(result.getFieldValue(StepResult.DEFAULT_FIELD_VALUE));
		return num;
	}


	/**
	 * <p>Defines the value for the dynamic index or name variable
	 * <p>This method is only available on uia agent now</p>
     * @param  agentName the name of agent on which to run the step
     * @param  vName the name of the dynamic index or name whose value is set up by uia spy tool
     * @param  vValue the value to set for the dynamic index or name
     *        
     * <p>Pattern :
     * <blockquote><pre>@When("^(\\w+) define variable (@\\w+) with value as \"([^\"]*)\"$")</pre></blockquote>
     * <p>Example : 
     * <blockquote><pre>When DesktopAgent define variable @Node_Name with value as "TestOrder7"</pre></blockquote>
     * <p>Define dynamic variable with uia spy tool, see 
     * <a href="https://cedt-confluence.nam.nsroot.net/confluence/display/167813001/%5BUIA+Spy+Tool%5D+Custom+Actions"> Visit list of controls with variable name or index</a>
     * <p>For example, set up an control with a dynamix name "?$nodename" by uia spy tool
     * <p>Then, use this method defineVarible("DesktopAgent","nodename","TestOrder7") to pass dynamic name to specify control for later use
     * @since 1.5
     * @see fast.common.agents.UiaAgent#defineVariable(String, String)
     */
	@When("^(\\w+) define variable (@\\w+) with value as \"([^\"]*)\"$")
	public void defineVarible(String agentName, String vName, String vValue) throws Throwable {

		String processedName = getScenarioContext().processString(vName);
		String processedValue = getScenarioContext().processString(vValue);

		AgentsManager.getInstance().getOrCreateAgent(agentName).run("defineVariable", processedName, processedValue);
	}


	/**
	 * <p>Clears the content of the specified control whose type is input
	 * <p>This method is only available on web browser agent now</p>
	 * @param  agentName the name of agent on which to run the step
     * @param  controlName the name of the control to clear its input, referenced in the GUI repository. 
     *        
     * <p>Pattern :
     * <blockquote><pre>@When("^(\\w+) clear input control ([\\w\\.]+)$)</pre></blockquote>
     * <p>Example : 
     * <blockquote><pre>When WebAgent clear input control SearchBox</pre></blockquote>
     * 
     * @since 1.5
     * @see fast.common.agents.WebBrowserAgent#clearInputControl(String)
     */
	@When("^(\\w+) clear input control ([\\w\\.]+)$")
	public void clearInputControl(String agentName, String controlName) throws Throwable {
		String processedControlName = getScenarioContext().processString(controlName);
		AgentsManager.getInstance().getOrCreateAgent(agentName).run("clearInputControl", processedControlName);
	}

	public void getSpecificValue(String agentName, String controlName, String columnHeader, String containsValue,
			String varName) throws Throwable {
		StepResult result = AgentsManager.getInstance().getOrCreateAgent(agentName).runWithResult("getSpecificValue",
				controlName, columnHeader, containsValue);
		assertNotNull(result);
		getScenarioContext().saveLastStepResult(result, varName);

		scenarioAndLogWrite(
				String.format("read substring \"%s\" from control %s", result.getFieldValue(StepResult.DEFAULT_FIELD_VALUE), columnHeader));
	}
	/**
	 * Switches to a specified iframe
	 * <p>This method is only available on web browser agent now</p>
	 * @param agentName the name of agent on which to run the step
	 * @param frameName the name of iframe to switch to
	 * <p>Pattern :
	 * <blockquote><pre>@When("^(\\w+) switch to framework \"([^\"]*)\"$")</pre></blockquote>
	 * <p>Example : 
     * <blockquote><pre>When WebAgent switch to framework "clarity-reporting"</pre></blockquote>
	 * @throws Throwable
	 * @since 1.5
	 * @see fast.common.agents.WebBrowserAgent#switchFrame(String)
	 */
	@When("^(\\w+) switch to framework \"([^\"]*)\"$")
	public void switchFrame(String agentName, String frameName) throws Throwable{
		String processedControlName = getScenarioContext().processString(frameName);
		AgentsManager.getInstance().getOrCreateAgent(agentName).run("switchFrame", processedControlName);
		
	}
	/**
	 * Switches to default content from iframe
	 * <p>This method is only available on web browser agent now</p>
	 * @param agentName the name of agent on which to run the step
	 * <p>Pattern :
	 * <blockquote><pre>@When("^(\\w+) switch to default content$")</pre></blockquote>
	 * <p>Example : 
     * <blockquote><pre>When WebAgent switch to default content</pre></blockquote>
	 * @throws Throwable
	 * @since 1.5
	 * @see fast.common.agents.WebBrowserAgent#switchToDefault()
	 */
	
	@When("^(\\w+) switch to default content$")
	public void switchToDefault(String agentName) throws Throwable{
		AgentsManager.getInstance().getOrCreateAgent(agentName).run("switchToDefault");
	}
	
	/**
	 * Moves the mouse to the web element
	 * <p>This method is only available on web browser agent now</p>
	 * @param agentName the name of agent on which to run the step
	 * @param controlName the name of the specified web element to move to
	 * <p>Pattern :
	 * <blockquote><pre>@When("^(\\w+) mouse over to control (@?\\w+)$")</pre></blockquote>
	 * <p>Example : 
     * <blockquote><pre>Then WebAgent mouse over to control custodyItem</pre></blockquote>
	 * @throws Throwable
	 * @since 1.5
	 * @see fast.common.agents.WebBrowserAgent#moveTo(String)
	 */
	@When("^(\\w+) mouse over to control (@?\\w+)$")
	public void moveTo(String agentName, String controlName) throws Throwable{
		String processedControlName = (controlName.contains("@")) ? getScenarioContext()
				.processString(controlName + ".Value") : controlName;
		AgentsManager.getInstance().getOrCreateAgent(agentName).run("moveTo", processedControlName);
		
	}
	
	/**
	 * Move a web element to a location in a give offset of a target element
	 * <p>This method is only available on web browser agent now</p>
	 * @param agentName the name of agent on which to run the step
	 * @param controlName1 a web element will be moving
	 * @param controlName2 a target element
	 * @param x horizontal move offset of the target element
	 * @param y vertical move offset of the target element
	 * <p>Pattern :
	 * <blockquote><pre>@When("^(\\w+) drag from ([\\w\\.]+) and drop to ([\\w\\.]+) by offset ([-\\+]?[\\d]+),([-\\+]?[\\d]+)$")</pre></blockquote>
	 * <p>Example : 
     * <blockquote><pre>When WebAgent drag from Change and drop to ReportArea by offset 300,300</pre></blockquote>
	 * @throws Exception
	 * @since 1.5
	 * @see #dragAndDropTo(String, String, String)
	 * @see fast.common.agents.WebBrowserAgent#dragAndDrop(String, String, String, String)
	 * @see fast.common.agents.WebBrowserAgent#dragAndDropTo(String, String)
	 */
	@When("^(\\w+) drag from ([\\w\\.]+) and drop to ([\\w\\.]+) by offset ([-\\+]?[\\d]+),([-\\+]?[\\d]+)$")
	public void dragAndDrop(String agentName, String controlName1, String controlName2, String x, String y) throws Exception{
		String processedControlName1 = getScenarioContext().processString(controlName1);
		String processedControlName2 = getScenarioContext().processString(controlName2);
		AgentsManager.getInstance().getOrCreateAgent(agentName).run("dragAndDrop", processedControlName1, processedControlName2, x, y);
		
	}
	/**
	 *Grabs a web element and drag it N times with a relative offset
	 * <p>This method is only available on web browser agent now</p>
	 * @param agentName the name of agent on which to run the step
	 * @param controlName a web element will be moving
	 * @param times drag times
	 * @param x horizontal move offset 
	 * @param y vertical move offset
	 * <p>Pattern :
	 * <blockquote><pre>@When("^(\\w+) drag ([\\w\\.]+) with \"([^\"]*)\" times and offset ([-\\+]?[\\d]+),([-\\+]?[\\d]+)$")</pre></blockquote>
	 * <p>Example : 
     * <blockquote><pre>When WebAgent drag Change with 2 times and offset 10,10</pre></blockquote>
	 * @throws Exception
	 * @since 1.7
	 * @see #dragAndDropTo(String, String, String)
	 * @see fast.common.agents.WebBrowserAgent#dragAndDrop(String, String, String, String)
	 * @see fast.common.agents.WebBrowserAgent#dragNTimes(String, String, String, String)
	 * @see fast.common.agents.WebBrowserAgent#dragAndDropTo(String, String)
	 */
	@When("^(\\w+) drag ([\\w\\.]+) with \"([^\"]*)\" times and offset ([-\\+]?[\\d]+),([-\\+]?[\\d]+)$")
	public void dragNTimes(String agentName, String controlName, String times, String x, String y) throws Exception{
		String processedControlName = getScenarioContext().processString(controlName);
		String processedTimes = getScenarioContext().processString(times);
		AgentsManager.getInstance().getOrCreateAgent(agentName).run("dragNTimes", processedControlName, processedTimes, x, y);
		
	}
	/**
	 * Move a web element to a target area or element
	 * <p>This method is only available on web browser agent now</p>
	 * @param agentName the name of agent on which to run the step
	 * @param controlName1 a web element will be moving
	 * @param controlName2 a target area or element 
	 * @throws Exception
	 * @since 1.5
	 * @see #dragAndDrop(String, String, String, String, String)
	 * @see fast.common.agents.WebBrowserAgent#dragAndDrop(String, String, String, String)
	 * @see fast.common.agents.WebBrowserAgent#dragAndDropTo(String, String)
	 */
	@When("^(\\w+) drag and drop from ([\\w\\.]+) to ([\\w\\.]+)$")
	public void dragAndDropTo(String agentName, String controlName1, String controlName2) throws Exception{
		String processedControlName1 = getScenarioContext().processString(controlName1);
		String processedControlName2 = getScenarioContext().processString(controlName2);
		AgentsManager.getInstance().getOrCreateAgent(agentName).run("dragAndDropTo", processedControlName1, processedControlName2);
		
	}
	/**
	 *  Moves scroll bar until to see the specified web element 
	 * <p>This method is only available on web browser agent now</p>
	 * @param agentName the name of agent on which to run the step
	 * @param controlName the name of the specified web element
	 * @param offset the offset relative to the web element
	 * <p>Pattern :
	 * <blockquote><pre>@When("^(\\w+) scroll on ([\\w\\.]+) by offset ([-\\+]?[\\d]+)")</pre></blockquote>
	 * <p>Example : 
     * <blockquote><pre>Then WebAgent scroll on dashboard by offset 30</pre></blockquote>
	 * @throws Exception
	 * @since 1.5
	 * @see fast.common.agents.WebBrowserAgent#scrollTo(String, String)
	 */
	@When("^(\\w+) scroll on ([\\w\\.]+) by offset ([-\\+]?[\\d]+)$")
	public void scrollTo(String agentName, String controlName, String offset) throws Exception{
		String processedControlName = getScenarioContext().processString(controlName);
		String processedOffset = getScenarioContext().processString(offset);
		AgentsManager.getInstance().getOrCreateAgent(agentName).run("scrollTo", processedControlName, processedOffset);
		
	}
	
	/**
	 * Scrolls page to specified position
	 * <p>This method is only available on web browser agent now</p>
	 * @param agentName the name of agent on which to run the step
	 * @param widthOffset the width offset 
	 * @param heightOffset the height offset
	 * <p>Pattern :
	 * <blockquote><pre>@When("^(\\w+) scroll current page to width:([-\\+]?[\\d]+) height:([-\\+]?[\\d]+)$")</pre></blockquote>
	 * <p>Example : 
     * <blockquote><pre>Then WebAgent scroll current page to width:0 height:300</pre></blockquote>
	 * @throws Exception
	 * @since 1.5
	 * @see fast.common.agents.WebBrowserAgent#scrollPage(int, int)
	 */
	@When("^(\\w+) scroll current page to width:([-\\+]?[\\d]+) height:([-\\+]?[\\d]+)$")
	public void scrollPage(String agentName, int widthOffset, int heightOffset) throws Exception{
		AgentsManager.getInstance().getOrCreateAgent(agentName).run("scrollPage", widthOffset, heightOffset);
		
	}
	
	/**
	 * Click a control with a dynamic index on web page
	 * <p>This method is only available on web browser agent now</p>
	 * @param agentName the name of agent on which to run the step
	 * @param indexValue its value is index number
	 * @param controlName the name of the specified web element
	 * @throws Throwable
	 * <p>Pattern :
	 * <blockquote><pre>@When("^(\\w+) click with dynamic index ([\\w\\.]+) on control ([\\w\\.]+)")</pre></blockquote>
	 * <p>Example : 
     * <blockquote><pre>Then WebAgent click with dynamic index dashboardNum on control holdings</pre></blockquote>
	 * <p>Specify control contains keyword "dynamicIndex" in configuration file and "dynamicIndex" will be replaced by indexValue from parameter:
	 * <blockquote><pre>holdings: /html/body/section/section/section/portlet/div/div[2]/div[dynamicIndex]//*[@id="center"]/div/div[4]/div[3]/div/div/div[1]/div[2]</pre></blockquote>
     * @since 1.5
     * @see fast.common.agents.WebBrowserAgent#clickWithIndex(String, String)
	 */
	@When("^(\\w+) click with dynamic index ([\\w\\.]+) on control ([\\w\\.]+)$")
	public void clickControlWithIndex(String agentName, String indexValue, String controlName) throws Throwable{
		String processedControlName = getScenarioContext().processString(controlName);
		String processedindexName = getScenarioContext().processString(indexValue);
		AgentsManager.getInstance().getOrCreateAgent(agentName).run("clickWithIndex", processedindexName, processedControlName);
		
	} 
	/**
	 * Performs click action on the specified control. Does not care about whether the control is clickable.
	 * <p>This method is only available on web browser agent now</p>
	 * @param agentName the name of agent on which to run the step
	 * @param controlName the name of the specified web element
	 * <p>Pattern :
	 * <blockquote><pre>@When("^(\\w+) click hidden on element ([\\w\\.]+)$")</pre></blockquote>
	 * <p>Example : 
     * <blockquote><pre>And WebAgent click hidden on element Regression</pre></blockquote>
	 * @throws Throwable
	 * @since 1.5
	 * @see fast.common.agents.WebBrowserAgent#clickControlOnHidden(String)
	 */
	@When("^(\\w+) click hidden on element ([\\w\\.]+)")
    public void clickControlOnHiddenElement(String agentName, String controlName) throws Throwable {
           String processedControlName = getScenarioContext().processString(controlName);
           AgentsManager.getInstance().getOrCreateAgent(agentName).run("clickControlOnHidden", processedControlName);
    }
	/**
	 * Get a row index from a table by a specified cell value and the index of the column contains the cell value, then saves it into a variable
	 * <p>This method is only available on web browser agent now</p>
	 * @param agentName the name of agent on which to run the step
	 * @param controlName the name of the specified web element
	 * @param columnNum the index of the column contains the cell value  
	 * @param value the specified cell value
	 * @param varName a variable to save row index
	 * <p>Pattern :
	 * <blockquote><pre>@When("^(\\w+) get row index on table ([\\w\\.]+) in column (\\d+) with value \"([^\"]*)\" into (@\\w+)")</pre></blockquote>
	 * <p>Example : 
     * <blockquote><pre>When WebAgent get row index on table Tbody in column 1 with value "CET QA Automation Framework(C167813)" into @index</pre></blockquote>
	 * @throws Throwable
	 * @since 1.5
	 * @see fast.common.agents.WebBrowserAgent#getRowIndex(String, String, String)
	 * @see #getRowIndex(String, String, String, String)
	 */
	@When("^(\\w+) get row index on table ([\\w\\.]+) in column (\\d+) with value \"([^\"]*)\" into (@\\w+)")
	public void getWebRowIndex(String agentName, String controlName, String columnNum, String value, String varName) throws Throwable {
		String processedControlName = getScenarioContext().processString(controlName);
		StepResult result = AgentsManager.getInstance().getOrCreateAgent(agentName).runWithResult("getRowIndex", processedControlName, columnNum,value);
		assertNotNull(result);
		getScenarioContext().saveLastStepResult(result, varName);
		scenarioAndLogWrite(String.format("Save text %s into variable %s", result.getFieldValue(StepResult.DEFAULT_FIELD_VALUE), varName));
	
	}
	/**
	 * Click a control contains a specified value 
	 * <p>This method is only available on web browser agent now</p>
	 * @param agentName the name of agent on which to run the step
	 * @param value the specified value to identify a control
	 * @param controlName the name of the specified web element
	 * <p>Pattern :
	 * <blockquote><pre>@When("^(\\w+) click with special value \"([^\"]*)\" on control ([\\w\\.]+)")</pre></blockquote>
	 * <p>Example : 
     * <blockquote><pre>When WebAgent click with special value "FAST REPORT" on control CreatedReport</pre></blockquote>
	 * <p>Specify control contains keyword "SpecialValue" in configuration file and "SpecialValue" will be replaced by value from parameter:
	 * <blockquote><pre> CreatedReport: /html/body/div[3]/section/div[1]/div/div/div[2]/menu-group-item[1]/div/div[2]/div/ul/li/div/div[1]/div[1]/span[contains(text(),'SpecialValue')]</pre></blockquote>
	 * @throws Exception
	 * @since 1.5
	 * @see fast.common.agents.WebBrowserAgent#clickWithSpecialValue(String, String)
	 */
	@When("^(\\w+) click with special value \"([^\"]*)\" on control ([\\w\\.]+)$")
	public void clickWithSpecialValue(String agentName, String value, String controlName) throws Exception{
		String processedControlName = getScenarioContext().processString(controlName);
		String processedValue = getScenarioContext().processString(value);
		AgentsManager.getInstance().getOrCreateAgent(agentName).run("clickWithSpecialValue", processedValue, processedControlName);
		
	}
	/**
	 * Moves to a previous page according to actions history
	 * @param agentName the name of agent on which to run the step
	 * <p>Pattern :
	 * <blockquote><pre>@When("^(\\w+) navigate back$")</pre></blockquote>
	 * <p>Example : 
     * <blockquote><pre>When WebAgent navigate back</pre></blockquote>
	 * @throws Exception
	 * @since 1.5
	 * @see fast.common.agents.WebBrowserAgent#navigateBack()
	 */
	@When("^(\\w+) navigate back$")
	public void navigateBack(String agentName) throws Exception{
		AgentsManager.getInstance().getOrCreateAgent(agentName).run("navigateBack");
		
	}
	
	/**
	 * Refresh current web page
	 * @param agentName the name of agent on which to run the step
	 * <p>Pattern :
	 * <blockquote><pre>@When("^(\\w+) refresh$")</pre></blockquote>
	 * <p>Example : 
     * <blockquote><pre>When WebAgent refresh</pre></blockquote>
	 * @throws Exception
	 * @since 1.5
	 * @see fast.common.agents.WebBrowserAgent#refresh()
	 */
	@When("^(\\w+) refresh$")
	public void refresh(String agentName) throws Exception{
		AgentsManager.getInstance().getOrCreateAgent(agentName).run("refresh");  
		
	}
	/**
	 * Moves forward in web browser according to actions history
	 * @param agentName the name of agent on which to run the step
	 * <p>Pattern :
	 * <blockquote><pre>@When("^(\\w+) forward$")</pre></blockquote>
	 * <p>Example : 
     * <blockquote><pre>When WebAgent forward</pre></blockquote>
	 * @throws Exception
	 * @since 1.5
	 * @see fast.common.agents.WebBrowserAgent#forward()
	 */
	
	@When("^(\\w+) forward$")
	public void forward(String agentName) throws Exception{
		AgentsManager.getInstance().getOrCreateAgent(agentName).run("forward");  
		
	}
	/**
	 * Gets attribute value of the specified web element and saves it into a variable
	 * <p>This method is only available on web browser agent now</p>
	 * @param agentName the name of agent on which to run the step
	 * @param attributeName the name of the specified attribute
	 * @param controlName the name of the specified web element
	 * @param varName  a variable to store attribute value
	 * <p>Pattern :
	 * <blockquote><pre>@When("^(\\w+) read attribute \"([^\"]*)\" on control ([\\w\\.]+) into (@\\w+)$")</pre></blockquote>
	 * <p>Example : 
     * <blockquote><pre>When WebAgent read attribute "name" on control SearchTextbox into @searcheName</pre></blockquote>
     * 
	 * @throws Throwable
	 * @since 1.5
	 * @see fast.common.agents.WebBrowserAgent#readAttributValue(String, String)
	 */
	@When("^(\\w+) read attribute \"([^\"]*)\" on control ([\\w\\.]+) into (@\\w+)$")
	public void readAttributValue(String agentName,String attributeName, String controlName, String varName) throws Throwable{
		String processedControlName = getScenarioContext().processString(controlName);
		StepResult result = AgentsManager.getInstance().getOrCreateAgent(agentName).runWithResult("readAttributValue",
				processedControlName, attributeName);
		assertNotNull(result);
		getScenarioContext().saveLastStepResult(result, varName);
		scenarioAndLogWrite(String.format("Save text %s into variable %s", result.getFieldValue(StepResult.DEFAULT_FIELD_VALUE), varName));
		
	}
	
	/**
	 * Gets the css value of a web element and saves it into a variable
	 * <p>This method is only available on web browser agent now</p>
	 * @param agentName the name of agent on which to run the step
	 * @param attributeName the name of css attribute 
	 * @param controlName the name of the specified web element
	 * @param varName a variable to store css value
	 * <p>Pattern :
	 * <blockquote><pre>@When("^(\\w+) read CSS value \"([^\"]*)\" on control ([\\w\\.]+) into (@\\w+)$")</pre></blockquote>
	 * <p>Example : 
     * <blockquote><pre>When WebAgent read CSS value "font-size" on control usernameTextbox into @fontSize</pre></blockquote>
     * 
	 * @throws Throwable
	 * @since 1.5
	 * 
	 * @see fast.common.agents.WebBrowserAgent#readCssValue(String, String)
	 */
	@When("^(\\w+) read CSS value \"([^\"]*)\" on control ([\\w\\.]+) into (@\\w+)$")
	public void readCssValue(String agentName,String attributeName, String controlName, String varName) throws Throwable{
		String processedControlName = getScenarioContext().processString(controlName);
		StepResult result = AgentsManager.getInstance().getOrCreateAgent(agentName).runWithResult("readCssValue",
				processedControlName, attributeName);
		assertNotNull(result);
		getScenarioContext().saveLastStepResult(result, varName);
		scenarioAndLogWrite(String.format("Save text %s into variable %s", result.getFieldValue(StepResult.DEFAULT_FIELD_VALUE), varName));
	}	
	/**
	 * Checks element does not exist in web page 
	 * <p>This method is only available on web browser agent now</p>
	 * @param agentName the name of agent on which to run the step
	 * @param controlName the name of the specified web element
	 * <p>Pattern :
	 * <blockquote><pre>@When("^(\\w+) check control ([\\w\\.]+) not exist$")</pre></blockquote>
	 * <p>Example : 
     * <blockquote><pre>When WebAgent check control SearchButton not exist</pre></blockquote>
     *
	 * @throws Exception
	 * @since 1.5
	 * @see fast.common.agents.WebBrowserAgent#checkElementNotExist(String)
	 */
	@Then("^(\\w+) check control ([\\w\\.]+) not exist$")
	public void checkElementNotExist(String agentName, String controlName) throws Exception{
		String processedControlName = getScenarioContext().processString(controlName);
		AgentsManager.getInstance().getOrCreateAgent(agentName).run("checkElementNotExist", processedControlName);
		
	}
	/**
	 * Clicks a control by given css selector
	 * <p>This method is only available on web browser agent now</p>
	 * @param agentName the name of agent on which to run the step
	 * @param controlName the name of the specified web element
	 * <p>Pattern :
	 * <blockquote><pre>@When("^(\\w+) click by Css on control ([\\w\\.]+)$")</pre></blockquote>
	 * <p>Example : 
     * <blockquote><pre>when WebAgent click by Css on control SearchButton</pre></blockquote>
     *
	 * @throws Throwable
	 * @since 1.5
	 * @see fast.common.agents.WebBrowserAgent#clickControlByCss(String)
	 */
	@When("^(\\w+) click by Css on control ([\\w\\.]+)$")
	public void clickControlByCss(String agentName, String controlName) throws Throwable{
		String processedControlName = getScenarioContext().processString(controlName);
		AgentsManager.getInstance().getOrCreateAgent(agentName).run("clickControlByCss", processedControlName);
		
	}
	/**
	 * Uses specified leanft model
	 * <p>This method is only available on leanft agent now</p>
	 * @param agentName  the name of leanft agent
	 * @param modelName model name
	 * @throws Throwable
	 * @see fast.common.agents.LeanftAgent#useModel(String)
	 */
	@When("^(\\w+) use leanft model ([\\w\\.]+)$")
	public void useModel(String agentName, String modelName) throws Throwable{
		String processedModelName = getScenarioContext().processString(modelName);
		AgentsManager.getInstance().getOrCreateAgent(agentName).run("useModel", processedModelName);
		
	}
	

	/**
	 * Saves file name into variable after taking screenshot
	 * <p>This method is only available on web browser agent now</p>
	 * @param agentName the name of agent on which to run the step
	 * @param TestCaseName test case name
	 * @param varName a variable to store file path
	 * <p>Pattern :
	 * <blockquote><pre>@When("^(\\w+) create screenshot for test case \"([^\"]*)\" into (@\\w+)")</pre></blockquote>
	 * <p>Example : 
     * <blockquote><pre>When WebAgent create screenshot for test case "test download" into @imagePath</pre></blockquote>
     * 
	 * @throws Throwable
	 * @since 1.5
	 * @see fast.common.agents.WebBrowserAgent#_captureScreenshot(String)
	 */
	@When("^(\\w+) create screenshot for test case \"([^\"]*)\" into (@\\w+)$")
	public void createScreenshot(String agentName, String TestCaseName, String varName) throws Throwable{
		StepResult result = AgentsManager.getInstance().getOrCreateAgent(agentName).runWithResult("captureScreenshot", TestCaseName);
		assertNotNull(result);
		getScenarioContext().saveLastStepResult(result, varName);
		scenarioAndLogWrite(String.format("Save text %s into variable %s", result.getFieldValue(StepResult.DEFAULT_FIELD_VALUE), varName));
	
	}
	/**
	 * Executes local javaScript file
	 * <p>This method is only available on web browser agent now</p>
	 * @param agentName the name of agent on which to run the step
	 * @param scriptPath scriptPath file path of script file
	 * @param varName stores execution results into this variable
	 * <p>Pattern :
	 * <blockquote><pre>@When("^(\\w+) generate script \"([^\"]*)\" into (@\\w+)$")</pre></blockquote>
	 * <p>Example : 
     * <blockquote><pre>When WebAgent generate script "repos\testjs.js" into @jsResult</pre></blockquote>
     * 
	 * @throws Throwable
	 * @since 1.5
	 * @see fast.common.agents.WebBrowserAgent#generateScript(String)
	 */
	@When("^(\\w+) generate script \"([^\"]*)\" into (@\\w+)$")
	public void generateScript(String agentName, String scriptPath, String varName) throws Throwable{
		StepResult result = AgentsManager.getInstance().getOrCreateAgent(agentName).runWithResult("generateScript", scriptPath);
		assertNotNull(result);
		getScenarioContext().saveLastStepResult(result, varName);
		scenarioAndLogWrite(String.format("Save text %s into variable %s", result.getFieldValue(StepResult.DEFAULT_FIELD_VALUE), varName));
	}
	/**
	 * Counts row number for a data grid and saves it into a variable
	 * <p>This method is only available on uia agent now</p>
	 * @param agentName the name of agent on which to run the step
	 * @param controllName the name of the specified control
	 * @param varName a variable to save row number
	 * <p>Pattern :
	 * <blockquote><pre>@When("^(\\w+) get row count from (\\w+) into (@\\w+)$")</pre></blockquote>
	 * <p>Example : 
     * <blockquote><pre>Then DesktopAgent get row count from TDM_Solution_StoreValue_Dg into @rowCount</pre></blockquote>
     *
	 * @throws Throwable
	 * @since 1.5
	 * @see fast.common.agents.UiaAgent#getRowCount(String)
	 */
	@Then("^(\\w+) get row count from (\\w+) into (@\\w+)$")
	public void getRowCount(String agentName, String controllName, String varName) throws Throwable{
		StepResult result = AgentsManager.getInstance().getOrCreateAgent(agentName).runWithResult("getRowCount",
				controllName);
		assertNotNull(result);
		getScenarioContext().saveLastStepResult(result, varName);
		scenarioAndLogWrite(String.format("Save row count %s into variable %s", result.getFieldValue(StepResult.DEFAULT_FIELD_VALUE), varName));
	}	
	/**
	 * Counts column number for a data grid and saves it into a variable
	 * <p>This method is only available on uia agent now</p>
	 * @param agentName the name of agent on which to run the step
	 * @param controllName the name of the specified control
	 * @param varName a variable to save column number
	 * <p>Pattern :
	 * <blockquote><pre>@When("^(\\w+) get column count from (\\w+) into (@\\w+)$")</pre></blockquote>
	 * <p>Example : 
     * <blockquote><pre>Then DesktopAgent get column count from TDM_Solution_StoreValue_Dg into @columnCount</pre></blockquote>
     *
	 * @throws Throwable
	 * @since 1.5
	 * @see fast.common.agents.UiaAgent#getColumnCount(String)
	 */
	@Then("^(\\w+) get column count from (\\w+) into (@\\w+)$")
	public void getColumnCount(String agentName, String controllName, String varName) throws Throwable{
		StepResult result = AgentsManager.getInstance().getOrCreateAgent(agentName).runWithResult("getColumnCount",
				controllName);
		assertNotNull(result);
		getScenarioContext().saveLastStepResult(result, varName);
		scenarioAndLogWrite(String.format("Save column count %s into variable %s", result.getFieldValue(StepResult.DEFAULT_FIELD_VALUE), varName));
	}       
	
	/**
	 * Gets the whole table data into a variable
	 * <p>This method is only available on UIa agent now</p>
	 * @param agentName the name of agent on which to run the step
	 * @param controllName the name of the specified table
	 * @param varName a variable to save table
	 * <p>Pattern :
	 * <blockquote><pre>@When("^(\\w+) get whole table data from (\\w+) into (@\\w+)$")</pre></blockquote>
	 * <p>Example : 
     * <blockquote><pre>And WebAgent get whole table data from RDEF_table into @Table</pre></blockquote>
	 * @throws Throwable
	 * @since 1.6
	 * @see fast.common.agents.UiaAgent#getWholeTableData(String)
	 */
	@Then("^(\\w+) get whole table data from (\\w+) into (@\\w+)$")
	public void getWholeTableData(String agentName, String controllName,String varName) throws Throwable{
		StepResult result = AgentsManager.getInstance().getOrCreateAgent(agentName).runWithResult("getWholeTableData",
				controllName);
		assertNotNull(result);
		getScenarioContext().saveLastStepResult(result, varName);
		scenarioAndLogWrite(String.format("Save %d rows, %d columns table data into variable %s",
				((UiaStepResult)result).getTableRowCount(),
				((UiaStepResult)result).getTableColumnCount(),
				varName));
	}
	
	/**
	 * Handles alert/popup dialog with operations: accept or dismiss or show
	 * <p>This method is only available on web browser agent now</p>
	 * @param agentName the name of agent on which to run the step
	 * @param operation accept or dismiss or show
	 * <p>Pattern :
	 * <blockquote><pre>@When("^(\\w+) (accept|dismiss|show) alert$")</pre></blockquote>
	 * <p>Example : 
     * <blockquote><pre>And WebAgent accept alert</pre></blockquote>
     * 
	 * @throws Throwable
	 * @since 1.6
	 * @see fast.common.agents.WebBrowserAgent#alertOperation(String)
	 */	@Then("^(\\w+) (accept|dismiss|show) alert$")
	public void alertOperation(String agentName, String operation) throws Throwable {
		String processedOperation = getScenarioContext().processString(operation);
		AgentsManager.getInstance().getOrCreateAgent(agentName).run("alertOperation", processedOperation);
	}
	 
	 /**
	  * To get a row data on table and save it into a given variable
	  * <p>This method is only available on web browser agent now</p>
	  * @param agentName the name of agent on which to run the step
	  * @param rowIndex the index of specified row on table
	  * @param controlName the name of the specified table 
	  * @param varName a variable to save table
	  * <p>Pattern :
	  * <blockquote><pre>@Then("^(\\w+) get row (\\d+) data on table (\\w+) into (@\\w+)$")</pre></blockquote>
	  * <p>Example :
	  * <blockquote><pre>Then DesktopAgent get row 1 data on table DEQFV_DataGrid_Records into @table</pre></blockquote>
	  * 
	  * @throws Throwable
	  * @since 1.6
	  * @see fast.common.agents.UiaAgent#getTableRowData(String, int)
	  */@Then("^(\\w+) get row (\\d+) data on table (\\w+) into (@\\w+)$")
	public void getTableRowData(String agentName, int rowIndex, String controlName,String varName) throws Throwable{
		StepResult result=AgentsManager.getInstance().getOrCreateAgent(agentName).runWithResult("getTableRowData", controlName,rowIndex);
		assertNotNull(result);
		getScenarioContext().saveLastStepResult(result,varName);
		scenarioAndLogWrite(String.format("Save row %d data on table %s into %s successfully", rowIndex,controlName,varName));
	}
	
	/**
	* To get a column data on table and save it into a given variable
	* <p>This method is only available on web browser agent now</p>
	* @param agentName the name of agent on which to run the step
	* @param columnName the name of specified column on table
	* @param controlName the name of the specified table 
	* @param varName a variable to save table
	* <p>Pattern :
	* <blockquote><pre>@Then("^(\\w+) get row (\\d+) data on table (\\w+) into (@\\w+)$")</pre></blockquote>
	* <p>Example :
	* <blockquote><pre>Then DesktopAgent get row 1 data on table DEQFV_DataGrid_Records into @table</pre></blockquote>
	* 
	* @throws Throwable
	* @since 1.6
	* @see fast.common.agents.UiaAgent#getTableRowData(String, int)
	*/@Then("^(\\w+) get column '(.*?)' data on table (\\w+) into (@\\w+)$")
	public void getTableColumnData(String agentName, String columnName, String controlName,String varName) throws Throwable{
		StepResult result=AgentsManager.getInstance().getOrCreateAgent(agentName).runWithResult("getTableColumnData", controlName,columnName);
		assertNotNull(result);
		getScenarioContext().saveLastStepResult(result,varName);
		scenarioAndLogWrite(String.format("Save column %s data on table %s into %s successfully", columnName,controlName,varName));
	}
	
	/**
	* Create a new tab and open a new page with giving url
	* <p>This method is only available on web browser agent now</p>
	* @param agentName the name of agent on which to run the step
	* @param url the url to open a new tab
	* <p>Pattern :
	* <blockquote><pre>@Then("^(\\w+) open \"([^\"]*)\" url in new tab$")</pre></blockquote>
	* <p>Example :
	* <blockquote><pre>Then WebAgent open "https://www.wikipedia.org/" url in new tab</pre></blockquote>
	* 
	* @throws Throwable
	* @since 1.7
	* @see fast.common.agents.WebBrowserAgent#createNewTab(String url)
	*/
	@Then("^(\\w+) open \"([^\"]*)\" url in new tab$")
	public void createNewTab(String agentName, String url) throws Throwable{
		String processedUrl = getScenarioContext().processString(url);
		AgentsManager.getInstance().getOrCreateAgent(agentName).run("createNewTab", processedUrl);
	}
	
	/**
	* Change from current tab to next newer tab
	* <p>This method is only available on web browser agent now</p>
	* @param agentName the name of agent on which to run the step
	* <p>Pattern :
	* <blockquote><pre>@Then("^(\\w+) change to next tab$")</pre></blockquote>
	* <p>Example :
	* <blockquote><pre>Then WebAgent change to next tab</pre></blockquote>
	* 
	* @throws Throwable
	* @since 1.7
	* @see fast.common.agents.WebBrowserAgent#changeToNextTab()
	*
	*/@Then("^(\\w+) change to next tab$")
	public void changeToNextTab(String agentName) throws Throwable{
		AgentsManager.getInstance().getOrCreateAgent(agentName).run("changeToNextTab");
	}
	
	/**
	* Change from current tab to last older tab
	* <p>This method is only available on web browser agent now</p>
	* @param agentName the name of agent on which to run the step
	* <p>Pattern :
	* <blockquote><pre>@Then("^(\\w+) change to last tab$")</pre></blockquote>
	* <p>Example :
	* <blockquote><pre>Then WebAgent change to last tab</pre></blockquote>
	* 
	* @throws Throwable
	* @since 1.7
	* @see fast.common.agents.WebBrowserAgent#changeToLastTab()
	*
	*/@Then("^(\\w+) change to last tab$")
	public void changeToLastTab(String agentName) throws Throwable{
		AgentsManager.getInstance().getOrCreateAgent(agentName).run("changeToLastTab");
	}
	
	/**
	* Close current tab
	* <p>This method is only available on web browser agent now</p>
	* @param agentName the name of agent on which to run the step
	* <p>Pattern :
	* <blockquote><pre>@Then("^(\\w+) close current tab$")</pre></blockquote>
	* <p>Example :
	* <blockquote><pre>Then WebAgent close current tab</pre></blockquote>
	* 
	* @throws Throwable
	* @since 1.7
	* @see fast.common.agents.WebBrowserAgent#closeCurrentTab()
	*
	*/@Then("^(\\w+) close current tab$")
	public void closeCurrentTab(String agentName) throws Throwable{
		AgentsManager.getInstance().getOrCreateAgent(agentName).run("closeCurrentTab");
	}
	
	/**
	 * Get property value from control
	 * @param controlName he name of the control to read text
	 * @return a StepResult with the text
	 * @throws Throwable 
	 * @since 1.7
	 * @see fast.common.agents.UiaAgent#getPropertyOnControl(String, String)
	 */
	@Then("^(\\w+) get property (\\w+) from (@?\\w+) into (@\\w+)$")
	public void getPropertyOnControl(String agentName,String target,String controlName,String varName) throws Throwable {
		String processControlName = (controlName.contains("@")) ? getScenarioContext()
				.processString(controlName + ".Value") : controlName;
		String propertyName = (target.contains("@")) ? getScenarioContext()
						.processString(target + ".Value") : target;
		StepResult result = AgentsManager.getInstance().getOrCreateAgent(agentName).runWithResult("getPropertyOnControl",
				processControlName,propertyName);
		assertNotNull(result);
		getScenarioContext().saveLastStepResult(result, varName);
		scenarioAndLogWrite(String.format("Save property value %s into variable %s", result.getFieldValue(StepResult.DEFAULT_FIELD_VALUE), varName));
	}
	
	/**
	  * To get headers on table and save it into a given variable
	  * <p>This method is only available on web browser agent now</p>
	  * @param agentName the name of agent on which to run the step
	  * @param controlName the name of the specified table 
	  * @param varName a variable to save table
	  * <p>Pattern :
	  * <blockquote><pre>@Then("^(\\w+) get headers on table (\\w+) into (@\\w+)$")</pre></blockquote>
	  * <p>Example :
	  * <blockquote><pre>Then DesktopAgent get headers on table DEQFV_DataGrid_Records into @headers</pre></blockquote>
	  * 
	  * @throws Throwable
	  * @since 1.7
	  * @see fast.common.agents.UiaAgent#getTableHeaders(String)
	  */@Then("^(\\w+) get headers on table (\\w+) into (@\\w+)$")
	public void getTableHeaders(String agentName, String controlName,String varName) throws Throwable{
		StepResult result=AgentsManager.getInstance().getOrCreateAgent(agentName).runWithResult("getTableHeaders", controlName);
		assertNotNull(result);
		getScenarioContext().saveLastStepResult(result,varName);
		scenarioAndLogWrite(String.format("Save headers on table %s into %s successfully",controlName,varName));
	}
	  
	  /**
		  * To select item on comboBox
		  * <p>This method is only available on uia agent now</p>
		  * @param agentName the name of agent on which to run the step
		  * @param itemName a item to select
		  * @param controlName the name of the specified table 
		  * <p>Pattern :
		  * <blockquote><pre>@When("^(\\w+) select item \"(\\w+)\" from combobox \"(\\w+)\"")</pre></blockquote>
		  * <p>Example :
		  * <blockquote><pre>When DesktopAgent_PFS select item "11CGBR" from combobox "PV_ComboBox__comboFirmAccount_Entity"</pre></blockquote>
		  * 
		  * @throws Throwable
		  * @since 1.8
		  * @see fast.common.agents.UiaAgent#selectItemInCombobox(String,String)
		  */
	@When("^(\\w+) select item \"(\\w+)\" from combobox \"(\\w+)\"")
	public void selectItemFromComboBox(String agentName, String itemName,String controlName) throws Throwable{
		String processedControlName = getScenarioContext().processString(controlName);
		String processedItemName=getScenarioContext().processString(itemName);
		AgentsManager.getInstance().getOrCreateAgent(agentName).run("selectItemInCombobox", processedControlName,processedItemName);		
	}
	/**
	 * press hot key from a control with a dynamic index on web page
	 * <p>This method is only available on web browser agent now</p>
	 * @param agentName the name of agent on which to run the step
	 * @param keyName the name of the hot key to press
	 * @param controlName the name of the specified web element
	 * @param indexValue its value is index number
	 * @throws Throwable
	 * <p>Pattern :
	 * <blockquote><pre>@When("^(\\w+) press \"([^\"]*)\" on ([\\w\\.]+) with dynamic index \"([^\"]*)\"$")</pre></blockquote>
	 * <p>Example :
	 * <blockquote><pre>Then WebAgent click with dynamic index dashboardNum on control holdings</pre></blockquote>
	 * <p>Specify control contains keyword "dynamicIndex" in configuration file and "dynamicIndex" will be replaced by indexValue from parameter:
	 * <blockquote><pre>holdings: /html/body/section/section/section/portlet/div/div[2]/div[dynamicIndex]//*[@id="center"]/div/div[4]/div[3]/div/div/div[1]/div[2]</pre></blockquote>
	 * @since 1.9
	 * @see fast.common.agents.WebBrowserAgent#pressHotKeyOnControlWithIndex(String,String,String)
	 */
	@When("^(\\w+) press \"([^\"]*)\" on ([\\w\\.]+) with dynamic index \"([^\"]*)\"$")
	public void pressHotKeyOnControlWithIndex(String agentName, String keyName, String controlName, String indexValue) throws Throwable{
		AgentsManager.getInstance().getOrCreateAgent(agentName).run("pressHotKeyOnControlWithIndex", keyName, controlName,indexValue);
	}

	/**
	 * Gets attribute value of the specified web element and saves it into a variable
	 * <p>This method is only available on web browser agent now</p>
	 * @param agentName the name of agent on which to run the step
	 * @param attributeName the name of the specified attribute
	 * @param controlName the name of the specified web element
	 * @param varName  a variable to store attribute value
	 * @param index  its value is index number
	 * <p>Pattern :
	 * <blockquote><pre>@When("^(\\w+) read attribute \"([^\"]*)\" on control ([\\w\\.]+) with \"([^\"]*)\" into (@\\w+)$")</pre></blockquote>
	 * <p>Example :
	 * <blockquote><pre>When WebAgent read attribute "name" on control SearchTextbox with "dynamiceIndex" into @searcheName</pre></blockquote>
	 *
	 * @throws Throwable
	 * @since 1.9
	 * @see fast.common.agents.WebBrowserAgent#readAttributValueWithIndex(String, String,String)
	 */
	@When("^(\\w+) read attribute \"([^\"]*)\" on control ([\\w\\.]+) with \"([^\"]*)\" into (@\\w+)$")
	public void readAttributValueWithIndex(String agentName,String attributeName, String controlName, String index,String varName) throws Throwable{
		String processedControlName = getScenarioContext().processString(controlName);
		StepResult result = AgentsManager.getInstance().getOrCreateAgent(agentName).runWithResult("readAttributValueWithIndex",
				processedControlName, attributeName,index);
		assertNotNull(result);
		getScenarioContext().saveLastStepResult(result, varName);
		scenarioAndLogWrite(String.format("Save text %s into variable %s", result.getFieldValue(StepResult.DEFAULT_FIELD_VALUE), varName));
	}

	/**
	 * read text from a control with a dynamic index on web page
	 * <p>This method is only available on web browser agent now</p>
	 * @param agentName the name of agent on which to run the step
	 * @param controlName the name of the specified web element
	 * @param indexValue its value is index number
	 * @param varName the name of the variable which stores the result read from the control
	 * @throws Throwable
	 * <p>Pattern :
	 * <blockquote><pre>@Then("^(\\w+) read text from (@?\\w+) with dynamic index ([\\w\\.]+) into (@\\w+)$")</pre></blockquote>
	 * <p>Example :
	 * <blockquote><pre>Then WebAgent click with dynamic index dashboardNum on control holdings</pre></blockquote>
	 * <p>Specify control contains keyword "dynamicIndex" in configuration file and "dynamicIndex" will be replaced by indexValue from parameter:
	 * <blockquote><pre>holdings: /html/body/section/section/section/portlet/div/div[2]/div[dynamicIndex]//*[@id="center"]/div/div[4]/div[3]/div/div/div[1]/div[2]</pre></blockquote>
	 * @since 1.9
	 * @see fast.common.agents.WebBrowserAgent#readTextWithIndex(String,String)
	 */
	@Then("^(\\w+) read text from (@?\\w+) with dynamic index ([\\w\\.]+) into (@\\w+)$")
	public void readTextWithIndex(String agentName,String controlName,String indexValue, String varName) throws Throwable{
		String processControlName = (controlName.startsWith("@")) ? getScenarioContext()
				.processString(controlName + ".Value") : controlName;
		String processedindexName = getScenarioContext().processString(indexValue);
		StepResult result = AgentsManager.getInstance().getOrCreateAgent(agentName).runWithResult("readTextWithIndex",
				processControlName,processedindexName);
		assertNotNull(result);
		getScenarioContext().saveLastStepResult(result, varName);
		scenarioAndLogWrite(String.format("Save text %s into variable %s", result.getFieldValue(StepResult.DEFAULT_FIELD_VALUE), varName));
	}

	/**
	 * Type Text on a control with a dynamic index on web page
	 * <p>This method is only available on web browser agent now</p>
	 * @param agentName the name of agent on which to run the step
	 * @param text the text to type
	 * @param indexValue its value is index number
	 * @param controlName the name of the specified web element
	 * @throws Throwable
	 * <p>Pattern :
	 * <blockquote><pre>@When("^(\\w+) click with dynamic index ([\\w\\.]+) on control ([\\w\\.]+)")</pre></blockquote>
	 * <p>Example :
	 * <blockquote><pre>Then WebAgent click with dynamic index dashboardNum on control holdings</pre></blockquote>
	 * <p>Specify control contains keyword "dynamicIndex" in configuration file and "dynamicIndex" will be replaced by indexValue from parameter:
	 * <blockquote><pre>holdings: /html/body/section/section/section/portlet/div/div[2]/div[dynamicIndex]//*[@id="center"]/div/div[4]/div[3]/div/div/div[1]/div[2]</pre></blockquote>
	 * @since 1.9
	 * @see fast.common.agents.WebBrowserAgent#typeTextWithIndex(String,String,String)
	 */
	@When("^(\\w+) type \"([^\"]*)\" with dynamic index ([\\w\\.]+) into ([\\w\\.]+)$")
	public void typeControlWithIndex(String agentName, String text,String indexValue, String controlName) throws Throwable{
		String processedText = getScenarioContext().processString(text);
		String processedControlName = getScenarioContext().processString(controlName);
		String processedindexName = getScenarioContext().processString(indexValue);
		AgentsManager.getInstance().getOrCreateAgent(agentName).run("typeTextWithIndex",processedindexName, processedControlName, processedText);
	}

	/**
	 * <p>Reads all the text from the specified control and then return a List of String
	 *
	 * @param  agentName the name of agent on which to run the step
	 * @param  controlName the name of the control to read text, referenced in the GUI repository
	 *
	 * <p>Pattern :
	 * <blockquote><pre>@Then("^(\\w+) read all text from (@?\\w+) into List$")</pre></blockquote>
	 * <p>Example :
	 *<blockquote><pre>List<String> allStatus = _guiCommonStepDefs.readAllTextOnControl(agentName, "varname");</pre></blockquote>
	 * @since 1.9
	 * @see fast.common.agents.WebBrowserAgent#readAllTextOnControl(String)
	 */

	@Then("^(\\w+) read all text from (@?\\w+) into List$")
	public List<String> readAllTextOnControl(String agentName, String controlName) throws Throwable {
		String processControlName = (controlName.startsWith("@")) ? getScenarioContext()
				.processString(controlName + ".Value") : controlName;
		StepResult result = AgentsManager.getInstance().getOrCreateAgent(agentName).runWithResult("readAllTextOnControl",
				processControlName);
		return result.getFieldsValues(StepResult.DEFAULT_FIELD_VALUE);
	}


	/**
	 * <p>Type text on the specified point from its control
	 * @param  agentName the name of agent on which to run the step
	 * @param  pointOffsetX the X offset of the point
	 * @param  pointOffsetY the Y offset of the point
	 * @param  controlName the name of the control form which to locate the point by its offset, referenced in the GUI repository.
	 *
	 * <p>Pattern :
	 * <blockquote><pre>@When("^(\\w+) Type \"([^\"]*)\" point (\\d+),(\\d+) in ([\\w\\.]+)")</pre></blockquote>
	 * <p>Example :
	 * <blockquote><pre>When DesktopAgent type "56000610291" point 900,80 in SMI_Group_Sub</pre></blockquote>
	 *
	 * @since 1.9
	 * @see fast.common.agents.UiaAgent#typeTextOnControlWithPoint(String,String, String, String)
	 */
	@When("^(\\w+) type \"([^\"]*)\" point (\\d+),(\\d+) in ([\\w\\.]+)")
	public void typeTextOnControlWithPoint(String agentName, String text,String pointOffsetX, String pointOffsetY, String controlName)
			throws Throwable {
		AgentsManager.getInstance().getOrCreateAgent(agentName).runWithResult("typeTextOnControlWithPoint",text, controlName,
				pointOffsetX, pointOffsetY);
	}
	
	/**
	 * <p>fetch color on control point
	 * @param  agentName the name of agent on which to run the step
	 * @param point offset x related to control
	 * @param point offset y related to control
	 * @param  controlName the name of the control	 
	 * @param  varName to save value
	 *
	 * <p>Pattern :
	 * <blockquote><pre>@When("^(\\w+) fetch rgb color from (@?\\w+) into (@\\w+)$")</pre></blockquote>
	 * <p>Example :
	 * <blockquote><pre>When SimpleAgent fetch rgb color from F_Pane_panel1 into @color</pre></blockquote>
	 *
	 * @since 1.9
	 * @see fast.common.agents.UiaAgent#getColorOnControlPoint(String)
	 */
	@When("^(\\w+) fetch rgb color from point -?(\\d+),-?(\\d+) on (@?\\w+) into (@\\w+)$")
	public void getRgbColorOnControlPoint(String agentName, String pointOffsetX, String pointOffsetY, String controlName, String varName)
			throws Throwable {
		String processControlName = (controlName.startsWith("@")) ? getScenarioContext()
				.processString(controlName + ".Value") : controlName;
		StepResult result = AgentsManager.getInstance().getOrCreateAgent(agentName).runWithResult("getColorOnControlPoint", processControlName,pointOffsetX, pointOffsetY);
		assertNotNull(result);
		getScenarioContext().saveLastStepResult(result, varName);
		scenarioAndLogWrite(String.format("Fetch color %s from control %s, then save in %s", result.getFieldValue(StepResult.DEFAULT_FIELD_VALUE), processControlName, varName));
	}
	
	/**
	 * <p>get state on check box
	 * @param  agentName the name of agent on which to run the step
	 * @param  controlName the name of the control
	 * @param  varName to save value
	 *
	 * <p>Pattern :
	 * <blockquote><pre>@When("^(\\w+) get state from (@?\\w+) into (@\\w+)$")</pre></blockquote>
	 * <p>Example :
	 * <blockquote><pre>When SimpleAgent fetch rgb color from F_Pane_panel1 into @color</pre></blockquote>
	 *
	 * @since 1.10
	 * @see fast.common.agents.UiaAgent#getCheckBoxState(String)
	 */
	@When("^(\\w+) get state from (@?\\w+) into (@\\w+)$")
	public void getCheckBoxState(String agentName, String controlName, String varName)
			throws Throwable {
		String processControlName = (controlName.startsWith("@")) ? getScenarioContext()
				.processString(controlName + ".Value") : controlName;
		StepResult result = AgentsManager.getInstance().getOrCreateAgent(agentName).runWithResult("getCheckBoxState", processControlName);
		assertNotNull(result);
		getScenarioContext().saveLastStepResult(result, varName);
		scenarioAndLogWrite(String.format("Get state %s from control %s, then save in %s", result.getFieldValue("Value"), processControlName, varName));
	}
}
