package fast.common.replay;

import java.util.HashSet;

public class TagIncorrectFormat_ReplayException extends TagError_ReplayException {
    public String expectedFormat;
    public String actualFormat;
    public TagIncorrectFormat_ReplayException(int tag, String expectedFormat, String actualFormat) {
        super(tag);
        this.expectedFormat = expectedFormat;
        this.actualFormat = actualFormat;
    }

    public HashSet<String> getReportTags() {
        HashSet<String> set = new HashSet<String>();
        set.add(String.format("Tag '%d' has incorrect format", tag));
        return set;
    }

    public String getMessage() {
        return String.format("Tag '%d' has incorrect format: expected format '%s' vs actual format '%s'", tag, expectedFormat, actualFormat);
    }
}
