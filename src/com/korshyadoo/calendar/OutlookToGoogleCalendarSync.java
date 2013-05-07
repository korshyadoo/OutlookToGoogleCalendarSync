//   Copyright (c) 2013, Jason Matthews
//
//   Licensed under the Apache License, Version 2.0 (the "License");
//   you may not use this file except in compliance with the License.
//   You may obtain a copy of the License at
//
//       http://www.apache.org/licenses/LICENSE-2.0
//
//   Unless required by applicable law or agreed to in writing, software
//   distributed under the License is distributed on an "AS IS" BASIS,
//   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//   See the License for the specific language governing permissions and
//   limitations under the License.

package com.korshyadoo.calendar;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.TimeZone;

import javax.swing.JOptionPane;
import javax.swing.LookAndFeel;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import org.jasypt.util.text.BasicTextEncryptor;

import com.google.gdata.client.calendar.CalendarQuery;
import com.google.gdata.client.calendar.CalendarService;
import com.google.gdata.data.DateTime;
import com.google.gdata.data.Link;
import com.google.gdata.data.PlainTextConstruct;
import com.google.gdata.data.batch.BatchOperationType;
import com.google.gdata.data.batch.BatchStatus;
import com.google.gdata.data.batch.BatchUtils;
import com.google.gdata.data.calendar.CalendarEventEntry;
import com.google.gdata.data.calendar.CalendarEventFeed;
import com.google.gdata.data.extensions.When;
import com.google.gdata.data.extensions.Where;
import com.google.gdata.util.AuthenticationException;
import com.google.gdata.util.ServiceException;
import com.pff.PSTAppointment;
import com.pff.PSTException;
import com.pff.PSTFile;
import com.pff.PSTFolder;

/**
 * Contains methods for interfacing with Google API, including authenitcation,
 * running queries, and running batch requests. 
 * Handles conversion of a PST object to a CEE object as well as 
 * comparison between PST and CEE objects.
 *
 */
public class OutlookToGoogleCalendarSync {

	// The base URL for a user's calendar metafeed (needs a username appended).
	private static final String METAFEED_URL_BASE = 
			"https://www.google.com/calendar/feeds/";

	// The string to add to the user's metafeedUrl to access the event feed for
	// their primary calendar.
	private static final String EVENT_FEED_URL_SUFFIX = "/private/full";

	// The URL for the metafeed of the specified user.
	// (e.g. http://www.google.com/feeds/calendar/jdoe@gmail.com)
	private static URL metafeedUrl = null;

	// The URL for the event feed of the specified user's primary calendar.
	// (e.g. http://www.googe.com/feeds/calendar/jdoe@gmail.com/private/full)
	private static URL eventFeedUrl = null;

	//The default location for the .pst file in Windows XP
	public static final String PST_LOCATION_DEFAULT_XP = "C:\\Documents and Settings\\" + 
			System.getProperty("user.name") +
			"\\Local Settings\\Application Data\\Microsoft\\Outlook\\Outlook.pst";

	//The default location for the .pst file in Windows Vista / 7
	public static final String PST_LOCATION_DEFAULT_VISTA = "C:\\Users\\" +
			System.getProperty("user.name") +
			"\\AppData\\Local\\Microsoft\\Outlook\\Outlook.pst";

	//An alternate location for the .pst file in Windows Vista / 7
	public static final String PST_LOCATION_DEFAULT_VISTA2 = "C:\\Users\\" +
			System.getProperty("user.name") +
			"\\Documents\\Outlook Files\\Outlook.pst";


	//Location of settings.ini
	//public static final String SETTINGS_INI_LOCATION = "C:\\bakup\\Java\\Net Beans\\MyCalendarSample\\src\\settings.ini";
	public static final String SETTINGS_INI_LOCATION = "settings.ini";

	//Location of log.txt
	public static final String LOG_TXT = "log.txt";

	//Separator for log file
	public static final String SEPARATOR = "-------------------------------------------";

