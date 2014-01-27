//TODO
//Should getField() throw exceptions instead of returning null?

package nf.co.korshyadoo.dataIo;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;

import javax.swing.JOptionPane;

import nf.co.korshyadoo.calendar.Fields;
import nu.xom.Attribute;
import nu.xom.Builder;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Elements;
import nu.xom.ParsingException;
import nu.xom.Serializer;


/**
 * A singleton class used for input/output from/to an XML file. 
 * @author korshyadoo
 */
public class XmlFileIo implements DataIo {
	private final String XML_FILE_LOCATION;
	private static XmlFileIo instance; 
	private Document memoryBuffer;
	
	/**
	 * For debugging purposes only
	 */
	public String memoryString;


	/**
	 * Create the XmlFileIo object and load the XML file into memory. 
	 * If the XML file is empty, memoryBuffer is {@code null}.
	 * @throws IOException If an I/O error occurred creating the XML file 
	 */
	private XmlFileIo(String xmlFileLocation) throws IOException {
		XML_FILE_LOCATION = xmlFileLocation;

		if(!fileExists()) {
			createFile();
		}
		loadMemoryBuffer(new File(XML_FILE_LOCATION));
	}

	/**
	 * Retrieves an instance of XmlFileIo. 
	 * @return 
	 * @throws IOException If an I/O error occurred creating the XML file 
	 */
	public static XmlFileIo getInstance() throws IOException {
		return instance;
	}

	/**
	 * Reads the XML file into the memory buffer. {@code memoryBuffer} will be null if the file was empty or doesn't exist
	 * or if there was a parsing exception.
	 * @throws IOException If an I/O error occurred creating the XML file 
	 */
	private void loadMemoryBuffer(File xmlFile) throws IOException {
		nu.xom.Builder builder = new Builder();
		String xmlString = readFile(xmlFile);

		if((xmlString.length() != 0) && (xmlString != null)) {
			try {
				memoryBuffer = builder.build(xmlString, null);
				memoryString = memoryBuffer.toXML();
			} catch (ParsingException e) {
				JOptionPane.showMessageDialog(null, "The XML file is not valid");
				memoryBuffer = null;
			}
		} else {
			memoryBuffer = null;
		}
	}

	/**
	 * Loads the passed StringBuilder into the memory buffer, clearing the previous buffer contents. 
	 * If the StringBuilder is null or has a length of 0, the memory buffer will be set to null
	 * @param XML The contents to be loaded into the memory buffer
	 * @throws IOException thrown by nu.xom.Builder.build(-) if an I/O error such as a bad disk prevents the document's external DTD subset from being read
	 */
	private void loadMemoryBuffer(StringBuilder xml) throws IOException{
		if(xml != null && xml.length() != 0) {
			try {
				Builder builder = new Builder();
				memoryBuffer = builder.build(xml.toString(), null);
				memoryString = memoryBuffer.toXML();
			} catch (ParsingException e) {
				//TODO log parsing exception
				JOptionPane.showMessageDialog(null, "The XML file is not valid");
				memoryBuffer = null;
			}
		} else {
			memoryBuffer = null;
		}
	}

	/**
	 * Reads the contents of a file.
	 * @param file The file to be read. 
	 * @return A String containing the contents of the file. Returns {@code null} if the file doesn't exist.
	 * Returns an empty String if the file exists but is empty.
	 */
	private static String readFile(File file) {
		if(file.exists()) {
			try(BufferedReader br = new BufferedReader(new FileReader(file))) {
				char[] charBuffer = new char[5000];
				int numChar = br.read(charBuffer, 0, 5000);              				//Reads settings.ini into charBuffer and returns the number of characters that were read
				if(numChar > 0) {
					String input = new String(charBuffer, 0, numChar);  				//Creates a string from the number of chars read
					return input;
				}
			} catch (IOException e) {
				JOptionPane.showMessageDialog(null,"There was a problem reading settings.ini. File may be missing or in use");
				System.exit(0);
			} 
			return new String("");
		} else {
			return null;
		}
	}

