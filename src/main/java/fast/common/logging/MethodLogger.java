/* KT: I see we don't use such detailed level of logging. So we can disable it. And it allows us to remove dependency on AspectJ Weaver

package fast.common.logging;

import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.CodeSignature;
import org.aspectj.lang.reflect.MethodSignature;

import java.util.ArrayList;

// TODO: do we need META_INF/aop.xml?

@Aspect
public class MethodLogger {
    static FastLogger logger = FastLogger.getLogger("autolog");

    private String getParamsString(ProceedingJoinPoint point) {
        String[] param_names = ((CodeSignature)point.getSignature()).getParameterNames();
        Object[] param_values = point.getArgs();
        String args_str = "<empty>";
        ArrayList<String> args_list = new ArrayList<String>();
        if(param_names.length > 0) {
            for(int i=0; i<param_names.length; i++) {
                String param_name = param_names[i];
                String param_value_str = "<null>";
                Object param_value = param_values[i];
                if(param_value != null) {
                    param_value_str = param_value.toString();
                }
                args_list.add(String.format("%s=%s", param_name, param_value_str));
            }
            args_str = StringUtils.join(args_list, ", ");
        }

        return args_str;
    }

    private String getEnterLogString(ProceedingJoinPoint point) {
        String classname_str = point.getSignature().getDeclaringType().getName();
        String methodname_str = MethodSignature.class.cast(point.getSignature()).getMethod().getName();
        String args_str = getParamsString(point);

        String result = String.format("%s.%s() - Enter, params: %s",
                classname_str,
                methodname_str,
                args_str);
        return result;
    }

    private String getExitLogString(ProceedingJoinPoint point, Object call_result, long duration) {
        String classname_str = point.getSignature().getDeclaringType().getName();
        String methodname_str = MethodSignature.class.cast(point.getSignature()).getMethod().getName();
        String call_result_str = "<null>";
        if(call_result != null) {
            call_result_str = call_result.toString();
        }
        String result = String.format("%s.%s() - Exit, result: %s (in %d ms)",
                classname_str,
                methodname_str,
                call_result_str,
                duration);
        return result;
    }

    private String getExceptionLogString(ProceedingJoinPoint point, Throwable ex) {
        String classname_str = point.getSignature().getDeclaringType().getName();
        String methodname_str = MethodSignature.class.cast(point.getSignature()).getMethod().getName();
        String args_str = getParamsString(point);

        String result = String.format("Exception in %s.%s(), params: %s - %s",
                classname_str,
                methodname_str,
                args_str,
                ex.toString());
        return result;
    }

    @Around("execution(public * fast.common.agents.*.*(..))")
    public Object aroundCore(ProceedingJoinPoint point) throws Throwable {
        long start = System.currentTimeMillis();

        String enter_str = getEnterLogString(point);
        logger.debug(enter_str);
        Object call_result = null;
        try {
            call_result = point.proceed();
        }
        catch (Throwable ex) {
            String exception_str = getExceptionLogString(point, ex);
            logger.error(exception_str);
            throw ex;
        }
        long duration = System.currentTimeMillis() - start;
        String exit_str = getExitLogString(point, call_result, duration);
        logger.debug(exit_str);

        return call_result;
    }

    @Around("execution(public * fast.common.replay.*.*(..))")
    public Object aroundReplay(ProceedingJoinPoint point) throws Throwable {
        long start = System.currentTimeMillis();

        String enter_str = getEnterLogString(point);
        logger.debug(enter_str);
        Object call_result = null;
        try {
            call_result = point.proceed();
        }
        catch (Throwable ex) {
            String exception_str = getExceptionLogString(point, ex);
            logger.error(exception_str);
            throw ex;
        }
        long duration = System.currentTimeMillis() - start;
        String exit_str = getExitLogString(point, call_result, duration);
        logger.debug(exit_str);

        return call_result;
    }
}

*/
