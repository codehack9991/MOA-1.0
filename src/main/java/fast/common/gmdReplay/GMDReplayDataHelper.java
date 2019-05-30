package fast.common.gmdReplay;

import com.citi.gmd.client.utils.structs.CString;
import com.google.gson.*;

import fast.common.gmdReplay.client.GMDMessageError;

import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Type;
import java.sql.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedTransferQueue;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by ab56783 on 08/22/2017.
 */
class GMDStepData {
    public int id;
    public Instant ts;
    public String name;
    public String data;
    public boolean matched;

    public GMDStepData(int id, Instant ts, String name, String data) {
        this.id = id;
        this.ts = ts;
        this.name = name;
        this.data = data;
        this.matched = false;
    }
}

enum MessageStatus {
    INORDER, UNEXPECTED, MISSING, OUTOFSEQUENCE, UNDEFINED
}

public class GMDReplayDataHelper {
    public static final int PERFECT_MATCH = 0;
    private final Logger _logger;
    private HashMap<String, ArrayList<GMDStepData>> _scenarios = new HashMap<>();
    //private HashMap<String, Integer> _current_steps = new HashMap<>();
    //private HashMap<String, Integer> _finished_steps = new HashMap<>();
    //private HashMap<String, Instant> _last_ts = new HashMap<>();
    private HashMap<String, LinkedTransferQueue<GMDdata>> _queues = new HashMap<>();
    private HashMap<String, dataProcessor> _processors = new HashMap<>();
    private final ArrayList<String> _labelsForMessageMatching;
    private final ArrayList<String> _labelsToIgnoreCompletely;
    private final ArrayList<String> _labelsToIgnoreValue;
    private final int _dev_lim;
    private final int _dev_time;
    private final int _wait_time;
    private final boolean _async_mode;
    private Integer _GC_MDspeed = 0;
    private Integer _MDspeed = 0;
    //private final HashMap<String, ReentrantLock> _recordingLocks = new HashMap<>();
    private CountDownLatch _countDownLatch = null;
    private Connection _sqlResultCon = null;
    public static final Gson gson;
    static {
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(com.citi.gmd.client.utils.structs.CString.class, new CitiCStringAdapter());
        gson = gsonBuilder.create();
    }
    /*private static final String SQLquery_search = "select object_data.rowid, object_data.ts, object_data.data " +
            "from object_data, json_each(report.details, '$.expectedMessage.header.fields') where " +
            "json_each.key = \"%s\" and json_extract(json_each.value, '$.object') = '%s'";*/

    private static final String SQLquery_search = "select * from object_data where name = '%s' and symbol = '%s'";
    private static final String SQLquery_load_data = "select * from object_data ORDER BY symbol, ts";
    private static final String SQLquery_load_scenario_list = "select distinct symbol from object_data"; //TODO: replace tentative SQL statement
    private static final String SQLquery_load_MD_speed = "select value from properties where key = 'MDspeed'"; //TODO: replace tentative SQL statement

    public static final DateTimeFormatter millisecondsTimeFormatter;
    static {
        millisecondsTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS z");
    }

    public GMDReplayDataHelper(Map rules, int dev_lim, int dev_time, int wait_time, boolean async_mode) {
        _logger = LoggerFactory.getLogger(GMDReplayDataHelper.class);
        ArrayList arrayList = (ArrayList)rules.get("LabelsForMessageMatching");
        _labelsForMessageMatching = new ArrayList<String>();
        for(Object obj: arrayList) {
            _labelsForMessageMatching.add(obj.toString());
        }
        arrayList = (ArrayList)rules.get("LabelsToIgnoreCompletely");
        _labelsToIgnoreCompletely = new ArrayList<String>();
        for(Object obj: arrayList) {
            _labelsToIgnoreCompletely.add(obj.toString());
        }
        arrayList = (ArrayList)rules.get("LabelsToIgnoreValue");
        _labelsToIgnoreValue = new ArrayList<String>();
        for(Object obj: arrayList) {
            _labelsToIgnoreValue.add(obj.toString());
        }
        _dev_lim = dev_lim;
        _dev_time = dev_time * 1000;
        _wait_time = wait_time * 1000;
        _async_mode = async_mode;
    }

