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


//TODO:
//When syncing, if a appointment is found in gmail that doesn't
// exist in outlook, write it to a log file
//Why is timeQuery() boolean??
//Allow for the option to do a full sync of the calendar in a later build (will be very time consuming and resource heavy)
//Securty can be tightened by changing username, password, and settings to char[] instead of String.
// This would allow the char[] to be wiped after use instead of having an 
// immutable String hanging around.
// This would require changing getSettingsField() to do a char[] search.
//Deal with all catch blocks
//Make sure the pst file on the computer can be found and correct directory structure for settings.ini
//Add a progress bar
//Add the ability to cancel the sync
//Something to think about: the created date for a CEE is the date it was synced
// to the calendar, not the date the PSTAppointment was created
//Investigate combo box setModel()
//Investigate: In the MainFrame being passed to IOExceptionFrame protected fields 
// are visible but private fields are not. Should all used fields be made private?
//When switching frames, instead of disposes of the mainframe, just set it invisible
//Is passing settings.toString() to getSettingsField redundant?
//Debug the locating of the PST file



//BUGS:
//When a duplicate appointment exists on the google calendar, syncing
// doesn't remove one of them (CAN'T DUPLICATE)
//When running delete all events, when exiting the method, an exception is thrown, but doesn't get handled properly
//Action times are wrong
//Ran Delete all Events followed by a Sync and it reported "In Google: 0 events before, 1492 after"
// Running a new Sync immediately after reported "In Google: 365 events before, 365 after"
//DelteAllEvents not updating action time label


package com.korshyadoo.calendar;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.TimeZone;
import java.util.Vector;

import javax.swing.UIManager;

import org.jasypt.util.text.BasicTextEncryptor;

import com.google.gdata.client.Query;
import com.google.gdata.client.calendar.CalendarQuery;
import com.google.gdata.client.calendar.CalendarService;
import com.google.gdata.data.DateTime;
import com.google.gdata.data.Link;
import com.google.gdata.data.PlainTextConstruct;
import com.google.gdata.data.batch.BatchOperationType;
import com.google.gdata.data.batch.BatchStatus;
import com.google.gdata.data.batch.BatchUtils;
import com.google.gdata.data.calendar.CalendarEntry;
import com.google.gdata.data.calendar.CalendarEventEntry;
import com.google.gdata.data.calendar.CalendarEventFeed;
import com.google.gdata.data.calendar.CalendarFeed;
import com.google.gdata.data.calendar.WebContent;
import com.google.gdata.data.extensions.Recurrence;
import com.google.gdata.data.extensions.When;
import com.google.gdata.data.extensions.Where;
import com.google.gdata.util.AuthenticationException;
import com.google.gdata.util.ServiceException;
import com.pff.PSTAppointment;
import com.pff.PSTException;
import com.pff.PSTFile;
import com.pff.PSTFolder;

