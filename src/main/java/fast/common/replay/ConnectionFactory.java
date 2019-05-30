package fast.common.replay;

import fast.common.logging.FastLogger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public abstract class ConnectionFactory {
    protected ReplayManager _replayManager;
    protected String _name;
    protected Map _params;
    protected String _configFolder;
    private FastLogger _logger;
    protected HashMap<String, ReplayConnection> _connections = new HashMap<>();


    public ConnectionFactory(ReplayManager replayManager, String name, Map params, String configFolder) {
        _replayManager = replayManager;
        _name = name;
        _logger = FastLogger.getLogger(String.format("%s:ConnectionFactory", _name));
        _params = params;
        _configFolder = configFolder;
    }

    public ArrayList<MessagesOutOfScope_ReplayException> cleanup() {
        ArrayList<MessagesOutOfScope_ReplayException> exceptions = new ArrayList<>();
        for(ReplayConnection objConn: _connections.values().stream().distinct().collect(Collectors.toSet())) { // we might have multiple kays mapped to the same connection because of EMS topics mapping
            MessagesOutOfScope_ReplayException exception = objConn.cleanup();
            if(exception != null) {
                exceptions.add(exception);
            }
        }
        _connections.clear();
        return exceptions.size() == 0 ? null : exceptions;
    }

    abstract protected String getConnectionName(String connection, String action) throws Exception;

    public void openAllConnections() throws Exception {
        for(ReplayConnection objConn: _connections.values().stream().distinct().collect(Collectors.toSet())) { // collection can contain duplicate values in case few keys are mapped to the same connection
            objConn.connect(); // if any connection fails then all com.citi.fast.replay fail and will not be started
        }
    }

    public ReplayConnection getOrCreateConnection(String connection, String action) throws Exception {
        ReplayConnection connObj = null;
        String connectionName = getConnectionName(connection, action) ;

        synchronized (_connections) {
            if(_connections.containsKey(connectionName)) {
                connObj = _connections.get(connectionName);
            } else {
                connObj = createConnection(connection, action); // instead of create can reuse connection for EMS because of mapping
                _connections.put(connectionName, connObj);
            }
        }

        return connObj;
    }

    public ReplayConnection getConnection(String connection, String action) throws Exception {
        ReplayConnection connObj = null;
        String connectionName = getConnectionName(connection, action) ;

        synchronized (_connections) {
            if(_connections.containsKey(connectionName)) {
                connObj = _connections.get(connectionName);
            } else {
                connObj = null;
            }
        }

        return connObj;
    }

    protected abstract ReplayConnection createConnection(String connection, String action) throws Exception;
}
