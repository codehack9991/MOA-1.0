package fast.common.fix;

import fast.common.replay.TagError_ReplayException;

import java.util.HashSet;

public class TagMissing extends TagError_ReplayException {
    public String expectedValue;
    public String tagName;
    public String expectedValueName;

    public TagMissing(int tag, String expectedValue, String tagName, String expectedValueName) {
        super(tag);
        this.expectedValue = expectedValue;
        this.tagName = tagName;
        this.expectedValueName = expectedValueName;
    }

    public HashSet<String> getReportTags() {
        HashSet<String> set = new HashSet<String>();
        set.add(String.format("Tag '%d' is missing", tag));
        return set;
    }

    public String getMessage() {
        String err_tag_name = "'" + tag + "'";
        if(tagName != null) {
            err_tag_name = tagName + " (" + tag + ")";
        }

        String err_expected_value = "'" + expectedValue + "'";
        if(expectedValueName != null) {
            err_expected_value = expectedValueName + " (" + expectedValue + ")";
        }

        return String.format("Tag %s is missing: expected value: %s", err_tag_name, err_expected_value);
    }
}
