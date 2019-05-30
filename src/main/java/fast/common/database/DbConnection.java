package fast.common.database;

import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import fast.common.context.DataTable;
import fast.common.logging.FastLogger;

public class DbConnection {	
	public static final String OPTIONAL_PARAM_INSTANCE = "instance";
	public static final String OPTIONAL_PARAM_ENCRYPT_PASSWARD = "encryptPassword";
	public static final String OPTIONAL_PARAM_JECPROVIDER_CLASS = "jceProviderClass";
	
	private static FastLogger logger = FastLogger.getLogger(DbConnection.class.getName());
	private DatabaseType databaseType;
	private String hostName;
	private int port;
	private String databaseName;
	private String username;
	private String password;
	private String jdbcConnectionUrl;
	private Properties props = new Properties();
	private String instance;
	private String driver;	
	private boolean encryptPassword = false;
	private String jceProviderClass = null;
	private boolean connected = false;
	private java.sql.Connection conn;
	private boolean connectAsService;
	
	public DbConnection(DatabaseType databaseType, String hostName, int port, String databaseName) {
		this.databaseType = databaseType;
		this.hostName = hostName;
		this.port = port;
		this.databaseName = databaseName;		
	}
	
	public DbConnection(DatabaseType databaseType, String hostName, int port, String databaseName, String username,
			String password) {
		this(databaseType, hostName, port, databaseName);		
		this.username = username;
		this.password = password;
		props.put("user", username);
		props.put("password", password);
	}
	
	/**
	 * @param databaseType - ORACLE
	 * @param hostName - db hostname
	 * @param port
	 * @param databaseName - Service Name value
	 * @param username
	 * @param password
	 * @param isDBService - if connection uses service name to connect, provide true
	 */
	public DbConnection(DatabaseType databaseType, String hostName, int port, String databaseName, String username,
			String password, boolean isDBService) {
		this(databaseType, hostName, port, databaseName);		
		this.username = username;
		this.password = password;
		this.connectAsService = isDBService;
		props.put("user", username);
		props.put("password", password);
	}
	
	public void setOptionalParameters(String instance, boolean encryptPassword, String jceProviderClass){
		this.instance = instance;
		this.encryptPassword = encryptPassword;
		this.jceProviderClass = jceProviderClass;
	}

	public boolean Open() throws SQLException, ClassNotFoundException  {
		if(connected && conn != null && !conn.isClosed()){
			return connected;
		}

		if(this.jdbcConnectionUrl == null){
			this.jdbcConnectionUrl = buildConnectionString();
		}
		Class.forName(this.driver);
		conn = DriverManager.getConnection(this.jdbcConnectionUrl, props);				
		
		connected = true;
		return connected;
	}

	public void Close() throws SQLException  {
		if (conn != null && connected) {
			conn.close();
			connected = false;
		}
	}
	
