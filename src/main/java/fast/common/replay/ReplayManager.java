package fast.common.replay;

import co.paralleluniverse.fibers.Suspendable;
import co.paralleluniverse.strands.concurrent.CountDownLatch;
import co.paralleluniverse.strands.concurrent.ReentrantLock;
import co.paralleluniverse.strands.concurrent.Semaphore;
import fast.common.logging.FastLogger;
import org.apache.commons.lang3.time.FastDateFormat;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.sql.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Date;
import java.util.concurrent.TimeUnit;

//import java.util.concurrent.CountDownLatch;


// TODO: refactor code: extract SQL work
// TODO: refactor code: merge ReplayManager & ReplayAgent


public class ReplayManager {
    public Map _replayParams;
    private String _sqlQuery;
    private String _configFolder;

    static FastLogger logger = FastLogger.getLogger("ReplayManager");
    public CountDownLatch countDownLatch;
    private Semaphore sendPermission = null;
    private Semaphore scenarioPermission = null;
    public float speed = 1.0f;

    private HashMap<String, ConnectionFactory> _connectionFactories = new HashMap<>();
    private HashMap<String, ReplayScenario> _scenarios = new HashMap<>();
    public Date startDateTime;
    public Date endDateTime;
    public Date actualStartDateTime;
    public MiniFixHelper miniFixHelper;
    ReentrantLock _reportingLock = new ReentrantLock();
    private Connection _sqlConnectionForReporting;
    private ArrayList<String> errorTypesForLogging= new ArrayList<>();
    private HashMap<String, ReplayRunner> _aggregatedRunners = new HashMap<>();
    private Integer _syncType;
    private ArrayList<String> _syncTargets = new ArrayList<>();

    public int _numTries;

    private boolean sendWaitTO = false;
    private boolean enableSync = false;

    public ReplayMgmtCenter PrtyScen = null;


    public ReplayManager(Map replayParams, String configFolder, String recordName, String sqlQuery) throws Exception {
        _replayParams = replayParams;
        _sqlQuery = sqlQuery;
        _configFolder = configFolder;
        miniFixHelper = new MiniFixHelper(_replayParams); // used to store tags mapping globally acros all scenarios and connections. this instance is passed to every connection

    }

    public void cleanup(HashMap<String, ReplayScenario> scenarios) {
        try {
            Thread.sleep(10000l);
        } catch (InterruptedException e) {
        	Thread.currentThread().interrupt();
        }
        for(ReplayScenario scenario: scenarios.values()) {
            scenario.cleanup();
        }

        for(ConnectionFactory connectionFactory: _connectionFactories.values()) {
            ArrayList<MessagesOutOfScope_ReplayException> exceptions = connectionFactory.cleanup();
            if(exceptions != null) {
                for (MessagesOutOfScope_ReplayException exception : exceptions) {
                    //(String side, String connection, String message, String result, Exception exception)
                    writeOutOfScenarioReport("UNEXPECTED", exception, scenarios);
                }
            }
        }
        scenarios.clear();
        _scenarios.clear();
        _connectionFactories.clear();

        writeReportLastPart();
    }

