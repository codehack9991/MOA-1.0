package fast.common.agents;


import fast.common.logging.FastLogger;
import fast.common.core.Configurator;

import java.util.HashMap;

public class AgentsManager implements AutoCloseable {
    private HashMap<String, Agent> _agents = new HashMap<>();
    private static FastLogger logger = FastLogger.getLogger("AgentsManager");

    private static AgentsManager _instance = null;
    public static AgentsManager getInstance() {
        synchronized (AgentsManager.class) {
            if(_instance == null) {
                _instance = new AgentsManager();
            }
            return _instance;
        }
    }

    private AgentsManager(){

    }

    public static <T extends Agent> T getAgent(String agentName) throws Exception {
    	return getInstance().getOrCreateAgent(agentName);
    }
    
    public <T extends Agent> T getOrCreateAgent(String agentName) throws Exception {
        Agent agent = _getOrCreateAgent(agentName);
        return (T)agent;
    }

    private Agent _getOrCreateAgent(String agentName) throws Exception  {
        Agent agent = null;
        synchronized (_agents) {
            if (_agents.containsKey(agentName)) {
                agent = _agents.get(agentName);
            } else {
                agent = Configurator.getInstance().createAgent(agentName);
                _agents.put(agentName, agent);
            }
        }
        return agent;
    }

    @Override
    public void close() throws Exception {
        synchronized (_agents) {
            for (Agent agent : _agents.values()) {
                agent.close();
            }
            _agents.clear();
        }
    }

    public void flushBuffersToLog() {
        try {
            boolean isSingleThread = Configurator.IsSingleThread();
            synchronized (_agents) {
                for (Agent agent : _agents.values()) {
                    agent.flushBuffersToLog(isSingleThread);
                }
            }
        }catch (Exception e) {
            logger.error(String.format("flushBuffersToLog failed: %s", e.toString()));
            throw e;
        }
    }
}