	//The password for the encryptor
	public static final String ENCRYPTOR_PASS = "pass";

	private static String username;
	private static String password;
	private static CalendarService myService;
	private static StringBuilder settings = new StringBuilder();
	private static Date startDate;
	private static Date endDate;
	private static List<Path> pstResults;
	protected static String pstLocation;			//Location of the PST file used by Outlook

	/**
	 * Outlook calendar file is at C:\Users\Lappy2\Documents\Outlook Files\Outlook.pst
	 * 
	 * settings.ini:
	 * First line contains a long representing the Date of the last time the OutlookPSTTest was run
	 * 
	 * @param args Must be length 2 and contain a valid username/password
	 */
	public static void main(String[] args) {
		myService = new CalendarService("korshyadoo-MyOutlookSync-0.1");

		//Set the start and end times used for syncing
		Calendar startCal = new GregorianCalendar();
		startCal.add(Calendar.MONTH, -5);                                   
		startDate = startCal.getTime();
		Calendar endCal = new GregorianCalendar();
		endCal.add(Calendar.YEAR, 100);                                   
		endDate = endCal.getTime();      

		//Set look and feel
		try {
			UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
		} catch (Throwable e) {
			//LookAndFeel not found on user's system
			try {
				LookAndFeel laf = null;
				UIManager.setLookAndFeel(laf);
			} catch (UnsupportedLookAndFeelException e1) {}
		}

		//If this is the first time running, i.e. settings.ini is missing or empty,
		//locate the .pst file and run FirstRunFrame, otherwise run MainFrame. 
		//If authentication exception is thrown running MainFrame, run FirstRunFrame
		try {
			if(firstRun()) {			//First time running the application or missing settings.ini
				//Locate the .pst file
				pstLocation = locatePST();
				if(pstLocation != null) {
					//A .pst file was found
					//Write pstLocation to settings.ini for future executions
					setSettings("pstLocation", pstLocation);

					//Run FirstRunFrame
					java.awt.EventQueue.invokeLater(new Runnable() {
						@Override
						public void run() {
							new FirstRunFrame().setVisible(true);
						}
					});
				} else {
					//No .pst file was found in the default locations
					//Run PSTSearchFrame
					java.awt.EventQueue.invokeLater(new PSTSearchFrameRunnable());
				}
			} else {		//Not the first run
				//Read credentials from settings.ini, set username and password static fields,
				// set the credentials for myService, and create URL Objects
				settings = new StringBuilder(readSettings());
				username = getSettingsField("username");
				password = getSettingsField("password");
				pstLocation = getSettingsField("pstLocation");
				myService.setUserCredentials(username, password);			//Authenticate on Google server
				createURLObjects();											//Form the URLs needed to use Google feeds

				//Run MainFrame
				java.awt.EventQueue.invokeLater(new Runnable() {
					@Override
					public void run() {
						new MainFrame().setVisible(true);
					}
				});            
			}
		} catch(MalformedURLException e) {
			// Bad URL
			JOptionPane.showMessageDialog(null,"Uh oh - you've got an invalid URL");
			System.exit(0);
		} catch(AuthenticationException e) {
			//Run FirstRunFrame
			java.awt.EventQueue.invokeLater(new Runnable() {
				@Override
				public void run() {
					new FirstRunFrame().setVisible(true);
				}
			});          
		} catch (IOException e) {
			JOptionPane.showMessageDialog(null,"IOException checking settings.ini. File may be missing or in use.");
			System.exit(0);
		}
	}

