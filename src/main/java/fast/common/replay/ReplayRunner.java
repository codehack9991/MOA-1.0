package fast.common.replay;

import co.paralleluniverse.actors.BasicActor;
import co.paralleluniverse.fibers.SuspendExecution;
import co.paralleluniverse.fibers.Suspendable;
import fast.common.logging.FastLogger;

import java.util.ArrayList;
import java.util.Comparator;

/**
 * Created by ab56783 on 07/24/2017.
 */
public class ReplayRunner  extends BasicActor<String, Void> {
    public ReplayManager replayManager;
    private FastLogger _logger;
    //public String connName;
    public ArrayList<ReplayStep> steps = new ArrayList<>();
    private boolean _singleScenario = true;
    public int id = -1;

    public ReplayRunner(ReplayManager replayManager, ReplayScenario scen, int id) {
        this.replayManager = replayManager;
        this.addScenario(scen);
        _singleScenario = true;
        this.id = id;
        _logger = FastLogger.getLogger(String.format("Runner[%s]", scen.name));
    }

    public void addScenario(ReplayScenario scen) {
        if(scen != null) {
            _singleScenario = false;
            for(ReplayStep step : scen.steps) {
                this.steps.add(step);
            }
        }
    }

    public String getCriteriaName(){
        return steps.get(0).replayScenario.connName;
    }

    @Override
    @Suspendable
    protected Void doRun() throws InterruptedException, SuspendExecution {
        try {
            steps.sort(new Comparator<ReplayStep>() {
                @Override
                public int compare(ReplayStep o1, ReplayStep o2) {
                    return o1.ts.compareTo(o2.ts);
                }
            });
            internalRun(this.steps);
        }
        catch (Exception e) { // unexpected exception - print stack trace
            _logger.error(e.getMessage());
            // then exit
        }
        finally {
            //Nothing here so far
        }

        return null;
    }

    @Suspendable
    private void internalRun(ArrayList<ReplayStep> steps) {
        //ArrayList<ReplayException> exceptions = new ArrayList<>();
        // KT: now we will not continue playback after step failed - because as we see it make no practical sence - all further validation steps will fail and errors will be related the the first error

        try {
            replayManager.PrtyScen.initSyncPoints(this);
            for (int i=0; i<steps.size(); i++) {
                ReplayStep step = steps.get(i);

                replayManager.PrtyScen.waitIfNecessary(this, i);

                step.replayScenario.runOneStep();
            }
        }
        catch(Exception e) { // Unexpected error - so we print out stack trace
        	_logger.error(e.getMessage());
        }
        finally { // TODO: move this to cleanup() . otherwise becasue of increased load on CFORE waiting some seconds is not enough - unexpected messages are coming to us much later
            replayManager.PrtyScen.deregisterRunner(this);
        }
    }
}
