
package fast.common.fix;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import fast.common.agents.IFixMessagingAgent;
import fast.common.context.FixStepResult;
import fast.common.context.HelperMethods;
import fast.common.context.ScenarioContext;
import fast.common.context.StepResult;
import fast.common.context.MapMessageTemplateHelper;
import fast.common.core.Configurator;
import fast.common.core.MapMessageTemplate;
import fast.common.core.ValidationFailed;
import fast.common.logging.FastLogger;
import fast.common.replay.*;

import org.apache.commons.lang3.time.FastDateFormat;

import quickfix.*;
import quickfix.Dictionary;
import quickfix.field.ClOrdID;
import quickfix.field.MsgType;
import quickfix.field.OrderID;
import quickfix.field.OrderQty;

import javax.xml.xpath.XPathExpressionException;

import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FixHelper {
	static FastLogger logger = FastLogger.getLogger("FixHelper");

	public static final String FIXMESSAGETEMPLATES_MAP_NAME = "FixMessageTemplates";
	public static final String STRUCTUREDFIXMESSAGETEMPLATES_MAP_NAME = "StructuredFixMessageTemplates";
	public static final String DUP_TAGS_TO_REMOVE_IN_EMS = "RemoveDuplicateTagsInIncomingEmsMessages";
	public static final String RAISE_PARSE_EXCEPTION = "RaiseFixParseException";
	public boolean _raiseParseException = false; // also used in Tcp agent on
													// message receive

	private Map<String,MapMessageTemplate> fixTemplates= new HashMap<String, MapMessageTemplate>();
	private final Set _dupTagsToRemoveInEms;

	private static final FastDateFormat sendingTimeFormatter;
	private static final FastDateFormat transactTimeFormatter;

	private static final String FAIL_ON_ADDITIONAL_TAGS = "FailOnAdditionalTags";
	private static final String ALLOW_EMPTY_TAGS_IN_MSG = "AllowEmptyTagsInMsg";

	private static final String FIX_BEGIN_STR = "8=FIX";
	private static final String FIX_BODYLENGTH = "9=";
	private static final String FIX_CHECKSUM = "10=";	
	public static final Character FIX_SEP_ESCAPE = 'Â¶';
	private static final String VALIDATE_BY_DEFAULT = "validateByEqual";

	private static int _counter = 0;
	private static Object _orderIdSyncObject = new Object();
	static {
		sendingTimeFormatter = FastDateFormat.getInstance("yyyyMMdd-HH:mm:ss", TimeZone.getTimeZone("UTC"));
		transactTimeFormatter = FastDateFormat.getInstance("yyyyMMdd-HH:mm:ss.SSS", TimeZone.getTimeZone("UTC"));
	}

	private DataDictionary _transportDataDictionary;
	private DataDictionary _appDataDictionary;

	private boolean failOnAdditionalTags = false;

	private boolean allowEmptyTagsInMessage = false;
	
	private static boolean validateByEqual =  false;


	@SuppressWarnings("unchecked")
	public FixHelper(Map agentParams, Configurator configurator) throws ConfigError {
		_raiseParseException = Configurator.getBooleanOr(configurator.getSettingsMap(), RAISE_PARSE_EXCEPTION, false); // global
																														// setting

		allowEmptyTagsInMessage = Configurator.getBooleanOr(configurator.getSettingsMap(), ALLOW_EMPTY_TAGS_IN_MSG, false); // global setting

		failOnAdditionalTags = Configurator.getBooleanOr(configurator.getSettingsMap(), FAIL_ON_ADDITIONAL_TAGS, false); // global setting
		validateByEqual = Configurator.getBooleanOr(agentParams, VALIDATE_BY_DEFAULT, false);

		String transportDataDictionary_filename = configurator.getFilenameOr(agentParams, "TransportDataDictionary",
				null);
		String appDataDictionary_filename = configurator.getFilenameOr(agentParams, "AppDataDictionary", null);
		String data_dictionary_new_filename = configurator.getFilenameOr(agentParams, "DataDictionary", null);
		String data_dictionary_old_filename = configurator.getFilenameOr(agentParams, "data_dictionary", null);

		if (data_dictionary_old_filename != null && data_dictionary_new_filename != null) {
			logger.warn("Parameter 'data_dictionary' is not used because we have 'DataDictionary'");
		}

		if (transportDataDictionary_filename != null && appDataDictionary_filename != null) {
			if (data_dictionary_old_filename != null) {
				logger.warn(
						"Parameter 'data_dictionary' is not user because we have both 'TransportDataDictionary' and 'AppDataDictionary'");
			}
		}

		if (transportDataDictionary_filename != null && appDataDictionary_filename != null) {
			if (data_dictionary_old_filename != null) {
				logger.warn(
						"Parameter 'DataDictionary' is not user because we have both 'TransportDataDictionary' and 'AppDataDictionary'");
			}
		}

		if (transportDataDictionary_filename != null && appDataDictionary_filename != null) {
			logger.debug(
					String.format("loading transport data dictionary from '%s'", transportDataDictionary_filename));
			_transportDataDictionary = new DataDictionary(transportDataDictionary_filename);

			logger.debug(String.format("loading app data dictionary from '%s'", appDataDictionary_filename));
			_appDataDictionary = new DataDictionary(appDataDictionary_filename);
		} else if (data_dictionary_new_filename != null) {
			logger.debug(
					String.format("loading transport and app data dictionary from '%s'", data_dictionary_new_filename));
			_transportDataDictionary = new DataDictionary(data_dictionary_new_filename);
			_appDataDictionary = _transportDataDictionary;
		} else if (data_dictionary_old_filename != null) {
			logger.debug(
					String.format("loading transport and app data dictionary from '%s'", data_dictionary_old_filename));
			_transportDataDictionary = new DataDictionary(data_dictionary_old_filename);
			_appDataDictionary = _transportDataDictionary;
		} else {
			throw new RuntimeException(
					"No data dictionary provided. Use 'DataDictionary' or 'AppDataDictionary' and 'TransportDataDictionary' parameters");
		}
		MapMessageTemplateHelper.populateTemplateMap(configurator.getSettingsMap(), FIXMESSAGETEMPLATES_MAP_NAME, null, fixTemplates,_appDataDictionary);
		MapMessageTemplateHelper.populateTemplateMap(configurator.getSettingsMap(), STRUCTUREDFIXMESSAGETEMPLATES_MAP_NAME, null, fixTemplates,_appDataDictionary);	
		_dupTagsToRemoveInEms = Configurator.getSetOr(configurator.getSettingsMap(), DUP_TAGS_TO_REMOVE_IN_EMS, null);
	}

	public static String getSendingTimeStr() {
		return sendingTimeFormatter.format(new Date());
	}

	public static String getTransactTimeStr() {
		return transactTimeFormatter.format(new Date());
	}

	public static String generateClOrdID() {
		int orderId = getNextNumber();
		String orderId_str = Integer.toString(orderId) + "/" + getTransactTimeStr();
		return orderId_str;
	}

	public static int getNextNumber() {
		int counter;
		synchronized (_orderIdSyncObject) {
			_counter += 1;
			counter = _counter;
		}
		return counter;
	}

	private int convertKeyToTag(String key) throws XPathExpressionException {
		int tag_int = 0;
		try {
			tag_int = Integer.parseInt(key); // can be negative!
		} catch (Exception ex) {
			tag_int = _appDataDictionary.getFieldTag(key);
		}
		// tag_int can be <0 now
		return tag_int;
	}

	public String convertUserstrToFulluserstr(String msgName, String userstr, String extraTags) {		
		MapMessageTemplate userMapMessage = MapMessageTemplateHelper.generateTemplate(msgName, userstr, fixTemplates,_appDataDictionary);
		
		String fullUserstr = userMapMessage.getFieldValueString();

		if (extraTags != null) {
			fullUserstr = extraTags + MapMessageTemplateHelper.MESSAGE_FIELD_SEP + fullUserstr;
		}

		logger.debug(String.format("msgName='%s' userstr=[%s], extraTags=[%s] => fulluserstr=[%s]", msgName, userstr,
				extraTags, fullUserstr));
		return fullUserstr;
	}

	public Message convertFulluserstrToMessage(ScenarioContext scenarioContext, String fulluserstr)
			throws XPathExpressionException, InvalidMessage {
		boolean weExpectRepeatingGroups = false;
		String msgTypeValue = null;	
		int msgTypeStart = fulluserstr.lastIndexOf("|35=");
		if (msgTypeStart != -1) {
			// get value of tag 35
			int endIndex = fulluserstr.indexOf("|", msgTypeStart + 1);
			int valueIndex = -1;
			if (endIndex != -1) {
				valueIndex = fulluserstr.indexOf("=", msgTypeStart + 1);
			}
			if (valueIndex != -1) {
				msgTypeValue = fulluserstr.substring(valueIndex + 1, endIndex);
			}
		}
		ArrayList<StringField> tagsList = new ArrayList<>();
		/*Made changes to FixHelper.
		Moshin Comments : Commenting the below line because its getting failed if we have given a reference of a tag from a template and it has Blank Data or it does have the reference template.
		Eg. I want to override a cancel message with tag 11, tag 37,  etc.... from feature file coz I know I wont get any data from the template then it will get fail.

		Process string should happens only after merging of tags. 
		*/
		
		//if (scenarioContext != null) fulluserstr = scenarioContext.processString(fulluserstr); --- Commented by Moshin
              
		String[] tagValuePairs = fulluserstr.split("\\|"); // TODO: consider \|
															// and \\ - they
															// should not split
															// string
		for (String tagValuePair : tagValuePairs) {
			if (tagValuePair.length() > 0) // can be 0 if data driven and empty
											// value
			{
				int pos = tagValuePair.indexOf("=");
				if (pos < 0) {
					throw new RuntimeException(String.format(
							"symbol '=' not found in tag-value pair '%s' in message [%s]", tagValuePair, fulluserstr));
				}

				String key = tagValuePair.substring(0, pos); // i is not
																// included
				if (scenarioContext != null) { // if we need to resolve then do
												// it
					key = scenarioContext.processString(key); // process tag
																// name. because
																// it can be
																// parameter -
																// e.g.
																// $CapacityTag
				}
				int tag_int = convertKeyToTag(key); // using quckfix dictionary,
													// tag_int can also be
													// negative
				String value_str = null;
				if (pos < tagValuePair.length() - 1) { // not empty tag value.
														// However it still can
														// be empty via
														// parameter value!
					value_str = tagValuePair.substring(pos + 1);
				}

				boolean needToAddField = true;
				// TODO: consider repeating groups! and repeating tags!
				if (_appDataDictionary.IsRepeatingTag(msgTypeValue, tag_int)) {
					// do nothing - we will add it anyway
					weExpectRepeatingGroups = true;
				} else {
					int foundCount = 0;
					// here we assume that this tag can't be repeated!
					// this way we will keep initial order of tags - as Ganesh
					// wanted
					for (int i = tagsList.size() - 1; i >= 0; i--) {
						StringField storedField = tagsList.get(i);
						if (storedField.getTag() == tag_int) { // need to
																// overwrite or
																// delete
							foundCount++;
							needToAddField = false;

							if (value_str == null) { // need to delete
								tagsList.remove(i);
							} else {
								// at this stage value_str is UNPROCESSED (we
								// will process it later because at this time
								// some fields that should be overriden may not
								// be processed)
								storedField.setValue(value_str); // overwriting,
																	// via param
																	// it can be
																	// empty -
																	// we will
																	// resolve
																	// param
																	// later and
																	// decide
																	// later
							}
						}
					}

					if (foundCount > 1) {
						throw new RuntimeException(String.format(
								"This should never happen. Field '%d' was found more than one time in str '%s' after override",
								tag_int, fulluserstr));
					}

				}
				if (needToAddField) { // we add new field
					if (value_str != null) {
						StringField newField = new quickfix.StringField(tag_int, value_str); // value_str
																								// can
																								// be
																								// null.
																								// it
																								// is
																								// unprocessed
																								// here
																								// as
																								// it
																								// might
																								// be
																								// overriden
						tagsList.add(newField);
					}
				}
			}
		}

		StringBuilder rawMsgStr = new StringBuilder();
		// TODO: we might have two data dictionaties!
		// http://www.quickfixj.org/quickfixj/usermanual/1.5.0/usage/configuration.html
		// DataDictionary, TransportDataDictionary, AppDataDictionary
		// now tagsList does not have duplicates. value are unprocessed yet
		Message msg = new Message(); // not used really TODO: get rid of it - we
										// create it later anyway
		StringField msgTypeField = null;
		StringField msgChecksum = null;
		for (StringField field : tagsList) {
			int tag_int = field.getTag();
			String value = field.getValue();
			if (scenarioContext != null) { // if we need to resolve then do it
				value = scenarioContext.processString(value); // processed
																// string now,
																// also need to
																// resolve
																// quickfix
																// consts
			}
			value = _appDataDictionary.getReverseValueName(tag_int, value);
			field.setValue(value); // save processed value back

			if (tag_int == 35) {
				msgTypeField = field;
			} else if (tag_int == 10) {
				// Move tag 10 to the end otherwise quickfix skips all the tags
				// after 10 and we dont compare them
				msgChecksum = field;
			} else { // we skip tag 35 and add it in front of msg!
				if (value != null && value.length() > 0) { // we skip empty
															// fields - they
															// overrided prev
															// fields and should
															// now be skipped
					rawMsgStr.append(String.format("%d=%s\001", tag_int, value)); // store
																					// processed
																					// tag
																					// and
																					// value
				}
			}

			if (_transportDataDictionary.isHeaderField(tag_int)) {
				msg.getHeader().setField(field);
			} else {
				msg.setField(field);
			}
		}

		if (msgTypeField == null) { // this may happen only during RECEIVE!
									// TODO: think - may be we should have
									// another way of validation for NOT RECEIVE
									// - not via creating fix message?
			rawMsgStr.insert(0, "35=D\001"); // added temp FAKE msg type
		} else {
			rawMsgStr.insert(0, String.format("%d=%s\001", msgTypeField.getTag(), msgTypeField.getValue())); // store
																												// processed
																												// tag
																												// and
																												// value
		}

		// Add tag 10 to the end
		if (msgChecksum != null) {
			rawMsgStr.append(String.format("10=%s\001", msgChecksum));
		}
		String resoreCharacters = HelperMethods.UnEscapseReservedCharacters(rawMsgStr
				.toString());

		msg = createFixMessage(resoreCharacters);
		// msg = new Message(rawMsgStr.toString(), _appDataDictionary, false);

		if (msgTypeField == null) {
			msg.getHeader().removeField(35); // remove fake field
		}

		return msg; // fully ready message
	}

	public quickfix.Message convertUserstrToMessage(ScenarioContext scenarioContext, String msgName, String userstr,
			String extraTags) throws XPathExpressionException, InvalidMessage {
		String fullUserstr = convertUserstrToFulluserstr(msgName, userstr, extraTags); // unprocessed
																						// yet
																						// -
																						// both
																						// tags
																						// and
																						// values!

		quickfix.Message msg = convertFulluserstrToMessage(scenarioContext, fullUserstr); // process
																							// TagNames
																							// and
																							// remove
																							// duplicate
																							// tags,
																							// values
																							// are
																							// unprocessed
		return msg;
	}
	
	public quickfix.Message convertUserstrToMessage(ScenarioContext scenarioContext, MapMessageTemplate userMapMessage,
			String extraTags) throws XPathExpressionException, InvalidMessage {
		String fullUserstr = userMapMessage.getFieldValueString();

		if (extraTags != null) {
			fullUserstr = extraTags + MapMessageTemplateHelper.MESSAGE_FIELD_SEP + fullUserstr;
		}	

		quickfix.Message msg = convertFulluserstrToMessage(scenarioContext, fullUserstr); 		
		return msg;
	}

	private Map<String, String> convertRawStringToFixTagMap(String rawstr) {
		Map<String, String> arr = new HashMap<String, String>();
		String[] tags = rawstr.split("\\x01");
		for (String tag : tags) {
			if (tag.length() > 0) { // last element is "" because of trailing
									// '\x01'
				int i = tag.indexOf("=");
				if (!allowEmptyTagsInMessage && (i >= tag.length() - 1)) // empty tag - we expect this means
				// tag should be ignored
				{
					throw new RuntimeException(String.format("empty tag: '%s' in [%s]", tag, rawstr));
				}
				String key = tag.substring(0, i); // i is not included
				String value_str = tag.substring(i + 1);
				arr.put(key, value_str);
			}
		}
		return arr;
	}

	public String convertRawstrToUserstr(String rawstr) {
		String userstr = "";
		Map<String, String> arr = convertRawStringToFixTagMap(rawstr);
		for (Map.Entry<String, String> pair : arr.entrySet()) {
			String key = pair.getKey();
			String value = pair.getValue();
			int field = Integer.parseInt(key);
			String key_name = _appDataDictionary.getFieldName(field);
			if (key_name == null)
				key_name = key;
			String value_name = _appDataDictionary.getValueName(field, value);
			if (value_name == null)
				value_name = value;

			String tag = key_name + "=" + value_name;
			userstr += tag + "|";
		}

		userstr = userstr.substring(0, userstr.length() - 1);
		return userstr;
	}

	public void checkMessageContainsUserstr(ScenarioContext scenarioContext, quickfix.Message actualMessage,
			String userstr) throws XPathExpressionException, FieldNotFound, InvalidMessage {
		logger.debug(
				String.format("checking that message [%s] contains userstr [%s]", actualMessage.toString(), userstr));
		Message expectedMessage = convertFulluserstrToMessage(scenarioContext, userstr);

		compareMessage(actualMessage, expectedMessage, false);
	}

	public String getMessageFieldValue(String key, Message msg) throws XPathExpressionException, FieldNotFound {
		int tag_int = convertKeyToTag(key);
		String result = null;
		if (_transportDataDictionary.isHeaderField(tag_int)) {
			result = msg.getHeader().getString(tag_int);
		} else {
			result = msg.getString(tag_int);
		}
		return result;
	}

	private Boolean receiveReportCancel(ScenarioContext scenarioContext, IFixMessagingAgent agent, String clIrdID)
			throws Throwable {
		String msgName = "ReportCancel";
		String userstr = String.format("ClOrdID=%s", clIrdID);
		try {
			agent.receiveMessage(scenarioContext, msgName, null, userstr);
			return true;
		} catch (Exception ex) { // do nothing here - we didn't receive message
		}

		return false;
	}

	private Boolean receiveRejectNew(ScenarioContext scenarioContext, IFixMessagingAgent agent, String clIrdID)
			throws Throwable {
		String msgName = "RejectNew";
		String userstr = String.format("ClOrdID=%s", clIrdID);
		try {
			agent.receiveMessage(scenarioContext, msgName, null, userstr);
			return true;
		} catch (Exception ex) { // do nothing here - we didn't receive message
		}

		return false;
	}

	private Boolean receiveConfirmNew(ScenarioContext scenarioContext, IFixMessagingAgent agent, String clIrdID)
			throws Throwable {
		String msgName = "ConfirmNew";
		String userstr = String.format("ClOrdID=%s", clIrdID);
		try {
			agent.receiveMessage(scenarioContext, msgName, null, userstr);
			return true;
		} catch (Exception ex) { // do nothing here - we didn't receive message
		}

		return false;
	}

	private void clearOrderBook(ScenarioContext scenarioContext, IFixMessagingAgent agent, String templateSetName,
			String userStr) throws Throwable {
		logger.debug(String.format("clearing order book using template '%s'", templateSetName));
		if (templateSetName == null || templateSetName.length() == 0) {
			throw new RuntimeException(
					String.format("Can't clear orderbook with null or empty template: '%s'", templateSetName));
		}

		// Symbol should be either in templateSet or in userStr

		int numTries = 5;
		// BUY
		String msgName = templateSetName + "_Clear_Buy";
		Boolean receivedCancelOrReject = false;
		for (int i = 0; i < numTries; i++) {
			StepResult result = agent.sendMessage(scenarioContext, msgName, null, userStr);
			String clOrdId = result.getFieldValue("ClOrdID");

			if (receiveReportCancel(scenarioContext, agent, clOrdId)
					|| receiveRejectNew(scenarioContext, agent, clOrdId)) {
				receivedCancelOrReject = true;
				break;
			}
		}
		if (!receivedCancelOrReject) {
			String errorMsg = String
					.format("CLEAR ORDERBOOK using '%s' failed - can not receive Report Cancel or Reject New", msgName);
			throw new RuntimeException(errorMsg);
		}

		// SELL
		msgName = templateSetName + "_Clear_Sell";
		receivedCancelOrReject = false;
		for (int i = 0; i < numTries; i++) {
			StepResult result = agent.sendMessage(scenarioContext, msgName, null, userStr);
			String clOrdId = result.getFieldValue("ClOrdID");

			if (receiveReportCancel(scenarioContext, agent, clOrdId)
					|| receiveRejectNew(scenarioContext, agent, clOrdId)) {
				receivedCancelOrReject = true;
				break;
			}
		}
		if (!receivedCancelOrReject) {
			String errorMsg = String
					.format("CLEAR ORDERBOOK using '%s' failed - can not receive Report Cancel or Reject New", msgName);
			throw new RuntimeException(errorMsg);
		}
	}

	void fillOrderBook(ScenarioContext scenarioContext, IFixMessagingAgent agent, String templateSetName,
			String orderBook, String extraTags) throws Throwable {
		HashMap<String, List<Integer>> book = new ObjectMapper()
				.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true)
				.readValue(orderBook, new TypeReference<HashMap<String, List<Integer>>>() {
				});

		// Symbol should be either in templateSet or in userStr

		for (String price : book.keySet()) { // TODO: doublecheck if it works
			for (Integer amount : book.get(price)) {

				String msgName = templateSetName + "_Set_Buy";
				if (amount < 0) {
					msgName = templateSetName + "_Set_Sell";
				}

				String userstr = String.format("Price=%s|OrderQty=%s", price, Integer.toString(Math.abs(amount)));
				if (extraTags != null)
					userstr = userstr + "|" + extraTags;
				logger.info("Filling order book with " + userstr);
				StepResult result = agent.sendMessage(scenarioContext, msgName, null, userstr);
				String clOrdId = result.getFieldValue("ClOrdID");
				logger.info("Response is " + result.toString());
				if (!receiveConfirmNew(scenarioContext, agent, clOrdId))
					throw new RuntimeException("Failed to get ConfirmNew response to " + userstr);
			}
		}
	}

	public void setOrderBook(ScenarioContext scenarioContext, IFixMessagingAgent agent, String templateSetName,
			String orderBook, String userStr) throws Throwable {
		logger.debug(
				String.format("BEGIN: SET ORDER BOOK USING TEMPLATE '%s' AND TAGS '%s'", templateSetName, userStr));
		clearOrderBook(scenarioContext, agent, templateSetName, userStr);
		fillOrderBook(scenarioContext, agent, templateSetName, orderBook, userStr);
		logger.debug(String.format("END: SET ORDER BOOK USING TEMPLATE '%s' AND TAGS '%s'", templateSetName, userStr));

	}

	public void checkMessageNotContainsUserstr(ScenarioContext scenarioContext, Message actualMessge, String userstr)
			throws InvalidMessage, XPathExpressionException {
		logger.debug(String.format("checking that message [%s] does not contain tags [%s]", actualMessge.toString(),
				userstr));

		String[] tags = userstr.split(",");
		for (String tag : tags) {
			String tag_str = tag.trim();
			int tag_int = convertKeyToTag(tag_str);

			if (actualMessge.isSetField(tag_int)) {
				throw new RuntimeException(String.format("tag %d should not be present in the message", tag_int));
			}
		}
	}	

	private List<Message> findMessageByTag(List<Message> actualMessages, String expectedValue, int tagId)
			throws FieldNotFound {
		List<Message> filterMsg = new ArrayList<Message>();
		for (Message actualMessage : actualMessages) {
			boolean matched = false;
			// if thios tag is in header
			if (_transportDataDictionary.isHeaderField(tagId)) {
				matched = actualMessage.getHeader().getString(tagId).equals(expectedValue);
			} else {
				if (actualMessage.isSetField(tagId)) {
					matched = actualMessage.getString(tagId).equals(expectedValue);
					}
			}

			if (matched == true) {
				filterMsg.add(actualMessage);
			}
		}

		return filterMsg;
	}

	private boolean isPlainValue(Message msg, int tag) throws FieldNotFound {
		String value = null;
		// Check if tag is set in header
		if (_transportDataDictionary.isHeaderField(tag)) {
			if (msg.getHeader().isSetField(tag)) {
				value = msg.getHeader().getString(tag);
			}
		} else {
			// check if tag is in message
			if (msg.isSetField(tag)) {
				value = msg.getString(tag);
			}
		}

		if (value != null) {
			// can be regex
			if ((value.length() > 2) && (value.charAt(0) == '/') && (value.charAt(value.length() - 1) == '/')) {
				return false;
			}
			return true;
		}

		return false;
	}

	public Message findMessage(List<Message> actualMessages, Message expectedMessage) throws FieldNotFound {
		List<Message> found = actualMessages;

		// Find messages by tag 35. This to handle 35=j/3 reject messages
		if (isPlainValue(expectedMessage, MsgType.FIELD)) {
			String expectedValue = expectedMessage.getHeader().getString(MsgType.FIELD);
			found = findMessageByTag(found, expectedValue, MsgType.FIELD);
		}

		// Now refine the messages by comparing with tag 37 then tag 11
		if (isPlainValue(expectedMessage, OrderID.FIELD)) {
			// OrderId (tag 37) is set in expected message - search for message
			// by tag 37
			String expectedValue = expectedMessage.getString(OrderID.FIELD);
			found = findMessageByTag(found, expectedValue, OrderID.FIELD);
		} else if (isPlainValue(expectedMessage, ClOrdID.FIELD)) {
			// ClOrdId (tag 11) is set in expected message - search for message
			// by tag 11
			String expectedValue = expectedMessage.getString(ClOrdID.FIELD);
			found = findMessageByTag(found, expectedValue, ClOrdID.FIELD);
		} else if (isPlainValue(expectedMessage, OrderQty.FIELD)) {
			found = findMessageByTag(found, expectedMessage.getString(OrderQty.FIELD), OrderQty.FIELD);
		} else {
			// We do brute force comparing all the message
		}

		if (found.size() > 0) {
			MessageIncorrect lastKnownFailure = null;
			for (Message msg : found) {
				try {
					compareMessage(msg, expectedMessage, false);
					// No throw, message is correct
					return msg;
				} catch (MessageIncorrect e) {
					lastKnownFailure = e;
				}
			}
			if (lastKnownFailure != null)
				throw lastKnownFailure;

		}

		return null;
	}

	protected ArrayList<TagError_ReplayException> compareRepeatingGroups(quickfix.FieldMap actualMessage,
			quickfix.FieldMap expectedMessage, boolean checkAdditionalTags) throws FieldNotFound {
		// actualMessage can be null if we dont have this repeating group
		ArrayList<TagError_ReplayException> tagErrors = new ArrayList<>();

		Iterator<Integer> iter = expectedMessage.groupKeyIterator();
		while (iter.hasNext()) {
			int groupKey = iter.next();
			List<Group> expectedGroups = expectedMessage.getGroups(groupKey);
			List<Group> actualGroups = null;
			if (actualMessage != null) {
				actualGroups = actualMessage.getGroups(groupKey);
			}

			int expectedSize = expectedGroups.size();
			int actualSize = 0;
			if (actualGroups != null) {
				actualSize = actualGroups.size();
			}
			if (actualSize != expectedSize) {
				// incorrect group size error
				String groupKeyName = _appDataDictionary.getFieldName(groupKey);
				tagErrors.add(new TagIncorrectGroupSize(groupKey, groupKeyName, expectedSize, actualSize));
				// continue; // no need to report all?
			}

			for (int i = 0; i < expectedSize; i++) {
				Group expectedGroup = expectedGroups.get(i);
				Group actualGroup = null;
				if (i < actualSize && actualGroups != null) {
					actualGroup = actualGroups.get(i); // can be null if group
														// does not exist
				}
				if(actualGroup != null && expectedGroup != null)
					tagErrors.addAll(compareMessageParts(actualGroup, expectedGroup, checkAdditionalTags));
			}
		}

		return tagErrors;
	}

	public void compareMessage(Message actualMessage, Message expectedMessage, boolean checkAdditionalTags)
			throws FieldNotFound {
		Message actualMsgClone = (Message) actualMessage.clone();
		// expectedMessage.removeField(9); // BodyLength should not be checked
		// this is not working
		ArrayList<TagError_ReplayException> tagErrors = new ArrayList<TagError_ReplayException>();
		tagErrors.addAll(
				compareMessageParts(actualMsgClone.getHeader(), expectedMessage.getHeader(), checkAdditionalTags));
		tagErrors.addAll(compareMessageParts(actualMsgClone, expectedMessage, checkAdditionalTags));
		// DONE: we now support repeating groups - inside compareMessageParts

		if (tagErrors.size() > 0) {
			throw new MessageIncorrect(actualMessage, tagErrors);
		}
	}

	private ArrayList<TagError_ReplayException> compareMessageParts(quickfix.FieldMap actualMessage,
			quickfix.FieldMap expectedMessage, boolean checkAdditionalTags) throws FieldNotFound {
		// actualMessage can be null in case we comparing egainst empty
		// repeating group
		ArrayList<TagError_ReplayException> tagErrors = new ArrayList<>();

		Iterator<Field<?>> expectedIter = expectedMessage.iterator();
		while (expectedIter.hasNext()) {
			Field<?> expectedField = expectedIter.next();
			int tag = expectedField.getTag();

			if (checkAdditionalTags == true) {
				// remove this tag from the actual message if additional
				// tags need to be verified
				if(actualMessage != null)
					actualMessage.removeField(tag);
			} else {
				if (tag == 9) { // we skip and not check tag BodyLength (9)
					continue;
				}

				String expectedValue = expectedField.getObject().toString();

				if ((actualMessage != null) && actualMessage.isSetField(tag)) {
					String actualValue = actualMessage.getString(tag);

					// can be regex
					if ((expectedValue.length() > 2) && (expectedValue.charAt(0) == '/')
							&& (expectedValue.charAt(expectedValue.length() - 1) == '/')) {
						String rx = expectedValue.substring(1, expectedValue.length() - 1);
						Pattern p = Pattern.compile(rx);
						Matcher m = p.matcher(actualValue);
						if (!m.find()) {
							// error in regex
							String tagName = _appDataDictionary.getFieldName(tag);
							String actualValueName = _appDataDictionary.getValueName(tag, actualValue);
							tagErrors.add(new TagIncorrectFormat(tag, rx, actualValue, tagName, actualValueName));
						}
						// else this tag is good
					} else if (expectedValue.startsWith(FIX_BEGIN_STR)) {
						// compare as 2 fix messages
						try {
							Message actualSubMessage = createFixMessage(actualValue);
							Message expectedSubMessage = createFixMessage(expectedValue);
							compareMessage(actualSubMessage, expectedSubMessage, checkAdditionalTags);
						} catch (InvalidMessage e) {
							// Messages cannot be parsed properly. Compare as strings
							if (!actualValue.equals(expectedValue)) {
								String tagName = _appDataDictionary.getFieldName(tag);
								String expectedValueName = _appDataDictionary.getValueName(tag, expectedValue);
								String actualValueName = _appDataDictionary.getValueName(tag, actualValue);
								tagErrors.add(new TagIncorrectValue(tag, expectedValue, actualValue, tagName,
										expectedValueName, actualValueName));
							}
							// else this tag is good
						} catch (MessageIncorrect eMsg) {
							tagErrors.add(new TagIncorrectSubTagValue(tag, eMsg.getTagErrors()));
						}
						// else this tag is good
					} else if (!actualValue.equals(expectedValue)) {
						// error in tag
						String tagName = _appDataDictionary.getFieldName(tag);
						String expectedValueName = _appDataDictionary.getValueName(tag, expectedValue);
						String actualValueName = _appDataDictionary.getValueName(tag, actualValue);
						tagErrors.add(new TagIncorrectValue(tag, expectedValue, actualValue, tagName, expectedValueName,
								actualValueName));
					} // else this tag is good
				} else {
					// missing tag or actual is null (empty repeating group)
					String tagName = _appDataDictionary.getFieldName(tag);
					String expectedValueName = _appDataDictionary.getValueName(tag, expectedValue);
					tagErrors.add(new TagMissing(tag, expectedValue, tagName, expectedValueName));
				}
			}
		}

		if (checkAdditionalTags) {
			// If the FieldMap is empty, no additional tags
			if(actualMessage!=null && !actualMessage.isEmpty()){
					// Report additional tags
					Iterator<Field<?>> actualIter = actualMessage.iterator();
					while (actualIter.hasNext()) {
						Field<?> field = actualIter.next();
						int tag = field.getTag();
						tagErrors.add((new MessageAdditionalTag(tag)));
				}
			}else{
				logger.error("!!! No actual messages!");
			}
		}

		// now let's iterate through all groups and compare them - it will have
		// recursive calls to compareMessageParts()
		tagErrors.addAll(compareRepeatingGroups(actualMessage, expectedMessage, checkAdditionalTags));

		return tagErrors;
	}

	public void saveResult(FixStepResult result, ScenarioContext scenarioContext, String templateName, String varName) {
		// save result as variable under template name and under varName
		if (templateName != null) { // always save under template name if it is
									// defined
			scenarioContext.saveLastStepResult(result, "@" + templateName);
		}
		scenarioContext.saveLastStepResult(result, varName);
	}
	
	public FixStepResult receiveAndVerifyMessage(ArrayList<Message> receivedMessages,ScenarioContext scenarioContext, String templateName,String varName, String userstr, String receiveExtraTags)
			throws FieldNotFound, ValidationFailed, XPathExpressionException,
			InvalidMessage, InterruptedException {
		int numTries = 500; 
		int pollTimeout = 10; 
		for (int i = 0; i < numTries; i++) {
			if (receivedMessages.isEmpty()) {
				try {
					Thread.sleep(pollTimeout);
				} catch (InterruptedException e) {
					logger.error(e.getMessage());
					throw e;
				}
			}else{
				break;
			}
		}
		if (receivedMessages.isEmpty()) {
			throw new ValidationFailed(String.format("Searched %d messages in the buffer:", receivedMessages.size()));
		}
	
		quickfix.Message expectedMessage = convertUserstrToMessage(scenarioContext, templateName, userstr, receiveExtraTags);
		String rawstr = expectedMessage.toString();
		logger.debug(String.format(
				"CHECK RECEIVED userstr=[%s],%nrawstr=[%s]%n",
				convertRawstrToUserstr(rawstr), rawstr));
		MapMessageTemplate userMapMessage = MapMessageTemplateHelper
				.generateTemplate(templateName, userstr, fixTemplates,
						_appDataDictionary);

		Message foundMessage = null;
		for (Message message : receivedMessages) {
			StringBuilder diffBuilder = new StringBuilder();
			validateMessageParts(message.getHeader(),
					expectedMessage.getHeader(), userMapMessage, diffBuilder);
			validateMessageParts(message, expectedMessage, userMapMessage,
					diffBuilder);
			if (diffBuilder.length() <= 0) {
				foundMessage = message;
				break;
			}
		}

		if (foundMessage == null) {
			StringBuilder expected = new StringBuilder();
			MapMessageTemplateHelper.getExceptedVerifyMessage(expectedMessage.getHeader(),userMapMessage,expected);
			MapMessageTemplateHelper.getExceptedVerifyMessage(expectedMessage,userMapMessage,expected);
			throw new ValidationFailed(String.format("\n\n!!!!! ERROR: Missing message matching pattern: %s",expected.toString()));
		}
		FixStepResult result = new FixStepResult(foundMessage, this);
		saveResult(result, scenarioContext, templateName, varName);
		return result;

	}
	public FixStepResult receiveMessageAndSaveResult(ArrayList<Message> receivedMessages,
			ScenarioContext scenarioContext, String templateName, String varName, Message expectedMessage)
			throws InterruptedException, FieldNotFound, InvalidMessage {
		try {
			Message foundMessage = checkReceived(receivedMessages, expectedMessage);
			FixStepResult result = new FixStepResult(foundMessage, this);
			// save result as variable under template name and under varName
			saveResult(result, scenarioContext, templateName, varName);
			return result;
		} catch (Exception e) {
			synchronized (receivedMessages) {
				logger.error(String.format("\n\n!!!!! ERROR: Missing message matching pattern: %s",
						expectedMessage.toString()));
				logger.error(String.format("Searched %d messages in the buffer:", receivedMessages.size()));
				for (Message msg : receivedMessages) {
					logger.error(String.format("    %s", msg.toString()));
				}
				logger.error("!!!!!\n\n");
			}
			throw e;
		}
	}

	public Message checkReceived(ArrayList<Message> receivedMessages, Message expectedMessage)
			throws InvalidMessage, InterruptedException, FieldNotFound {
		// Current logic is:
		// - match message by tag 11 if it's defined in expected
		// - else, match message by tag 37 if it's defined in expected
		// - else, match message by order size (tag 53)
		// TODO: This logic is not ideal, as not all testcases have unique order
		// size. Replace this by matching by symbol (needs to be unique per
		// execution thread)

		int numTries = 500; // Poll up to 500 times...
		int pollTimeout = 10; // ...every 10 milliseconds - message timeout = 5
								// seconds
		Message result = null;
		RuntimeException lastBestException = null;
		for (int i = 0; i < numTries; i++) {
			synchronized (receivedMessages) {
				if (receivedMessages.size() > 0) {
					try {
						result = findMessage(receivedMessages, expectedMessage);
						if (failOnAdditionalTags == true && result != null) {
							compareMessage(result, expectedMessage, true);
						}
						if (result != null) {
							receivedMessages.remove(result); 
							return result;
						}
					} catch (MessageIncorrect ex) {
						lastBestException = ex;
					}
				}
			}
			Thread.sleep(pollTimeout);
		}

		if (lastBestException != null) {
			throw lastBestException;
		}

		// Message hasn't been found, throw missing message exception
		String tag11Value = null;
		if (expectedMessage.isSetField(ClOrdID.FIELD)) {
			tag11Value = expectedMessage.getString(ClOrdID.FIELD);
		}
		String tag37Value = null;
		if (expectedMessage.isSetField(OrderID.FIELD)) {
			tag37Value = expectedMessage.getString(OrderID.FIELD);
		}
		throw new MessageMissing_ReplayException(tag11Value, tag37Value, expectedMessage);
	}

	public static void updateFilePathInDictionary(Dictionary d, String configFolder)
			throws FieldConvertError, ConfigError {
		String[] arr = { "DataDictionary", "TransportDataDictionary", "AppDataDictionary" };
		for (int i = 0; i < arr.length; i++) {
			String key = arr[i];
			if (d.has(key)) {
				String filename = d.getString(key);
				String fullFilename = Paths.get(configFolder).resolve(filename).toString();
				d.setString(key, fullFilename);
			}
		}
	}

	// to consider repeating groups
	public Message createFixMessage(String rawMsg) throws InvalidMessage {
		Message msg = new Message(rawMsg, _appDataDictionary, false);
		if (msg.getException() != null && _raiseParseException) {
			throw new RuntimeException(String.format("Error during parsing string '%s' to FIX message", rawMsg),
					msg.getException());
		}
		return msg;
	}

	public String removeDuplicateNonRepeatingGroupTags(String rawstr) {
		if (_dupTagsToRemoveInEms == null)
			return rawstr;

		StringBuilder result = new StringBuilder();

		Map<String, String> arr = new HashMap<String, String>();
		String[] tags = rawstr.split("\\x01");
		for (String tag : tags) {
			boolean removeTag = false;
			if (tag.length() > 0) { // last element is "" because of trailing
									// '\x01'
				int i = tag.indexOf("=");
				if (i >= tag.length() - 1) // empty tag - we don't expect this
											// here
				{
					throw new RuntimeException(String.format("empty tag: '%s' in [%s]", tag, rawstr));
				} else {
					String key = tag.substring(0, i); // i is not included
					String value_str = tag.substring(i + 1);

					if (_dupTagsToRemoveInEms.contains(key)) {
						if (arr.containsKey(key)) {
							// this tag shall be removed
							removeTag = true;
						}
					}

					arr.put(key, value_str);
				}
			}

			if (!removeTag) {
				result.append(tag);
				result.append('\01');
			} else {
				logger.debug(String.format("Remove duplicated tag '%s'", tag));
			}
		}

		arr.clear();
		return result.toString();
	}
	public String validate(ScenarioContext scenarioContext, String result, String templateName, String userstr)
			throws XPathExpressionException, InvalidMessage, FieldNotFound, ValidationFailed {
		quickfix.Message actualMessage = convertFulluserstrToMessage(scenarioContext, result);
		MapMessageTemplate userMapMessage = MapMessageTemplateHelper.generateTemplate(templateName, userstr, fixTemplates,_appDataDictionary);
		quickfix.Message expectedMessage = convertUserstrToMessage(scenarioContext, userMapMessage, null);
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("Actual:" + actualMessage.toString().replaceAll(new Character('\001') + "", MapMessageTemplateHelper.MESSAGE_FIELD_SEP) + "\n");
		StringBuffer expectedMessageBuffer = new StringBuffer(
				expectedMessage.toString().replaceAll(new Character('\001') + "", MapMessageTemplateHelper.MESSAGE_FIELD_SEP));
		expectedMessageBuffer.replace(expectedMessageBuffer.indexOf(MapMessageTemplateHelper.MESSAGE_FIELD_SEP+FIX_CHECKSUM), expectedMessageBuffer.length(), "");
		if (!expectedMessageBuffer.toString().startsWith(FIX_BODYLENGTH)) {
			int tag9Index = expectedMessageBuffer.indexOf(MapMessageTemplateHelper.MESSAGE_FIELD_SEP+FIX_BODYLENGTH);
			expectedMessageBuffer.replace(tag9Index, expectedMessageBuffer.indexOf(MapMessageTemplateHelper.MESSAGE_FIELD_SEP, tag9Index + 1), "");
		}
		expectedMessageBuffer.replace(0, expectedMessageBuffer.indexOf(MapMessageTemplateHelper.MESSAGE_FIELD_SEP) + 1, ""); 
		
		logBuilder.append("Excepted:" + expectedMessageBuffer.toString() + "\n");

		StringBuilder diffBuilder = new StringBuilder();		
		validateMessageParts(actualMessage.getHeader(),expectedMessage.getHeader(), userMapMessage,diffBuilder);
		validateMessageParts(actualMessage,expectedMessage, userMapMessage,diffBuilder);

		if (diffBuilder.length() > 0) {
			throw new ValidationFailed(String.format("Validation Failed:\n%sDifferences:\n%s", logBuilder.toString(),
					diffBuilder.toString()));
		}

		return logBuilder.toString();
	}
	
	public static void validateMessageParts(quickfix.FieldMap actualMessage,
			quickfix.FieldMap expectedMessage,MapMessageTemplate template,StringBuilder diffBuilder) throws FieldNotFound, ValidationFailed {
		Iterator<Field<?>> expectedIter = expectedMessage.iterator();
		while (expectedIter.hasNext()) {
			Field<?> expectedField = expectedIter.next();
			int tag = expectedField.getTag();
			Map validateItem =template.getValidateField(String.valueOf(tag));
			if (validateItem != null) {
				if (!MapMessageTemplateHelper.validateWithField(actualMessage.getString(tag), expectedField.getObject().toString(), validateItem.get(MapMessageTemplateHelper.VALIDATE_DATATYPE).toString(),
						validateItem.get(MapMessageTemplateHelper.VALIDATE_OPERATOR).toString()))
					diffBuilder.append(tag + "=(" + actualMessage.getString(tag) + ")(" + expectedField.getObject().toString() + ")(" + validateItem.get(MapMessageTemplateHelper.VALIDATE_OPERATOR).toString().toUpperCase()
							+ ")\n");
			}else if(validateByEqual){
				if(tag==8 ||tag==9||tag==10)continue;
				String expectedValue = expectedField.getObject().toString();
				String actualValue = actualMessage.getString(tag);
				MapMessageTemplateHelper.validateFieldByDefault(tag, actualValue, expectedValue,diffBuilder);
			}
		}
	}
}

