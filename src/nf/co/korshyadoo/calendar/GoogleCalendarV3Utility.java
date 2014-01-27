package nf.co.korshyadoo.calendar;

import java.io.IOException;
import java.io.InputStream;
import java.net.SocketTimeoutException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Properties;

import javax.swing.JOptionPane;

import com.google.api.client.auth.oauth2.TokenResponseException;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeRequestUrl;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeTokenRequest;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.googleapis.batch.BatchRequest;
import com.google.api.client.googleapis.batch.json.JsonBatchCallback;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.googleapis.json.GoogleJsonError;
import com.google.api.client.http.HttpHeaders;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson.JacksonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.CalendarList;
import com.google.api.services.calendar.model.CalendarListEntry;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;
import com.google.api.services.calendar.model.Events;


/**
 * @author korshyadoo
 *
 */
public class GoogleCalendarV3Utility implements GoogleCalendarUtility {
	private final String PROPERTIES_LOCATION = "properties.properties";				//The location of the properties file
	private Calendar service;
	private final String CLIENT_ID = "459542417843.apps.googleusercontent.com";
	private final String CLIENT_SECRET = "7a8NYV1-QHhF1YHJxdP6aCKi";
	private final String REDIRECT_URL_1 = "urn:ietf:wg:oauth:2.0:oob";
	private final Collection<String> SCOPES = new ArrayList<>();
	private GoogleCredential credential;
	private String authorizationUrl;
	private String username;
	private Date startDate;
	private Date endDate;
	
	{
		//Retrieve the start and end dates from the .properties file
		Properties prop = new Properties();
		ClassLoader loader = Thread.currentThread().getContextClassLoader();           
		InputStream stream = loader.getResourceAsStream(PROPERTIES_LOCATION);
		try {
			prop.load(stream);
		} catch (IOException e) {
			JOptionPane.showMessageDialog(null, "IO exception reading .properties file. The file may be inaccessible or in use");
			e.printStackTrace();
		}
		Integer monthsBack = Integer.parseInt(prop.getProperty("monthsBack"));
		Integer monthsForward = Integer.parseInt(prop.getProperty("monthsForward"));
		setStartAndEndDates(monthsForward, monthsBack);
		credential = createCredential();
		service = createCalendarService();
	}

	/**
	 * If there is no refresh token passed to the constructor, the user is directed to the Google
	 * authorization page in their browser to obtain an authorization code.
	 * @param userName The user being authenticated on the Google server
	 */
	public GoogleCalendarV3Utility(String username) {
		this.setUsername(username); 
		
		//The authorization URL is made of many parts and is constructed using GoogleAuthorizationCodeRequestUrl
		setAuthorizationUrl(new GoogleAuthorizationCodeRequestUrl(CLIENT_ID, REDIRECT_URL_1, SCOPES).build());
	}

