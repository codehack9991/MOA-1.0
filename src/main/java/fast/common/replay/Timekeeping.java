package fast.common.replay;

import co.paralleluniverse.fibers.SuspendExecution;
import co.paralleluniverse.fibers.Suspendable;
import co.paralleluniverse.strands.Strand;

/**
 * Created by st47350
 */
public class Timekeeping {
    @Suspendable
    public static void sleep(int msecs) {
        try {
            Strand.sleep(msecs);
        } catch (SuspendExecution suspendExecution) {
            //suspendExecution.printStackTrace();
        } catch (InterruptedException e) {
        	Thread.currentThread().interrupt();
        }
    }
}