	private boolean fileExists() {
		File file = new File(XML_FILE_LOCATION);
		return file.exists();
	}

	private void createFile() throws IOException {
		File file = new File(XML_FILE_LOCATION);
		if(!file.exists()) {
			file.createNewFile();
		}
	}

	@Override
	public String getPstLocation() {
		return getNonUserEntry("pstLocation");
	}

	@Override
	public String getField(String userName, String field) {
		//Make sure the XML file is not empty
		if(memoryBuffer == null) {
			return null;
		}

		//Make sure arguments are not null
		if(userName == null || field == null) {
			return null;
		}

		//Retrieve the element for the specified userName
		Element element = getUserElement(userName);

		if(element != null) {
			Element resultElement = element.getFirstChildElement(field);
			if(resultElement != null) {
				return resultElement.getValue();
			} else {
				return null;
			}
		} else {
			//No result found
			return null;
		}
	}

	/**
	 * Determines if the XML file has the .pst location stored. 
	 * @return {@code true} if the XML file has a pstLocation element; otherwise, {@code false}
	 */
	@Override
	public boolean hasPstLocation() {
		if(memoryBuffer == null) {
			return false;
		}

		Element result = memoryBuffer.getRootElement().getFirstChildElement("pstLocation");
		return (result != null);
	}

	@Override
	public boolean hasUser() {
		if(memoryBuffer == null) {
			return false;
		}

		Element result = memoryBuffer.getRootElement().getFirstChildElement("user");
		return (result != null);
	}

	//Get the user element with the specified userName attribute
	//@param userName The value of the userName attribute of the user element to be retrieved
	//@return The user element with the specified userName attribute. {@code null} if the userName was not found.
	private Element getUserElement(String userName) {
		//Return null if the XML file is empty or the passed userName is null
		if(memoryBuffer == null || userName == null) {
			return null;
		}

		//Retrieve all elements under the root
		Elements elements = memoryBuffer.getRootElement().getChildElements();

		//Iterate through the elements to find the specified information
		//////
		for(int x = 0; x < elements.size(); x++) {
			Element element = elements.get(x);

			//If the element is a user and has the specified userName attribute, then return it
			if("user".equals(element.getLocalName()) && userName.equals(element.getAttribute("userName").getValue())) {
				return element;
			}
		}
		//////
		
		return null;
	}

	/**
	 * Get the pstLocation element
	 * @return The pstLocation Element. {@code null} if the element was not found
	 */
	private Element getPstElement() {
		//Return null if the XML file is empty
		if(memoryBuffer == null) {
			return null;
		}

		//Retrieve all elements under the root
		Elements elements = memoryBuffer.getRootElement().getChildElements();

		//Iterate through the elements to find the pstLocation Element
		//////
		for(int x = 0; x < elements.size(); x++) {
			Element element = elements.get(x);

			//If the element is the pstLocation element, return it
			if("pstLocation".equals(element.getLocalName())) {
				return element;
			}
		}
		//////
		
		return null;
	}
	
	/**
	 * Get the specified Element. If more than one Element with the given
	 * name exists, only the first one is returned.
	 * @param elementName The name of the Element to retrieve
	 * @return The specified Element. {@code null} if the element was not found
	 */
	private Element getElement(String elementName) {
		//Return null if the XML file is empty
		if(memoryBuffer == null || elementName == null) {
			return null;
		}

		//Retrieve all elements under the root
		Elements elements = memoryBuffer.getRootElement().getChildElements();

		//Iterate through the elements to find the specified Element
		//////
		for(int x = 0; x < elements.size(); x++) {
			Element element = elements.get(x);

			//If the element is the pstLocation element, return it
			if(elementName.equals(element.getLocalName())) {
				return element;
			}
		}
		//////
		
		return null;
	}

