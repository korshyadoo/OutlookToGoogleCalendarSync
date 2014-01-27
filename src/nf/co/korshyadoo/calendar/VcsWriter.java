package nf.co.korshyadoo.calendar;

import java.awt.EventQueue;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import javax.swing.JOptionPane;

import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;
import com.ibm.icu.util.Calendar;

/**
 * This class takes a Google calendar Event and writes it to a VCard file
 * so that it can be imported into Microsoft Outlook. The VCard file is
 * created when VscWriter.create() is called.
 * @author korshyadoo
 *
 */
public abstract class VcsWriter extends Writer {
	private static final String FILE_LOCATION = "VCards/";
	private static final String PREFIX = "BEGIN:VCALENDAR\n" +
			"PRODID:-//Microsoft Corporation//Outlook 14.0 MIMEDIR//EN\n" +
			"VERSION:1.0\n" +
			"BEGIN:VEVENT\n";
	private static final String SUFFIX = "PRIORITY:3\n" +
			"END:VEVENT\n" +
			"END:VCALENDAR";
	private static final int START_TIME = 0;
	private static final int END_TIME = 1;
	
	static {
		//Create the VCards folder if it doesn't exist
		try {
			Files.createDirectories(Paths.get(FILE_LOCATION));
		} catch (IOException e) {
			JOptionPane.showMessageDialog(null, e.getMessage());
		}
	}

	/**
	 * Create a separate VCard file for each {@code Event} in the {@code List}
	 * @param events The {@code List} of {@code Event}s to create VCards from
	 * @return {@code true} if successful; otherwise, {@code false}
	 */
	public static boolean create(List<Event> events) {
		boolean result = true;
		for(Event e : events) {
			if(!create(e))
				result = false;
		}
		return result;
	}

	private static String createString(Event event) {
		java.util.Calendar startCal = getCalendar(event.getStart());
		java.util.Calendar endCal = getCalendar(event.getEnd());

		String dtStart = formatTime(startCal.get(Calendar.YEAR), startCal.get(Calendar.MONTH), 
				startCal.get(Calendar.DATE), startCal.get(Calendar.HOUR_OF_DAY), startCal.get(Calendar.MINUTE), START_TIME);
		String dtEnd = formatTime(endCal.get(Calendar.YEAR), endCal.get(Calendar.MONTH), 
				endCal.get(Calendar.DATE), endCal.get(Calendar.HOUR_OF_DAY), endCal.get(Calendar.MINUTE), END_TIME);

		//Format the title
		String title = "SUMMARY:" + event.getSummary();

		//Format the location
		String location = "LOCATION:";
		if(event.getLocation() != null) {
			location += event.getLocation();
		}

		//Format the content
		String content;
		if(event.getDescription() == null) {
			content = "DESCRIPTION;ENCODING=QUOTED-PRINTABLE:=0D=0A";
		} else {
			content = "DESCRIPTION;ENCODING=QUOTED-PRINTABLE:" + event.getDescription() + "=0D=0A";
		}

		//Put all the components together to create the properly formatted text of the VCard
		return PREFIX + dtStart + "\n" + dtEnd + "\n" + location + "\n" +
		content + "\n" + title + "\n" + SUFFIX;
	}

	
	/**
	 * Create a VCard file for an appointment in a specified colour category
	 * @param event The {@code Event} to create the VCard from
	 * @param category The colour category that the {@code Event} is in
	 * @return {@code true} if successful; otherwise, {@code false}
	 */
	public static boolean create(Event event, Colours category) {
		//Create the filename as: MONTH_DAY_YEAR_{first 20 characters of title}
		java.util.Calendar startCal = getCalendar(event.getStart());
		String fileName = (startCal.get(Calendar.MONTH) + 1) + "_" + startCal.get(Calendar.DATE) + "_" + 
				startCal.get(Calendar.YEAR) + "_" + 
				(event.getSummary().length() > 20 ? event.getSummary().substring(0, 20) : event.getSummary()) +
				".vcs";

		String vCard = createString(event);
		
		//Insert the colour category into the VCard text
		int index = vCard.indexOf("DESCRIPTION;ENCODING=QUOTED-PRINTABLE:");
		String start = vCard.substring(0, index);
		String middle = "CATEGORIES:" + category.name() + " Category\n";
		String end = vCard.substring(index, vCard.length());
		vCard = start + middle + end;
		
		return write(fileName, vCard);
	}

	/**
	 * Create a VCard file from the passed {@code Event}
	 * @param event The calendar event being written to the VCard file
	 * @return {@code false} if unsuccessful (possible IO Exception when creating VCard file); otherwise, {@code true}
	 */
	public static boolean create(Event event) {
		//Create the filename as: MONTH_DAY_YEAR_{first 20 characters of title}
		java.util.Calendar startCal = getCalendar(event.getStart());
		String fileName = (startCal.get(Calendar.MONTH) + 1) + "_" + startCal.get(Calendar.DATE) + "_" + 
				startCal.get(Calendar.YEAR) + "_" + 
				(event.getSummary().length() > 20 ? event.getSummary().substring(0, 20) : event.getSummary()) +
				".vcs";

		String vCard = createString(event);
		return write(fileName, vCard);
	}

