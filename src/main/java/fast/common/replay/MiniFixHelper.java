package fast.common.replay;

import co.paralleluniverse.fibers.Suspendable;
import fast.common.logging.FastLogger;
import quickfix.*;
import quickfix.field.ClOrdID;
import quickfix.field.MsgType;
import quickfix.field.OrderID;

import java.text.ParseException;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;

class StructuredTagList {
    public HashMap<Integer, String> fields = new HashMap<>();
    public HashMap<Integer, ArrayList<StructuredTagList>> groups = new HashMap<>();

    public void clear()  {
        for(ArrayList<StructuredTagList> group : groups.values()){
            for(StructuredTagList part : group) part.clear();
            group.clear();
        }
        groups.clear();
        fields.clear();
    }

    public void finalize() throws Throwable {
        clear();
        super.finalize();
    }

    public boolean isSetField(int tag) {
        return this.fields.containsKey(tag);
    }

    public String getString(int tag) {
        return this.fields.get(tag);
    }

    public void setString(int tagClOrdID, String origVal) {
        this.fields.put(tagClOrdID, origVal);
    }
}

public class MiniFixHelper {
    FastLogger _logger;
    Map _params;
    ArrayList<Integer> _tagsWithIgnoreValueCheckList; // we don't ignore tags themselves - we ignore tag values only!
    ArrayList<Integer> _tagsWithClOrdIDCheck;
    ArrayList<Integer> _tagsWithOrderIDCheck;
    ArrayList<Integer> _tagsWithDateTimeCheck;
    ArrayList<Integer> _tagsWithTimeInForceCheck;
    ArrayList<Integer> _tagsWithProfilesCheck;
    HashMap<Integer, Float> _tagsWithRangeCheckMap;
    long _timeRangeCheck;
    long _timeSmallRangeCheck;
    boolean _tsFormatCheck;
    ZoneId _C4ZoneId;

    public MiniFixHelper(Map params) {
        _logger = FastLogger.getLogger("MiniFixHelper");

        _params = params;
        ArrayList arrayList = (ArrayList)_params.get("TagsWithIgnoreValueCheck");
        _tagsWithIgnoreValueCheckList = new ArrayList<Integer>();
        for(Object obj: arrayList) {
            _tagsWithIgnoreValueCheckList.add(Integer.parseInt(obj.toString()));
        }

        arrayList = (ArrayList)_params.get("TagsWithClOrdIDGeneratorAndCheck");
        _tagsWithClOrdIDCheck = new ArrayList<Integer>();
        for(Object obj: arrayList) {
            _tagsWithClOrdIDCheck.add(Integer.parseInt(obj.toString()));
        }

        arrayList = (ArrayList)_params.get("TagsWithOrderIDGeneratorAndCheck");
        _tagsWithOrderIDCheck = new ArrayList<Integer>();
        for(Object obj: arrayList) {
            _tagsWithOrderIDCheck.add(Integer.parseInt(obj.toString()));
        }

        arrayList = (ArrayList)_params.get("TagsWithDateTimeGeneratorAndCheck");
        _tagsWithDateTimeCheck = new ArrayList<Integer>();
        for(Object obj: arrayList) {
            _tagsWithDateTimeCheck.add(Integer.parseInt(obj.toString()));
        }

        arrayList = (ArrayList)_params.get("TagsWithTimeInForceGeneratorAndCheck");
        _tagsWithTimeInForceCheck = new ArrayList<Integer>();
        for(Object obj: arrayList) {
            _tagsWithTimeInForceCheck.add(Integer.parseInt(obj.toString()));
        }

        Map rangeValidationTagsStrs = (Map)_params.get("TagsWithRangeCheck");
        _tagsWithRangeCheckMap = new HashMap<Integer, Float>();
        Iterator iter = rangeValidationTagsStrs.entrySet().iterator();
        while(iter.hasNext()) {
            Map.Entry entry = (Map.Entry)iter.next();
            int tag = Integer.parseInt(entry.getKey().toString());
            float value = Float.parseFloat(entry.getValue().toString());
            _tagsWithRangeCheckMap.put(tag, value);
        }

        arrayList = (ArrayList)_params.get("TagsWithProfilesCheck");
        _tagsWithProfilesCheck = new ArrayList<Integer>();
        for(Object obj: arrayList) {
            _tagsWithProfilesCheck.add(Integer.parseInt(obj.toString()));
        }

        _timeRangeCheck = Long.parseLong(_params.get("TimeRangeCheck").toString()); // seconds - used to validate TS fields in different messages

        _timeSmallRangeCheck = Long.parseLong(_params.get("TimeSmallRangeCheck").toString()); // seconds - used to validate TS fields inside the same message

        _tsFormatCheck = (Integer.parseInt(_params.get("SkipTimestampFormatCheck").toString()) != 1);

        _C4ZoneId = ZoneId.of(_params.get("C4TimeZone").toString());
    }

