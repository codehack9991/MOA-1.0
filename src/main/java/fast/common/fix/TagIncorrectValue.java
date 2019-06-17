package fast.common.fix;

import fast.common.replay.TagError_ReplayException;
import fast.common.replay.TagIncorrectValue_ReplayException;
import java.util.HashSet;

/**
 */
public class TagIncorrectValue extends TagIncorrectValue_ReplayException {
    public String tagName;
    public String expectedValueName;
    public String actualValueName;

    public TagIncorrectValue(int tag, String expectedValue, String actualValue) {
        super(tag, expectedValue, actualValue);
    }

    public TagIncorrectValue(int tag, String expectedValue, String actualValue, String tagName, String expectedValueName, String actualValueName) {
        super(tag, expectedValue, actualValue);
        this.tagName = tagName;
        this.expectedValueName = expectedValueName;
        this.actualValueName = actualValueName;
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

        String err_actual_value = "'" + actualValue + "'";
        if(actualValueName != null) {
            err_actual_value = actualValueName + " (" + actualValue + ")";
        }

        return String.format("Tag %s has incorrect value: expected value %s vs actual value %s", err_tag_name, err_expected_value, err_actual_value);
    }
}


