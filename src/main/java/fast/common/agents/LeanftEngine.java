package fast.common.agents;

import java.awt.image.RenderedImage;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.net.URLClassLoader;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.imageio.ImageIO;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.reflect.MethodUtils;
import com.hp.lft.report.ReportException;
import com.hp.lft.sdk.GeneralLeanFtException;
import com.hp.lft.sdk.ModifiableSDKConfiguration;
import com.hp.lft.sdk.SDK;
import com.hp.lft.sdk.TestObject;
import com.hp.lft.sdk.internal.AppModelBase;

import fast.common.logging.FastLogger;

public class LeanftEngine {

	private static FastLogger _logger = FastLogger.getLogger("LeanftEngine");
	private String _runtimeEnginePath = null;
	private AppModelBase _currentModel = null;
	private Map<String, AppModelBase> _modelMap;
	private Process _runtimeEngineProcess;
	private boolean _isAlive = false;
	private String _currentKey;

	public LeanftEngine(String runtimeEnginePath, String modelJarPath, String modelName) throws Exception {
		_runtimeEnginePath = runtimeEnginePath;
		_modelMap = new HashMap<>();
		launchRuntimeEngine();
		initialSDK();
		createModel(modelJarPath, modelName);
	}

	private Boolean launchRuntimeEngine() throws Exception {
		try {
			_runtimeEngineProcess = Runtime.getRuntime().exec(_runtimeEnginePath);
			_runtimeEngineProcess.waitFor(10, TimeUnit.SECONDS);
			_isAlive = true;
			return true;
		}catch (IOException e) {
			_logger.error("Failed to launch the runtime engine with exception:\n" + e.getMessage());
			return false;
		} 
	}

	private void initialSDK() throws Exception {
		// initialize the SDK and report only once per process
		ModifiableSDKConfiguration modifiableSDKConfiguration = new ModifiableSDKConfiguration();
		SDK.init(modifiableSDKConfiguration);
//		Reporter.init();
//		Reporter.setSnapshotCaptureLevel(CaptureLevel.OnError);
	}

	public void cleanUpSDK() throws ReportException {
		// Generate the report and cleanup the SDK usage.
//		Reporter.generateReport();
		SDK.cleanup();
	}

	public void createModel(String modelJarPath, String model) throws ClassNotFoundException, InstantiationException, IllegalAccessException, IOException {
		String[] models = model.split(";");
		for(String modelName : models){
			if (_modelMap.containsKey(modelName)) {
				_modelMap.get(modelName);
				_logger.info(modelName + " already exists");
			} else {

				String filePath = System.getProperty("user.dir");
				filePath = "file:" + filePath + "\\" + modelJarPath;
				URLClassLoader myClassLoader = null;

				try {
					URL url = new URL(filePath);
					myClassLoader = new URLClassLoader(new URL[] { url }, Thread.currentThread().getContextClassLoader());
					Class<?> modelClass = myClassLoader.loadClass(modelName);
//					_currentModel = (AppModelBase) modelClass.newInstance();
					_modelMap.put(modelName, (AppModelBase) modelClass.newInstance());
					_logger.info("Load additional model from " + modelName);
				} catch (Exception e) {
					_logger.error("Failed to load the model " + modelName + " with exception :\n" + e.getMessage());
				} finally {
					if (myClassLoader != null) {
						myClassLoader.close();
					}
				}
			}
		}
		if(_modelMap.size() == 1){
			Object[] appModelBase = _modelMap.values().toArray();
			_currentModel = (AppModelBase) appModelBase[0];
		}
		else{
			_logger.info("Muldiple leanft models are loaded and please set the current model !");
		}
		
	}

	public void setCurrentModel(String modelName){
		_currentModel = _modelMap.get(modelName);
		_logger.info("The Leanft Runtime Engine is using model " + modelName + " now!");
	}
	public TestObject getTestObject(String key) {
		TestObject keyObj = null;
		int counter = 0;
		if(_currentModel != null){
		 keyObj = _currentModel.getTestObjectMap().get(key);
		 }
		else {
			_logger.warn("The current model is not set! Will search the object with key" + key + " in all models !");
			for(AppModelBase appModelBase : _modelMap.values()){
				TestObject obj = appModelBase.getTestObjectMap().get(key);
				if(obj != null){
					counter ++;
					if(counter > 1){
						_logger.error("Find duplicate object in " + appModelBase.getName() + ", please set the current model !");
						break;
					}
					else{
//						_logger.info("Find the object with key " + key + " in the model " + appModelBase.getName());
						keyObj = obj;
					}
					
				}
			}
		}
		if(keyObj == null){
			_logger.error("The specified object with key" + key + " is not found !");
		}
		return keyObj;
	}

	public Object execute(String name, String action) throws NoSuchMethodException, SecurityException,
			IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		_currentKey = name;
		Object testObject = getTestObject(name);
		Object result = MethodUtils.invokeMethod(testObject, action, new Object[0]);
		
//		_currentObject = getTestObject(name);
//		Object result = MethodUtils.invokeMethod(_currentObject, action, new Object[0]);
		return result;
	}

	public Object execute(String name, String action, Object args) throws NoSuchMethodException, SecurityException,
			IllegalAccessException, IllegalArgumentException, InvocationTargetException, GeneralLeanFtException {
		_currentKey = name;
		Object testObject = getTestObject(name);
		Object result = MethodUtils.invokeMethod(testObject, action, args);
//		_currentObject = getTestObject(name);
//		Object result = MethodUtils.invokeMethod(_currentObject, action, args);
		return result;
	}

	public Object execute(String name, String action, Object[] args, Class<?>[] parameterTypes)
			throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException,
			InvocationTargetException {
		_currentKey = name;
		Object testObject = getTestObject(name);
		Object result = MethodUtils.invokeMethod(testObject, action, args, parameterTypes);
//		_currentObject = getTestObject(name);
//		Object result = MethodUtils.invokeMethod(_currentObject, action, args, parameterTypes);
		return result;
	}

	public void close() throws ReportException {
		_isAlive = false;
		cleanUpSDK();
		_runtimeEngineProcess.destroy();
		_currentModel = null;
//		_currentObject = null;
	}
	public void createScreenShot() throws GeneralLeanFtException{
		
		int index = _currentKey.indexOf('.');

		String windowKey;
		if (index != (-1)) {
			windowKey = _currentKey.substring(0, index);
		} else {
			windowKey = _currentKey;
		}

		windowKey = _currentKey.substring(0,index);

		
		try {
			RenderedImage a = getTestObject(windowKey).getSnapshot();
			Date date = new Date(System.currentTimeMillis());
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
			String fileName = dateFormat.format(date) + ".png";
			File outputfile = new File(fileName);
		    ImageIO.write(a, "png", outputfile);
		    FileUtils.moveFileToDirectory(outputfile, new File(".\\ScreenShots\\"), true);
		} catch (IOException e) {
		    _logger.error("Faild to get the snapshot of the window :\n" + windowKey);
		}
		
	}
}
