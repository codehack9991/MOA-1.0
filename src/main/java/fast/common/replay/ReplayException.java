package fast.common.replay;

import quickfix.FieldNotFound;
import quickfix.Message;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;

// step reference is not stored here but added into report
public abstract class ReplayException extends RuntimeException {
    /*public Date errorTs;
    public ReplayException() {
        errorTs = new Date();
    }
*/ //  KT: not needed - we will save ts during writeReport - it is better because it will work for PASSED and FAILED steps and for any type of exception

    public String getFilterString(String tag11Value, String tag37Value) {
        if(tag11Value == null) {
            if(tag37Value == null) {
                return "";
            }
            else {
                return "37=" + tag37Value + "";
            }
        }
        else {
            if(tag37Value == null) {
                return "11=" + tag11Value + "";
            }
            else {
                return "11=" + tag11Value + "|37=" + tag37Value;
            }
        }
    }

    protected abstract HashSet<String> getReportTags();
}


/*
class MessagesIncorrect_ReplayException extends ReplayException {
    public quickfix.Message expectedMessage;
    public ArrayList<MessageIncorrect_ReplayException> actualMessagesErrors;
    String tag11Value;
    String tag37Value;

    public MessagesIncorrect_ReplayException(String tag11Value, String tag37Value, quickfix.Message expectedMessage, ArrayList<MessageIncorrect_ReplayException> actualMessagesErrors) {
        this.tag11Value = tag11Value;
        this.tag37Value = tag37Value;
        this.expectedMessage = expectedMessage;
        this.actualMessagesErrors = actualMessagesErrors;
    }

    public String getMessage() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("Matching messages found with errors.\r\nExpected message - Identity:[%s] Body:[%s]\r\n",
                getFilterString(tag11Value, tag37Value), expectedMessage.toString()));
        sb.append(String.format(String.format("Found %d matching messages with errors\r\n", actualMessagesErrors.size())));

        for(MessageIncorrect_ReplayException error: actualMessagesErrors) {
            sb.append(error.getMessage());
        }

        return sb.toString();
    }
}
*/
//           errorMsg = String.format("Failed to check received message. After waiting %ds expected message [%s] still was not found among %d received messages. Last received message caused exception: %s",


enum RangeCheckError {CANT_PARSE_VALUES, ZERO_EXPECTED, ACTUAL_NOT_IN_RANGE}
class TagRangeCheckError_ReplayException extends TagError_ReplayException {
    public String expectedValue;
    public String actualValue;
    public float range;
    public RangeCheckError error;


    public HashSet<String> getReportTags() {
        HashSet<String> set = new HashSet<String>();
        set.add(String.format("Tag '%d' range check error", tag));
        return set;
    }


    private int getRangePercent() {
        return (int)(range * 100.0);
    }
    public TagRangeCheckError_ReplayException(int tag, String expectedValue, String actualValue, float range, RangeCheckError error) {
        super(tag);
        this.expectedValue = expectedValue;
        this.actualValue = actualValue;
        this.range = range;
        this.error = error;
    }

    public String getMessage() {
        if(error == RangeCheckError.CANT_PARSE_VALUES) {
            return String.format("Tag '%d' range check error: can't parse values - expected value: '%s', actual value: '%s', range: %d%%", tag, expectedValue, actualValue, getRangePercent());
        }
        else if(error == RangeCheckError.ZERO_EXPECTED) {
            return String.format("Tag '%d' range check error: can't check done because expected value is zero: '%s', actual value: '%s', range: %d%%", tag, expectedValue, actualValue, getRangePercent());
        }
        else if(error == RangeCheckError.ACTUAL_NOT_IN_RANGE) {
            return String.format("Tag '%d' range check error: expected value: '%s', actual value: '%s', range: %d%%", tag, expectedValue, actualValue, getRangePercent());
        }
        return String.format("Tag '%d' range check error: expected value: '%s', actual value: '%s', range: %d%, error: '%s'", tag, expectedValue, actualValue, getRangePercent(), error.toString());
    }
}