	/**
	 * Retrieves all events from the Google calendar and adds them to an ArrayList<CalendarEventEntry>
	 * @param service
	 * @return All events from Google Calendar in an ArrayList<CalendarEventEntry>
	 * @throws ServiceException
	 * @throws IOException 
	 */
	public static ArrayList<CalendarEventEntry> getAllEvents(CalendarService service)
			throws ServiceException, IOException {
		// Send the request and receive the response:
		CalendarEventFeed resultFeed = service.getFeed(eventFeedUrl,
				CalendarEventFeed.class);
		ArrayList<CalendarEventEntry> allEvents = new ArrayList<>();
		int page = 1;
		do {
			System.out.println("Page " + page);
			for (int i = 0; i < resultFeed.getEntries().size(); i++) {          //For each event in the result feed
				allEvents.add(resultFeed.getEntries().get(i));                  //Add to allEvents
				System.out.println("Added " + resultFeed.getEntries().get(i).getTitle().getPlainText());
			}
			if(resultFeed.getNextLink() != null) {                              //If there's another page of results in the feed, get it
				resultFeed = service.getFeed(new URL(resultFeed.getNextLink().getHref()), CalendarEventFeed.class);
			} else {
				resultFeed = null;
			}
			System.out.println();
			page++;
		} while(resultFeed != null);
		return allEvents;
	}   

	/**
	 * Converts a PSTAppointmnet to a CalendarEventEntry.
	 * Converts the start time, end time, title, and locataion
	 * Needs recurrence added
	 * @param app The PSTAppointment object to be converted to a CalendarEventEntry
	 * @return 
	 */
	public static ConvertReturn convertPSTToCEE(PSTAppointment app) {
		//Check for weird daylight saving bias and load the start and end times into the When object 
		DateTime startTime =  null;
		DateTime endTime = null;
		boolean nullTZ = false;
		if(app.getStartTimeZone() != null) {
			if(!TimeZone.getDefault().inDaylightTime(app.getCreationTime()) &&
					TimeZone.getDefault().inDaylightTime(app.getStartTime()) &&
					app.getStartTimeZone().getDaylightBias() == 0) {			//If app was created outside DST and start time is during DST and the daylight bias is 0
				//remove 60 minutes from start time
				Calendar startChange = new GregorianCalendar();
				startChange.setTime(app.getStartTime());
				startChange.add(Calendar.MINUTE, -60);
				startTime = new DateTime(startChange.getTime(), TimeZone.getDefault());

				//Remove 60 minutes from the end time
				Calendar endChange = new GregorianCalendar();
				endChange.setTime(app.getEndTime());
				endChange.add(Calendar.MINUTE, -60);
				endTime = new DateTime(endChange.getTime(), TimeZone.getDefault());
			} else if(TimeZone.getDefault().inDaylightTime(app.getCreationTime()) &&
					!TimeZone.getDefault().inDaylightTime(app.getStartTime()) &&
					app.getStartTimeZone().getDaylightBias() == -60) {
				//Add 60 minutes to start time
				Calendar startChange = new GregorianCalendar();
				startChange.setTime(app.getStartTime());
				startChange.add(Calendar.MINUTE, 60);
				startTime = new DateTime(startChange.getTime(), TimeZone.getDefault());

				//Add 60 minutes to end time
				Calendar endChange = new GregorianCalendar();
				endChange.setTime(app.getEndTime());
				endChange.add(Calendar.MINUTE, 60);
				endTime = new DateTime(endChange.getTime(), TimeZone.getDefault());
			} else {														
				//No need to adjust for daylight savings
				startTime = new DateTime(app.getStartTime(), TimeZone.getDefault());
				endTime = new DateTime(app.getEndTime(), TimeZone.getDefault());
			}
		} else {
			//The time zone on the PSTAppointment is null so just use the default time zone
			startTime = new DateTime(app.getStartTime(), TimeZone.getDefault());
			endTime = new DateTime(app.getEndTime(), TimeZone.getDefault());

			//Set nullTZ to true for the return value (indicates to MainFrame that a log entry needs to be written)
			nullTZ = true;		
		}

		Date startDate = new Date(app.getStartTime().getTime());
		Calendar startCal = new GregorianCalendar();
		startCal.setTime(startDate);
		Date endDate = new Date(app.getEndTime().getTime());
		Calendar endCal = new GregorianCalendar();
		endCal.setTime(endDate);
		if(!nullTZ) {
			startTime.setTzShift(app.getStartTimeZone().getBias() - (startCal.get(Calendar.DST_OFFSET) / (60 * 1000)));
			endTime.setTzShift(app.getEndTimeZone().getBias() - (endCal.get(Calendar.DST_OFFSET) / (60 * 1000)));
		} else {
			startTime.setTzShift(startCal.get(Calendar.DST_OFFSET) / (60 * 1000));
			endTime.setTzShift(endCal.get(Calendar.DST_OFFSET) / (60 * 1000));
		}
		When when = new When();
		when.setStartTime(startTime);
		when.setEndTime(endTime);

		//Create Where object and set the location
		Where location = new Where();
		location.setValueString(app.getLocation());

		//Create new CalendarEventEntry and set the times, title, location, and content
		CalendarEventEntry entry = new CalendarEventEntry();
		entry.addTime(when);
		entry.setTitle(new PlainTextConstruct(app.getSubject()));
		entry.addLocation(location);
		entry.setContent(new PlainTextConstruct(app.getBody()));

		return new ConvertReturn(entry, nullTZ);
	}

