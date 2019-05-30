package FixTcpAgent;

import fast.common.agents.AgentsManager;
import fast.common.agents.FixTcpClientAgent;
import fast.common.agents.FixTcpServerAgent;
import fast.common.context.FixStepResult;
import fast.common.context.ScenarioContext;

/**
 * Required settings: -DenvironmentName="sim_local" -DuserName="kt46743_sim_local"
 */
/*
public class Test {

    //@org.junit.Test TODO: we need to disable tests built into fast.common when we start dependent projects such sa fast.gma-root
    public void LocalSimulatorTest() throws Exception {
        ScenarioContext scenarioContext = new ScenarioContext(null);

        // Given Simulator start
        FixTcpServerAgent simulator = AgentsManager.getInstance().getOrCreateAgent("Simulator");

        FixTcpClientAgent client = AgentsManager.getInstance().getOrCreateAgent("Client");

        // When Client send "@clientRequestNew" RequestNew [OrdType=LIMIT|OrderQty=1000|Side=BUY|Symbol=$Symbol|Price=100]
        FixStepResult result = client.sendMessage(scenarioContext, "RequestNew", "OrdType=LIMIT|OrderQty=1000|Side=BUY|Symbol=$Symbol|Price=100");
        scenarioContext.saveLastStepResult(result, "@clientRequestNew");

        // Then Simulator receive "@serverRequestNew" RequestNew []
        result = simulator.receiveMessageAndSaveResult(scenarioContext, "RequestNew", "");
        scenarioContext.saveLastStepResult(result, "@serverRequestNew");

        // And it contains [Symbol=@clientRequestNew.Symbol|OrdType=@clientRequestNew.OrdType|Price=@clientRequestNew.Price|OrderQty=@clientRequestNew.OrderQty|Side=@clientRequestNew.Side]
        result.contains(scenarioContext, "Symbol=@clientRequestNew.Symbol|OrdType=@clientRequestNew.OrdType|Price=@clientRequestNew.Price|OrderQty=@clientRequestNew.OrderQty|Side=@clientRequestNew.Side");

        // When Simulator send "@serverConfirmNew" ConfirmNew [Symbol=@clientRequestNew.Symbol|OrdType=@clientRequestNew.OrdType|Price=@clientRequestNew.Price|OrderQty=@clientRequestNew.OrderQty|Side=@clientRequestNew.Side|37=%generateClOrdID()%|11=@serverRequestNew.11]
        result = simulator.sendMessage(scenarioContext, "ConfirmNew", "Symbol=@clientRequestNew.Symbol|OrdType=@clientRequestNew.OrdType|Price=@clientRequestNew.Price|OrderQty=@clientRequestNew.OrderQty|Side=@clientRequestNew.Side|37=%generateClOrdID()%|11=@serverRequestNew.11");
        scenarioContext.saveLastStepResult(result, "@serverConfirmNew");


        // Then Client receive "@clientConfirmNew" ConfirmNew [11=@clientRequestNew.11]
        result = client.receiveMessageAndSaveResult(scenarioContext, "ConfirmNew", "11=@clientRequestNew.11");
        scenarioContext.saveLastStepResult(result, "@clientConfirmNew");


        // And it contains [Symbol=@clientRequestNew.Symbol|OrdType=@clientRequestNew.OrdType|Price=@clientRequestNew.Price|OrderQty=@clientRequestNew.OrderQty|Side=@clientRequestNew.Side]
        result.contains(scenarioContext, "Symbol=@clientRequestNew.Symbol|OrdType=@clientRequestNew.OrdType|Price=@clientRequestNew.Price|OrderQty=@clientRequestNew.OrderQty|Side=@clientRequestNew.Side");


        // When Client send "@clientRequestCancel" RequestCancel [37=@clientConfirmNew.37|OrigClOrdID=@clientRequestNew.ClOrdID|Symbol=@clientRequestNew.Symbol|Side=@clientRequestNew.Side]
        result = client.sendMessage(scenarioContext, "RequestCancel", "37=@clientConfirmNew.37|OrigClOrdID=@clientRequestNew.ClOrdID|Symbol=@clientRequestNew.Symbol|Side=@clientRequestNew.Side");
        scenarioContext.saveLastStepResult(result, "@clientRequestCancel");


        // Then Simulator receive "@serverRequestCancel" RequestCancel [37=@serverConfirmNew.37]
        result = simulator.receiveMessageAndSaveResult(scenarioContext, "RequestCancel", "37=@serverConfirmNew.37");
        scenarioContext.saveLastStepResult(result, "@serverRequestCancel");

        // And it contains [Symbol=@clientRequestNew.Symbol|Side=@clientRequestNew.Side]
        result.contains(scenarioContext, "Symbol=@clientRequestNew.Symbol|Side=@clientRequestNew.Side");


        // When Simulator send "@serverConfirmCancel" ConfirmCancel [37=@serverConfirmNew.37|11=@serverRequestCancel.11|OrigClOrdID=@serverRequestCancel.OrigClOrdID|Symbol=@serverRequestCancel.Symbol]
        result = simulator.sendMessage(scenarioContext, "ConfirmCancel", "37=@serverConfirmNew.37|11=@serverRequestCancel.11|OrigClOrdID=@serverRequestCancel.OrigClOrdID|Symbol=@serverRequestCancel.Symbol");
        scenarioContext.saveLastStepResult(result, "@serverConfirmCancel");

        // Then Client receive "@clientConfirmCancel" ConfirmCancel [ClOrdID=@clientRequestCancel.ClOrdID]
        result = client.receiveMessageAndSaveResult(scenarioContext, "ConfirmCancel", "ClOrdID=@clientRequestCancel.ClOrdID");
        scenarioContext.saveLastStepResult(result, "@clientConfirmCancel");

        // And it contains [OrigClOrdID=@clientRequestNew.ClOrdID]
        result.contains(scenarioContext, "OrigClOrdID=@clientRequestNew.ClOrdID");
    }

}

*/