    public void initReporting(Connection _sqlConnectionForReporting, String GCfile) throws Exception {
        DateFormat df = new SimpleDateFormat("yyyyMMddHHmm");
        df.setTimeZone(TimeZone.getTimeZone("UTC"));
        PreparedStatement pstmt = null;
        String sqlQuery1 = "CREATE TABLE IF NOT EXISTS test_results(id INTEGER PRIMARY KEY ASC, exects DATETIME, name TEXT, symbol TEXT, msg_errs TEXT, original_id INTEGER, data TEXT, outcome INTEGER, diffList TEXT);";
//        String sqlQuery1 = "CREATE TABLE IF NOT EXISTS test_results(id INTEGER PRIMARY KEY ASC, ts DATETIME, exects DATETIME, symbol TEXT, message_type TEXT, gc_data TEXT, actual_data TEXT, errors TEXT, err_lst TEXT);";
        String sqlQuery2 = "CREATE TABLE IF NOT EXISTS properties(id INTEGER PRIMARY KEY ASC, key TEXT, value TEXT);";
        String sqlQuery3 = "INSERT INTO properties (key, value) VALUES (?,?);";
        String sqlQuery4 = "CREATE TABLE IF NOT EXISTS error_types (id INTEGER PRIMARY KEY ASC, label_path TEXT, error_type TEXT);";
        String sqlQueryErrorID = "CREATE TABLE IF NOT EXISTS error_id (id INTEGER PRIMARY KEY ASC, result_id INTEGER, error_id INTEGER);";
        try {
            pstmt  = _sqlConnectionForReporting.prepareStatement(sqlQuery1);
            pstmt.executeUpdate();
            if(pstmt != null) pstmt.close();
            pstmt  = _sqlConnectionForReporting.prepareStatement(sqlQuery2);
            pstmt.executeUpdate();
            if(pstmt != null) pstmt.close();
            pstmt  = _sqlConnectionForReporting.prepareStatement(sqlQuery3);
            pstmt.setString(1, "GoldenCopy_file");
            pstmt.setString(2, GCfile);
            pstmt.executeUpdate();
            if(pstmt != null) pstmt.close();
            pstmt  = _sqlConnectionForReporting.prepareStatement(sqlQuery4);
            pstmt.executeUpdate();
            if(pstmt != null) pstmt.close();
            pstmt  = _sqlConnectionForReporting.prepareStatement(sqlQueryErrorID);
            pstmt.executeUpdate();
            _sqlResultCon = _sqlConnectionForReporting;
        }
        finally {
            if(pstmt != null) pstmt.close();
        }
    }

    public String createObjectSearchQuery(String name, String symbol, String obj) {
        //GMDAbstractMsg msg = (GMDAbstractMsg)(Class.forName(name).cast(obj));
        return String.format(SQLquery_search, name, symbol);
    }

    public void loadOriginalData(Connection con) throws SQLException {
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		// 1. Load list of scenarios
		try {
			pstmt = con.prepareStatement(SQLquery_load_scenario_list);
			rs = pstmt.executeQuery();
			while (rs.next()) {
				String name = rs.getString("symbol");
				_scenarios.put(name, new ArrayList<>());
				// _current_steps.put(name, 0);
				// _finished_steps.put(name,-1);
				// _recordingLocks.put(name, new ReentrantLock());
				dataProcessor prc;
				if (_async_mode) {
					_queues.put(name, new LinkedTransferQueue<>());
					prc = new dataProcessor(name, _scenarios.get(name), 0, -1, _queues.get(name), new ReentrantLock(),
							this);
					new Thread(prc).start();
				} else {
					prc = new dataProcessor(name, _scenarios.get(name), 0, -1, null, new ReentrantLock(), this);
				}
				_processors.put(name, prc);
			}

		} catch (SQLException e) {
		} finally {
			if (rs != null)
				rs.close();
			if (pstmt != null)
				pstmt.close();
		}

		// 2. Load scenarios
		try {
			pstmt = con.prepareStatement(SQLquery_load_data);
			rs = pstmt.executeQuery();
			while (rs.next()) {
				int id = Integer.parseInt(rs.getString("id"));
				String name = rs.getString("name");
				String symbol = rs.getString("symbol");
				String data = rs.getString("data");
				Instant ts = ZonedDateTime.parse(rs.getString("ts"), millisecondsTimeFormatter).toInstant();
				_scenarios.get(symbol).add(new GMDStepData(id, ts, name, data));
			}
		} catch (Exception e) {
			_logger.error(e.getMessage());
		} finally {
			if (rs != null)
				rs.close();
			if (pstmt != null)
				pstmt.close();
		}

		// 3. Load MD speed
		try {
			pstmt = con.prepareStatement(SQLquery_load_MD_speed);
			rs = pstmt.executeQuery();
			rs.next();
			_GC_MDspeed = Integer.parseInt(rs.getString("value"));
			_countDownLatch = new CountDownLatch(_scenarios.size());
		} catch (SQLException e) {
			// TODO: handle exception
		} finally {
			if (rs != null)
				rs.close();
			if (pstmt != null)
				pstmt.close();
		}
    }

