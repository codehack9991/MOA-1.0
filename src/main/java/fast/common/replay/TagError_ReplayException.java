package fast.common.replay;

////////////// internal exceptions
public abstract class TagError_ReplayException extends ReplayException {
    public int tag;
    public TagError_ReplayException(int tag) {
        this.tag = tag;
    }
}
