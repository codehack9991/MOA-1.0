package fast.common.glue;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;


import cucumber.api.DataTable;
import cucumber.api.Scenario;
import cucumber.api.java.After;
import cucumber.api.java.Before;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import fast.common.agents.Agent;
import fast.common.agents.AgentDNA;
import fast.common.agents.AgentsManager;
import fast.common.agents.ElkAgent;
import fast.common.agents.FixTcpServerAgent;
import fast.common.agents.IFixMessagingAgent;
import fast.common.agents.IStartable;
import fast.common.agents.WebApiAgent;
import fast.common.agents.WebApiAgent.RequestType;
import fast.common.agents.messaging.IMessagingAgent;
import fast.common.context.CommonStepResult;
import fast.common.context.DNAStepResult;
import fast.common.context.DatabaseStepResult;
import fast.common.context.DateTimeDifferStepResult;
import fast.common.context.FixStepResult;
import fast.common.context.HelperMethods;
import fast.common.context.ITableResult;
import fast.common.context.MapMessageTemplateHelper;
import fast.common.context.StepResult;
import fast.common.context.IStringResult;
import fast.common.context.exception.ColIndexOutofRangeException;
import fast.common.context.exception.ColumnNotExistsException;
import fast.common.context.exception.RowIndexOutofRangeException;
import fast.common.core.FastException;
import fast.common.fix.RIOFixHelper;
import fast.common.logging.FastLogger;
import fast.common.utilities.DateTimeUtils;

/**
 * The {@code CommonStepDefs} class defines some common actions for agents, which can be used in different scenarios.
 * @author Shreyas
 * @since 1.5
 */

public class CommonStepDefs extends BaseCommonStepDefs{
	public static final FastLogger logger = FastLogger.getLogger("CommonStepDefs");

	/** 
     * Use hook @Before to set up runtime environment before each scenario
     * @param scenario  
     * @since 1.5
     */
	@Before
	@Override
	public void beforeScenario(Scenario scenario) throws Exception {
		super.beforeScenario(scenario);		
	}

	@After
	@Override
	public void afterScenario(Scenario scenario) throws Exception {
		Set<String> tag37set = getScenarioContext().getTag37set();
		String tag37list = null;
		if (tag37set != null) {
			tag37list = String.join(", ", getScenarioContext().getTag37set());
		}

		runtimeInfoManager.notifyScenarioEnd(scenario, this);
		if (runtimeInfoManager.isScenarioEnded(scenario)) {
			runtimeInfoManager.saveReportToFile(scenario);

			// DONE: if total count of threads == 1 then clear all buffers and
			// even probably kill agents
			// TODO: otherwise agents should remove old data from their buffers.
			// it can be done here or internally inside agents. Probably can
			// consider all running scenarios start time
			AgentsManager.getInstance().flushBuffersToLog();

			// Release Agent resources after testing
//			boolean isSingleThread = Configurator.IsSingleThread();
			// KT: we can only do this in single threaded mode! Update - we
			// can't do it at all!
			// we should do it differently in multithreaded mode!
			// if(isSingleThread) { KT: we SHALL NOT kill agents - it
			// SIGNIFICANTLY increase test execution time (~15 times in case of
			// GMA project!)
			// AgentsManager.getInstance().close();
			// }

			getScenarioContext().close();

			if (tag37list != null && !tag37list.isEmpty()) {
				scenarioAndLogWrite(String.format("Scenario ID: '%s' FINISHED with status: '%s', TAGS 37: [%s]",
						scenario.getId(), scenario.getStatus(), tag37list));
			} else {
				scenarioAndLogWrite(String.format("Scenario ID: '%s' FINISHED with status: '%s'", scenario.getId(),
						scenario.getStatus()));
			}
			scenarioAndLogWrite("--------------------------------------------------");
		}
	}
	/**
	 * Waits few seconds to run next step 
	 * @param sec time to wait
	 * <p>Pattern :
     * <blockquote><pre>@When("^Wait (\\d+) seconds$")</pre></blockquote>
     * <p>Example : 
     * <blockquote><pre>When Wait 5 seconds</pre></blockquote>
	 * @throws Throwable
	 * @since 1.5
	 */
	@When("^Wait (\\d+) seconds$")
	public void waitSeconds(int sec) throws Throwable {
		Thread.sleep(sec * 1_000L);
	}
	
	/**
	 * <p>Closes the corresponding agent
	 * 
     * @param  agentName the name of agent on which to run the step
     * <p>Pattern :
     * <blockquote><pre>@When("^(\\w+) exit$")</pre></blockquote>
     * <p>Example : 
     * <blockquote><pre>When DesktopAgent exit</pre></blockquote>
     * <blockquote><pre>When KafkaAgent exit</pre></blockquote>
     * @since 1.5
	 * @see fast.common.agents.WebBrowserAgent#close()
	 * @see fast.common.agents.UiaAgent#close()
	 * @see fast.common.agents.messaging.KafkaAgent#close()
	 * @see fast.common.agents.messaging.MessagingQueueAgent#close()
	 * @see fast.common.agents.SshAgent#close()
	 * @see fast.common.agents.ElkAgent#close()
	 * @see fast.common.agents.DatabaseAgent#close()	 
	 * @see fast.common.agents.AvroAgent#close()
	 * <p>...</p>
     */
	@When("^(\\w+) exit$")
	public void exit(String agentName) throws Exception {
		AgentsManager.getInstance().getOrCreateAgent(agentName).close();
	}