    private quickfix.Message compareFoundAndExpectedMessages(String tag11Value, String tag37Value, ArrayList<quickfix.Message> foundMessage, quickfix.Message expectedMessage, StructuredTagList msgTagsToReadAndStore, DataDictionary dict) throws FieldNotFound, ParseException,NullPointerException{
        // String tag11Value, String tag37Value are used just for reporting error - to keep info how we found actual messages
        //ArrayList<MessageIncorrect_ReplayException> errors = new ArrayList<MessageIncorrect_ReplayException>();
        assert(foundMessage.size() > 0);

        MessageIncorrect_ReplayException bestError = null; // error with least number of tag errors
        for (quickfix.Message actualMessage : foundMessage) {
			_logger.info("expected: 37=" + tag37Value + ", 11="+tag11Value);
			if (actualMessage.isSetField(OrderID.FIELD)) {
				_logger.info("received: 37="+tag37Value);
				if (tag37Value != actualMessage.getString(OrderID.FIELD)) {
					continue;
				}
			} else if (actualMessage.isSetField(ClOrdID.FIELD) && !tag11Value.equals(actualMessage.getString(ClOrdID.FIELD))) {
				_logger.info("received: 37="+tag11Value);
				continue;
			}
            // if null then everything is ok, otherwise return list of errors
            MessageIncorrect_ReplayException error = compareActualAndExpectedMessages(actualMessage, expectedMessage, msgTagsToReadAndStore, dict);
            if(error != null) {
                if(bestError == null) {
                    bestError = error;
                }
                else {
                    if(error.tagErrors.size() < bestError.tagErrors.size()) {
                        bestError = error;
                    }
                }

                //errors.add(error);
            } else {
                return actualMessage; // we found! return it
            }
        }
			throw bestError;

        //throw new MessagesIncorrect_ReplayException(tag11Value, tag37Value, expectedMessage, errors);
    }

    private HashMap<String, String> getKeyValuePairsFromTag(String tagValue, String delimiter) {
        String[] tokens = tagValue.split(delimiter);
        HashMap<String, String> KeyValues = new HashMap<>();
        for (String token: tokens) {
            if(token.length() > 0) // can be 0 if data driven and empty value
            {
                int i = token.indexOf("=");
                if(i < 0) throw new RuntimeException(String.format("symbol '=' not found after key '%s' in string [%s]", token,tagValue));
                String key = token.substring(0,i);
                if(i >= token.length() - 1) {
                    // empty tag - we expect this means tag should be ignored
                    KeyValues.put(key, null);
                }
                else
                {
                    String value_str = token.substring(i+1);
                    KeyValues.put(key, value_str);
                }
            }
        }
        return KeyValues;
    }

