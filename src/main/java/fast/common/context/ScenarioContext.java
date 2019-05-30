package fast.common.context;

import fast.common.logging.FastLogger;
import fast.common.core.Configurator;

import javax.script.ScriptException;

import cucumber.api.Scenario;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// TODO: merge EcmaScriptInterpreter, ScenarioContext, EvalScope. only keeo custom functions in separate class
// TODO: move log4j2.yml to config folder
public class ScenarioContext implements AutoCloseable {
    public final static String PARAMS_MAP_NAME = "Params";
    public final static String THREADPARAMS_MAP_NAME = "ThreadParams";
    public final static String THREAD_NUMBER_SETTING = "thread-number";
    private static FastLogger logger = FastLogger.getLogger("ScenarioContext");
    private EvalScope _evalScope = new EvalScope(); // TODO: merge this class with EvalScope
    private int _threadNumber;
    private StepResult _lastStepResult;
    private Object _scenario;
    private HashSet<String> tag37set = new HashSet<>();

    public ScenarioContext(Object scenario) throws Exception {
        LoadConfig();
        _scenario = scenario;
    }

    @SuppressWarnings("unchecked")
    private void LoadConfig() throws Exception {
        // log all properties for debugging
        logger.debug("All System Properties:");
        Properties props = System.getProperties();
        Iterator iterator = props.keySet().iterator();
        while (iterator.hasNext()) {
            String propertyName = (String)iterator.next();
            String propertyValue = props.getProperty(propertyName);
            logger.debug(propertyName + ": " + propertyValue);
        }


        EcmaScriptInterpreter.getInstance().setParams((Map) Configurator.getInstance().getSettingsMap().get(PARAMS_MAP_NAME));
        if (EcmaScriptInterpreter.getInstance().getParams() == null) {
            // KT: this is ok now - we can not use Params
            logger.warn(String.format("Section '%s' is not defined in config", PARAMS_MAP_NAME));
        }

        String threadNumberStr = System.getProperty(THREAD_NUMBER_SETTING); // for some reason 0${surefire.forkNumber} - works only with leading 0, otherwise it returns null
        if(threadNumberStr == null) {
//            throw new Exception(String.format("System.Property '%s' is not set", THREAD_NUMBER_SETTING));
            logger.warn(String.format("System.Property '%s' is not set, considering that the test is running in single-thread mode.", THREAD_NUMBER_SETTING));
            threadNumberStr = "01";
        }


        logger.debug(String.format("thread number: '%s'", threadNumberStr));
        _threadNumber = Integer.parseInt(threadNumberStr);
        Map threadParams = (Map) Configurator.getInstance().getSettingsMap().get(THREADPARAMS_MAP_NAME);
        logger.debug(String.format("reading fork variables from [%s]", threadParams));
        if (threadParams == null) { // this is ok now
            logger.warn(String.format("Section '%s' is not defined in config", THREADPARAMS_MAP_NAME));
        }

        _evalScope.setThreadParams(threadParams, _threadNumber);
        // TODO: ensure that all thread variables has enough number of items >= num of threads!

    }

    public HashSet<String> getTag37set() {
		return tag37set;
	}

	public void setTag37set(HashSet<String> tag37set) {
		this.tag37set = tag37set;
	}

	public <T extends Object> T getScenario(){
    	return (T)_scenario;
    }

    public StepResult getLastResultVariable() {
        return _lastStepResult;
    }
    
    public <T extends StepResult> T getVariable(String varName){
    	return (T)_evalScope.getVar(varName);
    }

    public String processString(String str) {
        return _evalScope.processString(str);
    }

    public void setThreadParam(String paramName, Object value) throws Exception {
        _evalScope.updateParam(paramName, value);
    }

    public void saveLastStepResult(StepResult lastStepResult, String varName) { // TODO: make name optional
        _lastStepResult = lastStepResult;
        if (varName != null) {
            _evalScope.saveVar(varName, lastStepResult);
        }
    }

    @Override
    public void close() throws Exception {
        logger.debug("Clearing Scenario Context");
        _evalScope.close();
        tag37set.clear();
    }
}
