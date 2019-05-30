package fast.common.agents;

import fast.common.context.ScenarioContext;
import fast.common.context.StepResult;
import fast.common.fix.MessageIncorrect;
import fast.common.logging.FastLogger;
import fast.common.core.Configurator;
import fast.common.fix.FixHelper;
import fast.common.context.FixStepResult;
import fast.common.context.MsgHook;
import fast.common.replay.MessageMissing_ReplayException;
import quickfix.*;
import quickfix.field.BeginString;
import quickfix.field.SenderCompID;
import quickfix.field.TargetCompID;

import javax.xml.xpath.XPathExpressionException;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;
/**
 * The {@code FixTcpAgent} class defines common methods to send and receive/verify messages based on QuickFix
 * <p>Fix tcp agent includes client and server agents, they are: {@link fast.common.agents.FixTcpClientAgent} and 
 * {@link fast.common.agents.FixTcpServerAgent}
 * <p>Details information for using an FixTcpAgent can see: 
 * <p><a href="https://cedt-confluence.nam.nsroot.net/confluence/display/167813001/Message+Agent+Config+Settings">Examples</a></p>
 * 
 * @author QA Framework Team
 * @since 1.5
 */
public abstract class FixTcpAgent extends Agent implements quickfix.Application, IFixMessagingAgent {
	private FastLogger _logger;
	private FixHelper _fixHelper;
	private ArrayList<quickfix.Message> _receivedMessages = new ArrayList<>();
	String _sendExtraTags;
	String _receiveExtraTags;

	private ReentrantLock _origMsglock = new ReentrantLock();
	Message _originalMessage = null; // Quickfix v1.6.4 library overrides tag 52
										// and removes required pricision (e.g.
										// microseconds, nanoseconds). So we
										// need to update message in toApp(). We
										// store all tags 52 here
	// TODO: other tags are also updated by Quickfix - decide maybe we need also
	// overwrite them?
	// 8, 9, 49, 50, 142, 56, 57, 143, 34, 43, 122
	static int[] tagsToKeep = { 52 };

	SessionID _sessionID;

	// int _nextSenderSeqNum; // set during logout to correct next sender
	// sequence number
	int _lastTargetSeqNumAfterLogonLogout = 0; // last client sequence number
												// after Logon
	int _lastTargetSeqNumOnLogon = 0; // we will ignore everything below this
	
	// hook to change message data just before sending use for negative tests
	private ThreadLocal<MsgHook> hookMsgBeforeSending = new ThreadLocal<>(); 

	// int _nextSeqNum = 0;
	// boolean _heartbeatsStarted = false;
	// int _msgSeqNumClientLogon = 0; // seq num from client during Logon
	// int _msgSeqNumClientLast = 0; // last client sequence number TODO: we
	// migh thave many clients - need to manage it

	public FixTcpAgent(String name, Map agentParams, Configurator configurator)
			throws ConfigError, InterruptedException {
		this(name, agentParams, configurator, new FixHelper(agentParams, configurator));
	}
	/**
     * Constructs a new <tt>FixTcpAgent</tt> with
     * default configuration file (config.yml) and custom configuration files to
     * fetch required parameters.
	 * 
	 * @param   name a string for naming the creating FixTcpAgent 
     * @param   agentParams a map to get the required parameters for creating a FixTcpAgent 
     * @param   configurator a Configurator instance to provide configuration info for the actions of the FixTcpAgent
	 * @throws  ConfigError
	 * @throws  InterruptedException
	 */
	public FixTcpAgent(String name, Map agentParams, Configurator configurator, FixHelper helper)
			throws ConfigError, InterruptedException {
		super(name, agentParams, configurator);
		_logger = FastLogger.getLogger(String.format("%s:FixTcpAgent", _name));

		_fixHelper = helper;

		String beginString = Configurator.getString(_agentParams, "BeginString");
		String senderCompID = Configurator.getString(_agentParams, "SenderCompID");
		String targetCompID = Configurator.getString(_agentParams, "TargetCompID");
		_sessionID = new SessionID(new BeginString(beginString), new SenderCompID(senderCompID),
				new TargetCompID(targetCompID));

		_sendExtraTags = Configurator.getString(_agentParams, "send_extra_tags");
		_receiveExtraTags = Configurator.getStringOr(_agentParams, "receive_extra_tags", null);
	}
	
	@Override
	public void close() throws Exception {
		synchronized (_receivedMessages) {
			_logger.error(String.format("Number of messages left: %d", _receivedMessages.size()));
			_receivedMessages.clear();
		}
		_lastTargetSeqNumAfterLogonLogout = 0;
		_lastTargetSeqNumOnLogon = 0;
	}
	/**
	 * @see fast.common.agents.FixTcpClientAgent#start()
	 * @see fast.common.agents.FixTcpServerAgent#start()
	 * @throws Exception
	 */
	@Override
	public void start() throws Exception {

	}