	@When("^(.*) set (\\w+) to (\\{.*\\})(?: \\[(.*)\\])?$") // symbol is now
																// part of
																// template or
																// user tags
	public void setOrderBook(String agentName, String templateSetName, String orderBook, String userStr)
			throws Throwable {
		String processedUserStr = getScenarioContext().processString(userStr);
		IFixMessagingAgent fixMsgAgent = AgentsManager.getInstance().getOrCreateAgent(agentName);
		fixMsgAgent.setOrderBook(getScenarioContext(), templateSetName, orderBook, processedUserStr);
	}
	/**
	 * Start a agent
	 * @param agentName the name of agent to start
	 * <p>Pattern :
     * <blockquote><pre>@When("^(\\w+) start$")</pre></blockquote>
     * <p>Example : 
     * <blockquote><pre>Given LH2 start</pre></blockquote>
     * <blockquote><pre>When KafkaAgent start</pre></blockquote>
	 * @throws Throwable
	 * @see fast.common.agents.FixAgent#start()
	 * @see fast.common.agents.messaging.KafkaAgent#start()
	 * @see fast.common.agents.messaging.MessagingQueueAgent#start()
	 * <p>...</p>
	 * @since 1.5
	 */
	@Given("^(\\w+) start$")
	@When("^(\\w+) connect$")
	public void startAgent(String agentName) throws Throwable {
		IStartable agent =  AgentsManager.getInstance().getOrCreateAgent(agentName);
		agent.start();
		
	}
	/**
	 * Stops a agent
	 * @param agentName the name of agent to stop
	 * <p>Pattern :
     * <blockquote><pre>@When("^(\\w+) stop$")</pre></blockquote>
     * <p>Example : 
     * <blockquote><pre>Given LH2 stop</pre></blockquote>
     * <blockquote><pre>When KafkaAgent stop</pre></blockquote>
	 * @throws Throwable
	 * @see fast.common.agents.ElkAgent#close()
	 * @see fast.common.agents.messaging.KafkaAgent#close()
	 * @see fast.common.agents.messaging.MessagingQueueAgent#close()
	 * <p>...</p>
	 * @since 1.5
	 */
	@Given("^(\\w+) stop$")
	@When("^(\\w+) disconnect$")
	public void stopAgent(String agentName) throws Throwable {
		Agent agent = AgentsManager.getInstance().getOrCreateAgent(agentName);
		agent.close();
	}
	/**
	 * Sends a message by agentSenderName and receives a message by agentReceiverName
	 * @param agentSenderName the name of agent to send a message
	 * @param agentReceiverName the name of agent to receive a message
	 * @param varName the name of variable to save the received message 
	 * @param msgTemplate is used to create a message
	 * @param userstr is used to concatenate to msgTemplate for creating a message
	 * @throws Throwable
	 * @since 1.5
	 * @see #sendFixMessage(String, String, String, String)
	 * @see #receiveFixMessage(String, String, String, String)
	 */
	@When("^(\\w+) send and (\\w+) receive(?: \"(@\\w+)\")?(?: (\\w+))?(?: \\[(.*)\\])?$")
	public void sendAndReceiveFixMessage(String agentSenderName, String agentReceiverName, String varName,
			String msgTemplate, String userstr) throws Throwable {
		String processedUserstr = getScenarioContext().processString(userstr);
		IFixMessagingAgent senderAgent = AgentsManager.getInstance().getOrCreateAgent(agentSenderName);
		IFixMessagingAgent receiverAgent = AgentsManager.getInstance().getOrCreateAgent(agentReceiverName);
		senderAgent.sendMessage(getScenarioContext(), msgTemplate, varName, processedUserstr);
		receiverAgent.receiveMessage(getScenarioContext(), msgTemplate, varName,
				processedUserstr);
	}
	/**
	 * Sends a message with a template and user string and saves transmitted message into a variable
	 * @param agentName the name of messaging agent on which to run the step
	 * @param varName a variable to save transmitted message
	 * @param msgTemplate is used to create a message
	 * @param userstr is used to concatenate to msgTemplate for creating a message
	 * <p>Pattern :</p>
     * <blockquote><pre>@Then("^(\\w+) send(?: \"(@\\w+)\")?(?: (\\w+))?(?: \\[(.*)\\])?$")</pre></blockquote>
     * <p>Example : </p>
     * <blockquote><pre>When Client send "@clientRequestNew" Client_RequestNew [OrdType=LIMIT|OrderQty=1000|Side=BUY|Symbol=$Symbol|Price=100]</pre></blockquote>
	 * <p>Template is from configuration file:</p>
	 * <blockquote><pre> Client_RequestNew: # send</pre></blockquote>
     * <blockquote><pre>&nbsp;&nbsp;- SubTemplates: OrderBook_Clear_Buy|Test</pre></blockquote>
     * <blockquote><pre>&nbsp;&nbsp;- MsgType: NewOrderSingle<<>>String<<>>Equal</pre></blockquote>
     * <blockquote><pre>&nbsp;&nbsp;- OrdType: LIMIT<<>>String<<>>NONEqual</pre></blockquote>
     * <blockquote><pre>&nbsp;&nbsp;- OrderQty: $OrderQty<<>>Number<<>>GREATER</pre></blockquote>
     * <blockquote><pre>&nbsp;&nbsp;- Currency: $Currency<<>>String<<>>Equal</pre></blockquote>
     * <blockquote><pre>&nbsp;&nbsp;- Side: BUY<<>>String<<>>Equal</pre></blockquote>
     * <blockquote><pre>&nbsp;&nbsp;- Symbol: $Symbol</pre></blockquote>
     * <blockquote><pre>&nbsp;&nbsp;- Price: $Price<<>>Number<<>>Equal</pre></blockquote>
     * <blockquote><pre>&nbsp;&nbsp;- ClOrdID: '%generateClOrdID()%<<>>String<<>>Equal'</pre></blockquote>
     * <blockquote><pre>&nbsp;&nbsp;- TimeInForce: DAY</pre></blockquote>
	 * @throws Throwable
	 * @since 1.5
	 * @see fast.common.agents.FixAgent#sendMessage(fast.common.context.ScenarioContext, String, String, String)
	 * @see fast.common.agents.FixEmsAgent#sendMessage(fast.common.context.ScenarioContext, String, String, String)
	 * @see fast.common.agents.FixTcpAgent#sendMessage(fast.common.context.ScenarioContext, String, String, String)
	 */
	// When <Agent> send ["@varname"] [Template name] [tags]
	@When("^(\\w+) send(?: \"(@\\w+)\")?(?: (\\w+))?(?: \\[(.*)\\])?$")
	public void sendFixMessage(String agentName, String varName, String msgTemplate, String userstr) throws Throwable {
		String processedUserstr = getScenarioContext().processString(userstr);
		IFixMessagingAgent fixMsgAgent = AgentsManager.getInstance().getOrCreateAgent(agentName);
		StepResult lastStepResult = fixMsgAgent.sendMessage(getScenarioContext(), msgTemplate, varName, processedUserstr);
		scenarioAndLogWrite(agentName + " sent: " + lastStepResult.toString());
	}
	/**
	 * Sends a message with a variable and user string
	 * @param agentName the name of messaging agent on which to run the step
	 * @param newVarName a variable to save the transmitted message
	 * @param lastVarName a variable stores results of previous step
	 * @param userstr is used to concatenate to previous results for creating a message
	 * <p>Pattern :
     * <blockquote><pre>@Then("^(\\w+) send(?: \"(@\\w+)\")?(?: \"([^\"]*)\")?(?: \\[(.*)\\])?$")</pre></blockquote>
     * <p>Example : 
     * <blockquote><pre>When FixAgent_CRIO send "@newOrder" "@previous_result" [35=8|30007=ANY]</pre></blockquote>
	 * @throws Throwable
	 * @since 1.5
	 * @see #sendFixMessage(String, String, String, String)
	 */
	// When <Agent> send ["@newVarname"] [@latestVarname] [user tags]
	@When("^(\\w+) send(?: \"(@\\w+)\")?(?: \"([^\"]*)\")?(?: \\[(.*)\\])?$")
	public void sendFixMessageVariable(String agentName, String newVarName,
			String lastVarName, String userstr) throws Throwable {
		FixStepResult fixStepResult = (FixStepResult) getScenarioContext()
				.getVariable(lastVarName);
		String processMsg = fixStepResult.getActualMessage();
		String processedUserstr = getScenarioContext().processString(userstr);
		String msgString = (processMsg.endsWith("|") == true) ? processMsg
				+ processedUserstr : processMsg + "|" + processedUserstr;
		sendFixMessage(agentName, newVarName, null, msgString);
	}
	/**
	 * Sends a message with a template and customize table and saves transmitted message into a variable
	 * @param agentName the name of messaging agent on which to run the step
	 * @param varName a variable to save the transmitted message
	 * @param msgTemplate creates a message
	 * @param table is converted into user string and used to concatenate to template for creating a message 
	 * @throws Throwable
	 * @since 1.5
	 * @see #sendFixMessage(String, String, String, String)
	 */
	@When("^(\\w+) send(?: \"(@\\w+)\")?(?: (\\w+))?:$")
	public void sendFixMessageTable(String agentName, String varName, String msgTemplate, DataTable table)
			throws Throwable {
		String userstr = MapMessageTemplateHelper.tableToUserstr(table);
		sendFixMessage(agentName, varName, msgTemplate, userstr);
	}
	/**
	 * Sends a message for several times
	 * @param agentName the name of messaging agent on which to run the step
	 * @param count sending times of this message
	 * @param varName a variable to save the transmitted message
	 * @param msgTemplate is used to create a message
	 * @param userstr is used to concatenate to template for creating a message 
	 * @throws Throwable
	 * @since 1.5
	 * @see #sendFixMessage(String, String, String, String)
	 */
	@When("^(.*) send (\\d+) x (?:\"(@\\w+)\" )?(?:(\\w+) )?(?:\\[(.*)\\])?$")
	public void sendNtimesFixMessage(String agentName, int count, String varName, String msgTemplate, String userstr)
			throws Throwable {
		String processedUserstr = getScenarioContext().processString(userstr);
		IFixMessagingAgent fixMsgAgent = AgentsManager.getInstance().getOrCreateAgent(agentName);

		for (int i = 0; i < count; i++) {
			fixMsgAgent.sendMessage(getScenarioContext(), msgTemplate, varName, processedUserstr);
		}
	}
	

