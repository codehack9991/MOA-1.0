package fast.common.agents;

import fast.common.logging.FastLogger;
import fast.common.core.Configurator;
import fast.common.context.FixStepResult;
import org.apache.commons.lang3.NotImplementedException;
import fast.common.replay.ReplayManager;

import java.util.*;


public class ReplayAgent extends Agent {
    private FastLogger _logger;

    public ReplayAgent(String name, Map agentParams, Configurator configurator) throws Exception {
        super(name, agentParams, configurator);
        _logger = FastLogger.getLogger(String.format("%s:ReplayAgent", _name));
    }

    public void notReceiveMessage(String msgName, String userstr) throws Exception {
        throw new NotImplementedException("notReceiveMessage() not implemented");
    }

    public void runScenarioFromDatabase(String scenarioName) throws Exception {
        _logger.debug(String.format("Run scenario='%s'", scenarioName));
        ReplayManager replayManager = new ReplayManager(_agentParams,_config_folder,null, null);
        replayManager.runOneScenario(scenarioName);
    }

    public void runScenariosFromDatabaseByQuery(String sqlQuery) throws Exception {
        _logger.debug(String.format("Run scenarios by query='%s'", sqlQuery));
        ReplayManager replayManager = new ReplayManager(_agentParams,_config_folder,null, sqlQuery);
        replayManager.runAll();
    }

    @Override
    public void close() throws Exception {

    }
}



