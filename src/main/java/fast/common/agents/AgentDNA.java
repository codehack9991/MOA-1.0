package fast.common.agents;

import com.citi.dna.client.Service;
import com.citi.dna.data.BaseTable;
import com.citi.dna.data.Table;
import com.citi.dna.discovery.DiscoveryClient;
import com.citi.dna.util.c.Dict;
import com.citi.dna.util.c.Flip;

import fast.common.context.DNAStepResult;
import fast.common.core.Configurator;
import fast.common.logging.FastLogger;
import fast.common.utilities.ExcelUtility;
import fast.common.utilities.ObjectFactory;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import com.citi.dna.comm.Transport;
import com.citi.dna.comm.TransportFactory;

/**
 * The {@code AgentDNA} class defines the common method to send query to the specified DNA server
 * <p>The method support send query and save results into DNAStepResult.</p>
 * 
 * <p>Details information for using a AgentDNA can see: 
 * <p><a href="https://cedt-confluence.nam.nsroot.net/confluence/display/167813001/DNA+Automation+Example">Examples</a></p>
 * @author QA Framework Team
 * @since 1.5
 */
public class AgentDNA extends Agent {
	public static final String PARAM_TRANSPORT_ID = "Transport.ID";
	public static final String PARAM_TRANSPORT_CLASS = "Transport.CLASSNAME";
	public static final String PARAM_CONNECTION_FACTORY = "connectionFactory";
	public static final String PARAM_HOST = "host";
	public static final String PARAM_USER = "user";
	public static final String PARAM_PASSWORD = "password";
	public static final String PARAM_ENV = "ENV";
	public static final String PARAM_REUSE_SERVICE = "reuse_service";
	public static final String PARAM_SERVICES = "services";
	public static final String PARAM_SERVICE_NAME = "name";
	public static final String PARAM_SERVICE_USER = "user";
	public static final String PARAM_SERVICE_PASSWORD = "password";
	public static final String PARAM_FUNCTIONS = "functions";
	
    private FastLogger logger;
    private boolean reuseService;
    private Map<String, Service> servicesInUse;   
    
    /**
     * Constructs a new <tt>AgentDNA</tt> with
     * default configuration file (config.yml) and custom configuration files to
     * fetch required parameters.
     *
     * @param   name a string for naming the creating AgentDNA 
     * @param   agentParams a map to get the required parameters for creating a AgentDNA 
     * @param   configurator a Configurator instance to provide configuration info for the method of the AgentDNA
     * @throws  Exception
     * @since 1.0
     */
    public AgentDNA(String name, Map agentParams, Configurator configurator) throws Exception {
        super(name, agentParams, configurator);
        logger = FastLogger.getLogger(String.format("%s:AgentDNA", _name));
        reuseService = Configurator.getBooleanOr(_agentParams , PARAM_REUSE_SERVICE, false);
        servicesInUse = reuseService ? new HashMap<>() : null;

        start();
    }

    private void start() {

        String transportID =  Configurator.getStringOr(_agentParams , PARAM_TRANSPORT_ID, null); //AgentDNA
        String transportClassname =  Configurator.getStringOr(_agentParams,  PARAM_TRANSPORT_CLASS, null);
        String host = Configurator.getStringOr(_agentParams, PARAM_HOST, null);
        String user = Configurator.getStringOr(_agentParams, PARAM_USER, null);
        String password = Configurator.getStringOr(_agentParams, PARAM_PASSWORD, null);
        String connectionFactory=Configurator.getStringOr(_agentParams, PARAM_CONNECTION_FACTORY, null);
        String environment=Configurator.getStringOr(_agentParams, PARAM_ENV, null);
        logger.info("\n\nConnecting:");

        Properties props = new Properties();
        props.put(Transport.ID, transportID); //AgentDNA
        props.put(Transport.CLASSNAME, transportClassname);
        props.put("provider", host);
        props.put("connectionFactory", connectionFactory);
        props.put("username", user);
        props.put("password", password);
        props.put("ENV", environment);

        TransportFactory.getInstance().getTransport(props);
    }
    
