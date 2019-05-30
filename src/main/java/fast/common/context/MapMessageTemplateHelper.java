package fast.common.context;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cucumber.api.DataTable;
import fast.common.core.Configurator;
import fast.common.core.MapMessageTemplate;
import fast.common.core.ValidationFailed;
import fast.common.fix.DataDictionary;
import fast.common.fix.TimeHelper;
import fast.common.logging.FastLogger;
import quickfix.Field;
import quickfix.FieldMap;
import quickfix.Message;

public class MapMessageTemplateHelper {
	static FastLogger logger = FastLogger.getLogger("MapMessageTemplateHelper");

	public static final String MESSAGE_FIELD_SEP = "|";
	public static final String VALIDATE_SEPARATOR = "<<>>";
	public static final String VALIDATE_VALUE = "Value";
	public static final String VALIDATE_DATATYPE = "DataType";
	public static final String VALIDATE_OPERATOR = "Operator";
	private static final String SYMBOL_EQUAL = "=";
	private static final String SUBTEMPLATES = "SubTemplates";

	public static final String OPERATOR_EQUAL = "EQUAL";
	public static final String OPERATOR_NONEQUAL = "NONEQUAL";
	public static final String OPERATOR_GREATER = "GREATER";
	public static final String OPERATOR_GREATEROREQUAL = "GREATEROREQUAL";
	public static final String OPERATOR_LESS = "LESS";
	public static final String OPERATOR_LESSOREQUAL = "LESSOREQUAL";
	public static final String OPERATOR_CONTAIN = "CONTAIN";

	public static final String DATA_TYPE_NUMBER = "NUMBER";
	public static final String DATA_TYPE_STRING = "STRING";
	public static final String DATA_TYPE_DATETIME = "DATETIME";
	public static final DateTimeFormatter dfs = new DateTimeFormatterBuilder()
			.appendOptional(TimeHelper.nanosecondsTimeFormatter)
			.appendOptional(TimeHelper.microsecondsTimeFormatter)
			.appendOptional(TimeHelper.millisecondsTimeFormatter)
			.appendOptional(TimeHelper.secondsTimeFormatter)
			.appendOptional(TimeHelper.dateFormatter)
			.appendOptional(TimeHelper.microsecondsTimeFormatter)
			.appendOptional(TimeHelper.timeFormatter).toFormatter()
			.withZone(ZoneId.of("UTC"));
	
	/**
	 * @param existingtemplates: have to give non-null reference
	 * @param fixDataDictionary: only required for FIX templates
	 */
	public static void populateTemplateMap(Map map, String templateGroupName, Map oldValue,
			Map existingtemplates, DataDictionary fixDataDictionary) {
		Map templateMap = Configurator.getMapOr(map, templateGroupName, oldValue);
		if (templateMap == null) 
			return ;
		Iterator<Map.Entry<String, Object>> entries = templateMap.entrySet()
				.iterator();
		while (entries.hasNext()) {
			Map.Entry<String, Object> entry = entries.next();
			String entryKey = entry.getKey();
			Object entryObject = entry.getValue();
			MapMessageTemplate template = generateTemplate(entryKey,
					entryObject, existingtemplates, fixDataDictionary);
			existingtemplates.put(entryKey, template);
		}
	}
	