	protected void _sendMessage(quickfix.Message msg) throws Exception {
		synchronized (this) { // to avoid send and wait connect/connect at the
								// same time
			String rawstr = msg.toString();
			_logger.info(String.format("SENDING userstr=[%s],%nrawstr=[%s]%n",
					_fixHelper.convertRawstrToUserstr(rawstr), rawstr));

			_origMsglock.lock();
			try {
				_originalMessage = (Message) msg.clone();
				quickfix.Session.sendToTarget(msg, _sessionID);
			} finally {
				_origMsglock.unlock();
			}
		}
	}
	 /**
	 * @see fast.common.glue.CommonStepDefs#sendFixMessage(String, String, String, String)
	 * @see fast.common.agents.FixAgent#sendMessage(ScenarioContext, String, String, String)
	 */
	@Override
	public FixStepResult sendMessage(ScenarioContext scenarioContext, String templateName, String varName,
			String userstr) throws Exception {
		Message msg = _fixHelper.convertUserstrToMessage(scenarioContext, templateName, userstr, _sendExtraTags);
		_sendMessage(msg);
		FixStepResult result = new FixStepResult(msg, _fixHelper);
		_fixHelper.saveResult(result, scenarioContext, templateName, varName);
		return result;
	}
	
	@Override
	public FixStepResult sendMultiMessagesInTimeWindow(ScenarioContext scenarioContext,int messageCount, int timeWindow, String msgTemplate, String userstr) throws Throwable {
		ArrayList<quickfix.Message> multipleMsg = new ArrayList<>();
		for (int i = 0; i < messageCount; i++) {
			quickfix.Message msg = _fixHelper.convertUserstrToMessage(
					scenarioContext, msgTemplate, userstr, _sendExtraTags);
			multipleMsg.add(msg);
		}
		LocalDateTime startTime = LocalDateTime.now();
		long timeWait = (timeWindow*1000-2*messageCount)/messageCount;
		for (quickfix.Message s : multipleMsg) {
			_sendMessage(s);
			if(timeWait>0)Thread.sleep(timeWait);
		}
		LocalDateTime endTime = LocalDateTime.now();
		Duration duration = Duration.between(startTime, endTime);
		_logger.debug("Totally takes " + duration.toMillis() +" milliseconds !!!");
		return new FixStepResult(multipleMsg, _fixHelper);
	}
	 /**
	 * @see fast.common.glue.CommonStepDefs#receiveFixMessage(String, String, String, String)
	 * @see fast.common.agents.FixAgent#receiveMessage(ScenarioContext, String, String, String)
	 */
	@Override
	public FixStepResult receiveMessage(ScenarioContext scenarioContext, String templateName, String varName,
			String userstr) throws InterruptedException, XPathExpressionException, InvalidMessage, FieldNotFound {
		quickfix.Message msg = _fixHelper.convertUserstrToMessage(scenarioContext, templateName, userstr,
				_receiveExtraTags);
		String rawstr = msg.toString();
		_logger.debug(String.format("CHECK RECEIVED userstr=[%s],%nrawstr=[%s]%n",
				_fixHelper.convertRawstrToUserstr(rawstr), rawstr));

		return _fixHelper.receiveMessageAndSaveResult(_receivedMessages, scenarioContext, templateName, varName, msg);
	}

	@Override
	public FixStepResult receiveAndVerifyMessage(ScenarioContext scenarioContext, String templateName, String varName,
			String userStr) throws Throwable {
        return _fixHelper.receiveAndVerifyMessage(_receivedMessages, scenarioContext, templateName, varName, userStr,_receiveExtraTags);

	}
	/**
	 * @see fast.common.glue.CommonStepDefs#notReceiveFixMessage(String, String, String, String)
	 * @see fast.common.agents.FixAgent#notReceiveMessage(ScenarioContext, String, String)
	 */
	public void notReceiveMessage(ScenarioContext scenarioContext, String msgName, String userstr) throws Exception {
		//we shall not use extra tags here - in NOT RECEIVE command
		quickfix.Message msg = _fixHelper.convertUserstrToMessage(scenarioContext, msgName, userstr, null);
		String rawstr = msg.toString();
		_logger.debug(String.format("CHECK NOT RECEIVED userstr=[%s],%nrawstr=[%s]%n",
				_fixHelper.convertRawstrToUserstr(rawstr), rawstr));

		try {
			_logger.warn(String.format("%s: %d", this.getClass().getName(), _receivedMessages.size()));
			_fixHelper.checkReceived(_receivedMessages, msg);
		} catch (MessageIncorrect e) {
			// as we expected
			return;
		} catch (MessageMissing_ReplayException e) {
			// as we expected
			return;
		}

		throw new RuntimeException("We received message when we didn't expect it");
	}

