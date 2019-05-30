package fast.common.agents;

import fast.common.context.FixStepResult;
import fast.common.context.ScenarioContext;
import fast.common.core.Configurator;
import fast.common.fix.FixHelper;
import fast.common.logging.FastLogger;
import quickfix.*;
import quickfix.field.BeginString;
import quickfix.field.SenderCompID;
import quickfix.field.TargetCompID;

import java.nio.file.Paths;
import java.util.Iterator;
import java.util.Map;
/**
 * The {@code FixTcpClientAgent} class defines common methods to start agent and send messages based on QuickFix
 * <p>This agent is client agent of {@link fast.common.agents.FixTcpAgent}  and the server agent is
 * {@link fast.common.agents.FixTcpServerAgent}
 * <p>Details information for using an FixTcpClientAgent can see: 
 * <p><a href="https://cedt-confluence.nam.nsroot.net/confluence/display/167813001/Message+Agent+Config+Settings">Examples</a></p>
 * 
 * @author QA Framework Team
 * @since 1.5
 */
public class FixTcpClientAgent extends FixTcpAgent {
    private FastLogger _logger;
    private SocketInitiator _initiator;
    private boolean isStarted = false;
    
    public FixTcpClientAgent(String name, Map agentParams, Configurator configurator) throws ConfigError, InterruptedException {
        super(name, agentParams, configurator);
        _logger = FastLogger.getLogger(String.format("%s:FixTcpClientAgent", _name));

       // start(); no need for this - we call it from _sendMessage
    }

	@Override
	public void close() throws Exception {
		if (_initiator != null)
			_initiator.stop();
		_initiator = null;
		super.close();
		isStarted = false;
	}
	/**
	 * Creates session to logon
	 */
    @Override
    public void start() throws Exception {
        if(_initiator != null) {
            return;
        }

        SessionSettings settings = new quickfix.SessionSettings();
        quickfix.Dictionary d = new quickfix.Dictionary("AllSettings", _agentParams);
        FixHelper.updateFilePathInDictionary(d, _config_folder); // updated file paths

        settings.set(_sessionID, d);
        quickfix.FileStoreFactory storeFactory = new quickfix.FileStoreFactory(settings);
        quickfix.LogFactory logFactory = new quickfix.FileLogFactory(settings);
        quickfix.DefaultMessageFactory messageFactory = new quickfix.DefaultMessageFactory();

        _initiator = new quickfix.SocketInitiator(this, storeFactory, settings, logFactory, messageFactory);
        // _status = Status.INITIALIZING;
        _initiator.start();


        int numTries = 10;
        for (int i = 0; i < numTries; i++) {
            if (!_initiator.isLoggedOn()) {
                _logger.debug(String.format("Waiting for logon %d", i));
                Thread.sleep(1000);
            } else {
                break;
            }
        }

        if (!_initiator.isLoggedOn()) {
            String errorMsg = "connect failure";
            throw new RuntimeException(errorMsg);
        }
        
        isStarted = true;
    }

    /**
     * Sends a message
     */
    @Override
    protected void _sendMessage(quickfix.Message msg) throws Exception {
        // if we are disconnected then connect first
        if(_initiator == null) { // if need we start connection
            this.start();
        }
        else if (!_initiator.isLoggedOn()) { // if we were logged out then we reconnect
            this.close();
            this.start();
        }

        super._sendMessage(msg);
    }

	@Override
	public boolean isStarted() {
		return isStarted;
	}

	@Override
	public FixStepResult sendMultiMessagesInTimeWindow(ScenarioContext scenarioContext,int messageCount, int timeWindow, String msgTemplate, String userstr) throws Throwable {
		if (_initiator == null) { // if need we start connection
			this.start();
		} else if (!_initiator.isLoggedOn()) { // if we were logged out then we
												// reconnect
			this.close();
			this.start();
		}
		return super.sendMultiMessagesInTimeWindow(scenarioContext, messageCount,
				timeWindow, msgTemplate, userstr);
	}
}


