package fast.common.context;

import fast.common.fix.FixHelper;
import fast.common.fix.TimeHelper;

import javax.script.*;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * 
 */
public class EcmaScriptInterpreter {
	private static EcmaScriptInterpreter instance;
	private ScriptEngineManager manager;
	private ScriptEngine engine;
	private Map<String, Object> params;

	@FunctionalInterface
	public interface VarArgFunction<R, T, U> {

		R apply(T arg, U[] args);
	}

	public ScriptEngine getEngine() {
		return engine;
	}

	private EcmaScriptInterpreter() {
		manager = new ScriptEngineManager();
		engine = manager.getEngineByName("nashorn");
		params = new HashMap<>();

		exposeJavaMethods();
	}

	/**
	 * Static methods added to EcmaScriptExposedMethods and bound here will be
	 * visible in JS code embedded in Cucumber
	 */
	@FunctionalInterface
	public interface BiFunction<R, S, T> {
		R apply(S s, T t) throws ScriptException;
	}

	@FunctionalInterface
	public interface TrFunction<R, S, T, U> {
		R apply(S s, T t, U u) throws ScriptException;
	}

	private void exposeJavaMethods() {
		engine.put("generateTsWithNanoseconds", (Supplier<String>) TimeHelper::generateTsWithNanoseconds);
		engine.put("generateTsWithMicroseconds", (Supplier<String>) TimeHelper::generateTsWithMicroseconds);
		engine.put("generateTsWithMilliseconds", (Supplier<String>) TimeHelper::generateTsWithMilliseconds);
		engine.put("generateTsWithSeconds", (Supplier<String>) TimeHelper::generateTsWithSeconds);
		engine.put("generateTsWithDate", (Supplier<String>) TimeHelper::generateTsWithDate);
		engine.put("generateTsFormat", (BiFunction<String, String, String>) TimeHelper::generateTsFormat);
		engine.put("generateClOrdID", (Supplier<String>) FixHelper::generateClOrdID);
		engine.put("getRandom", (BiFunction<Integer, Integer, Integer>) HelperMethods::getRandom);
		engine.put("getPriceForSymbolFromReuters",
				(Function<String, Number>) HelperMethods::getPriceForSymbolFromReuters);
		engine.put("Round", (BiFunction<Number, Number, Integer>) HelperMethods::Round);
        engine.put("Mod", (BiFunction<Number,Number,Number>)  HelperMethods::Mod);
        engine.put("Escapse", (Function<String,String>)  HelperMethods::EscapseReservedCharacters);
        engine.put("UnEscapse", (Function<String,String>)  HelperMethods::UnEscapseReservedCharacters);
		engine.put("FormatNumber", (BiFunction<String, Number, String>) HelperMethods::FormatNumber);
		engine.put("getQuotes", (Function<String, String>) HelperMethods::getQuotes);
		engine.put("getOrCalculate", (BiFunction<String, String, String>) HelperMethods::getOrCalculate);
		engine.put("getNextNumber", (Supplier<Integer>) FixHelper::getNextNumber);
		engine.put("addMinutesToNow", (TrFunction<String, Integer, String, String>) TimeHelper::addMinutesToNow);
		engine.put("addBusinessDaysFromToday", (Function<Integer, String>) HelperMethods::addBusinessDaysFromToday);
		engine.put("createTsID", (Supplier<String>) HelperMethods::createTsID);
	}

	public static EcmaScriptInterpreter getInstance() {
		if (instance == null)
			instance = new EcmaScriptInterpreter();
		return instance;
	}

	public String interpret(String script) throws ScriptException {
		engine.put("params", params);
		return engine.eval(script).toString();
	}

	@SuppressWarnings("unchecked")
	public <T> T interpretAndReturnAsIs(String script) throws ScriptException {
		engine.put("params", params);
		return (T) engine.eval(script);
	}

	public void addJavaMethods(String methodName, Object methodFunc) {
		engine.put(methodName, methodFunc);
	}

	public Map<String, Object> getParams() {
		return params;
	}

	public void setParams(Map<String, Object> params) {
		this.params = params;
	}
}