    public void putDataToQueue(Connection con, String name, String symbol, Object target) {
        if(!_queues.containsKey(symbol)) {
            _logger.error(String.format("Received message on UNEXPECTED SYMBOL: %s, message: %s", symbol, target.toString()));
        }
        else {
            GMDdata data = new GMDdata(con, name, symbol, GMDReplayDataHelper.gson.toJson(target));
            if(_async_mode) {
                _queues.get(symbol).put(data);
            }
            else {
                _processors.get(symbol).processMsg(data);
            }
        }
    }

    /**
     *
     * @param name - message class name
     * @param original - original (prerecorded) message (from the golden copy)
     * @param target - comparison target messaege
     * @return: 0 - complete match
     *          n>0 - partial match, n differences
     *          n<0 - no match
     */
    public int checkMsgMatch(String symbol, String name, String original_name, String target, String original, GMDMessageError msg_errs) {
        //_logger.info("\n Checking message: " + name);
        int result = -1;
        if (!name.equals(original_name)) {
            //_logger.error(String.format("Different message types, expected: %s, actual: %s", original_name, name));
            //_logger.error(String.format("Messages cannot be matched"));
            return -1;
        }
        try {
            String actual_json = target;
            JSONObject targetJson = new JSONObject(actual_json);
            JSONObject originalJson = new JSONObject(original);
            result = this.checkMsgMatchLocal(symbol, targetJson, originalJson, null, msg_errs);
        }catch (JSONException e) {
            _logger.error(e.getMessage());
        }
        catch (NullPointerException e) {
        	 _logger.error(e.getMessage());
        } catch (Exception e) {
        	 _logger.error(e.getMessage());
        }
        return  (result < 0) ? result : msg_errs.getErrNumber();
    }

