package fast.common.glue;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import cucumber.api.Scenario;
import cucumber.api.java.After;
import cucumber.api.java.Before;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import fast.common.agents.AgentsManager;
import fast.common.agents.IFixMessagingAgent;
import fast.common.agents.messaging.TibcoRvAgent;
import fast.common.context.FixStepResult;
import fast.common.context.StepResult;
import fast.common.logging.FastLogger;
/**
 * The {@code MessagingCommonStepDefs} class defines some common actions for Messaging agents, which can be used in different scenarios.
 * @author QA Framework Team
 * @since 1.5
 */
public class MessagingCommonStepDefs extends BaseCommonStepDefs {

	private static FastLogger logger = FastLogger.getLogger("MessagingCommonStepDefs");

	@Before	
	@Override
	public void beforeScenario(Scenario scenario) throws Exception {
		super.beforeScenario(scenario);
	}

	@After
	@Override
	public void afterScenario(Scenario scenario) throws Exception {
		super.afterScenario(scenario);
	}
	/**
	 * Sends a message by Kafka agent
	 * @param agentName the name of Kafka agent
	 * @param msg the fix string to send
	 * <p>Pattern :
     * <blockquote><pre>@When("^(\\w+) send Kafka message \"([^\"]*)\"$")</pre></blockquote>
     * <p>Example : 
     * <blockquote><pre>When KafkaAgent send Kafka message "This is the first kafka message"</pre></blockquote>
	 * @throws Exception
	 * @since 1.5
	 * @see fast.common.agents.messaging.KafkaAgent#send(Object)
	 */
	@When("^(\\w+) send Kafka message \"([^\"]*)\"$")
	public void sendKafkaMessageWithoutTopic(String agentName, String msg) throws Exception{
		AgentsManager.getInstance().getOrCreateAgent(agentName).run("send", msg);
		
	}
	/**
	 * Receives messages by Kafka agent
	 * @param agentName the name of Kafka agent
	 * <p>Pattern :
     * <blockquote><pre>@When("^(\\w+) receive Kafka message$")</pre></blockquote>
     * <p>Example : 
     * <blockquote><pre>When KafkaAgent receive Kafka message</pre></blockquote>
	 * @throws Exception
	 */
	@Then("^(\\w+) receive Kafka message$")
	public void receiveKafkaMessageWithoutTopic(String agentName) throws Exception{
		AgentsManager.getInstance().getOrCreateAgent(agentName).runWithResult("receive");
	}

	@When("^(\\w+) send Avro message$")
	public void sendAvroMessageWithoutTopic(String agentName) throws Exception{
		AgentsManager.getInstance().getOrCreateAgent(agentName).run("send");
		
	}
	/**
	 * Sends messages by Tibvo RV
	 * @param agentName the name of messaging agent on which to run the step
	 * @param varName a variable to save transmitted message
	 * @param templateName is used to create a message
	 * @param userstr is used to concatenate to msgTemplate for creating a message
	 * <p>Pattern :
     * <blockquote><pre>@Then("^(\\w+) send Tibco RV message(?: \"(@\\w+)\")?(?: (\\w+))?(?: \\[(.*)\\])?")</pre></blockquote>
     * <p>Example : 
     * <blockquote><pre>When TibcoRvAgent_DPM send Tibco RV message "@var" OrderBook_Clear_Buy1 [SYMBOL=C|BID=20]</pre></blockquote>
	 * @throws Exception
	 * @since 1.5
	 * @see fast.common.glue.CommonStepDefs#sendFixMessage(String, String, String, String)
	 */
	// When <Agent> send "@varname" <Template name]> [fields]
	@When("^(\\w+) send Tibco RV message(?: \"(@\\w+)\")?(?: (\\w+))?(?: \\[(.*)\\])?$")	
	public void sendTibrvMessage(String agentName, String varName, String templateName, String userstr) throws Exception {		
		TibcoRvAgent agent = AgentsManager.getInstance().getOrCreateAgent(agentName);
		agent.send(getScenarioContext(), varName, templateName, userstr);	
		scenarioAndLogWrite(String.format("Sent Tibco RV message [%s]", getScenarioContext().getVariable(varName).toString()));
	}	
	/**
	 * Receives messages by Tibvo RV
	 * @param agentName the name of messaging agent on which to run the step
	 * @param varName a variable to save received message
	 * @param templateName is used to create excepted message
	 * @param userstr is used to concatenate to msgTemplate for creating excepted message
	 * <p>Pattern :
     * <blockquote><pre>@Then("^(\\w+) receive Tibco RV message(?: \"(@\\w+)\")?(?: (\\w+))?(?: \\[(.*)\\])?$")</pre></blockquote>
     * <p>Example : 
     * <blockquote><pre>Then TibcoRvAgent_DPM receive Tibco RV message "@var" OrderBook_Clear_Buy2 [SYMBOL=C<<>>String<<>>Equal]</pre></blockquote>
	 * @throws Exception
	 * @since 1.5
	 * @see fast.common.glue.CommonStepDefs#sendFixMessage(String, String, String, String)
	 */
	@Then("^(\\w+) receive Tibco RV message(?: \"(@\\w+)\")?(?: (\\w+))?(?: \\[(.*)\\])?$")
	public void receiveTibrvMessage(String agentName, String varName, String templateName, String userstr) throws Exception {
		TibcoRvAgent agent = AgentsManager.getInstance().getOrCreateAgent(agentName);
		StepResult lastStepResult = agent.receive(getScenarioContext(), templateName, userstr);
		getScenarioContext().saveLastStepResult(lastStepResult, varName);
		logger.info(String.format("Received Tibco RV message [%s] and saved into variable %s", lastStepResult.toString(), varName));
	}	
	/**
	 * Validate received message with excepted message created by template and user string
	 * @param agentName the name of messaging agent on which to run the step
	 * @param varName a variable to save received message
	 * @param templateName is used to create excepted message
	 * @param userstr is used to concatenate to msgTemplate for creating excepted message
	 * <p>Pattern :
     * <blockquote><pre>@Then("^(\\w+) validate Tibco RV message(?: \"(@\\w+)\")?(?: (\\w+))?(?: \\[(.*)\\])?$")</pre></blockquote>
     * <p>Example : 
     * <blockquote><pre>Then TibcoRvAgent_DPM validate Tibco RV message "@var" OrderBook_Clear_Buy3 [SYMBOL=D<<>>String<<>>Equal|OrderQty=5000<<>>Number<<>>Less]</pre></blockquote>
	 * @throws Exception
	 * @since 1.5
	 * @see fast.common.glue.CommonStepDefs#sendFixMessage(String, String, String, String)
	 */
	@Then("^(\\w+) validate Tibco RV message(?: \"(@\\w+)\")?(?: (\\w+))?(?: \\[(.*)\\])?$")
	public void validateTibrvMessage(String agentName, String varName, String templateName, String userstr) throws Exception {
		TibcoRvAgent agent = AgentsManager.getInstance().getOrCreateAgent(agentName);
		String result = agent.validate(getScenarioContext(), templateName, userstr, getScenarioContext().getVariable(varName));
		scenarioAndLogWrite(result);
	}
	
