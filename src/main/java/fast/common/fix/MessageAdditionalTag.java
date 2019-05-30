package fast.common.fix;

import java.util.HashSet;

import fast.common.replay.TagError_ReplayException;

public class MessageAdditionalTag extends TagError_ReplayException {
	
	public MessageAdditionalTag(int tag) {
		super(tag);
	}

	@Override
	protected HashSet<String> getReportTags() {
        HashSet<String> set = new HashSet<String>();
        set.add(String.format("Tag '%d' is additional", tag));
        return set;
	}
	
    public String getMessage() {
        String err_tag_name = "'" + tag + "'";

        return String.format("Tag %s is additional tag in actual message", err_tag_name);
    }

}