class TagDateTimeRangeCheckError_ReplayException extends TagError_ReplayException {
    public String expectedValue;
    public String actualValue;
    public long range; // seconds
    public TagDateTimeRangeCheckError_ReplayException(int tag, String expectedValue, String actualValue, long range) {
        super(tag);
        this.expectedValue = expectedValue;
        this.actualValue = actualValue;
        this.range = range;
    }

    public HashSet<String> getReportTags() {
        HashSet<String> set = new HashSet<String>();
        set.add(String.format("Tag '%d' time range check error", tag));
        return set;
    }

    public String getMessage() {
        return String.format("Tag '%d' time range check error: expected value: '%s', actual value: '%s', range: %ds", tag, expectedValue, actualValue, range);
    }
}


class TagIncorrectGroup_ReplayException extends TagError_ReplayException {
    public TagIncorrectGroup_ReplayException(int tag) {
        super(tag);
    }

    public HashSet<String> getReportTags() {
        HashSet<String> set = new HashSet<String>();
        set.add(String.format("Tag '%d' groups do not match", tag));
        return set;
    }

    public String getMessage() {
        return String.format("Tag '%d' groups do not match expected values", tag);
    }
}

class TagUnexpected_ReplayException extends TagError_ReplayException {
    public String actualValue;
    public TagUnexpected_ReplayException(int tag, String actualValue) {
        super(tag);
        this.actualValue = actualValue;
    }

    public HashSet<String> getReportTags() {
        HashSet<String> set = new HashSet<String>();
        set.add(String.format("Tag '%d' is unexpected", tag));
        return set;
    }

    public String getMessage() {
        return String.format("Tag '%d' is unexpected: actual value: '%s'", tag, actualValue);
    }
}

class TagWrongKeyValueSet_ReplayException extends TagError_ReplayException {
    public ArrayList<KeyValueError_ReplayException> keyValueErrors;
    public TagWrongKeyValueSet_ReplayException(int tag, ArrayList<KeyValueError_ReplayException> keyValueErrors) {
        super(tag);
        this.keyValueErrors = keyValueErrors;
    }

    public HashSet<String> getReportTags() {
        HashSet<String> set = new HashSet<String>();
        for(KeyValueError_ReplayException keyValueError: keyValueErrors) {
            HashSet<String> subset = keyValueError.getReportTags();
            Iterator iter = subset.iterator();
            while(iter.hasNext()) {
                String reportSubTag = (String)iter.next();
                set.add(String.format("Tag '%d' %s", tag, reportSubTag));
            }
        }

        return set;
    }

    public String getMessage() {
        StringBuilder sb = new StringBuilder();

        for(KeyValueError_ReplayException keyValueError: keyValueErrors) {
            sb.append(String.format("Tag '%d' ", tag));
            sb.append(keyValueError.getMessage());
            sb.append("\r\n");
        }
        return sb.toString();
    }
}

abstract class KeyValueError_ReplayException extends ReplayException {
    public String key;
    public KeyValueError_ReplayException(String key) {
        this.key = key;
    }
}

class KeyValueMissing_ReplayException extends KeyValueError_ReplayException {
    public String valueExpected;
    public KeyValueMissing_ReplayException(String key, String valueExpected) {
        super(key);
        this.valueExpected = valueExpected;
    }

    public HashSet<String> getReportTags() {
        HashSet<String> set = new HashSet<String>();
        set.add(String.format("profile '%s' is missing", key));
        return set;
    }

    public String getMessage() {
        return String.format("profile '%s' is missing: expected value: '%s'", key, valueExpected);
    }
}


class KeyValueUnexpected_ReplayException extends KeyValueError_ReplayException {
    public String valueActual;
    public KeyValueUnexpected_ReplayException(String key, String valueActual) {
        super(key);
        this.valueActual = valueActual;
    }

    public HashSet<String> getReportTags() {
        HashSet<String> set = new HashSet<String>();
        set.add(String.format("profile '%s' is unexpected", key));
        return set;
    }

    public String getMessage() {
        return String.format("profile '%s' is unexpected: actual value: '%s'", key, valueActual);
    }
}

class KeyValueIncorrectValue_ReplayException extends KeyValueError_ReplayException {
    public String valueExpected;
    public String valueActual;
    public KeyValueIncorrectValue_ReplayException(String key, String valueExpected, String valueActual) {
        super(key);
        this.valueExpected = valueExpected;
        this.valueActual = valueActual;
    }