	String scname = "";
	List<String> names;
	/**
	 * Finds the best match message from received messages with a template and user string and saves it into a variable
	 * @param agentName the name of messaging agent on which to run the step
	 * @param varName a variable to save received message
	 * @param msgTemplate is used to create the excepted message
	 * @param userstr is used to concatenate to msgTemplate for creating the excepted message
	 * <p>Pattern :
     * <blockquote><pre>@Then("^(\\w+) receive(?: \"(@\\w+)\")?(?: (\\w+))?(?: \\[(.*)\\])?$")</pre></blockquote>
     * <p>Example : 
     * <blockquote><pre>Then Client receive ReportFullfill [35=8|39=2]</pre></blockquote>
	 * @throws Throwable
	 * @since 1.5
	 * @see #sendFixMessage(String, String, String, String)
	 * @see fast.common.agents.FixAgent#receiveMessage(fast.common.context.ScenarioContext, String, String, String)
	 * @see fast.common.agents.FixEmsAgent#receiveMessage(fast.common.context.ScenarioContext, String, String, String)
	 * @see fast.common.agents.FixTcpAgent#receiveMessage(fast.common.context.ScenarioContext, String, String, String)
	 */
	@Then("^(\\w+) receive(?: \"(@\\w+)\")?(?: (\\w+))?(?: \\[(.*)\\])?$")
	public void receiveFixMessage(String agentName, String varName, String msgTemplate, String userstr)
			throws Throwable {
		IFixMessagingAgent fixMsgAgent = AgentsManager.getInstance().getOrCreateAgent(agentName);
		boolean ok = true;
		StepResult str = null;
		if (msgTemplate != null) {
			if (msgTemplate.contains("TraderLogonRequest") || agentName.contains("Simulator")) {
				ok = false;
			}
		}
		if (ok) {
			if (getScenarioContext() != null) {
				Scenario scenario = getScenarioContext().getScenario();
				scname = scenario.getName();

			}

			try {

				StepResult sr = fixMsgAgent.receiveMessage(getScenarioContext(), msgTemplate, varName, userstr);
				str = sr;

			} catch (Exception xep) {
				appendLine(scname + "~FAILED", "");
			}
		} else {
			if (agentName.contains("Simulator")) {
				try {

					StepResult sr = fixMsgAgent.receiveMessage(getScenarioContext(), msgTemplate, varName, userstr);
					str = sr;

				} catch (Exception xep) {
					appendLine(scname + "~FAILED", "");
				}
			}
		}
		StepResult lastStepResult = (str == null)
				? fixMsgAgent.receiveMessage(getScenarioContext(), msgTemplate, varName, userstr) : str;
		scenarioAndLogWrite(agentName + " received: " + lastStepResult.toString());
		writeTag37(lastStepResult);

		if (msgTemplate != null) { // always save under template name if it is
									// defined
			getScenarioContext().saveLastStepResult(lastStepResult, "@" + msgTemplate);
		}
		if (ok) {
			if (names == null) {
				names = new ArrayList<>();
			}

			if (lastStepResult != null) {
				if (lastStepResult.toString().contains("37=")) {
					String val = parseFrom(lastStepResult.toString(), "37=", "" + '\u0001');
					char[] cva = val.toCharArray();
					String idvalue = "";
					for (char vv : cva) {
						if (vv != '\u0001') {
							idvalue = idvalue + vv;
						} else {
							break;
						}
					}

					if (!names.contains(scname)) {
						appendLine(scname + "~PASSED", idvalue);
						names.add(scname);
					}
				}
			}
		}
		getScenarioContext().saveLastStepResult(lastStepResult, varName);
	}
	/**
	 * Finds the best match message from received messages with a template and customize table and saves it into a variable
	 * @param agentName the name of messaging agent on which to run the step
	 * @param varName a variable to save received message
	 * @param msgTemplate is used to create the excepted message
	 * @param table is used to concatenate to msgTemplate for creating the excepted message
	 * @throws Throwable
	 * @since 1.5
	 * @see #receiveFixMessage(String, String, String, String)
	 */

	@Then("^(\\w+) receive(?: \"(@\\w+)\")?(?: (\\w+))?:$")
	public void receiveFixMessageTable(String agentName, String varName, String msgTemplate, DataTable table)
			throws Throwable {
		String userstr = MapMessageTemplateHelper.tableToUserstr(table);
		receiveFixMessage(agentName, varName, msgTemplate, userstr);
	}
	
	@Then("^(\\w+) receive and validate(?: \"(@\\w+)\")?(?: (\\w+))?(?: \\[(.*)\\])?$")
	public void receiveAndVerifyMessage(String agentName, String varName, String msgTemplate, String userstr)
			throws Throwable {
		IFixMessagingAgent fixMsgAgent = AgentsManager.getInstance().getOrCreateAgent(agentName);
		fixMsgAgent.receiveAndVerifyMessage(getScenarioContext(), msgTemplate, varName, userstr);
	}
	/**
	 * Checks the messaging agent not receives the specified message
	 * @param agentName the name of messaging agent on which to run the step
	 * @param varName is not used
	 * @param msgName a msgTemplate, is used to create the excepted message
	 * @param userstr is used to concatenate to msgTemplate for creating the excepted message
	 * @throws Throwable
	 * @since 1.5
	 */
	@Then("^(\\w+) not receive(?: \"(@\\w+)\")?(?: (\\w+))?(?: \\[(.*)\\])?$")
	public void notReceiveFixMessage(String agentName, String varName, String msgName, String userstr)
			throws Throwable {
		IFixMessagingAgent fixMsgAgent = AgentsManager.getInstance().getOrCreateAgent(agentName);

		fixMsgAgent.notReceiveMessage(getScenarioContext(), msgName, userstr);
	}
	
	/**
	 * Validates received message with a template and user string 
	 * @param varName a variable saves previous received message
	 * @param msgTemplate is used to create the excepted message
	 * @param userstr is used to concatenate to msgTemplate for creating the excepted message
	 * <p>Pattern :
     * <blockquote><pre>@Then("^validate fix(?: \"(@\\w+)\")?(?: (\\w+))?(?: \\[(.*)\\])?$")</pre></blockquote>
     * <p>Example : 
     * <blockquote><pre>Then validate fix "@validateResult" RequestNew [Side=SELL|528=P|30007=U,W,Y,P|10202=EMEADSA]</pre></blockquote>
	 * @throws Throwable
	 * @since 1.5
	 * @see #sendFixMessage(String, String, String, String)
	 */
	@Then("validate fix(?: \"(@\\w+)\")?(?: (\\w+))?(?: \\[(.*)\\])?$")
	public void validateFixMessage(String varName, String msgTemplate, String userstr) throws Throwable{
		String processedUserstr = getScenarioContext().processString(userstr);
		FixStepResult fixStepResult = (FixStepResult) getScenarioContext().getVariable(varName);
		String result =fixStepResult.validate(getScenarioContext(),fixStepResult.toString(),msgTemplate,processedUserstr);
		scenarioAndLogWrite(result);
	}
	/**
	 * Checks previous received message contains the specified string
	 * @param userstr excepted string
	 * <p>Pattern :
     * <blockquote><pre>@Then("^it contains \\[(.*)\\]$")</pre></blockquote>
     * <p>Example : 
     * <blockquote><pre>Then Client receive "@clientConfirmNew" ConfirmNew [11=@clientRequestNew.11]</pre></blockquote>
     * <blockquote><pre>And it contains [Symbol=@clientRequestNew.Symbol|OrdType=@clientRequestNew.OrdType|Price=@clientRequestNew.Price|OrderQty=@clientRequestNew.OrderQty|Side=@clientRequestNew.Side]</pre></blockquote>
	 * @throws Throwable
	 * @since 1.5
	 * @see #sendFixMessage(String, String, String, String)
	 * @see #var_contains(String, String)
	 */
	@Then("^it contains \\[(.*)\\]$")
	public void it_contains(String userstr) throws Throwable {
		String processedUserstr = getScenarioContext().processString(userstr);
		((IStringResult)getScenarioContext().getLastResultVariable()).contains(processedUserstr);
		scenarioAndLogWrite("Contains: " + processedUserstr);
	}
	/**
	 * Checks previous received message contains the specified string
	 * @param varName a variable stores previous received message
	 * @param userstr  excepted string
	 * <p>Pattern :
     * <blockquote><pre>@Then("^(@\\w+) contains \\[(.*)\\]$")</pre></blockquote>
     * <p>Example : 
     * <blockquote><pre>@confirm contains [11=@requestNew.11]</pre></blockquote>
	 * @throws Throwable
	 * @since 1.5
	 * @see #sendFixMessage(String, String, String, String)
	 * @see #it_contains(String)
	 */
	@Then("^(@\\w+) contains \\[(.*)\\]$")
	public void var_contains(String varName, String userstr) throws Throwable {
		String processedUserstr = getScenarioContext().processString(userstr);
		((IStringResult)getScenarioContext().getVariable(varName)).contains(processedUserstr);
		scenarioAndLogWrite("Contains: " + processedUserstr);
	}
	/**
	 * Checks previous received message does not contain the specified string
	 * @param userstr excepted string
	 * <p>Pattern :
     * <blockquote><pre>@Then("^it does not contain \\[(.*)\\]$")</pre></blockquote>
     * <p>Example : 
     * <blockquote><pre>Then Client receive "@clientConfirmNew" ConfirmNew [11=@clientRequestNew.11]</pre></blockquote>
     * <blockquote><pre>And it does not contain [Symbol=@clientRequestNew.Symbol|OrdType=@clientRequestNew.OrdType|Price=@clientRequestNew.Price|OrderQty=@clientRequestNew.OrderQty|Side=@clientRequestNew.Side]</pre></blockquote>
	 * @throws Throwable
	 * @since 1.5
	 * @see #sendFixMessage(String, String, String, String)
	 * @see #var_not_contains(String, String)
	 */
	@Then("^it does not contain  \\[(.*)\\]$")
	public void it_not_contains(String userstr) throws Throwable {
		String processedUserstr = getScenarioContext().processString(userstr);
		((IStringResult)getScenarioContext().getLastResultVariable()).not_contains(processedUserstr);
		scenarioAndLogWrite("Doesn't contain: " + processedUserstr);
	}
	/**
	 * Checks previous received message does not contain the specified string
	 * @param varName a variable stores previous received message
	 * @param userstr  excepted string
	 * <p>Pattern :
     * <blockquote><pre>@Then("^(@\\w+) contains \\[(.*)\\]$")</pre></blockquote>
     * <p>Example : 
     * <blockquote><pre>@confirm does not contain [11=@requestNew.11]</pre></blockquote>
	 * @throws Throwable
	 * @since 1.5
	 * @see #sendFixMessage(String, String, String, String)
	 * @see #it_not_contains(String)
	 */
	@Then("^(@\\w+) does not contain \\[(.*)\\]$")
	public void var_not_contains(String varName, String userstr) throws Throwable {
		String processedUserstr = getScenarioContext().processString(userstr);
		((IStringResult)getScenarioContext().getVariable(varName)).not_contains(processedUserstr);
		scenarioAndLogWrite("Doesn't contain: " + processedUserstr);
	}

