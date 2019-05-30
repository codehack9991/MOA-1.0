package fast.common.core;

import com.esotericsoftware.yamlbeans.YamlException;

import fast.common.logging.FastLogger;
import fast.common.agents.Agent;
import fast.common.cipher.*;
import fast.common.fix.DataDictionary;

import com.esotericsoftware.yamlbeans.YamlReader;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.nio.file.Paths;
import java.util.*;

public class Configurator {
    private static FastLogger logger = FastLogger.getLogger("Configurator");

    public static final String CONFIG_FILENAME_SETTING = "configFilename"; // default: config/config.yml
    public static final String DEFAULT_CONFIG_FILENAME = "config/config.yml";
    public static final String CONFIG_ENVIRONMENTNAME_SETTING = "environmentName";
    public static final String CONFIG_USERNAME_SETTING = "userName";
    public static final String  SECRETKEYFILEPATH = "secretKeyFile";
    public static final String USERS_FOLDER = "users";
    public static final String ENVIRONMENTS_FOLDER = "environments";
    public static final String SUB_CONFIGS = "subconfigs";
    public static final String AGENTS_MAP_NAME = "Agents";

    private String configFolder;
    
	private Map settingsMap;

	public String getConfigFolder() {
		return configFolder;
	}

	public void setConfigFolder(String configFolder) {
		this.configFolder = configFolder;
	}

	public Map getSettingsMap() {
		return settingsMap;
	}

	public void setSettingsMap(Map settingsMap) {
		this.settingsMap = settingsMap;
	}

	private static Configurator _instance = null;
    public static Configurator getInstance() throws Exception {
        synchronized (Configurator.class) {
            if(_instance == null) {
                _instance = new Configurator();
            }
            return _instance;
        }
    }

    public static boolean IsSingleThread() {
        String str = System.getProperty("forkCount");
        boolean isSingleTread = true;
        if (str != null) {
            isSingleTread = false;
            if (str.equals("1")) {
                isSingleTread = true;
            }
        }
        return isSingleTread;
    }

    public Agent createAgent(String agentName) throws Exception {
        Map agentsMap = (Map) _instance.settingsMap.get(AGENTS_MAP_NAME);
        Map agentParams = (Map) agentsMap.get(agentName);
        if (agentParams == null) {
            throw new RuntimeException(String.format("settings for agent '%s' not found", agentName));
        }
		//processAgentParams(agentParams); KT replaced with decodeSettings for whole map
        logger.debug(String.format("creating agent '%s' with params [%s]", agentName, agentParams));
        String class_name = agentParams.get("class_name").toString();

        Class<?> class_ = Class.forName(class_name);
        Constructor<?> ctor = class_.getConstructor(String.class, Map.class, Configurator.class);
        return (Agent) ctor.newInstance(agentName, agentParams, _instance);
    }
	
	private void decodeSettings(Map<String, String> settingsMap) {
        // now need to deep copy srcMap settings to dstMap
        if(settingsMap == null)
            return;

        Iterator srcIter = settingsMap.keySet().iterator();
        while(srcIter.hasNext()) {
            String key = (String)srcIter.next();
            Object srcObj = settingsMap.get(key);
            if(srcObj instanceof Map) {
                Map subSrcMap = (Map)srcObj;
                decodeSettings(subSrcMap); // deep decode
            }
            else if(srcObj instanceof String){ // then just copy the value
                String value = (String)srcObj;
                String realValue = AES.isCipherFormat(value) ? AES.decode(value) : value;
                settingsMap.put(key, realValue);
            }
        }
	}

    private Configurator() throws Exception {
        try {
            LoadConfig();
        }
        catch (Exception e) {
            logger.error(e.toString());
           
            throw e;
        }
    }
	
	private void setSecretKeyFile() throws IOException {
    	String sFile = (String) settingsMap.get(SECRETKEYFILEPATH);
    	if (sFile != null) {
    		AES.setSecretKeyFilePath((sFile) );
    		logger.info(String.format("Set AES Secret key file path: %s", sFile));
    	}
    	else
    		logger.warn("Secret key file path is not specifed.");
	}

	
	//load sub configuration when the subconfigs path is set
	public void loadSubconfig() throws FileNotFoundException, YamlException {
		
		if (settingsMap.get(SUB_CONFIGS) == null || settingsMap.get(SUB_CONFIGS).toString().isEmpty()) {
			return;
		}
		String filePath = settingsMap.get(SUB_CONFIGS).toString();
		readSubConfig(filePath);
		
	}
	
