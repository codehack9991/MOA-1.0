package fast.common.agents;

import fast.common.core.Configurator;
import fast.common.gmdReplay.GMDReplayDataHelper;
import fast.common.logging.FastLogger;

import java.io.File;
import java.nio.file.Files;
import java.sql.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by ab56783 on 08/22/2017.
 */
public class DataComparatorAgent extends Agent implements IDataProcessAgent {
    private final FastLogger _logger;
    private Connection _sqlConnection;
    private Connection _sqlResultConnection;
    private GMDReplayDataHelper _dataHelper;
    private int _mode;

    public DataComparatorAgent(){
    	_logger = FastLogger.getLogger(String.format("%s:DataComparator", "emptyConstructor"));
    }
    
    public int get_mode() {
		return _mode;
	}

	public void set_mode(int _mode) {
		this._mode = _mode;
	}

	public DataComparatorAgent(String name, Map agentParams, Configurator configurator) {
        super(name, agentParams, configurator);
        _mode = Integer.parseInt(agentParams.get("mode").toString());
        _logger = FastLogger.getLogger(String.format("%s:DataComparator", _name));
        int dev_lim = Integer.parseInt(agentParams.get("msg_seq_dev_limit").toString());
        int dev_time = Integer.parseInt(agentParams.get("msg_dev_time").toString());
        int wait_time = Integer.parseInt(agentParams.get("msg_wait_time").toString());
        boolean async_mode = Boolean.parseBoolean(agentParams.get("async_mode").toString());
        try {
            String mode = agentParams.get("data_type").toString();
            Map rules = (Map) agentParams.get("rules");
            if(mode.equals("GMD")) {
                _dataHelper = new GMDReplayDataHelper(rules, dev_lim, dev_time, wait_time, async_mode);
            }
            else {
                //Should never come here
                throw new Exception("Unexpected");
            }
            _sqlConnection = initDBconnection(agentParams.get("golden_db").toString(),agentParams.get("access_prefix").toString(), false);
            if(_mode == 0) {
                _dataHelper.loadOriginalData(_sqlConnection);
            }
            DateFormat df = new SimpleDateFormat("yyyyMMddHHmm");
            df.setTimeZone(TimeZone.getTimeZone("UTC"));
            String DB_name = org.apache.commons.io.FilenameUtils.removeExtension(agentParams.get("result_db").toString()) + df.format(new java.util.Date()) + ".db";
            _sqlResultConnection = initDBconnection(DB_name,agentParams.get("access_prefix").toString(), true);
            _dataHelper.initReporting(_sqlResultConnection, agentParams.get("golden_db").toString());
        } catch (Exception e) {
			_logger.error("Failed to initiate Data Comparator. " + e.getMessage());

        }finally{
        	if(_sqlConnection != null) {
                try {
                    _sqlConnection.close();
                } catch (SQLException e1) {
                    _logger.error("Failed to close sql connection. "+e1.getMessage());
                } finally {
                    _sqlConnection = null;
                }
            }
            if(_sqlResultConnection != null) {
                try {
                    _sqlResultConnection.close();
                } catch (SQLException e1) {
                    _logger.error("Failed to close sql result connection. "+e1.getMessage());
                } finally {
                    _sqlResultConnection = null;
                }
            }
        }
    }

    private Connection initDBconnection(String file, String schema, boolean create_new) throws Exception {
        String databaseName = schema + file;
        File db = new File(file);
        if(!create_new) {
            if (!db.exists()) {
                throw new Exception(String.format("Cannot find DB file '%s'", file));
            }
        }
        else if (db.exists()) {
        	Files.delete(db.toPath());
        }
        Class.forName("org.sqlite.JDBC");
        return DriverManager.getConnection(databaseName);
    }

    public void waitForFinish() {
        _dataHelper.waitForReplayFinish();
    }

    public void processData(String name, String symbol, Object obj) {
		switch (_mode) {
		default:
		case 0:
			_dataHelper.putDataToQueue(null, name, symbol, obj);
			break;
		case 1:
			_dataHelper.putDataToQueue(_sqlConnection, name, symbol, obj);
			break;
		}
    }

    @Override
    public void setMDspeed(int speed) {
        _dataHelper.setMDspeed(speed);
        GMDReplayDataHelper.writeMDspeed(speed, _sqlResultConnection);
    }

    @Override
    public void close() throws Exception {
        close_connection(_sqlConnection);
        _sqlConnection = null;
        close_connection(_sqlResultConnection);
        _sqlResultConnection = null;
    }

    private void close_connection(Connection conn) throws Exception {
        if(conn != null) {
            conn.close();
        }
    }
}
