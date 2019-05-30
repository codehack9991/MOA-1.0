package fast.common.context;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.script.ScriptException;

import fast.common.cipher.*;
import fast.common.logging.FastLogger;

public class EvalScope implements AutoCloseable
{
    static FastLogger logger = FastLogger.getLogger("EvalScope");
    Pattern _varPattern = null;
    Pattern _paramsPattern = null;
    Pattern _jsPattern = null;
    Pattern _userStrPattern = null;
    private HashMap<String, StepResult> _variables;
    private Map _threadParams;
    private int _threadNumber;
    private String privateKeyFilePath; 
    private String secretKey;
    
    public EvalScope() {
        _variables = new HashMap<String, StepResult>();
        _paramsPattern = Pattern.compile("^\\$(\\w+)");
        _varPattern = Pattern.compile("^(@\\w+\\.-?\\w+)");
        _jsPattern = Pattern.compile("^%(.*)%$");
        _userStrPattern = Pattern.compile("^\\[\\[(.*)\\]\\]$");
    }

    void saveVar(String varName, StepResult varValue) {
        _variables.put(varName, varValue);
        logger.debug(String.format("saved variable %s=%s", varName, varValue != null ? varValue.toString() : "null"));
    }

    StepResult getVar(String varName) {
        return _variables.get(varName);
    }
    
    public void updateParam(String paramName, Object value) throws Exception {
        if(_threadParams != null) {
            ArrayList paramValues = (ArrayList) _threadParams.get(paramName);
            if(paramValues != null) {
                paramValues.set(_threadNumber - 1, value);
                logger.debug(String.format("ThreadParam '%s[%d]', was updated to '%s'", paramName, _threadNumber - 1, value));
                return;
            }
        }

        if (EcmaScriptInterpreter.getInstance().getParams() != null) {
            Object oldValue = EcmaScriptInterpreter.getInstance().getParams().get(paramName);
            if (oldValue != null) {
                EcmaScriptInterpreter.getInstance().getParams().put(paramName, value);
                logger.debug(String.format("Param '%s', was updated to '%s'", paramName, value));
                return;
            }
        }

        throw new Exception(String.format("Param or ThreadParam '%s' is not defined and due to this cannot be updated", paramName));
    }

    String getParam(String paramName) throws Exception {
        String result = null;
        Object resultObj = null;
        if(_threadParams != null) {
            ArrayList paramValues = (ArrayList) _threadParams.get(paramName);
            if(paramValues != null) {
                resultObj = paramValues.get(_threadNumber - 1);
            } // else ThreadParam now found
        }

        if(resultObj == null) { // try to find in global Params
            if (EcmaScriptInterpreter.getInstance().getParams() != null) {
                resultObj = EcmaScriptInterpreter.getInstance().getParams().get(paramName);
            }
        }

        if (resultObj == null) {
            //throw new Exception(String.format("Parameter '%s' not defined", paramName));
        	return null;
        }

        result = resultObj.toString();
        return result;
    }

    String getScriptResult(String script) throws ScriptException {
        return EcmaScriptInterpreter.getInstance().interpret(script);
    }

    public String processString(String msg) {
    	if(msg == null){
    		logger.warn("Input msg is null!");
    		return null;
    	}
    	
    	StringBuilder result = new StringBuilder(evaluateString(msg));
	
    	processPassword(result);
		return result.toString(); 	
    }
    
    public String evaluateString(String msg) {
    	
    	StringBuilder result = new StringBuilder();

		ArrayList<String> parts = getParts(msg);
		
		for(String part : parts){
			logger.debug("father node: \""+msg+"\" , process part: \""+part+"\"");
			try {
    			switch(part.charAt(0)){
    			case '@':
    				result.append(processVariables(part));
    				break;
    			case '$':
    				result.append(processParams(part));
    				break;
    			case '%':
    				result.append(processScripts(part));
    				break;
    			case '[':
    				result.append(proseeSelfDefine(part));
    				break;
    			default:
    				result.append(part);
    				break;
    			}
            } catch (Throwable ex) {
                throw new RuntimeException(String.format("Error during processing string '%s'", msg), ex);
            }
		}
		
		return result.toString();   	
    }
    