    private Service getService(String name) throws Exception{    	
    	if(reuseService && servicesInUse.containsKey(name)){
    		return servicesInUse.get(name);
    	} else {
    		DiscoveryClient discovery = new DiscoveryClient();
    		String serviceName = name;
    		Map servicesParams = Configurator.getMapOr(_agentParams , PARAM_SERVICES, null);
    		Map serviceParams = servicesParams != null ? Configurator.getMapOr(servicesParams, name, null) : null;
    		if(serviceParams != null){
    			serviceName = Configurator.getStringOr(serviceParams, PARAM_SERVICE_NAME, name);
    			String serviceUser = Configurator.getStringOr(serviceParams, PARAM_SERVICE_USER, null);
    			String servicePassword = Configurator.getStringOr(serviceParams, PARAM_SERVICE_PASSWORD, null);
    			if(serviceUser != null && servicePassword != null){
    				discovery.getAuthenticationManager().registerCredentials(serviceName, serviceUser, servicePassword);
    			}
    		}
    		
    		Service service = discovery.getService(serviceName);
    		if(service == null){
    			throw new RuntimeException("Failed to discover service " + serviceName);
    		}
    		// Add service to map if service is up
    		if(servicesInUse != null) {
    			servicesInUse.put(name, service);
    		}
    		return service;
    	}
    }
    
     /**
     * <p>Executes dna query string and saves results into DNAStepResult
     * @param sendTopicName send a topic to connect the specified DNA server
     * @param queryValue query string used to fetch data from DNA serve 
     * @return a DNAStepResult contains results for later use
     * @throws Throwable
     * @since 1.5
     * @see fast.common.glue.CommonStepDefs#dnaExecuteAndCheck(String, String, String, String)
     * @see fast.common.glue.CommonStepDefs#dnaExecuteAndCheck(String, String, String, String, cucumber.api.DataTable)
     * @see fast.common.glue.CommonStepDefs#dnaExecuteAndCheckAll(String, String, String, String, cucumber.api.DataTable)
     * @see fast.common.glue.CommonStepDefs#dnaExecuteAndCheckValue(String, String, String, String, cucumber.api.DataTable)
     */
    public DNAStepResult sendQuery(String sendTopicName, String queryValue) throws Throwable {
		Service dna = getService(sendTopicName);		
		Object resultOfQuery = dna.exec(queryValue);

		if (resultOfQuery == null) {
			logger.info("Query result is empty!");
			return new DNAStepResult(resultOfQuery);
		}
		if (resultOfQuery instanceof Dict|| resultOfQuery instanceof Flip) {
			Table tableResultDNA = dna.getDataManager().toTable(resultOfQuery);
			return new DNAStepResult(tableResultDNA);
		} else {
			String strResult = resultOfQuery.toString();
			return new DNAStepResult(strResult);
		}
	}
    
    /**
     * <p>Executes DNA function with arguments and saves results into DNAStepResult
     * @param serviceName DNA service name
     * @param functionName function name
     * @param functionArgs arguments for the function 
     * @return a DNAStepResult contains results for later use
     * @throws Throwable
     * @since 1.9
     * @see fast.common.glue.CommonStepDefs#dnaExecuteAndCheck(String, String, String, String)
     * @see fast.common.glue.CommonStepDefs#dnaExecuteAndCheck(String, String, String, String, cucumber.api.DataTable)
     * @see fast.common.glue.CommonStepDefs#dnaExecuteAndCheckAll(String, String, String, String, cucumber.api.DataTable)
     * @see fast.common.glue.CommonStepDefs#dnaExecuteAndCheckValue(String, String, String, String, cucumber.api.DataTable)
     */
    public DNAStepResult executeFunction(String serviceName, String functionName, Object[] functionArgs) throws Throwable {
		Service dna = getService(serviceName);		
		Object result = dna.exec(functionName, functionArgs);

		if (result == null) {
			logger.info("Query result is empty!");
			return new DNAStepResult(result);
		}
		if (result instanceof Dict|| result instanceof Flip) {
			Table tableResultDNA = dna.getDataManager().toTable(result);
			return new DNAStepResult(tableResultDNA);
		} else {
			String strResult = result.toString();
			return new DNAStepResult(strResult);
		}
	}    
    