	/**
	 * Makes a batch request to delete all the events in the given list. If any of
	 * the operations fails, the errors returned from the server are displayed.
	 * The CalendarEntry objects in the list given as a parameters must be entries
	 * returned from the server that contain valid edit links (for optimistic
	 * concurrency to work). Note: You can add entries to a batch request for the
	 * other operation types (INSERT, QUERY, and UPDATE) in the same manner as
	 * shown below for DELETE operations.
	 * 
	 * @param service An authenticated CalendarService object.
	 * @param eventsToDelete A list of CalendarEventEntry objects to delete.
	 * @throws ServiceException If the service is unable to handle the request.
	 * @throws IOException Error communicating with the server.
	 */
	public static void deleteEvents(List<CalendarEventEntry> eventsToDelete) throws ServiceException,
	IOException {

		// Add each item in eventsToDelete to the batch request.
		CalendarEventFeed batchRequest = new CalendarEventFeed();
		for (int i = 0; i < eventsToDelete.size(); i++) {
			CalendarEventEntry toDelete = eventsToDelete.get(i);
			// Modify the entry toDelete with batch ID and operation type.
			BatchUtils.setBatchId(toDelete, String.valueOf(i));
			BatchUtils.setBatchOperationType(toDelete, BatchOperationType.DELETE);
			batchRequest.getEntries().add(toDelete);
		}

		// Get the URL to make batch requests to
		CalendarEventFeed feed = myService.getFeed(eventFeedUrl,
				CalendarEventFeed.class);
		Link batchLink = feed.getLink(Link.Rel.FEED_BATCH, Link.Type.ATOM);
		URL batchUrl = new URL(batchLink.getHref());

		// Submit the batch request
		CalendarEventFeed batchResponse = myService.batch(batchUrl, batchRequest);

		// Ensure that all the operations were successful.
		boolean isSuccess = true;
		for (CalendarEventEntry entry : batchResponse.getEntries()) {
			String batchId = BatchUtils.getBatchId(entry);
			if (!BatchUtils.isSuccess(entry)) {
				isSuccess = false;
				BatchStatus status = BatchUtils.getBatchStatus(entry);
				System.out.println("\n" + batchId + " failed (" + status.getReason()
						+ ") " + status.getContent());
			}
			//      } else {
			//          System.out.print("Deleted: " + entry.getTitle().getPlainText());
			//      }
		}
		if (isSuccess) {
			System.out.println("Successfully deleted all events via batch request.");
		}
	}
	