	/**
	 * When a refresh token is passed to the constructor, it is used to obtain an access token
	 * instead of having the user obtaining an authorization code from their browser.
	 * @param refreshToken The refresh token used to obtain the access token for the Google service
	 * @throws IOException 
	 * @throws TokenResponseException Error getting access token or invalid refresh token. An error can occur if the credentials
	 * from the Google API console are invalid (i.e. ClientID and Client Secret)
	 * @throws SocketTimeoutException 
	 */
	public GoogleCalendarV3Utility(String username, String refreshToken) throws SocketTimeoutException, TokenResponseException, IOException {
		this.setUsername(username);
		
		setAccessToken(refreshToken);
	}
	
	
	/**
	 * DEBUG
	 */
	public void getColours() {
		try {
			String pageToken = null;
			do {
				CalendarList calendarList = service.calendarList().list().setPageToken(pageToken).execute();
				List<CalendarListEntry> items = calendarList.getItems();

				for (CalendarListEntry calendarListEntry2 : items) {
					System.out.println(calendarListEntry2.getSummary());
					System.out.println(calendarListEntry2.getId());
					System.out.println("Colour: " + calendarListEntry2.getColorId());
				}
				pageToken = calendarList.getNextPageToken();
			} while (pageToken != null);
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	
	/**
	 * Sets the start and end times used for syncing. When the calendar is synced, it will
	 * only sync events within this time range
	 * @param monthsForward The number of months forward from the current date to sync. 
	 * i.e. If it is currently September and monthsForward=12, the calendar will sync
	 * appointments up to August next year.
	 * @param monthsBack The number of months back from the current date to sync.
	 * i.e. If it is currently September and monthsBack=2, the calendar will sync
	 * appointments starting in July
	 */
	private void setStartAndEndDates(Integer monthsForward, Integer monthsBack) {
		//If monthsForward is null, set the default value
		if(monthsForward == null) {
			monthsForward = new Integer(1200);
		}
		
		//If monthsBack is null, set the default value
		if(monthsBack == null) {
			monthsBack = new Integer(5);
		}
		
		
		//Set the start and end times
		java.util.Calendar startCal = new GregorianCalendar();
		startCal.add(java.util.Calendar.MONTH, monthsBack*(-1));                                    
		setStartDate(startCal.getTime());
		java.util.Calendar endCal = new GregorianCalendar();
		endCal.add(java.util.Calendar.MONTH, monthsForward);                                   
		setEndDate(endCal.getTime()); 
	}

	private GoogleCredential createCredential() {
		SCOPES.add("https://www.googleapis.com/auth/calendar");
		SCOPES.add("https://www.googleapis.com/auth/calendar.readonly");
		HttpTransport transport;
		try {
			transport = GoogleNetHttpTransport.newTrustedTransport();
		} catch (GeneralSecurityException | IOException e) {
			transport = new NetHttpTransport();
		}
		JacksonFactory jsonFactory = new JacksonFactory();
		GoogleCredential.Builder credBuilder = new GoogleCredential.Builder();
		credBuilder.setJsonFactory(jsonFactory);
		credBuilder.setTransport(transport);
		credBuilder.setClientSecrets(CLIENT_ID, CLIENT_SECRET);
		return credBuilder.build();
	}

	public void printCalendarIDs() throws IOException {
		String pageToken = null;
		do {
			CalendarList calendarList = service.calendarList().list().setPageToken(pageToken).execute();
			List<CalendarListEntry> items = calendarList.getItems();

			for (CalendarListEntry calendarListEntry2 : items) {
				System.out.println(calendarListEntry2.getSummary());
				System.out.println(calendarListEntry2.getId());
			}
			pageToken = calendarList.getNextPageToken();
		} while (pageToken != null);
	}
	
	
	/**
	 * Retrieves all events from the Google calendar that are within the time range specified
	 * in the properties file
	 */
	public List<Event> dateRangeQuery(MainFrame.SyncWorker syncWorker) {
		List<Event> results = new ArrayList<>();															//Stores only the events that are within the date range specified in the .properties file
		
		double progress = 0;
		//<--Retrieve all events in the Google calendar
		try {
			String pageToken = null;
			do {
				Events events = service.events().list("primary").setPageToken(pageToken).execute();			//Executes the query
				List<Event> allEventsPage = events.getItems();												//Events are retrieved from the Google calendar one page at a time and stored in this List
				
				//<--For each retrieved event, if it's within the date range, add it to the result List
				for (Event event : allEventsPage) {
					long eventStartLong = getStartDateTime(event);
					long eventEndLong = getEndDateTime(event);
					
					if(eventStartLong >= getStartDate().getTime() &&
							eventEndLong < getEndDate().getTime()) {
						results.add(event);
					}
				}
				//<--For each END

				//Increase progress but don't let it go above 80
				progress += 10.0;
				if(progress > 80.0) {
					progress = 80.0;
				}
				
				syncWorker.publicSetProgress(progress);
				
				pageToken = events.getNextPageToken();
			} while (pageToken != null);																	//Retrieve the next page of results if there is one
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//<--Retrieve END

		return results;
	}

	/**
	 * Retrieves the created date of the most recently created {@code Event} in the Google calendar
	 * @return The created date of the most recently created {@code Event} in the Google calendar as a {@code long}
	 */
	public long getLatestCreatedDate() {
		long result = 0;
		
		try {
			String pageToken = null;
			do {
				Events events = service.events().list("primary").setPageToken(pageToken).execute();			//Executes the query
				List<Event> allEventsPage = events.getItems();												//Events are retrieved from the Google calendar one page at a time and stored in this List

				for (Event event : allEventsPage) {
					if(event.getCreated().getValue() > result) {
						result = event.getCreated().getValue();
					}
				}
				pageToken = events.getNextPageToken();
			} while (pageToken != null);																	//Retrieve the next page of results if there is one
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return result;
	}
	
	
	/**
	 * Retrieves all events in the Google calendar.
	 * @return A List<Event> containing all events in the Google calendar. Returns {@code null} if an error ocurred. 
	 * Returns a List<Event> of size 0 if no events found.
	 */
	public List<Event> allEventsQuery() {
		List<Event> results = new ArrayList<>();												//Stores only the events that are within the date range specified in the .properties file
		
		String pageToken = null;
		do {
			Events events;
			List<Event> allEventsPage;
			try {
				events = service.events().list("primary").setPageToken(pageToken).execute();	//Executes the query
				allEventsPage = events.getItems();												//Events are retrieved from the Google calendar one page at a time and stored in this List
			} catch (IOException e) {
				return null;
			}			
			
			//For each Event in this page of results, add it to the result List
			for (Event event : allEventsPage) {
				results.add(event);
			}

			pageToken = events.getNextPageToken();
		} while (pageToken != null);
		return results;
	}


	/**
	 * Takes a refresh token and uses it to set the access token on the GoogleCredential field 
	 * @param refreshToken
	 * @throws SocketTimeoutException If there was a timeout waiting for the server
	 * @throws TokenResponseException 
	 * @throws IOException
	 */
	public void setAccessToken(String refreshToken) throws SocketTimeoutException, TokenResponseException, IOException {
		credential.setRefreshToken(refreshToken);
		credential.refreshToken();
		System.out.println("Access token: " + credential.getAccessToken());
	}

	/**
	 * Takes the authorization code obtained from the redirect URL and uses it to obtain an access token and a refresh token on the GoogleCredential field
	 * @param authorizationCode The authorization code entered by the user to obtain access and refresh tokens from
	 * @return {@code true} if access and refresh tokens were obtained successfully; otherwise, {@code false}
	 */
	public boolean setAccessAndRefreshTokens(String authorizationCode) {
		System.out.println("Obtaining new access token");
		try {
			GoogleTokenResponse tokenResponse = new GoogleAuthorizationCodeTokenRequest(credential.getTransport(), credential.getJsonFactory(), CLIENT_ID, CLIENT_SECRET, authorizationCode, REDIRECT_URL_1).execute();
			if(tokenResponse != null) {
				credential.setFromTokenResponse(tokenResponse);
				return true;
			}
		}  catch (TokenResponseException e) {
			if (e.getDetails() != null) {
				System.err.println("Error: " + e.getDetails().getError());
				if (e.getDetails().getErrorDescription() != null) {
					System.err.println(e.getDetails().getErrorDescription());
				}
				if (e.getDetails().getErrorUri() != null) {
					System.err.println(e.getDetails().getErrorUri());
				}
			} else {
				System.err.println(e.getMessage());
			}
		} catch (IOException ioe) {
			//Google does not explain why this throws an IOException
			JOptionPane.showMessageDialog(null, "Unexplained IO Exception");
			System.exit(0);
		}
		return false;
	}

	/**
	 * Uses the authenticated GoogleCredential to create a calendar service which is, in turn, 
	 * used to send/receive data to/from the Google calendar
	 * @return The calendar service
	 */
	public Calendar createCalendarService() {
		Calendar.Builder builder = new Calendar.Builder(credential.getTransport(), credential.getJsonFactory(), credential);
		builder.setHttpRequestInitializer(credential);
		builder.setApplicationName("Outlook Sync");

		return builder.build();
	}
	
	
	public boolean deleteEvents(List<Event> eventsToDelete) {
		JsonBatchCallback<Void> callback = new MyCallback<>();

		BatchRequest batch = service.batch();
		try {
			//For each event in the List, get it's eventID and add it to the batch queue
			for(Event event : eventsToDelete) {
				String eventID = event.getId();
				
				//delete() requires the calendarID and the eventID
				//queue() adds the command to the batch queue. The command isn't executed until batch.execute() is run
				service.events().delete("primary", eventID).queue(batch, callback);
			}
			
			batch.execute();
			
 		} catch (IOException e) {
 			JOptionPane.showMessageDialog(null, "IO Exception building HTTP Request");
 			System.exit(5);
 		}
		
		return true;
	}
	
	public boolean insertEvents(List<Event> eventsToInsert) {
		JsonBatchCallback<Event> callback = new MyCallback<Event>();

		BatchRequest batch = service.batch();
		try {
			//For each event in the List, add it to the batch queue
			for(Event event : eventsToInsert) {
				//queue() adds the command to the batch queue. The command isn't executed until batch.execute() is run
				service.events().insert("primary", event).queue(batch, callback);
			}
			
			batch.execute();
 		} catch (IOException e) {
 			JOptionPane.showMessageDialog(null, "IO Exception building HTTP Request");
 			System.exit(5);
 		}
		return true;
	}
	
	
	
	
	private class MyCallback<T> extends JsonBatchCallback<T> {
		@Override
		public void onFailure(GoogleJsonError arg0, HttpHeaders arg1)
				throws IOException {
			System.out.println("Error Message: " + arg0.getMessage());

		}

		@Override
		public void onSuccess(T arg0, HttpHeaders arg1) throws IOException {
			// TODO Auto-generated method stub
			System.out.println("success");
		}
	}


	/**
	 * Gets a {@code long} representing the start date and time of the event
	 * @param event The Event to get the start date and time of
	 * @return A {@code long} representing the start date and time of the event
	 */
	public static long getStartDateTime(Event event) {
		if(isAllDay(event)) {
			return ((DateTime)(event.getStart().get("date"))).getValue();
		} else {
			return event.getStart().getDateTime().getValue();
		}
	}
	
	/**
	 * Gets a {@code long} representing the end date and time of the event
	 * @param event The Event to get the end date and time of
	 * @return A {@code long} representing the end date and time of the event
	 */
	public static long getEndDateTime(Event event) {
		if(isAllDay(event)) {
			return ((DateTime)(event.getEnd().get("date"))).getValue();
		} else {
			return event.getEnd().getDateTime().getValue();
		}
	}

	/**
	 * Checks if an {@code Event} is an all day event 
	 * @param event The {@code Event} to be checked
	 * @return {@code true} if the {@code Event} is an all day event; othewise, {@code false}
	 */
	public static boolean isAllDay(Event event) {
		EventDateTime startEventDateTime = event.getStart();
		DateTime dateTime = (DateTime)(startEventDateTime.get("date"));
		if(dateTime == null) {
			return false;
		} else {
			return true;
		}
	}


	
	
	
	public String hasAccessToken() {
		return credential.getAccessToken();
	}
	
	public String getRefreshToken() {
		return credential.getRefreshToken();
	}

	public String getAuthorizationUrl() {
		return authorizationUrl;
	}

	public void setAuthorizationUrl(String authorizationUrl) {
		this.authorizationUrl = authorizationUrl;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public Date getStartDate() {
		return startDate;
	}

	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}

	public Date getEndDate() {
		return endDate;
	}

	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}
}
