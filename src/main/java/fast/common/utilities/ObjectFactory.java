package fast.common.utilities;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import org.ini4j.Ini;
import org.ini4j.Profile.Section;
import com.thoughtworks.paranamer.*;
import fast.common.logging.FastLogger;

public class ObjectFactory {
	protected static FastLogger log;;

	public static <T> T getInstance(Class<T> classz, String configFilePath, String sectionName) {

		Ini ini = null;
		try {
			ini = new Ini(new File(configFilePath));
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			log.error(e1.getMessage());
		}
		Section sec = ini.get(sectionName);

		Set<String> keys = sec.keySet();

		List<String> validParamName = new ArrayList<String>();
		List<Class> validParamClass = new ArrayList<Class>();
		Map<String, Class> validParamMap = new HashMap<String, Class>();
		Paranamer paramInfo = new CachingParanamer(new AnnotationParanamer(new BytecodeReadingParanamer()));

		Constructor[] constructors = classz.getConstructors();
		for (Constructor constructor : constructors) {

			String[] parameterNames = new String[] {};
			try {
				parameterNames = paramInfo.lookupParameterNames(constructor);
				Class[] paramTypes = constructor.getParameterTypes();
				int i = 0;
				for (String string : parameterNames) {
					if (!validParamName.contains(string)) {
						validParamName.add(string);
						validParamClass.add(paramTypes[i]);
					}
					i++;
				}
				i = 0;
				for (String string : validParamName) {
					validParamMap.put(string, validParamClass.get(i++));
				}
			} catch (Exception e) {
				log.error("Error: " + e);
			}
		}

		// If key does not contain some param, remove it
		for (String paramName : validParamName) {
			if (!keys.contains(paramName)) {
				validParamMap.remove(paramName);
			}
		}

		boolean parameterCountMatched = false;
		for (Constructor constructor : constructors) {
			if (constructor.getParameterCount() == validParamMap.size()) {
				parameterCountMatched = true;
				// constructor.newInstance()

				String[] parameterNames = paramInfo.lookupParameterNames(constructor);
				Object[] objectArgs = new Object[parameterNames.length];
				int i = 0;
				for (String param : parameterNames) {
					objectArgs[i++] = convertStringType(sec.fetch(param).trim(), validParamMap.get(param),
							configFilePath);
				}

				try {
					return (T) constructor.newInstance(objectArgs);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					log.error("Class initialization error" + e);
					continue;
				}

			}
		}

		return null;
	}

	private static <T> Object convertStringType(String value, Class<T> clazz, String configFilePath) {

		if (clazz.equals(String.class)) {
			return value;
		} else if (clazz.isEnum()) {
			try {
				return Enum.valueOf((Class<Enum>) clazz, value);
			} catch (Exception e) {
				try {
					return Enum.valueOf((Class<Enum>) clazz, value.toUpperCase());
				} catch (Exception e2) {
					// TODO: handle exception
					log.error("Enum conversion" + e2);
				}
			}

		} else {

			if (clazz.equals(int.class) || clazz.equals(Integer.class)) {
				return Integer.parseInt(value);
			} else if (clazz.equals(double.class) || clazz.equals(Double.class)) {
				return Double.parseDouble(value);
			} else if (clazz.equals(long.class) || clazz.equals(Long.class)) {
				return Long.parseLong(value);
			} else if (clazz.equals(boolean.class) || clazz.equals(Boolean.class)) {
				return Boolean.parseBoolean(value);
			} else {
				return getInstance(clazz, configFilePath, value);
				// throw new RuntimeException("Incorrect Type" +
				// clazz.getSimpleName());
			}
		}

		return value;

	}

	public static <T> T getInstance(Class<T> classz, Map params) {

		Set<String> keys = params.keySet();
		Map<String, String> newParams = new HashMap<String, String>();
		for (String string : keys) {
			newParams.put(string.toLowerCase(), params.get(string).toString());
		}
		Set<String> newkeys = newParams.keySet();

		Map<String, Class> validParamMap = new HashMap<String, Class>();
		List<String> validParamName = new ArrayList<String>();
		List<Class> validParamClass = new ArrayList<Class>();

		Paranamer paramInfo = new CachingParanamer(new AnnotationParanamer(new BytecodeReadingParanamer()));

		Constructor[] constructors = classz.getConstructors();
		for (Constructor constructor : constructors) {

			String[] parameterNames = new String[] {};
			try {
				parameterNames = paramInfo.lookupParameterNames(constructor);
				Class[] paramTypes = constructor.getParameterTypes();
				int i = 0;
				for (String string : parameterNames) {
					if (!validParamName.contains(string)) {
						validParamName.add(string.toLowerCase());
						// validParamName.add(string);
						validParamClass.add(paramTypes[i]);
					}
					i++;
				}
				i = 0;
				for (String string : validParamName) {
					// validParamMap.put(string, validParamClass.get(i++));
					validParamMap.put(string.toLowerCase(), validParamClass.get(i++));
				}
			} catch (Exception e) {
				log.error("Error: " + e);
			}
		}

		// If key does not contain some param, remove it
		for (String paramName : validParamName) {

			if (!newkeys.contains(paramName)) {
				validParamMap.remove(paramName);
			}
		}

		@SuppressWarnings("unused")
		boolean parameterCountMatched = false;
		for (Constructor constructor : constructors) {
			if (constructor.getParameterCount() == validParamMap.size()) {
				parameterCountMatched = true;
				// constructor.newInstance()

				String[] parameterNames = paramInfo.lookupParameterNames(constructor);
				Object[] objectArgs = new Object[parameterNames.length];
				int i = 0;
				for (String param : parameterNames) {
					// setting params values with converted types
					objectArgs[i++] = convertStringType(newParams.get(param.toLowerCase()).toString().trim(),
							validParamMap.get(param.toLowerCase()));
				}

				try {
					Object obj = constructor.newInstance(objectArgs);
					return classz.cast(obj);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					log.error("Class initialization error" + e);
					continue;
				}
			}
		}

		return null;

	}

	private static <T> Object convertStringType(String value, Class<T> clazz) {

		if (clazz.equals(String.class)) {
			return value;
		} else if (clazz.isEnum()) {
			try {
				return Enum.valueOf((Class<Enum>) clazz, value);
			} catch (Exception e) {
				try {
					return Enum.valueOf((Class<Enum>) clazz, value.toUpperCase());
				} catch (Exception e2) {
					// TODO: handle exception
					log.error("Enum conversion" + e2);
				}
			}

		} else {

			if (clazz.equals(int.class) || clazz.equals(Integer.class)) {
				return Integer.parseInt(value);
			} else if (clazz.equals(double.class) || clazz.equals(Double.class)) {
				return Double.parseDouble(value);
			} else if (clazz.equals(long.class) || clazz.equals(Long.class)) {
				return Long.parseLong(value);
			} else if (clazz.equals(boolean.class) || clazz.equals(Boolean.class)) {
				return Boolean.parseBoolean(value);
			} else {

				throw new RuntimeException("Incorrect Type" + clazz.getSimpleName());
			}
		}

		return value;

	}
	
	/**
	 * Convert String to a given type of Object
	 * @param value the given string value
	 * @param type the target type containing method valueof(String)
	 * @return object in given type
	 * @since 1.9
	 * @throws RuntimeException
	 */
	public static Object parseTypeFromString(String value, Class<?> type) throws RuntimeException{
		try{
			return type.getMethod("valueOf", String.class).invoke(null, value);
		}catch(Exception ex){
			throw new RuntimeException("Failed to convert " + value + " to type " + type.getName() + ", " + ex.getMessage());
		}
	}
}
