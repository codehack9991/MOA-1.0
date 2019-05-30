package fast.common.gmdReplay.pserver;

import com.citi.gmd.client.config.*;
import com.citi.gmd.client.config.loader.GMDConfigLoader;
import com.citi.gmd.client.config.loader.GMDRegionConfigLoader;
import com.citi.gmd.client.messages.component.GMDSubscription;
import com.citi.gmd.client.messages.constants.GMDSubscriptionType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.ini4j.Ini;
import org.ini4j.Profile.Section;

import java.io.*;
import java.util.StringTokenizer;

public class GMDSITConfigLoader implements GMDConfigLoader {
    private static final Logger log = LoggerFactory.getLogger(GMDSITConfigLoader.class);
    private static String App_Session_Config = "App_Session_Config";
    private Ini ini = null;
    private static int StartSequence = 1;
    private static String SenderCompId;
    private static int ChannelId;
    private static int PartitionNum;
    private GMDRegionConfig regionCfg;
    
    public GMDConfig loadConfig(String pathname) {
        GMDConfig config = new GMDConfig();
        try {
            ini = new Ini(new File(pathname));
            if (!ini.containsKey(App_Session_Config)) {
                log.error("No section with name App_Session_Config in " + pathname);
                System.exit(0);
            }
            Section section = ini.get(App_Session_Config);
            for (String session : section.values()) {
                if (!ini.containsKey(session)) {
                    log.error("No session with name  " + session + " in " + pathname);
                    System.exit(0);
                }
                populateSession(ini.get(session), config);
            }
            if (ini.containsKey("Region_Config")) {
                GMDRegionConfigLoader regionCfgLoader = new GMDRegionConfigLoader();
                regionCfg = regionCfgLoader.loadRegionConfig(ini.get("Region_Config").get("RegionConfigFile"));
            }
            getConfigString();
        } catch (FileNotFoundException e) {
            log.error("FileNotFoundException", e);
            System.exit(0);
        } catch (IOException e) {
            log.error("IOException", e);
            System.exit(0);
        }
        return config;
    }
    
