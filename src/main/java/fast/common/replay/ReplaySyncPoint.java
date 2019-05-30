package fast.common.replay;

import co.paralleluniverse.actors.BasicActor;
import co.paralleluniverse.fibers.Suspendable;
import co.paralleluniverse.strands.concurrent.CountDownLatch;
import co.paralleluniverse.strands.concurrent.Phaser;
import fast.common.logging.FastLogger;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;

enum SyncApplicability {NOT_APPLICABLE, SYNC_LATER, ALL_BEFORE, ALL_AFTER}

public class ReplaySyncPoint extends BasicActor<String, Void> {
    public String client_connection;
    public String mgmt_action;
    public String mgmt_side;
    public java.util.Date execution_ts;
    public java.util.Date ts;
    public String side;
    public String connection;
    public String action;
    public String data;
    private FastLogger _logger;
    private ReplayMgmtCenter replayMgmt;
    public ReplaySyncPoint next = null;
    ReplayConnection connObj;
    ReplayConnection connTCPObj;
    private Integer _maxWaitScenNum = 60000;

    private final ArrayList<Phaser> _phasers = new ArrayList<>();
    //private final Phaser phaser = new Phaser(1);
    public final CountDownLatch lock = new CountDownLatch(1);

    public ReplaySyncPoint(ReplayMgmtCenter replayMgmt, java.util.Date ts, String side, String connection, String action, String data) throws Exception {
        this.replayMgmt = replayMgmt;
        this.ts = ts;
        this.data = String.format(replayMgmt.EODmessage, replayMgmt.C4InstanceName, data);
        this.side = side;
        this.mgmt_side = side.equals("EMS")? "TCP" : null;
        this.connection = connection;
        this.action = action.contains("SEND") ? "SEND" : action;
        this.client_connection = data;
        this.mgmt_action = action;
        this.execution_ts = null;
        _phasers.add(new Phaser(1));
        _logger = FastLogger.getLogger(String.format("SyncPoint for connection[%s] - %s(%s)",
                client_connection,
                this.mgmt_action,
                LocalDateTime.ofInstant(this.ts.toInstant(), ZoneId.systemDefault()).format(
                        this.replayMgmt.replayManager.miniFixHelper.millisecondsTimeFormatter)));
    }

    public boolean canInitConnections(){
        try {
            ConnectionFactory connFactory = replayMgmt.replayManager.getOrCreateConnectionFactory(side);
            ConnectionFactory connTCPFactory = replayMgmt.replayManager.getOrCreateConnectionFactory(mgmt_side);
            connObj = connFactory.getOrCreateConnection(connection, action);
            connTCPObj = connTCPFactory.getConnection(client_connection, "SEND");
            return (connTCPObj != null);
        }
        catch (Exception e){
            //Oops! Problem with connection...
            _logger.error(e.getMessage());
            replayMgmt.replayManager.writeReport(null, "UNEXPECTED_ERROR", e);
            return false;
        }
    }

    public boolean isSyncBefore(ReplaySyncPoint next) {
        if((this.client_connection.equals(next.client_connection)) &&
                (this.mgmt_action.equals(next.mgmt_action)) &&
                (this.mgmt_side.equals(next.mgmt_side)) &&
                (this.ts.before(next.ts))) {
            if((this.next != null) && (this.next.ts.before(next.ts))) {
                return false;
            }
            else {
                return true;
            }
        }
        else {
            return false;
        }
    }