    private ArrayList<TagError_ReplayException> compareActualAndExpectedMessagesPart(quickfix.FieldMap actualPart, quickfix.FieldMap expectedPart, StructuredTagList msgTagsToReadAndStore, DataDictionary dict, String msgType) throws FieldNotFound, ParseException {
        ArrayList<TagError_ReplayException> tagErrors = new ArrayList<>();
        // NB! we should ignore values of tags from global ignore list and msgTagsToReadAndStore list
        // however presence of tags should be validated anyway
        Iterator<Field<?>> expectedIter = expectedPart.iterator();
        while (expectedIter.hasNext()) {
            Field<?> expectedField = expectedIter.next();
            int tag = expectedField.getTag();
            String expectedValue = expectedField.getObject().toString();

            if (actualPart.isSetField(tag)) {
                String actualValue = actualPart.getString(tag);

                if((msgTagsToReadAndStore != null) && (msgTagsToReadAndStore.isSetField(tag)) ||
                        _tagsWithIgnoreValueCheckList.contains(tag)) {
                    // we want to skip actual vs expected value validation

                    // but we want to validate time format if it is TS or TIF field
                    if(_tagsWithDateTimeCheck.contains(tag) ||
                            _tagsWithTimeInForceCheck.contains(tag)) {
                        // we need to validate original time format
                        DateTimeFormatter dfExpected = getDateFormatter(expectedValue);
                        DateTimeFormatter dfActual = getDateFormatter(actualValue);
                        if(_tsFormatCheck && (dfExpected != dfActual)) {
                            tagErrors.add(new TagIncorrectFormat_ReplayException(tag, expectedValue, actualValue));
                        }
                    }

                    continue;
                }

                if (!actualValue.equals(expectedValue)) {
                    if(_tagsWithProfilesCheck.contains(tag)) {
                        //We need special comparision logic for such tags as their value is actually a key-value map
                        ArrayList<KeyValueError_ReplayException> profileErrors = new ArrayList<KeyValueError_ReplayException>();
                        HashMap<String, String> actualProfile = getKeyValuePairsFromTag(actualValue, ";");
                        HashMap<String, String> expectedProfile = getKeyValuePairsFromTag(expectedValue, ";");
                        for(HashMap.Entry<String, String> entry : actualProfile.entrySet()) {
                            String actualKey = entry.getKey();
                            String actualVal = entry.getValue();
                            if(expectedProfile.containsKey(actualKey)) {
                                String expectedVal = expectedProfile.get(actualKey);
                                boolean equalVals = compare(expectedVal, actualVal);
                                if(!equalVals) {
                                    profileErrors.add(new KeyValueIncorrectValue_ReplayException(actualKey, expectedVal, actualVal));
                                }
                                expectedProfile.remove(actualKey); //DONE: Was a bug, fixed <-  Andrey, you cannot remove value from collection while inside cycle thru it (for(int i=0; i<actualProfileKey.size(); i++) {)
                            }
                            else {
                                //Unexpected Profile ID in actual message
                                profileErrors.add(new KeyValueUnexpected_ReplayException(actualKey, actualVal));
                            }
                        }
                        if(expectedProfile.size() != 0) {
                            //Some Profiles IDs are missing
                            expectedProfile.forEach((k, v) -> profileErrors.add(new KeyValueMissing_ReplayException(k, v)));
                        }
                        if(profileErrors.size() != 0) {
                            tagErrors.add(new TagWrongKeyValueSet_ReplayException(tag, profileErrors));
                        }
                        expectedProfile.clear();
                        actualProfile.clear();

                        continue;
                    } // if(_tagsWithProfilesCheck.contains(tag))

                    if(_tagsWithRangeCheckMap.containsKey(tag)) { // try to check against range
                        float range = _tagsWithRangeCheckMap.get(tag);
                        float actualValueFloat = 0.0f;
                        float expectedValueFloat = 0.0f;
                        try {
                            actualValueFloat = Float.parseFloat(actualValue);
                            expectedValueFloat = Float.parseFloat(expectedValue);
                        }
                        catch (Exception e) {
                            tagErrors.add(new TagRangeCheckError_ReplayException(tag, expectedValue, actualValue, range, RangeCheckError.CANT_PARSE_VALUES));
                            continue;
                        }

						if (Float.compare(0.0f, expectedValueFloat) != 0) {
                            float min = expectedValueFloat * (1.0f - range);
                            float max = expectedValueFloat * (1.0f + range);
                            if((actualValueFloat < min) ||
                                    (actualValueFloat > max)) {
                                tagErrors.add(new TagRangeCheckError_ReplayException(tag, expectedValue, actualValue, range, RangeCheckError.ACTUAL_NOT_IN_RANGE));
                            } else {
                                // else everything is good - value is within the range
                            }
                        }
						else if (Float.compare(0.0f, actualValueFloat) == 0) {
                            // Ok both values expected and actual are 0.0f -> good
                        }
                        else {
                            tagErrors.add(new TagRangeCheckError_ReplayException(tag, expectedValue, actualValue, range, RangeCheckError.ZERO_EXPECTED));
                        }

                        continue;
                    }

                    if(_tagsWithDateTimeCheck.contains(tag) ||
                            _tagsWithTimeInForceCheck.contains(tag)) {
                        // if this is TS or TIF then IF it does not contain milliseconds then we should compare it for range
                        DateTimeFormatter dfExpected = getDateFormatter(expectedValue);
                        DateTimeFormatter dfActual = getDateFormatter(actualValue);
                        if(_tsFormatCheck && (dfExpected != dfActual)) { // wrong format of TS or TIF field
                            tagErrors.add(new TagIncorrectFormat_ReplayException(tag, expectedValue, actualValue));
                            continue;
                        }
                        else if((dfActual == secondsTimeFormatter) ||
                                (dfActual == millisecondsTimeFormatter) ||
                                (dfActual == microsecondsTimeFormatter) ||
                                (dfActual == nanosecondsTimeFormatter)) { // sometimes 10014 is different while it is the same in PROD
                            if(compareTimeStamps(actualValue, expectedValue, _timeRangeCheck)) {
                                // everything is good - TS is in the range
                                continue;
                            }
                            else { // TS is not in the range
                                tagErrors.add(new TagDateTimeRangeCheckError_ReplayException(tag, expectedValue, actualValue, _timeRangeCheck));
                                continue;
                            }
                        }else { // we check not like TS is date format (not using range) and it is incorrect already
                            // we will report TagIncorrectValue_ReplayException error now with any other errors - below
                        }
                    }

                    // if it is not Profile, not Range, not DateTime/TIF then it value is incorrect
                    tagErrors.add(new TagIncorrectValue_ReplayException(tag, expectedValue, actualValue));
                } else {
                    if(dict.isGroup(msgType,tag)) {
                        //Need to check groups
                        //First iterate through groups in Expected message to see if we can find match in the Actual message
                        List<Group> expectedGroups = expectedPart.getGroups(tag);
                        List<Group> actualGroups = actualPart.getGroups(tag);
                        List<StructuredTagList> msgTagsListGrp = msgTagsToReadAndStore.groups.containsKey(tag) ? msgTagsToReadAndStore.groups.get(tag) :  null;
                        int completeMatch = 0;
                        for(int i=0; i<expectedGroups.size(); i++) {
                            /*for(int j=0; j<actualGroups.size(); j++) {
                                ArrayList<TagError_ReplayException> errors = compareActualAndExpectedMessagesPart(expectedGroups.get(i), actualGroups.get(j), msgTagsToReadAndStore, dict, msgType);
                                if((errors == null) || (errors.size() == 0)) {
                                    completeMatch++;
                                    break;
                                }
                            }*/
                            //At the moment the requirement is to compare groups one by one expecting CFORE to keep the order of the groups
                            ArrayList<TagError_ReplayException> errors = compareActualAndExpectedMessagesPart(expectedGroups.get(i), actualGroups.get(i), (msgTagsListGrp!=null)&&(i<msgTagsListGrp.size()) ? msgTagsListGrp.get(i) : null, dict, msgType);
                            if((errors == null) || (errors.size() == 0)) {
                                completeMatch++;
                                //break;
                            }
                            else {
                                tagErrors.addAll(errors);
                            }
                        }
                        if(completeMatch < expectedGroups.size()) {
                            //Some groups where missing or included different tags/values
                            tagErrors.add(new TagIncorrectGroup_ReplayException(tag));
                        }
                    }
                }

            } else { // expected tag is missing
                // we ignore actual value validation then
                if((msgTagsToReadAndStore != null) && (msgTagsToReadAndStore.isSetField(tag))) {
                    expectedValue = "<read value>";
                }
                /*else if( _tagsWithIgnoreValueCheckList.contains(tag)) {
                    expectedValue = "<ignore value>";
                }*/

                tagErrors.add(new TagMissing_ReplayException(tag, expectedValue));
            }
        }

        // now find and report all unexpected tags
        // NB! we should NOT skip here ignored tags or tags marked for read values were already ignored when comparing to expected and now all tags are really unexpected
        Iterator<Field<?>> actualIter = actualPart.iterator();
        while (actualIter.hasNext()) {
            Field<?> actualField = actualIter.next();
            int tag = actualField.getTag();
            if(!expectedPart.isSetField(tag)) {
                // this is unexpected tag
                //String actualValue = actualMessage.getString(tag);
                String actualValue = actualField.getObject().toString();
                tagErrors.add(new TagUnexpected_ReplayException(tag, actualValue));
            }
        }
        if(tagErrors.size() == 0) {
            //return null; // no errors found!
            return tagErrors;
        }
        else {
            return tagErrors;
        }
    }