    public void populateSession(Section sessionSection, GMDConfig config) {
        log.info("Loading section : " + sessionSection.getName());
        if (sessionSection.get(GMDConfigConstant.SessionType).equalsIgnoreCase(GMDConfigConstant.Tcp)) {
            GMDTransportTCP tcpTrasport = new GMDTransportTCP();
            updateCommonFields(sessionSection, tcpTrasport);
            tcpTrasport.setRetryWaitInterval(1);
            tcpTrasport.setHbInterval(60);
            tcpTrasport
                    .setByPassLogOnCheck(Boolean.parseBoolean(sessionSection.get(GMDConfigConstant.ByPassLogOnCheck)));
            
            String serverListStr = sessionSection.get(GMDConfigConstant.TcpServerAddress);
            StringTokenizer serverListTokenizer = new StringTokenizer(serverListStr, ",");
            

            if(sessionSection.containsKey("SessionPurpose")){
                tcpTrasport.setSessionPurpose(sessionSection.get("SessionPurpose"));
            }
            
            while (serverListTokenizer.hasMoreTokens()) {
                GMDServerConfig serverConfig = new GMDServerConfig();
                String serverConfigStr = serverListTokenizer.nextToken();
                StringTokenizer tempTokenizer = new StringTokenizer(serverConfigStr, ":");
                serverConfig.setServerName(tempTokenizer.nextToken());
                serverConfig.setPortNo(Integer.parseInt(tempTokenizer.nextToken()));
                serverConfig.setUserId(sessionSection.get(GMDConfigConstant.UserName));
                serverConfig.setPassword(sessionSection.get(GMDConfigConstant.Password));
                serverConfig.setUsageType("1");
                serverConfig.setAccountType('C');
                tcpTrasport.getGmdServerList().add(serverConfig);
                tcpTrasport.setNumOfRetries(1);
            }
            config.getGmdTransportList().add(tcpTrasport);
        } else if (sessionSection.get(GMDConfigConstant.SessionType).equalsIgnoreCase(GMDConfigConstant.MultiCast)) {
            GMDTransportMulticast mcTransport = new GMDTransportMulticast();
            updateCommonFields(sessionSection, mcTransport);
            String serverListStr = sessionSection.get("MulticastAddress");
            String sessionPurpose = sessionSection.get("SessionPurpose");
            if (sessionPurpose.equalsIgnoreCase("Sender")) {
                mcTransport.setSender(true);
            } else {
                mcTransport.setSender(false);
            }
            StringTokenizer serverListTokenizer = new StringTokenizer(serverListStr, ",");
            while (serverListTokenizer.hasMoreTokens()) {
                GMDServerConfig serverConfig = new GMDServerConfig();
                String serverConfigStr = serverListTokenizer.nextToken();
                StringTokenizer st1 = new StringTokenizer(serverConfigStr, ";");
                serverConfig.setLocalInterface(st1.nextToken());
                StringTokenizer tempTokenizer = new StringTokenizer(st1.nextToken(), ":");
                serverConfig.setServerName(tempTokenizer.nextToken());
                serverConfig.setPortNo(Integer.parseInt(tempTokenizer.nextToken()));
                mcTransport.getGmdServerList().add(serverConfig);
            }
            config.getGmdTransportList().add(mcTransport);
        } else if (sessionSection.get(GMDConfigConstant.SessionType).equalsIgnoreCase(GMDConfigConstant.LBM)) {
            GMDTransportLBM lbmTransport = new GMDTransportLBM();
            updateCommonFields(sessionSection, lbmTransport);
            
            /**
             * ListeningMode - Static, Dynamic
             * If it does not exist of invalid value specified then warn the user but use default
             */
            
            if(sessionSection.containsKey(GMDConfigConstant.ListeningMode)){
                String lbmPubMode = sessionSection.get(GMDConfigConstant.ListeningMode).trim();
                if(lbmPubMode == null || lbmPubMode.isEmpty()){
                    log.warn("ListeningMode value not specified using default "+ GMDConfigConstant.LBM_DEFAULT_LISTENING_MODE + ", in the section "+ sessionSection.getName());
                    lbmTransport.setListeningMode(GMDConfigConstant.LBM_DEFAULT_LISTENING_MODE);
                }else if(!lbmPubMode.equalsIgnoreCase("Static") && !lbmPubMode.equalsIgnoreCase("Dynamic")){
                    log.warn("Invalid value for PublishingMode using default "+ GMDConfigConstant.LBM_DEFAULT_LISTENING_MODE+", in the section "+ sessionSection.getName());
                    lbmTransport.setListeningMode(GMDConfigConstant.LBM_DEFAULT_LISTENING_MODE);
                }else{
                    lbmTransport.setListeningMode(lbmPubMode);
                }
            }else{
                log.warn("Key- ListeningMode does not exist using default "+ GMDConfigConstant.LBM_DEFAULT_LISTENING_MODE+", in the section "+ sessionSection.getName());
                lbmTransport.setListeningMode(GMDConfigConstant.LBM_DEFAULT_LISTENING_MODE);
            }
            
            if(sessionSection.containsKey(GMDConfigConstant.BlockingMode) ){
                String bm = sessionSection.get(GMDConfigConstant.BlockingMode);
                if(bm != null && !bm.trim().isEmpty()){
                    lbmTransport.setBlockingMode(Boolean.parseBoolean(bm));
                }
            }
          
            /**
             * PartitionType
             */
            if(sessionSection.containsKey(GMDConfigConstant.PartitionType)){
                String lbmPartitionType = sessionSection.get(GMDConfigConstant.PartitionType).trim();
                if(lbmPartitionType == null || lbmPartitionType.isEmpty()){
                    log.warn("PartitionType value not specified using default "+ GMDConfigConstant.LBM_DEFAULT_PARTITION_TYPE + ", in the section "+ sessionSection.getName());
                    lbmTransport.setPartitionType(GMDConfigConstant.LBM_DEFAULT_PARTITION_TYPE);
                }else if(!lbmPartitionType.equalsIgnoreCase("Range") && !lbmPartitionType.equalsIgnoreCase("Symbol") && !lbmPartitionType.equalsIgnoreCase("MsgType")){
                    log.warn("Invalid value for PartitionType using default "+ GMDConfigConstant.LBM_DEFAULT_PARTITION_TYPE+", in the section "+ sessionSection.getName());
                    lbmTransport.setPartitionType(GMDConfigConstant.LBM_DEFAULT_PARTITION_TYPE);
                }else{
                    lbmTransport.setPartitionType(lbmPartitionType);
                }
            }else{
                log.warn("Key- PartitionType does not exist using default "+ GMDConfigConstant.LBM_DEFAULT_PARTITION_TYPE+", in the section "+ sessionSection.getName());
                lbmTransport.setPartitionType(GMDConfigConstant.LBM_DEFAULT_PARTITION_TYPE);
            }
            
            lbmTransport.setLicense(sessionSection.get(GMDConfigConstant.License));
            lbmTransport.setLicenseOption(sessionSection.get(GMDConfigConstant.LicenseOption));
            lbmTransport.setReqSessionTopic(sessionSection.get(GMDConfigConstant.ReqSessionTopic));
            lbmTransport.setReqSessionTransportType(sessionSection.get(GMDConfigConstant.ReqSessionTransportType));
            lbmTransport.setRespSessionTopic(sessionSection.get(GMDConfigConstant.RespSessionTopic));
            lbmTransport.setTopic(sessionSection.get(GMDConfigConstant.Topic));
            lbmTransport.setPartitionType(sessionSection.get(GMDConfigConstant.PartitionType));
            lbmTransport.setConfigFileName(sessionSection.get(GMDConfigConstant.LBMConfigFileName));
            config.getGmdTransportList().add(lbmTransport);
        }
    }
    
