package fast.common.context;

import fast.common.logging.FastLogger;
import fast.common.core.ValidationFailed;
import fast.common.fix.FixHelper;
import gherkin.formatter.model.Scenario;
import quickfix.FieldNotFound;
import quickfix.InvalidMessage;

import javax.xml.xpath.XPathExpressionException;

import java.util.ArrayList;

public class FixStepResult extends StepResult implements IStringResult {
    private quickfix.Message _actualMessage = null;
    private ArrayList<quickfix.Message> _actualMessageList;
    FixHelper _fixHelper;
    static FastLogger logger = FastLogger.getLogger("FixStepResult");


    public FixStepResult(quickfix.Message actualMessage, FixHelper fixHelper) {
        _fixHelper = fixHelper;
        _actualMessage = actualMessage;
        _actualMessageList = null;
    }

    public FixStepResult(ArrayList<quickfix.Message> actualMessageList, FixHelper fixHelper) {
        _fixHelper = fixHelper;
        _actualMessageList = actualMessageList;
        _actualMessage = null;
    }

    public String toString() {
        if(_actualMessage != null) {
        	String tempMsg = _actualMessage.toString();
        	return tempMsg.replaceAll(new Character('\001') + "", "|");
        }
        if(_actualMessageList != null) {
            StringBuilder sb = new StringBuilder();
            for(quickfix.Message msg: _actualMessageList) {
            	String tempMsg = msg.toString();
                sb.append(tempMsg.replaceAll(new Character('\001') + "", "|"));
                sb.append("\r\n");
            }
            return sb.toString();
        }
        return "<Empty FixStepResult!>";
    }

    public String getFieldValue(String field) throws XPathExpressionException, FieldNotFound {
        return _fixHelper.getMessageFieldValue(field, _actualMessage);
    }

    public ArrayList<String> getFieldsValues(String field) {
        ArrayList<String> values = new ArrayList<String>();
        for(quickfix.Message msg: _actualMessageList) {
            try {
                String value = _fixHelper.getMessageFieldValue(field, msg);
                values.add(value);
            }
            catch (Exception ex){ // do nothing here
            }
        }
        return values;
    }
    public String getActualMessage(){
    	String tempMsg = HelperMethods.EscapseReservedCharacters(_actualMessage.toString());
    	return tempMsg.replaceAll(new Character('\001') + "", "|");
    }
    public void contains(ScenarioContext scenarioContext, String userstr) throws XPathExpressionException, FieldNotFound, InvalidMessage {
        _fixHelper.checkMessageContainsUserstr(scenarioContext, _actualMessage, userstr);
    }

    @Override
    public void contains(String userstr) throws XPathExpressionException, FieldNotFound, InvalidMessage {
        _fixHelper.checkMessageContainsUserstr(null, _actualMessage, userstr);
    }
    
    public void not_contains(String userstr) throws Throwable {
        _fixHelper.checkMessageNotContainsUserstr(null, _actualMessage, userstr);
    }

    public String validate(ScenarioContext scenarioContext,String varName,String template,String userstr) throws XPathExpressionException, InvalidMessage, FieldNotFound, ValidationFailed {
    	return _fixHelper.validate(scenarioContext,varName, template, userstr);
    }
}
