package fast.common.agents;


public interface IDataProcessAgent {
    void processData(String name, String symbol, Object obj);
    void setMDspeed(int speed);
}
