package fast.common.jira;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;

public class TestJiraTransactionException {

	@Test
	public void testJiraTransactionException() {
		JiraTransactionException jiraTransactionException1 = new JiraTransactionException();
		JiraTransactionException jiraTransactionException2 = new JiraTransactionException("message");
		JiraTransactionException jiraTransactionException3 = new JiraTransactionException(new Throwable("Exception"));
		JiraTransactionException jiraTransactionException4 = new JiraTransactionException("message", new Throwable("Exception"));
		JiraTransactionException jiraTransactionException5 = new JiraTransactionException("message", new Throwable("Exception"), false, false);
		assertNotNull(jiraTransactionException1);
		assertNotNull(jiraTransactionException2);
		assertNotNull(jiraTransactionException3);
		assertNotNull(jiraTransactionException4);
		assertNotNull(jiraTransactionException5);
	}

}
