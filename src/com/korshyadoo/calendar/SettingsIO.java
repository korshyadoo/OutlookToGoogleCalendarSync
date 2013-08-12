package com.korshyadoo.calendar;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;

import javax.swing.JOptionPane;

import org.jasypt.util.text.BasicTextEncryptor;

/**
 * Retrieves information from settings.ini
 * @author korshyadoo
 *
 */
public class SettingsIO {
	public static final String SETTINGS_INI_LOCATION = "settings.ini";			//Location of settings.ini
	public static final String ENCRYPTOR_PASS = "pass";							//The password for the encryptor
	public static final String PST_LOCATION = "pstLocation";					//The settings.ini field for the .pst location
	public static final String USERNAME = "username";							//The settings.ini field for the username
	public static final String PASSWORD = "password";							//The settings.ini field for the password

	private StringBuilder settingsBuffer;										//Stores the contents of settings.ini
	
	private static SettingsIO sio;

	//Constructor
	/**
	 * If settings.ini exists and is not empty, it reads it into the settingsBuffer field. 
	 * If not, it creates it. 
	 * @throws IOException an I/O error occurred accessing settings.ini
	 */
	private SettingsIO() throws IOException {
		//If settings.ini doesn't exist, create it
		if(!settingsINIExists()) {
			createFile();
		}
		
		//settings.ini now exists. Read it into memory and store it in settingsBuffer (works even if settings.ini is empty)
		settingsBuffer = readSettings();
	}
	
	public static SettingsIO getInstance() throws IOException {
		if(sio == null) {
			sio = new SettingsIO();
		}
		return sio;
	}

	/**
	 * Retrieves a field from the settingsBuffer. 
	 * @param settingsBuffer String containing the decrypted contents of settings.ini
	 * @param field The field to retrieve from the decrypted settings.ini. Can be one of the following:
	 * SettingsIO.PST_LOCATION
	 * SettingsIO.USERNAME
	 * SettingsIO.PASSWORD
	 * @return The field located in the decrypted settings.ini. Returns null if the field was not found.
	 */
	public String getSettingsField(String field) {
		String result = null;
		int searchIndex = settingsBuffer.toString().indexOf(field);
		if(searchIndex != -1) {
			searchIndex += (field.length() + 1);     									//Points searchIndex at the beginning of the field (e.g. username is stored in settings.ini as "username=xxxxx", where"xxxxx" is the username, so searchIndex begins after "=")
			int searchEndIndex = settingsBuffer.indexOf("\n", searchIndex);				//Point unEndIndex at the "\n" at the end of the field
			result = settingsBuffer.toString().substring(searchIndex, searchEndIndex);
		} 
		return result;
	}

	/**
	 * Adds or updates a field in settings.ini
	 * @param elementName Name of the element to add or update (i.e. "username", "password", or "pstlocation")
	 * @param elementContents The contents of the element being added or updated
	 */
	public void setSettingsField(String elementName, String elementContents) {
		StringBuilder updatedSettings = new StringBuilder();
		if(settingsBuffer.length() > 0) {													//If settingsBuffer is not empty
			//Search for elementName in settingsBuffer. If it exists, delete the element
			int startIndex = settingsBuffer.indexOf(new String(elementName + "="));
			if(startIndex == 0) {															//If startIndex is zero, do a substring starting at the first instance of "\n" to the end of the string
				//Element found at the beginning of settingsBuffer. Set updatedSettings to the remaining elements
				updatedSettings = new StringBuilder(settingsBuffer.substring(settingsBuffer.indexOf("\n") + 1));
			} else if(startIndex > 0) {														//If startIndex is greater than zero, do a substring before and after
				//Element found. Delete it
				settingsBuffer.delete(startIndex, settingsBuffer.indexOf("\n", startIndex) + 1);
			}
		}

		//Either way, append the new element to settingsBuffer. 
		//updatedSettings will be empty unless the first element in settingsBuffer is being updated
		settingsBuffer.append(updatedSettings.toString() + elementName + "=" + elementContents + "\n");

		//Encrypt settingsBuffer in preparation for writing to settings.ini
		BasicTextEncryptor encryptor = new BasicTextEncryptor();
		encryptor.setPassword(ENCRYPTOR_PASS);
		String encryptedBuffer = encryptor.encrypt(settingsBuffer.toString());

		//Write encryptedBuffer to settings.ini
		boolean success = false;
		do {
			try(PrintWriter outFile = new PrintWriter(new File(SETTINGS_INI_LOCATION))) {
				outFile.print(encryptedBuffer);
				success = true;
			} catch(FileNotFoundException e) {
				//File not found. Create settings.ini and try writing again
				File f = new File(SETTINGS_INI_LOCATION);
				try {
					f.createNewFile();
				} catch (IOException e1) {
					JOptionPane.showMessageDialog(null,"IOException checking settings.ini. File may be missing or in use.");
				}
			}
		} while(!success);
	}

	/**
	 * Read encrypted contents of settings.ini, decrypt them, and store them in the settingsBuffer field.
	 * This method is called by the constructor.
	 * @return A String containing all the decrypted contents of settings.ini
	 * @throws FileNotFoundException
	 * @throws IOException 
	 */
	
	
	/**
	 * Read encrypted contents of settings.ini and decrypt them. 
	 * This method is called by the constructor.
	 * @return A StringBuilder containing all the decrypted contents of settings.ini.
	 * Returns null if settings.ini doesn't exist
	 */
	private StringBuilder readSettings() {
		if(settingsINIExists()) {
			try(BufferedReader br = new BufferedReader(new FileReader(new File(SETTINGS_INI_LOCATION)))) {
				char[] charBuffer = new char[5000];
				int numChar = br.read(charBuffer, 0, 5000);              						//Reads settings.ini into charBuffer and returns the number of characters that were read
				if(numChar > 0) {
					String encryptedSettings = new String(charBuffer, 0, numChar);  			//Creates a string from the number of chars read
					BasicTextEncryptor encryptor = new BasicTextEncryptor();
					encryptor.setPassword(ENCRYPTOR_PASS);
					return new StringBuilder(encryptor.decrypt(encryptedSettings));				//Return the decrypted contents of the settings.ini file
				}
			} catch (IOException e) {
				JOptionPane.showMessageDialog(null,"There was a problem reading settings.ini. File may be missing or in use");
				System.exit(0);
			} 
			return new StringBuilder();
		} else {
			return null;
		}
	}

	/**
	 * If settings.ini doesn't exist, it is created
	 * @throws IOException The file may be inaccessible or in use
	 */
	private void createFile() throws IOException {
		File file = new File(SETTINGS_INI_LOCATION);
		if(!file.exists()) {
			file.createNewFile();
		}
	}
	
	/**
	 * Checks if settings.ini exists
	 * @return
	 */
	private boolean settingsINIExists() {
		File file = new File(SETTINGS_INI_LOCATION);
		return file.exists();
	}
	
	/**
	 * Checks if there is anything in the settingsBuffer field
	 * @return true if settingsBuffer is empty or null; otherwise, false.
	 */
	public boolean isEmptyBuffer() {
		return(settingsBuffer.length() == 0 || settingsBuffer == null);
	}
	
	public boolean deleteSettingsINI() {
		boolean success = false;
		File file = new File(SETTINGS_INI_LOCATION);
		if(file.exists()) {
			success = file.delete();
		}
		return success;
	}
}