    private ArrayList<String> getParts(String str){
    	ArrayList<String> parts = new ArrayList<String>();
    	StringBuilder commonPart = new StringBuilder();
    	int strLenth = str.length();
    	for(int pos = 0; pos < strLenth; pos ++){
    		int offset = pos;
    		char charAtPos = str.charAt(pos);
    		if((charAtPos == '@' || charAtPos == '$' || charAtPos == '%' || charAtPos == '[') && pos < strLenth-1){
    			if(commonPart.length()>0){
    				parts.add(commonPart.toString());
    				commonPart.delete(0, commonPart.length());
    			}
    			switch(charAtPos){
    			case '@':
    			case '$':
        			do{
        				offset++;
        			}while(offset < strLenth && !str.substring(offset).matches("[@|\\$|%|\\[]+.*"));//until the str{offset,end} start with @|$|%|[ or arrive end
        			parts.add(str.substring(pos, offset));
        			pos = offset-1;
    				break;
    			case '%':
        			do{
        				offset++;
        			}while(offset < strLenth && str.charAt(offset) != '%');//until found another % or arrive end
    				if(offset == strLenth){
    					//logger.debug("Missing [%] in ["+str+"] when pairing at position ["+pos+"]!");
    	    			commonPart.append(charAtPos);//single "%" will parse as common char
    				}else{
            			parts.add(str.substring(pos, offset+1));
            			pos = offset;
    				}
    				break;
    			case '[':
    				offset = offset + 2;
        			do{
        				offset++;
        			}while(offset < strLenth && !(str.substring(pos,offset+1).matches("^\\[\\[.*\\]\\]$")));//until found "]" or arrive end
    				if(offset >= strLenth){
    					//logger.debug("Missing []] in ["+str+"] when pairing at position ["+pos+"]!");
    	    			commonPart.append(charAtPos);//single "[" will parse as common char
    				}else{
            			parts.add(str.substring(pos, offset+1));
            			pos = offset;
    				}
    				break;
    			}
    		}
    		else{
    			commonPart.append(charAtPos);  			
    		}
			if(pos == strLenth - 1 && commonPart.length() > 0){
				parts.add(commonPart.toString());
			} 
    	}
    	return parts;
    }
    private String processParams(String str) throws Exception{
    	StringBuffer sb = new StringBuffer();
        Matcher m = _paramsPattern.matcher(str);
        String param = null;
        if (m.find()) {
            String match = m.group(1);
            try {
                //logger.debug(String.format("Parameter '%s' evaluating...", match));
                param = getParam(match);
                //logger.info(String.format("Parameter '%s' evaluated to '%s'", match, param));
                param = param.replace("\\", "\\\\"); // in order to keep "/" we need to quote them
                param = param.replace("$", "\\$");  // in order to keep "$" we need to quote the
                m.appendReplacement(sb,param);              
            }
            catch (Exception e) {
                logger.warn(String.format("Parameter '%s' cannot be evaluated", match));
                throw e;
            }
            m.appendTail(sb);
            String rez = sb.toString();
            return evaluateString(rez);
        }else{
        	//logger.debug(String.format("Not Found"));
        	return str;
        }
    	
    }
    private String processVariables(String str) throws Throwable{
    	StringBuffer sb = new StringBuffer();
        Matcher m = _varPattern.matcher(str); 
        if (m.find()) {
            String full_name = m.group(1);
             //logger.debug(String.format("Variable '%s' parsing...", full_name));
            try {
                Integer i = full_name.indexOf(".");
                if (i < 0) {
                    throw new RuntimeException(String.format("Variable reference '%s' does not have field (@variable.FIELD)", full_name));
                }
                String obj_name = full_name.substring(0, i);
                String obj_field = full_name.substring(i + 1, full_name.length());
                //logger.debug(String.format("Variable '%s' parsed to '%s->%s', evaluating...", full_name, obj_name, obj_field));
                StepResult obj = getVar(obj_name);
                if (obj == null) {
                    throw new RuntimeException(String.format("Variable '%s' is null", obj_name));
                }
                String val = obj.getFieldValue(obj_field);
                //logger.debug(String.format("Variable '%s' parsed to '%s->%s', evaluated to '%s'", full_name, obj_name, obj_field, val));
                val = val.replace("\\", "\\\\"); // in order to keep "/" we need to quote them
                val = val.replace("$", "\\$");  // in order to keep "$" we need to quote them
                m.appendReplacement(sb,val);
            }
            catch (Exception e) {
                String errorStr = String.format("Error processing variable '%s'", full_name);
                logger.error(errorStr);
                throw e;
            }
            m.appendTail(sb);
            String rez = sb.toString();
            return rez;
        }else{
        	return str;
        }

    }
    private String processScripts(String str) throws ScriptException{
        Matcher m = _jsPattern.matcher(str);
        String result = null;
        if (m.find()) {
            String match = m.group(1);
            String script = evaluateString(match);
            try {
				result = getScriptResult(script);
			} catch (ScriptException e) {
				String errorStr = String.format("Error processing scripts '%s'", str);
                logger.error(errorStr);
                throw e;
			}
        }else{
        	return str;
        }
        return result;
    }
    private String proseeSelfDefine(String str){
    	Matcher m = _userStrPattern.matcher(str);
        String match = null;
        if (m.find()) {
            match = m.group(1);
        }else{
        	return str;
        }
        return match;
    }

