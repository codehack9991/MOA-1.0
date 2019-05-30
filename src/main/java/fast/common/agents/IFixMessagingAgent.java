package fast.common.agents;

import fast.common.context.FixStepResult;
import fast.common.context.ScenarioContext;
import fast.common.context.StepResult;
import quickfix.InvalidMessage;

public interface IFixMessagingAgent extends IStartable {
    public FixStepResult sendMessage(ScenarioContext scenarioContext, String templateName, String varName, String userStr) throws Throwable;
    public FixStepResult sendRawMessage(String msgName) throws Throwable; // for debugging

    public FixStepResult receiveMessage(ScenarioContext scenarioContext, String templateName, String varName, String userStr) throws Throwable;
    
    public FixStepResult receiveAndVerifyMessage(ScenarioContext scenarioContext, String templateName, String varName, String userStr) throws Throwable;

    public void setOrderBook(ScenarioContext scenarioContext, String templateSetName, String orderBook, String userStr) throws Throwable;

    public void notReceiveMessage(ScenarioContext scenarioContext, String msgName, String userstr) throws Throwable;

    StepResult createFixMessage(String rawMsg) throws InvalidMessage;

	public FixStepResult sendMultiMessagesInTimeWindow(ScenarioContext scenarioContext,int messageCount, int timeWindow, String msgTemplate, String userstr) throws Throwable;
}
