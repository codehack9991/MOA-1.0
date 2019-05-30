package fast.common.replay;

import java.util.HashSet;

public class TagIncorrectValue_ReplayException extends TagError_ReplayException {
    public String expectedValue;
    public String actualValue;
    public TagIncorrectValue_ReplayException(int tag, String expectedValue, String actualValue) {
        super(tag);
        this.expectedValue = expectedValue;
        this.actualValue = actualValue;
    }

    public HashSet<String> getReportTags() {
        HashSet<String> set = new HashSet<String>();
        set.add(String.format("Tag '%d' has incorrect value", tag));
        return set;
    }

    public String getMessage() {
        return String.format("Tag '%d' has incorrect value: expected value '%s' vs actual value '%s'", tag, expectedValue, actualValue);
    }
}
