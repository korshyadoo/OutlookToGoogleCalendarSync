package com.korshyadoo.calendar;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;
import java.util.List;

import com.google.gdata.data.calendar.CalendarEventEntry;

public class LogWriter {
	private static final String LOG_LOCATION = "log.txt";													//Location of log.txt
	private static final String SEPARATOR = "-------------------------------------------";					//Separator for log file

	public void writeDeleteDateRangeLog(List<CalendarEventEntry> deleteDateRangeEvents, int minutes, int seconds) throws IOException {
		BufferedWriter log = new BufferedWriter(new FileWriter(new File(LOG_LOCATION), true));
		//Add formatting and record current Date
		Date now = new Date();
		log.write("\n" + SEPARATOR + 
				"\n" + "DELETED the following events at " + now.toString() + 
				"\nACTION TIME: " + minutes + " minutes, " + seconds + " seconds");

		//Write each deleted event to the log
		for(CalendarEventEntry cee : deleteDateRangeEvents) {											//For each deleted appointment
			log.write("\n\n\"" + cee.getTitle().getPlainText() + "\"\n" +
					"Start: " + new Date(cee.getTimes().get(0).getStartTime().getValue()).toString() + "\n" +
					"End: " + new Date(cee.getTimes().get(0).getEndTime().getValue()).toString() + "\n");
			if(cee.getLocations().get(0).getValueString().length() > 0) {								//If the event has a location, write it
				log.write("Location: " + cee.getLocations().get(0).getValueString() + "\n");
			}
			if(cee.getPlainTextContent().length() > 0) {												//If the event has content, write it
				log.write("Content: " + cee.getPlainTextContent() + "\n");
			}
			log.write("Created: " + new Date(cee.getPublished().getValue()).toString());
		}
		log.close();
	}
	
	public void writeSyncLog(int minutes, int seconds, int before, int after) throws IOException {
		//Write log file entry with current time and number of Google events before and after
		BufferedWriter log = new BufferedWriter(new FileWriter(new File(LOG_LOCATION), true));
		Date now = new Date();
		log.write("\n" + SEPARATOR + "\n" +
				"SYNC at " + now.toString() + "\nACTION TIME: " + minutes + " minutes, " + seconds + " seconds" +
				"\n" + "In Google: " + before + " events before, " + after + " after" + "\n");
		log.close();
	}
}