	@Override
	public String getNonUserEntry(String elementName) {
		//Make sure the XML file is not empty
		if(memoryBuffer == null) {
			return null;
		}

		//Retrieve all elements under the root
		Element root = memoryBuffer.getRootElement();

		Element child = root.getFirstChildElement(elementName);
		if(child != null && child.getChildCount() == 1) {
			return child.getChild(0).getValue();
		}
		return null;
	}

	@Override
	public boolean setField(String userName, String field, String value) {
		//Make sure the XML file is not empty
		if(memoryBuffer == null) {
			return false;
		}

		//Retrieve the user element for the given username
		Element user = getUserElement(userName);
		if(user != null) {
			//Retrieve the field for the given user element
			Element fieldElement = user.getFirstChildElement(field);

			//Update the field
			if(fieldElement != null) {
				//Field does exist. Update it
				fieldElement.removeChild(0);
			} else {
				//Field doesn't exist for the user. Create a new one.
				fieldElement = new Element(field);
				user.appendChild(fieldElement);
			}
			fieldElement.appendChild(value);

			//Write memeoryBuffer to the XML file
			memoryString = memoryBuffer.toXML();
			formatMemoryBuffer();
			writeMemoryBufferToFile();
		} else {
			return false;
		}
		return true;
	}

	@Override
	public boolean setPstLocation(String pstLocation) {
		return setNonUserEntry(Fields.PST_LOCATION, pstLocation);
	}

	public boolean createUser(String userName, String[] fields, String[] values) {
		//Make sure there are the same number of fields and values
		if(fields.length != values.length) {
			throw new IllegalArgumentException("Unequal number of fields and values");
		}

		//Make sure the userName is not a duplicate
		Element check = getUserElement(userName);
		if(check != null) {
			//Duplicate user
			return false;
		}

		//Create a new user element and set its userName attribute
		Element element = new Element("user");
		Attribute attribute = new Attribute("userName", userName);
		element.addAttribute(attribute);

		//For each Element specified, add it to the new Element
		for(int z = 0; z < fields.length; z++) {
			//Create new field element
			Element child = new Element(fields[z]);
			child.appendChild(values[z]);

			//Add it to the user element
			element.appendChild(child);
		}

		//If the XML document is empty, point the memory buffer to a new Document
		if(memoryBuffer == null) {
			Element root = new Element("database");
			memoryBuffer = new Document(root);
		}

		//Add the new element to the memoryBuffer
		Element rootElement = memoryBuffer.getRootElement();
		rootElement.appendChild(element);
		formatMemoryBuffer();
		memoryString = memoryBuffer.toXML();

		//Write the memory buffer to XML_FILE_LOCATION
		return writeMemoryBufferToFile();
	}

	/**
	 * Determines if the XML file is empty. 
	 * @return {@code true} if the XML file is empty; otherwise, {@code false}
	 */
	@Override
	public boolean isEmpty() {
		return(memoryBuffer == null);
	}

	/**
	 * Deletes the file located at XML_FILE_LOCATION. 
	 * @return {@code true} if successfully deleted; otherwise, {@code false}
	 */
	@Override
	public boolean deleteAllData() {
		boolean success = false;
		File file = new File(XML_FILE_LOCATION);
		if(file.exists()) {
			success = file.delete();
		}
		return success;
	}

	//DEBUG
	/**
	 * 
	 * @return {@code null} if memory buffer is empty. 
	 */
	public String getMemoryBuffer() {
		if(memoryBuffer == null) {
			return null;
		}
		return memoryBuffer.toXML();
	}

	@Override
	public boolean deleteUser(String userName) {
		if(userName == null) {
			return false;
		}
		
		//Retrieve the user element to be removed. If it doesn't exist, return false
		Element user = getUserElement(userName);
		
		if(user == null) {
			return false;
		}

		user.detach();										//Remove the user element
		formatMemoryBuffer();								//Re-format the memory buffer to a properly formatted XML file
		memoryString = memoryBuffer.toXML();				//Store the String version of the memory buffer to the memoryString
		writeMemoryBufferToFile();							//Write the memory buffer to the XML file
		return true;
	}

