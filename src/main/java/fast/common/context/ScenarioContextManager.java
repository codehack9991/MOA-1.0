package fast.common.context;

import fast.common.logging.FastLogger;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Properties;

import cucumber.api.Scenario;

public class ScenarioContextManager implements AutoCloseable {
    private HashMap<Object, ScenarioContext> _scenarioContexts = new HashMap<Object, ScenarioContext>();
    private static FastLogger logger = FastLogger.getLogger("ScenarioContextManager");

    public ScenarioContextManager() {

    }

    private static ScenarioContextManager _instance = null;
    public static ScenarioContextManager getInstance() {
        synchronized (ScenarioContextManager.class) {
            if(_instance == null) {
                _instance = new ScenarioContextManager();
            }
            return _instance;
        }
    }

    public ScenarioContext getOrCreateScenarioContext(Object scenario) throws Exception {
        ScenarioContext scenarioContext = null;
        synchronized (_scenarioContexts) {
            if (_scenarioContexts.containsKey(scenario)) {
                scenarioContext = _scenarioContexts.get(scenario);
            } else {
                scenarioContext = new ScenarioContext(scenario);
                _scenarioContexts.put(scenario, scenarioContext);
            }
        }
        return scenarioContext;
    }
    
    public Object[] getAllScenarios(){
    	return _scenarioContexts.keySet().toArray();
    }

    @Override
    public void close() throws Exception {
        synchronized (_scenarioContexts) {
            for (ScenarioContext scenarioContext : _scenarioContexts.values()) {
                scenarioContext.close();
            }
            _scenarioContexts.clear();
        }
    }
}