	@When("^(\\w+) run scenario \"([^\\\"]*)\"$")
	public void run_scenario_from_database(String agentName, String scenarioName) throws Exception {
		String processedScenarioname = getScenarioContext().processString(scenarioName);
		AgentsManager.getInstance().getOrCreateAgent(agentName).run("runScenarioFromDatabase", processedScenarioname);
	}

	@When("^(\\w+) run \"([^\\\"]*)\"$")
	public void run_senarios_from_database_by_query(String agentName, String sqlQuery) throws Exception {
		String processedRecodingName = getScenarioContext().processString(sqlQuery);
		AgentsManager.getInstance().getOrCreateAgent(agentName).run("runScenariosFromDatabaseByQuery", processedRecodingName);
	}
	private DNAStepResult runDNAQuery(String agentName, String service, String queryValue, boolean shouldPoll) throws Throwable {
		String processedService = getScenarioContext().processString(service);
		String processedQueryValue = getScenarioContext().processString(queryValue);
		logger.info("DNA Service Name : " + processedService + "--" + "Query : " + processedQueryValue);
		
		DNAStepResult tableResult = null;
		AgentDNA dnaAgent = (AgentDNA)AgentsManager.getInstance().getOrCreateAgent(agentName);
		
		if(shouldPoll) {
			tableResult = dnaAgent.executeQueryWithPoll(processedService, processedQueryValue);
		} else {
			tableResult = dnaAgent.sendQuery(processedService, processedQueryValue);
		}
		if(tableResult != null) {
			logger.info("DNA query result is : " + tableResult.toString());
		} else {
			throw new FastException("DNA Agent returned null result");
		}
		return tableResult;
	}
	
	/**
	 * 
	 * <p>Execute query and saves results into a variable.
	 * 
	 * @param agentName the name of agent on which to run the step
	 * @param sendTopic send a topic to connect the DNA server
	 * @param varName a variable to save results
	 * @param queryValue query string
	 * <p>Pattern :
     * <blockquote><pre>@Then("^(\\w+) (.*) execute(?: \"(@\\w+)\")? \'(.*)\'$")</pre></blockquote>
     * <blockquote><pre>@When("^(\\w+) (.*) execute(?: \"(@\\w+)\")? \"(.*)\"$")</pre></blockquote>
     * <p>Example : 
     * <blockquote><pre>Given DNA CRD.US.INVENTORY.ANALYTICS.RT execute "@Data4" "select from output"</pre></blockquote>
	 * @see fast.common.agents.AgentDNA#sendQuery(String, String)
	 * @see #dnaExecuteAndCheck(String, String, String, String, DataTable)
	 * @see #dnaExecuteAndCheckAll(String, String, String, String, DataTable)
	 * @see #dnaExecuteAndCheckValue(String, String, String, String, DataTable)
	 */
	@Then("^(\\w+) (.*) execute(?: \"(@\\w+)\")? \'(.*)\'$")
	@When("^(\\w+) (.*) execute(?: \"(@\\w+)\")? \"(.*)\"$")
	public void dnaExecuteAndCheck(String agentName, String sendTopic, String varName, String queryValue)
			throws Throwable {
		
		DNAStepResult tableResult = runDNAQuery(agentName, sendTopic, queryValue, false);
		getScenarioContext().saveLastStepResult(tableResult, varName); 
		writeReport(tableResult.toString());
	}
	
	/**
	 * 
	 * <p>Execute query with polling time and saves results into a variable.
	 * 
	 * @param agentName the name of agent on which to run the step
	 * @param sendTopic send a topic to connect the DNA server
	 * @param varName a variable to save results
	 * @param queryValue query string
	 * <p>Pattern :
     * <blockquote><pre>@Then("^(\\w+) (.*) execute with poll(?: \"(@\\w+)\")? \'(.*)\'$")</pre></blockquote>
     * <blockquote><pre>@When("^(\\w+) (.*) execute with poll(?: \"(@\\w+)\")? \"(.*)\"$")</pre></blockquote>
     * <p>Example : 
     * <blockquote><pre>Given DNA CRD.US.INVENTORY.ANALYTICS.RT execute "@Data4" "select from output"</pre></blockquote>
	 * @see fast.common.agents.AgentDNA#executeQueryWithPoll(String, String)
	 */
	@Then("^(\\w+) (.*) execute with poll(?: \"(@\\w+)\")? \'(.*)\'$")
	@When("^(\\w+) (.*) execute with poll(?: \"(@\\w+)\")? \"(.*)\"$")
	public void dnaExecuteAndPollAndCheck(String agentName, String sendTopic, String varName, String queryValue)
			throws Throwable {
		
		DNAStepResult tableResult = runDNAQuery(agentName, sendTopic, queryValue, true);
		writeReport(tableResult.toString());
		getScenarioContext().saveLastStepResult(tableResult, varName);
	}
	
	/**
	 * 
	 * <p>Execute query and check whether a excepted value in a specified column  
	 * @param agentName the name of agent on which to run the step
	 * @param sendTopic send a topic to connect the DNA server
	 * @param varName a variable to save query results
	 * @param queryValue query string
	 * @param table a data table to check excepted value, only support a cell value to check
	 * <p>Pattern :
     * <blockquote><pre>@Then("^(\\w+) (.*) execute(?: \"(@\\w+)\")? \'(.*)\' and check value:$")</pre></blockquote>
     * <blockquote><pre>@When("^(\\w+) (.*) execute(?: \"(@\\w+)\")? \"(.*)\" and check value:$")</pre></blockquote>
     * <p>Example : 
     * <blockquote><pre>Given DNA CRD.US.INVENTORY.ANALYTICS.RT execute "@Data4" "select from output" and check value: </pre></blockquote>
     * <blockquote><pre> |dnContract|</pre></blockquote>
     * <blockquote><pre> |true| </pre></blockquote>
     * 
	 * @see fast.common.agents.AgentDNA#sendQuery(String, String)
	 * @see #dnaExecuteAndCheck(String, String, String, String)
	 * @see #dnaExecuteAndCheck(String, String, String, String, DataTable)
	 * @see #dnaExecuteAndCheckAll(String, String, String, String, DataTable)
	 */
	@Then("^(\\w+) (.*) execute(?: \"(@\\w+)\")? \'(.*)\' and check value:$")
	@When("^(\\w+) (.*) execute(?: \"(@\\w+)\")? \"(.*)\" and check value:$")
	public void dnaExecuteAndCheckValue(String agentName, String sendTopic, String varName, String queryValue,
			DataTable table) throws Throwable {
		List<Map<String, String>> expectedMaps = table.asMaps(String.class, String.class); // "table"
																							// is
																							// table
																							// from
																							// feature
																							// TC
																							// #9
		String processedTableColumn = getScenarioContext().processString(table.topCells().get(0));
		String processedTableValue = getScenarioContext().processString(expectedMaps.get(0).get(processedTableColumn));

		DNAStepResult tableResult = runDNAQuery(agentName, sendTopic, queryValue, false);
		String tableResultValue = tableResult.getFieldValue(processedTableColumn); // actual
																					// value
		if (tableResultValue.equals(processedTableValue)) {
			getScenarioContext().saveLastStepResult(tableResult, varName);
		} else {
			throw new FastException("Value of cells doesn't match. Actual value: [" + tableResultValue
					+ "]. Expected value: [" + processedTableValue + "]");// Expected
																			// value
																			// -
																			// from
																			// feature
																			// table
		}
	}
	/**
	 * 
	 * <p>Execute query and check multiple columns value
	 * @param agentName the name of agent on which to run the step
	 * @param sendTopic send a topic to connect the DNA server
	 * @param varName a variable to save query results
	 * @param queryValue query string
	 * @param table a data table to check excepted value, only support a table with one row and multiple columns
	 * <p>Pattern :
     * <blockquote><pre>@Then("^(\\w+) (.*) execute(?: \"(@\\w+)\")? \'(.*)\' and check:$")</pre></blockquote>
     * <blockquote><pre>@When("^(\\w+) (.*) execute(?: \"(@\\w+)\")? \"(.*)\" and check:$")</pre></blockquote>
     * <p>Example : 
     * <blockquote><pre>Given DNA CRD.US.INVENTORY.ANALYTICS.RT execute with poll "@Data1" "select [1] from Toxicity where soeID = `generic" and check:</pre></blockquote>
     * <blockquote><pre> |aggUnit|deskId|</pre></blockquote>
     * <blockquote><pre> |503|6| </pre></blockquote>
     * 
	 * @see fast.common.agents.AgentDNA#sendQuery(String, String)
	 * @see #dnaExecuteAndCheck(String, String, String, String)
	 * @see #dnaExecuteAndCheckAll(String, String, String, String, DataTable)
	 * @see #dnaExecuteAndCheckValue(String, String, String, String, DataTable)
	 */
	@Then("^(\\w+) (.*) execute(?: \"(@\\w+)\")? \'(.*)\' and check:$")
	@When("^(\\w+) (.*) execute(?: \"(@\\w+)\")? \"(.*)\" and check:$")
	public void dnaExecuteAndCheck(String agentName, String sendTopic, String varName, String queryValue,
			DataTable table) throws Throwable {
		DNAStepResult tbl = new DNAStepResult();
		table = tbl.convertTableToFulltable(table, getScenarioContext());
		DNAStepResult tableResult = runDNAQuery(agentName, sendTopic, queryValue, false);
		tableResult.check(table);
		getScenarioContext().saveLastStepResult(tableResult, varName);
	}
	