    private int checkMsgMatchLocal (String symbol, Object actual, Object expected, String bc, GMDMessageError msg_errs) throws JSONException {
        int result = 0;
        String current_bc = (bc == null) ? "|->" : bc + "->";

        if((expected.getClass()) != (actual.getClass())) {
            //TODO: this is a tentative logic. Need to decide what is the correct behavior in this case
            _logger.error(String.format("Different object types for the same key '%s', expected: %s of class %s, actual: %s of class %s", current_bc, expected.toString(), expected.getClass(), actual.toString(), actual.getClass()));
            _logger.error(String.format("Messages cannot be matched"));
            msg_errs.clear();
            return -1;
        }
        else if(expected instanceof JSONObject) {
            Iterator keysExpected = ((JSONObject)expected).keys();
            while (keysExpected.hasNext()) {
                String expectedKey = (String) keysExpected.next();
                if (this._labelsToIgnoreCompletely.contains(expectedKey)) {
                    continue;
                }
                else if (!((JSONObject)actual).has(expectedKey)) {
                    if(_labelsForMessageMatching.contains(current_bc + expectedKey)) {
                    //if((_labelsForMessageMatching.contains(expectedKey)) && (bc == null)) {
                        //_logger.error(String.format("Messages cannot be matched due to missing message matching element %s", expectedKey));
                        msg_errs.clear();
                        return -1;
                    }
                    else {
                        String text = msg_errs.addMissingElement(current_bc + expectedKey, ((JSONObject) expected).get(expectedKey).toString());
                        _logger.error(text);
                        result++;
                    }
                }
                else if (this._labelsToIgnoreValue.contains(expectedKey)) {
                    continue;
                }
                else {
                    Object expectedValue = ((JSONObject)expected).get(expectedKey);
                    Object actualValue = ((JSONObject)actual).get(expectedKey);
                    int res = this.checkMsgMatchLocal(symbol, actualValue, expectedValue, current_bc + expectedKey, msg_errs);
                    if (res < 0) {
                        //_logger.error(String.format("Messages cannot be matched"));
                        return -1;
                    }
                    else if((_labelsForMessageMatching.contains(current_bc + expectedKey) && (res>0))) {
                    //else if((_labelsForMessageMatching.contains(expectedKey) && (bc == null) && (res>0))) {
                        //_logger.error(String.format("Messages cannot be matched due to value difference for message matching element %s", expectedKey));
                        msg_errs.clear();
                        return -1;
                    }
                    else {
                        result += res;
                    }
                }
            }
            //Check for unexpected data
            Iterator keysActual = ((JSONObject)actual).keys();
            while (keysActual.hasNext()) {
                String actualKey = (String) keysActual.next();
                if (!((JSONObject)expected).has(actualKey)) {
                    if(_labelsForMessageMatching.contains(current_bc + actualKey)) {
                    //if((_labelsForMessageMatching.contains(actualKey)) && (bc == null)) {
                        //_logger.error(String.format("Messages cannot be matched due to unexpected message matching element %s", actualKey));
                        msg_errs.clear();
                        return -1;
                    }
                    else {
                        String text = msg_errs.addUnexpectedElement(current_bc + actualKey, ((JSONObject) actual).get(actualKey).toString());
                        _logger.error(text);
                        result++;
                    }
                }
            }
        }
        else if (expected instanceof org.json.JSONArray) {
            int limit = Math.min(((JSONArray)expected).length(), ((JSONArray)actual).length());
            for (int k = 0; k < limit; k++) {
                Object expectedObject = ((JSONArray)expected).get(k);
                Object actualObject = ((JSONArray)actual).get(k);
                int res = this.checkMsgMatchLocal(symbol, actualObject, expectedObject, current_bc + "[" + k + "]", msg_errs);
                if (res < 0) {
                    //_logger.error(String.format("Messages cannot be matched"));
                    return -1;
                }
                else {
                    result += res;
                }
            }
            for(int i = limit; i < ((JSONArray)expected).length(); i++) {
                String text = msg_errs.addMissingElement(current_bc + "[" + i + "]", ((JSONArray)expected).get(i).toString());
                _logger.error(text);
                result++;
            }
            for(int i = limit; i < ((JSONArray)actual).length(); i++) {
                String text = msg_errs.addUnexpectedElement(current_bc + "[" + i + "]", ((JSONArray)actual).get(i).toString());
                _logger.error(text);
                result++;
            }
        }
        else if (!(expected).equals(actual)) {
            String text;
            if(expected instanceof String) {
                //text = msg_errs.addValueError(bc, JSONObject.quote(expected.toString()), JSONObject.quote(actual.toString()));
                text = msg_errs.addValueError(bc, buildEscString((String)expected), buildEscString((String)actual));
            }
            else {
                text = msg_errs.addValueError(bc, expected.toString(), actual.toString());
            }
            //_logger.error(text);
            result++;
        }
        return result;
    }

