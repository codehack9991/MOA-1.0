package fast.common.agents;

import com.citi.gmd.client.config.GMDConfig;
import com.citi.gmd.client.config.GMDTransport;
import com.citi.gmd.client.config.GMDTransportTCP;
import com.citi.gmd.client.config.GMDTransportType;
import fast.common.core.Configurator;
import fast.common.gmdReplay.pserver.GMDCertificationClient;
import fast.common.gmdReplay.pserver.GMDSITConfigLoader;
import fast.common.gmdReplay.pserver.GMDTcpServer;

import java.util.Map;


public class GmdPserverAgent extends Agent {

    public GmdPserverAgent(String name, Map agentParams, Configurator configurator) {
        super(name, agentParams, configurator);
        String configFullFilename = configurator.getFilename(agentParams, "configFile");
        GMDSITConfigLoader loader = new GMDSITConfigLoader();
        GMDConfig cfg = loader.loadConfig(configFullFilename);
        for(GMDTransport transport : cfg.getGmdTransportList()){
            if(transport.getTransportType() == GMDTransportType.TCP){
                GMDTransportTCP tcpTransport = (GMDTransportTCP) transport;
                GMDTcpServer server = new GMDTcpServer(tcpTransport.getGmdServerList().get(0).getPortNo());
                GMDCertificationClient client = new GMDCertificationClient(server);
                server.registerMBeans(client);
                server.cb = client;
                server.connect();
            }
        }
    }

    public void start() {

    }

    @Override
    public void close() throws Exception {

    }
}