	/**
	 * Write the VCard information to disk
	 * @param fileName The name of the VCard file to be written
	 * @param vCard The information to put in the VCard file being written
	 * @return {@code true} if successful; otherwise, {@code false}
	 */
	private static boolean write(String fileName, String vCard) {
		File vcsFile = getFile(fileName);						//Retrieve a File object for the VCard. Creates a backup name if the file already exists
		if(vcsFile != null) {
			try(PrintWriter pw = new PrintWriter(vcsFile)) {
				pw.print(vCard);
			} catch (FileNotFoundException e) {
				//The file is already checked to exist so this shouldn't be possible
				e.printStackTrace();
			} 
		} else {
			return false;
		}

		return true;
	}

	/**
	 * Creates a {@code File} object with the given fileName. If the passed file name contains
	 * illegal characters, they are removed. If the file already exists, it creates a backup
	 * with ".bak" appended. If the backup already exists, it appends a number.
	 * @param fileName The name of the VCard file being created
	 * @return A {@code File} object with the given fileName. {@code null} if there was an I/O error
	 */
	private static File getFile(String fileName) {
		fileName = fileName.replaceAll("[\\\\/:*?\"<>|]", "");			//Removes illegal characters from the file name
		String fullPath = FILE_LOCATION + fileName;
		File result = new File(fullPath);
		if(result.exists()) {
			//The file already exists. Rename it to .bak
			File bakFile = new File(fullPath + ".bak");
			if(bakFile.exists()) {
				//The bak file already exists. Find an available name
				int bakNumber = 1;
				while(new File(fullPath + ".bak" + bakNumber).exists()) {
					bakNumber++;
				}
				bakFile = new File(fullPath + ".bak" + bakNumber);
			}

			//Rename the exists VCard file (result) to the available backup name (bakFile)
			result.renameTo(bakFile);
			result = new File(fullPath);
		} else {
			try {
				result.createNewFile();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				try {
					EventQueue.invokeAndWait(new Runnable() {

						@Override
						public void run() {
							JOptionPane.showMessageDialog(null, "I/O Exception trying to create VCard file");
						}
					});
				} catch (InvocationTargetException | InterruptedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				return null;
			}
		}
		return result;
	}

	/**
	 * Retrieve a java.util.Calendar set with the time from the passed EventDateTime
	 * @param eventDateTime The time to set the Calendar to
	 * @return A java.util.Calendar set with the time from the passed EventDateTime
	 */
	private static java.util.Calendar getCalendar(EventDateTime eventDateTime) {
		java.util.Calendar cal = new GregorianCalendar();
		DateTime dateTime = (DateTime)(eventDateTime.get("date"));
		long eventStartLong;
		if(dateTime == null) {
			eventStartLong = eventDateTime.getDateTime().getValue();
		} else {
			eventStartLong = dateTime.getValue();
		}
		cal.setTime(new Date(eventStartLong));
		return cal;
	}

	/**
	 * Fortmats the end date and time into the format needed for the VCard file. Also converts the time
	 * to GMT, which is required for the VCard.
	 * @param year
	 * @param month
	 * @param date
	 * @param hourOfDay
	 * @param minute
	 * @return The end date and time properly formated for insertion into a VCard
	 */
	private static String formatTime(int year, int month, int date, int hourOfDay, int minute, int startOrEnd) {
		//Load a java.util.Calendar with the desired date
		java.util.Calendar cal = new GregorianCalendar();
		cal.set(year, month, date, hourOfDay, minute);

		int offset = cal.getTimeZone().getOffset(cal.getTime().getTime());					//Get the timezone offset
		cal.add(Calendar.MILLISECOND, (-1 * offset));										//Adjust the Calendar to the offset. UTC = local time - offset

		//Adjust the month, date, hourOfDay, and minute so that they have the correct number of digets
		String formattedMonth = twoDigit((cal.get(Calendar.MONTH) + 1) + "");				//Month is 0-based, so 1 must be added			
		String formattedDate = twoDigit(cal.get(Calendar.DATE) + "");
		String formattedHourOfDay = twoDigit(cal.get(Calendar.HOUR_OF_DAY) + "");
		String formattedMinute = twoDigit(cal.get(Calendar.MINUTE) + "");

		String formattedSecond = "00";
		String prefix = "";
		switch(startOrEnd) {
		case 0:
			prefix = "DTSTART:";
			break;
		case 1:
			prefix = "DTEND:";
			break;
		}

		return (prefix + cal.get(Calendar.YEAR) + formattedMonth + 
				formattedDate + "T" + formattedHourOfDay + formattedMinute + formattedSecond + "Z");
	}

}