    public int searchMsgMatch(Connection con, String name, String symbol, String target, dataProcessor prc) {
        //Here we have to compare obj against the data in the DB.
        int final_result = -100;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            pstmt  = con.prepareStatement(createObjectSearchQuery(name, symbol, target));
            rs = pstmt.executeQuery();
            int result = -1;
            String final_original = null;
            int cnt = 0;
            GMDMessageError msgErr = null;
            int original_id = 0;
            while (rs.next() && (cnt<10)) {
                //Ok, we have found something that might be relevant to this object
                //Confirm if we have a perfect match or some discrepancies
                original_id = Integer.parseInt(rs.getString("id"));
                String data = rs.getString("data");
                String original_name = rs.getString("name");
                GMDMessageError msg_errs = new GMDMessageError(symbol, name);
                result = checkMsgMatch(symbol, name, original_name, target, data, msg_errs);
                if(result == GMDReplayDataHelper.PERFECT_MATCH) {
                    final_original = data;
                    final_result = 0;
                    msgErr = null;
                    msg_errs.clear();
                    break;
                }
                else if(result > GMDReplayDataHelper.PERFECT_MATCH) {
                    if((final_result > result)||(final_original == null)) {
                        final_result = result;
                        final_original = data;
                        if(msgErr!=null)
                        	msgErr.clear();
                        msgErr = msg_errs;
                    }
                    else {
                        msg_errs.clear();
                    }
                    cnt++;
                }
                else {
                    //Messages cannot be matched
                }
            }
            if(final_original != null) {
                logComparisonResults(MessageStatus.UNDEFINED, final_result, name, symbol, target, original_id, msgErr);
            }
        } catch (SQLException e) {
            _logger.error(String.format("Failed to execute the search query, error: %s. stack trace:\n", e.toString()));
            _logger.error(Arrays.toString(e.getStackTrace()));
        }
        finally {
            if(rs != null) try {
                rs.close();
            } catch (SQLException e) {
                //
            }
            if(pstmt != null) try {
                pstmt.close();
            } catch (SQLException e) {
                //
            }
        }
        return final_result;
    }

    public int searchMsgMatch(String name, String symbol, String target, dataProcessor prc) {
        //Here we have to compare obj against the data in HashMap.
        int final_result = -100;
        try {
            prc.recordingLock.lock();
            ArrayList<GMDStepData> scen_list = prc.scenario;
            int index = prc.current_step;
            int finished = prc.finished_step;
            if (index < scen_list.size()) {
                int original_id = scen_list.get(index).id;
                String original = scen_list.get(index).data;
                String original_name = scen_list.get(index).name;
                GMDMessageError msg_errs = new GMDMessageError(symbol, name);
                final_result = checkMsgMatch(symbol, name, original_name, target, original, msg_errs);
                if (final_result >= GMDReplayDataHelper.PERFECT_MATCH) {
                    prc.current_step = index + 1;
                    scen_list.get(index).matched = true;
                    logComparisonResults(MessageStatus.INORDER, final_result, name, symbol, target, original_id, msg_errs);
                    prc.last_ts = Instant.now();
                    prc.base_ts = scen_list.get(index).ts;
                    if((finished < 0) || (finished == (index -1))) {
                        prc.finished_step = index;
                    }
                    if ((index + 1) >= scen_list.size()) {
                        _countDownLatch.countDown();
                    }
                } else {
                    //Search in skipped messages
                    boolean msg_found = false;
                    finished = (finished < 0) ? -1 : finished;
                    boolean move = true;
                    for (int i = (finished + 1); (i < index) && (!msg_found); i++) {
                        GMDStepData step = scen_list.get(i);
                        if (!step.matched) {
                            final_result = checkMsgMatch(symbol, name, step.name, target, step.data, msg_errs);
                            if (final_result >= GMDReplayDataHelper.PERFECT_MATCH) {
                                //Ok, found match in the skipped messages
                                //_finished_steps.put(symbol, i);
                                step.matched = true;
                                msg_found = true;
                                prc.last_ts = Instant.now();
                                prc.base_ts = step.ts;
                                logComparisonResults(MessageStatus.OUTOFSEQUENCE, final_result, name, symbol, target, step.id, msg_errs);
                                if(move) {
                                    prc.finished_step = i;
                                }
                                //break;
                            }
                            else {
                                move = false;
                            }
                        }
                    }
                    if (!msg_found) {
                        //Search in upcoming expected messages
                        Instant original_ts = scen_list.get(index).ts;
                        int findex = index++;
                        while ((findex < scen_list.size()) && (!msg_found)) {
                            GMDStepData step = scen_list.get(findex);
                            if (step.ts.isBefore(original_ts.plusMillis(_dev_time * _GC_MDspeed / _MDspeed))) {
                                //Try to compare
                                final_result = checkMsgMatch(symbol, name, step.name, target, step.data, msg_errs);
                                if (final_result >= GMDReplayDataHelper.PERFECT_MATCH) {
                                    //Ok, found match in the skipped messages
                                    prc.current_step = findex + 1;
                                    step.matched = true;
                                    msg_found = true;
                                    prc.last_ts = Instant.now();
                                    prc.base_ts = step.ts;
                                    logComparisonResults(MessageStatus.OUTOFSEQUENCE, final_result, name, symbol, target, step.id, msg_errs);
                                    //break;
                                }
                            } else {
                                break;
                            }
                            findex++;
                        }
                    }
                    if (!msg_found) {
                        //We have unexpected message
                        logComparisonResults(MessageStatus.UNEXPECTED, 0, name, symbol, target, 0, null);
                    }
                }
                msg_errs.clear();
            }
        } catch (Exception e) {
            throw e;
        } finally {
            prc.recordingLock.unlock();
        }
        return final_result;
    }

    public void waitForReplayFinish() {
        Timer timer = new Timer();
        try {
            timer.schedule(new MessageFlushTimer(this), _wait_time, _wait_time);
            _countDownLatch.await();
        } catch (InterruptedException e) {
            //e.printStackTrace();
        	Thread.currentThread().interrupt();
        }
        finally {
            timer.cancel();
        }
    }

    public void flushMessages(){
        for(String key : _processors.keySet()) {
            dataProcessor prc = _processors.get(key);
            prc.recordingLock.lock();
            try {
                if (prc.last_ts == null) continue;
                int new_f = prc.finished_step;
                int end = prc.current_step;
                int start = new_f + 1;
                ArrayList<GMDStepData> scen_list = prc.scenario;
                if (end >= scen_list.size()) {
                    //_recordingLocks.get(key).unlock();
                    continue;
                }
                Instant current = scen_list.get(end).ts;
                while (start < end) {
                    GMDStepData step = scen_list.get(start);
                    if (!step.matched) {
                        if (current.isAfter(step.ts.plusMillis(_dev_time * _GC_MDspeed / _MDspeed))) {
                            //This is a missing message
                            step.matched = true;
                            logComparisonResults(MessageStatus.MISSING, 0, step.name, key, null, step.id, null);
                        } else {
                            break;
                        }
                    }
                    new_f = start;
                    start++;
                }
                prc.finished_step = new_f;
                long frame = Instant.now().toEpochMilli() - prc.last_ts.toEpochMilli() - (current.toEpochMilli() - prc.base_ts.toEpochMilli()) * _GC_MDspeed / _MDspeed;
                if (frame > _wait_time) {
                    while (end < scen_list.size()) {
                        GMDStepData step = scen_list.get(end);
                        step.matched = true;
                        logComparisonResults(MessageStatus.MISSING, 0, step.name, key, null, step.id, null);
                        end++;
                        if (end >= scen_list.size()) {
                            _countDownLatch.countDown();
                            break;
                        } else {
                            long diff = (scen_list.get(end).ts.toEpochMilli() - current.toEpochMilli()) * _GC_MDspeed / _MDspeed;
                            if (frame < (_wait_time + diff)) {
                                break;
                            }
                        }
                    }
                    prc.current_step = end;
                    prc.last_ts = Instant.now();
                    prc.base_ts = scen_list.get(end - 1).ts;
                }
            }
            finally {
                prc.recordingLock.unlock();
            }
        }
    }

    public void setMDspeed(int speed) {
        _MDspeed = speed;
    }
    public static void writeMDspeed(int speed, Connection con) {
        //IMPORTANT! This method IS NOT THREADSAFE, better to call it before connecting to GMD server.
        PreparedStatement pstmt = null;
        Integer spd = speed;
        String sqlQuery = "INSERT INTO properties (key, value) VALUES (?,?);";
        try {
            pstmt = con.prepareStatement(sqlQuery);
            pstmt.setString(1, "MDspeed");
            pstmt.setString(2, spd.toString());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            //Oops, something went very wrong...
        } finally {
            try {
            	if (pstmt!=null)
            		pstmt.close();
            } catch (SQLException e) {
                //
            }
        }
    }

    public enum Outcome{
        INORDER_PERFECT,
        INORDER_PARTIAL,
        OUTOFSEQUENCE_PERFECT,
        OUTOFSEQUENCE_PARTIAL,
        MISSING,
        UNEXPECTED;
    }

    ArrayList<MutablePair<Integer, Pair<String, Integer>>> errTypesInDb = new ArrayList<>();
    // TODO: use ReentrantLock instead of 'synchronized'
    private synchronized void logComparisonResults(MessageStatus status, int final_result, String name, String symbol, String obj, int id, GMDMessageError msg_errs) {
        PreparedStatement tableResult = null;
        PreparedStatement errorTypes = null;
        PreparedStatement errorId = null;
        String err_desc = null;
        int keyTableResult=0;
        List<MutablePair<Integer, Pair<String, Integer>>> errTypesInQueue = new LinkedList<>();
        List<MutablePair<Integer, Pair<String, Integer>>> errTypesAlreadyInDb = new LinkedList<>();
        //1. Save data to the result DB - no additional synchronization is necessary - see comments above
        String data = obj;
        String sqlQuery = "INSERT INTO test_results (exects, name, symbol, msg_errs, original_id, data, outcome, diffList) VALUES (?,?,?,?,?,?,?,?);";

        Outcome outcome = null;
        switch (status) {
            case INORDER:
                if(final_result == 0) {
                    outcome = Outcome.INORDER_PERFECT;
                }
                else {
                    outcome = Outcome.INORDER_PARTIAL;
                }
                break;
            case OUTOFSEQUENCE:
                if(final_result == 0) {
                    outcome = Outcome.OUTOFSEQUENCE_PERFECT;
                }
                else {
                    outcome = Outcome.OUTOFSEQUENCE_PARTIAL;
                }
                break;
            case MISSING:
                outcome = Outcome.MISSING;
                break;
            case UNEXPECTED:
                outcome = Outcome.UNEXPECTED;
                break;
            default:
                //Should never come here
                break;
        }

        try {
            tableResult = _sqlResultCon.prepareStatement(sqlQuery, Statement.RETURN_GENERATED_KEYS);
            tableResult.setString(1, ZonedDateTime.now().withZoneSameInstant(ZoneId.of("UTC")).format(millisecondsTimeFormatter));
            tableResult.setString(2, name);
            tableResult.setString(3, symbol);
            err_desc = ((msg_errs == null) || (msg_errs.getErrNumber() < 1)) ? "" : msg_errs.getErrorDescription();
            tableResult.setString(4, err_desc);
            tableResult.setString(5, String.valueOf(id));
            tableResult.setString(6, data);
            tableResult.setString(7, String.valueOf(outcome.ordinal()));
            tableResult.setString(8, /*StringUtils.join(diffListInt, ',')*/ "");
            tableResult.executeUpdate();

            ResultSet rs = tableResult.getGeneratedKeys();
            rs.next();
            keyTableResult = rs.getInt(1);
            rs.close();
            tableResult.close();
        }
        catch (SQLException e) {
            //Oops, something went very wrong...
            _logger.error(String.format("Encountered problem with report SQLite DB: %s", e.toString()));
        }

        ArrayList<GMDMessageError.ErrorDetails> errors = msg_errs == null ? new ArrayList<>() : msg_errs.getErrors();
        try {
			for(int i = 0; i< errors.size(); i++) {
			    String label_path = errors.get(i).getLabel_path();
			    int error_type = errors.get(i).getType().ordinal();

			    Pair<String, Integer> pair =  new MutablePair<>(label_path,error_type);
			    if(errTypesInDb.stream().filter(x -> x.getRight().equals(pair)).count() == 0) {
			        errTypesInQueue.add(new MutablePair<>(-1, pair));
			    }
			    else {
			        errTypesAlreadyInDb.add(errTypesInDb.stream().filter(x -> x.getRight().equals(pair)).findFirst().get());
			    }
			}
		} catch (NullPointerException e) {
			// TODO Auto-generated catch block
			 _logger.error(e.getMessage());
		}

        String sqlQueryErrorTypes = "INSERT INTO error_types (label_path, error_type) VALUES (?,?);";
        try {
            for (MutablePair<Integer, Pair<String, Integer>> errorType : errTypesInQueue) {
                errorTypes = _sqlResultCon.prepareStatement(sqlQueryErrorTypes, Statement.RETURN_GENERATED_KEYS);
                errorTypes.setString(1, errorType.getRight().getLeft());
                errorTypes.setString(2, errorType.getRight().getRight().toString());
                errorTypes.executeUpdate();
                ResultSet rss = errorTypes.getGeneratedKeys();
                rss.next();
                rss.close();
                errorType.setLeft(rss.getInt(1));
                
            }
        }catch (SQLException e) {
        	 _logger.error(e.getMessage());
        } finally {
            errTypesInDb.addAll(errTypesInQueue);
            try {
				errorTypes.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				 _logger.error(e.getMessage());
			}
        }

        String sqlQueryErrorID = "INSERT INTO error_id (result_id, error_id) VALUES (?,?);";
        try {
            for (MutablePair<Integer, Pair<String, Integer>> error : errTypesInQueue){
                errorId = _sqlResultCon.prepareStatement(sqlQueryErrorID);
                errorId.setString(1, String.valueOf((keyTableResult)));
                errorId.setString(2, String.valueOf(error.getLeft()));
                errorId.executeUpdate();
            }
            for (MutablePair<Integer, Pair<String, Integer>> error : errTypesAlreadyInDb){
                errorId = _sqlResultCon.prepareStatement(sqlQueryErrorID);
                errorId.setString(1, String.valueOf((keyTableResult)));
                errorId.setString(2, String.valueOf(error.getLeft()));
                errorId.executeUpdate();
            }

        }catch (SQLException e) {
        	 _logger.error(e.getMessage());
        }
		finally {
			if (errorId != null) {
				try {
					errorId.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					 _logger.error(e.getMessage());
				}
			}
		}

        //2. Output result to the log
        switch (status) {
            case INORDER:
                if(final_result == 0) {
                    _logger.info(String.format("Found PERFECT match for received message '%s' for symbol '%s'", name, symbol, final_result));
                }
                else {
                    _logger.error(String.format("Found PARTIAL match for received message '%s' for symbol '%s', discrepancies: %s", name, symbol, err_desc));
                }
                break;
            case OUTOFSEQUENCE:
                if(final_result == 0) {
                    _logger.info(String.format("Found OUT_OF_SEQUENCE match for received message '%s' for symbol '%s'", name, symbol, final_result));
                }
                else {
                    _logger.error(String.format("Found PARTIAL OUT_OF_SEQUENCE match for received message '%s' for symbol '%s', discrepancies: %s", name, symbol, err_desc));
                }
                break;
            case MISSING:
                _logger.error(String.format("Message '%s' for symbol '%s' is MISSING, message id in Golden Copy DB: %d", name, symbol, id));
                break;
            case UNEXPECTED:
                _logger.error(String.format("Received message '%s' for symbol '%s' is UNEXPECTED, message contents: '%s'", name, symbol, obj.toString()));
                break;
            default:
                //Should never come here
                break;
        }
    }

    private String buildEscString(String cstr) {
        StringBuilder sb1 = new StringBuilder();
        byte[] ch = cstr.getBytes();
        int i = 0;
        while((i < cstr.length()) && (ch[i] != 0)){
            sb1.append((char)ch[i]);
            i++;
        }
        StringBuilder sb2 = new StringBuilder(" (");
        Boolean diff = false;
        for(int j = i+1; j<cstr.length(); j++) {
            Byte b = ch[j];
            sb2.append(' ');
            sb2.append(b.toString());
            if((!diff) && (b != 0)) {
                diff = true;
            }
        }
        sb2.append(")");

        return diff ? sb1.append(sb2).toString() : sb1.toString();
    }
}

