package fast.common.replay;

import fast.common.logging.FastLogger;

import java.util.Map;

public class FixEmsConnectionFactory extends ConnectionFactory {
    private FastLogger _logger;

    private static String default_inbound = "<default_receive>";
    private static String default_outbound = "<default_send>";

    public FixEmsConnectionFactory(ReplayManager replayManager, String name, Map params, String configFolder) {
        super(replayManager, name, params, configFolder);
        _logger = FastLogger.getLogger(String.format("%s:FixEmsConnectionFactory", _name));
    }

    @Override
    protected String getConnectionName(String connection, String action) throws Exception {
        if(action.equals(Action.SEND.name()) || action.equals(Action.CHECK_RECEIVE.name())) {
            return String.format("%s->%s", connection, action);
        }

        throw new Exception(String.format("FixEmsConnectionFactory.getConnectionName() received incorrect action:'%s'", action));
    }

    @Override
    protected ReplayConnection createConnection(String connection, String action) throws Exception {
        Boolean send;
        if(action.equals(Action.SEND.name())) {
            send = true;
        }
        else if (action.equals(Action.CHECK_RECEIVE.name())) {
            send = false;
        }
        else {
            throw new Exception(String.format("FixEmsConnectionFactory.createConnection() received incorrect action:'%s'", action));
        }

        String topicNameProd = "null";
        if(connection != null)
            topicNameProd = connection;

        Map brokersMap = (Map)_params.get("Brokers");
        Map topicsMapping = (Map)_params.get("TopicsMapping");
        String brokerAntTopicStr;
        if(!topicsMapping.containsKey(topicNameProd)) {
            if(topicsMapping.containsKey(send ? default_outbound : default_inbound)){
                brokerAntTopicStr = topicsMapping.get(send ? default_outbound : default_inbound).toString();
            }
            else {
                throw new Exception(String.format("Can't find mapping for EMS Topic '%s'", topicNameProd));
            }
        }
        else {
            brokerAntTopicStr = topicsMapping.get(topicNameProd).toString();
        }
        String[] brokerAndTopicArr = brokerAntTopicStr.split(":");
        if(brokerAndTopicArr.length != 2) {
            throw new Exception(String.format("Mapping for EMS Topic '%s' is wrong: '%s'", topicNameProd, brokerAntTopicStr));
        }
        String brokerName = brokerAndTopicArr[0];
        String topicName = brokerAndTopicArr[1];
        Map brokerParams = (Map)brokersMap.get(brokerName);

        for(ReplayConnection objConn: _connections.values()) {
            if(objConn._connection.equals(topicName) && ((FixEmsReplayConnection)objConn)._send == send) {
                return objConn;
            }
        }
        return new FixEmsReplayConnection(_replayManager, _name, brokerParams, _configFolder, topicName, send);
    }
}
