package fast.common.phantom.agents;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;


import com.cet.citi.automation.framework.database.connections.DbConnection;

import com.citi.cet.automation.framework.core.ObjectFactory;

import fast.common.agents.Agent;
import fast.common.core.Configurator;

public class DatabaseAgent extends Agent {

	private DbConnection dbconnection;
	
	
	public DatabaseAgent(String name, Map agentParams, Configurator configurator) {
		super(name, agentParams, configurator);
		
		dbconnection = ObjectFactory.getInstance(DbConnection.class, agentParams);
		dbconnection.Open();
		System.out.println("");
	}

	public DbConnection getConnection(){
		return dbconnection;
	}
	
	@Override
	public void close() throws Exception {
		dbconnection.Close();

	}
	
	 
}