    public HashSet<String> getReportTags() {
        HashSet<String> set = new HashSet<String>();
        set.add(String.format("profile '%s' has incorrect value", key));
        return set;
    }

    public String getMessage() {
        return String.format("profile '%s' has incorrect value: expected value '%s' vs actual value '%s'", key, valueExpected, valueActual);
    }
}


class MessagesUnexpected_ReplayException extends ReplayException {
    public ArrayList<quickfix.Message> unexpectedMessages;

    public MessagesUnexpected_ReplayException(ArrayList<quickfix.Message> unexpectedMessages) {
        this.unexpectedMessages = unexpectedMessages;
    }

    public HashSet<String> getReportTags() {
        HashSet<String> set = new HashSet<String>();
        for(quickfix.Message msg: unexpectedMessages) {
            try {
                String tag58 = msg.getString(58);
                set.add(String.format("Unexpected message: 58=%s", tag58));
            }
            catch (FieldNotFound fieldNotFound) {
                set.add("Unexpected message");
            }
        }


        return set;
    }

    public String getMessage() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("Found %d unexpected messages.\r\n", unexpectedMessages.size()));

        for(Message msg: unexpectedMessages) {
            String tag11Value = null;
            String tag37Value = null;
            if(msg.isSetField(11)) {
                try {
                    tag11Value = msg.getString(11);
                }
                catch (Exception e) {
                    tag11Value = null;
                }
            }
            if(msg.isSetField(37)) {
                try {
                    tag37Value = msg.getString(37);
                }
                catch (Exception e) {
                    tag37Value = null;
                }
            }
            sb.append(String.format("Unexpected message - Identity:[%s] Body:[%s]\r\n",
                    getFilterString(tag11Value, tag37Value), msg.toString()));
        }

        return sb.toString();
    }
}

class MessagesOutOfScope_ReplayException extends MessagesUnexpected_ReplayException {
    public String connection;
    public String side;

    public MessagesOutOfScope_ReplayException(String connection, String side, ArrayList<Message> outOfScopeMessages) {
        super(outOfScopeMessages);
        this.connection = connection;
        this.side = side;
    }

    public HashSet<String> getReportTags() {
        HashSet<String> set = new HashSet<String>();
        for(quickfix.Message msg: unexpectedMessages) {
            try {
                String tag58 = msg.getString(58);
                set.add(String.format("Unexpected message: 58=%s", tag58));
            }
            catch (FieldNotFound fieldNotFound) {
                set.add("Unexpected message");
            }
        }

        return set;
    }

    public String getMessage() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("Found %d unexpected messages.\r\n", unexpectedMessages.size()));

        for(Message msg: unexpectedMessages) {
            String tag11Value = null;
            String tag37Value = null;
            if(msg.isSetField(11)) {
                try {
                    tag11Value = msg.getString(11);
                }
                catch (Exception e) {
                    tag11Value = null;
                }
            }
            if(msg.isSetField(37)) {
                try {
                    tag37Value = msg.getString(37);
                }
                catch (Exception e) {
                    tag37Value = null;
                }
            }
            sb.append(String.format("Unexpected message - [%s:%s] - Identity:[%s] Body:[%s]\r\n", this.side, this.connection,
                    getFilterString(tag11Value, tag37Value), msg.toString()));
        }

        return sb.toString();    }
}


class CheckDisconnectedFailed_ReplayException extends ReplayException {
    String errorMsg;

    public CheckDisconnectedFailed_ReplayException(String errorMsg) {
        this.errorMsg = errorMsg;
    }

    public HashSet<String> getReportTags() {
        HashSet<String> set = new HashSet<String>();
        set.add("Check disconnected failed");
        return set;
    }

    public String getMessage() {
        return errorMsg;
    }
}

class SendWaitTimeout_ReplayException extends ReplayException {
    String errorMsg;

    public SendWaitTimeout_ReplayException(String errorMsg) { this.errorMsg = errorMsg; }

    @Override
    public HashSet<String> getReportTags() {
        HashSet<String> set = new HashSet<String>();
        set.add("Send wait timed out");
        return set;
    }
}