	/**
	 * Retrieves a List<CalendarEventEntry> containing all appointments with a start time between startDate and endDate
	 * @param startDate The beginning Date for the query
	 * @param endDate The ending Date for the query
	 * @return A List<CalendarEventEntry> containing all appointments with a start time between startDate and endDate
	 * @throws ServiceException Query request failed or system error retrieving feed (thrown by CalendarService.query() and
	 * CalendarService.getFeed())
	 * @throws IOException Error communicating with the GData service (thrown by CalendarService.query() and
	 * CalendarService.getFeed())
	 */
	public static List<CalendarEventEntry> timeQuery(Date startDate, Date endDate) throws ServiceException, IOException {
		List<CalendarEventEntry> output = new ArrayList<>();
		
		//Set up the resultfeed
		CalendarQuery myQuery = new CalendarQuery(eventFeedUrl);
		DateTime start = new DateTime(startDate, TimeZone.getDefault());
		myQuery.setMinimumStartTime(start);
		DateTime end = new DateTime(endDate, TimeZone.getDefault());
		myQuery.setMaximumStartTime(end);
		CalendarEventFeed resultFeed = myService.query(myQuery, CalendarEventFeed.class);

		//Retrieve the results
		int page = 1;
		do {
			for (int i = 0; i < resultFeed.getEntries().size(); i++) {          					//For each entry in the resultFeed
				CalendarEventEntry entry = resultFeed.getEntries().get(i);							//Retrieve the entry
				
				//Google's criteria for an appointment being within the time range for the query includes
				//any appointment which extends into the time range (not necessarily one that starts within the time range).
				//e.g. If an appointment starts at 1:00pm and ends at 2:00pm, and the query start time is at 1:30pm, 
				//the appointment is included in the results of the query.
				//The following code eliminates entries which have a start time before the desired time range
				Date entryDate = new Date(entry.getTimes().get(0).getStartTime().getValue());		//Get the start time of the entry
				int test = entryDate.compareTo(startDate);
				switch(test) {
				case 0:
				case 1:
					output.add(entry);
					break;
				}
				
			}            
			if(resultFeed.getNextLink() != null) {                              //If there's another page of results in the feed, get it
				resultFeed = myService.getFeed(new URL(resultFeed.getNextLink().getHref()), CalendarEventFeed.class);
			} else {
				resultFeed = null;
			}
			page++;
		} while(resultFeed != null);

		return output;
	}

	/**
	 * Find all events in gmail calendar starting 6 months prior to run time and ending 100 years past run time.
	 * @return ArrayList containing all event results. size == 0 if no events found.
	 * @throws ServiceException
	 * @throws IOException 
	 */
	public static ArrayList<CalendarEventEntry> timeRangeQuery()
			throws ServiceException, IOException {
		DateTime startDateTime = new DateTime(startDate, TimeZone.getDefault());
		DateTime endDateTime = new DateTime(endDate, TimeZone.getDefault());

		//Prepare the CalendarQuery
		CalendarQuery myQuery = new CalendarQuery(eventFeedUrl);
		myQuery.setMinimumStartTime(startDateTime);
		myQuery.setMaximumStartTime(endDateTime);

		/*            
            //Debugging output
            System.out.println("start = " + startDateTime.toUiString());
            System.out.println("MinimumStartTime = " + myQuery.getMinimumStartTime());
            System.out.println("end = " + endDateTime.toUiString());
            System.out.println("MaximumStartTime = " + myQuery.getMaximumStartTime());  
		 */

		CalendarEventFeed resultFeed = myService.query(myQuery, CalendarEventFeed.class);
		ArrayList<CalendarEventEntry> results = new ArrayList<>();
		int page = 1;
		do {
			System.out.println("Query page " + page);
			for (int i = 0; i < resultFeed.getEntries().size(); i++) {
				CalendarEventEntry entry = resultFeed.getEntries().get(i);
				results.add(entry);
			}            
			if(resultFeed.getNextLink() != null) {                              //If there's another page of results in the feed, get it
				resultFeed = myService.getFeed(new URL(resultFeed.getNextLink().getHref()), CalendarEventFeed.class);
			} else {
				resultFeed = null;
			}
			page++;
		} while(resultFeed != null);

		return results;
	}

