package fast.common.replay;

import java.util.HashSet;

public class TagMissing_ReplayException extends TagError_ReplayException {
    public String expectedValue;
    public TagMissing_ReplayException(int tag, String expectedValue) {
        super(tag);
        this.expectedValue = expectedValue;
    }

    public HashSet<String> getReportTags() {
        HashSet<String> set = new HashSet<String>();
        set.add(String.format("Tag '%d' is missing", tag));
        return set;
    }

    public String getMessage() {
        return String.format("Tag '%d' is missing: expected value: '%s'", tag, expectedValue);
    }
}
