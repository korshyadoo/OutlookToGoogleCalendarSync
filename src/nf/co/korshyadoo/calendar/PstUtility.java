//PST responsibilities:
// searching for PST file
// comparing PST to CEE
// converting PST to CEE
// housing default .pst locations
// returning the outlook folders


package nf.co.korshyadoo.calendar;

import java.awt.EventQueue;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.TimeZone;

import javax.swing.JOptionPane;

import nf.co.korshyadoo.dataIo.DataIo;

import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;
import com.pff.PSTException;
import com.pff.PSTAppointment;
import com.pff.PSTFile;
import com.pff.PSTFolder;



public class PstUtility {
	private static PstUtility pstUtility;
	private String pstLocation;
	private static boolean searching = true;
	
	/**
	 * The default location for the .pst file in Windows XP
	 */
	public static final String PST_LOCATION_DEFAULT_XP = "C:\\Documents and Settings\\" + 
			System.getProperty("user.name") +
			"\\Local Settings\\Application Data\\Microsoft\\Outlook\\Outlook.pst";
	
	/**
	 * The default location for the .pst file in Windows Vista / 7
	 */
	public static final String PST_LOCATION_DEFAULT_VISTA = "C:\\Users\\" +	
			System.getProperty("user.name") +
			"\\AppData\\Local\\Microsoft\\Outlook\\Outlook.pst";
	
	/**
	 * An alternate location for the .pst file in Windows Vista / 7
	 */
	public static final String PST_LOCATION_DEFAULT_VISTA2 = "C:\\Users\\" +
			System.getProperty("user.name") +
			"\\Documents\\Outlook Files\\Outlook.pst";
	
	/**
	 * Performs a search for the .pst file
	 */
	private PstUtility() {
		pstLocation = defaultPstSearch();
		if(pstLocation == null) {
			searching = true;
			//Launch PstSearchFrame
			EventQueue.invokeLater(new Runnable() {
				@Override
				public void run() {
					new PstSearchFrame().setLocationRelativeTo(null);
				}
			});
		} else {
			searching = false;
		}
	}

	/**
	 * Sets the pstLocation to the passed value. Does not perform a search for the .pst file
	 * @param pstLocation
	 */
	private PstUtility(String pstLocation) {
		this.pstLocation = pstLocation;
		searching = false;
	}
	
