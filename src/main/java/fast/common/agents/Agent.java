package fast.common.agents;

import java.util.Map;

import org.apache.commons.lang3.reflect.MethodUtils;

import fast.common.context.StepResult;
import fast.common.core.Configurator;
import fast.common.logging.FastLogger;


// Agent should be threadsafe and stateless for callers - it's methods could be called from multiple parallel running tests
public abstract class Agent implements AutoCloseable {
	protected String _name;
	protected Map _agentParams;
	protected String _config_folder;
	protected Configurator _configurator;
	private FastLogger _logger;

	public Agent(){
		
	}
	
	public Agent(String name, Map agentParams, Configurator configurator) {
		_name = name;
		_logger = FastLogger.getLogger(String.format("%s:Agent", _name));
		_agentParams = agentParams;
		_config_folder = configurator.getConfigFolder();
		_configurator = configurator;
	}

	public <T extends StepResult> T run(String methodName, Class<T> returnClass, Object... params) throws Exception {
		Object returnValue;
		try {
			returnValue = MethodUtils.invokeMethod(this, methodName, params);
		} catch (Exception e) {
			afterException();
			throw e;
		}
		if (returnClass != null) {
			try {
				return returnClass.cast(returnValue);
			} catch (ClassCastException e) {
				_logger.error("Failed to cast return value to " + returnClass.getName());
				throw e;
			}
		} else {
			return null;
		}
	}

	public void run(String methodName, Object... params) throws Exception {
		this.run(methodName, null, params);
	}

	public void run(String methodName) throws Exception {
		this.run(methodName, null, new Object[0]);
	}

	public StepResult runWithResult(String methodName, Object... params) throws Exception {
		return this.run(methodName, StepResult.class, params);
	}

	// if agent has buffer then we usually want to dump it to log file after
	// scenario is completed - we can do it by overriding this method
	// if single thread we can remove all elements from buffer, otherwise they
	// might be used by another thread
	public void flushBuffersToLog(boolean isSingleTread) {
	}

	public Object getExtensionObject() {
		return this;
	}

	protected void afterException() throws Exception {
	}
}