    private MessageIncorrect_ReplayException compareActualAndExpectedMessages(quickfix.Message actualMessage, quickfix.Message expectedMessage, StructuredTagList msgTagsToReadAndStore, DataDictionary dict) throws FieldNotFound, ParseException {
        ArrayList<TagError_ReplayException> tagErrors = new ArrayList<TagError_ReplayException>();
        // TODO: shouldn't we support checkign tags in hearder?
        tagErrors.addAll(compareActualAndExpectedMessagesPart(actualMessage, expectedMessage, msgTagsToReadAndStore, dict, expectedMessage.getHeader().getString(MsgType.FIELD)));
        //Now take care about the groups
        if(tagErrors.size() == 0) {
            return null; // no errors found!
        }

        return new MessageIncorrect_ReplayException(actualMessage,tagErrors);
    }

    private boolean compareTimeStamps(String actualValue, String expectedValue, long timeRangeCheck) throws ParseException {
        DateTimeFormatter dfExpected = getDateFormatter(expectedValue);
        DateTimeFormatter dfActual = getDateFormatter(actualValue);

        ZonedDateTime expectedDateTime = ZonedDateTime.parse(expectedValue, dfExpected);
        ZonedDateTime actualDateTime = ZonedDateTime.parse(actualValue, dfActual);
        long diffMilliseconds = Math.abs(expectedDateTime.toInstant().toEpochMilli() - actualDateTime.toInstant().toEpochMilli());
        long rangeMs = timeRangeCheck * 1000;
        if(diffMilliseconds > rangeMs) {
            return false;
        }
        return true;
    }


    // from found received message we read and store some tags
    private void readAndStoreTags(HashMap<String, String> tagValuesMap, FieldMap actualMessage, StructuredTagList msgTagsToReadAndStore) throws Exception {
        for(Integer key : msgTagsToReadAndStore.fields.keySet()) {
            int tag = key;

            if(actualMessage.isSetField(tag)) { // then we already sipped validation of this field but now should store in our hash map
                String origValue = msgTagsToReadAndStore.getString(tag);
                String actualValue = actualMessage.getString(tag);
                // now need to map readField.toString() to actualValue
                synchronized (tagValuesMap) { // DONE: open question - do I need to keep all tags at Replay level or at Scenario level. We decided to keep it at scenario level (however there are no evidence it is important - so we could keep it global as well)
                   if(! tagValuesMap.containsKey(origValue)) {
                       tagValuesMap.put(origValue, actualValue);
                   }
                   else {
                       String expectedValue = tagValuesMap.get(origValue);
                       if(expectedValue.equals(actualValue)) {
                           // Do nothing
                           // This is possible since CFORE can return the same generated value in more than one field (e.g. 37 and 11210 in the same message)
                       } else { // we should report error, but only if it is not TS with very small difference
                            boolean reportError = true;

                           if(_tagsWithDateTimeCheck.contains(tag) ||
                                   _tagsWithTimeInForceCheck.contains(tag)) {
                               //DateFormat dfExpected = getDateFormatter(expectedValue);
                               DateTimeFormatter dfActual = getDateFormatter(actualValue);
                               //if(dfExpected != dfActual) - this was already checked before during compare

                               if ((dfActual == secondsTimeFormatter) ||
                                       (dfActual == millisecondsTimeFormatter) ||
                                       (dfActual == microsecondsTimeFormatter) ||
                                       (dfActual == nanosecondsTimeFormatter)) { // only for ts with milliseconds - as so far we only saw such case
                                   if (compareTimeStamps(actualValue, expectedValue, _timeSmallRangeCheck)) {
                                       // everything is good - TS WITH SECONDS is in the range, we don't store this map
                                       reportError = false;
                                   }
                               }
                           }

                           if(reportError) {
                               // other tag inside this scenario's step affected us - don't really know what do to here.
                               throw new Exception(String.format("Unexpected error: generated tag '%d' first introduced on CHECK_RECEIVE action with original value='%s' (used as key), however after the message is received the map already contained value='%s' meanwhile value from message is different='%s'. Actual message=[%s]", tag, origValue, expectedValue, actualValue, actualMessage.toString()));
                           }
                       }
                   }
                }
            }
        }
        for(Integer key : msgTagsToReadAndStore.groups.keySet()) {
            if(actualMessage.hasGroup(key)) {
                List<Group> lst = actualMessage.getGroups(key);
                ArrayList<StructuredTagList> tagsLst = msgTagsToReadAndStore.groups.get(key);
                for(int i=0; (i<lst.size()) && (i<tagsLst.size()); i++) {
                    readAndStoreTags(tagValuesMap, lst.get(i), tagsLst.get(i));
                }
            }
        }

    }