	/**
	 * Attempts to locate the .pst in the default locations
	 * @return A String containing the location of the .pst file. Returns null if no file was found.
	 */
	private String defaultPstSearch() {
		String location;
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
				} else {
					location = null;
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
				} else {
					location = null;
				}
			}
		}
		return location;
	}
	
	/**
	 * Formats the content of the {@code PSTAppointment} so that it doesn't contain any
	 * superfluous carriage returns or new line characters.
	 * @param pst The {@code PSTAppointment} to have its content formated
	 * @return The 
	 */
	private String formatContent(String pst) {
		//Most of the time, "\r\n" is appended to the end of the content by the libpst API, so it must be removed. 
		if(pst.endsWith("\r\n")) {
			pst = pst.substring(0, pst.length() - 2);
		}
		
		//For every "\n" character found, if it is preceded by a "\r" then remove the "\r"
		return pst.replaceAll("\\\r\\\n", "\n");
	}
	
	/**
	 * If the Outlook appointment is scheduled for a time that is inside Daylight Savings Time (DST),
	 * and the DST state of the time the appointment was created and the current time the sync
	 * is taking place are opposite (i.e. appointment created during DST, sync occuring outside DST, OR
	 * appointment created outside DST, sync occuring during DST) the start and end times of the PSTAppointment
	 * will be one hour behind what the scheduled times should be.
	 * This method checks if the start time needs to be adjusted to correct for DST.
	 * @param app The {@code PSTAppointment} to check
	 * @return {@code true} if the start time needs to be moved forward an hour to correct it; otherise, {@code false}
	 */
	private boolean requiresDstAdjustment(PSTAppointment app) {
		boolean result = false;
		
		//Is the scheduled start time of the appointment inside DST?
		Date startTime = app.getStartTime();
		TimeZone tz = new GregorianCalendar().getTimeZone();
		boolean scheduledInDST = tz.inDaylightTime(startTime);
		
		if(scheduledInDST) {
			//The appointment is scheduled in DST
			
			//Check if the current DST state and created DST state are opposite
			boolean currentInDst = tz.inDaylightTime(new Date());		//true if the current time is inside DST
			Date createdTime = app.getCreationTime();
			boolean createdInDst = tz.inDaylightTime(createdTime);
			if(currentInDst != createdInDst) {
				//The start and end times need to be adjusted
				result = true;
			}
		}
		
		return result;
	}
	
	
	/**
	 * Returns a reference to the singleton PffPstUtility object. If the class
	 * has not yet been instantiated, a search is made for the .pst file used
	 * by Outlook in the default locations. If it was not found in the default
	 * locations, a PstSearchFrame is run to perform a full search for the file.
	 * When the full search has completed, getInstance(String pstLocation) is
	 * called to set the newly found pstLocation.
	 * @return A reference to the singleton PffPstUtility object.
	 */
	public static PstUtility getInstance() {
		if(pstUtility == null) {
			pstUtility = new PstUtility();
		}
		
		return pstUtility;
	}
	
	/**
	 * Returns a reference to the singleton PffPstUtility object and updates 
	 * the pstLocation to the passed String.
	 * @param pstLocation The location of the .pst file used by Outlook
	 * @return A reference to the singleton PffPstUtility object.
	 */
	public static PstUtility getInstance(String pstLocation) {
		if(pstUtility == null) {
			pstUtility = new PstUtility(pstLocation);
		} else {
			pstUtility.pstLocation = pstLocation;
		}
		
		return pstUtility;
	}

	/**
	 * Indicates if the object knows the location of the Outlook.pst file.
	 * @return {@code true} if the pstLocation field is not null; otherwise, {@code false}
	 */
	public boolean foundPst() {
		return pstLocation != null;
	}

	public void setPstLocation(String pstLocation) {
		this.pstLocation = pstLocation;
	}
	
	public String getPstLocation() {
		return pstLocation;
	}
	
	/**
	 * Sets the searching field which indicates if the object is currently searching for the Outlook.pst file
	 * @param searching
	 */
	public static void setSearching(boolean searching) {
		PstUtility.searching = searching;
	}
	
	/**
	 * Indicates whether or not a search for the Outlook.pst file is currently underway
	 * @return {@code ture} if the Outlook.pst file is currently being searched for; otherwise, {@code false}
	 */
	public static boolean isSearching() {
		return searching;
	}
	
	/**
	 * Locates the folder within the file structure of the .pst file that houses the Outlook folders
	 * (e.g. inbox, outbox, trash, calendar, etc.)
	 * @author korshyadoo
	 * @return A List<PSTFolder> containing references to each of the Outlook folders, including the calendar folder
	 * @throws FileNotFoundException
	 * @throws PSTException
	 * @throws IOException I/O error reading .pst
	 */
	public List<PSTFolder> getOutlookFolders() 
			throws FileNotFoundException, PSTException, IOException {
		//If the user has read permission for the .pst file, then create the PSTFile object
		//If not, display warning and exit
		PSTFile pstFile = null;
		File file = new File(pstLocation);
		if(file.exists()) {
			if(file.canRead()) {
				pstFile = new PSTFile(pstLocation);
			} else {
				JOptionPane.showMessageDialog(null,"The user does not have permission to read the Outlook calendar");
				System.exit(0);
			}
		} else {
			//Outlook.pst is not in the location specified by settings.ini. Delete pstLocation from the data source and exit so that it can be searched for when the application runs again
			DataIo dataIo = ProgramLauncher.getDataIo();
			dataIo.deletePstLocation();
			JOptionPane.showMessageDialog(null,"Outlook.pst has gone missing. Please re-launch the program.");
			System.exit(0);
		} 

		List<PSTFolder> rootSubs = pstFile.getRootFolder().getSubFolders();			//getSubFolers returns a java.util.Vector<PSTFolder>
		
		//In Outlook 2003, the sub folder containing the Outlook folders is called "Top of Personal Folders"
		//In Outlook 2010, the sub folder containing the Outlook folders is called "Top of Outlook data file"
		//Locate the Outlook folders
		int outlookFoldersNumber = 0;
		for(int x = 0; x < rootSubs.size(); x++) {
			if(rootSubs.get(x).getDisplayName().startsWith("Top of ")) {
				outlookFoldersNumber = x;
				break;
			}
		}
		return rootSubs.get(outlookFoldersNumber).getSubFolders();      
	}
	
	/**
	 * Checks if a Google Event is meaningfully equivalent to a PSTAppointment
	 * @param pst The PSTAppointment (from the Outlook calendar) to compare
	 * @param event The Google Event to compare
	 * @return {@code true} if they are meaningfully equivalent; otherwise, {@code false}
	 */
	public boolean comparePstToEvent(PSTAppointment pst, Event event) {
//		PSTAppointment.getBody() adds a carriage return, '\r' (ASCII 13), and a 
//		 * line feed, '\n' (ASCII 10), to the end of the content, even when 
//		 * the content is empty. Also, any line feed in the content is 
//		 * preceded by a carriage return.
//		 * Remove all ASCII 13 from pst
		
		String eventTitle = event.getSummary();
		//If there is no title in the Google Event, eventTitle will be null 
		if(eventTitle == null) {
			eventTitle = "";
		}
		
		String pstTitle = pst.getSubject();
		
		//If the title of the pst appointment and the Google Event match, then convert the pst to an Event and compare the objects
		if(eventTitle.equals(pstTitle)) {
			//Get the location for the Google event. If it's null, make it empty
			String eventLocation = event.getLocation();
			if(eventLocation == null) {
				eventLocation = "";
			}
			
			//Retrieve the content from the Google event. If it's null, make it empty instead
			String eventContent = event.getDescription();
			if(eventContent == null) {
				eventContent = "";
			}
			
			//Retrieve the pst content with proper formatting (excess line feeds and carriage returns removed)
			String pstContentString = formatContent(pst.getBody());
			
			Event convertedPST = convertPstToEvent(pst);
			
//			String colour = null;
//			try {
//				String[] colours = pst.getColorCategories();
//				if(colours.length != 0) {
//					colour = colours[0];
//				}
//			} catch (PSTException e) {
//				e.printStackTrace();
//			}
//			
//			System.out.println(colour);
			
			//Retrieve colour category ID
			int colourID = 0;
			try {
				if(pst.getColorCategories().length != 0) {
					switch(pst.getColorCategories()[0]) {
					case "Blue Category":
						colourID = 9;
						break;
					case "Green Category":
						colourID = 10;
						break;
					case "Orange Category":
						colourID = 6;
						break;
					case "Purple Category":
						colourID = 3;
						break;
					case "Red Category":
						colourID = 4;
						break;
					case "Yellow Category":
						colourID = 5;
						break;
					}
				}
			} catch (PSTException e) {
			}
			
			return GoogleCalendarV3Utility.getStartDateTime(event) == GoogleCalendarV3Utility.getStartDateTime(convertedPST) &&
					GoogleCalendarV3Utility.getEndDateTime(event) == GoogleCalendarV3Utility.getEndDateTime(convertedPST) &&
					eventLocation.equals(convertedPST.getLocation()) && 
					pstContentString.equals(eventContent) &&
					((event.getColorId() == null ? 0 : Integer.parseInt(event.getColorId())) == colourID);
		} else {
			return false;
		}
	}
	
	
	
	
	
	public Event convertPstToEvent(PSTAppointment app) {
		try {
			String[] cats = app.getColorCategories();
			if(cats.length != 0) {
				
			}
		} catch (PSTException e) {
		}
			
		
		
		Date startDate = app.getStartTime();
		Date endDate = app.getEndTime();
		
		//Check for weird daylight saving bias
		//Further testing in November 2013 (outside DST) has shown that a time mismatch may only be occurring if
		//the sync occurs inside DST.
		TimeZone tz = new GregorianCalendar().getTimeZone();
		boolean adjustForDST;
		if(tz.inDaylightTime(new Date())) {
			adjustForDST = requiresDstAdjustment(app);
		} else {
			adjustForDST = false;
		}
		
		if(adjustForDST) {
			//Move the start and end times ahead 1 hour
			Calendar startCal = new GregorianCalendar();
			startCal.setTime(startDate);
			startCal.add(Calendar.HOUR_OF_DAY, -1);				//Subtract one hour to calendar
			startDate = startCal.getTime();
			Calendar endCal = new GregorianCalendar();
			endCal.setTime(endDate);
			endCal.add(Calendar.HOUR_OF_DAY, -1);				//Subtract one hour to calendar
			endDate = endCal.getTime();
		}

		//Create DateTime objects for start and end
		DateTime startTime = new DateTime(startDate);
		DateTime endTime = new DateTime(endDate);
		
		//Create new Event and set the times, title, location, and content
		Event event = new Event();
		event.setStart(new EventDateTime().setDateTime(startTime));
		event.setEnd(new EventDateTime().setDateTime(endTime));
		event.setSummary(app.getSubject());
		event.setLocation(app.getLocation());
		event.setDescription(formatContent(app.getBody()));
		event.setCreated(new DateTime(app.getCreationTime()));
		
		//Set the colour category
		String[] cats = null;
		try {
			cats = app.getColorCategories();
		} catch (PSTException e) {
		}
		if(cats != null && cats.length != 0) {
			switch(cats[0]) {
			case "Blue Category":
				event.setColorId("9");
				break;
			case "Green Category":
				event.setColorId("10");
				break;
			case "Orange Category":
				event.setColorId("6");
				break;
			case "Purple Category":
				event.setColorId("3");
				break;
			case "Red Category":
				event.setColorId("4");
				break;
			case "Yellow Category":
				event.setColorId("5");
				break;
			}
		}
		
		return event;
	}



}