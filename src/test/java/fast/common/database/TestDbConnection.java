package fast.common.database;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.Properties;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

@RunWith(PowerMockRunner.class)
public class TestDbConnection {

	private DbConnection testConnection;
	private final DatabaseType dbType = DatabaseType.DB2;
	private final String hostName = "0.0.0.1";
	private final int port = 100;
	private final String databaseName = "test_db";
	private final String username = "test_user";
	private final String password = "12345678";
	private final boolean isDBService = true;
	private static Properties props = new Properties();

	@Before
	public void setUp() throws Exception {
		testConnection = new DbConnection(dbType, hostName, port, databaseName, username, password, isDBService);
		props.put("user", username);
		props.put("password", password);
	}

	@Test
	public void testBuildConnectionString_Sybase() throws Exception {
		DbConnection dbConn1 = new DbConnection(DatabaseType.SYBASE, "host", 2018, "TestDB");
		String result1 = Whitebox.invokeMethod(dbConn1, "buildConnectionString");
		assertEquals("jdbc:jtds:sybase://host:2018;databaseName=TestDB;", result1);

		DbConnection dbConn2 = new DbConnection(DatabaseType.SYBASE, "host", 2018, "TestDB");
		dbConn2.setOptionalParameters(null, true, "JCE Provider");
		String result2 = Whitebox.invokeMethod(dbConn2, "buildConnectionString");
		assertEquals("jdbc:sybase:Tds:host:2018", result2);
	}

	@Test
	public void testBuildConnectionString_SqlServer() throws Exception {
		DbConnection dbConn1 = new DbConnection(DatabaseType.SQLSERVER, "host", 2018, "TestDB", "user", "password");
		String result1 = Whitebox.invokeMethod(dbConn1, "buildConnectionString");
		assertEquals("jdbc:sqlserver://host:2018;databaseName=TestDB;", result1);

		DbConnection dbConn2 = new DbConnection(DatabaseType.SQLSERVER, "host", 2018, "TestDB");
		String result2 = Whitebox.invokeMethod(dbConn2, "buildConnectionString");
		assertEquals("jdbc:sqlserver://host:2018;databaseName=TestDB;integratedSecurity=true;", result2);

		DbConnection dbConn3 = new DbConnection(DatabaseType.SQLSERVER, "host", 2018, "TestDB", "user", "password");
		dbConn3.setOptionalParameters("TestInstance", false, "");
		String result3 = Whitebox.invokeMethod(dbConn3, "buildConnectionString");
		assertEquals("jdbc:sqlserver://host:2018;databaseName=TestDB;instance=TestInstance;", result3);

		DbConnection dbConn4 = new DbConnection(DatabaseType.SQLSERVER, "host", 2018, "TestDB");
		dbConn4.setOptionalParameters("TestInstance", false, "");
		String result4 = Whitebox.invokeMethod(dbConn4, "buildConnectionString");
		assertEquals("jdbc:sqlserver://host:2018;databaseName=TestDB;instance=TestInstance;integratedSecurity=true;",
				result4);
	}

	/**
	 * Oracle connection using SID or service (cloud service) Provide an EXTRA
	 * config parameter for connecting as service 'serviceName: true' else if no
	 * config key provided for service, it will take connection as SID
	 * 
	 * @throws Exception
	 */
	@Test
	public void testOracleDBConnectionService() throws Exception {
		// ServiceName
		DbConnection dbConn4 = new DbConnection(DatabaseType.ORACLE, "hostname", 2018, "databaseName", "username",
				"password", true);
		String result1 = Whitebox.invokeMethod(dbConn4, "buildConnectionString");
		assertEquals("jdbc:oracle:thin:@//hostname:2018/databaseName", result1);

		// SID
		dbConn4 = new DbConnection(DatabaseType.ORACLE, "hostname", 2018, "databaseName", "username", "password",
				false);
		result1 = Whitebox.invokeMethod(dbConn4, "buildConnectionString");
		assertEquals("jdbc:oracle:thin:@hostname:2018:databaseName", result1);
	}

	@Test
	public void checkDbConnectionDatabaseType() throws Exception {
		DatabaseType expected = dbType;
		DatabaseType actual = testConnection.getDatabaseType();
		assertEquals(expected, actual);
	}

	@Test
	public void checkDbConnectionHostName() throws Exception {
		String expected = hostName;
		String actual = testConnection.getHostName();
		assertEquals(expected, actual);
	}

	@Test
	public void checkDbConnectionPort() throws Exception {
		int expected = port;
		int actual = testConnection.getPort();
		assertEquals(expected, actual);
	}

	@Test
	public void checkDbConnectionDatabaseName() throws Exception {
		String expected = databaseName;
		String actual = testConnection.getDatbaseName();
		assertEquals(expected, actual);
	}

	@Test
	public void checkDbConnectionUserName() throws Exception {
		String expected = username;
		String actual = testConnection.getUsername();
		assertEquals(expected, actual);
	}

	@Test
	public void checkDbConnectionPassword() throws Exception {
		String expected = password;
		String actual = testConnection.getPassword();
		assertEquals(expected, actual);
	}

	@Test
	public void openTestConnectionWithInvalidParametersShouldReturnFalseAndThrowException() throws Exception {

		boolean connected = false;
		try {
			connected = testConnection.Open();
		} catch (Exception e) {
		}

		boolean expected = false;
		assertEquals(expected, connected);
	}

	@Test
	public void getJdbcConnectionStringFromMockDbConnection() throws Exception {
		try {
			testConnection.Open();
		} catch (Exception e) {
		}
		String actual = testConnection.getJdbcConnectionString();
		String expected = "jdbc:db2://0.0.0.1:100/test_db";
		assertEquals(expected, actual);
	}

	@Test
	public void queryWithTestConnectionShouldReturnNull() throws Exception {
		String sql = "select * from test";
		Object actual = null;
		try {
			actual = testConnection.query(sql);
		} catch (Exception e) {
		}
		assertNull(actual);
	}

	@Test
	public void updateWithTestConnectionShouldReturnZero() throws Exception {
		String sql = "update test set name = 'ABC' where id = 0";
		int actual = 0;
		try {
			actual = testConnection.update(sql);
		} catch (Exception e) {
		}
		int expected = 0;
		assertEquals(expected, actual);
	}
}