	/**
	 * Makes a batch request to insert all the events in the given list. If any of
	 * the operations fails, the errors returned from the server are displayed.
	 * The CalendarEntry objects in the list given as a parameters must be entries
	 * returned from the server that contain valid edit links (for optimistic
	 * concurrency to work). Note: You can add entries to a batch request for the
	 * other operation types (INSERT, QUERY, and UPDATE) in the same manner as
	 * shown below for DELETE operations.
	 * 
	 * @param service An authenticated CalendarService object.
	 * @param eventsToInsert A list of CalendarEventEntry objects to delete.
	 * @throws ServiceException If the service is unable to handle the request.
	 * @throws IOException Error communicating with the server.
	 */
	public static void insertEvents(List<CalendarEventEntry> eventsToInsert) throws ServiceException,
	IOException {

		// Add each item in eventsToDelete to the batch request.
		CalendarEventFeed batchRequest = new CalendarEventFeed();
		for (int i = 0; i < eventsToInsert.size(); i++) {
			CalendarEventEntry toDelete = eventsToInsert.get(i);
			// Modify the entry toDelete with batch ID and operation type.
			BatchUtils.setBatchId(toDelete, String.valueOf(i));
			BatchUtils.setBatchOperationType(toDelete, BatchOperationType.INSERT);
			batchRequest.getEntries().add(toDelete);
		}

		// Get the URL to make batch requests to
		CalendarEventFeed feed = myService.getFeed(eventFeedUrl,
				CalendarEventFeed.class);
		Link batchLink = feed.getLink(Link.Rel.FEED_BATCH, Link.Type.ATOM);
		URL batchUrl = new URL(batchLink.getHref());

		// Submit the batch request
		CalendarEventFeed batchResponse = myService.batch(batchUrl, batchRequest);

		// Ensure that all the operations were successful.
		boolean isSuccess = true;
		for (CalendarEventEntry entry : batchResponse.getEntries()) {
			String batchId = BatchUtils.getBatchId(entry);
			if (!BatchUtils.isSuccess(entry)) {
				isSuccess = false;
				BatchStatus status = BatchUtils.getBatchStatus(entry);
				System.out.println("\n" + batchId + " failed (" + status.getReason()
						+ ") " + status.getContent());
			}
		}
		if (isSuccess) {
			System.out.println("Successfully inserted all events via batch request.");
		}
	}

	/**
	 * Determine if this is the first run but whether or not settings.ini exists or is empty. 
	 * Creates settings.ini if it doesn't exist.
	 * @return true if settings.ini doesn't exist or is empty. Otherwise, returns false.
	 * @throws IOException Can be thrown by createNewFile() if the file doesn't exist; 
	 * by read(); or by close().
	 * @throws FileNotFoundException when constructing FileReader
	 */
	private static boolean firstRun() throws IOException, FileNotFoundException {
		File file = new File(SETTINGS_INI_LOCATION);
		if(!file.exists()) {   
			file.createNewFile();                                           //If settings.ini doesn't exist create it
			System.out.println("settings created");
			return true;                                                    //settings.ini didn't exist
		} else {
			FileReader fr = new FileReader(new File(OutlookToGoogleCalendarSync.SETTINGS_INI_LOCATION));
			if(fr.read() == -1) {
				//settings.ini empty
				System.out.println("empty");
				fr.close();
				return true;
			} else {
				//settings.ini not empty
				System.out.println("not empty");
				fr.close();
				return false;
			}
		}
	}

