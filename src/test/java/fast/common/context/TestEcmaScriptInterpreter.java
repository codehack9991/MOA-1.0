package fast.common.context;

import static org.junit.Assert.assertNotNull;
import static org.testng.Assert.assertEquals;

import java.util.Map;

import javax.script.ScriptEngine;
import javax.script.ScriptException;

import org.junit.Test;

public class TestEcmaScriptInterpreter {

	@Test
	public void testEcmaScriptInterpreter() throws ScriptException {
		EcmaScriptInterpreter instance = EcmaScriptInterpreter.getInstance();
		assertNotNull(instance);
		ScriptEngine engine = EcmaScriptInterpreter.getInstance().getEngine();
		assertNotNull(engine);
		String result = instance.interpret("1+2");
		assertEquals(result, "3");
		instance.addJavaMethods("getZero", "{return 0;}");
		Map<String, Object> params = instance.getParams();
		assertNotNull(params);
		instance.setParams(null);
		Object result2 = instance.interpretAndReturnAsIs("1+2");
		assertNotNull(result2);
	}

}