	protected Map readSubConfig(String repoPath) throws FileNotFoundException, YamlException {

		File dir = new File(repoPath);

		if (dir.isDirectory()) {
			File[] files = dir.listFiles();
			for (int i = 0; i < files.length; i++) {
				if (files[i].isDirectory()) {
					settingsMap = readSubConfig(files[i].getPath());
				} else if (files[i].isFile()) {
					Map fs = (Map) new YamlReader(new FileReader(files[i].getPath())).read();
					mergeSettings(settingsMap, fs);
					logger.info(String.format("Loaded additional config from: '%s'", files[i].getPath()));
				}
			}
		} else if (dir.isFile()) {
			Map fs = (Map) new YamlReader(new FileReader(dir.getPath())).read();
			mergeSettings(settingsMap, fs);

			logger.info(String.format("Loaded additional repo from: '%s'", dir.getPath()));
		}

		return settingsMap;

	}

    // deep copy to dstMap from srcMap
    private void mergeSettings(Map dstMap, Map srcMap) {
        // now need to deep copy srcMap settings to dstMap
        if(srcMap == null)
            return;

        Iterator srcIter = srcMap.keySet().iterator();
        while(srcIter.hasNext()) {
            String key = (String)srcIter.next();
            Object srcObj = srcMap.get(key);
            if(srcObj instanceof Map) {
                Map subSrcMap = (Map)srcObj;
                Map subDstMap = (Map)dstMap.get(key);
                if(subDstMap == null) { // then create map
                    subDstMap = new HashMap();
                    dstMap.put(key, subDstMap);
                }
                mergeSettings(subDstMap, subSrcMap); // deep copy
            }
            else if(srcObj instanceof ArrayList) {
                ArrayList subSrcArr = (ArrayList)srcObj;
                ArrayList subDstArr = (ArrayList)dstMap.get(key);
                if(subDstArr == null) { // then create map
                    subDstArr = new ArrayList();
                    dstMap.put(key, subDstArr);
                }
                subDstArr.addAll(subSrcArr);
            }
            else { // then just copy the value
                dstMap.put(key, srcObj);
            }
        }
    }

    private Map readSettings(String folderName, String fileName) throws Exception {
        Map result = null;
        if(fileName != null) {
            String elementFileName = fileName + ".yml"; 

            String elementFullFileName = Paths.get(configFolder, folderName).resolve(elementFileName).toString();
            logger.debug(String.format("Loading additional config from: '%s'", elementFullFileName));
            result = readYaml(elementFullFileName);
            logger.info(String.format("Loaded additional config from: '%s'", elementFullFileName));
            logger.debug(String.format("%s", result));
        }

        return result;
    }
    
    public static Map readYaml(String filename) throws Exception{
    	YamlReader yamlReader =null;
        try {
             yamlReader = new YamlReader(new FileReader(filename));

		} catch (Exception e) {
            logger.info(String.format("yamlReader config error: '%s'", e.getMessage()));
		}
        if(yamlReader == null) {
            throw new Exception(String.format("Can't read YAML file '%s'", filename));
        }
        return (Map) yamlReader.read();
    }

    private String getGlobalPropertyValue(Map settingsMap, String propertyName) {
        String result = System.getProperty(propertyName);
        if(result == null) {
            Object resultObj = settingsMap.get(propertyName);
            if(resultObj != null) {
                result = resultObj.toString();
                logger.info(String.format("System.Property '%s' is not set. Using default value from config file: '%s'", propertyName, result));
            }
            else {
                logger.info(String.format("System.Property '%s' is not set. Default value in config file is also not set", propertyName));
            }
        }
        else {
            logger.info(String.format("System.Property '%s' is set to '%s'", propertyName, result));
        }

        return result;
    }