	/**
	 * Attempts to locate the .pst in the default locations
	 * @return String containing the location of the .pst file. Returns null if no file was found.
	 */
	private static String locatePST() {
		String location = null;
		if(System.getProperty("os.version").equals("5.1")) {
			//Windows XP
			File file = new File(PST_LOCATION_DEFAULT_XP);
			if(file.exists()) {
				location = PST_LOCATION_DEFAULT_XP;
			} else {
				//Can't find .pst file. Try alternate location
				file = new File(PST_LOCATION_DEFAULT_VISTA2);
				if(file.exists()) {
					location = PST_LOCATION_DEFAULT_VISTA2;
				} 
			}
		} else {
			//Not Windows XP
			File file = new File(PST_LOCATION_DEFAULT_VISTA);
			if(file.exists()) {
				location = PST_LOCATION_DEFAULT_VISTA;
			} else {
				//Can't find .pst file. Try alternate location
				file = new File(PST_LOCATION_DEFAULT_VISTA2);
				if(file.exists()) {
					location = PST_LOCATION_DEFAULT_VISTA2;
				} 
			}
		}
		return location;
	}

	/**
	 * Locates the folder within the file structure of the .pst file that houses the Outlook folders
	 * (e.g. inbox, outbox, trash, calendar, etc.)
	 * @return A List<PSTFolder> containing references to each of the Outlook folders, including the calendar folder
	 * @throws FileNotFoundException
	 * @throws PSTException
	 * @throws IOException
	 */
	public static List<PSTFolder> getOutlookFolders() 
			throws FileNotFoundException, PSTException, IOException {
		PSTFile pstFile = new PSTFile(pstLocation);

		List<PSTFolder> rootSubs = pstFile.getRootFolder().getSubFolders();			//getSubFolers returns a java.util.Vector<PSTFolder>
		return rootSubs.get(0).getSubFolders();      
	}
	
	/**
	 * Read encrypted contents of settings.ini
	 * @return A String containing all the decrypted contents of settings.ini
	 * @throws FileNotFoundException
	 * @throws IOException 
	 */
	static String readSettings() {
		try(BufferedReader br = new BufferedReader(new FileReader(new File(OutlookToGoogleCalendarSync.SETTINGS_INI_LOCATION)))) {
			char[] charBuffer = new char[5000];
			int numChar = br.read(charBuffer, 0, 5000);              //The number of characters read from settings.ini (encrypted)
			String encryptedSettings = new String(charBuffer, 0, numChar);   //Creates a string from the number of chars read
			BasicTextEncryptor encryptor = new BasicTextEncryptor();
			encryptor.setPassword(ENCRYPTOR_PASS);
			return encryptor.decrypt(encryptedSettings);
		} catch (FileNotFoundException e) {
			JOptionPane.showMessageDialog(null,"settings.ini is tested to exist before calling readSettings, so this should never be reached");
			System.exit(0);
		} catch (IOException e) {
			JOptionPane.showMessageDialog(null,"There was a problem reading settings.ini. File may be missing or in use");
			System.exit(0);
		}
		return null;
	}

	/**
	 * Adds or updates an element in the settings field
	 * @param elementName Name of the element to add or update (i.e. "username", "password", or "pstlocation")
	 * @param elementContents The contents of the element being added or updated
	 */
	static void setSettings(String elementName, String elementContents) {
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
	}
	
	//Setters
	public static void setUsername(String un) {
		username = un;
	}
	public static void setPassword(String pass) {
		password = pass;
	}
	static void setPSTLocation(String l) {
		pstLocation = l;
	}

	//Getters
	public static String getUserrname() {
		return username;
	}
	public static String getPassword() {
		return password;
	}
	public static CalendarService getMyService() {
		return myService;
	}
	public static StringBuilder getSettings() {
		return settings;
	}
	public static URL getEventFeedURL() {
		return eventFeedUrl;
	}
	public static Date getStartDate() {
		return startDate;
	}
	public static Date getEndDate() {
		return endDate;
	}
	static List<Path> getPSTResults() {
		return pstResults;
	}

