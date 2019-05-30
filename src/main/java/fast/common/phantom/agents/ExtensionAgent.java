package fast.common.phantom.agents;

import java.util.Map;

import com.citi.cet.automation.framework.core.ObjectFactory;
import com.citi.cet.automation.framework.messaging.dna.DNAConfig;
import com.citi.cet.automation.framework.messaging.dna.DNAService;

import fast.common.agents.Agent;
import fast.common.core.Configurator;
import fast.common.logging.FastLogger;

public class ExtensionAgent extends Agent {
	private static FastLogger _logger = FastLogger.getLogger("ExtensionAgent");

	public ExtensionAgent(String name, Map agentParams, Configurator configurator) {
		super(name, agentParams, configurator);
		// TODO Auto-generated constructor stub
		construct(agentParams);
	}

	private Object extensionObject;
	
	private void construct(Map agentParams) {
		// TODO Auto-generated method stub
		Class classz = null;
		try {
			classz = Class.forName((String) agentParams.get("extension_class_name"));
		} catch (ClassNotFoundException e) {
			_logger.error("Failed to load the extension class with exception:\n" + e.getMessage());
		}
		extensionObject = ObjectFactory.getInstance(classz, agentParams);
		
	}

	@Override
	public Object getExtensionObject(){
		return extensionObject;
	}
	
	@Override
	public void close() throws Exception {
		// TODO Auto-generated method stub
		extensionObject = null;
	}

}
