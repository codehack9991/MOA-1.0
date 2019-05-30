package fast.common.replay;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;

public class MessageIncorrect_ReplayException extends ReplayException {
    public quickfix.Message actualMessage;
    public ArrayList<TagError_ReplayException> tagErrors;
    public MessageIncorrect_ReplayException(quickfix.Message actualMessage, ArrayList<TagError_ReplayException> tagErrors) {
        this.actualMessage = actualMessage;
        this.tagErrors = tagErrors;
    }

    public HashSet<String> getReportTags() {
        HashSet<String> set = new HashSet<String>();
        for(TagError_ReplayException tagError: tagErrors) {
            HashSet<String> subset = tagError.getReportTags();
            Iterator iter = subset.iterator();
            while(iter.hasNext()) {
                String reportSubTag = (String)iter.next();
                set.add(reportSubTag); // KT: removed (String.format("Incorrect message: %s", reportSubTag) - in order to make reports more readable
            }
        }

        return set;
    }


    public String getMessage() {
        StringBuilder sb = new StringBuilder();

        String tag11Value = null;
        String tag37Value = null;
        if(actualMessage.isSetField(11)) {
            try {
                tag11Value = actualMessage.getString(11);
            }
            catch (Exception e) {
                tag11Value = null;
            }
        }
        if(actualMessage.isSetField(37)) {
            try {
                tag37Value = actualMessage.getString(37);
            }
            catch (Exception e) {
                tag37Value = null;
            }
        }
        // 37 can be empty! CFORE CAN sent message w/o 37 - it is called "pass thru flow" and sometimes it might be incorrect

        sb.append(String.format("Incorrect message - Identity:[%s] Body:[%s]\r\n",
                getFilterString(tag11Value, tag37Value), actualMessage.toString()));


        for(TagError_ReplayException tagError: tagErrors) {
            sb.append(tagError.getMessage());
            sb.append("\r\n");
        }
        return sb.toString();
    }

	public ArrayList<TagError_ReplayException> getTagErrors() {
		return tagErrors;
	}
}