	/**
	 * Create the necessary URL objects.
	 * @throws MalformedURLException 
	 */
	public static void createURLObjects() throws MalformedURLException {
		metafeedUrl = new URL(METAFEED_URL_BASE + username);
		eventFeedUrl = new URL(METAFEED_URL_BASE + username
				+ EVENT_FEED_URL_SUFFIX);
	}

	/**
	 * Retrieves a field entry from the decrypted settings.ini
	 * @param settings String containing the decrypted contents of settings.ini
	 * @param field The field to retrieve from the decrypted settings.ini
	 * @return The field located in the decrypted settings.ini. Returns null if the field was not found.
	 */
	public static String getSettingsField(String field) {
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
	 * Check if a CalendarEventEntry is meaningfully equivalent to a PSTAppointment.
	 * PSTAppointment.getBody() adds a line feed, '\r' (ASCII 13), and a 
	 * carriage return, '\n' (ASCII 10), to the end of the content, even when 
	 * the content is empty. Also, any carriage return in the content is 
	 * preceded by a line feed.
	 * Remove all ASCII 13 from pst
	 * @param cee CalendarEventEntry to be compared.
	 * @param pst PSTAppointment to be compared.
	 * @return true if they are meaningfully equivalent, otherwise false.
	 */
	public static boolean compareCEEToPST(CalendarEventEntry cee, PSTAppointment pst) {
		//DEBUG
		//        System.out.println("Compare:");
		//        System.out.println("Title: " + pst.getSubject() + " vs " + cee.getTitle().getPlainText());
		//        System.out.println("Start: " + pst.getStartTime().toString() + " vs " + new Date(cee.getTimes().get(0).getStartTime().getValue()).toString());
		//        System.out.println("End: " + pst.getEndTime().toString() + " vs " + cee.getTimes().get(0).getEndTime().getValue());
		//        System.out.println("Location: " + pst.getLocation() + " vs " + cee.getLocations().get(0).getValueString());
		//        System.out.println("Content: " + pst.getBody().trim() + " vs " + cee.getPlainTextContent());
		//        
		//Create a String containing the content from pst with the excess line feeds and carriage return removed
		char[] pstContentArray = pst.getBody().trim().toCharArray();
		char[] correctedPSTContentArray = new char[pstContentArray.length];
		int index = 0;
		for(int n = 0; n < pstContentArray.length; n++) {
			if(pstContentArray[n] != (char)13) {
				correctedPSTContentArray[index] = pstContentArray[n];
				index++;
			}
		}
		String pstContentString = new String(correctedPSTContentArray).trim();

		/*DEBUG        
        char[] debugarray2 = cee.getPlainTextContent().toCharArray();
        for(int n = 0; n < debugarray2.length; n++) {
            System.out.println("Array char " + n + " = " + (int)debugarray2[n] + " " + debugarray2[n]);
        }        
		 */        
		if(pstContentString.equals(cee.getPlainTextContent())) {                //If the content on the cee and pst match, compare the rest
			CalendarEventEntry convertedPST = convertPSTToCEE(pst).getCEE();
			//            System.out.println("Matched: PST " + pst.getSubject() + " / " + pst.getStartTime().toString());
			//            System.out.println("with: converted " + convertedPST.getTitle().getPlainText() + " / " + 
			//            				   new Date(convertedPST.getTimes().get(0).getStartTime().getValue()).toString());
			//            System.out.println("with: CEE " + cee.getTitle().getPlainText() + " / " + 
			// 				   new Date(cee.getTimes().get(0).getStartTime().getValue()).toString());
			return cee.getTitle().getPlainText().equals(convertedPST.getTitle().getPlainText()) && 
					cee.getTimes().get(0).getStartTime().getValue() == convertedPST.getTimes().get(0).getStartTime().getValue() &&
					cee.getTimes().get(0).getEndTime().getValue() == convertedPST.getTimes().get(0).getEndTime().getValue() &&
					cee.getLocations().get(0).getValueString().equals(convertedPST.getLocations().get(0).getValueString());
		} else {
			return false;
		}
	}
}