    @Suspendable
    public Void doRun() {

        try {

            int num = _phasers.get(0).getRegisteredParties();
            if(num < 2) {
                //No one is syncing on this - just skip it
                _logger.info(String.format("The sync for %s is SKIPPED: no scenarios are syncing on it. Action: %s", client_connection, mgmt_action));
                next = null;
                return null;
            }
            if(next != null) {
                next._phasers.get(0).register();
                //next.spawn();
            }
            while(_phasers.get(0).getRegisteredParties() > 1) {
                _phasers.get(0).arriveAndAwaitAdvance();
            }

            _logger.info(String.format("The sync for %s is in progress. Action: %s", client_connection, mgmt_action));
            if (action.equals(Action.SEND.name())) {
                if (mgmt_action.equals(Action.SEND_EOD.name())) {
                    //1. Disconnect as client
                    connTCPObj.force_disconnect();
                    Timekeeping.sleep(1000);

                    //2. Send EOD
                    connObj.sendraw(data);
                    _logger.info(String.format("success [%s]", data));
                    Timekeeping.sleep(15000);
                    execution_ts = new Date();

                    //3. Reconnect as client
                    connTCPObj.connect();

                    //4. Unlock steps (Do it inside 'finally' statemant)
                    _logger.info(String.format("The sync for %s is DONE. Action: %s", client_connection, mgmt_action));
                } else {
                    throw new Exception("Unexpected MGMT action");
                }
            } else {
                throw new Exception(String.format("Unexpected action:'%s'", action));
            }
        }
        catch (Exception e) {
            _logger.error(e.getMessage());
            //replayMgmt.replayManager.writeReport(null, "UNEXPECTED_ERROR", e);
        }
        finally {
            _logger.info(String.format("The sync for %s is DONE. Action: %s.", client_connection, mgmt_action));
            if(next != null) {
                next._phasers.get(0).arriveAndDeregister();
            }
            lock.countDown();
            //Finally notify MgmtCenter that the job is done
            replayMgmt.endlock.countDown();
        }
        return null;
    }

    @Suspendable
    public void registerSyncApplicable(ArrayList<ReplayStep> steps, String connection, int id) throws Exception {
        if(isApplicable(steps, connection)) {
            if(!checkSyncApplicable(steps, 0, connection).equals(SyncApplicability.ALL_AFTER)) {
                if(_phasers.size()<= (id / _maxWaitScenNum)) {
                    for(int i=0; i<=((id/_maxWaitScenNum)-_phasers.size()) ; i++) {
                        _phasers.add(new Phaser(_phasers.get(0)));
                    }
                }
                _phasers.get(id/_maxWaitScenNum).register();
            }
        }
    }

    @Suspendable
    public void arriveSyncApplicable(ArrayList<ReplayStep> steps, int current, String connection, int id) throws Exception {
        if(isApplicable(steps, connection)) {
            if (execution_ts == null) {
                if((current+1) < steps.size()) {
                    if (checkSyncApplicable(steps, current+1, connection).equals(SyncApplicability.ALL_AFTER)) {
                        _phasers.get(id/_maxWaitScenNum).arriveAndDeregister();
                    }
                }
                else if(!checkSyncApplicable(steps, 0, connection).equals(SyncApplicability.ALL_AFTER)) {
                    //This is the last step so deregister here
                    _phasers.get(id/_maxWaitScenNum).arriveAndDeregister();
                }
            }
        }
    }

    @Suspendable
    public java.util.Date waitIfSyncApplicable (ArrayList<ReplayStep> steps, int current, String connection) throws Exception {
        if(isApplicable(steps, connection)) {
            if (execution_ts == null) {
                if (checkSyncApplicable(steps, current, connection).equals(SyncApplicability.ALL_AFTER)) {
                    lock.await();
                }
            }
            return execution_ts;
        }
        else return null;
    }

    private boolean isApplicable(ArrayList<ReplayStep> steps, String connection) {
        return this.client_connection.equals(connection);
    }

    private SyncApplicability checkSyncApplicable(ArrayList<ReplayStep> steps, int start, String connection) throws Exception {
        int first = -1;
        if(!steps.get(start).action.equals(Action.SEND.name())) {
            return SyncApplicability.SYNC_LATER;
        }
        for(int i=start; i<steps.size(); i++) {
            ReplayStep step = steps.get(i);
            if (step.action.equals(Action.SEND.name())) {
                if(step.ts.after(ts)) {
                    first = i;
                    break;
                }
            }
        }
        if(first != -1) {
            if(first != start) {
                return SyncApplicability.SYNC_LATER;
            }
            else return SyncApplicability.ALL_AFTER;
        }
        else return SyncApplicability.ALL_BEFORE;
    }
}