    private static boolean compare(String str1, String str2) {
        return (str1== null ? str2 == null : str1.equals(str2));
    }

    // also used by FAST.GMA TODO: move to common FIX library
    public static ArrayList<quickfix.Message> findMessagesBy11and37(String tag11Value, String tag37Value, ArrayList<quickfix.Message> receivedMessages) throws FieldNotFound { // access to receivedMessages already synchronized
        // tag11Value can be null
        // tag37Value can be null
        // but both can't be null at the same time!!


        // DONE: ensure there are no receive messages w/o 11 and 37 at the same time - only logon, equence reset, heardbeats and other session level messages might not have 11 and 37 at the same time - we don't use them here - DONE- in all day prod data we have only login/logout, heardbeats, sequence reset etc messages w/o 11 & 37 but we don't have that messages here.
        if((tag11Value == null)&&(tag37Value == null)) {
            throw new RuntimeException("Can't find messages when both 11 and 37 tags are undefined");
        }

        ArrayList<quickfix.Message> foundMessages = new ArrayList<quickfix.Message>();
        for (quickfix.Message actualMessage : receivedMessages) {
            String actualTag11Value = null;
            if(tag11Value != null) {
                if(actualMessage.isSetField(ClOrdID.FIELD)) {
                    actualTag11Value = actualMessage.getString(ClOrdID.FIELD);
                }
            }

            String actualTag37Value = null;
            if(tag37Value != null) {
                if (actualMessage.isSetField(OrderID.FIELD)) {
                    actualTag37Value = actualMessage.getString(OrderID.FIELD);
                }
            }

            if(compare(tag11Value, actualTag11Value) && compare(tag37Value, actualTag37Value)) {
                foundMessages.add(actualMessage);
            }
        }

        if(foundMessages.size() == 0)
            return null;

        return foundMessages;
    }

    @Suspendable
    public quickfix.Message checkReceive(ReplayScenario replayScenario, quickfix.Message expectedMessage, LockedBufferOfReceivedMessages receivedMessages, StructuredTagList msgTagsToReadAndStore, int numTries, DataDictionary dict) throws Exception {

        // 1. find messages by tags 11 and 37
        // 2. in found messages check other tags
        // 3 if anything is incorrect - wait a littler and try again
        // 4. after many tries report error
        // possible errors: Missing message, Found N messages but with wrong tags: missing, unexpected, wrong value, wrong value not in range
        quickfix.Message result = null;

        String tag11Value = null;
        if(expectedMessage.isSetField(ClOrdID.FIELD)) { // shouldn't be empty TODO: check with Jason/Vijay as we found evidence this can be null!
            if (!msgTagsToReadAndStore.isSetField(ClOrdID.FIELD)) { // if we read it then ignore it for find, otherwise use it for find
                tag11Value = expectedMessage.getString(ClOrdID.FIELD);
            }
        }

        String tag37Value = null;
        if(expectedMessage.isSetField(OrderID.FIELD)) { // shouldn't be empty TODO: check with Jason/Vijay as we found evidence this can be null!
            if (!msgTagsToReadAndStore.isSetField(OrderID.FIELD)) { // if we read it then ignore it for find, otherwise use it for find
                tag37Value = expectedMessage.getString(OrderID.FIELD);
            }
        }

        if((tag11Value == null)&&(tag37Value == null)) {
            throw new RuntimeException(String.format("Invalid Scenario '%s'. Can't check receive message with both 11 and 37 tags marked for read", replayScenario.name));
        }

        Exception lastExeption = null;
        int toQuantifier = 1;
        for(int i = 0; i < numTries; i++) {
            receivedMessages.locker.lock(); // we lock buffer of specific connection that called us
            try {
                ArrayList<quickfix.Message> foundMessages = findMessagesBy11and37(tag11Value, tag37Value, receivedMessages.buffer);
                if(foundMessages != null) {
                    try {
                        result = compareFoundAndExpectedMessages(tag11Value, tag37Value, foundMessages, expectedMessage, msgTagsToReadAndStore, dict);
                        // if no exception then we found right message - need to read it's tags and remove it from buffer
                        // DONE: in case of difference in tags we still should  remove one message - the one that has less differences
                        break; // exit cycle and return this result;
                    }
                    catch (MessageIncorrect_ReplayException e) { // if incorrect message found then we try again
                        lastExeption = e;
                        result = e.actualMessage;
                    }
                }
            }
            finally {
                receivedMessages.locker.unlock();
            }

            toQuantifier = (result == null) ? (toQuantifier * 2) : 1;
            Timekeeping.sleep(1000 * toQuantifier);
        }


        // result here is found or the most closest message
        if(result != null) { // then we should remove message (found or the most close to expected) from the buffer
            readAndStoreTags(replayScenario.tagValuesMap, result, msgTagsToReadAndStore);

            receivedMessages.locker.lock(); // we lock buffer of specific connection that called us
            try {
                receivedMessages.buffer.remove(result); // delete received message from buffer so it is not read multiple times.
            }
            finally {
                receivedMessages.locker.unlock();
            }
        }

        if(lastExeption != null) {
            throw lastExeption; // incorrect message found
        }

        if(result != null) {
            return result; // we found message and already removed it from the buffer
        }


        // otherwise we even could not find any message by 11 & 37 - throw missing message
        throw new MessageMissing_ReplayException(tag11Value, tag37Value, expectedMessage);
    }

