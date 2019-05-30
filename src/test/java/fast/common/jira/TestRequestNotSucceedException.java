package fast.common.jira;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;

public class TestRequestNotSucceedException {

	@Test
	public void testRequestNotSucceedException() {
		RequestNotSucceedException requestNotSucceedException1 = new RequestNotSucceedException();
		RequestNotSucceedException requestNotSucceedException2 = new RequestNotSucceedException("message");
		RequestNotSucceedException requestNotSucceedException3 = new RequestNotSucceedException(new Throwable("exception"));
		RequestNotSucceedException requestNotSucceedException4 = new RequestNotSucceedException("message", new Throwable("exception"));
		RequestNotSucceedException requestNotSucceedException5 = new RequestNotSucceedException("message", new Throwable("exception"), false, false);
		assertNotNull(requestNotSucceedException1);
		assertNotNull(requestNotSucceedException2);
		assertNotNull(requestNotSucceedException3);
		assertNotNull(requestNotSucceedException4);
		assertNotNull(requestNotSucceedException5);
	}

}
