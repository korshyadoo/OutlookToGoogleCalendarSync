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

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.TimeZone;

import com.google.gdata.client.calendar.CalendarQuery;
import com.google.gdata.client.calendar.CalendarService;
import com.google.gdata.data.DateTime;
import com.google.gdata.data.Link;
import com.google.gdata.data.batch.BatchOperationType;
import com.google.gdata.data.batch.BatchStatus;
import com.google.gdata.data.batch.BatchUtils;
import com.google.gdata.data.calendar.CalendarEventEntry;
import com.google.gdata.data.calendar.CalendarEventFeed;
import com.google.gdata.util.AuthenticationException;
import com.google.gdata.util.ServiceException;

/**
 * Contains methods for interfacing with Google API, including authenitcation,
 * running queries, and running batch requests. 
 * 
 * @author korshyadoo
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
	private URL metafeedUrl = null;

	// The URL for the event feed of the specified user's primary calendar.
	// (e.g. http://www.googe.com/feeds/calendar/jdoe@gmail.com/private/full)
	private URL eventFeedUrl = null;

	private Date startDate;						//The start date for the sync range
	private Date endDate;						//The end date for the sync range
	private CalendarService myService;
	private String username;					//Username for logging into Google
	private String password;					//Password for logging into Google
	
	public OutlookToGoogleCalendarSync() {
		myService = new CalendarService("korshyadoo-MyOutlookSync-0.1");
		
		//Set the start and end times used for syncing
		Calendar startCal = new GregorianCalendar();
		startCal.add(Calendar.MONTH, -5);                                   
		startDate = startCal.getTime();
		Calendar endCal = new GregorianCalendar();
		endCal.add(Calendar.YEAR, 100);                                   
		endDate = endCal.getTime(); 
	}
	
	
	/**
	 * Retrieves all events from the Google calendar and adds them to an ArrayList<CalendarEventEntry>
	 * @param service
	 * @return All events from Google Calendar in an ArrayList<CalendarEventEntry>
	 * @throws ServiceException
	 * @throws IOException 
	 */
	public ArrayList<CalendarEventEntry> getAllEvents(CalendarService service)
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
	public void deleteEvents(List<CalendarEventEntry> eventsToDelete) throws ServiceException,
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
	 * Retrieves a List<CalendarEventEntry> containing all appointments in the Google calendar with a start time between startDate and endDate
	 * @param startDate The beginning Date for the query
	 * @param endDate The ending Date for the query
	 * @return A List<CalendarEventEntry> containing all appointments with a start time between startDate and endDate
	 * @throws ServiceException Query request failed or system error retrieving feed (thrown by CalendarService.query() and
	 * CalendarService.getFeed())
	 * @throws IOException Error communicating with the GData service (thrown by CalendarService.query() and
	 * CalendarService.getFeed())
	 */
	public List<CalendarEventEntry> timeQuery(Date startDate, Date endDate) throws ServiceException, IOException {
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
	 * Find all events in gmail calendar in the range determined by the startDate and endDate fields.
	 * @return ArrayList containing all event results. size == 0 if no events found.
	 * @throws ServiceException query request failed
	 * @throws IOException error communicating with the GData service
	 */
	public ArrayList<CalendarEventEntry> timeRangeQuery()
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
	 * Find all events in gmail calendar in the range determined by the startDate and endDate fields.
	 * Overloaded to accept a SwingWorker reference to update the progress of the task.
	 * @return ArrayList containing all event results. size == 0 if no events found.
	 * @throws ServiceException query request failed
	 * @throws IOException error communicating with the GData service
	 */
	public ArrayList<CalendarEventEntry> timeRangeQuery(MainFrame.SyncWorker sw)
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
		double progress = 0;
		double totalResults = resultFeed.getTotalResults();
		int page = 1;
		do {
			System.out.println("Query page " + page);
			for (int i = 0; i < resultFeed.getEntries().size(); i++) {			//For each entry in the result feed
				CalendarEventEntry entry = resultFeed.getEntries().get(i);
				results.add(entry);
				progress += ((1 / totalResults) * 80);							//Each result increases the progress bar to a max of 80%
				sw.publicSetProgress((int)progress);
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
	public void insertEvents(List<CalendarEventEntry> eventsToInsert) 
			throws ServiceException, IOException {

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

	public void setUserCredentials() throws AuthenticationException {
		myService.setUserCredentials(username, password);
	}

	/**
	 * Returns the CalendarService used to connect to Google
	 * @return the myService field
	 */
	CalendarService getMyService() {
		return myService;
	}
	
	/**
	 * Returns the URL for the event feed of the specified user's primary calendar
	 * @return the eventFeedUrl field
	 */
	URL getEventFeedURL() {
		return eventFeedUrl;
	}
	
	/**
	 * Returns the date of the beginning of the range of time that will be synced to the Google calendar
	 * @return a Date set to the start of the time range used for syncing
	 */
	public Date getStartDate() {
		return new Date(startDate.getTime());
	}
	
	/**
	 * Returns the date of the end of the range of time that will be synced to the Google calendar
	 * @return a Date set to the end of the time range used for syncing
	 */
	public Date getEndDate() {
		return new Date(endDate.getTime());
	}
	
	/**
	 * Returns the Google username
	 * @return a String containing the Google username
	 */
	public String getUsername() {
		return username;
	}
		
	/**
	 * Sets the Google username
	 * @param un a String containing the Google username
	 */
	public void setUsername(String un) {
		username = un;
	}
	
	/**
	 * Sets the Google password
	 * @param pw a String containing the Google password
	 */
	public void setPassword(String pw) {
		password = pw;
	}

	/**
	 * Create the necessary URL objects.
	 * @throws MalformedURLException The user is authenticated before creating the URL objects, therefore the URL should never be malformed
	 */
	public void createURLObjects() throws MalformedURLException {
		metafeedUrl = new URL(METAFEED_URL_BASE + username);
		eventFeedUrl = new URL(METAFEED_URL_BASE + username
				+ EVENT_FEED_URL_SUFFIX);
	}
}