    private void LoadConfig() throws Exception {
        // read config filename
       String currentDir = new java.io.File(".").getCanonicalPath();
       logger.info(String.format("Current directory: %s", currentDir));

        String config_filename = System.getProperty(CONFIG_FILENAME_SETTING);
        if(config_filename == null) {
            logger.info(String.format("System.Property '%s' is not set. Using default value: '%s'", CONFIG_FILENAME_SETTING, DEFAULT_CONFIG_FILENAME));
            config_filename = DEFAULT_CONFIG_FILENAME;
        }

        // get config folder name - we will use it as a base folder for settings
        configFolder = Paths.get(config_filename).getParent().toString();

        // read global settings from config file
        YamlReader yamlReader = new YamlReader(new FileReader(config_filename));
        settingsMap = (Map) yamlReader.read();
        
        // env name - can be null
        String environment_name = getGlobalPropertyValue(settingsMap, CONFIG_ENVIRONMENTNAME_SETTING);

        // user name - can be null
        String user_name = getGlobalPropertyValue(settingsMap, CONFIG_USERNAME_SETTING);

        logger.info(String.format("Loaded config: %s='%s', %s='%s',  %s='%s':\r\n%s",
                CONFIG_FILENAME_SETTING, config_filename,
                CONFIG_ENVIRONMENTNAME_SETTING, environment_name,
                CONFIG_USERNAME_SETTING, user_name,
                settingsMap));


        // read environment settings
        Map environment_settings_map = readSettings(ENVIRONMENTS_FOLDER, environment_name);

        // read user settings
        Map user_settings_map = readSettings(USERS_FOLDER, user_name);

        // merge all settings - first environment, then user
        mergeSettings(settingsMap, environment_settings_map);
        mergeSettings(settingsMap, user_settings_map);
		loadSubconfig();
		// now everything is inside settingsMap
        logger.debug(String.format("Loaded and merged settings: %s", settingsMap));
        // now decode encoded fields (can be anywhere)
        setSecretKeyFile();
        decodeSettings(settingsMap);  
    }

 
    private static Object getObject(Map map, String paramName) {
        Object obj = map.get(paramName);
        if (obj == null) {
            throw new RuntimeException(String.format("Parameter '%s' not found", paramName));
        }
        return obj;
    }

    public static boolean getBoolean(Map map, String paramName) {
        Object obj = getObject(map, paramName);
        return Boolean.parseBoolean(obj.toString());
    }

    public static boolean getBooleanOr(Map map, String paramName, boolean oldValue) {
        try {
            return getBoolean(map, paramName);
        }
        catch (Exception e) {
            return oldValue;
        }
    }


    public static String getString(Map map, String paramName) {
        Object obj = getObject(map, paramName);
        return obj.toString();
    }

    public String getFilename(Map map, String paramName) {
        String filename = Configurator.getString(map, paramName);
        return Paths.get(configFolder).resolve(filename).toString();
    }

    public String getFilenameOr(Map map, String paramName, String oldValue) {
        try {
            return getFilename(map, paramName);
        }
        catch (Exception e) {
            return oldValue;
        }
    }

    public static Map getMap(Map map, String paramName) {
        Object obj = getObject(map, paramName);
        return (Map)obj;
    }

    public static Map getMapOr(Map map, String paramName, Map oldValue) {
        try {
            return getMap(map, paramName);
        }
        catch (Exception e) {
            return oldValue;
        }
    }

    public static String getStringOr(Map map, String paramName, String oldValue) {
        try {
            return getString(map, paramName);
        }
        catch (Exception e) {
            return oldValue;
        }
    }


    public static int getInt(Map map, String paramName) {
        String str = getString(map, paramName);
        int result = 0;
        try {
            result = Integer.parseInt(str);
        }
        catch (Exception e) {
            throw new RuntimeException(String.format("Parameter '%s' is not an integer: '%s'", paramName, str));
        }
        return result;
    }

    public static <T extends Object> ArrayList<T> getArrayList(Map map, String paramName) {
        Object obj = getObject(map, paramName);
        ArrayList<T> arrayList = null;
        try {
            arrayList = (ArrayList<T>) obj;
        }
        catch (Exception e){
            throw new RuntimeException(String.format("Parameter '%s' is not an array list: '%s'", paramName, obj.toString()));
        }
        return arrayList;
    }

    public static <T extends Object> ArrayList<T> getArrayListOr(Map map, String paramName, ArrayList<T> oldValue) {
        try {
            return getArrayList(map, paramName);
        }
        catch (Exception e){
            return oldValue;
        }
    }

    public static <T extends Object> Set<T> getSetOr(Map map, String paramName, Set<T> oldValue) {
        try {
            return getSetOr(map, paramName);
        }
        catch (Exception e){
            return oldValue;
        }
    }

    private static <T extends Object> Set<T> getSetOr(Map map, String paramName) {
        Object obj = getObject(map, paramName);
        Set<T> set = null;
        try {
            ArrayList<T> arrayList = (ArrayList<T>) obj;
            set = new HashSet<>(arrayList);
        }
        catch (Exception e){
            throw new RuntimeException(String.format("Parameter '%s' is not an array list: '%s'", paramName, obj.toString()));
        }
        return set;
    }
    
    public Map getConfiguration() {
    	return settingsMap;
    }
    
    public void release() {
    	Configurator._instance = null;
    }
}
