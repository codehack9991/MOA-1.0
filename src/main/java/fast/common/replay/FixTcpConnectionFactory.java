package fast.common.replay;

import fast.common.logging.FastLogger;

import java.util.Map;

public class FixTcpConnectionFactory extends ConnectionFactory {
    private FastLogger _logger;

    public FixTcpConnectionFactory(ReplayManager replayManager, String name, Map params, String configFolder) {
        super(replayManager, name, params, configFolder);
        _logger = FastLogger.getLogger(String.format("%s:FixTcpConnectionFactory", _name));
    }

    @Override
    protected String getConnectionName(String connection, String action) {
        return connection;
    }

    @Override
    protected ReplayConnection createConnection(String connection, String action) throws Exception {
        return new FixTcpReplayConnection(_replayManager, _name, _params, _configFolder, connection);
    }
}