	/**
	 * 
	 * <p>Execute query and check multiple columns value with polling interval
	 * @param agentName the name of agent on which to run the step
	 * @param sendTopic send a topic to connect the DNA server
	 * @param varName a variable to save query results
	 * @param queryValue query string
	 * @param table a data table to check excepted value, only support a table with one row and multiple columns
	 * <p>Pattern :
     * <blockquote><pre>@Then("^(\\w+) (.*) execute with poll(?: \"(@\\w+)\")? \'(.*)\' and check:$")</pre></blockquote>
     * <blockquote><pre>@When("^(\\w+) (.*) execute with poll(?: \"(@\\w+)\")? \"(.*)\" and check:$")</pre></blockquote>
     * <p>Example : 
     * <blockquote><pre>Given DNA CRD.US.INVENTORY.ANALYTICS.RT execute "@Data1" "select [1] from Toxicity where soeID = `generic" and check:</pre></blockquote>
     * <blockquote><pre> |aggUnit|deskId|</pre></blockquote>
     * <blockquote><pre> |503|6| </pre></blockquote>
     * 
	 * @see fast.common.agents.AgentDNA#sendQuery(String, String)
	 * @see #dnaExecuteAndCheck(String, String, String, String)
	 * @see #dnaExecuteAndCheckAll(String, String, String, String, DataTable)
	 * @see #dnaExecuteAndCheckValue(String, String, String, String, DataTable)
	 */
	@Then("^(\\w+) (.*) execute with poll(?: \"(@\\w+)\")? \'(.*)\' and check:$")
	@When("^(\\w+) (.*) execute with poll(?: \"(@\\w+)\")? \"(.*)\" and check:$")
	public void dnaExecuteAndCheckWithPoll(String agentName, String sendTopic, String varName, String queryValue,
			DataTable table) throws Throwable {
		DNAStepResult tbl = new DNAStepResult();
		table = tbl.convertTableToFulltable(table, getScenarioContext());
		DNAStepResult tableResult = runDNAQuery(agentName, sendTopic, queryValue, true);
		tableResult.check(table);
		getScenarioContext().saveLastStepResult(tableResult, varName);
	}
	
	/**
	 * 
	 * <p>Execute query and check excepted table with query results
	 * @param agentName the name of agent on which to run the step
	 * @param sendTopic send a topic to connect the DNA server
	 * @param varName a variable to save query results
	 * @param queryValue query string
	 * @param table a data table to check excepted valueï¼Œsupport a full table 
	 * <p>Pattern :
     * <blockquote><pre>@Then("^(\\w+) (.*) execute(?: \"(@\\w+)\")? \'(.*)\' and check all:$")</pre></blockquote>
     * <blockquote><pre>@When("^(\\w+) (.*) execute(?: \"(@\\w+)\")? \"(.*)\" and check all:$")</pre></blockquote>
     * <p>Example : 
     * <blockquote><pre>Given DNA CRD.US.INVENTORY.ANALYTICS.RT execute "@Data1" "select [2] aggUnit,deskId, tierValue, tierType from Toxicity where soeID = `generic" and check all: </pre></blockquote>
     * <blockquote><pre> |aggUnit|deskId|tierValue|tierType|</pre></blockquote>
     * <blockquote><pre> |503|6|-1|-1|</pre></blockquote>
     * <blockquote><pre> |503|59|-1|-1| </pre></blockquote>
	 * @see fast.common.agents.AgentDNA#sendQuery(String, String)
	 * @see #dnaExecuteAndCheck(String, String, String, String)
	 * @see #dnaExecuteAndCheck(String, String, String, String, DataTable)
	 * @see #dnaExecuteAndCheckValue(String, String, String, String, DataTable)
	 */
	@Then("^(\\w+) (.*) execute(?: \"(@\\w+)\")? \'(.*)\' and check all:$")
	@When("^(\\w+) (.*) execute(?: \"(@\\w+)\")? \"(.*)\" and check all:$")
	public void dnaExecuteAndCheckAll(String agentName, String sendTopic, String varName, String queryValue,
			DataTable table) throws Throwable {
		DNAStepResult tbl = new DNAStepResult();
		table = tbl.convertTableToFulltable(table, getScenarioContext());
		DNAStepResult actualTable = runDNAQuery(agentName, sendTopic, queryValue, false);
		actualTable.checkAll(table);
		getScenarioContext().saveLastStepResult(actualTable, varName);
	}

	public void writeTag37(StepResult lastStepResult) throws Throwable {
		String tag37;
		try {
			if ((tag37 = lastStepResult.getFieldValue("37")) != null) {
				scenarioAndLogWrite("Tag 37: " + tag37);
				getScenarioContext().getTag37set().add(tag37);

				// save tag37 to log it later with scenario result
			}
		} catch (Exception e) { // it is ok - some messages might not have tag
								// 37
			logger.debug(String.format("Error during writeTag37(): %s", e.toString()));
		}
	}

	@Given("^(\\w+) start receiving heartbeats$")
	public void simulatorEstablishedConnection(String agentName) throws Throwable {
		FixTcpServerAgent fixTcpServerAgent = AgentsManager.getInstance()
				.getOrCreateAgent(agentName);
		fixTcpServerAgent.waitForEstablishedIncomingConnectionIfNeeded();

	}
	/**
	 * Sets a variable value with expression
	 * @param varName the name of variable to set value
	 * @param expression javascript expression or methods defined in {@link fast.common.context.EcmaScriptInterpreter}
	 * <p>Pattern :
     * <blockquote><pre>@When("^(@\\w+) = %(.*)%$")</pre></blockquote>
	 * <p>Example : 
     * <blockquote><pre>When @Tag52 = %generateTsFormat("yyyyMMdd-HH:mm:ss.SSS","UTC+08:00")%</pre></blockquote>
     * <blockquote><pre>When @Tag37 = %FormatNumber(getRandom(1,1000000),"1")+getRandom(1,10000)%</pre></blockquote>
	 * @throws Throwable
	 * @since 1.5
	 */
	// TODO: implement it - need to create new StepResult class and update
	// processString
	@When("^(@\\w+) = %(.*)%$")
	public void setVariable(String varName, String expression) throws Throwable {
		String processedExpression = getScenarioContext().processString("%" + expression + "%");
		CommonStepResult stepResult = new CommonStepResult();
		stepResult.setFieldValue("Value", processedExpression);
		 getScenarioContext().saveLastStepResult(stepResult, varName);
	}
	
	
	/**
	 * Writes messages to report
	 * @param message message to write
	 * @since 1.5
	 */
	public void writeReport(String message) {
		Scenario scenario = getScenarioContext().getScenario();
		scenario.write(message);
	}
	/**
	 * <p>Sends commands to remote Unix server.
	 * 
	 * @param agentName the name of agent on which to run the step
	 * @param command unix commands with ";" as a delimiter 
	 * @param varName a variable to save results
	 * <p>Pattern :
     * <blockquote><pre>@When("^(\\w+) send command with \"([^\"]*)\" into (@\\w+)")</pre></blockquote>
	 * <p>Example : 
     * <blockquote><pre>Then SshAgent send command with "ls -lrt;su dm18232;bf d2 ca 99 c9 8a 17 aa 97 79 10 fe a2 00 29 f6 ;cd tmp;ls" into @value</pre></blockquote>
	 * @since 1.5
	 * @see fast.common.agents.SshAgent#sendCommand(String)
	 */
	@When("^(\\w+) send command with \"([^\"]*)\" into (@\\w+)")
	public void runCommand(String agentName, String command, String varName) throws Throwable {
		String processedCommand = getScenarioContext().processString(command);
		StepResult result = AgentsManager.getInstance().getOrCreateAgent(agentName).runWithResult("sendCommand", processedCommand);
		getScenarioContext().saveLastStepResult(result, varName);
	}
	/**
	 * <p>Uploads a file from local to remote Unix server.
	 * 
	 * @param agentName the name of agent on which to run the step
	 * @param source source file location
	 * @param destination destination location
	 * <p>Pattern :
     * <blockquote><pre>@When("^(\\w+) upload file from \"([^\"]*)\" to \"([^\"]*)\"$")</pre></blockquote>
	 * <p>Example : 
     * <blockquote><pre>When SshAgent upload file from "C://Users//dm18232//Temp//uploadtest.txt" to "/tmp/test/"</pre></blockquote>
	 * @since 1.5
	 * @see fast.common.agents.SshAgent#transferFile(String,String,String)
	 */
	@When("^(\\w+) upload file from \"([^\"]*)\" to \"([^\"]*)\"$")
	public void uploadFile(String agentName, String source, String destination) throws Throwable {
		AgentsManager.getInstance().getOrCreateAgent(agentName).runWithResult("transferFile", "upload",source, destination);
	}
	/**
	 * <p>Downloads a file from remote Unix server to local system.
	 * 
	 * @param agentName the name of agent on which to run the step
	 * @param source source file location
	 * @param destination destination location
	 * <p>Pattern :
     * <blockquote><pre>@When("^(\\w+) download file from \"([^\"]*)\" to \"([^\"]*)\"$")</pre></blockquote>
	 * <p>Example : 
     * <blockquote><pre>When SshAgent download file from "/tmp/test/download.txt" to "C://Users//dm18232//Temp//"</pre></blockquote>
	 * @since 1.5
	 * @see fast.common.agents.SshAgent#transferFile(String,String,String)
	 */
	@When("^(\\w+) download file from \"([^\"]*)\" to \"([^\"]*)\"$")
	public void downloadFile(String agentName, String source, String destination) throws Throwable {
		AgentsManager.getInstance().getOrCreateAgent(agentName).runWithResult("transferFile", "download",source, destination);
	}
	/**
	 * <p>Moves file from source location to destination location in remote server
	 * 
	 * @param agentName the name of agent on which to run the step
	 * @param source source location
	 * @param destination destination location
	 * <p>Pattern :
     * <blockquote><pre>@When("^(\\w+) move file from \"([^\"]*)\" to \"([^\"]*)\"$")</pre></blockquote>
	 * <p>Example : 
     * <blockquote><pre>Given SshAgent move file from "/home/da142332/spe_auto/336_file1A.txt" to "/home/da142332/landing_dir/"</pre></blockquote>
	 * @since 1.5
	 * @see fast.common.agents.SshAgent#transferFile(String,String,String)
	 */
	@When("^(\\w+) move file from \"([^\"]*)\" to \"([^\"]*)\"$")
	public void moveFile(String agentName, String source, String destination) throws Throwable {
		AgentsManager.getInstance().getOrCreateAgent(agentName).runWithResult("transferFile", "move",source, destination);

	}