    @Suspendable
    private void writeReportLastPart() {
        try {
            if (_sqlConnectionForReporting != null) {
                PreparedStatement pstmt = null;
                String sqlQuery3 = "INSERT INTO properties (key, value) VALUES (?,?),(?,?),(?,?);";
                String sqlQuery4 = "INSERT INTO error_types (id, text) VALUES (?,?);";
                _reportingLock.lock();
                try {
                    pstmt = _sqlConnectionForReporting.prepareStatement(sqlQuery3);
                    pstmt.setString(1, "Replay_start");
                    pstmt.setString(2, tsFormatMilliseconds.format(actualStartDateTime != null ? actualStartDateTime : new Date()));
                    pstmt.setString(3, "Replay_speed");
                    pstmt.setString(4, String.valueOf(speed));
                    pstmt.setString(5, "Replay_end");
                    pstmt.setString(6, tsFormatMilliseconds.format(new Date()));
                    pstmt.executeUpdate();
                    if(pstmt != null) pstmt.close();
                    pstmt = _sqlConnectionForReporting.prepareStatement(sqlQuery4);
                    for(Integer i=0; i<errorTypesForLogging.size();i++) {
                        pstmt.setInt(1, i);
                        pstmt.setString(2, errorTypesForLogging.get(i));
                        pstmt.addBatch();
                        //pstmt.clearParameters();
                    }
                    pstmt.executeBatch();
                    //_sqlConnectionForReporting.commit();
                } catch (SQLException e) {
                    //Oops, something went very wrong...
                    logger.error(String.format("Encountered problem with report SQLite DB: %s", e.toString()));
                } finally {
                    try {
                    	if (pstmt !=null)
                    		pstmt.close();
                    } catch (SQLException e) {
                        logger.error(String.format("Encountered problem with report SQLite DB: %s", e.toString()));
                    }
                    _reportingLock.unlock();
                }
                _sqlConnectionForReporting.close();
                _sqlConnectionForReporting = null;
            }
        } catch (SQLException e) {
            logger.error(e.toString());
        }
    }

    private void loadScenarios() throws Exception {
//        Connection c = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        ArrayList<ReplayStep> result = new ArrayList<>();
        SimpleDateFormat tsFormatSeconds = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); // 2017-04-24 05:51:27
        SimpleDateFormat tsFormatMilliseconds = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS"); // 2017-04-24 05:51:27.123
        tsFormatSeconds.setTimeZone(TimeZone.getTimeZone("UTC"));
        tsFormatMilliseconds.setTimeZone(TimeZone.getTimeZone("UTC"));

        java.util.Date minTs = new Date(); // now
        java.util.Date maxTs = new Date(0L); // start of time

        String databaseName = _replayParams.get("Database").toString();

        if(_replayParams.containsKey("selectQuery")) {
            _sqlQuery =  _replayParams.get("selectQuery").toString();
        }

        try (Connection c = DriverManager.getConnection(databaseName)){
            Class.forName("org.sqlite.JDBC");
            if(_sqlQuery == null) { // load all scenarios
                String sqlQuery = "SELECT * FROM steps ORDER BY scenario, ts, action DESC";
                pstmt  = c.prepareStatement(sqlQuery);
            }
            else {
                String sqlQuery = _sqlQuery;
                pstmt  = c.prepareStatement(sqlQuery);
            }

            //logger.info(String.format("SQL query: '%s'", pstmt.toString()));
            rs = pstmt.executeQuery();

            // CREATE TABLE messages (int id, recording text, ts DATETIME, scenario text, side text, connection text, action text, data text, filename text)
            String currentScenarioName = null;
            ReplayScenario currentScenario = null;
            boolean priority_scenario = false;
            int i = 0;
            ReplayStep step = null;
            ReplaySyncPoint point = null;
            while ( rs.next() ) {
                i++;
                String scenarioName = rs.getString("scenario");
                String connection = rs.getString("connection");
                if(scenarioName == null) {// TODO: add exception here - and address all such cases in logs parser
                    throw new Exception(String.format("Scenario name is null, row #%d", i));
                }

                if(!scenarioName.equals(currentScenarioName)) {
                    currentScenarioName = scenarioName;

                    if(scenarioName.equals("EXEC_PRIORITY")) {
                        PrtyScen = new ReplayMgmtCenter(this, scenarioName);
                        //currentScenario = PrtyScen;
                        priority_scenario = true;
                    }
                    else {
                        currentScenario = new ReplayScenario(this, scenarioName, connection);
                        priority_scenario = false;
                        _scenarios.put(scenarioName, currentScenario);
                    }
                }


                String tsString = rs.getString("ts");

                // parse timestamp from database  format: 2017-04-24 09:49:12.917000
                int pos = tsString.indexOf(".");
                Date ts;
                if(pos < 0) {
                    ts = tsFormatSeconds.parse(tsString); // 2017-04-24 09:49:12
                }
                else {
                    int subslen = (tsString.length() - pos) > 3 ? (pos + 4) : tsString.length();
                    tsString = tsString.substring(0,subslen);
                    ts = tsFormatMilliseconds.parse(tsString); // 2017-04-24 09:49:12.917
                }

                String side = rs.getString("side");
                String action = rs.getString("action");
                String data = rs.getString("data");

                if(priority_scenario){
                    point = new ReplaySyncPoint(PrtyScen, ts, side, connection, action, data);
                    PrtyScen.allSyncPoints.add(point);
                    action = action.contains("SEND") ? "SEND" : action;
                }
                else {
                    step = new ReplayStep(currentScenario, ts, side, connection, action, data);
                    currentScenario.steps.add(step);
                }


                // inititlize all connection objects but not start them
                ConnectionFactory connFactory = getOrCreateConnectionFactory(side);
                connFactory.getOrCreateConnection(connection, action);

                if((!priority_scenario) && (step != null)) {
                    if (step.ts.after(maxTs))
                        maxTs = step.ts;
                    if (step.ts.before(minTs))
                        minTs = step.ts;
                }
                step = null;
                point = null;
            }

            logger.info(String.format("Read %d steps, divided them into %d scenarios", i, _scenarios.size()));
        }
        finally {
			if (rs != null)
				rs.close();
			if (pstmt != null)
				pstmt.close();
        }