    public StructuredTagList processStepData(ReplayScenario replayScenario, Message msg, boolean send) throws Exception {

        return processStepDataPart(replayScenario, msg, msg, send, false); // return what tags should be read and stored during check receive
        // send is not using it
    }

    public StructuredTagList processStepDataPart(ReplayScenario replayScenario, Message origmsg, FieldMap msg, boolean send, boolean group) throws Exception {
        StructuredTagList msgTagsToReadAndStore = null; // later we will use this list to 1. ignore check against expected, 2. read and store actual values

        // NB! we do not ignore (remove) tags from expected msg here - we only collect tags that should be read

        // replayScenario.tagValuesMap comes from scenario level, access to it better be synchronized, however at scenario level there should be no conflicts really as no two steps of one scenario are executed in parallel

        if(!send)
            msgTagsToReadAndStore = new StructuredTagList(); // only for receive

        for(int tagClOrdID: _tagsWithClOrdIDCheck) {
            if (msg.isSetField(tagClOrdID)) {
                String origVal = msg.getString(tagClOrdID);
                String val = getOrGenerateClOrdId(replayScenario, origVal, send, origmsg);
                // if null we should read it from actual message
                if (val == null) {
                    msgTagsToReadAndStore.setString(tagClOrdID, origVal);
                } else {
                    msg.setString(tagClOrdID, val);
                }
            }
        }

        for(int tagOrderID: _tagsWithOrderIDCheck) {
            if (msg.isSetField(tagOrderID)) {
                String origVal = msg.getString(tagOrderID);
                String val = getOrGenerateOrderId(replayScenario, origVal, send);
                // if null we should read it from actual message
                if (val == null) {
                    msgTagsToReadAndStore.setString(tagOrderID, origVal);
                } else {
                    msg.setString(tagOrderID, val);
                }
            }
        }

        for(int tagDateTime: _tagsWithDateTimeCheck) {
            if (msg.isSetField(tagDateTime)) {
                String origVal = msg.getString(tagDateTime);
                String val = getOrGenerateDateTime(replayScenario, origVal, send); // keeps original time format
                // if null we should read it from actual message
                if (val == null) { // we not just read but also validate format of this field
                    msgTagsToReadAndStore.setString(tagDateTime, origVal);
                } else {
                    msg.setString(tagDateTime, val);
                }
            } // TODO: understand header fields vs body fields - don't we need to generalize it everywhere - in our comparison, in read tag value code? e.g. tag 52, 122 are inside header!
            else if ((!group) && (((Message)msg).getHeader().isSetField(tagDateTime))) { // some tags belong to header (122)
                String origVal = ((Message)msg).getHeader().getString(tagDateTime);
                String val = getOrGenerateDateTime(replayScenario, origVal, send); // keeps original time format
                // if null we should read it from actual message
                if (val == null) { // we not just read but also validate format of this field
                    msgTagsToReadAndStore.setString(tagDateTime, origVal);
                } else {
                    ((Message)msg).getHeader().setString(tagDateTime, val);
                }
            }
        }

        for(int tagTimeInFore: _tagsWithTimeInForceCheck) {
            if (msg.isSetField(tagTimeInFore)) {
                String origVal = msg.getString(tagTimeInFore);
                String val = getOrGenerateTimeInForce(replayScenario, origVal, send); // keeps original time format
                // if null we should read it from actual message
                if (val == null) { // we not just read but also validate format of this field
                    msgTagsToReadAndStore.setString(tagTimeInFore, origVal);
                } else {
                    msg.setString(tagTimeInFore, val);
                }
            }
        }
        Iterator<Integer> iter = msg.groupKeyIterator();
        ArrayList<StructuredTagList> arr = null;
        while(iter.hasNext()) {
            Integer id = iter.next();
            for(Group grp : msg.getGroups(id)) {
                StructuredTagList newPart = processStepDataPart(replayScenario, origmsg, grp, send, true);
                if((!send) && (newPart != null)) {
                    if(arr == null) arr = new ArrayList<>();
                    arr.add(newPart);
                }
            }
            if((arr != null) && (arr.size() != 0)) {
                msgTagsToReadAndStore.groups.put(id, arr);
            }
        }
        return msgTagsToReadAndStore;
    }

    private String replayTs = "replay" + LocalDateTime.now().format(genIdTimeFormatter);

    //private int _tagGeneratedId = 0; // this was moved to Scenario level