/**
 * Syncs Outlook calendar to Google calendar
 * 
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

	//Make false to remove blocks not needed in deployment
	private static final boolean DEBUG_OUTPUT = false;  
	private static final boolean DEBUG1 = true;

	//The default location for the .pst file in Windows XP
	public static final String PST_LOCATION_DEFAULT_XP = "C:\\Documents and Settings\\" + 
			System.getProperty("user.name") +
			"\\Local Settings\\Application Data\\Microsoft\\Outlook\\Outlook.pst";

	//The default location for the .pst file in Windows Vista / 7
	public static final String PST_LOCATION_DEFAULT_VISTA = "C:\\Users\\" +
			System.getProperty("user.name") +
			"\\username\\AppData\\Local\\Microsoft\\Outlook\\Outlook.pst";

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
	private static StringBuilder settings;
	private static Date startDate;
	private static Date endDate;
	private static String pstLocation;			//Location of the PST file used by Outlook
	private static List<Path> pstResults;

	//Constructor
	public OutlookToGoogleCalendarSync() {

	}

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

		//Get the start and end times used for syncing
		Calendar startCal = new GregorianCalendar();
		startCal.add(Calendar.MONTH, -7);                                   
		startDate = startCal.getTime();
		Calendar endCal = new GregorianCalendar();
		endCal.add(Calendar.YEAR, 100);                                   
		endDate = endCal.getTime();      

		//Set look and feel
		try {
			UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
		} catch (Throwable e) {
			e.printStackTrace();
		}

		//If this is the first time running, or if settings.ini is missing,
		//locate the .pst file and run FirstRunFrame, otherwise run MainFrame. 
		//If authentication exception is thrown running MainFrame, run FirstRunFrame
		try {
			if(firstRun()) {			//First time running the application or missing settings.ini
				//Locate the .pst file
				pstLocation = locatePST();
				if(pstLocation != null) {
					//A .pst file was found
					//Write pstLocation to settings.ini for future executions
					setSettings("pstLocation=" + pstLocation + "\n");

					//Run FirstRunFrame
					java.awt.EventQueue.invokeLater(new Runnable() {
						@Override
						public void run() {
							new FirstRunFrame().setVisible(true);
						}
					});
				} else {
					//No .pst file was found
					//Run NoPSTFrame
					java.awt.EventQueue.invokeLater(new Runnable() {
						@Override
						public void run() {
							new NoPSTFrame().setVisible(true);
						}
					});
				}
			} else {		//Not the first run
				//Read credentials from settings.ini, set username and password static fields,
				// set the credentials for myService, and create URL Objects
				settings = new StringBuilder(readSettings());
				username = getSettingsField(settings.toString(), "username");
				password = getSettingsField(settings.toString(), "password");
				pstLocation = getSettingsField(settings.toString(), "pstLocation");
				myService.setUserCredentials(username, password);
				createURLObjects();

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
			System.err.println("Uh oh - you've got an invalid URL.");
		} catch(AuthenticationException e) {
			System.out.println("Authentication error");

			//Run FirstRunFrame
			java.awt.EventQueue.invokeLater(new Runnable() {
				@Override
				public void run() {
					new FirstRunFrame().setVisible(true);
				}
			});          
		} catch (IOException e) {
			System.out.println("IOException checking settings.ini");
		}

		//Write the lastMod time and then a test line to settings.ini
		if(DEBUG_OUTPUT) {
			DateFormat df = new SimpleDateFormat("dd/MM/yyyy");
			try (PrintWriter outFile = new PrintWriter(new File(SETTINGS_INI_LOCATION))) {
				Date outMod = df.parse("01/01/1999");
				outFile.println(outMod.getTime());
				outFile.println("123");
			} catch(FileNotFoundException e) {
				e.printStackTrace();
			} catch(ParseException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Prints the titles of all events on the calendar specified by
	 * {@code feedUri}.
	 * 
	 * @param service An authenticated CalendarService object.
	 * @throws ServiceException If the service is unable to handle the request.
	 * @throws IOException Error communicating with the server.
	 */
	private static void printAllEvents(CalendarService service)
			throws ServiceException, IOException {
		// Send the request and receive the response:
		CalendarEventFeed resultFeed = service.getFeed(eventFeedUrl,
				CalendarEventFeed.class);

		System.out.println("All events on your calendar:");
		System.out.println();
		for (int i = 0; i < resultFeed.getEntries().size(); i++) {
			CalendarEventEntry entry = resultFeed.getEntries().get(i);
			System.out.println("\t" + entry.getTitle().getPlainText() + " / " + entry.getTimes().get(0).getStartTime());
		}
		System.out.println();
	}

	/**
	 * Retrieves all events from the Google calendar and adds them to an ArrayList<CalendarEventEntry>
	 * @Author Korshyadoo
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
	 * @author Korshyadoo
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
	 * Helper method to create either single-instance or recurring events. For
	 * simplicity, some values that might normally be passed as parameters (such
	 * as author name, email, etc.) are hard-coded.
	 * 
	 * @param service An authenticated CalendarService object.
	 * @param eventTitle Title of the event to create.
	 * @param eventContent Text content of the event to create.
	 * @param recurData Recurrence value for the event, or null for
	 *        single-instance events.
	 * @param isQuickAdd True if eventContent should be interpreted as the text of
	 *        a quick add event.
	 * @param wc A WebContent object, or null if this is not a web content event.
	 * @return The newly-created CalendarEventEntry.
	 * @throws ServiceException If the service is unable to handle the request.
	 * @throws IOException Error communicating with the server.
	 */
	private static CalendarEventEntry createEvent(CalendarService service,
			String eventTitle, String eventContent, String recurData,
			boolean isQuickAdd, WebContent wc) throws ServiceException, IOException {
		CalendarEventEntry myEntry = new CalendarEventEntry();

		myEntry.setTitle(new PlainTextConstruct(eventTitle));
		myEntry.setContent(new PlainTextConstruct(eventContent));
		myEntry.setQuickAdd(isQuickAdd);
		myEntry.setWebContent(wc);

		// If a recurrence was requested, add it. Otherwise, set the
		// time (the current date and time) and duration (30 minutes)
		// of the event.
		if (recurData == null) {
			Calendar calendar = new GregorianCalendar();
			DateTime startTime = new DateTime(calendar.getTime(), TimeZone
					.getDefault());

			calendar.add(Calendar.MINUTE, 30);
			DateTime endTime = new DateTime(calendar.getTime(), 
					TimeZone.getDefault());

			When eventTimes = new When();
			eventTimes.setStartTime(startTime);
			eventTimes.setEndTime(endTime);
			myEntry.addTime(eventTimes);
		} else {
			Recurrence recur = new Recurrence();
			recur.setValue(recurData);
			myEntry.setRecurrence(recur);
		}

		// Send the request and receive the response:
		return service.insert(eventFeedUrl, myEntry);
	}

	/**
	 * Creates a single-occurrence event.
	 * 
	 * @param service An authenticated CalendarService object.
	 * @param eventTitle Title of the event to create.
	 * @param eventContent Text content of the event to create.
	 * @return The newly-created CalendarEventEntry.
	 * @throws ServiceException If the service is unable to handle the request.
	 * @throws IOException Error communicating with the server.
	 */
	private static CalendarEventEntry createSingleEvent(CalendarService service,
			String eventTitle, String eventContent) throws ServiceException,
			IOException {
		return createEvent(service, eventTitle, eventContent, null, false, null);
	}

	/**
	 * Creates a quick add event.
	 * 
	 * @param service An authenticated CalendarService object.
	 * @param quickAddContent The quick add text, including the event title, date
	 *        and time.
	 * @return The newly-created CalendarEventEntry.
	 * @throws ServiceException If the service is unable to handle the request.
	 * @throws IOException Error communicating with the server.
	 */
	private static CalendarEventEntry createQuickAddEvent(
			CalendarService service, String quickAddContent) throws ServiceException,
			IOException {
		return createEvent(service, null, quickAddContent, null, true, null);
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
	 * Prints a list of all the user's calendars.
	 * 
	 * @param service An authenticated CalendarService object.
	 * @throws ServiceException If the service is unable to handle the request.
	 * @throws IOException Error communicating with the server
	 */
	private static void printUserCalendars(CalendarService service)
			throws IOException, ServiceException {
		// Send the request and receive the response:
		CalendarFeed resultFeed = service.getFeed(metafeedUrl, CalendarFeed.class);

		System.out.println("Your calendars:");
		System.out.println();
		for (int i = 0; i < resultFeed.getEntries().size(); i++) {
			CalendarEntry entry = resultFeed.getEntries().get(i);
			System.out.println("\t" + entry.getTitle().getPlainText());
		}
		System.out.println();
	}

	/**
	 * Prints the titles of all events matching a full-text query.
	 * 
	 * @param service An authenticated CalendarService object.
	 * @param query The text for which to query.
	 * @throws ServiceException If the service is unable to handle the request.
	 * @throws IOException Error communicating with the server.
	 */
	private static void fullTextQuery(CalendarService service, String query)
			throws ServiceException, IOException {
		Query myQuery = new Query(eventFeedUrl);
		myQuery.setFullTextQuery("Work2");

		CalendarEventFeed resultFeed = service.query(myQuery,
				CalendarEventFeed.class);

		System.out.println("Events matching " + query + ":");
		System.out.println();
		for (int i = 0; i < resultFeed.getEntries().size(); i++) {
			CalendarEventEntry entry = resultFeed.getEntries().get(i);
			System.out.println("\t" + entry.getTitle().getPlainText());
		}
		System.out.println();
	}

	/**
	 * Searches for an appointment at a specific start time. 
	 * CalendarQuery.MaximumStartTime needs to be 2 seconds
	 * greater than CalendarQuery.MinimumStartTime. 
	 * Constructing the MaximumStartTime DateTime using a long instead of a Date
	 * does not properly set the MaximumStartTime for some reason
	 * @author Korshyadoo
	 * @param service The calendar service
	 * @param query The start time to search for
	 * @throws ServiceException
	 * @throws IOException 
	 */
	public static boolean timeQuery(PSTAppointment query)
			throws ServiceException, IOException {
		//Retrive the query feed by searching by the start time
		CalendarQuery myQuery = new CalendarQuery(eventFeedUrl);
		Date date = query.getStartTime();
		DateTime start = new DateTime(date, TimeZone.getDefault());
		myQuery.setMinimumStartTime(start);
		Date endDate = new Date(start.getValue());
		Calendar endCal = new GregorianCalendar();
		endCal.setTime(endDate);
		endCal.add(Calendar.SECOND, 2);
		DateTime end = new DateTime(endCal.getTime(), TimeZone.getDefault());
		myQuery.setMaximumStartTime(end);
		CalendarEventFeed resultFeed = myService.query(myQuery, CalendarEventFeed.class);

		//DEBUG OUTPUT
		//        System.out.println("start = " + start.toString());
		//        System.out.println("MinimumStartTime = " + myQuery.getMinimumStartTime());
		//        System.out.println("end = " + end.toStringRfc822());
		//        System.out.println("MaximumStartTime = " + myQuery.getMaximumStartTime());      

		//Compare the entries in the result feed to find one equivalent to query
		int page = 1;
		do {
			for (int i = 0; i < resultFeed.getEntries().size(); i++) {          //For each entry in the resultFeed
				CalendarEventEntry entry = resultFeed.getEntries().get(i);
				System.out.println("Found timeQuery:");
				System.out.println("Start: " + new Date(entry.getTimes().get(0).getStartTime().getValue()).toString() +
						" / End: " + new Date(entry.getTimes().get(0).getEndTime().getValue()).toString() +
						" / Title: " + entry.getTitle().getPlainText() +
						" / Location: " + entry.getLocations().get(0).getValueString() +
						" / Content: " + entry.getPlainTextContent());
				if(compareCEEToPST(entry, query)) {                             //If the entry is equivalent to the query PST
					return true;                                                //return true
				}
			}            
			if(resultFeed.getNextLink() != null) {                              //If there's another page of results in the feed, get it
				resultFeed = myService.getFeed(new URL(resultFeed.getNextLink().getHref()), CalendarEventFeed.class);
			} else {
				resultFeed = null;
			}
			page++;
		} while(resultFeed != null);

		//No match found
		return false;
	}

	/**
	 * Finds a specific appointment on the Google calendar and outputs its details.
	 * For debugging
	 * @return
	 * @throws ServiceException
	 * @throws IOException
	 */
	public static boolean timeQuery() throws ServiceException, IOException {
		//Set up the resultfeed
		CalendarQuery myQuery = new CalendarQuery(eventFeedUrl);
		Calendar startCal = new GregorianCalendar();
		startCal.set(2013, 3, 7, 9, 0);
		Date startDate = startCal.getTime();
		DateTime start = new DateTime(startDate, TimeZone.getDefault());
		myQuery.setMinimumStartTime(start);
		Calendar endCal = new GregorianCalendar();
		endCal.set(2013, 3, 14, 21, 0);
		Date endDate = endCal.getTime();
		DateTime end = new DateTime(endCal.getTime(), TimeZone.getDefault());
		myQuery.setMaximumStartTime(end);
		CalendarEventFeed resultFeed = myService.query(myQuery, CalendarEventFeed.class);

		//Retrieve the results
		int page = 1;
		do {
			for (int i = 0; i < resultFeed.getEntries().size(); i++) {          //For each entry in the resultFeed
				CalendarEventEntry entry = resultFeed.getEntries().get(i);
				System.out.println("Start: " + new Date(entry.getTimes().get(0).getStartTime().getValue()).toString() +
						" / End: " + new Date(entry.getTimes().get(0).getEndTime().getValue()).toString() +
						" / Title: " + entry.getTitle().getPlainText() +
						" / Location: " + entry.getLocations().get(0).getValueString() +
						" / Content: " + entry.getPlainTextContent());
				System.out.println("Created: " + new Date(entry.getPublished().getValue()).toString() + "\n");
			}            
			if(resultFeed.getNextLink() != null) {                              //If there's another page of results in the feed, get it
				resultFeed = myService.getFeed(new URL(resultFeed.getNextLink().getHref()), CalendarEventFeed.class);
			} else {
				resultFeed = null;
			}
			page++;
		} while(resultFeed != null);

		return true;
	}
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
			for (int i = 0; i < resultFeed.getEntries().size(); i++) {          //For each entry in the resultFeed
				CalendarEventEntry entry = resultFeed.getEntries().get(i);
				Date entryDate = new Date(entry.getTimes().get(0).getStartTime().getValue());
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
	 * @throws IOException 
	 */
	private static boolean firstRun() throws IOException {
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
	 * Searches c:\ for any .pst files. If only one is found, the String location is set to the location of the .pst. 
	 * If none are found, loation is left null. If more than one is found, ChoosePSTFrame is launced to have the 
	 * user choose which file to use.
	 * @param location Passed in as null and set to the location of a .pst file once found
	 */
	private static void pstSearch(String location) {
		Path startingDir = Paths.get("c:\\");
		String pattern = "*.pst";
		Finder finder = new Finder(pattern);
		try {
			Files.walkFileTree(startingDir, finder);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		List<Path> results = finder.getResults();
		if(results.size() == 1) {
			//Only one pst file found
			location = results.get(0).toString();
		} else if(results.size() > 1) {
			//Pass the list of pst files to the contsructor of a new form that will let the user choose which pst file to use
			System.out.println("more than one found");
			//Run ChoosePSTFrame
			java.awt.EventQueue.invokeLater(new Runnable() {
				@Override
				public void run() {
					new ChoosePSTFrame().setVisible(true);
				}
			});
		} else {
			//No results found. Leave location as null
		}
	}


	/**
	 * Locates the Outlook .pst file. 
	 * The default locations are checked first and if unsuccessful, 
	 * a full search of c:\ is conducted. If multiple .pst files are found, 
	 * the user is prompted to pick one.
	 * @return String containing the location of the .pst file. Null if no file was found.
	 */
	private static String locatePST() {
		String location = null;
		if(System.getProperty("os.version").equals("5.1")) {
			//Windows XP
			File file = new File(PST_LOCATION_DEFAULT_XP);
			if(file.exists()) {
				location = PST_LOCATION_DEFAULT_XP;
			} else {
				//Can't find .pst file. Initiate search
				//TODO Create frame showing search progress
				pstSearch(location);
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
				} else {
					//Can't find .pst file. Initiate search
					//TODO Create frame showing search progress
					pstSearch(location);
				}
			}
		}
		return location;
	}

	public static Vector<PSTFolder> getOutlookFolders() 
			throws FileNotFoundException, PSTException, IOException {
		PSTFile pstFile = new PSTFile("C:\\Users\\Lappy2\\Documents\\Outlook Files\\Outlook.pst");

		Vector<PSTFolder> rootSubs = pstFile.getRootFolder().getSubFolders();
		return rootSubs.get(0).getSubFolders();      
	}

	//Setters
	public static void setUsername(String un) {
		username = un;
	}
	public static void setPassword(String pass) {
		password = pass;
	}
	public static void setSettings(StringBuilder s) {
		if(settings == null) {
			settings = new StringBuilder(s);
		} else {
			settings.append(s);
		}
	}
	public static void setSettings(String s) {
		if(settings == null) {
			settings = new StringBuilder(s);
		} else {
			settings.append(s);
		}
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
	 * Read encrypted contents of settings.ini
	 * @return A String containing all the decrypted contents of settings.ini
	 * @throws FileNotFoundException
	 * @throws IOException 
	 */
	public static String readSettings() throws FileNotFoundException, IOException {
		BufferedReader br = new BufferedReader(new FileReader(new File(OutlookToGoogleCalendarSync.SETTINGS_INI_LOCATION)));
		char[] charBuffer = new char[5000];
		int numChar = br.read(charBuffer, 0, 5000);              //The number of characters read from settings.ini (encrypted)
		String encryptedSettings = new String(charBuffer, 0, numChar);   //Creates a string from the number of chars read
		BasicTextEncryptor encryptor = new BasicTextEncryptor();
		encryptor.setPassword(ENCRYPTOR_PASS);
		return encryptor.decrypt(encryptedSettings);
	}

	/**
	 * Retrieves a field entry from the decrypted settings.ini
	 * @param settings String containing the decrypted contents of settings.ini
	 * @param field The field to retrieve from the decrypted settings.ini
	 * @return The field located in the decrypted settings.ini. Returns null if the field was not found.
	 */
	public static String getSettingsField(String settings, String field) {
		String result = null;
		int searchIndex = settings.indexOf(field);
		if(searchIndex != -1) {
			searchIndex += (field.length() + 1);     //Points unIndex at the beginning of the field (e.g. username is stored in settings.ini as "username=xxxxx", where"xxxxx" is the username, so unIndex begins after "=")
			int searchEndIndex = searchIndex + 1;

			//Point unEndIndex at the next whitespace character
			while(searchEndIndex < settings.length() && !Character.isWhitespace(settings.charAt(searchEndIndex))) {         //While usernameEndIndex is not whitespace, and EOF hasn't been reached
				searchEndIndex++;
			}

			result = settings.substring(searchIndex, searchEndIndex);
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