	/*
	 * TODO: add back using new methods public FixStepResult
	 * findMessages(ScenarioContext scenarioContext, String msgName, String
	 * userstr) throws XPathExpressionException { String fullUserstr =
	 * _fixHelper.convertUserstrToFulluserstr(scenarioContext, msgName, userstr,
	 * _receiveExtraTags); ArrayList<StringField> searchFields =
	 * _fixHelper.convertFulluserstrToMessage(fullUserstr); String rawstr =
	 * _fixHelper.convertFieldsToRawstr(searchFields);
	 * _logger.debug(String.format("finding userstr=[%s],%nrawstr=[%s]%n",
	 * fullUserstr, rawstr));
	 * 
	 * ArrayList<String> found_rawstrs =
	 * _fixHelper.findAllReceivedRawstrByFields(_receivedMessages,
	 * searchFields); _logger.debug(String.format("found %s messages",
	 * found_rawstrs.size())); FixStepResult result = new
	 * FixStepResult(found_rawstrs, _fixHelper); return result; }
	 */

	public void setOrderBook(ScenarioContext scenarioContext, String templateSetName, String orderBook, String userStr)
			throws Throwable {
		_fixHelper.setOrderBook(scenarioContext, this, templateSetName, orderBook, userStr);
	}

	@Override
	public void onCreate(SessionID sessionId) {
		_logger.info(String.format("onCreate(sessionId=%s)", sessionId.toString()));
	}

	@Override
	public void onLogon(SessionID sessionId) {
		_logger.info(String.format("Session[%s] logged on", sessionId.toString()));
	}

