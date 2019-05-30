package fast.common.phantom.agents;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.citi.cet.automation.framework.core.ObjectFactory;
import com.citi.cet.automation.framework.messaging.dna.DNAConfig;
import com.citi.cet.automation.framework.messaging.dna.DNAService;
import com.citi.cet.automation.framework.messaging.dna.DataAccessException;

import fast.common.agents.Agent;
import fast.common.core.Configurator;
import fast.common.logging.FastLogger;

public class DNAAgent extends Agent {
	private static FastLogger _logger = FastLogger.getLogger("DNAAgent");
	DNAService dnaService = null;

	public DNAAgent(String name, Map agentParams, Configurator configurator) {
		super(name, agentParams, configurator);
		connectDNA(agentParams);
	}

	public void connectDNA(Map config) {
		try {
			DNAConfig dnaConfig = (DNAConfig) ObjectFactory.getInstance(DNAConfig.class, config);
			dnaService = new DNAService(dnaConfig);
			dnaService.open();

		} catch (Exception e) {
			_logger.error("Failed to conncect DNA with exception:\n" + e.getMessage());
		}
	}

	@Override
	public void close() throws Exception {
		dnaService.close();

	}

	public List<LinkedHashMap<String, Object>> queryDNA(String sql) {

		try {
			return dnaService.query(sql);
		} catch (DataAccessException e) {
			e.printStackTrace();
		}
		return null;
	}
}
