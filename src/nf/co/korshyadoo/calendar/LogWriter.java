package nf.co.korshyadoo.calendar;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import javax.swing.JOptionPane;

import com.google.api.services.calendar.model.Event;
import com.ibm.icu.util.Calendar;


/**
 * The log file is stored in a String field. Entries in the log file are appended to the String field using the append() method.
 * When all entries are appended and the log is ready to be written to the disk, the flush() method must be called. The LogWriter
 * chooses a file name in the flush method. After the buffer is flushed, that log file can no longer be written to. If flush() is
 * called again, a new file with a different file name will be created.
 * @author korshyadoo
 *
 */
public class LogWriter extends Writer {
	private static final String LOG_LOCATION = "logs/";												//Location of log.txt
	private static final String SEPARATOR = "-------------------------------------------\n";		//Separator for log file

	/**
	 * Indicates the accompanying information is for an event being insert into the Google Calendar
	 */
	public static final int INSERT = 0;															
	/**
	 * Indicates the accompanying information is for an event being deleted from the Google Calendar
	 */
	public static final int DELETE = 1;

	private String buffer = "";

	public LogWriter() {
		//Create the VCards folder if it doesn't exist
		try {
			Files.createDirectories(Paths.get(LOG_LOCATION));
		} catch (IOException e) {
			JOptionPane.showMessageDialog(null, e.getMessage());
		}
	}



	/**
	 * Gets the file name for the log file, modeled as:
	 * 
	 * MM_DD_YYYY-logN.log
	 * 
	 * Where MM is the current month, DD is the current day, YYYY is the current year, and N is the log number.
	 * The log number starts at 1. At the time this method is called, if one or more logs exist with the same
	 * timestamp, the new file name will increment the log number by 1.
	 * @return The name for the new log file to be created.
	 * @throws IOException I/O exception when creating new file
	 */
	private String getFileName() throws IOException {
		Date date = new Date();
		java.util.Calendar cal = new GregorianCalendar();
		cal.setTime(date);

		String month = twoDigit((cal.get(Calendar.MONTH) + 1) + "");
		String dayOfMonth = twoDigit(cal.get(Calendar.DATE) + "");

		String fileNamePrefix = month + "_" + dayOfMonth + "_" + 
				cal.get(Calendar.YEAR) + "-log";

		//If the file name exists, keep incrementing the log number until an available name is found
		int logNumber = 0;
		String fullFileName;
		File file;
		do {
			logNumber++;
			fullFileName = LOG_LOCATION + fileNamePrefix + logNumber + ".log";
			file = new File(fullFileName);
		}while(file.exists());
		file.createNewFile();

		return fullFileName;
	}

	public void writeDeleteDateRangeLog(List<Event> deleteDateRangeEvents, int minutes, int seconds) throws IOException {
		BufferedWriter log = new BufferedWriter(new FileWriter(new File(LOG_LOCATION), true));
		//Add formatting and record current Date
		Date now = new Date();
		log.write("\n" + SEPARATOR + 
				"\n" + "DELETED the following events at " + now.toString() + 
				"\nACTION TIME: " + minutes + " minutes, " + seconds + " seconds");

		//Write each deleted event to the log
		for(Event event : deleteDateRangeEvents) {											//For each deleted appointment
			//Get the start time
			long eventStartLong = GoogleCalendarV3Utility.getStartDateTime(event);

			//Get the end time
			long eventEndLong = GoogleCalendarV3Utility.getEndDateTime(event);

			log.write("\n\n\"" + event.getSummary() + "\"\n" +
					"Start: " + new Date(eventStartLong).toString() + "\n" +
					"End: " + new Date(eventEndLong).toString() + "\n");
			if(event.getLocation() != null) {								//If the event has a location, write it
				log.write("Location: " + event.getLocation() + "\n");
			}
			if(event.getDescription() != null) {												//If the event has content, write it
				log.write("Content: " + event.getDescription() + "\n");
			}
			log.write("Created: " + new Date(event.getCreated().getValue()).toString());
		}
		log.close();
	}

	/**
	 * Append sync details to the beginning of the buffer, write the buffer to a new log file, and clear the buffer.
	 * @return {@code true} if successful; otherwise, {@code false}
	 * @throws IOException 
	 */
	public boolean flush(int minutes, int seconds, int before, int after) throws IOException {
		if(buffer.isEmpty()) {
			buffer = "No items to sync";
		}
		Date now = new Date();
		String lastSync = ProgramLauncher.getDataIo().getField(ProgramLauncher.getCalendarUtility().getUsername(), Fields.LAST_SYNC_TIME_DATETIME);
		String syncInfo = "SYNC at " + now.toString() + "\n" +
				(lastSync != null ? lastSync + "\n" : "No previous sync\n") +
				"\nACTION TIME: " + minutes + " minutes, " + seconds + " seconds" +
				"\n" + "In Google: " + before + " events before, " + after + " after" + "\n" + SEPARATOR;
		buffer = syncInfo + buffer;						//Append the syncInfo to the beginning of the log buffer
		File file = new File(getFileName());
		PrintWriter pw = new PrintWriter(file);
		pw.write(buffer);								//Write the buffer to the file
		pw.close();										//Close the PrintWriter
		buffer = "";									//Clear the buffer
		return true;
	}

	/**
	 * Append the details of each {@code Event} in the passed {@code List} to the buffer. 
	 * @param events The list of {@code Event}s to append to the log buffer
	 * @param action One of the {@code LogWriter} action fields indicating what action is being taken on the {@code Event}s. 
	 * i.e. if an event is being deleted from the Google calendar, pass LogWriter.DELETE and the log will reflect that information
	 * @return {@code ture} if the append was successful; otherwise, {@code false}
	 */
	public boolean append (List<Event> events, int action) {
		java.util.Calendar startCal = new GregorianCalendar();
		java.util.Calendar endCal = new GregorianCalendar();

		buffer += (action == 0 ? "INSERT:\n" : "DELETE:\n");
		
		//For each event, add the event's details
		for(Event e : events) {
			//Add the title
			buffer += (e.getSummary() + "\n");

			//Add the time frame as MM/DD/YYYY 12:00 - 12:30
			startCal.setTimeInMillis(GoogleCalendarV3Utility.getStartDateTime(e));
			endCal.setTimeInMillis(GoogleCalendarV3Utility.getEndDateTime(e));
			buffer += ((startCal.get(Calendar.MONTH) + 1) + "/" + startCal.get(Calendar.DATE) + "/" +
					startCal.get(Calendar.YEAR) + " " + startCal.get(Calendar.HOUR_OF_DAY) + ":" +
					startCal.get(Calendar.MINUTE) + " - " +
					endCal.get(Calendar.HOUR_OF_DAY) + ":" + endCal.get(Calendar.MINUTE) + "\n");

			// Add the location
			buffer += ("Location: " + e.getLocation() + "\n");

			//Add the content
			buffer += ("Content: " + e.getDescription() + "\n");
			
			//Add created time
			buffer += ("Created: " + new Date(e.getCreated().getValue()).toString() + "\n");
			
			buffer += SEPARATOR;
		}

		return true;
	}

	public void writeCurrentDSTState() {
		boolean inDST = new GregorianCalendar().getTimeZone().inDaylightTime(new Date());
		try(BufferedWriter log = new BufferedWriter(new FileWriter(new File(LOG_LOCATION + "DST.log"), true))) {
			log.write("\n" + SEPARATOR + "\n" +
					"We are currently in DST: " + inDST + "\n");
		} catch (IOException e) {
			System.out.println("IOE in log writer");
			e.printStackTrace();
		}
	}




}
