package com.korshyadoo.calendar;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.TimeZone;

import javax.swing.JOptionPane;

import com.google.gdata.data.DateTime;
import com.google.gdata.data.PlainTextConstruct;
import com.google.gdata.data.calendar.CalendarEventEntry;
import com.google.gdata.data.extensions.When;
import com.google.gdata.data.extensions.Where;
import com.pff.PSTAppointment;
import com.pff.PSTException;
import com.pff.PSTFile;
import com.pff.PSTFolder;

/**
 * 
 * @author korshyadoo
 *
 */
public class PSTInterface {
	public static final String PST_LOCATION_DEFAULT_XP = "C:\\Documents and Settings\\" + 		//The default location for the .pst file in Windows XP
			System.getProperty("user.name") +
			"\\Local Settings\\Application Data\\Microsoft\\Outlook\\Outlook.pst";
	public static final String PST_LOCATION_DEFAULT_VISTA = "C:\\Users\\" +						//The default location for the .pst file in Windows Vista / 7
			System.getProperty("user.name") +
			"\\AppData\\Local\\Microsoft\\Outlook\\Outlook.pst";
	public static final String PST_LOCATION_DEFAULT_VISTA2 = "C:\\Users\\" +					//An alternate location for the .pst file in Windows Vista / 7
			System.getProperty("user.name") +
			"\\Documents\\Outlook Files\\Outlook.pst";
	private String pstLocation;																	//Location of the .pst file used by Outlook

	//Constructors
	public PSTInterface() {
		pstLocation = locatePST();
	}
	public PSTInterface(String pstLocation) {
		this.pstLocation = pstLocation;
	}

	/**
	 * Attempts to locate the .pst in the default locations
	 * @author korshyadoo
	 * @return String containing the location of the .pst file. Returns null if no file was found.
	 */
	public String locatePST() {
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
	 * Check if a CalendarEventEntry is meaningfully equivalent to a PSTAppointment.
	 * PSTAppointment.getBody() adds a line feed, '\r' (ASCII 13), and a 
	 * carriage return, '\n' (ASCII 10), to the end of the content, even when 
	 * the content is empty. Also, any carriage return in the content is 
	 * preceded by a line feed.
	 * Remove all ASCII 13 from pst
	 * @author korshyadoo
	 * @param cee CalendarEventEntry to be compared.
	 * @param pst PSTAppointment to be compared.
	 * @return true if they are meaningfully equivalent, otherwise false.
	 */
	public boolean compareCEEToPST(CalendarEventEntry cee, PSTAppointment pst) {
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
			CalendarEventEntry convertedPST = convertPSTToCEE(pst);
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
		if(new File(pstLocation).canRead()) {
			pstFile = new PSTFile(pstLocation);
		} else {
			JOptionPane.showMessageDialog(null,"The user does not have permission to read the Outlook calendar");
			System.exit(0);
		}

		List<PSTFolder> rootSubs = pstFile.getRootFolder().getSubFolders();			//getSubFolers returns a java.util.Vector<PSTFolder>
		return rootSubs.get(0).getSubFolders();      
	}

	/**
	 * Converts a PSTAppointmnet to a CalendarEventEntry.
	 * Converts the start time, end time, title, and locataion.
	 * Needs recurrence added
	 * @author korshyadoo
	 * @param app The PSTAppointment object to be converted to a CalendarEventEntry
	 * @return 
	 */
	public CalendarEventEntry convertPSTToCEE(PSTAppointment app) {
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

		return entry;
	}
	
	/**
	 * Used to determine if a .pst file was located in one of the default locations.
	 * The location of the .pst file may also have been passed to the constructor.
	 * @return true if a .pst file has been found, false if one has not
	 */
	public boolean foundPST() {
		return (pstLocation != null);
	}
	
	public String getPSTLocation() {
		return pstLocation;
	}
}