	/**
	 * Start subscriber by Umb agent
	 * @param agentName the name of Umb agent
	 * <p>Pattern :
     * <blockquote><pre>@When("^(\\w+) start umb subscriber$")</pre></blockquote>
     * <p>Example : 
     * <blockquote><pre>When UmbAgent start umb subscriber</pre></blockquote>
	 * @throws Exception
	 * @since 1.7
	 * @see fast.common.agents.messaging.UmbAgent#startSubscriber()
	 */
	@When("^(\\w+) start umb subscriber$")
	public void startUmbSubscriber(String agentName) throws Exception{
		AgentsManager.getInstance().getOrCreateAgent(agentName).run("startSubscriber");
	}
	
	/**
	 * Shutdown subscriber by Umb agent
	 * @param agentName the name of Umb agent
	 * <p>Pattern :
     * <blockquote><pre>@Then("^(\\w+) shutdown umb subscriber$")</pre></blockquote>
     * <p>Example : 
     * <blockquote><pre>Then UmbAgent shutdown umb subscriber</pre></blockquote>
	 * @throws Exception
	 * @since 1.7
	 * @see fast.common.agents.messaging.UmbAgent#shutdownSubscriber()
	 */
	@Then("^(\\w+) shutdown umb subscriber$")
	public void shutdownUmbSubscriber(String agentName) throws Exception{
		AgentsManager.getInstance().getOrCreateAgent(agentName).run("shutdownSubscriber");
	}
	
	/**
	 * Start publisher by Umb agent
	 * @param agentName the name of Umb agent
	 * <p>Pattern :
     * <blockquote><pre>@When("^(\\w+) start umb publisher$")</pre></blockquote>
     * <p>Example : 
     * <blockquote><pre>When UmbAgent start umb publisher</pre></blockquote>
	 * @throws Exception
	 * @since 1.7
	 * @see fast.common.agents.messaging.UmbAgent#startPublisher()
	 */
	@When("^(\\w+) start umb publisher$")
	public void startUmbPublisher(String agentName) throws Exception{
		AgentsManager.getInstance().getOrCreateAgent(agentName).run("startPublisher");
	}
	
	/**
	 * Shutdown publisher by Umb agent
	 * @param agentName the name of Umb agent
	 * <p>Pattern :
     * <blockquote><pre>@Then("^(\\w+) shutdown umb publisher$")</pre></blockquote>
     * <p>Example : 
     * <blockquote><pre>Then UmbAgent shutdown umb publisher</pre></blockquote>
	 * @throws Exception
	 * @since 1.7
	 * @see fast.common.agents.messaging.UmbAgent#shutdownPublisher()
	 */
	@Then("^(\\w+) shutdown umb publisher$")
	public void shutdownUmbPublisher(String agentName) throws Exception{
		AgentsManager.getInstance().getOrCreateAgent(agentName).run("shutdownPublisher");
	}
	