    private String getOrGenerateClOrdId(ReplayScenario replayScenario, String id, boolean send, Message msg) throws Exception {
        String result;

        // ClOrdId can contain suffix - e.g. "164678958:IDBIVO" in this case we should extract it and reuse it, howeber ignore for our hash map
        String pureId = id;
        String suffix = "";
        String name = msg.isSetField(8001) ? msg.getString(8001) : replayScenario.connName;
        int pos = id.lastIndexOf(":");
        if ((pos > 0) &&(pureId.length() > (pos + 1))) {
            String enrichment = id.substring(pos + 1);
            pureId = id.substring(0, pos); // pos is not included
            if(enrichment.contains(name)) {
                String juliaday = enrichment.replace(name,"");
                if(juliaday.length()>0) {
                    try {
                        int day = Integer.parseInt(juliaday);
                        Date now = new Date();
                        suffix = ":" + String.valueOf(LocalDate.now(_C4ZoneId).getDayOfYear());
                    } catch (NumberFormatException e) {
                        //Should never come here!
                        throw new Exception("Unexpected value as an enrichment suffix!");
                    }
                }
                else {
                    suffix = ":";
                }
                suffix += name; // starting from pos (":")
            }
            else {
                //We cannot recognize this suffix, let's attempt to attach it back
                pureId = id;
                suffix = "";
            }
        }

        synchronized (replayScenario.tagValuesMap) { // this comes from Scenario level
            if (replayScenario.tagValuesMap.containsKey(pureId)) {
                result = replayScenario.tagValuesMap.get(pureId) + suffix;
            } else {
                if (send) {
                    replayScenario.tagGeneratedId += 1;
                    int orderId = replayScenario.tagGeneratedId;
                    result = "C" + Integer.toString(orderId) + "/" + replayScenario.name + "/" + replayTs; // + "/" + id;
                    replayScenario.tagValuesMap.put(pureId, result);
                } else { // NB! sometimes we can check receive new 11 - e.g. CFORE generate cancel request to downstream. Our
                    result = null;
                    // throw new Exception(String.format("Cannot check receive with tag '11'='%s' because it was not defined before by us", id)); THIS IS POSSIBLE ACTUALLY!
                }

            }
        }

        return result;
    }

    private String extractScenIdFromClOrdId(String id) {
        String result = null;
        if (id.matches("C\\d+/.+/" + replayTs + "(:.*)?$")) {
            try {
                result = id.split("/")[1];
            }
            catch (Exception e) {
                result = null;
            }
        }
        return result;
    }

    private String getOrGenerateOrderId(ReplayScenario replayScenario, String id, boolean send) {
        String result;
        synchronized (replayScenario.tagValuesMap) { // this comes from Scenario level
            if (replayScenario.tagValuesMap.containsKey(id)) {
                result = replayScenario.tagValuesMap.get(id);
            } else {
                if (send) { // we generate it
                    replayScenario.tagGeneratedId += 1;
                    int orderId = replayScenario.tagGeneratedId;
                    result = "S" + Integer.toString(orderId) + "/" + replayScenario.name + "/" + replayTs; // + "/" + id;
                    replayScenario.tagValuesMap.put(id, result);
                } else { // during check we should ignore it, get it and store it for future use
                    result = null;
                }

            }
        }

        return result;
    }

    private String extractScenIdFromOrderId(String id) {
        String result = null;
        if (id.matches("S\\d+/.+/" + replayTs + "$")) {
            try {
                result = id.split("/")[1];
            }
            catch (Exception e) {
                result = null;
            }
        }
        return result;
    }

    public String extractScenarioId(quickfix.Message msg) {
        String result = null;
        String tag11Value = null;
        try {
            if (msg.isSetField(ClOrdID.FIELD)) {
                tag11Value = msg.getString(ClOrdID.FIELD);
            }
            result = this.extractScenIdFromClOrdId(tag11Value);
            if (result != null) return result;
            String tag37Value = null;
            if (msg.isSetField(OrderID.FIELD)) {
                tag11Value = msg.getString(OrderID.FIELD);
            }
            result = this.extractScenIdFromOrderId(tag11Value);
            return result;
        }
        catch (Exception e) {
            return null;
        }
    }

    private static final DateTimeFormatter genIdTimeFormatter;
    public static final DateTimeFormatter nanosecondsTimeFormatter;
    public static final DateTimeFormatter microsecondsTimeFormatter;
    public static final DateTimeFormatter millisecondsTimeFormatter;
    public static final DateTimeFormatter secondsTimeFormatter;
    public static final DateTimeFormatter dateFormatter;
    static {
        genIdTimeFormatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss").withZone(ZoneId.of("UTC"));
        nanosecondsTimeFormatter = DateTimeFormatter.ofPattern("yyyyMMdd-HH:mm:ss.SSSSSSSSS").withZone(ZoneId.of("UTC"));
        microsecondsTimeFormatter = DateTimeFormatter.ofPattern("yyyyMMdd-HH:mm:ss.SSSSSS").withZone(ZoneId.of("UTC"));
        millisecondsTimeFormatter = DateTimeFormatter.ofPattern("yyyyMMdd-HH:mm:ss.SSS").withZone(ZoneId.of("UTC"));
        secondsTimeFormatter = DateTimeFormatter.ofPattern("yyyyMMdd-HH:mm:ss").withZone(ZoneId.of("UTC"));
        dateFormatter = DateTimeFormatter.ofPattern("yyyyMMdd").withZone(ZoneId.of("UTC"));
    }

    private DateTimeFormatter getTifFormatter(String tsStr) {
        DateTimeFormatter result = null;
        if (tsStr.length() == 17) { // this is Date and Time with seconds only
            result = secondsTimeFormatter;
        }
        else if (tsStr.length() == 21) { // this is Date and Time with milliseconds
            result = millisecondsTimeFormatter;
        }
        else if (tsStr.length() == 24) { // this is Date and Time with milliseconds
            result = microsecondsTimeFormatter;
        }
        else if (tsStr.length() == 27) { // this is Date and Time with milliseconds
            result = nanosecondsTimeFormatter;
        }
        else {
            throw new RuntimeException(String.format("Incorrect TimeInForce format '%s'", tsStr));
        }
        return result;
    }

