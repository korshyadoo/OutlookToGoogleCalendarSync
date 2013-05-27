package com.korshyadoo.calendar;



class debugClass {
//	/**
//	 * Searches for an appointment at a specific start time. 
//	 * CalendarQuery.MaximumStartTime needs to be 2 seconds
//	 * greater than CalendarQuery.MinimumStartTime. 
//	 * Constructing the MaximumStartTime DateTime using a long instead of a Date
//	 * does not properly set the MaximumStartTime for some reason
//	 * @author Korshyadoo
//	 * @param service The calendar service
//	 * @param query The start time to search for
//	 * @throws ServiceException
//	 * @throws IOException 
//	 */
//	public static boolean timeQuery(PSTAppointment query)
//			throws ServiceException, IOException {
//		//Retrive the query feed by searching by the start time
//		CalendarQuery myQuery = new CalendarQuery(eventFeedUrl);
//		Date date = query.getStartTime();
//		DateTime start = new DateTime(date, TimeZone.getDefault());
//		myQuery.setMinimumStartTime(start);
//		Date endDate = new Date(start.getValue());
//		Calendar endCal = new GregorianCalendar();
//		endCal.setTime(endDate);
//		endCal.add(Calendar.SECOND, 2);
//		DateTime end = new DateTime(endCal.getTime(), TimeZone.getDefault());
//		myQuery.setMaximumStartTime(end);
//		CalendarEventFeed resultFeed = myService.query(myQuery, CalendarEventFeed.class);
//
//		//DEBUG OUTPUT
//		//        System.out.println("start = " + start.toString());
//		//        System.out.println("MinimumStartTime = " + myQuery.getMinimumStartTime());
//		//        System.out.println("end = " + end.toStringRfc822());
//		//        System.out.println("MaximumStartTime = " + myQuery.getMaximumStartTime());      
//
//		//Compare the entries in the result feed to find one equivalent to query
//		int page = 1;
//		do {
//			for (int i = 0; i < resultFeed.getEntries().size(); i++) {          //For each entry in the resultFeed
//				CalendarEventEntry entry = resultFeed.getEntries().get(i);
//				System.out.println("Found timeQuery:");
//				System.out.println("Start: " + new Date(entry.getTimes().get(0).getStartTime().getValue()).toString() +
//						" / End: " + new Date(entry.getTimes().get(0).getEndTime().getValue()).toString() +
//						" / Title: " + entry.getTitle().getPlainText() +
//						" / Location: " + entry.getLocations().get(0).getValueString() +
//						" / Content: " + entry.getPlainTextContent());
//				if(compareCEEToPST(entry, query)) {                             //If the entry is equivalent to the query PST
//					return true;                                                //return true
//				}
//			}            
//			if(resultFeed.getNextLink() != null) {                              //If there's another page of results in the feed, get it
//				resultFeed = myService.getFeed(new URL(resultFeed.getNextLink().getHref()), CalendarEventFeed.class);
//			} else {
//				resultFeed = null;
//			}
//			page++;
//		} while(resultFeed != null);
//
//		//No match found
//		return false;
//	}
//
//	/**
//	 * Finds a specific appointment on the Google calendar and outputs its details.
//	 * For debugging
//	 * @return
//	 * @throws ServiceException
//	 * @throws IOException
//	 */
//	public static boolean timeQuery() throws ServiceException, IOException {
//		//Set up the resultfeed
//		CalendarQuery myQuery = new CalendarQuery(eventFeedUrl);
//		Calendar startCal = new GregorianCalendar();
//		startCal.set(2013, 3, 7, 9, 0);
//		Date startDate = startCal.getTime();
//		DateTime start = new DateTime(startDate, TimeZone.getDefault());
//		myQuery.setMinimumStartTime(start);
//		Calendar endCal = new GregorianCalendar();
//		endCal.set(2013, 3, 14, 21, 0);
//		Date endDate = endCal.getTime();
//		DateTime end = new DateTime(endCal.getTime(), TimeZone.getDefault());
//		myQuery.setMaximumStartTime(end);
//		CalendarEventFeed resultFeed = myService.query(myQuery, CalendarEventFeed.class);
//
//		//Retrieve the results
//		int page = 1;
//		do {
//			for (int i = 0; i < resultFeed.getEntries().size(); i++) {          //For each entry in the resultFeed
//				CalendarEventEntry entry = resultFeed.getEntries().get(i);
//				System.out.println("Start: " + new Date(entry.getTimes().get(0).getStartTime().getValue()).toString() +
//						" / End: " + new Date(entry.getTimes().get(0).getEndTime().getValue()).toString() +
//						" / Title: " + entry.getTitle().getPlainText() +
//						" / Location: " + entry.getLocations().get(0).getValueString() +
//						" / Content: " + entry.getPlainTextContent());
//				System.out.println("Created: " + new Date(entry.getPublished().getValue()).toString() + "\n");
//			}            
//			if(resultFeed.getNextLink() != null) {                              //If there's another page of results in the feed, get it
//				resultFeed = myService.getFeed(new URL(resultFeed.getNextLink().getHref()), CalendarEventFeed.class);
//			} else {
//				resultFeed = null;
//			}
//			page++;
//		} while(resultFeed != null);
//
//		return true;
//	}
	
	
	//FROM MainFrame.SyncWorker.doInBackground()
	//->DEBUG                   
	//                        Calendar compare = new GregorianCalendar();
	//                        compare.set(2013, 2, 10);
	//                        if(appointment.getCreationTime().compareTo(compare.getTime()) < 0 &&
	//                        		appointment.getStartTime().compareTo(compare.getTime()) > 0) {				//If the appointment was created before march 10 and start time is after march 10
	//                        	System.out.println("**** " + appointment.getSubject() + " " + appointment.getStartTime().toString() + 
	//                        			"created " + appointment.getCreationTime() +
	//                        			" last mod " + appointment.getLastModificationTime() +
	//                        			" start time zone daylight bias" + appointment.getStartTimeZone().getDaylightBias());
	//                        }
	//                        if(appointment.getSubject().equals("Groceries") || 
	//                        		appointment.getSubject().equals("IT Job Fair")) {
	//                        	System.out.println("**** " + appointment.getSubject() + " " + appointment.getStartTime().toString() + 
	//                        			"created " + appointment.getCreationTime() +
	//                        			" last mod " + appointment.getLastModificationTime() +
	//                        			" start time zone daylight bias" + appointment.getStartTimeZone().getDaylightBias());
	//                        }
	//->END DEBUG	
	//->DEBUG
	//	                                	Calendar debugCal = new GregorianCalendar();
	//	                                	debugCal.set(2013, 3, 1);
	//	                                	Date debugDate = debugCal.getTime();
	//	                                	if(debugDate.compareTo(new Date(events.get(y).getTimes().get(0).getStartTime().getValue())) < 0) {
	//	                                		System.out.println(events.get(y).getTitle().getPlainText() + " start time = " + new Date(events.get(y).getTimes().get(0).getStartTime().getValue()).toString());
	//	                                	}
	//->END DEBUG
	
	
	//->From original Event Feed Demo file
//	/**
//	 * Prints the titles of all events on the calendar specified by
//	 * {@code feedUri}.
//	 * 
//	 * @param service An authenticated CalendarService object.
//	 * @throws ServiceException If the service is unable to handle the request.
//	 * @throws IOException Error communicating with the server.
//	 */
//	private static void printAllEvents(CalendarService service)
//			throws ServiceException, IOException {
//		// Send the request and receive the response:
//		CalendarEventFeed resultFeed = service.getFeed(eventFeedUrl,
//				CalendarEventFeed.class);
//
//		System.out.println("All events on your calendar:");
//		System.out.println();
//		for (int i = 0; i < resultFeed.getEntries().size(); i++) {
//			CalendarEventEntry entry = resultFeed.getEntries().get(i);
//			System.out.println("\t" + entry.getTitle().getPlainText() + " / " + entry.getTimes().get(0).getStartTime());
//		}
//		System.out.println();
//	}
//
//	/**
//	 * Creates a single-occurrence event.
//	 * 
//	 * @param service An authenticated CalendarService object.
//	 * @param eventTitle Title of the event to create.
//	 * @param eventContent Text content of the event to create.
//	 * @return The newly-created CalendarEventEntry.
//	 * @throws ServiceException If the service is unable to handle the request.
//	 * @throws IOException Error communicating with the server.
//	 */
//	private static CalendarEventEntry createSingleEvent(CalendarService service,
//			String eventTitle, String eventContent) throws ServiceException,
//			IOException {
//		return createEvent(service, eventTitle, eventContent, null, false, null);
//	}
//
//	/**
//	 * Creates a quick add event.
//	 * 
//	 * @param service An authenticated CalendarService object.
//	 * @param quickAddContent The quick add text, including the event title, date
//	 *        and time.
//	 * @return The newly-created CalendarEventEntry.
//	 * @throws ServiceException If the service is unable to handle the request.
//	 * @throws IOException Error communicating with the server.
//	 */
//	private static CalendarEventEntry createQuickAddEvent(
//			CalendarService service, String quickAddContent) throws ServiceException,
//			IOException {
//		return createEvent(service, null, quickAddContent, null, true, null);
//	}
//	/**
//	 * Helper method to create either single-instance or recurring events. For
//	 * simplicity, some values that might normally be passed as parameters (such
//	 * as author name, email, etc.) are hard-coded.
//	 * 
//	 * @param service An authenticated CalendarService object.
//	 * @param eventTitle Title of the event to create.
//	 * @param eventContent Text content of the event to create.
//	 * @param recurData Recurrence value for the event, or null for
//	 *        single-instance events.
//	 * @param isQuickAdd True if eventContent should be interpreted as the text of
//	 *        a quick add event.
//	 * @param wc A WebContent object, or null if this is not a web content event.
//	 * @return The newly-created CalendarEventEntry.
//	 * @throws ServiceException If the service is unable to handle the request.
//	 * @throws IOException Error communicating with the server.
//	 */
//	private static CalendarEventEntry createEvent(CalendarService service,
//			String eventTitle, String eventContent, String recurData,
//			boolean isQuickAdd, WebContent wc) throws ServiceException, IOException {
//		CalendarEventEntry myEntry = new CalendarEventEntry();
//
//		myEntry.setTitle(new PlainTextConstruct(eventTitle));
//		myEntry.setContent(new PlainTextConstruct(eventContent));
//		myEntry.setQuickAdd(isQuickAdd);
//		myEntry.setWebContent(wc);
//
//		// If a recurrence was requested, add it. Otherwise, set the
//		// time (the current date and time) and duration (30 minutes)
//		// of the event.
//		if (recurData == null) {
//			Calendar calendar = new GregorianCalendar();
//			DateTime startTime = new DateTime(calendar.getTime(), TimeZone
//					.getDefault());
//
//			calendar.add(Calendar.MINUTE, 30);
//			DateTime endTime = new DateTime(calendar.getTime(), 
//					TimeZone.getDefault());
//
//			When eventTimes = new When();
//			eventTimes.setStartTime(startTime);
//			eventTimes.setEndTime(endTime);
//			myEntry.addTime(eventTimes);
//		} else {
//			Recurrence recur = new Recurrence();
//			recur.setValue(recurData);
//			myEntry.setRecurrence(recur);
//		}
//
//		// Send the request and receive the response:
//		return service.insert(eventFeedUrl, myEntry);
//	}
//	
//
//	/**
//	 * Prints a list of all the user's calendars.
//	 * 
//	 * @param service An authenticated CalendarService object.
//	 * @throws ServiceException If the service is unable to handle the request.
//	 * @throws IOException Error communicating with the server
//	 */
//	private static void printUserCalendars(CalendarService service)
//			throws IOException, ServiceException {
//		// Send the request and receive the response:
//		CalendarFeed resultFeed = service.getFeed(metafeedUrl, CalendarFeed.class);
//
//		System.out.println("Your calendars:");
//		System.out.println();
//		for (int i = 0; i < resultFeed.getEntries().size(); i++) {
//			CalendarEntry entry = resultFeed.getEntries().get(i);
//			System.out.println("\t" + entry.getTitle().getPlainText());
//		}
//		System.out.println();
//	}
//
//	/**
//	 * Prints the titles of all events matching a full-text query.
//	 * 
//	 * @param service An authenticated CalendarService object.
//	 * @param query The text for which to query.
//	 * @throws ServiceException If the service is unable to handle the request.
//	 * @throws IOException Error communicating with the server.
//	 */
//	private static void fullTextQuery(CalendarService service, String query)
//			throws ServiceException, IOException {
//		Query myQuery = new Query(eventFeedUrl);
//		myQuery.setFullTextQuery("Work2");
//
//		CalendarEventFeed resultFeed = service.query(myQuery,
//				CalendarEventFeed.class);
//
//		System.out.println("Events matching " + query + ":");
//		System.out.println();
//		for (int i = 0; i < resultFeed.getEntries().size(); i++) {
//			CalendarEventEntry entry = resultFeed.getEntries().get(i);
//			System.out.println("\t" + entry.getTitle().getPlainText());
//		}
//		System.out.println();
//	}
	//->END From original Event Feed Demo file
}