	/**
	 * Execute query operation with sql string
	 * @param sql
	 * @return Result set
	 * @throws SQLException 
	 * @throws SQLException
	 */
	public DataTable query(String sql) throws SQLException {		
		return query(sql,1,1,-1);
	}	
	
	
	/**
	 * Execute query operation with sql string,specify a particular result set to be returned
	 * @param sql
	 * @param resultIndex, the index of the ResultSets that you want to get. starts from 1.
	 * @param startIndex, the start index of the ResultSet. starts from 1.  
	 * @param endIndex, the end index of the ResultSet.  
	 * @return Result set
	 * @throws SQLException 
	 */
	public DataTable query(String sql,int resultIndex, int startIndex,int endIndex) throws SQLException {
			Statement stmt = null;
			ResultSet rs = null;
			SQLException ex = null;
			
			DataTable resultDataTable = new DataTable();
			
			if(startIndex<1||(endIndex!=-1&&endIndex<startIndex))
				throw new IndexOutOfBoundsException(String.format("startIndex: %d or endIndex: %d invalid!", startIndex,endIndex));
			try {
				stmt = conn.createStatement();
				boolean isResultSet = stmt.execute(sql);
				for(int i=resultIndex;i>1;i--)
				{
					isResultSet=stmt.getMoreResults();
				}
				if(!isResultSet)
					throw new SQLException(String.format("The %d result is not a ResultSet", resultIndex));
				rs=stmt.getResultSet();		
				ResultSetMetaData metadata = rs.getMetaData();			
				int columnCount = metadata.getColumnCount();
				if (columnCount > 0) {
					ArrayList<List<Object>> rows = new ArrayList<List<Object>>();
					int currentRow=1;
					while (rs.next()) {
						if(endIndex==-1||(currentRow>=startIndex&&currentRow<=endIndex))
						{
							ArrayList<Object> row = new ArrayList<>();
							for (int rowIndex = 1; rowIndex <= metadata.getColumnCount(); ++rowIndex) {
								Object value = rs.getObject(rowIndex) == null ? null : rs.getObject(rowIndex);
								row.add(value);
							}
							rows.add(row);
						}
						currentRow++;
					}	
					ArrayList<String> header = new ArrayList<>();
					for (int columnIndex = 0; columnIndex < columnCount; columnIndex++) {//						
						String columnName = metadata.getColumnName(columnIndex+1);
						header.add(columnName);
					}
					resultDataTable = new DataTable(header,rows);
				}
			} catch (SQLException e) {
				ex = e;
			} finally {
				if (rs !=null) rs.close();
				if (stmt !=null) stmt.close();			
			}
			
			if(ex != null){
				logger.warn(String.format("Error occurred when running SQL : %s, cause: %s", sql, ex.getMessage()));
				throw ex;
			}
			
			return resultDataTable;
		}		
	
	
	/**
	 * Execute insert or update operation with SQL string
	 * @param sql
	 * @return affected rows count
	 * @throws SQLException
	 */
	public int update(String sql) throws SQLException {
		int affectedRows = 0;
		Statement statement = null;
		SQLException ex = null; 
		try {
			statement = conn.createStatement();
			affectedRows = statement.executeUpdate(sql,
					Statement.RETURN_GENERATED_KEYS);
		} catch (SQLException e) {
			ex = e;			
		} finally {
			if (statement != null) {
				statement.close();
			}
		}
		
		if(ex != null){
			logger.warn(String.format("Error occurred when running SQL : %s, cause: %s", sql, ex.getMessage()));
			throw ex;
		}
		
		return affectedRows;
	}

	private String buildConnectionString() {
		String connectionString = "";
		switch (this.databaseType) {
		case ORACLE:
			if(connectAsService)
				connectionString = String.format("jdbc:oracle:thin:@//%s:%d/%s", this.hostName, this.port, this.databaseName);

			else 
				connectionString = "jdbc:oracle:thin:@" + this.hostName + ":" + this.port + ":" + this.databaseName;
			
			this.driver = "oracle.jdbc.driver.OracleDriver";
			
			break;
		case SYBASE:
			if(encryptPassword){
				//connect to Sybase with password encrypted, see more @ http://infocenter.sybase.com/help/index.jsp?topic=/com.sybase.infocenter.dc39001.0605/html/prjdbc/CIHGFHDI.htm
				this.driver = "com.sybase.jdbc4.jdbc.SybDriver";				
				connectionString = String.format("jdbc:sybase:Tds:%s:%d", hostName, port);				
				props.put("ENCRYPT_PASSWORD", String.valueOf(encryptPassword));
				props.put("JCE_PROVIDER_CLASS", jceProviderClass);	
				props.put("DATABASE", databaseName);
			}
			else {
				connectionString = String.format("jdbc:jtds:sybase://%s:%d;databaseName=%s;", this.hostName, this.port, this.databaseName);
				this.driver = "net.sourceforge.jtds.jdbc.Driver";
			}			
			break;
		case SQLSERVER:
			connectionString = String.format("jdbc:sqlserver://%s:%d;databaseName=%s;%s%s", this.hostName, this.port, this.databaseName,
					instance != null ? "instance=" + instance + ";" : "", username != null && password != null ? "" : "integratedSecurity=true;");			
			this.driver = "com.microsoft.sqlserver.jdbc.SQLServerDriver";
			break;
		case DB2:
			connectionString = "jdbc:db2://" + this.hostName + ":" + this.port + "/" + this.databaseName;
			this.driver = "com.ibm.db2.jcc.DB2Driver";
			break;
		default:
			break;
		}
		return connectionString;
	}

	public DatabaseType getDatabaseType() {
		return databaseType;
	}

	public String getHostName() {
		return hostName;
	}

	public int getPort() {
		return port;
	}

	public String getDatbaseName() {
		return databaseName;
	}

	public String getUsername() {
		return username;
	}

	public String getPassword() {
		return password;
	}

	public String getJdbcConnectionString() {
		return jdbcConnectionUrl;
	}
}

enum DatabaseType {
	ORACLE, SYBASE, SQLSERVER, DB2
}
