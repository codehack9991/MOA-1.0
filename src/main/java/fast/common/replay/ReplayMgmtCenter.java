package fast.common.replay;

import co.paralleluniverse.fibers.Suspendable;
import co.paralleluniverse.strands.concurrent.CountDownLatch;
import fast.common.logging.FastLogger;

import java.util.ArrayList;
import java.util.Date;

public class ReplayMgmtCenter {

    public String EODmessage;
    public String C4InstanceName;
    public ReplayManager replayManager;
    private FastLogger _logger;
    public String name;
    public ArrayList<ReplaySyncPoint> allSyncPoints = new ArrayList<>();
    public ArrayList<ReplaySyncPoint> syncPoints = new ArrayList<>();
    private CountDownLatch lock = null;
    private CountDownLatch startlock = null;
    public CountDownLatch endlock = null;

    public ReplayMgmtCenter(ReplayManager replayManager, String name) {
        this.replayManager = replayManager;
        this.name = name;
        _logger = FastLogger.getLogger(String.format("Scenario[%s]", this.name));
        if(this.replayManager._replayParams.containsKey("EODmessage")) {
            EODmessage = this.replayManager._replayParams.get("EODmessage").toString();
        }
        else {
            EODmessage = null;
        }
        if(this.replayManager._replayParams.containsKey("C4InstanceName")) {
            C4InstanceName = this.replayManager._replayParams.get("C4InstanceName").toString();
        }
        else {
            C4InstanceName = null;
        }
    }

    public void prepareStartSync(int numScenarios){
        startlock = new CountDownLatch(numScenarios);
        lock = new CountDownLatch(1);
    }

    public void skipSyncForAggregatedPart(){
        if(startlock != null ) startlock.countDown();
    }

    @Suspendable
    public void initSyncPoints(ReplayRunner runner) throws Exception {
        //Based on steps, determine Synchronization points applicable and register to them
        for(ReplaySyncPoint point : allSyncPoints) {
            point.registerSyncApplicable(runner.steps, runner.getCriteriaName(), runner.id);
        }
        if(startlock != null ) startlock.countDown();
        if(lock != null) lock.await();
    }

    @Suspendable
    public void startSyncFrameworkWait() {
        try {
            if(startlock != null ) startlock.await();
            for(int i=0; i<allSyncPoints.size()-1; i++) {
                ReplaySyncPoint first = allSyncPoints.get(i);
                for(int j=i+1; j<allSyncPoints.size(); j++) {
                    ReplaySyncPoint next = allSyncPoints.get(j);
                    if(first.isSyncBefore(next)) {
                        first.next = next;
                        break;
                    }
                }
            }
            for (ReplaySyncPoint point : allSyncPoints) {
                //Discard all sync points with TS BEFORE test start or AFTER test end
                if((point.ts.before(replayManager.startDateTime)) || (point.ts.after(replayManager.endDateTime))) {
                    _logger.info(String.format("The sync for %s is SKIPPED - happen before test start. Action: %s", point.client_connection, point.mgmt_action));
                }
                else{
                    if(point.canInitConnections()) {
                        syncPoints.add(point);
                    }
                    else {
                        _logger.info(String.format("The sync for %s is SKIPPED - no scenarios are using this connection. Action: %s", point.client_connection, point.mgmt_action));
                    }
                }
            }
            endlock = new CountDownLatch(syncPoints.size());
            for(ReplaySyncPoint point : syncPoints) {
                point.spawn();
            }
        }
        catch(Exception e) { // Unexpected error - so we print out stack trace
            _logger.error(e.getMessage());
        }
        finally { // TODO: move this to cleanup() . otherwise becasue of increased load on CFORE waiting some seconds is not enough - unexpected messages are coming to us much later
            allSyncPoints.clear();
            if(lock != null) lock.countDown();
            try {
                //Wait for all sync points to complete
                endlock.await();
            } catch (InterruptedException e) {
            	_logger.error(e.getMessage());
                Thread.currentThread().interrupt();
            }
        }
    }

    public void deregisterRunner(ReplayRunner runner) {
        for(ReplaySyncPoint point : syncPoints) {
            try {
                point.arriveSyncApplicable(runner.steps, runner.steps.size(), runner.getCriteriaName(), runner.id);
            }
            catch (Exception e) {

            }
        }
    }

    public void cleanup() {
        syncPoints.clear();
    }

    @Suspendable
    public void waitIfNecessary(ReplayRunner runner, int curr_step) throws Exception{
        ReplaySyncPoint last = null;
        if(curr_step>0) {
            for(ReplaySyncPoint point : syncPoints) {
                point.arriveSyncApplicable(runner.steps, (curr_step-1), runner.getCriteriaName(), runner.id);
            }
        }
        for(ReplaySyncPoint point : syncPoints) {
            //Check if we need to sync at this point now
            Date end = point.waitIfSyncApplicable(runner.steps, curr_step, runner.getCriteriaName());
            if((end != null) && ((last == null) || (last.execution_ts.before(end)))) {
                last = point;
            }
        }

        Date local_start_actual;
        Date local_start_prod;
        if (last != null) {
            local_start_actual = last.execution_ts;
            local_start_prod = last.ts;
        }
        else {
            local_start_actual = replayManager.actualStartDateTime;
            local_start_prod = replayManager.startDateTime;
        }
        float speed = replayManager.speed;
        Date currDT = new Date();
        ReplayStep step = runner.steps.get(curr_step);
        long timeDiff = (Float.compare(speed, 0) == 0) ? 0 : (long) ((step.ts.getTime() - local_start_prod.getTime()) / speed - (currDT.getTime() - local_start_actual.getTime()));
        if (timeDiff > 0) {
            Timekeeping.sleep((int) timeDiff);
        }
    }
}