        startDateTime = minTs;
        endDateTime = maxTs;
    }

    private void initReporting() throws Exception {
        DateFormat df = new SimpleDateFormat("yyyyMMddHHmm");
        df.setTimeZone(TimeZone.getTimeZone("UTC"));
        PreparedStatement pstmt = null;
        String databaseName = _replayParams.get("Database").toString();
        databaseName = org.apache.commons.io.FilenameUtils.removeExtension(databaseName);
        databaseName = databaseName + "_report" + ".db";
        String sqlQuery1 = "CREATE TABLE IF NOT EXISTS step_results(id INTEGER PRIMARY KEY ASC, ts DATETIME, exects DATETIME, scenario TEXT, side TEXT, connection TEXT, action TEXT, data TEXT, result TEXT, details TEXT, errno TEXT);";
        String sqlQuery2 = "CREATE TABLE IF NOT EXISTS properties(id INTEGER PRIMARY KEY ASC, key TEXT, value TEXT);";
        String sqlQuery3 = "INSERT INTO properties (key, value) VALUES (?,?),(?,?),(?,?);";
        String sqlQuery4 = "CREATE TABLE IF NOT EXISTS error_types (id INTEGER PRIMARY KEY ASC, text TEXT);";
        try {
            File db = new File(databaseName.replace("jdbc:sqlite:", ""));
            if(db.exists()) db.delete();
            Class.forName("org.sqlite.JDBC");
            _sqlConnectionForReporting = DriverManager.getConnection(databaseName);
            pstmt  = _sqlConnectionForReporting.prepareStatement(sqlQuery1);
            pstmt.executeUpdate();
            if(pstmt != null) pstmt.close();
            pstmt  = _sqlConnectionForReporting.prepareStatement(sqlQuery2);
            pstmt.executeUpdate();
            if(pstmt != null) pstmt.close();
            pstmt  = _sqlConnectionForReporting.prepareStatement(sqlQuery3);
            pstmt.setString(1, "Scenario_file");
            pstmt.setString(2, _replayParams.get("Database").toString());
            pstmt.setString(3, "Scenario_start");
            pstmt.setString(4, tsFormatMilliseconds.format(startDateTime));
            pstmt.setString(5, "Scenario_end");
            pstmt.setString(6, tsFormatMilliseconds.format(endDateTime));
            pstmt.executeUpdate();
            if(pstmt != null) pstmt.close();
            pstmt  = _sqlConnectionForReporting.prepareStatement(sqlQuery4);
            pstmt.executeUpdate();
        }
        finally {
            if(pstmt != null) pstmt.close();
        }
    }


    // external entry point
    public void runAll() throws Exception {
        if(_replayParams.containsKey("speed")) {
            speed = Float.parseFloat(_replayParams.get("speed").toString());
        }
        else {
            speed = 500.0f;
        }
        if(_replayParams.containsKey("maxReceiveTime")) {
            _numTries = Integer.parseInt(_replayParams.get("maxReceiveTime").toString());
        }
        else {
            _numTries = 10;
        }
        if(_replayParams.containsKey("maxConcurrentSends")) {
            sendPermission =  new Semaphore(Integer.parseInt(_replayParams.get("maxConcurrentSends").toString()), true);
        }
        else {
            sendPermission = null;
        }
        if(_replayParams.containsKey("sendWaitTO")) {
            sendWaitTO =  Boolean.parseBoolean(_replayParams.get("sendWaitTO").toString());
        }
        else {
            sendWaitTO = false;
        }
        if(_replayParams.containsKey("maxConcurrentScenarios")) {
            scenarioPermission =  new Semaphore(Integer.parseInt(_replayParams.get("maxConcurrentScenarios").toString()));
        }
        else {
            scenarioPermission = null;
        }
        if(_replayParams.containsKey("enableSync")) {
            enableSync =  Boolean.parseBoolean(_replayParams.get("enableSync").toString());
        }
        else {
            enableSync = false;
        }
        if(_replayParams.containsKey("scenarioSyncType") && _replayParams.containsKey("scenarioSyncTarget")) {
            _syncType =  Integer.parseInt(_replayParams.get("scenarioSyncType").toString());
            _syncTargets = (ArrayList)_replayParams.get("scenarioSyncTarget");
        }
        else {
            _syncType = -1;
        }
        loadScenarios();
        if(_replayParams.containsKey("scenarioName")) {
            ArrayList arrayList = (ArrayList)_replayParams.get("scenarioName");
            if(arrayList.size() > 0) {
                HashMap<String, ReplayScenario> scenarios = new HashMap<String, ReplayScenario>();
                for(Object sname : arrayList) {
                    ReplayScenario scenario = _scenarios.get(sname.toString());
                    if(scenario == null) {
                        throw new RuntimeException(String.format("Can't find scenario '%s'", sname.toString()));
                    }
                    scenarios.put(sname.toString(), scenario);
                }
                if(arrayList.size() == 1) {
                    speed = 0.0f;
                }
                runScenarios(scenarios);
                return;
            }
        }
        runScenarios(_scenarios);
    }

    // external entry point
    public void runOneScenario(String scenarioName) throws Exception {
        speed = 0.0f;
        loadScenarios(); // load _scenarios
        ReplayScenario scenario = _scenarios.get(scenarioName);
        if(scenario == null) {
            throw new RuntimeException(String.format("Can't find scenario '%s'", scenarioName));
        }

        HashMap<String, ReplayScenario> scenarios = new HashMap<String, ReplayScenario>();
        scenarios.put(scenarioName, scenario);
        runScenarios(scenarios);
    }

    //MAIN METHOD
    private void runScenarios(HashMap<String, ReplayScenario> scenarios) throws Exception {
        // runScenario all parallel lightweight threads for every ReplayScenario
        try {
            //loadScenarios(); already loaded

            initReporting();

            openAllConnections();

            int numScenarios = scenarios.size();
			if (Float.compare(speed, 0.0f) == 0 || (!enableSync)) {
                //Sequential run assumes that no need to synchronize so discard PrtyScen
                PrtyScen.allSyncPoints.clear();
            }
            if (speed > 0.0f) { // parallel run
                countDownLatch = new CountDownLatch(numScenarios);
                PrtyScen.prepareStartSync(numScenarios);
            }

            //Should it be here or before opening all connections? KT: it's allright. we start runnning when all connections are ready
            actualStartDateTime = new Date();

            int cnt = 0;
            for (ReplayScenario scenario : scenarios.values()) {
                if (Float.compare(speed, 0.0f) == 0) { // one by one
                    countDownLatch = new CountDownLatch(1);
                    logger.info(String.format("Starting scenario '%s'", scenario.name));
                }

                if(scenarioPermission != null) scenarioPermission.acquire();
                if(_syncType < 0) {
                    ReplayRunner runner = new ReplayRunner(this, scenario, cnt);
                    runner.spawn();
                    cnt++;
                }
                else {
                    if((_syncTargets.contains(scenario.connName)) || (_syncTargets.size() == 0)){
                        if(_aggregatedRunners.containsKey(scenario.connName)) {
                            _aggregatedRunners.get(scenario.connName).addScenario(scenario);
                            countDownLatch.countDown();
                            PrtyScen.skipSyncForAggregatedPart();
                        }
                        else {
                            _aggregatedRunners.put(scenario.connName, new ReplayRunner(this, scenario, cnt));
                            cnt++;
                        }
                    }
                    else {
                        ReplayRunner runner = new ReplayRunner(this, scenario, cnt);
                        runner.spawn();
                        cnt++;
                    }
                }

                if (Float.compare(speed, 0.0f) == 0) {  // one by one
                    countDownLatch.await();
                    logger.info(String.format("Scenario finished '%s'", scenario.name));
                    countDownLatch = null;
                }
            }
            for(ReplayRunner agg_runner : _aggregatedRunners.values()) {
                agg_runner.spawn();
            }

            if (speed > 0.0f) { // parallel run
                PrtyScen.startSyncFrameworkWait();
				if (countDownLatch != null) {
					countDownLatch.await(); // every scenario decrease this counter when finished running
					countDownLatch = null;
				}
            }
            logger.info(String.format("Finished %d scenarios", numScenarios));
        }
        catch (Exception e) {
            logger.error(e.toString());
            throw e;
        }
        finally {
            cleanup(scenarios);
            PrtyScen.cleanup();
            logger.warn("Test run COMPLETE.");
        }
    }

    public void openAllConnections() throws Exception {
        for(ConnectionFactory connectionFactory: _connectionFactories.values()) {
            connectionFactory.openAllConnections();
        }
    }


    public ConnectionFactory getOrCreateConnectionFactory(String side) throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        ConnectionFactory connectionFactory = null;
        synchronized (_connectionFactories) {
            if(_connectionFactories.containsKey(side)) {
                connectionFactory = _connectionFactories.get(side);
            } else {
                connectionFactory = createConnectionFactory(side);
                _connectionFactories.put(side, connectionFactory);
            }
        }

        return connectionFactory;
    }

    private ConnectionFactory createConnectionFactory(String side) throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        Map connectionFactories = (Map)_replayParams.get("ConnectionFactories");
        Map sideParams = (Map)connectionFactories.get(side);

        String class_name = sideParams.get("class_name").toString();

        Class<?> class_ = Class.forName(class_name);
        Constructor<?> ctor = class_.getConstructor(ReplayManager.class, String.class, Map.class, String.class);
        ConnectionFactory connectionFactory = (ConnectionFactory)ctor.newInstance(this, side, sideParams, _configFolder);
        return connectionFactory;
    }

    private static final FastDateFormat tsFormatMilliseconds;
    static {
        tsFormatMilliseconds = FastDateFormat.getInstance("yyyy-MM-dd HH:mm:ss.SSS z", TimeZone.getTimeZone("UTC"));
    }

    // this method is called by parallel threads form Steps and then during cleanup from single thread
    @Suspendable
    public void writeReport(ReplayStep step, String result, Exception exception) {
        String result_details = null;
        HashSet<String> errorSet = null;
        if(exception != null) {
            if(exception instanceof ReplayException) {
                errorSet = ((ReplayException)exception).getReportTags();
                result_details = exception.getMessage();
            }
            else {
                result_details = exception.toString() + " *** stack: ";
                for(StackTraceElement t : exception.getStackTrace()) {
                    result_details += "\n" + t.toString();
                }
                errorSet = new HashSet<String>();
                errorSet.add("Replay Tool internal error");
            }
        }

        String connectionName = step.getFullConnectionName();
        // TODO: update report generator to consider connectionName can be null
        // TODO: use exception.reportTags to mark scenario with the tags and to use it for filtration inside report
        writeToDB(tsFormatMilliseconds.format(step.ts), tsFormatMilliseconds.format(new Date()), step.replayScenario.name, step.side, connectionName, step.action, step.data, result, result_details, errorSet);
    }


    @Suspendable
    public void writeOutOfScenarioReport(String result, MessagesOutOfScope_ReplayException exception, HashMap<String, ReplayScenario> scenarios) {
        /*Gson gson = new Gson();*/
        String result_details = null;
        if(exception != null) {
            //result_details = exception.getMessage();//gson.toJson(exception);
            // TODO: format should be like in source database
            for (quickfix.Message msg : exception.unexpectedMessages) {
                String scenarioId = miniFixHelper.extractScenarioId(msg);
                if (!scenarios.containsKey(scenarioId)) scenarioId = null;
                writeToDB(null, null, scenarioId, exception.side, exception.connection, null, msg.toString(), result, result_details, null);
            }
            logger.error(exception.getMessage());
        }
    }

    @Suspendable
    private void writeToDB(String ts, String exects, String name, String side, String connection, String action, String data, String result, String details, HashSet<String> errno) {
        PreparedStatement pstmt = null;
        // (id INTEGER PRIMARY KEY ASC, ts DATETIME, scenario TEXT, side TEXT, connection TEXT, action TEXT, data TEXT, result TEXT, details TEXT, errno TEXT
        String sqlQuery = "INSERT INTO step_results (ts, exects, scenario, side, connection, action, data, result, details, errno) VALUES (?,?,?,?,?,?,?,?,?,?)";
        _reportingLock.lock();
        String errors = null;
        if(errno != null) {
            StringBuilder sb = new StringBuilder();
            for(String s : errno) {
                if(!errorTypesForLogging.contains(s)) {
                    errorTypesForLogging.add(s);
                }
                sb.append(':');
                sb.append(errorTypesForLogging.indexOf(s));
            }
            errors = sb.toString();
        }
        try {
            pstmt = _sqlConnectionForReporting.prepareStatement(sqlQuery);
            pstmt.setString(1, ts);
            pstmt.setString(2, exects);
            pstmt.setString(3, name);
            pstmt.setString(4, side);
            pstmt.setString(5, connection);
            pstmt.setString(6, action);
            pstmt.setString(7, data);
            pstmt.setString(8, result);
            pstmt.setString(9, details);
            pstmt.setString(10, errors);
            pstmt.executeUpdate();
            //_sqlConnectionForReporting.commit();
        } catch (SQLException e) {
            //Oops, something went very wrong...
            logger.error(String.format("Encountered problem with report SQLite DB: %s", e.toString()));
        } finally {
            try {
            	if(pstmt !=null)pstmt.close();
            } catch (SQLException e) {
                logger.error(String.format("Encountered problem with report SQLite DB: %s", e.toString()));
            }
            _reportingLock.unlock();
        }
    }

    @Suspendable
    public boolean getSendPermission(){
        try {
            if(sendPermission != null) {
                if(sendWaitTO) {
                    return sendPermission.tryAcquire(60, TimeUnit.MINUTES);
                }
                else {
                    sendPermission.acquire();
                    return true;
                }
            }
            else {
                return true;
            }
        } catch(InterruptedException e) {
        	Thread.currentThread().interrupt();
            return false;
        }
    }

    public void releaseSendPermisson(){
        if(sendPermission != null) {
            sendPermission.release();
        }
    }

    public void releaseScenarioPermisson(){
        if(scenarioPermission != null) {
            scenarioPermission.release();
        }
    }

}