	/**
	 *<p>Execute query to elk server and saves results into a variable.
	 * 
	 * @param agentName the name of agent on which to run the step
	 * @param index filter records with this index
	 * @param startTime filter records whose @timestamp is greater than startTime
	 * @param endTime filter records whose @timestamp is less than endTime
	 * @param queryString filter records with provided field and text from queryString, use "|" as a delimiter to combine multiple fields
	 * @param varName a variable to save results
	 * 
	 * <p>Pattern :
     * <blockquote><pre>@Then("^(\\w+) search index \"([^\"]*)\" startTime \"([^\"]*)\" endTime \"([^\"]*)\" with condition \"(.*)\" into (@\\w+)$")</pre></blockquote>
	 * <p>Example : 
     * <blockquote><pre>Then ELKAgent search index "logstash-*" startTime "2018-05-22T00:00:00.000+08:00" endTime "2018-05-22T23:59:59.999+08:00" with condition "message:BLACKROCK 8=FIX.4.4|beat.name:zts_zas" into @Table</pre></blockquote>
	 * @see fast.common.agents.ElkAgent#query(String, String,String,String)
	 * @see fast.common.agents.ElkAgent
	 * @since 1.5
	 */

	@Then("^(\\w+) search index \"([^\"]*)\" startTime \"([^\"]*)\" endTime \"([^\"]*)\" with condition \"(.*)\" into (@\\w+)$")
	public void elkQuery(String agentName, String index, String startTime, String endTime, String queryString, String varName) throws Throwable {
		String processedStartTime = getScenarioContext().processString(startTime);
		String processedEndTime = getScenarioContext().processString(endTime);
		ElkAgent elkAgent = AgentsManager.getInstance().getOrCreateAgent(agentName);
		StepResult result = elkAgent.query(index, processedStartTime, processedEndTime, queryString);
		getScenarioContext().saveLastStepResult(result, varName);
	}
	
	/**
	 *<p>Execute send request and saves results into a variable.
	 * 
	 * @param agentName the name of agent on which to run the step
	 * @param type the send request type
	 * @param uri the send request uri address
	 * @param varName a variable to save results
	 * 
	 * <p>Pattern :
     * <blockquote><pre>@When("^(\\w+) send request type \"([^\"]*)\" uri \"([^\"]*)\" into (@\\w+)$")</pre></blockquote>
	 * <p>Example : 
     * <blockquote><pre>When WEBAPIAgent send request type "GET" uri "XXXXXX" into @Result</pre></blockquote>
	 * @see fast.common.agents.WebApiAgent#sendRequest(RequestType, String)
	 * @see fast.common.agents.WebApiAgent
	 * @since 1.7
	 */
	@When("^(\\w+) send request type \"([^\"]*)\" uri \"([^\"]*)\" into (@\\w+)$")
	public void sendRequest(String agentName, String type, String uri, String varName) throws Throwable{
		RequestType processedType = RequestType.valueOf(getScenarioContext().processString(type));
		String processedUri = getScenarioContext().processString(uri);
		WebApiAgent webApiAgent = AgentsManager.getInstance().getOrCreateAgent(agentName);
		StepResult result = webApiAgent.sendRequest(processedType, processedUri);
		getScenarioContext().saveLastStepResult(result, varName);
	}
	
	/**
	 *<p>Execute send rest request and saves results into a variable.
	 * 
	 * @param agentName the name of agent on which to run the step
	 * @param type the send request type
	 * @param uri the send request uri address
	 * @param pathParams the uri address path params
	 * @param params the send request params
	 * @param body the send request body
	 * @param varName a variable to save results
	 * 
	 * <p>Pattern :
     * <blockquote><pre>@When("^(\\w+) send restRequest type \"([^\"]*)\" uri \"([^\"]*)\" pathParams \"([^\"]*)\" params \"([^\"]*)\" body \"([^\"]*)\" into (@\\w+)$")</pre></blockquote>
	 * <p>Example : 
     * <blockquote><pre>When WEBAPIAgent send restRequest type "GET" uri "XXXXXX/{secondPath}/{thirdPath}" pathParams "{'secondPath':'XXX','thirdPath':'YYY'}" params "{'XXX':'ZZZ','YYY':'WWW'}" body "{}" into @restResult</pre></blockquote>
	 * @see fast.common.agents.WebApiAgent#sendRestRequest(RequestType, String, Map<String, Object>, Map<String, Object>, Map<String, Object>)
	 * @see fast.common.agents.WebApiAgent
	 * @since 1.7
	 */
	@When("^(\\w+) send restRequest type \"([^\"]*)\" uri \"([^\"]*)\" pathParams \"([^\"]*)\" params \"([^\"]*)\" body \"([^\"]*)\" into (@\\w+)$")
	public void sendRestRequest(String agentName, String type, String uri, String pathParams, String params, String body,  String varName) throws Throwable{
		RequestType processedType = RequestType.valueOf(getScenarioContext().processString(type));
		String processedUri = getScenarioContext().processString(uri);
		Map<String,Object> processedPathParams = HelperMethods.processStringToMap(pathParams);
		Map<String,Object> processedParams = HelperMethods.processStringToMap(params);
		Map<String,Object> processedBody =HelperMethods.processStringToMap(body);
		WebApiAgent webApiAgent = AgentsManager.getInstance().getOrCreateAgent(agentName);
		StepResult result = webApiAgent.sendRestRequest(processedType, processedUri,processedPathParams,processedParams,processedBody);
		getScenarioContext().saveLastStepResult(result, varName);
	}
	