class GMDdata {
    public Connection con;
    public String name;
    public String symbol;
    public String target;

    public GMDdata(Connection con, String name, String symbol, String target) {
        this.con = con;
        this.name = name;
        this.symbol = symbol;
        this.target = target;
    }
}

class dataProcessor implements Runnable {
    public final String symbol;
    public final ArrayList<GMDStepData> scenario;
    public Integer current_step;
    public Integer finished_step;
    public Instant last_ts;
    public Instant base_ts;
    private final LinkedTransferQueue<GMDdata> _queue;
    public final ReentrantLock recordingLock;
    private final GMDReplayDataHelper _helper;

    dataProcessor(String symbol, ArrayList<GMDStepData> scenario, Integer current_step, Integer finished_step, LinkedTransferQueue<GMDdata> queue, ReentrantLock recordingLock, GMDReplayDataHelper helper) {
        this.symbol = symbol;
        this.scenario = scenario;
        this.current_step = current_step;
        this.finished_step = finished_step;
        _queue = queue;
        this.recordingLock = recordingLock;
        _helper = helper;
        this.last_ts = null;
        this.base_ts = null;
    }

    @Override
    public void run() {
        int size = scenario.size();
        GMDdata data = null;
        while(true) {
            try {
                data = _queue.take();
            } catch (InterruptedException e) {
            	Thread.currentThread().interrupt();
            }
            processMsg(data);
        }
    }

    public void processMsg(GMDdata data) {
        if(data.con == null) {
            _helper.searchMsgMatch(data.name, data.symbol, data.target, this);
        }
        else {
            _helper.searchMsgMatch(data.con, data.name, data.symbol, data.target, this);
        }
    }
}

class CitiCStringAdapter implements JsonSerializer<CString> {
    public JsonElement serialize(com.citi.gmd.client.utils.structs.CString src, Type typeOfSrc, JsonSerializationContext context) {
        String s = "";
        for(int i=0; i<src.length(); i++) s += src.getCharAtIdx(i);
        return new JsonPrimitive(s);
    }
}

class MessageFlushTimer extends TimerTask {
    private final GMDReplayDataHelper _helper;

    MessageFlushTimer(GMDReplayDataHelper helper) {
        _helper = helper;
    }

    @Override
    public void run() {
        _helper.flushMessages();
    }
}
