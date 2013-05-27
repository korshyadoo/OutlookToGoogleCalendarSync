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

	private static StringBuilder settings;												//Stores the contents of settings.ini

	//Constructor
	/**
	 * If settings.ini exists, it reads it into the settings field. 
	 * If not, it creates it. 
	 * @throws IOException an I/O error occurred accessing settings.ini
	 */
	public SettingsIO() throws IOException {
		settings = new StringBuilder();
		if(!this.checkSettingsState()) {
			readSettings();
		}
	}

	/**
	 * Retrieves a field from the decrypted settings.ini.
	 * readSettings() is called if settings is empty. 
	 * @param settings String containing the decrypted contents of settings.ini
	 * @param field The field to retrieve from the decrypted settings.ini
	 * @return The field located in the decrypted settings.ini. Returns null if the field was not found.
	 */
	public String getSettingsField(String field) {
		String result = null;
		int searchIndex = settings.toString().indexOf(field);
		if(searchIndex != -1) {
			searchIndex += (field.length() + 1);     						//Points searchIndex at the beginning of the field (e.g. username is stored in settings.ini as "username=xxxxx", where"xxxxx" is the username, so searchIndex begins after "=")
			int searchEndIndex = settings.indexOf("\n", searchIndex);		//Point unEndIndex at the "\n" at the end of the field
			result = settings.toString().substring(searchIndex, searchEndIndex);
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
		if(settings.length() > 0) {			//If settings is not empty
			//Search for elementName in settings. If it exists, delete the element
			int startIndex = settings.indexOf(new String(elementName + "="));
			if(startIndex == 0) {				//If startIndex is zero, do a substring starting at the first instance of "\n" to the end of the string
				//Element found at the beginning of settings. Set updatedSettings to the remaining elements
				updatedSettings = new StringBuilder(settings.substring(settings.indexOf("\n") + 1));
			} else if(startIndex > 0) {			//If startIndex is greater than zero, do a substring before and after
				//Element found. Delete it
				settings.delete(startIndex, settings.indexOf("\n", startIndex) + 1);
			}
		}

		//Either way, append the new element to settings. 
		//updatedSettings will be empty unless the first element in settings is being updated
		settings.append(updatedSettings.toString() + elementName + "=" + elementContents + "\n");

		//Encrypt settings in preparation for writing to settings.ini
		BasicTextEncryptor encryptor = new BasicTextEncryptor();
		encryptor.setPassword(ENCRYPTOR_PASS);
		String encryptedBuffer = encryptor.encrypt(settings.toString());

		//Write settings to settings.ini
		boolean success = false;
		do {
			try(PrintWriter outFile = new PrintWriter(new File(SETTINGS_INI_LOCATION))) {
				outFile.print(encryptedBuffer);
				success = true;
			} catch(FileNotFoundException e) {
				//File not found. Create settings.ini and try writing settings again
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
	 * Read encrypted contents of settings.ini and store them in the settings field.
	 * This method is called by the constructor.
	 * @return A String containing all the decrypted contents of settings.ini
	 * @throws FileNotFoundException
	 * @throws IOException 
	 */
	public void readSettings() {
		try(BufferedReader br = new BufferedReader(new FileReader(new File(SETTINGS_INI_LOCATION)))) {
			char[] charBuffer = new char[5000];
			int numChar = br.read(charBuffer, 0, 5000);              				//The number of characters read from settings.ini (encrypted)
			String encryptedSettings = new String(charBuffer, 0, numChar);  		//Creates a string from the number of chars read
			BasicTextEncryptor encryptor = new BasicTextEncryptor();
			encryptor.setPassword(ENCRYPTOR_PASS);
			settings = new StringBuilder(encryptor.decrypt(encryptedSettings));		//Stores decrypted settings.ini contents in the settings field
		} catch (FileNotFoundException e) {
			JOptionPane.showMessageDialog(null,"settings.ini is tested to exist before calling readSettings, so this should never be reached");
			System.exit(0);
		} catch (IOException e) {
			JOptionPane.showMessageDialog(null,"There was a problem reading settings.ini. File may be missing or in use");
			System.exit(0);
		}
	}


	/**
	 * Checks the state of settings.ini and creates it if it doesn't exist.
	 * @return true if settings.ini had to be created or is empty.
	 * false if the settings.ini exists and is not empty.
	 * 
	 * @throws IOException When trying to create or read settings.ini
	 */
	private boolean checkSettingsState() throws IOException {
		File file = new File(SETTINGS_INI_LOCATION);
		
		if(!file.exists()) {			
			//settings.ini doesn't exist; create it
			file.createNewFile();
			return true;
		} else {
			//settings.ini does exist. Determine if it's empty
			FileReader fr = new FileReader(new File(SETTINGS_INI_LOCATION));
			if(fr.read() == -1) {
				//settings.ini empty
				fr.close();
				return true;
			} else {
				//settings.ini not empty
				fr.close();
				return false;
			}
		}
	}
	
	/**
	 * Checks if there is anything in the settings field
	 * @return true if settings is empty or null; otherwise, false.
	 */
	public boolean isEmpty() {
		return(settings.length() == 0 || settings == null);
	}

	public void createNewFile() {

	}
}