    private void updateCommonFields(Section sessionSection, GMDTransport transport) {
        transport.setRecvBufferSize(1024);
        transport.setSendBufferSize(1024);
        transport.setSessionName(sessionSection.getName());
        if (sessionSection.containsKey("StartSequence")) {
            GMDSITConfigLoader.StartSequence = Integer.parseInt(sessionSection.get("StartSequence"));
        }
        if (sessionSection.containsKey("SenderCompId")) {
            GMDSITConfigLoader.SenderCompId = sessionSection.get("SenderCompId");
        }
        if (sessionSection.containsKey("ChannelId")) {
            GMDSITConfigLoader.ChannelId = Integer.parseInt(sessionSection.get("ChannelId"));
        }
        
        if (sessionSection.containsKey("PartitionNum")) {
            GMDSITConfigLoader.PartitionNum = Integer.parseInt(sessionSection.get("PartitionNum"));
        }
        
        if (sessionSection.containsKey("MsgType")) {
            transport.setMsgType(sessionSection.get("MsgType"));
        } else {
            transport.setMsgType("CitiBinary");
        }
        if (sessionSection.containsKey(GMDConfigConstant.SubscriptionFile)) {
            String symbolFile = sessionSection.get(GMDConfigConstant.SubscriptionFile);
            if (symbolFile != null && !symbolFile.isEmpty()) {
                int feedId = 0;
                int mktCtrId = 0;
                if (sessionSection.containsKey("FeedId")) {
                    feedId = Integer.parseInt(sessionSection.get("FeedId"));
                }
                if (sessionSection.containsKey("MktCtrId")) {
                    mktCtrId = Integer.parseInt(sessionSection.get("MktCtrId"));
                }
                BufferedInputStream in = null;
                BufferedReader bufRead = null;
                InputStream stream = null;
                FileReader fReader = null;
                try {
                	stream = this.getClass().getClassLoader().getResourceAsStream(symbolFile);
                    if(stream != null){
                        in = new BufferedInputStream(stream);
                        bufRead = new BufferedReader(new InputStreamReader(in));
                    }else{
                        File file = new File(symbolFile);
                        fReader = new FileReader(file);
                        bufRead = new BufferedReader(fReader);
                    }
                    String msg;
                    while ((msg = bufRead.readLine()) != null) {
                        StringTokenizer tokenizer = new StringTokenizer(msg, ",");
                        GMDSubscription subInfo = new GMDSubscription();
                        subInfo.setKeepSubActiveIfSymNotFound((byte) 1);
                        subInfo.setSubType(GMDSubscriptionType.GMD_CLIENT_SUBTYPE_SNAPSHOTUPDATES);
                        subInfo.setKeepSubActiveIfSymNotFound((byte) 1);
                        subInfo.setSymbol(tokenizer.nextToken());
                        subInfo.setFeedId((short) feedId);
                        subInfo.setMktCtrId((byte) mktCtrId);
                        if (!transport.getSubscriptionList().contains(subInfo)) {
                            transport.getSubscriptionList().add(subInfo);
                        } else {
                            log.warn("Duplicate entry: " + msg + ", in " + symbolFile);
                        }
                    }
                    
                } catch (FileNotFoundException e) {
                    log.error("File not found ", e);
                } catch (IOException e) {
                    log.error("IO exception ", e);
                }
                finally{
                	if(bufRead != null){
                        try {
							bufRead.close();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							log.error("Failed to close the file stream", e);
						}
                    }
                    if(in != null){
                        try {
							in.close();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							log.error("Failed to close the file stream", e);
						}
                    }
                    if(fReader != null){
                        try {
                        	fReader.close();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							log.error("Failed to close the file stream", e);
						}
                    }
                    if(stream != null){
                        try {
							stream.close();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							log.error("Failed to close the file stream", e);
						}
                    }
                }
            }
        }
    }
    
    public String getConfigString() {
        String str = ini.toString();
        log.info(str);
        str = str.replaceAll("\\[(.*?)\\]", "$1");
        str = str.replaceAll("\\[(.*?)\\]", "$1");
        str = str.replaceAll("\\{(.*?)\\}", "$1");
        return str;
    }
    
    public GMDRegionConfig getRegionConfig(){
        return regionCfg;
    }
    
    
}
