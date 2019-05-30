package fast.common.replay;

import co.paralleluniverse.fibers.Suspendable;
import co.paralleluniverse.strands.concurrent.ReentrantLock;
import quickfix.*;
import quickfix.field.OrigSendingTime;
import quickfix.field.PossDupFlag;

import java.nio.file.Paths;
import java.util.*;


// TODO: rename - remove Replay word
// TODO: think if we can merge with FixTcpAgent
public class FixTcpReplayConnection extends ReplayConnection implements quickfix.Application {
    private SessionSettings _settings;
    private MemoryStoreFactory _storeFactory;
    private ScreenLogFactory _logFactory;
    private MessageFactory _messageFactory;
    private SocketInitiator _initiator;

    private String _iniConnectionName;
    private String _senderCompID;
    private String _targetCompID;
    private String _beginString;
    private String _socketConnectHost;
    private String _socketConnectPort;

    private enum Status {
        INITIALIZING, DISCONNECTED, LOGGED_IN, LOGGED_OUT
    }
    private Status _status;
    private SessionID _sessionId = null;

    public FixTcpReplayConnection(ReplayManager replayManager, String factoryName, Map params, String configFolder, String connection) throws Exception {
        super(replayManager, factoryName, params, configFolder, connection);
    }

    @Suspendable
    private void checkConnected() throws InterruptedException {
        int numTries = _numTries; // timeout
        for(int i = 0; i < numTries; i++) {
            if(_status == Status.LOGGED_IN)
                return;

            Timekeeping.sleep(1000);
        }

        String errorMsg = String.format("Failed to check connected. After waiting %ds still not logged in",  numTries);
        throw new RuntimeException(errorMsg); // TODO: use our exceptions
    }

    // QuickFix/J clears two tags which we should keep - workaround is to set them inside toApp()
    private String _possDupFlag = null; // 43
    private String _origSendingTime = null; // 122
    private ReentrantLock msgModifyLocker = new ReentrantLock();

    @Override
    @Suspendable
    public void send(quickfix.Message msg) throws Exception {
        checkConnected();

        msgModifyLocker.lock(); // so parallel threads will wait us - as we are using _possDupFlag & _origSendingTime in callback toApp() method
        try {
            _possDupFlag = null;
            _origSendingTime = null;
            if (msg.getHeader().isSetField(PossDupFlag.FIELD)) {
                _possDupFlag = msg.getHeader().getString(PossDupFlag.FIELD);
            }
            if (msg.getHeader().isSetField(OrigSendingTime.FIELD)) {
                _origSendingTime = msg.getHeader().getString(OrigSendingTime.FIELD);
            }

            quickfix.Session.sendToTarget(msg, _sessionId); // adds BeginString, SenderCompID and TargetCompID it also clears PossDupFlag & OrigSendingTime which we are restoring inside toApp();
        }
        finally {
            msgModifyLocker.unlock();
        }

        _logger.debug(String.format("Send message [%s]", msg.toString()));
    }

    @Override
    @Suspendable
    public void sendraw(String msg) throws Exception {
        //
    }