	public static MapMessageTemplate generateTemplate(String templateName,
			Object object, Map<String, MapMessageTemplate> existingtemplates,
			DataDictionary dataDictionary) {
		StringBuilder fieldValueString = new StringBuilder();
		MapMessageTemplate template;
		if (existingtemplates.get(templateName) != null) {
			template = new MapMessageTemplate(templateName,existingtemplates.get(templateName).getAllValidateFields()); 
			fieldValueString.append(existingtemplates.get(templateName).getFieldValueString());
		} else {
			Map<String, Map<String, String>> validateField = new LinkedHashMap<String, Map<String, String>>();
			template = new MapMessageTemplate(templateName, validateField);
		}
		
		if (object instanceof Map) {
			Map map = (Map) object;
			for (Object field : map.keySet()) {
				String elementKey = field.toString();
				String elementValue = map.get(field).toString();
				parseFieldValue(elementKey, elementValue, template,
						fieldValueString, existingtemplates, dataDictionary);
			}
		} else if (object instanceof ArrayList) {
			ArrayList<Map<String, String>> arraylist = (ArrayList<Map<String, String>>) object;
			for (Map<String, String> field : arraylist) {
				Map.Entry<String, String> elementEntry = field.entrySet()
						.iterator().next();
				String elementKey = elementEntry.getKey();
				String elementValue = elementEntry.getValue();
				parseFieldValue(elementKey, elementValue, template,
						fieldValueString, existingtemplates, dataDictionary);
			}
		} else if (object instanceof String) {
			String messageString = object.toString();
			if (!messageString.contains(VALIDATE_SEPARATOR)) {
				fieldValueString.append(messageString);
				if (messageString.lastIndexOf(MESSAGE_FIELD_SEP) != messageString
						.length()-1)
					fieldValueString.append(MESSAGE_FIELD_SEP);
			} else {
				String[] arrayStrings = messageString.split("\\"
						+ MESSAGE_FIELD_SEP);
				for (String field : arrayStrings) {
					int pos = field.indexOf(SYMBOL_EQUAL);
					if (pos < 0) {
						throw new RuntimeException(
								String.format(
										"please check format with '%s' in message string [%s]",
										arrayStrings, messageString));
					}
					String fieldName = field.substring(0, pos);					
					if(dataDictionary != null){
						String fixTag = "";
						try {
							fixTag = String.valueOf(Integer.parseInt(fieldName));
						} catch (Exception e) {						
							fixTag = String.valueOf(dataDictionary
								.getFieldTag(fieldName));						
						}
						fieldName = fixTag;
					}
					
					String elementValue = field.substring(pos + 1);
					if (elementValue.contains(VALIDATE_SEPARATOR)) {
						Map<String, String> element = generateValueMap(elementValue);
						template.addValidateField(fieldName, element);
						fieldValueString.append(fieldName + SYMBOL_EQUAL
								+ element.get(VALIDATE_VALUE)
								+ MESSAGE_FIELD_SEP);
					} else {
						fieldValueString.append(fieldName + SYMBOL_EQUAL
								+ elementValue + MESSAGE_FIELD_SEP);
					}
				}
			}
		}
		template.setFieldValueString(fieldValueString.toString());
		return template;
	}

	public static void parseFieldValue(String elementKey, String elementValue,
			MapMessageTemplate template, StringBuilder fieldValueString,
			Map<String, MapMessageTemplate> fixTemplates, DataDictionary dataDictionary) {
		if (elementKey.equals(SUBTEMPLATES)) {
			String[] commons = elementValue.split("\\" + MESSAGE_FIELD_SEP);
			for (String common : commons) {
				Iterator<Map.Entry<String, Map<String, String>>> commonEntries = fixTemplates
						.get(common).getAllValidateFields().entrySet()
						.iterator();
				while (commonEntries.hasNext()) {
					Map.Entry<String, Map<String, String>> commonEntry = commonEntries
							.next();
					String commonEntryName = commonEntry.getKey();
					Map<String, String> commonTemplateValue = commonEntry
							.getValue();
					template.addValidateField(commonEntryName,
							commonTemplateValue);
				}

				fieldValueString.append(fixTemplates.get(common)
						.getFieldValueString());
			}
		} else {
			String fieldKey = elementKey;
			if(dataDictionary != null){
				String fixTag = "";
				try {
					fixTag = String.valueOf(Integer.parseInt(fieldKey));
				} catch (Exception e) {
					fixTag = String.valueOf(dataDictionary
							.getFieldTag(fieldKey));
				}
				fieldKey = fixTag;
			}
			if (elementValue.contains(VALIDATE_SEPARATOR)) {
				Map<String, String> element = generateValueMap(elementValue);
				template.addValidateField(fieldKey, element);
				fieldValueString.append(fieldKey + SYMBOL_EQUAL
						+ element.get(VALIDATE_VALUE) + MESSAGE_FIELD_SEP);
			} else {
				fieldValueString.append(fieldKey + SYMBOL_EQUAL + elementValue
						+ MESSAGE_FIELD_SEP);
			}
		}
	}