    /**
     * <p>Load data in given CSV file into DNA table
     * @param serviceName DNA service name
     * @param tableName target table name
     * @param csvFile source CSV file name 
     * @param separator separator for CSV file
     * @param charset charset name for CSV file
     * @return a DNAStepResult contains results for later use
     * @throws Exception
     * @since 1.9
     */    
    public void bulkWrite(String serviceName, String tableName, String csvFile, String separator, String charset) throws Exception {
    	Service service = getService(serviceName);
    	//refer https://cedt-confluence.nam.nsroot.net/confluence/display/159788/Upsert+or+Update+data+to+KDB
    	//initialize columns for table to write
    	Object result = service.exec("select [0] from " + tableName);
    	Table table = service.getDataManager().toTable(result);    	
    	BaseTable tableToWrite = new BaseTable();
    	for(String colName : table.getColumnNames()){
    		tableToWrite.addColumn(colName, table.getColumn(table.getColumnIndex(colName)));
    	}
    	
    	ArrayList<String[]> csvData = ExcelUtility.loadCsv(csvFile, separator, Charset.forName(charset));
    	for(int i=0; i < csvData.size(); i++){
    		int row = tableToWrite.appendNewRow();
    		String[] rowData = csvData.get(i);
    		for(int j=0; j < rowData.length; j++){
    			tableToWrite.set(j, row, ObjectFactory.parseTypeFromString(rowData[j], table.getColumn(j).getClass()));
    		}
    	}
    	
    	tableToWrite.commit();    	
    	service.exec("upd", tableName, tableToWrite);
    }
    
    /**
     * <p>Executes dna query string with pooling time and saves results into DNAStepResult
     * @param sendTopicName send a topic to connect the specified DNA server
     * @param queryValue query string used to fetch data from DNA serve 
     * @return a DNAStepResult contains results for later use
     * @throws Throwable
     * @since 1.8
     * @see fast.common.glue.CommonStepDefs#dnaExecuteAndPollAndCheck(String, String, String, String)
     */
	public DNAStepResult executeQueryWithPoll(String sendTopicName, String queryValue) throws Throwable {
 
		String numTries=Configurator.getStringOr(_agentParams, "numberOfTries", "1"); // Poll up to <numTries> times. Default Value = 1
        String pollTimeout=Configurator.getStringOr(_agentParams, "pollingTimeOut", "10"); // ...every <pollTimeout> milliseconds - message timeout. Default Value = 10
		Service dna = getService(sendTopicName);

		for (int i = 0; i < Integer.parseInt(numTries); i++) {

			Object resultOfQuery = dna.exec(queryValue);
			
			if (resultOfQuery == null) {
				logger.info("Query result is empty!");
				return new DNAStepResult(resultOfQuery);
			}
			
			Table tableResultDNA = dna.getDataManager().toTable(resultOfQuery);

			if ((tableResultDNA.getSize()) != 0) {
				if (resultOfQuery instanceof Dict || resultOfQuery instanceof Flip) {

					return new DNAStepResult(tableResultDNA);
				} else {
					String strResult = resultOfQuery.toString();

					return new DNAStepResult(strResult);
				}
			}
			Thread.sleep(Integer.parseInt(pollTimeout));
		}
		throw new RuntimeException("Result of query : " + queryValue + " is blank.");
	}

    @Override
    public void close() throws Exception {
    	if(servicesInUse != null){
	    	for(Service service : servicesInUse.values()){
	    		if(service.isConnected()){
	    			service.getConnection().close();
	    		}
	    	}
	    	
	    	servicesInUse.clear();
    	}
    }
}
