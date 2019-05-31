package fast.common.replay;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;

public class TestAction {

	@Test
	public void test() {
		Action value = Action.SEND;
		assertNotNull(value);
	}

}
