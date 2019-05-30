package fast.common.replay;

import co.paralleluniverse.fibers.Suspendable;
import fast.common.logging.FastLogger;
import quickfix.field.PossDupFlag;

import java.util.ArrayList;
import java.util.HashMap;

public class ReplayScenario {
    public ReplayManager replayManager;
    private FastLogger _logger;
    private int _currentStepIdx = 0;
    public String name;
    public String connName;
    public int lastStepIdx = -1; // we set this during run() and use this this during cleaup to write all remaining errors (related unexpected messages)

    public int tagGeneratedId = 0;

    public ArrayList<ReplayStep> steps = new ArrayList<>();
    public HashMap<String, String> tagValuesMap = new HashMap<String, String>(); // use as sync object

    public ReplayScenario(ReplayManager replayManager, String name, String connName) {
        this.replayManager = replayManager;
        this.name = name;
        this.connName = connName;
        _logger = FastLogger.getLogger(String.format("Scenario[%s]", this.name));
    }

    // single threaded call here
    public void cleanup() {
        int currentStepIdx = lastStepIdx;
        // if any send was successful then we need to wait 10 seconds and then read TCP and EMS buffers to catch all messages that belong to this scenario - fetch them by 11 and 37 tags defined during send
        if(currentStepIdx < steps.size()) {
            // DONE: we need to wait some time before cleaning the buffers from unexpected messages that belong to this scenario

            for(int i = currentStepIdx+1; i < steps.size(); i++) {
                ReplayStep step = steps.get(i);
                Exception exception = null;

                try {
                    ArrayList<quickfix.Message> unexpectedMessages = step.cleanUpScenarioByFindingItsMessages(); // DONE: think, maybe replace return result via throwing exceptions? NO - we need to catch them all anyway here and rethrow altogether - so it is not convenient
                    if(unexpectedMessages != null) {
                        exception = new MessagesUnexpected_ReplayException(unexpectedMessages);
                    }
                }
                catch (Exception e){
                    exception = e;
                }

                replayManager.writeReport(step, "BLOCKED", exception); //Store corresponding unexpected messages to the report DB
            }

            // TODO: need to collect all unexpected messages that appear on wrong connections
            // TODO: need to collect all unexpected messages that belong to some scenarios (e.g. exec report when scenario don't have such step)

        }
        int balance = 0;
        int limit = (lastStepIdx < steps.size()) ? (lastStepIdx + 1) : steps.size();
        for(int i=0; i<limit; i++ ){
            if(steps.get(i).action.equals(Action.CHECK_RECEIVE.name())) {
                balance--;
            }
            else if(steps.get(i).action.equals(Action.SEND.name())) {
                balance++;
            }
        }
        for(int i=0; i<=balance; i++) replayManager.releaseSendPermisson();
        replayManager.releaseScenarioPermisson();

        tagValuesMap.clear();
    }

    public String toString() {
        ArrayList<String> stringList = new ArrayList<String>();
        for(ReplayStep step: steps) {
            stringList.add(step.toString());
        }
        String stepsStr = String.join("\r\n", stringList);
        return String.format(String.format("Scenario '%s':\r\n%s", name, stepsStr));
    }

    @Suspendable
    public void runOneStep() {
        if(lastStepIdx != -1) {
            //The scenario execution is stopped. Possible reasons are
            // (1) last step has been already executed  or
            // (2) has encountered scenario level error (at the moment only 'missing message')
            return;
        }
        ReplayStep currentStep = null;
        ReplayStep step = steps.get(_currentStepIdx);
        currentStep = step;
        try {
            try {
                step.run(); // will log error in case of failure
                _currentStepIdx++;
                replayManager.writeReport(currentStep, "PASSED", null);
            } catch (MessageIncorrect_ReplayException e) { // this exception can continue execution of steps
                replayManager.releaseSendPermisson();
                _currentStepIdx++;

                // report errors;
                replayManager.writeReport(currentStep, "TAG_ERROR", e);
            } catch (MessageMissing_ReplayException e) {
                replayManager.releaseSendPermisson();
                if (e.expectedMessage.getHeader().isSetField(PossDupFlag.FIELD) && e.expectedMessage.getHeader().getString(PossDupFlag.FIELD).equals("Y")) {
                    _currentStepIdx++;
                    replayManager.writeReport(currentStep, "SKIPPED", null);
                } else {
                    replayManager.writeReport(currentStep, "MESSAGE_ERROR", e);
                    lastStepIdx = _currentStepIdx;
                }
            }
        }
        catch(ReplayException e) { // any known exception incompatible with continuation of testing (only Missing message, actually)
            // report errors
            replayManager.writeReport(currentStep, "MESSAGE_ERROR", e);

            // exit from scenario now - via finally
            replayManager.releaseSendPermisson();
            lastStepIdx = _currentStepIdx;
        }
        catch(Exception e) { // Unexpected error - so we print out stack trace
        	_logger.error(e.getMessage());

            replayManager.writeReport(currentStep, "UNEXPECTED_ERROR", e);
            // exit from scenario now - via finally
            replayManager.releaseSendPermisson();
            lastStepIdx = _currentStepIdx;
        }
        finally { // TODO: move this to cleanup() . otherwise becasue of increased load on CFORE waiting some seconds is not enough - unexpected messages are coming to us much later
            //consider what to do here
            if(_currentStepIdx >= steps.size()) {
                lastStepIdx = _currentStepIdx;
            }
            if(lastStepIdx != -1){
                replayManager.countDownLatch.countDown(); // decrease counter - when it will be zero manager will stop execution
            }
        }

    }
}