	/**
	 * Sends a message by Umb agent
	 * @param agentName the name of Umb agent
	 * @param msg the string to send
	 * <p>Pattern :
     * <blockquote><pre>@Then("^(\\w+) send umb message \"([^\"]*)\"$")</pre></blockquote>
     * <p>Example : 
     * <blockquote><pre>Then UmbAgent send umb message "This is the first umb message"</pre></blockquote>
	 * @throws Exception
	 * @since 1.7
	 * @see fast.common.agents.messaging.UmbAgent#send(Object)
	 */
	@Then("^(\\w+) send umb message \"([^\"]*)\"$")
	public void sendUmbMessage(String agentName, String msg) throws Exception{
		AgentsManager.getInstance().getOrCreateAgent(agentName).run("send", msg);
		
	}
	
	/**
	 * Receives messages by Umb agent
	 * @param agentName the name of Umb agent
	 * @return StepResult received messages
	 * <p>Pattern :
     * <blockquote><pre>@Then("^(\\w+) receive umb message$")</pre></blockquote>
     * <p>Example : 
     * <blockquote><pre>Then UmbAgent receive umb message</pre></blockquote>
	 * @throws Exception
	 * @since 1.7
	 * @see fast.common.agents.messaging.UmbAgent#getSubscribedMessages()
	 */
	@Then("^(\\w+) receive umb message$")
	public StepResult receiveUmbMessage(String agentName) throws Exception{
		return AgentsManager.getInstance().getOrCreateAgent(agentName).runWithResult("receive");
	}
	
	/**
	 * Send build of message
	 * @param agentName the name of agent
	 * @param list of message
	 * <p>Pattern :
     * <blockquote><pre>@When("^(\\w+) send bulk of message using number of thread (\\d+) within (\\d+) minutes")</pre></blockquote>
     * <p>Example : 
     * <blockquote>
     * <pre>
     * When UmbAgent send bulk of message using number of thread 10 within 60 minutes
     * |message1|
     * |message2|
     * |message3|
     * 
     * if there is some special characters end of the message, you may add """ to make sure the special characters is not ignored
     * When UmbAgent send bulk of message using number of thread 10 within 60 minutes
     * |"message1"|
     * |"message2"|
     * |"message3"|
     * </pre></blockquote>
	 * @throws Exception
	 * @since 1.9
	 */
	@When("^(\\w+) send bulk of message using number of thread (\\d+) within (\\d+) minutes")
	public void sendBulkOfMessage(String agentName, int numberOfThread,int minutes, List<String> msgList) throws Exception {
		ExecutorService executor = Executors.newFixedThreadPool(numberOfThread);
		for (int i = 0; i < msgList.size(); i++) {
			String rawMsg = msgList.get(i).replace("\"", "");
			executor.execute(() -> {
				try {
					AgentsManager.getInstance().getOrCreateAgent(agentName).run("send", rawMsg);
				} catch (Exception e) {
					logger.error(e.getMessage());
				}
			});
		}
		executor.shutdown();
		if(!executor.awaitTermination(minutes, TimeUnit.MINUTES)){
			executor.shutdownNow();
		}
	}
	
	/**
	 * Send multiple messages in fix agent
	 * @param agentName the name of fix agent
	 * <p>Pattern :
     * <blockquote><pre>@When("^(.*) send \"([^\"]*)\" messages in \"([^\"]*)\" seconds (?:\"(@\\w+)\" )?(?:(\\w+) )?(?:\\[(.*)\\])?$")</pre></blockquote>
     * <p>Example : 
     * <blockquote><pre>When Client send "100" messages in "1" seconds "@clientRequestNew" Client_RequestNew_Sent [58=dm18232|11=[[MAG-]]%generateClOrdID()%|35=D]</pre></blockquote>
	 * @throws Exception
	 * @since 1.9
	 * @see fast.common.agents.messaging.UmbAgent#getSubscribedMessages()
	 */
	@When("^(.*) send \"(\\d+)\" messages in \"(\\d+)\" seconds (?:\"(@\\w+)\" )?(?:(\\w+) )?(?:\\[(.*)\\])?$")
	public void sendMultiMessagesInTimeWindow(String agentName, int messageCount, int  timeWindow, String varName, String msgTemplate, String userstr) throws Throwable {
		IFixMessagingAgent fixMsgAgent = AgentsManager.getInstance().getOrCreateAgent(agentName);
		FixStepResult result =  fixMsgAgent.sendMultiMessagesInTimeWindow(getScenarioContext(),messageCount,timeWindow, msgTemplate, userstr);
		getScenarioContext().saveLastStepResult(result, varName);

	}
}
