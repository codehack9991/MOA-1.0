package fast.common.agents;

import java.util.Map;

import fast.common.context.DataTable;
import fast.common.context.DatabaseStepResult;
import fast.common.context.StepResult.Status;
import fast.common.core.Configurator;
import fast.common.database.DbConnection;
import fast.common.logging.FastLogger;
import fast.common.utilities.ObjectFactory;

public class DatabaseAgent extends Agent {
	private FastLogger logger;
	private DbConnection dbConnection;
	/**
     * Constructs a new <tt>DatabaseAgent</tt> with
     * default configuration file (config.yml) and custom configuration files to
     * fetch required parameters.
     *
     * @param   name a string for naming the creating DatabaseAgent 
     * @param   agentParams a map to get the required parameters for creating a DatabaseAgent 
     * @param   configurator a Configurator instance to provide configuration info for the actions of the DatabaseAgent
     * 
     * @since 1.5
     */
	public DatabaseAgent(String name, Map<String, String> agentParams, Configurator configurator)  {
		super(name, agentParams, configurator);
		logger = FastLogger.getLogger(String.format("%s:DatabaseAgent", _name));
		_agentParams = agentParams;
		logger.info("Initializing Database Agent.");

		dbConnection = ObjectFactory.getInstance(DbConnection.class, agentParams);
		
		String instance = null;
		boolean encryptPassword = false;
		String jceProviderClass = null;
		if(agentParams.containsKey(DbConnection.OPTIONAL_PARAM_INSTANCE)){
			instance = agentParams.get(DbConnection.OPTIONAL_PARAM_INSTANCE);
			if(instance != null && instance.isEmpty()){
				instance = null;
			}
		}
		if(agentParams.containsKey(DbConnection.OPTIONAL_PARAM_ENCRYPT_PASSWARD)){
			encryptPassword = Boolean.parseBoolean(agentParams.get(DbConnection.OPTIONAL_PARAM_ENCRYPT_PASSWARD));			
		}
		if(agentParams.containsKey(DbConnection.OPTIONAL_PARAM_JECPROVIDER_CLASS)){
			jceProviderClass = agentParams.get(DbConnection.OPTIONAL_PARAM_JECPROVIDER_CLASS);			
		}
		dbConnection.setOptionalParameters(instance,  encryptPassword, jceProviderClass);
		
		try {
			if (dbConnection.Open()) {
				logger.info("Db connection is created.");
			} else {
				logger.warn("Failed to create db connection.");
			}

		} catch (Exception e) {
			logger.error("Exception occurs when create db connection. " + e.getMessage());
		}
	}
	/**
     * Gets DbConnection 
     * @return DbConnection
     * @since 1.0
     * @see fast.common.database.DbConnection
     */
	public DbConnection getConnection() {
		return dbConnection;
	}
	/**
	 *Execute query operation and acquired results to database server 
	 * 
	 * @param sql query string to execute
	 * @return a DatabaseStepResult object stores all query result(status and resultMap)
	 * @since 1.5
	 * @see fast.common.glue.CommonStepDefs#sqlQuery(String, String,String)
	 */
	public DatabaseStepResult query(String sql) {
		DatabaseStepResult result = new DatabaseStepResult();
		try {
			dbConnection.Open();
		} catch (Exception e1) {
			logger.error("Failed to open the db connection with exception: " + e1.getMessage());
			return null;
		}
		DataTable resultTable = new DataTable();
		
		try {
			resultTable = dbConnection.query(sql);
			result.setStatus(Status.Passed);
			result.setResult(resultTable);
			logger.info("Query operation is succeeded.");

		} catch (Exception e) {
			result.setStatus(Status.Failed);
			result.setFailedMessage(e.getMessage());
			logger.error("Query operation is failed with exception: " + e.getMessage());
		}
		try {
			dbConnection.Close();
		} catch (Exception e) {
			logger.error("Failed to close the db connection with exception: " + e.getMessage());
		}
		
		return result;
	}
	/**
	 * Execute update operation
	 * @param sql sql string to execute
	 * @return	a DatabaseStepResult stores all update result(status and affected rows)
	 * @since 1.5
	 * @see fast.common.glue.CommonStepDefs#sqlUpdate(String, String,String)
	 */
	public DatabaseStepResult update(String sql) {
		DatabaseStepResult result = new DatabaseStepResult();
		try {
			dbConnection.Open();
		} catch (Exception e1) {
			logger.error("Failed to open the db connection with exception: " + e1.getMessage());
			return null;
		}
		try {
			int affectedRows = dbConnection.update(sql);
			result.setStatus(Status.Passed);
			result.setAffectedRows(affectedRows);
			logger.info("Update operation is succeeded. Affected rows " + affectedRows);
		} catch (Exception e) {
			result.setStatus(Status.Failed);
			result.setFailedMessage(e.getMessage());
			logger.error("Update operation is failed with exception: " + e.getMessage());
		}
		try {
			dbConnection.Close();
		} catch (Exception e) {
			logger.error("Failed to close the db connection with exception: " + e.getMessage());
		}
		return result;
	}
	/**
	 * close DbConnection
	 */
	@Override
	public void close() throws Exception {
		dbConnection.Close();
		logger.info("Database agent is closed.");
	}
}
