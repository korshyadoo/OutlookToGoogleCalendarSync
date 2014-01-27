package nf.co.korshyadoo.dataIo;



public interface DataIo {
	
	/**
	 * Retrieves the value of the pstLocation element.
	 * @return Returns the location of the .pst file in a String. Returns {@code null} if 
	 * the data source is empty or not found, or if the pstLocation was not found
	 * in the data source. 
	 */
	public String getPstLocation();
	
	/**
	 * Sets the location of the .pst file in the data source.
	 * @param pstLocation The new location of the .pst file.
	 * @return {@code true} if successful; otherwise, {@code false}
	 */
	public boolean setPstLocation(String pstLocation);
	
	/**
	 * Deletes the stored pstLocation from the data source. 
	 * @return {@code true} if the pstLocation was successfully deleted or it did not exist; 
	 * otherwise, {@code false}
	 */
	public boolean deletePstLocation();
	
	public boolean hasPstLocation();
	
	/**
	 * Retrieves the data contained in the specified field for the specified user.
	 * @param userName The username of the user element to get the field from
	 * @param field The name of the field to be retrieved. 
	 * The Fields class contains constants representing the field names.
	 * @return A String containing the value of the specified field. 
	 * Returns {@code null} if the field was not found for the specified user.
	 */
	public String getField(String userName, String field);
	
	/**
	 * Set the field for the given userName. If the user doesn't have
	 * the field, it is created.
	 * e.g. To set the calendarID to "myCal" for user "jason@gmail.com": 
	 * {@code setField("jason@gmail.com", "calendarID", "myCal");}
	 * @return {@code false} if the user element could not be located; otherwise, {@code true}
	 */
	public boolean setField(String userName, String field, String value);
	
	/**
	 * Retrieves an entry in the data source that is not associated with a user.
	 * @param entryName The name of the entry to be retrieved.
	 * @return The data in the entry. {@code null} if the data was not found.
	 */
	public String getNonUserEntry(String entryName);
	
	/**
	 * Sets the value of an entry in the data source that is not
	 * associated with a user.
	 * @param entryName The name of the entry to be updated.
	 * @param entryValue The data to set the entry to.
	 * @return {@code true} if the update was successful; otherwise, {@code false}
	 */
	public boolean setNonUserEntry(String entryName, String entryValue);

	/**
	 * Deletes a field of data that is not associated with a user
	 * @param fieldName The name of the field to be deleted
	 * @return {@code true} if the field was successfully deleted or it doesn't exist;
	 * otherwise, {@code false}
	 */
	public boolean deleteNonUserField(String fieldName);

	/**
	 * Gets the username of the default user. If no user is set as the default, the first user
	 * in the data source is set as default.
	 * @return A String containing the username of the default user.Returns {@code null} if 
	 * the data source is empty or not found, or if no default user is found.
	 */
	public String getDefaultUserName();

	/**
	 * Sets the user as the default user
	 * @param userName The user to be set as default
	 */
	public boolean setDefaultUser(String userName);
	
	/**
	 * Add a new user element to the XML file and update the memory buffer.
	 * @param userName The new username to be added
	 * @param fields An array of Strings containing the names of the fields to be added
	 * @param values An array of Strings containing the values to be put in the fields
	 * @return {@code true} if created successfully; otherwise, {@code false}
	 * @throws IllegalArgumentException if the number of fields and values are not equal
	 */
	public boolean createUser(String userName, String[] fields, String[] values) throws IllegalArgumentException;

	public boolean deleteUser(String userName);
		
	/**
	 * Determines if the passed username exists in the data source.
	 * @param userName The username to search for.
	 * @return {@code true} if the user exists. {@code false} if the data source is empty, 
	 * the passed String is {@code null}, or the username was not found. 
	 */
	public boolean hasUser(String userName);
	
	/**
	 * Determines if the XML file has at least one user element. 
	 * @return {@code true} if there is at least one user element in the XML file; otherwise, {@code false}
	 */
	public boolean hasUser();

	/**
	 * Returns the number of user's stored in the data source.
	 * @return The number of user's stored in the data source.
	 */
	public int getUserCount();
	
	//When the object is created, it reads the file into memory. 
	//If the file is missing, it is created (and therefore empty).
	//If the file is missing or empty, the buffer will be empty.
	//This method checks if the buffer is empty.
	public boolean isEmpty() throws Exception;

	/**
	 * Deletes all stored data
	 * @return {@code true} if deleted successfully; otherwise, {@code false}
	 */
	public boolean deleteAllData() throws Exception;
}
