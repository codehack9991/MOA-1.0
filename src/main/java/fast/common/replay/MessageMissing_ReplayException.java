package fast.common.replay;

import java.util.HashSet;

// logged to report
public class MessageMissing_ReplayException extends ReplayException {
    public quickfix.Message expectedMessage;
    public String tag11Value;
    public String tag37Value;
    public MessageMissing_ReplayException(String tag11Value, String tag37Value, quickfix.Message expectedMessage) {
        this.tag11Value = tag11Value;
        this.tag37Value = tag37Value;
        this.expectedMessage = expectedMessage;
    }


    public HashSet<String> getReportTags() {
        HashSet<String> set = new HashSet<String>();
        set.add("Missing message");
        return set;
    }


    public String getMessage() {
        return String.format("Missing message - Identity:[%s] Body:[%s]\r\n",
                getFilterString(tag11Value, tag37Value), expectedMessage.toString());
    }
}