    private DateTimeFormatter getDateFormatter(String tsStr) {
        DateTimeFormatter result = null;
        if(tsStr.length() == 8) { // this is Date only
            result = dateFormatter;
        }
        else if (tsStr.length() == 17) { // this is Date and Time with seconds only
            result = secondsTimeFormatter;
        }
        else if (tsStr.length() == 21) { // this is Date and Time with milliseconds
            result = millisecondsTimeFormatter;
        }
        else if (tsStr.length() == 24) { // this is Date and Time with milliseconds
            result = microsecondsTimeFormatter;
        }
        else if (tsStr.length() == 27) { // this is Date and Time with milliseconds
            result = nanosecondsTimeFormatter;
        }
        else {
            throw new RuntimeException(String.format("Incorrect TimeStamp format '%s'", tsStr));
        }
        return result;
    }


    private String getOrGenerateTimeInForce(ReplayScenario replayScenario, String tsStr, boolean send) throws Exception {
        String result;

        // Time can be in different formats: with milliseconds or with seconds only! need to consider it here
        synchronized (replayScenario.tagValuesMap) { // this comes from Scenario level
            if (replayScenario.tagValuesMap.containsKey(tsStr)) {
                result = replayScenario.tagValuesMap.get(tsStr);
            } else {
                if(send) {
                    DateTimeFormatter df = getTifFormatter(tsStr);
                    if(df != null) {
                        ZonedDateTime origDateTime = ZonedDateTime.parse(tsStr, df);
                        // DONE: consider that startDateTime (like all TS) and origDateTime (126), like 52, 60 are all in UTC; however actualStartDateTime is in local
                        long delay = origDateTime.toInstant().toEpochMilli() - replayScenario.replayManager.startDateTime.getTime(); // utc delta
                        if(replayScenario.replayManager.speed > 0.0f) {
                            delay = (long) ((float) delay / replayScenario.replayManager.speed);
                        }
                        else {
                            delay = 1_000L * 5; // 5 seconds before expire
                        }
                        long newTime = delay + replayScenario.replayManager.actualStartDateTime.getTime();
                        LocalDateTime newDateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(newTime), ZoneId.of("UTC"));
                        result = newDateTime.format(df); // need to convert to UTC
                        _logger.debug(String.format("Generated TIF: origDateTime='%s', delay=%d, newDateTime=%s", tsStr, delay, result));
                    }
                    else {
                        throw new Exception(String.format("Unexpected time in force format in original message: '%s'", tsStr));
                    }

                    replayScenario.tagValuesMap.put(tsStr, result);
                }
                else {// during check we should ignore it, get it and store it for future use
                    result = null;
                }
            }
        }
        return result;
    }

    private String getOrGenerateDateTime(ReplayScenario replayScenario, String tsStr, boolean send) throws Exception {
        String result;

        // Time can be in different formats: with milliseconds, with seconds only, or even date only - we need to consider it here!

        synchronized (replayScenario.tagValuesMap) { // this comes from Scenario level
            if (replayScenario.tagValuesMap.containsKey(tsStr)) {
                result = replayScenario.tagValuesMap.get(tsStr);
            } else {
                if(send) {
                    ZonedDateTime ts = ZonedDateTime.now(ZoneId.of("UTC")); // TODO: we probably need to update TS smarter: Replay.Start + (Step.TS - Replay.RecordedStart) / speed
                    DateTimeFormatter df = getDateFormatter(tsStr);
                    if(df != null) {
                        result = ts.format(df);
                    }
                    else {
                        throw new Exception(String.format("Unexpected date time format in original message: '%s'", tsStr));
                    }

                    replayScenario.tagValuesMap.put(tsStr, result);
                }
                else {// during check we should ignore it, get it and store it for future use
                    result = null;
                }
            }
        }
        return result;
    }

    @Suspendable
    public ArrayList<Message> cleanUpScenarioByFindingItsMessages(Message expectedMessage, LockedBufferOfReceivedMessages receivedMessages, StructuredTagList msgTagsToReadAndStore) throws FieldNotFound {
        String tag11Value = null;
        if(expectedMessage.isSetField(ClOrdID.FIELD)) {
            tag11Value = expectedMessage.getString(ClOrdID.FIELD); // already has correct suffix
        }
        if(msgTagsToReadAndStore.isSetField(ClOrdID.FIELD)) { // if we read it then ignore it for find
            tag11Value = null;
        }
        String tag37Value = null;
        if(expectedMessage.isSetField(OrderID.FIELD)) {
            tag37Value = expectedMessage.getString(OrderID.FIELD); // can't be empty
        }
        if(msgTagsToReadAndStore.isSetField(OrderID.FIELD)) { // if we read it then ignore it for find
            tag37Value = null;
        }

        if((tag11Value == null)&&(tag37Value == null)) {
            // this may happen if we didn't receive 37 yet - because of errors in tags, and next steps generated and started using new 11
            // in this case we couldn't assign unexpected messages to scenario - such messages will be globally unexpected
            _logger.debug("Can't cleanup scenario by finding it's messages using both 11 and 37 tags marked for read");
            //throw new RuntimeException("Can't cleanup scenario by finding it's messages using both 11 and 37 tags marked for read");
            return null;
        }

        ArrayList<quickfix.Message> foundMessages = null;

        receivedMessages.locker.lock(); // we lock buffer of specific connection that called us
        try {
            foundMessages = findMessagesBy11and37(tag11Value, tag37Value, receivedMessages.buffer); // null means not found
            if(foundMessages != null) {
                for (quickfix.Message msg : foundMessages) {
                    receivedMessages.buffer.remove(msg); // delete received message from buffer so it is not read multiple times.
                }
            }
        }
        finally {
            receivedMessages.locker.unlock();
        }



        return foundMessages;
    }
}
