package fast.common.replay;

import co.paralleluniverse.fibers.SuspendExecution;
import fast.common.logging.FastLogger;
import co.paralleluniverse.fibers.Suspendable;
import co.paralleluniverse.strands.concurrent.ReentrantLock;
import quickfix.*;

import javax.jms.JMSException;
import java.util.*;


class LockedBufferOfReceivedMessages {
    ReentrantLock locker = new ReentrantLock();
    ArrayList<quickfix.Message> buffer = new ArrayList<>();
}

public abstract class ReplayConnection {
    FastLogger _logger;  // we use this logger for incoming messages, for outgoing commands we use logger provided from caller - ScenarioContext or Scenario or Step
    String _factoryName;
    Map _params;
    String _configFolder;
    String _connection;
    ReplayManager _replayManager;
    DataDictionary fixDictionary = null;

    LockedBufferOfReceivedMessages _receivedMessages = new LockedBufferOfReceivedMessages(); // all messages received by connection. DONE: clear by timeout and put all cleated messages into unknown messages list for report
    protected int _numTries = 10;

    public ReplayConnection(ReplayManager replayManager, String factoryName, Map params, String configFolder, String connection) {
        _replayManager = replayManager; // used to check receive only
        _factoryName = factoryName;
        _params = params;
        _configFolder = configFolder;
        _connection = connection;
        _logger = FastLogger.getLogger(String.format("Connection[%s:%s]", _factoryName, _connection));

    }

    @Suspendable
    public MessagesOutOfScope_ReplayException cleanup() {
        _logger.debug("Cleaning up");
        ArrayList<quickfix.Message> outOfScopeMessages = null;

        _receivedMessages.locker.lock();
        try {
            if(_receivedMessages.buffer.size()>0){
                outOfScopeMessages = new ArrayList<quickfix.Message>();
                for(quickfix.Message actualMessage : _receivedMessages.buffer) {
                    outOfScopeMessages.add(actualMessage);
                }

                _receivedMessages.buffer.clear();
            }
        }
        finally {
            _receivedMessages.locker.unlock();
        }

        try {
            disconnect();
        }
        catch (Exception e){
            _logger.error(e.toString());
        }
        return outOfScopeMessages == null ? null : new MessagesOutOfScope_ReplayException(_connection, _factoryName, outOfScopeMessages);
    }

    @Suspendable
    public void AddToReceivedMessages(quickfix.Message msg) {
        _receivedMessages.locker.lock();
        try {
            _receivedMessages.buffer.add(msg);
        }
        finally {
            _receivedMessages.locker.unlock();
        }
    }

    @Suspendable
    public ArrayList<quickfix.Message> cleanUpScenarioByFindingItsMessages(quickfix.Message msg, StructuredTagList msgTagsToReadAndStore) throws FieldNotFound {
        ArrayList<quickfix.Message> actualReceivedMessage = _replayManager.miniFixHelper.cleanUpScenarioByFindingItsMessages(msg, _receivedMessages, msgTagsToReadAndStore);
        if(actualReceivedMessage!= null) {
            _logger.debug(String.format("Cleanup Scenario by finding it's unexpected messages. Found %d unexpected messages.", actualReceivedMessage.size()));
        }
        else {
            _logger.debug(String.format("Cleanup Scenario by finding it's unexpected messages. No unexpected messages found."));
        }
        return actualReceivedMessage;

    }

    @Suspendable
    public quickfix.Message checkReceive(ReplayScenario replayScenario, quickfix.Message msg, StructuredTagList msgTagsToReadAndStore) throws Exception {
        quickfix.Message actualReceivedMessage = _replayManager.miniFixHelper.checkReceive(replayScenario, msg, _receivedMessages, msgTagsToReadAndStore, _replayManager._numTries, fixDictionary);
        _logger.debug(String.format("Checked received message [%s]", actualReceivedMessage.toString()));
        return actualReceivedMessage;
    }


    abstract public void send(quickfix.Message msg) throws Exception, SuspendExecution;
    abstract public void sendraw(String msg) throws Exception, SuspendExecution;
    abstract public void connect() throws Exception;
    abstract public void disconnect() throws JMSException;
    abstract public void checkDisconnect() throws Exception;
    abstract public void force_disconnect() throws JMSException;

}




