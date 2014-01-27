package nf.co.korshyadoo.dataIo;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.Properties;

import org.apache.log4j.Logger;


public class PropertiesFileIo implements DataIo {
	private final String PROPERTIES_FILE_LOCATION;
	private static PropertiesFileIo instance; 
	private Properties properties;
	private final Logger logger = Logger.getLogger("nf.co.korshyadoo.dataIo.PropertiesFileIo");
	
	/**
	 * Create the PropertiesFileIo object and load the properties file into memory. 
	 * If the properties file is empty, memoryBuffer is {@code null}.
	 * This constructor is accessed from the beans file used by Spring.
	 * @throws IOException If an I/O error occurred creating the properties file 
	 */
	private PropertiesFileIo(String propertiesFileLocation, Map<String, String> defaults) throws IOException {
		PROPERTIES_FILE_LOCATION = propertiesFileLocation;
		try {
			Properties defaultProperties;
			if(defaults.size() > 0) {
				defaultProperties = new Properties();
				for(String key : defaults.keySet()) {
					defaultProperties.setProperty(key, defaults.get(key));
				}
			} else {
				defaultProperties = null;
			}
			
			properties = new Properties(defaultProperties);
			properties.load(new FileInputStream(PROPERTIES_FILE_LOCATION));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			logger.error("FileNotFound from \"properties.load(new FileInputStream(PROPERTIES_FILE_LOCATION));\". Creating new empty file.");
			createFile();
		} catch (IOException e2) {
			e2.printStackTrace();
			logger.error("IOE loading properties");
		}
	}
	
	private PropertiesFileIo(String propertiesFileLocation) throws IOException {
		PROPERTIES_FILE_LOCATION = propertiesFileLocation;
		properties = new Properties();
		try {
			properties.load(new FileInputStream(PROPERTIES_FILE_LOCATION));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			logger.error("FileNotFound from \"properties.load(new FileInputStream(PROPERTIES_FILE_LOCATION));\". Creating new empty file.");
			createFile();
		} catch (IOException e2) {
			e2.printStackTrace();
			logger.error("IOE loading properties");
		}
	}

	/**
	 * Retrieves an instance of PropertiesFileIo. 
	 * @return 
	 * @throws IOException If an I/O error occurred creating the properties file 
	 */
	public static PropertiesFileIo getInstance() throws IOException {
		return instance;
	}
	
	private void createFile() throws IOException {
		File file = new File(PROPERTIES_FILE_LOCATION);
		if(!file.exists()) {
			file.createNewFile();
		}
	}	
	
	private boolean storeProperties() {
		try {
			properties.store(new FileOutputStream(PROPERTIES_FILE_LOCATION), "");
		} catch (FileNotFoundException e) {
			logger.error("File not found: PROPERTIES_FILE_LOCATION");
			e.printStackTrace();
			return false;
		} catch (IOException e) {
			logger.error("IOE writing to PROPERTIES_FILE_LOCATION");
			e.printStackTrace();
			return false;
		}
		return true;
	}

	@Override
	public String getPstLocation() {
		return properties.getProperty("pstLocation");
	}

	@Override
	public boolean setPstLocation(String pstLocation) {
		properties.setProperty("pstLocation", pstLocation);
		return storeProperties();
	}

	@Override
	public boolean deletePstLocation() {
		properties.remove("pstLocation");
		return storeProperties();
	}

	@Override
	public boolean hasPstLocation() {
		return properties.get("pstLocation") != null;
	}

	@Override
	public String getField(String userName, String field) {
		return null;
	}

	@Override
	public boolean setField(String userName, String field, String value) {
		return false;
	}

	@Override
	public String getNonUserEntry(String entryName) {
		return properties.getProperty(entryName);
	}

	@Override
	public boolean setNonUserEntry(String entryName, String entryValue) {
		properties.setProperty(entryName, entryValue);
		return storeProperties();
	}

	@Override
	public boolean deleteNonUserField(String fieldName) {
		properties.remove(fieldName);
		return storeProperties();
	}

	@Override
	public String getDefaultUserName() {
		return null;
	}

	@Override
	public boolean setDefaultUser(String username) {
		return false;
	}

	@Override
	public boolean createUser(String userName, String[] fields, String[] values) {
		return false;
	}

	@Override
	public boolean deleteUser(String userName) {
		return false;
	}

	@Override
	public boolean hasUser(String userName) {
		return false;
	}

	@Override
	public boolean hasUser() {
		return false;
	}

	@Override
	public int getUserCount() {
		return 0;
	}

	@Override
	public boolean isEmpty() throws Exception {
		return properties.isEmpty();
	}

	@Override
	public boolean deleteAllData() {
		properties.clear();
		return storeProperties();
	}

}