	/**
	 * Gets column values from table variable
	 * <p>The table supports results query from ELK, Database, DNA </p>
	 * @param varTable stores query results 
	 * @param field column name or keyword to filter specified data
	 * @param varName a variable to store the filter results
	 * <p>Pattern :
     * <blockquote><pre>@Then("^get (@\\w+)\\.(.*) values into (@\\w+)$")</pre></blockquote>
	 * <p>Example : 
     * <blockquote><pre>Then get @Table.ticker values into @ColumnValues</pre></blockquote>
	 * @throws Throwable
	 * @since 1.5
	 * @see fast.common.context.DNAStepResult#getFieldsValues(String)
	 * @see fast.common.context.ElkStepResult#getFieldsValues(String)
	 * @see fast.common.context.DatabaseStepResult#getFieldsValues(String)
	 */
	@Then("^get (@\\w+)\\.(.*) values into (@\\w+)$")
	public void getColumnValues( String varTable, String field,String varName) throws Throwable {
		StepResult stepResult = getScenarioContext().getVariable(varTable);
		ArrayList<String> fieldValues = stepResult.getFieldsValues(field.trim());
		setVariable(varName,  "'"+fieldValues.toString()+"'");
	}
	/**
	 * Gets a specified cell value from table variable
	 * <p>The table supports results query from ELK, Database, DNA </p>
	 * @param varTable stores query results 
	 * @param rowIndex row index
	 * @param columnName column name or keyword to filter specified data
	 * @param varName a variable to store the filter results
	 * @return
	 * <p>Pattern :
     * <blockquote><pre>@Then("^get (@\\w+)<(.*),(.*)> value into (@\\w+)$")</pre></blockquote>
	 * <p>Example : 
     * <blockquote><pre>Then get @Table<3,fii> value into @CellValue</pre></blockquote>
	 * @throws Throwable
	 * @since 1.5
	 * @see fast.common.context.DNAStepResult#getCellValue(String, String)
	 * @see fast.common.context.ElkStepResult#getCellValue(String, String)
	 * @see fast.common.context.DatabaseStepResult#getCellValue(String, String)
	 */
	@Then("^get (@\\w+)<(.*),(.*)> value into (@\\w+)$")
	public String getCellValues( String varTable, String rowIndex,String columnName,String varName) throws Throwable {
		StepResult stepResult = getScenarioContext().getVariable(varTable);
		if(stepResult instanceof ITableResult) {
			ITableResult itableResult =(ITableResult)stepResult;
			String value = itableResult.getCellValue(rowIndex, columnName.trim());
			scenarioAndLogWrite("Get variable(" + varTable + "."+columnName+") value :" + value);
			setVariable(varName, "'"+value+"'");
			return value;
		}else{
			throw new RuntimeException("The result isn't a table");
		}	
	}
	
    /**
	 * <p>Execute query to database server and saves results into a variable.
	 * 
	 * @param agentName the name of agent on which to run the step
	 * @param sql query string
	 * @param varName a variable to save results
	 * 
	 * <p>Pattern :
     * <blockquote><pre>@When("^(\\w+) query with \"([^\"]*)\" into (@\\w+)")</pre></blockquote>
	 * <p>Example : 
     * <blockquote><pre>When DataBaseServerAgent query with "select top 10 * from AllocationDetail" into @Table</pre></blockquote>
	 * @see fast.common.agents.DatabaseAgent#query(String)
	 * @see #sqlUpdate(String, String, String)
	 * @see #dataCheck(String, String, String, String)
	 */
	@When("^(\\w+) query with \"([^\"]*)\" into (@\\w+)")
	public void sqlQuery(String agentName, String sql, String varName) throws Throwable {
		String processedQueryString = getScenarioContext().processString(sql);
		StepResult result = AgentsManager.getInstance().getOrCreateAgent(agentName).runWithResult("query", processedQueryString);
		getScenarioContext().saveLastStepResult(result, varName);
	}
	/**
	 * <p>Execute update operation to database server
	 * 
	 * @param agentName the name of agent on which to run the step
	 * @param sql update or insert string 
	 * @param varName a variable to save results
	 * 
	 * <p>Pattern :
     * <blockquote><pre>@When("^(\\w+) update with \"([^\"]*)\" into (@\\w+)")</pre></blockquote>
	 * <p>Example : 
     * <blockquote><pre>When DataBaseServerAgent update with "UPDATE AllocationDetail SET deskId = '13' WHERE sourceid = '123'" into @Table</pre></blockquote>
	 * @see fast.common.agents.DatabaseAgent#update(String)
	 * @see #sqlQuery(String, String, String)
	 * @see #dataCheck(String, String, String, String)
	 */
	@When("^(\\w+) update with \"([^\"]*)\" into (@\\w+)")
	public void sqlUpdate(String agentName, String sql, String varName) throws Throwable {
		String processedQueryString = getScenarioContext().processString(sql);
		StepResult result = AgentsManager.getInstance().getOrCreateAgent(agentName).runWithResult("update", processedQueryString);
		getScenarioContext().saveLastStepResult(result, varName);
	}
	
