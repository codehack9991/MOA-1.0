package fast.common.gmdReplay.pserver;

import com.citi.gmd.client.config.GMDConfig;
import com.citi.gmd.client.config.GMDTransport;
import com.citi.gmd.client.config.GMDTransportTCP;
import com.citi.gmd.client.config.GMDTransportType;

public class GMDTcpServer extends GMDDummyServer {
    static{
        System.setProperty("appname", "GMDTcpServer");
    }
    
    public GMDTcpServer(int port) {
        this.port = port;
    }

}
