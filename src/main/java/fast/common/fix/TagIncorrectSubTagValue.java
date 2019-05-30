package fast.common.fix;

import fast.common.replay.TagError_ReplayException;
import fast.common.replay.TagIncorrectValue_ReplayException;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * Created by kt46743 on 8/9/2017.
 */
public class TagIncorrectSubTagValue extends TagIncorrectValue_ReplayException {
    private List<TagError_ReplayException> _tagErrorList;

    public TagIncorrectSubTagValue(int tag, List<TagError_ReplayException> tagErrorList) {
        super(tag, null, null);
        _tagErrorList = tagErrorList;
    }

    public String getMessage() {
    	String tagErrors = "";
    	if(_tagErrorList != null) {
    		for(TagError_ReplayException error:_tagErrorList) {
    			tagErrors += error.getMessage() + "\n";
    		}
    	}
        return String.format("Tag %s has incorrect sub tag values:\n %s", tag, tagErrors);
    }
}


