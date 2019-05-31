package fast.common.testng;

import org.junit.Before;
import org.junit.Test;
import org.testng.*;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

public class TestFastTestListener {

    private FastTestListner fastTestListner;

    @Before
    public void setup(){
        fastTestListner = new FastTestListner();
    }

    @Test
    public void onStart(){

        try{
            fastTestListner.onStart(null);
        }catch(Exception e){
            assertEquals(NullPointerException.class, e.getClass());
        }

    }

    @Test
    public void onFinish(){
        try{
            fastTestListner.onFinish(null);
        }catch(Exception e){
            assertEquals(NullPointerException.class, e.getClass());
        }
    }

    @Test
    public void onTestStart(){

        ITestResult iTestResult = mock(ITestResult.class);
        fastTestListner.onTestStart(iTestResult);

    }

    @Test
    public void onTestFailure(){

        ITestResult iTestResult = mock(ITestResult.class);
        ITestNGMethod iTestNGMethod = mock(ITestNGMethod.class);
        when(iTestResult.getMethod()).thenReturn(iTestNGMethod);

        fastTestListner.onTestFailure(iTestResult);

    }

    @Test
    public void onTestSuccess(){

        ITestResult iTestResult = mock(ITestResult.class);
        ITestNGMethod iTestNGMethod = mock(ITestNGMethod.class);
        when(iTestResult.getMethod()).thenReturn(iTestNGMethod);

        fastTestListner.onTestSuccess(iTestResult);

    }

    @Test
    public void onTestSuccess_JiraEnabled() throws Exception {

        ITestResult iTestResult = mock(ITestResult.class);
        ITestNGMethod iTestNGMethod = mock(ITestNGMethod.class);
        when(iTestResult.getMethod()).thenReturn(iTestNGMethod);
        when(iTestNGMethod.getDescription()).thenReturn("test description");

        fastTestListner.onTestSuccess(iTestResult);
    }

    @Test
    public void onTestSkipped(){

        ITestResult iTestResult = mock(ITestResult.class);
        ITestNGMethod iTestNGMethod = mock(ITestNGMethod.class);
        when(iTestResult.getMethod()).thenReturn(iTestNGMethod);

        fastTestListner.onTestSkipped(iTestResult);

    }
}
