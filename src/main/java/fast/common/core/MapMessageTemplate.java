package fast.common.core;

import java.util.LinkedHashMap;
import java.util.Map;

public class MapMessageTemplate {

	private String templateName;
	private String fieldValueString;
	private Map<String, Map<String, String>> validateField;

	public MapMessageTemplate(String templateName) {	
		this.templateName = templateName;
	}
	
	public MapMessageTemplate(String templateName,  Map<String, Map<String, String>> validateField) {
		this.templateName = templateName;
		this.validateField = new LinkedHashMap<String, Map<String, String>>();
		this.validateField.putAll(validateField);
	}
	
	public MapMessageTemplate(String templateName, String fieldValueString, Map<String, Map<String, String>> validateField) {
		this.templateName = templateName;
		this.fieldValueString = fieldValueString;
		this.validateField = validateField;
	}

	public String getTemplateName() {
		return templateName;
	}

	public void setTemplateName(String templateName) {
		this.templateName = templateName;
	}

	public String getFieldValueString() {
		return fieldValueString;
	}

	public void setFieldValueString(String fieldValueString) {
		this.fieldValueString = fieldValueString;
	}

	public Map<String, Map<String, String>> getAllValidateFields() {
		return validateField;
	}

	public int getAllValidateFieldSize() {
		return validateField.size();
	}

	public Map<String, String> getValidateField(String field) {
		return validateField.get(field);
	}


	public void setAllValidateFields(Map<String, Map<String, String>> validateField) {
		this.validateField = validateField;
	}
	
	public void addValidateField(String field, Map<String, String> validateField) {
		this.validateField.put(field, validateField);
	}
}
