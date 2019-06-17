package fast.common.agents;

import java.util.Map;

import fast.common.core.Configurator;
import fast.common.fix.FixHelper;
import fast.common.logging.FastLogger;
import quickfix.ConfigError;
import quickfix.SessionSettings;
import quickfix.ThreadedSocketAcceptor;
/**
 * The {@code FixTcpServerAgent} class defines common methods to start agent and receive messages based on QuickFix
 * <p>This agent is server agent of {@link fast.common.agents.FixTcpAgent}  and the client agent is
 * {@link fast.common.agents.FixTcpClientAgent}
 * 
 */
public class FixTcpServerAgent extends FixTcpAgent {
    private FastLogger _logger;

    protected ThreadedSocketAcceptor _acceptor;
    private boolean isStarted = false;

    public FixTcpServerAgent(String name, Map agentParams, Configurator configurator) throws ConfigError, InterruptedException {
        super(name, agentParams, configurator);
        _logger = FastLogger.getLogger(String.format("%s:FixTcpServerAgent", _name));

       // start(); we should always call it explicitly
    }

    @Override
    public void close() throws Exception {
        if (_acceptor != null)
            _acceptor.stop(true);
        _acceptor = null;

        super.close(); // and clean buffers and reset counters

        _logger.debug("close()");
    }

    /**
     * Creates sessions to handle messages
     */
    @Override
    public void start() throws Exception {
        synchronized (this) {
            if (_acceptor != null) {
                this.close();
            }

            SessionSettings settings = new quickfix.SessionSettings();
            quickfix.Dictionary d = new quickfix.Dictionary("AllSettings", _agentParams);
            FixHelper.updateFilePathInDictionary(d, _config_folder); // updated file paths

            settings.set(_sessionID, d);
            quickfix.FileStoreFactory storeFactory = new quickfix.FileStoreFactory(settings);
            quickfix.LogFactory logFactory = new quickfix.FileLogFactory(settings);
            quickfix.DefaultMessageFactory messageFactory = new quickfix.DefaultMessageFactory();

            _acceptor = new quickfix.ThreadedSocketAcceptor(this, storeFactory, settings, logFactory, messageFactory);
            _acceptor.start();
        }
        isStarted = true;
    }


    public void waitForEstablishedIncomingConnectionIfNeeded() throws Exception {
        synchronized (this) {
            if (_lastTargetSeqNumAfterLogonLogout > _lastTargetSeqNumOnLogon) {
                _logger.info("Session was previously established, reusing it");
                return;
            }

            if (_acceptor == null) {
                this.start();
            }

            int numTries = 60;
            for (int i = 0; i < numTries; i++) {
                if (_lastTargetSeqNumAfterLogonLogout <= _lastTargetSeqNumOnLogon) {
                    _logger.info(String.format("Waiting for incoming message with SeqNum > %d. Last incoming SeqNum = %d.", _lastTargetSeqNumOnLogon, _lastTargetSeqNumAfterLogonLogout));
                    this.wait(3000);
                } else {
                    break;
                }
            }

            if (_lastTargetSeqNumAfterLogonLogout == 0) {
                String errorMsg = String.format("Did not receive incoming logon");
                throw new RuntimeException(errorMsg);
            } else if (_lastTargetSeqNumAfterLogonLogout <= _lastTargetSeqNumOnLogon) {
                String errorMsg = String.format("Did not receive incoming message with SeqNum > %d. Last incoming SeqNum = %d.", _lastTargetSeqNumOnLogon, _lastTargetSeqNumAfterLogonLogout);
                throw new RuntimeException(errorMsg);
            }

            _logger.info("Incoming Logon, sessions established");
        }
    }
    /**
	 * Checks whether the agent is started
	 */
	@Override
	public boolean isStarted() {
		return isStarted;
	}
}