    @Override
    @Suspendable
    public void connect() throws Exception {
        _logger.info("Connecting");

        if(_status == Status.LOGGED_IN) {
            String errorMsg = String.format("TCP can't connect to connection '%s' because already connected", _connection);
            // logger.error(errorMsg);
            throw new Exception(errorMsg);
        }

        _status = Status.INITIALIZING;


        String ini_file = _params.get("ini_file").toString(); // relative to yml
        String full_filename = Paths.get(_configFolder).resolve(ini_file).toString();

        SessionSettings allSettings = new quickfix.SessionSettings(full_filename);
        Iterator<SessionID> iter = allSettings.sectionIterator();
        while(iter.hasNext()) {
            SessionID sessionID = iter.next();
            Properties defaultProperties = allSettings.getDefaultProperties();
            quickfix.Dictionary sessionProperties = allSettings.get(sessionID);
            _iniConnectionName = sessionProperties.getString("ConnectionName");

            if(_iniConnectionName.equals(_connection)) {
                _settings = new quickfix.SessionSettings();
                _settings.set(defaultProperties);
                _settings.set(sessionID, sessionProperties);

                _senderCompID = sessionProperties.getString("SenderCompID");
                _targetCompID = sessionProperties.getString("TargetCompID");
                _beginString = sessionProperties.getString("BeginString");
                _socketConnectHost = sessionProperties.getString("SocketConnectHost");
                _socketConnectPort = sessionProperties.getString("SocketConnectPort");

                fixDictionary = new DataDictionary(Paths.get(_configFolder).resolve("quickfix_spec/" +
                        _beginString.replace(".","") + ".xml").toString());

                break;
            }
        }
        if(_settings == null) {
            throw new Exception(String.format("Session settings not found for SenderCompID='%s' in '%s'", _connection, ini_file));
        }




        _storeFactory = new quickfix.MemoryStoreFactory();
        _logFactory = null;
        _messageFactory = new quickfix.DefaultMessageFactory();
        _initiator = new quickfix.SocketInitiator(this, _storeFactory, _settings, _logFactory, _messageFactory);


        _initiator.start();

        int numTries = 10;
        for(int i = 0; i < numTries; i++){
            if(_status != Status.LOGGED_IN) {
                Timekeeping.sleep(1000);
            }
            else {
                break;
            }
        }

        if(_status != Status.LOGGED_IN) {
            String errorMsg = String.format("TCP failed to connect. %s-(%s)->%s(%s:%s)",
                    _senderCompID, _beginString, _targetCompID, _socketConnectHost, _socketConnectPort);
            //logger.error(errorMsg);
            throw new Exception(errorMsg);
        }

        _logger.debug("Connected");
    }

    @Override
    public void disconnect() {
        if(_initiator != null) {
            _logger.debug("Disconnecting");
            _initiator.stop();
            _initiator = null;
            _status = Status.INITIALIZING;
            _logger.debug("Disconnected");
        }
    }

    @Override
    public void force_disconnect() {
        if(_initiator != null) {
            _logger.debug("Disconnecting");
            Iterator<SessionID> sessionIds = _initiator.getSessions().iterator();
            while (sessionIds.hasNext()) {
                SessionID sessionId = sessionIds.next();
                Session.lookupSession(sessionId).logout("user requested");
            }
            _initiator.stop(true);
            _initiator = null;
            _status = Status.INITIALIZING;
            _logger.debug("Disconnected");
        }
    }

    @Override
    public void checkDisconnect() throws InterruptedException {
        _logger.debug("Checking Disconnected");
        int numTries = _numTries; // timeout
        for(int i = 0; i < numTries; i++) {
            if(_status == Status.DISCONNECTED) {// TODO: understand on what event we should set this status - logout and/or some quickfix methods to check connection?
                _logger.debug("Checked Disconnected");
                return;
            }

            Timekeeping.sleep(1000);
        }

        String errorMsg = String.format("Failed to check disconnected. After waiting %ds connection was not disconnected",  numTries);
        throw new CheckDisconnectedFailed_ReplayException(errorMsg);
    }


    @Override
    public void onCreate(SessionID sessionId) {
        _sessionId = sessionId;
    }

    @Override
    public void onLogon(SessionID sessionId) {
        _logger.info(String.format("Logged on"));
        _status = Status.LOGGED_IN;
    }

    @Override
    public void onLogout(SessionID sessionId) {
        _logger.info(String.format("Logged out"));
        _status = Status.LOGGED_OUT;
    }

    @Override
    public void toAdmin(Message message, SessionID sessionId) {
        _logger.debug(String.format("Outgoing admin message [%s]", message.toString()));
    }

    @Override
    public void fromAdmin(Message message, SessionID sessionId) throws FieldNotFound, IncorrectDataFormat, IncorrectTagValue, RejectLogon {
        _logger.debug(String.format("Incoming admin message [%s]", message.toString()));
    }

    @Override
    public void toApp(Message message, SessionID sessionId) throws DoNotSend {
        // we are inside lock - we might need to restore tags 122 and 43
        if(_origSendingTime != null) {
            message.getHeader().setString(OrigSendingTime.FIELD, _origSendingTime);
        }
        if(_possDupFlag != null) {
            message.getHeader().setString(PossDupFlag.FIELD, _possDupFlag);
        }
    }

    @Override
    public void fromApp(Message message, SessionID sessionId) throws FieldNotFound, IncorrectDataFormat,
            IncorrectTagValue, UnsupportedMessageType {
        try {
            AddToReceivedMessages(message);
            _logger.info(String.format("Incoming message [%s]", message.toString()));
        } catch (Exception e) {
            _logger.error(String.format("Exception in fromApp(): %s", e.toString()));
        }
    }
}