	private static Map<String, String> generateValueMap(String elementValue) {
		String elementValidate[] = elementValue.split(VALIDATE_SEPARATOR);
		Map<String, String> element = new LinkedHashMap<String, String>();
		element.put(VALIDATE_VALUE, elementValidate[0]);
		element.put(VALIDATE_DATATYPE, elementValidate[1]);
		element.put(VALIDATE_OPERATOR, elementValidate[2]);
		return element;
	}


	public static Object convertFieldObject(String fieldValue, String dataType)
			throws ValidationFailed {
		Object convertedValue;
		dataType=dataType.toUpperCase();
		if (dataType.equals(DATA_TYPE_NUMBER)) {
			convertedValue = Double.parseDouble(fieldValue);
		} else if (dataType.equals(DATA_TYPE_DATETIME)) {
			convertedValue = ZonedDateTime.parse(fieldValue, dfs);
		} else if (dataType.equals(DATA_TYPE_STRING)) {
			convertedValue = fieldValue;
		} else
			throw new ValidationFailed(String.format("Invalid data type %s:",
					dataType));
		return convertedValue;
	}

	public static boolean operatorEqual(Object actualValue, Object exceptedValue) {
		boolean validateResult = false;
		if (actualValue instanceof String)
			validateResult = actualValue.toString().equals(	exceptedValue.toString());
		else if (actualValue instanceof Double)
			validateResult = ((Double) actualValue).compareTo((Double) exceptedValue) == 0;
		else
			validateResult = ((ZonedDateTime) actualValue).equals((ZonedDateTime) exceptedValue);
		
		return validateResult;
	}

	public static boolean operatorGreater(Object actualValue,
			Object exceptedValue) throws ValidationFailed {
		boolean validateResult = false;
		if (actualValue instanceof ZonedDateTime)
			validateResult = ((ZonedDateTime) actualValue)
					.isAfter((ZonedDateTime) exceptedValue);
		else if (actualValue instanceof Double)
			validateResult = ((Double) actualValue)
					.compareTo((Double) exceptedValue) > 0 ? true : false;
		else
			throw new ValidationFailed(String.format(
					"Invalid operator for datatype String:"));
		return validateResult;

	}

	public static boolean validateWithField(String actualValue, String exceptedValue, String dataType, String operator) throws ValidationFailed {
		if(actualValue == null){
			throw new ValidationFailed("Actual Value is null");
		}
		boolean validateResult = false;
		Object actualObject = convertFieldObject(actualValue, dataType);
		Object exceptedObject = convertFieldObject(exceptedValue, dataType);

		switch (operator.trim().toUpperCase()) {
		case OPERATOR_EQUAL:
			validateResult = operatorEqual(actualObject, exceptedObject);
			break;
		case OPERATOR_NONEQUAL:
			validateResult = !operatorEqual(actualObject, exceptedObject);
			break;
		case OPERATOR_CONTAIN:
			if (dataType.toUpperCase().equals(DATA_TYPE_STRING))
				validateResult = actualValue.contains(exceptedValue);
			else
				throw new ValidationFailed(String.format(
						"Invalid operator %s for data type %s:", operator,
						dataType));
			break;
		case OPERATOR_GREATER:
			validateResult = operatorGreater(actualObject, exceptedObject);
			break;
		case OPERATOR_GREATEROREQUAL:
			validateResult = operatorGreater(actualObject, exceptedObject)
					|| operatorEqual(actualObject, exceptedObject);
			break;
		case OPERATOR_LESS:
			validateResult = !operatorGreater(actualObject, exceptedObject)
					&& !operatorEqual(actualObject, exceptedObject);
			break;
		case OPERATOR_LESSOREQUAL:
			validateResult = !operatorGreater(actualObject, exceptedObject)
					|| operatorEqual(actualObject, exceptedObject);
			break;
		default:
			break;
		}
		return validateResult;
	}
	