    private boolean processPassword(StringBuilder str) {
        boolean passwordExists = false; // DONE substring can also be password - we need to consider and work with it! need some password prefix/suffix
        String rez = str.toString();
        
        if(AES.isCipherFormat(rez)){
        	//the whole string is password
        	passwordExists = true;
        	String plainString = AES.decode(rez);
        	str.replace(0, str.length(), plainString);        	
        }
        else {
        	//password is substring and prefix is '#'
        	StringBuffer sb = new StringBuffer();
        	String regex = "#([a-f0-9][a-f0-9] ){16,}";
    		Pattern pattern = Pattern.compile(regex);
    		Matcher matcher = pattern.matcher(rez);
    		    		
    		while(matcher.find()){
    			int start = rez.lastIndexOf('#', matcher.end(1));
    			String substring = rez.substring(start + 1, matcher.end(1));    			
    			if(AES.isCipherFormat(substring)) {	            
    				passwordExists = true;
    	            String plainString = AES.decode(substring);
    	            matcher.appendReplacement(sb, plainString);    	            
    	        }
    		}   
    		matcher.appendTail(sb);
    		str.replace(0, str.length(), sb.toString());
        }
        
        return passwordExists;
	}

	void setThreadParams(Map threadParams, int threadNumber) {
        _threadParams = threadParams;
        _threadNumber = threadNumber;
    }

	public String getPrivateKeyFilePath() {
		return privateKeyFilePath;
	}

	public void setPrivateKeyFilePath(String privateKeyFilePath) throws IOException {		
		this.privateKeyFilePath = privateKeyFilePath;		
		
		File file = new File(privateKeyFilePath);
		if (privateKeyFilePath != null && !privateKeyFilePath.isEmpty() && file.exists() && file.isFile()) {
			AES.setSecretKeyFilePath(privateKeyFilePath);
			secretKey = PrivateKeyGen.getKey(privateKeyFilePath);
		}
		else
			logger.warn("Private key file path is not given or correct");
	}

    @Override
    public void close() throws Exception {
        _variables.clear();
    }
}
