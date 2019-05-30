package fast.common.logging;

import org.apache.logging.log4j.*;
//import ru.yandex.qatools.allure.annotations.Step;

public class FastLogger {
    private Logger _innerLogger;

    public FastLogger(Logger innerLogger) {
        _innerLogger = innerLogger;
    }

    public void updateName(String newName) {
        _innerLogger = LogManager.getLogger(newName);
    }

    public static FastLogger getLogger(String name) {
        Logger innerLogger = LogManager.getLogger(name);
        return new FastLogger(innerLogger);
    }

    public void debug(String str) {
        _innerLogger.debug(str);
    }

    public void info(String str) {
        _innerLogger.info(str);
        /*String allure_str = String.format("%s - %s", _innerLogger.getName(), str);
        LogToAllure(allure_str);*/
    }
    public void warn(String str) {
        _innerLogger.warn("\r\n" + str);
        /*String allure_str = String.format("%s - %s", _innerLogger.getName(), str);
        LogToAllure(allure_str);*/
    }

    public void error(String str) {
        _innerLogger.error("\r\n" + str);
        /*String allure_str = String.format("%s - %s", _innerLogger.getName(), str);
        LogToAllure(allure_str);*/
    }

    /*@Step("{0}")
    private void LogToAllure(String str) {

    }*/
    
    public void setThreadContextValue(String key, String value) {
    	ThreadContext.put(key, value);
    }
    
    public String getThreadContextValue(String key) {
    	return ThreadContext.get(key);
    }
    
    public void clearThreadContext() {
    	ThreadContext.clearAll();
    }
}