	/**
	 * Filter database query results with multiple conditions, and check whether the expectedField value of the filtered data equals to expectedValue
	 * @param varName the variable stores query results
	 * @param condition filter records with condition from query results
	 * @param expectedField the expectedField of filter records 
	 * @param expectedValue the expectedValue of filter records 
	 * <p>Pattern :
     * <blockquote><pre>@When("^Check table (@\\w+) with condition \"([^\"]*)\" where (\\w+) equals to \"([^\"]*)\"$")</pre></blockquote>
	 * <p>Example : 
     * <blockquote><pre>When Check table @Table with condition "EVENT_STATUS_DESC = File Load completed | " where EVENT_STATUS equals to "Completed"</pre></blockquote>
	 * @throws ColIndexOutofRangeException 
	 * @throws RowIndexOutofRangeException 
	 * @throws ColumnNotExistsException 
	 * @see fast.common.context.DatabaseStepResult#compareData(String, String, String)
	 * @see #sqlQuery(String, String, String)
	 * @see #sqlUpdate(String, String, String)
	 */
	@When("^Check table (@\\w+) with condition \"([^\"]*)\" where (\\w+) equals to \"([^\"]*)\"$")
	public void dataCheck(String varName, String condition, String expectedField, String expectedValue) 
					throws RowIndexOutofRangeException, ColIndexOutofRangeException, ColumnNotExistsException {
		DatabaseStepResult dataSet = ((DatabaseStepResult) getScenarioContext().getVariable(varName));
		dataSet.compareData(condition, expectedField, expectedValue);
	}
	/**
	 * Gets value of a variable
	 * @param varName the name of the variable
	 * @return value
	 * @since 1.5
	 * <p>Pattern :
     * <blockquote><pre>@When("^get (@\\w+\\.\\w+) value$")</pre></blockquote>
	 * <p>Example : 
     * <blockquote><pre>get @ColumnValues.Value value</pre></blockquote>
	 */
	@When("^get (@\\w+\\.\\w+) value$")
	public String getVariableValue(String varName) {
		String value = getScenarioContext().processString(varName);
		scenarioAndLogWrite("Get variable(" + varName + ") value :" + value);
		return value;
	}
	/**
	 * Gets the log of response message from Unix
	 * @param varName the name of variable 
	 * @return log results
	 * @since 1.5
	 * <p>Pattern :
     * <blockquote><pre>@When("^get (@\\w+\\.\\w+) value$")</pre></blockquote>
	 * <p>Example : 
     * <blockquote><pre>Then get @value log</pre></blockquote>
	 */
	@When("^get (@\\w+) log$")
	public String getVariableLog(String varName) {
		String value = getScenarioContext().getLastResultVariable().getLog();
		scenarioAndLogWrite("Get variable(" + varName + ") log :" + value);
		return value;
	}
	/**
	 * Converts a fix string to a rio fix message
	 * @param fixString a raw fix string
	 * @param varName the name of variable to store results
	 * <p>Pattern :
     * <blockquote><pre>@When("^convert (.*) to rio fix message (@\\w+)$")</pre></blockquote>
	 * <p>Example : 
     * <blockquote><pre>When convert $NewRequest to rio fix message @newRIO</pre></blockquote>
     * <blockquote><pre>When FixAgent_CRIO send "@NewRequest" raw fix message "@newRIO.Value"</pre></blockquote>
	 * @throws Throwable
	 * @since 1.5
	 * @see #sendRawFixMessage(String, String, String)
	 */
	@Then("^convert (.*) to rio fix message (@\\w+)$")
	public void converetToFixMessage(String fixString, String varName) throws Throwable {        
		String processFixString = getScenarioContext().processString(fixString);
		String rioMessage = RIOFixHelper.convertFixStringToRioMessage(processFixString);
		setVariable(varName, "\'" + rioMessage + "\'");
	}
	/**
	 * Converts a raw fix string to a fix message
	 * @param agentName the name of agent on which to run the step
	 * @param varName the name of variable to save the fix message
	 * @param rawMsg raw fix string
	 * @throws Throwable
	 * @since 1.5
	 * @see fast.common.agents.FixAgent#createFixMessage(String)
	 */
	@When("^(.*) set \"(@\\w+)\" to fix message \"([^\"]*)\"$")
	public void setToFixMessage(String agentName, String varName, String rawMsg) throws Throwable {
		IFixMessagingAgent fixMsgAgent = AgentsManager.getInstance().getOrCreateAgent(agentName);
		StepResult lastStepResult = fixMsgAgent.createFixMessage(rawMsg);
		getScenarioContext().saveLastStepResult(lastStepResult, varName);
	}
	/**
	 * Sends a raw fix message
	 * @param agentName the name of agent on which to run the step
	 * @param varName the name of a variable to store results
	 * @param rawMsg convert this raw fix string to fix message then send to target server
	 * <p>Pattern :
     * <blockquote><pre>@When("^(.*) send(?: \"(@\\w+)\")? raw fix message \'([^\']*)\'$")</pre></blockquote>
     * <blockquote><pre>@When("^(.*) send(?: \"(@\\w+)\")? raw fix message \"([^\"]*)\"$")</pre></blockquote>
	 * <p>Example : 
     * <blockquote><pre>When FixAgent_CRIO send "@NewRequest" raw fix message "@newRIO.Value"</pre></blockquote>
	 * @throws Throwable
	 * @since 1.5
	 * @see #sendFixMessage(String, String, String, String)
	 * @see #sendTextMessage(String, String, String)
	 * @see fast.common.agents.FixAgent#sendRawMessage(String)
     * @see fast.common.agents.FixEmsAgent#sendRawMessage(String)
     * @see fast.common.agents.FixTcpAgent#sendRawMessage(String)
	 */
	@Then("^(.*) send(?: \"(@\\w+)\")? raw fix message \'([^\']*)\'$")
	@When("^(.*) send(?: \"(@\\w+)\")? raw fix message \"([^\"]*)\"$")	
	public void sendRawFixMessage(String agentName, String varName, String rawMsg) throws Throwable {
		String processMsg = getScenarioContext().processString(rawMsg);
		IFixMessagingAgent fixMsgAgent = AgentsManager.getInstance().getOrCreateAgent(agentName);
		StepResult lastStepResult = fixMsgAgent.sendRawMessage(processMsg);
		getScenarioContext().saveLastStepResult(lastStepResult, varName);
		scenarioAndLogWrite(agentName + " sent: " + lastStepResult.toString());
	}
	/**
	 * Sends raw fix string without any processing
	 * @param agentName the name of agent on which to run the step
	 * @param varName the name of a variable to store results
	 * @param textMessage send this raw fix without converting to fix message
	 * <p>Pattern :
     * <blockquote><pre>@When("^(\\w+) send text message \"([^\"]*)\" \"([^\"]*)\"$")</pre></blockquote>
	 * <p>Example : 
     * <blockquote><pre>When Client send text message "@varChildOrd" "CommandName=SendChildOrder;UserId=12345;OrderId=<Tag37>;Symbol= <symbol>;useDQuote=false"</pre></blockquote>
	 * @throws Throwable
	 * @since 1.5
	 * @see #sendFixMessage(String, String, String, String)
	 * @see #sendRawFixMessage(String, String, String)
	 */
	@When("^(\\w+) send text message \"([^\"]*)\" \"([^\"]*)\"$")
	public void sendTextMessage(String agentName, String varName, String textMessage) throws Throwable {
		String processedTextMessage = getScenarioContext().processString(textMessage);
	    IMessagingAgent messagingAgent = AgentsManager.getInstance().getOrCreateAgent(agentName);
	    messagingAgent.send(processedTextMessage); 
		CommonStepResult stepResult = new CommonStepResult();
		stepResult.setFieldValue("Value", processedTextMessage);
		getScenarioContext().saveLastStepResult(stepResult, varName);
		scenarioAndLogWrite(agentName + " sent: " + stepResult.toString());
	}
	/**
	 * Receives raw string without any processing
	 * @param agentName the name of agent on which to run the step
	 * @param varName the name of a variable to store results
	 * <p>Pattern :
     * <blockquote><pre>@Then("^(\\w+) receive text message \"([^\"]*)\"$")</pre></blockquote>
	 * <p>Example : 
     * <blockquote><pre>Then Client receive text message "@varChildOrd"</pre></blockquote>
	 * @throws Throwable
	 * @since 1.8
	 * @see #receiveFixMessage(String, String, String, String)
	 */
	@Then("^(\\w+) receive text message \"([^\"]*)\"$")
	public void receiveTextMessage(String agentName, String varName) throws Throwable {		
	    IMessagingAgent messagingAgent = AgentsManager.getInstance().getOrCreateAgent(agentName);	    
		StepResult stepResult = messagingAgent.receive();
		getScenarioContext().saveLastStepResult(stepResult, varName);
	}
	/**
	 * Reads context of a file into a variable
	 * @param path file path
	 * @param var a variable to store file context
	 * @throws IOException
	 * @since 1.5
	 */
	@When("read text file \"([^\"]*)\" into (@\\w+)$")
	public void readTextFile(String path, String var) throws IOException {
		File f = new File(path);
        BufferedReader br = null;
        FileReader fileReader = null;
		String ret = null;
		
		try {
			fileReader = new FileReader(f);
			br = new BufferedReader(fileReader);
			String line = null;
			StringBuilder sb = new StringBuilder((int) f.length());
			while ((line = br.readLine()) != null) {
				sb.append(line).append("\n");
			}
			ret = sb.toString();
		} catch (FileNotFoundException e) {
			logger.error("File is not found with path : " + path);
		}
		finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					logger.error("BufferedReader closed with exception");
				}
			}
			if (fileReader != null) {
				try {
					fileReader.close();
				} catch (IOException e) {
					logger.error("BufferedReader closed with exception");
				}
			}
		}

		CommonStepResult stepResult = new CommonStepResult();
		stepResult.setFieldValue("Value", ret);
		getScenarioContext().saveLastStepResult(stepResult, var);
        
	}
	public static void appendLine(String sname, String scid) {
		String path = "runresult.csv";
		FileWriter fw = null;
		BufferedWriter bw = null;
		try {
			fw = new FileWriter(new File(path), true);
			 bw = new BufferedWriter(fw);

			bw.write(sname + "~" + scid);
			bw.newLine();
			bw.flush();
			bw.close();

		} catch (Exception x) {
			logger.error(x.getMessage());
		}finally{
			try {
				if (bw != null){
					bw.close();
				}
				if (fw != null) {
					fw.close();
				}

			} catch (IOException e) {
				// TODO Auto-generated catch block
				logger.error(e.getMessage());
			}
		}

	}
	/**
	 * Check a string whether contains a substring starts with a specified string and ends with another specified string
	 * @param body a string to check
	 * @param start start string
	 * @param end end string
	 * @return the matched substring
	 * @see #receiveFixMessage(String, String, String, String)
	 */
	public static String parseFrom(String body, String start, String end) {
		String result = "";

		try {
			Pattern regex = Pattern.compile(start + "(.*)" + end);
			Matcher regexMatcher = regex.matcher(body);

			while (regexMatcher.find()) {
				for (int i = 1; i <= regexMatcher.groupCount(); i++) {
					result = regexMatcher.group(1);
				}

			}
		} catch (PatternSyntaxException ex) {
			// Syntax error in the regular expression
		}

		return result;

	}
	
	/**
	 * <p>Get differDateTime variable endTime from variable startTime with format
	 * 
     * @param endTime variable endTime
	 * @param startTime variable startTime
	 * @param dateTimeFormat time format likes "yyyy-MM-dd HH:mm:ss"/"EEE MMM dd HH:mm:ss zzz yyyy"/"HH:mm:ss" and so on.
	 * @param varName a variable to save results
	 * 
	 * Notice: not support 00:15:30 later than 23:45:30. (support scope:00:00:00 ~ 23:59:59)
	 * 
	 * <p>Pattern :
     * <blockquote><pre>@Then("^get differDateTime \"([^\"]*)\" from \"([^\"]*)\" into (@\\w+)$")</pre></blockquote>
	 * <p>Example : 
     * <blockquote><pre>
     * Then get differDateTime "10:05:30" from "08:04:15" with format "HH:mm:ss" into @differTime
     * Then get differDateTime "2019-03-04 00:05:30" from "2019-03-01 08:04:15" with format "yyyy-MM-dd HH:mm:ss" into @differTime
     * Then get differDateTime "Fri Dec 14 19:50:23 CST 2013" from "Tue May 14 09:10:53 CST 2013" with format "EEE MMM dd HH:mm:ss zzz yyyy" into @differ
     * </pre></blockquote>
	 */
	@Then("^get differDateTime \"([^\"]*)\" from \"([^\"]*)\" with format \"([^\"]*)\" into (@\\w+)$")
	public void getDateTimeDifferAttributes(String endTime, String startTime, String dateTimeFormat, String varName) throws Throwable {
		String processedEndTime = getScenarioContext().processString(endTime);
		String processedStartTime = getScenarioContext().processString(startTime);
		DateTimeDifferStepResult result = DateTimeUtils.getDifferDateTimeAttributes(processedEndTime,processedStartTime,dateTimeFormat);
		getScenarioContext().saveLastStepResult(result, varName);
	}
	
}
