package fast.common.agents;

import fast.common.core.Configurator;
import fast.common.gmdReplay.GMDReplayDataHelper;
import fast.common.logging.FastLogger;

import org.apache.commons.lang3.time.FastDateFormat;

import java.io.File;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by ab56783 on 08/18/2017.
 */
public class DataRecorderAgent extends Agent implements IDataProcessAgent {
    private final FastLogger _logger;
    private Connection _sqlConnection;
    private final ReentrantLock _recordingLock = new ReentrantLock();
    private static final FastDateFormat tsFormatMilliseconds;
    static {
        tsFormatMilliseconds = FastDateFormat.getInstance("yyyy-MM-dd HH:mm:ss.SSS z", TimeZone.getTimeZone("UTC"));
    }

    public DataRecorderAgent(String name, Map agentParams, Configurator configurator) {
        super(name, agentParams, configurator);
        _logger = FastLogger.getLogger(String.format("%s:DataRecorder", _name));
        try {
            initRecording(agentParams.get("database").toString(),agentParams.get("access_prefix").toString());
        } catch (SQLException e) {
            _logger.error("Failed to initiate Data Recorder, stack trace:\n"+e.getMessage());
        }
    }

    private void initRecording(String file, String schema) throws SQLException  {
        DateFormat df = new SimpleDateFormat("yyyyMMddHHmm");
        df.setTimeZone(TimeZone.getTimeZone("UTC"));
        PreparedStatement pstmt = null;
        String databaseName = org.apache.commons.io.FilenameUtils.removeExtension(file);
        databaseName = schema + databaseName + df.format(new Date()) + ".db";
        String sqlQuery1 = "CREATE TABLE IF NOT EXISTS object_data (id INTEGER PRIMARY KEY ASC, ts DATETIME, name TEXT, symbol TEXT,data TEXT);";
        String sqlQuery2 = "CREATE TABLE IF NOT EXISTS properties (id INTEGER PRIMARY KEY ASC, key TEXT, value TEXT);";
  
        try {
			File db = new File(file);
			if (db.exists())
				Files.delete(db.toPath());
			Class.forName("org.sqlite.JDBC");
			_sqlConnection = DriverManager.getConnection(databaseName);
			pstmt  = _sqlConnection.prepareStatement(sqlQuery1);
			pstmt.executeUpdate();
			if(pstmt != null) pstmt.close();
			pstmt  = _sqlConnection.prepareStatement(sqlQuery2);
			pstmt.executeUpdate();
		} catch (Exception e) {
			_logger.error("Failed to creates a prepared statement, stack trace:\n" + e.getMessage());
		}finally {
            if(pstmt != null) pstmt.close();
        }
    }

    public void processData(String name, String symbol, Object obj) {
        String data = GMDReplayDataHelper.gson.toJson(obj);
        PreparedStatement pstmt = null;
        String sqlQuery = "INSERT INTO object_data (ts, name, symbol, data) VALUES (?,?,?,?)";
        _recordingLock.lock();
        try {
            pstmt = _sqlConnection.prepareStatement(sqlQuery);
            pstmt.setString(1, tsFormatMilliseconds.format(new Date()));
            pstmt.setString(2, name);
            pstmt.setString(3, symbol);
            pstmt.setString(4, data);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            //Oops, something went very wrong...
            _logger.error(String.format("Encountered problem with GC SQLite DB: %s", e.getMessage()));
          
        } finally {
            try {
            	if(pstmt!=null)
            		pstmt.close();
            } catch (SQLException e) {
                _logger.error(String.format("Encountered problem with close statementd: %s", e.getMessage()));
              
            }
            _recordingLock.unlock();
        }
    }

    @Override
    public void setMDspeed(int speed) {
        GMDReplayDataHelper.writeMDspeed(speed, _sqlConnection);
    }

    @Override
    public void close() throws Exception {
        if(_sqlConnection != null) {
            _sqlConnection.close();
            _sqlConnection = null;
        }
    }
}