	@Override
	public void onLogout(SessionID sessionId) {
		_logger.info(String.format("Session[%s] logged out", sessionId.toString()));
	}
	/**
	 * Sends administrative messages
	 */
	@Override
	public void toAdmin(Message message, SessionID sessionId) {
		try {

			if (message.getHeader().isSetField(35)) {
				// we don't send ResendRequest
				if (message.getHeader().getString(35).equals("2")) { // ResendRequest
					_logger.warn("Cancelled Resend Request");
					throw new DoNotSend(); // not send resend requests
				}

				/*
				 * if (message.getHeader().getString(35).equals("A")) { // logon
				 * if(_nextSeqNum > 0) { // this is second try and at this time
				 * we know corrected SeqNum message.getHeader().setInt(34,
				 * _nextSeqNum);
				 * Session.lookupSession(sessionId).setNextSenderMsgSeqNum(
				 * _nextSeqNum); _logger.warn(String.
				 * format("Second Logon: Reset Sequence Number to %d",
				 * _nextSeqNum)); } }
				 */
			}

			String rawstr = message.toString();
			_logger.info(String.format("toAdmin(sessionId=%s): userstr:[%s], rawstr:[%s]", sessionId.toString(),
					_fixHelper.convertRawstrToUserstr(rawstr), rawstr));
		} catch (Exception ex) {
			_logger.error(String.format("toAdmin() raised exception: %s", ex.toString()));
		}
	}
	/**
	 * Receives administrative messages
	 */
	@Override
	public void fromAdmin(Message message, SessionID sessionId)
			throws FieldNotFound, IncorrectDataFormat, IncorrectTagValue, RejectLogon {
		try { // TODO: KT: investigate why I can't now connect from MiniFix . I
				// am receiving only one fromAdmin() - might depend on fixstore
				// or on dictionary usage
			_logger.info(String.format("Received sequence number %d", message.getHeader().getInt(34)));
			if (!message.getHeader().getString(35).equals("A") && !message.getHeader().getString(35).equals("5")) { // if
																													// not
																													// logon
																													// or
																													// logout
				_logger.info(String.format("FIrst sequence number after Logon: %d", message.getHeader().getInt(34)));
				_lastTargetSeqNumAfterLogonLogout = message.getHeader().getInt(34);
			}

			String rawstr = message.toString();
			_logger.debug(String.format("fromAdmin(sessionId=%s): userstr:[%s], rawstr:[%s]", sessionId.toString(),
					_fixHelper.convertRawstrToUserstr(rawstr), rawstr));

			if (message.getHeader().isSetField(35)) {
				// If LOGOUT then check if sequence number should be updated
				if (message.getHeader().getString(35).equals("5")) { // logout
					if (message.isSetField(58)) {
						String logoutMessage = message.getString(58);
						if (logoutMessage.indexOf("Low sequence number ") > -1) {
							// message sequence number we are sending is too low
							int index = logoutMessage.indexOf("expecting");
							if (index > -1) {
								int nextSpace = logoutMessage.lastIndexOf(' ');
								if (nextSpace > -1) {
									int expectedNum = Integer.parseInt(logoutMessage.substring(nextSpace + 1));
									_logger.warn(String.format("RETRIEVE AND REMEMBER NEW SEQUENCE NUMBER '%d'",
											expectedNum));
									// _nextSenderSeqNum = expectedNum; // used during Logon

									try {
										Session.lookupSession(sessionId).setNextSenderMsgSeqNum(expectedNum);
									} catch (IOException e) {
										_logger.info(String.format("Can't look up session "+ e.getMessage()));

									}
								}
							}
						}
					}
				} else if (message.getHeader().getString(35).equals("A")) { 
					// login here we store MsgSeqNum to ignore everything below this number
					_logger.info(String.format("Sequence number on Logon: %d", message.getHeader().getInt(34)));
					_lastTargetSeqNumOnLogon = message.getHeader().getInt(34);
				}

				// Add session reject messages to the received queue so that we
				// can verify them
				if (message.getHeader().getString(35).equals("3")) {
					synchronized (_receivedMessages) {
						_receivedMessages.add(message);
					}
				}

			}
		} catch (Exception ex) {
			_logger.error(String.format("fromAdmin() raised exception: %s", ex.toString()));
		}

	}
	/**
	 * Sends application messages 
	 */
	@Override
	public void toApp(Message message, SessionID sessionId) throws DoNotSend {
		try {
			
			if (_originalMessage != null) {
				for (int i = 0; i < tagsToKeep.length; i++) {
					int tag_int = tagsToKeep[i];
					if (_originalMessage.getHeader().isSetField(tag_int)) {
						message.getHeader().setString(tag_int, _originalMessage.getHeader().getString(tag_int));
					} else if (_originalMessage.isSetField(tag_int)) {
						message.setString(tag_int, _originalMessage.getString(tag_int));
					}
				}

				_originalMessage = null;
			}

			if(hookMsgBeforeSending.get() != null) {
				try {
				hookMsgBeforeSending.get().handleMessage(message, sessionId);
				} catch (Exception ex) {
					// Log error and continue procesing
					_logger.error("Error in hook " + ex.getMessage());
				}
			}

			String rawstr = message.toString();
			_logger.debug(String.format("toApp(sessionId=%s): userstr:[%s], rawstr:[%s]", sessionId.toString(),
					_fixHelper.convertRawstrToUserstr(rawstr), rawstr));
		} catch (Exception ex) {
			_logger.error(String.format("toApp() raised exception: %s", ex.toString()));
		}
	}
	/**
	 * Receives application messages
	 */
	@Override
	public void fromApp(Message message, SessionID sessionId)
			throws FieldNotFound, IncorrectDataFormat, IncorrectTagValue, UnsupportedMessageType {
		try {
			// _msgSeqNumClientLast = message.getHeader().getInt(34);

			String rawstr = message.toString();
			_logger.debug(String.format("received userstr=[%s],%nrawstr=[%s]%n",
					_fixHelper.convertRawstrToUserstr(rawstr), rawstr));

			if (message.getException() != null && _fixHelper._raiseParseException) {
				throw new RuntimeException("Error during parsing string received via TCP to FIX message",
						message.getException());
			}

			synchronized (_receivedMessages) {
				_receivedMessages.add(message);
			}
		} catch (Exception ex) {
			_logger.error(String.format("fromApp() raised exception: %s", ex.getMessage()));
		}
	}
	/**
	 * Prints the received messages
	 */
	@Override
	public void flushBuffersToLog(boolean isSingleTread) {
		synchronized (_receivedMessages) {
			_logger.info(String.format("Agent had %d received messages in buffer", _receivedMessages.size()));

			int i = 1;
			for (quickfix.Message msg : _receivedMessages) {
				_logger.debug(String.format("Message #%d: %s", i, msg.toString()));
				i++;
			}
		}
	}
	 /**
	  * @see fast.common.agents.FixAgent#createFixMessage(String)
	  */
	public StepResult createFixMessage(String rawMsg) throws InvalidMessage { // for
																				// debugging
		Message msg = _fixHelper.createFixMessage(rawMsg);
		FixStepResult result = new FixStepResult(msg, _fixHelper);
		return result;
	}
	/**
	 * @see fast.common.glue.CommonStepDefs#sendRawFixMessage(String, String, String)
	 * @see fast.common.agents.FixAgent#sendRawMessage(String)
	 */
	public FixStepResult sendRawMessage(String rawMsg) throws Throwable { // for
																			// debugging
		Message msg = _fixHelper.createFixMessage(rawMsg);
		_sendMessage(msg);
		FixStepResult result = new FixStepResult(msg, _fixHelper);
		return result;
	}
	
	public void setHookMsgBeforeSending(MsgHook hookMsgBeforeSending) {
		this.hookMsgBeforeSending.set(hookMsgBeforeSending);
	}
	
	public void resetHookMsgBeforeSending() {
		this.hookMsgBeforeSending.set(null);
	}
}

