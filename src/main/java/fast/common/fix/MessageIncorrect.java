package fast.common.fix;

import fast.common.replay.MessageIncorrect_ReplayException;
import fast.common.replay.TagError_ReplayException;
import quickfix.Message;

import java.util.ArrayList;

public class MessageIncorrect extends MessageIncorrect_ReplayException {
    private int _numCriticalErrors = 0;
    public int getNumCriticalErrors() { return _numCriticalErrors;}

    public MessageIncorrect(Message actualMessage, ArrayList<TagError_ReplayException> tagErrors) {
        super(actualMessage, tagErrors);

        for(TagError_ReplayException tagError: tagErrors) {
            if((tagError.tag == 11)||(tagError.tag == 37)) {
                _numCriticalErrors++;
            }
        }
    }
}
