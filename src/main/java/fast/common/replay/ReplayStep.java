package fast.common.replay;

import fast.common.logging.FastLogger;
import co.paralleluniverse.fibers.Suspendable;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

public class ReplayStep {
    ReplayScenario replayScenario;
    private FastLogger _logger;

    public java.util.Date ts;
    public String side;
    public String connection;
    public String action;
    public String data;

    public ReplayStep(ReplayScenario replayScenario, java.util.Date ts, String side, String connection, String action, String data) throws Exception {
        this.replayScenario = replayScenario;
        this.ts = ts;
        this.side = side;
        this.connection = connection;
        this.action = action;
        this.data = data;

        ConnectionFactory connFactory = replayScenario.replayManager.getOrCreateConnectionFactory(side);
        ReplayConnection connObj = connFactory.getOrCreateConnection(connection, action);
        String connectionNameWithMapping = connection;
        if(connectionNameWithMapping == null) {
            connectionNameWithMapping = "null";
        }
        if(!connectionNameWithMapping.equals(connObj._connection)) {
            connectionNameWithMapping = String.format("%s(original:%s)", connObj._connection, connectionNameWithMapping);
        }

        _logger = FastLogger.getLogger(String.format("Scenario[%s] - Connection[%s:%s] - %s(%s)",
                this.replayScenario.name,
                this.side,
                connectionNameWithMapping,
                this.action,
                LocalDateTime.ofInstant(ts.toInstant(), ZoneId.systemDefault()).format(
                        replayScenario.replayManager.miniFixHelper.millisecondsTimeFormatter)));
    }

    public String toString() {
        String tsStr = LocalDateTime.ofInstant(ts.toInstant(), ZoneId.systemDefault()).format(replayScenario.replayManager.miniFixHelper.millisecondsTimeFormatter);
        return String.format("ts:%s, side:%s, connection:%s, action:%s, data:[%s]", tsStr, side, connection, action, data);
    }

    public ArrayList<quickfix.Message> cleanUpScenarioByFindingItsMessages() throws Exception {
        ConnectionFactory connFactory = replayScenario.replayManager.getOrCreateConnectionFactory(side);
        ReplayConnection connObj = connFactory.getOrCreateConnection(connection, action);

        ArrayList<quickfix.Message> result = null;
        try{
            if (action.equals(Action.CHECK_RECEIVE.name())) {
                //logger.info(String.format("Parsing data for check receive [%s]", data));
                quickfix.Message msg = new quickfix.Message(data, false);
                StructuredTagList msgTagsToReadAndStore = replayScenario.replayManager.miniFixHelper.processStepData(replayScenario, msg, false); // only 11 and 37 are interesting for us here
                result = connObj.cleanUpScenarioByFindingItsMessages(msg, msgTagsToReadAndStore); // can return null
                msgTagsToReadAndStore.clear();
            }
            else {
                // nothing to clean up
            }
        }
        catch (Exception e){
              _logger.error(e.toString()); //  to find failed scenario and step
              throw e;
        }

        return result;
    }

    @Suspendable
    public void run() throws Exception {
        // DONE: wait until right time - it is done at scenario level
        Exception exception = null;

        try {
            ConnectionFactory connFactory = replayScenario.replayManager.getOrCreateConnectionFactory(side);
            ReplayConnection connObj = connFactory.getOrCreateConnection(connection, action);

            if(action.equals(Action.SEND.name())) {
                quickfix.Message msg = new quickfix.Message(data, connObj.fixDictionary, false);
                StructuredTagList msgTagsToReadAndStore = replayScenario.replayManager.miniFixHelper.processStepData(replayScenario, msg, true);
                if(msgTagsToReadAndStore != null) {
                    msgTagsToReadAndStore.clear();
                    throw new Exception("msgTagsToReadAndStore should be null for send action");
                }

                if(!replayScenario.replayManager.getSendPermission()) {
                    //Stack for more that XX minutes waiting for permission to send the message. Abort scenario execution
                    throw new SendWaitTimeout_ReplayException("Aborted. Waited for more than prescribed time interval to send message");
                }
                connObj.send(msg);
                _logger.info(String.format("success [%s]", msg.toString()));
            }
            else if (action.equals(Action.CHECK_RECEIVE.name())) {
                //logger.info(String.format("Parsing data for check receive [%s]", data));
                quickfix.Message msg = new quickfix.Message(data, connObj.fixDictionary, false);
                StructuredTagList msgTagsToReadAndStore = replayScenario.replayManager.miniFixHelper.processStepData(replayScenario, msg, false);

                quickfix.Message actualReceivedMessage = connObj.checkReceive(replayScenario, msg, msgTagsToReadAndStore);
                msgTagsToReadAndStore.clear();
                replayScenario.replayManager.releaseSendPermisson();
                _logger.info(String.format("success [%s]", actualReceivedMessage.toString()));
            }
            else if (action.equals(Action.CONNECT.name())) {
                connObj.connect();
                _logger.info(String.format("success"));
            }
            else if (action.equals(Action.DISCONNECT.name())) {
                connObj.disconnect();
                _logger.info(String.format("success"));
            }
            else if (action.equals(Action.CHECK_DISCONNECT.name())) {
                connObj.checkDisconnect();
                _logger.info(String.format("success"));
            }
            else {
                throw new Exception(String.format("Unexpected action:'%s'", action));
            }

        //} catch (ReplayException e) {
        //    exception = e;
         //   _logger.error(e.getMessage());
        } catch (Exception e) {
            exception = e;
            _logger.error(e.getMessage());
        }

        if(exception != null) {
            // DEBUG LOG MAP OF GENERATED/RECEIVED TAG VALUES
            boolean needToDebugScenarioMap = false;
            if(MessageIncorrect_ReplayException.class.isInstance(exception)) {
                MessageIncorrect_ReplayException incorrectMessage = (MessageIncorrect_ReplayException)exception;
                //for(MessageIncorrect_ReplayException incorrectMessage: incorrectMessages.actualMessagesErrors) {
                for(TagError_ReplayException tagError: incorrectMessage.tagErrors) {
                    if(replayScenario.replayManager.miniFixHelper._tagsWithClOrdIDCheck.contains(tagError.tag) ||
                            replayScenario.replayManager.miniFixHelper._tagsWithOrderIDCheck.contains(tagError.tag) ||
                            replayScenario.replayManager.miniFixHelper._tagsWithDateTimeCheck.contains(tagError.tag)
                            ) {
                        needToDebugScenarioMap = true;
                        break;
                    }
                }
                //if(needToDebugScenarioMap)
                //    break;
                //}
            }
            if(needToDebugScenarioMap) {
                StringBuilder sb = new StringBuilder();
                sb.append("CACHED MAP:\r\n");
                Iterator iter = replayScenario.tagValuesMap.entrySet().iterator();
                while(iter.hasNext()) {
                    Map.Entry entry = (Map.Entry) iter.next();
                    String key = entry.getKey().toString();
                    String value = entry.getValue().toString();
                    sb.append(String.format("'%s': '%s'\r\n", key, value));
                }
                _logger.debug(sb.toString());
            }

            throw exception;
        }

    }

    public String getFullConnectionName() {
        try {
            ConnectionFactory connFactory = replayScenario.replayManager.getOrCreateConnectionFactory(side);
            ReplayConnection connObj = connFactory.getOrCreateConnection(connection, action);
            if (connObj._connection.equals(connection)) {
                return connection;
            } else {
                return (connection + "->\n" + connObj._connection);
            }
        } catch (Exception e) {
            _logger.error(e.getMessage());
            return null;
        }
    }
}
