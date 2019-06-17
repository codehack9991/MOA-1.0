package fast.common.fix;

import fast.common.replay.TagError_ReplayException;
import fast.common.replay.TagIncorrectFormat_ReplayException;

import java.util.HashSet;

/**
 
 */
public class TagIncorrectFormat extends TagError_ReplayException {
    public String expectedFormat;
    public String actualValue;
    public String tagName;
    public String actualValueName; // nullable


    public HashSet<String> getReportTags() {
        HashSet<String> set = new HashSet<String>();
        set.add(String.format("Tag '%d' has incorrect format", tag));
        return set;
    }

    public TagIncorrectFormat(int tag, String expectedFormat, String actualValue, String tagName, String actualValueName) {
        super(tag);
        this.expectedFormat = expectedFormat;
        this.actualValue = actualValue;
        this.tagName = tagName;
        this.actualValueName = actualValueName;
    }

    public String getMessage() {
        String err_tag_name = "'" + tag + "'";
        if(tagName != null) {
            err_tag_name = tagName + " (" + tag + ")";
        }

        String err_actual_value = "'" + actualValue + "'";
        if(actualValueName != null) {
            err_actual_value = actualValueName + " (" + actualValue + ")";
        }

        return String.format("Tag %s has incorrect format: expected format '%s' vs actual value %s", err_tag_name, expectedFormat, err_actual_value);
    }
}