	@Override
	public boolean deletePstLocation() {
		//Retrieve the pstLocation element. If it doesn't exist, return true
		Element pstElement = getPstElement();
		if(pstElement == null) {
			return true;
		} else {

		}

		pstElement.detach();								//Remove the pstLocation element
		formatMemoryBuffer();								//Re-format the memory buffer to a properly formatted XML file
		memoryString = memoryBuffer.toXML();				//Store the String version of the memory buffer to the memoryString
		writeMemoryBufferToFile();							//Write the memory buffer to the XML file
		return true;
	}

	public boolean deleteNonUserField(String fieldName) {
		if(fieldName == null) {
			return false;
		}
		
		//Retrieve the element to be removed. If it doesn't exist, return false
		Element field = getElement(fieldName);
		
		if(field == null) {
			return false;
		}

		field.detach();										//Remove the element
		formatMemoryBuffer();								//Re-format the memory buffer to a properly formatted XML file
		memoryString = memoryBuffer.toXML();				//Store the String version of the memory buffer to the memoryString
		writeMemoryBufferToFile();							//Write the memory buffer to the XML file
		return true;
	}

	/**
	 * Updates the memory buffer so that it contains a properly formatted XML file.
	 */
	private void formatMemoryBuffer() {
		StringBuilder output = new StringBuilder();
		OutputStream sos = new StringBuilderOutputStream(output);
		try {
			Serializer s = new Serializer(sos, "ISO-8859-1");
			s.setIndent(4);
			s.write(memoryBuffer);

			//Read the serialized file back into memory
			loadMemoryBuffer(output);
		} catch (UnsupportedEncodingException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		} catch (IOException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
	}
	
	private boolean writeMemoryBufferToFile() {
		boolean success = false;
		do {
			try(PrintWriter outFile = new PrintWriter(new File(XML_FILE_LOCATION))) {
				outFile.print(memoryBuffer.toXML());
				success = true;
			} catch(FileNotFoundException e) {
				//File not found. Create XML file and try writing again
				File f = new File(XML_FILE_LOCATION);
				try {
					f.createNewFile();
				} catch (IOException e1) {
					JOptionPane.showMessageDialog(null,"IOException checking XML file. File may be missing or in use.");
				}
			}
		} while(!success);
		return success;
	}

	@Override
	public boolean hasUser(String userName) {
		return (getUserElement(userName) != null);
	}


	//Returns the user element that is currently set as default. Returns null if no default user is found.
	private Element getDefaultUserElement() {
		if(memoryBuffer != null) {
			Elements elements = memoryBuffer.getRootElement().getChildElements();
			for(int x = 0; x < elements.size(); x++) {													//For each of the root child elements
				if("user".equals(elements.get(x).getLocalName())) {										//If the element is a user element
					Attribute attribute = elements.get(x).getAttribute("default");
					if(attribute != null && "true".equals(attribute.getValue())) {						//If the user element is default
						return elements.get(x);
					}
				}
			}
		}
		return null;
	}

	@Override
	public String getDefaultUserName() {
		if(memoryBuffer != null) {
			Element defaultUserelement = getDefaultUserElement();
			if(defaultUserelement != null) {
				Attribute userNameAttribute = defaultUserelement.getAttribute("userName");
				return userNameAttribute.getValue();
			} else {
				//No user is set as default. Make the first default
				Element firstUser = memoryBuffer.getRootElement().getFirstChildElement("user");
				if(firstUser == null) {
					//There are no users in the data source
					return null;
				} else {
					Attribute firstUserDefaultAttribute = firstUser.getAttribute("default"); 
					if(firstUserDefaultAttribute != null) {
						//The user has a default attribute. Set it to false
						firstUserDefaultAttribute.setValue("true");
					} else {
						//The user does not have a default attribute. Create one
						Attribute defaultAttribute = new Attribute("default", "true");
						firstUser.addAttribute(defaultAttribute);
					}
					
					//Format the memory buffer and write it to the data source
					formatMemoryBuffer();
					memoryString = memoryBuffer.toXML();
					writeMemoryBufferToFile();
					
					//Return the userName of the default user
					Attribute userNameAttribute = firstUser.getAttribute("userName");
					return userNameAttribute.getValue();
				}
			}
		}
		return null;
	}

	@Override
	public int getUserCount() {
		int count = 0;
		Elements elements = memoryBuffer.getRootElement().getChildElements();
		for(int x = 0; x < elements.size(); x++) {
			if("user".equals(elements.get(x).getValue())) {
				count++;
			}
		}
		return count;
	}

	public boolean setNonUserEntry(String entryName, String entryValue) {
		entryValue = entryValue.replaceAll("\\\\", "\\\\\\\\");
		//If the XML file is missing or empty, create a new one with the pstLocation in it
		if(memoryBuffer == null) {
			//XML file is missing or empty. Create a new one with the pstLocation in it
			try {
				createFile();
			} catch (IOException e) {
				JOptionPane.showMessageDialog(null,"IOException creating XML file. File may be missing or in use.");
				e.printStackTrace();
			}

			//Create a new root element
			Element root = new Element("database");
			memoryBuffer = new Document(root);

			//Add the Entry element
			Element elementToUpdate = new Element(entryName);
			elementToUpdate.appendChild(entryValue);
		} else {
			//XML not missing. Update memoryBuffer and write to XML file
			
			Element root = memoryBuffer.getRootElement();
			Element elementToUpdate = root.getFirstChildElement(entryName);
			
			if(elementToUpdate == null) {
				//Element doesn't exist. Create a new one
				elementToUpdate = new Element(entryName);
				root.appendChild(elementToUpdate);
			} else {
				//Remove the old value that is being replaced
				elementToUpdate.removeChild(0);
			}
			
			elementToUpdate.appendChild(entryValue);		//Add the new value to the Element
			
			//Update the memory buffer and write to disk
			formatMemoryBuffer();
			memoryString = memoryBuffer.toXML();
			this.writeMemoryBufferToFile();
		}

		return true;
	}

	@Override
	public boolean setDefaultUser(String username) {
		if(username != null) {

			//If there is already a default user, change it to not default
			Element defaultUserElement = getDefaultUserElement();
			if(defaultUserElement != null) {
				//Set the current default user to not default
				Attribute defaultUserAttribute = defaultUserElement.getAttribute("default");
				defaultUserAttribute.setValue("false");
			}

			//Set the passed user as default
			Element userElement = getUserElement(username);
			Attribute userDefaultAttribute = userElement.getAttribute("default");
			if(userDefaultAttribute != null) {
				userDefaultAttribute.setValue("true");
			} else {
				userDefaultAttribute = new Attribute("default", "true");
				userElement.addAttribute(userDefaultAttribute);
			}

			//Format the memory buffer and write it to data source
			formatMemoryBuffer();
			writeMemoryBufferToFile();
			
			return true;
		} else {
			return false;
		}
	}
	
	private class StringBuilderOutputStream extends OutputStream {
		private StringBuilder output;
		private int mode;					//Append or overwrite mode
		public static final int APPEND_MODE = 1;
		public static final int OVERRITE_MODE = 2;
		
		
		public StringBuilderOutputStream(StringBuilder output) {
			this(output, APPEND_MODE);
		}
		
		public StringBuilderOutputStream(StringBuilder output, int mode) {
			this.output = output;
			this.mode = mode;
		}

		@Override
		public void write(int arg0) {
			switch(mode) {
			case APPEND_MODE:
				output.append((char)arg0);
				break;
			case OVERRITE_MODE:
				output = new StringBuilder((char)arg0);
				break;
			}

		}
		

	}



}