	public static String tableToUserstr(DataTable table) {
		StringBuilder sb = new StringBuilder();
		List<List<String>> rawTable = table.raw();
		for (int i = 0; i < rawTable.size(); i++) {
			List<String> row = rawTable.get(i);
			String tag = row.get(0);
			String value = row.get(1);
			sb.append(tag);
			sb.append("=");
			sb.append(value);
			sb.append("|");
		}
		return sb.toString();
	}
	
	public static MapMessageTemplate processTemplate(ScenarioContext scenarioContext, MapMessageTemplate template){
		template.setFieldValueString(scenarioContext.processString(template.getFieldValueString()));
		Map<String, Map<String, String>> processedValidateFields = new HashMap<String, Map<String, String>>();
		Map<String, Map<String, String>> validateFields = template.getAllValidateFields();
		for(Entry<String, Map<String, String>> entry : validateFields.entrySet()){
			Map<String, String> processedValue = new HashMap<String, String>();
			for(String key : entry.getValue().keySet()){
				processedValue.put(key, scenarioContext.processString(entry.getValue().get(key)));
			}
			processedValidateFields.put(entry.getKey(), processedValue);
		}
		
		template.setAllValidateFields(processedValidateFields);
		return template;
	}
	public static String setMessageFieldSep(String rawString){
		String[] messageFields;
		if (rawString.contains("")) {
			messageFields = rawString.split("");
		} else if (rawString.contains("^A")) {
			messageFields = rawString.split("\\^A");
		} else if (rawString.contains("~")) {
			messageFields = rawString.split("~");
		} else {
			messageFields = rawString.split("\n\t\t");
		}

		StringBuilder message = new StringBuilder();
		for (int i = 0; i < messageFields.length; i++) {
			message.append(HelperMethods.EscapseReservedCharacters(messageFields[i]) + "|");
		}
		return message.toString();
	}
	
	public static void validateFieldByDefault(int tag,String actualValue, String expectedValue,StringBuilder diffBuilder) throws ValidationFailed{
		if ((expectedValue.length() > 2) && (expectedValue.charAt(0) == '/')
				&& (expectedValue.charAt(expectedValue.length() - 1) == '/')) {
			String rx = expectedValue.substring(1, expectedValue.length() - 1);
			Pattern p = Pattern.compile(rx);
			Matcher m = p.matcher(actualValue);
			if (!m.find()) {
				diffBuilder.append(tag + "=(" + actualValue + ")(" + expectedValue + ")(" + MapMessageTemplateHelper.OPERATOR_EQUAL
						+ ")\n");
			}
		}else if (!validateWithField(actualValue,expectedValue , DATA_TYPE_STRING,OPERATOR_EQUAL))
			diffBuilder.append(tag + "=(" + actualValue + ")(" + expectedValue + ")(" + MapMessageTemplateHelper.OPERATOR_EQUAL
					+ ")\n");
	}
	public static void getExceptedVerifyMessage(FieldMap expectedMessage,MapMessageTemplate userMapMessage,StringBuilder expected){
		Iterator<Field<?>> expectedIter = expectedMessage.iterator();
		while (expectedIter.hasNext()) {
			Field<?> expectedField = expectedIter.next();
			int tag = expectedField.getTag();
			Map validateItem = userMapMessage.getValidateField(String
					.valueOf(tag));
			if (validateItem != null) {
				expected.append(tag+ "("+ validateItem.get(MapMessageTemplateHelper.VALIDATE_OPERATOR).toString() + ")"+ expectedField.getObject().toString() + "|");
			} else {
				expected.append(tag + "="+ expectedField.getObject().toString() + "|");
			}
		}
	}
}
