package fast.common.agents;

/**
 * Created by ab56783 on 08/22/2017.
 */
public interface IDataProcessAgent {
    void processData(String name, String symbol, Object obj);
    void setMDspeed(int speed);
}
