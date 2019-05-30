package fast.common.fix;

import fast.common.replay.TagError_ReplayException;

import java.util.HashSet;

public class TagIncorrectGroupSize extends TagError_ReplayException {
    public String tagName;
    public int expectedGroupSize;
    public int actualGroupSize;



    public TagIncorrectGroupSize(int tag, String tagName, int expectedGroupSize, int actualGroupSize) {
        super(tag);
        this.tagName = tagName;
        this.expectedGroupSize = expectedGroupSize;
        this.actualGroupSize = actualGroupSize;
    }

    public String getMessage() {
        String err_tag_name = "'" + tag + "'";
        if(tagName != null) {
            err_tag_name = tagName + " (" + tag + ")";
        }


        return String.format("Tag %s has incorrect group size: expected size = %d vs actual size = %d", err_tag_name, expectedGroupSize, actualGroupSize);
    }

    @Override
    protected HashSet<String> getReportTags() { // TODO: rewrite all error messages - separate replay from non-replay!
        HashSet<String> set = new HashSet<String>();
        set.add(String.format("Tag '%d' has incorrect group size", tag));
        return set;
    }
}
