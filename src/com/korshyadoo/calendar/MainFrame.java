package com.korshyadoo.calendar;

import java.awt.ComponentOrientation;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.ExecutionException;

import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingWorker;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;

import com.google.gdata.data.calendar.CalendarEventEntry;
import com.google.gdata.data.calendar.CalendarEventFeed;
import com.google.gdata.util.ServiceException;
import com.pff.PSTAppointment;
import com.pff.PSTException;
import com.pff.PSTFolder;

public class MainFrame extends JFrame {

	private JPanel contentPane;
	private JLabel lblClock;
	private JLabel lblCheck;
	private JLabel lblSuccess;
	protected JLabel lblNumEvents;
	private JButton btnSync;
	private JButton btnDeleteAllEvents;
	private JButton btnChangeUser;
	private JButton jButton1;
	private JComboBox cboFromMonth;
	private JComboBox cboToMonth;
	private JComboBox cboFromDay;
	private JComboBox cboToDay;
	private JLabel lblDeleteError;
	protected SyncReturn syncReturn;
	protected List<CalendarEventEntry> deleteDateRangeEvents;
	private int minutes;
	private int seconds;
	protected CalendarEventEntry nullTZ;
	private JButton btnDeleteDateRange;
	private JTextField txtFromYear;
	private JTextField txtToYear;
	private JLabel lblActionTime;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		try {
			UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
		} catch (Throwable e) {
			e.printStackTrace();
		}
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					MainFrame frame = new MainFrame();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public MainFrame() {
		setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 723, 440);
		setPreferredSize(new Dimension((int)getBounds().getWidth(), (int)getBounds().getHeight()));
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		centreWindow();

		JLabel lblUsername = new JLabel("Logged in as:");
		lblUsername.setText("Logged in as " + OutlookToGoogleCalendarSync.getUserrname());
		lblUsername.setBounds(57, 11, 346, 14);
		contentPane.add(lblUsername);

		lblClock = new JLabel("");
		lblClock.setVisible(false);
		lblClock.setIcon(new ImageIcon(MainFrame.class.getResource("/com/korshyadoo/calendar/clock_e0.gif")));
		lblClock.setBounds(59, 50, 75, 75);
		contentPane.add(lblClock);

		lblCheck = new JLabel("");
		lblCheck.setVisible(false);
		lblCheck.setIcon(new ImageIcon(MainFrame.class.getResource("/com/korshyadoo/calendar/check_mark_green.jpg")));
		lblCheck.setBounds(166, 50, 83, 75);
		contentPane.add(lblCheck);

		lblSuccess = new JLabel("Success!");
		lblSuccess.setVisible(false);
		lblSuccess.setBounds(187, 136, 62, 14);
		contentPane.add(lblSuccess);

		lblNumEvents = new JLabel("");
		lblNumEvents.setBounds(6, 228, 397, 14);
		contentPane.add(lblNumEvents);

		btnSync = new JButton("Sync");
		btnSync.addActionListener(new BTNSyncActionListener());
		btnSync.setBounds(57, 162, 89, 23);
		contentPane.add(btnSync);

		btnDeleteAllEvents = new JButton("Delete All Events");
		btnDeleteAllEvents.addActionListener(new BTNDeleteAllEventsActionListener());
		btnDeleteAllEvents.setBounds(166, 161, 120, 24);
		contentPane.add(btnDeleteAllEvents);

		btnChangeUser = new JButton("Change User");
		btnChangeUser.addActionListener(new ChangeUserActionListener());
		btnChangeUser.setBounds(113, 196, 120, 23);
		contentPane.add(btnChangeUser);


		//Query a narrow range of time and output the details of the entries found
		jButton1 = new JButton("<html><center>Find Google<br> appointments</center></html>");
		jButton1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				try {
					OutlookToGoogleCalendarSync.timeQuery();
				} catch (ServiceException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		});
		jButton1.setBounds(294, 35, 134, 44);
		contentPane.add(jButton1);

		cboFromMonth = new JComboBox();
		cboFromMonth.addActionListener(new CBOFromMonthActionListener());
		cboFromMonth.setModel(new DefaultComboBoxModel(new String[] {"January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"}));
		cboFromMonth.setBounds(342, 264, 120, 23);
		contentPane.add(cboFromMonth);

		cboFromDay = new JComboBox();
		cboFromDay.setModel(new DefaultComboBoxModel(new String[] {"1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23", "24", "25", "26", "27", "28", "29", "30", "31"}));
		cboFromDay.setBounds(474, 263, 62, 25);
		contentPane.add(cboFromDay);

		cboToMonth = new JComboBox();
		cboToMonth.addActionListener(new CBOToMonthActionListener());
		cboToMonth.setModel(new DefaultComboBoxModel(new String[] {"January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"}));
		cboToMonth.setBounds(342, 296, 120, 23);
		contentPane.add(cboToMonth);

		cboToDay = new JComboBox();
		cboToDay.setModel(new DefaultComboBoxModel(new String[] {"1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23", "24", "25", "26", "27", "28", "29", "30", "31"}));
		cboToDay.setBounds(474, 295, 62, 25);
		contentPane.add(cboToDay);

		btnDeleteDateRange = new JButton("Delete date range");
		btnDeleteDateRange.addActionListener(new BtnDeleteDateRangeActionListener());
		btnDeleteDateRange.setBounds(376, 331, 130, 28);
		contentPane.add(btnDeleteDateRange);

		JLabel lblFrom = new JLabel("From:");
		lblFrom.setBounds(275, 267, 55, 16);
		contentPane.add(lblFrom);

		JLabel lblTo = new JLabel("To:");
		lblTo.setBounds(275, 299, 55, 16);
		contentPane.add(lblTo);

		lblDeleteError = new JLabel("<html><font color='red'>From date is before To date</font></html>");
		lblDeleteError.setBounds(365, 371, 156, 14);
		lblDeleteError.setVisible(false);
		contentPane.add(lblDeleteError);

		txtFromYear = new JTextField();
		txtFromYear.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent arg0) {
				//When focus is lost, if February is selected as the from month, update the number of days in cboFromDay
				if(cboFromMonth.getSelectedIndex() == 1) {
					updateCboFromDay();
				}
			}
		});
		txtFromYear.setText("2013");
		txtFromYear.setBounds(554, 261, 47, 28);
		contentPane.add(txtFromYear);
		txtFromYear.setColumns(10);

		txtToYear = new JTextField();
		txtToYear.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent e) {
				//When focus is lost, if February is selected as the to month, update the number of days in cboToDay
				if(cboToMonth.getSelectedIndex() == 1) {
					updateCboToDay();
				}
			}
		});
		txtToYear.setText("2013");
		txtToYear.setColumns(10);
		txtToYear.setBounds(554, 296, 47, 28);
		contentPane.add(txtToYear);

		lblActionTime = new JLabel("");
		lblActionTime.setBounds(22, 267, 211, 20);
		contentPane.add(lblActionTime);


	}

	public JLabel getLblClock() {
		return lblClock;
	}

	private void btnChangeUserActionPerformed(java.awt.event.ActionEvent evt) {
		//Run FirstRunFrame and close this window
		java.awt.EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				new FirstRunFrame().setVisible(true);
			}
		});
		this.dispose();  
	}

	public class ChangeUserActionListener implements ActionListener {
		public void actionPerformed(ActionEvent arg0) {
			btnChangeUserActionPerformed(arg0);
		}
	}



	protected class DeleteDateRangeWorker extends SwingWorker<List<CalendarEventEntry>, Void> {
		private Date from;
		private Date to;
		private Date startTimer;

		public DeleteDateRangeWorker(Date f, Date t, Date st) {
			from = f;
			to = t;
			startTimer = st;
		}
		@Override
		protected List<CalendarEventEntry> doInBackground() {
			List<CalendarEventEntry> delete = null;
			try {
				delete = OutlookToGoogleCalendarSync.timeQuery(from, to);
				OutlookToGoogleCalendarSync.deleteEvents(delete);
				MainFrame.this.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
				lblClock.setVisible(false);
				lblCheck.setVisible(true);
				lblSuccess.setVisible(true);
				btnSync.setEnabled(true);
				btnDeleteAllEvents.setEnabled(true);
				btnChangeUser.setEnabled(true);
				btnDeleteDateRange.setEnabled(true);

				//Find the time it took to perform the action
				Date endTimer = new Date();
				int totalTime = (int)((endTimer.getTime() - startTimer.getTime()) / 1000);
				minutes = (int)(totalTime / 60);
				seconds = (int)totalTime - (minutes * 60);
				lblActionTime.setText("Action time: " + minutes + " minutes, " + seconds + " seconds");
			} catch (ServiceException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} 
			return delete;
		}

		@Override
		public void done() {
			//Get the List of deleted CalendarEventEntrys
			try {
				deleteDateRangeEvents = get();
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (ExecutionException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

			//Write log file entry recording all events deleted
			if(deleteDateRangeEvents != null && deleteDateRangeEvents.size() > 0) {														//If at least one event was deleted
				try (BufferedWriter log = new BufferedWriter(new FileWriter(new File(OutlookToGoogleCalendarSync.LOG_TXT), true))) {
					//Add formatting and record current Date
					Date now = new Date();
					log.write("\n" + OutlookToGoogleCalendarSync.SEPARATOR + 
							"\n" + "DELETED the following events at " + now.toString() + 
							"\nACTION TIME: " + minutes + " minutes, " + seconds + " seconds");

					//Write each deleted event to the log
					for(CalendarEventEntry cee : deleteDateRangeEvents) {													//For each deleted appointment
						log.write("\n\n\"" + cee.getTitle().getPlainText() + "\"\n" +
								"Start: " + new Date(cee.getTimes().get(0).getStartTime().getValue()).toString() + "\n" +
								"End: " + new Date(cee.getTimes().get(0).getEndTime().getValue()).toString() + "\n");
						if(cee.getLocations().get(0).getValueString().length() > 0) {						//If the event has a location, write it
							log.write("Location: " + cee.getLocations().get(0).getValueString() + "\n");
						}
						if(cee.getPlainTextContent().length() > 0) {										//If the event has content, write it
							log.write("Content: " + cee.getPlainTextContent() + "\n");
						}
						log.write("Created: " + new Date(cee.getPublished().getValue()).toString());
					}
				} catch (IOException e) {
					//Run IOExceptionFrame
					try {
						java.awt.EventQueue.invokeLater(new Runnable() {
							@Override
							public void run() {
								new IOExceptionFrame(IOExceptionFrame.RETRY_DELETE_DATE_RANGE, MainFrame.this).setVisible(true);
							}
						});
						MainFrame.this.setVisible(false);
						System.out.println("another test");
					} catch(Throwable ev) {
						System.out.println("Couldn't run IOExceptionFrame");
					}
				}
			}
		}
	}

	protected class SyncWorker extends SwingWorker<SyncReturn, Void> {
		private Date startTimer;

		public SyncWorker(Date start) {
			startTimer = start;
		}

		/**
		 * The Google calendar is queried to retrieve an ArrayList containing 
		 * all appointments with a start time 6 months prior to run time 
		 * and 100 years ahead. Then, each appointment in the .pst file 
		 * is compared to each appointment in events to look for matches. 
		 * If an appointment from the .pst is found in events, it is removed 
		 * from events. If a .pst appointment is not in events, it is added to 
		 * a second, insertQueue, ArrayList. When all .pst files in the time 
		 * frame have been checked, a batch request is sent to Google to delete 
		 * all appointments remaining in events and insert all appointments 
		 * in insertQueue.
		 * @return Has no use.
		 * @throws PSTException
		 * @throws ServiceException
		 * @throws IOException 
		 */
		@Override
		protected SyncReturn doInBackground() throws PSTException, ServiceException, IOException {
			//Retrieve all gmail events 6 months prior and 100 years into future
			ArrayList<CalendarEventEntry> events = OutlookToGoogleCalendarSync.timeRangeQuery();
			SyncReturn numEvents = new SyncReturn(events.size());

			ArrayList<CalendarEventEntry> insertQueue = new ArrayList<>();                                  //List of CEEs to be added to gmail calendar

			//For each PSTAppointment, check if it's in events; delete it from events if it is; add it to addQueue if it's not
			Vector<PSTFolder> outlookFolders = OutlookToGoogleCalendarSync.getOutlookFolders();
			for(int x = 0; x < outlookFolders.size(); x++) {                                                //For each outlook folder
				if(outlookFolders.elementAt(x).getDisplayName().equals("Calendar")) {                       //If the folder is named Calendar
					for(int l = 0; l < outlookFolders.elementAt(x).getContentCount(); l++) {                //For each appointment
						PSTAppointment appointment = (PSTAppointment)outlookFolders.get(x).getNextChild();
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

						if(events.isEmpty()) {
							//There are no events in the Google Calendar for the time range, so the appointment needs to be inserted
							ConvertReturn cr = OutlookToGoogleCalendarSync.convertPSTToCEE(appointment);
							insertQueue.add(cr.getCEE());
							//                            System.out.println("Adding PST \"" + appointment.getSubject() + "\"" + ", TZ = " +
							//                            		appointment.getStartTimeZone().getDaylightBias() + 
							//                            		" time " + appointment.getStartTime().toString() + 
							//                            		" created " + appointment.getCreationTime().toString() + " to insert queue" );
							if(cr.getNullTZ()) {
								//Write log entry for null timezone
								try (BufferedWriter log = new BufferedWriter(new FileWriter(new File(OutlookToGoogleCalendarSync.LOG_TXT), true))) {
									//Add formatting and record current Date
									log.write("\n" + OutlookToGoogleCalendarSync.SEPARATOR + 
											"\n" + "Found null time zone on \"" + cr.getCEE().getTitle().getPlainText() +
											"\" - " + new Date(cr.getCEE().getTimes().get(0).getStartTime().getValue()).toString());
								} catch (IOException e) {
									//Run IOExceptionFrame
									try {
										java.awt.EventQueue.invokeLater(new Runnable() {
											@Override
											public void run() {
												new IOExceptionFrame(IOExceptionFrame.RETRY_NULL_TZ, MainFrame.this).setVisible(true);
											}
										});
										MainFrame.this.setVisible(false);
									} catch(Throwable ev) {
										System.out.println("Couldn't run IOExceptionFrame");
									}
								}
							}
						} else {
							boolean found = false;
							if(appointment.getStartTime().getTime() >= OutlookToGoogleCalendarSync.getStartDate().getTime() && 
									appointment.getStartTime().getTime() <= OutlookToGoogleCalendarSync.getEndDate().getTime()) {                //If appointment is within the desired time range
								for(int y = 0; y < events.size(); y++) {                                            //For each event CEE
									//->DEBUG
									//	                                	Calendar debugCal = new GregorianCalendar();
									//	                                	debugCal.set(2013, 3, 1);
									//	                                	Date debugDate = debugCal.getTime();
									//	                                	if(debugDate.compareTo(new Date(events.get(y).getTimes().get(0).getStartTime().getValue())) < 0) {
									//	                                		System.out.println(events.get(y).getTitle().getPlainText() + " start time = " + new Date(events.get(y).getTimes().get(0).getStartTime().getValue()).toString());
									//	                                	}
									//->END DEBUG

									//Compare appointment to event[y]
									//If they match, delete event[y]
									//If no match, add appointment to insertQueue
									if(OutlookToGoogleCalendarSync.compareCEEToPST(events.get(y), appointment)) {
										events.remove(y);
										found = true;
										break;
									}
								}
								if(!found) {
									ConvertReturn cr = OutlookToGoogleCalendarSync.convertPSTToCEE(appointment);
									insertQueue.add(cr.getCEE());
									System.out.println("Adding PST \"" + appointment.getSubject() + "\"" + ", TZ = " +
											appointment.getStartTimeZone().getDaylightBias() + 
											" bias " + appointment.getStartTimeZone().getBias() + 
											" time " + appointment.getStartTime() + " to insert queue" );
									if(cr.getNullTZ()) {
										//Write log entry for null timezone
										try (BufferedWriter log = new BufferedWriter(new FileWriter(new File(OutlookToGoogleCalendarSync.LOG_TXT), true))) {
											//Add formatting and record current Date
											log.write("\n" + OutlookToGoogleCalendarSync.SEPARATOR + 
													"\n" + "Found null time zone on \"" + cr.getCEE().getTitle().getPlainText() +
													"\" - " + new Date(cr.getCEE().getTimes().get(0).getStartTime().getValue()).toString());
										} catch (IOException e) {
											//Run IOExceptionFrame
											nullTZ = cr.getCEE();
											try {
												java.awt.EventQueue.invokeLater(new Runnable() {
													@Override
													public void run() {
														new IOExceptionFrame(IOExceptionFrame.RETRY_NULL_TZ, MainFrame.this).setVisible(true);
													}
												});
												MainFrame.this.setVisible(false);
											} catch(Throwable ev) {
												System.out.println("Couldn't run IOExceptionFrame");
											}
										}
									}
								}
							} else {
								//DEBUG System.out.println(" // is not in the range");
							}
						}
					}
				}
			}

			//Delete events from gmail calendar
			if(events.isEmpty()) {
				System.out.println("no events to delete");
			} else {
				for(int x = 0; x < events.size(); x++) {
					System.out.println("deleting: ");
					System.out.println("" + new Date(events.get(x).getTimes().get(0).getStartTime().getValue()).toString());
				}
				OutlookToGoogleCalendarSync.deleteEvents(events);
				numEvents.after = numEvents.after - events.size();
			}

			//Add events in addQueue
			if(insertQueue.isEmpty()) {
				System.out.println("no events to insert");
			} else {
				for(int x = 0; x < insertQueue.size(); x++) {
					System.out.println("inserting: ");
					System.out.println(new Date(insertQueue.get(x).getTimes().get(0).getStartTime().getValue()).toString() +
							" / " + insertQueue.get(x).getTitle().getPlainText());
					System.out.println("CEE Tzshift = " + insertQueue.get(x).getTimes().get(0).getStartTime().getTzShift());
				}
				OutlookToGoogleCalendarSync.insertEvents(insertQueue);
				numEvents.after = numEvents.after + insertQueue.size();
			} 
			MainFrame.this.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
			lblClock.setVisible(false);
			lblCheck.setVisible(true);
			lblSuccess.setVisible(true);
			btnSync.setEnabled(true);
			btnDeleteAllEvents.setEnabled(true);
			btnChangeUser.setEnabled(true);
			btnDeleteDateRange.setEnabled(true);

			//Find the time it took to perform the action
			Date endTimer = new Date();
			int totalTime = (int)((endTimer.getTime() - startTimer.getTime()) / 1000);
			minutes = (int)(totalTime / 60);
			seconds = (int)totalTime - (minutes * 60);

			return numEvents;
		}

		@Override
		public void done() {
			try {
				//Update label
				if(get() != null) {
					syncReturn = get();
					lblNumEvents.setText("In Google: " + syncReturn.before + " events before, " + syncReturn.after + " after");
					lblActionTime.setText("Action Time: " + minutes + " minutes, " + seconds + " seconds");

					//Write log file entry with current time and number of Google events before and after
					try (BufferedWriter log = new BufferedWriter(new FileWriter(new File(OutlookToGoogleCalendarSync.LOG_TXT), true))) {
						log.newLine();
						log.write(OutlookToGoogleCalendarSync.SEPARATOR);
						log.newLine();
						Date now = new Date();
						log.write("SYNC at " + now.toString() + "\nACTION TIME: " + minutes + " minutes, " + seconds + " seconds");
						log.newLine();
						log.write("In Google: " + syncReturn.before + " events before, " + syncReturn.after + " after");
					} catch (IOException e) {
						//IOException can be caused by file in use
						//Run IOExceptionFrame
						try {
							java.awt.EventQueue.invokeLater(new Runnable() {
								@Override
								public void run() {
									new IOExceptionFrame(IOExceptionFrame.RETRY_SYNC, MainFrame.this).setVisible(true);
								}
							});
							MainFrame.this.setVisible(false);
						} catch(Throwable ev) {
							System.out.println("Couldn't run IOExceptionFrame");
						}
					}
				}
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (ExecutionException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
	}	


	protected class DeleteAllEventsWorker extends SwingWorker<Boolean, Void> {
		private Date from;
		private Date to;
		private Date startTimer;

		@Override
		protected Boolean doInBackground() {
			try {
				CalendarEventFeed resultFeed = OutlookToGoogleCalendarSync.getMyService().getFeed(OutlookToGoogleCalendarSync.getEventFeedURL(),
						CalendarEventFeed.class);
				ArrayList<CalendarEventEntry> allCEE = new ArrayList<>();
				int total = 0;
				int page = 1;
				do {
					for (int i = 0; i < resultFeed.getEntries().size(); i++) {          //For each entry in the resultFeed
						CalendarEventEntry entry = resultFeed.getEntries().get(i);
						allCEE.add(entry);
					}
					if(page % 4 == 0) {
						System.out.println("page " + page + "; " + allCEE.size() + " items");
						total += allCEE.size();
						OutlookToGoogleCalendarSync.deleteEvents(allCEE);
						allCEE.clear();
					} else {
						System.out.println("_page " + page);
					}
					if(resultFeed.getNextLink() != null) {                              //If there's another page of results in the feed, get it
						resultFeed = OutlookToGoogleCalendarSync.getMyService().getFeed(new URL(resultFeed.getNextLink().getHref()), CalendarEventFeed.class);
					} else {
						resultFeed = OutlookToGoogleCalendarSync.getMyService().getFeed(OutlookToGoogleCalendarSync.getEventFeedURL(),
								CalendarEventFeed.class);
						if(resultFeed.getEntries().size() == 0) {
							resultFeed = null;
						}
						if (allCEE.size() > 0) {
							OutlookToGoogleCalendarSync.deleteEvents(allCEE);
							total += allCEE.size();
							allCEE.clear();
						}
					}
					page++;
				} while(resultFeed != null);
				System.out.println("Deleted " + total + " events");
				MainFrame.this.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
				lblClock.setVisible(false);
				lblCheck.setVisible(true);
				lblSuccess.setVisible(true);
				btnSync.setEnabled(true);
				btnDeleteAllEvents.setEnabled(true);
				btnChangeUser.setEnabled(true);
				btnDeleteDateRange.setEnabled(true);

				//Find the time it took to perform the action
				Date endTimer = new Date();
				int totalTime = (int)((endTimer.getTime() - startTimer.getTime()) / 1000);
				minutes = (int)(totalTime / 60);
				seconds = (int)totalTime - (minutes * 60);
				lblActionTime.setText("Action time: " + minutes + " minutes, " + seconds + " seconds");
			} catch (MalformedURLException e) {
				System.out.println("MalformedURL exception deleting");
				e.printStackTrace();
			} catch (IOException e) {
				System.out.println("IO exception deleting");
				e.printStackTrace();
			} catch (ServiceException e) {
				System.out.println("Service exception deleting");
				e.printStackTrace();
			}
			return true;
		}
	}

	public class BTNSyncActionListener implements ActionListener {
		public void actionPerformed(ActionEvent arg0) {
			btnSyncActionPerformed(arg0);
		}
	}

	private class BTNDeleteAllEventsActionListener implements ActionListener {
		public void actionPerformed(ActionEvent arg0) {
			btnDeleteAllEventsActionPerformed(arg0);
		}
	}

	public void btnDeleteAllEventsActionPerformed(ActionEvent evt) {
		//Start the timer that times how long until the action is completed
		Date startTimer = new Date();

		this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		lblClock.setVisible(true);
		lblCheck.setVisible(false);
		lblSuccess.setVisible(false);
		lblDeleteError.setVisible(false);
		btnSync.setEnabled(false);
		btnDeleteAllEvents.setEnabled(false);
		btnChangeUser.setEnabled(false);
		btnDeleteDateRange.setEnabled(false);
		lblActionTime.setText("");
		new DeleteAllEventsWorker().execute();
	}


	private void btnSyncActionPerformed(ActionEvent evt) {
		//Start the timer that times how long until the action is completed
		Date startTimer = new Date();

		this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		lblClock.setVisible(true);
		lblCheck.setVisible(false);
		lblSuccess.setVisible(false);
		lblDeleteError.setVisible(false);
		btnSync.setEnabled(false);
		btnDeleteAllEvents.setEnabled(false);
		btnChangeUser.setEnabled(false);
		btnDeleteDateRange.setEnabled(false);
		lblActionTime.setText("");
		SyncWorker sworker = new SyncWorker(startTimer);
		sworker.execute();
	}


	private class BtnDeleteDateRangeActionListener implements ActionListener {
		public void actionPerformed(ActionEvent arg0) {
			btnDeleteDateRangeActionPerformed(arg0);
		}
	}

	private void btnDeleteDateRangeActionPerformed (ActionEvent evt) {
		//Start the timer that times how long until the action is completed
		Date startTimer = new Date();

		//Hide the success image and label
		lblCheck.setVisible(false);
		lblSuccess.setVisible(false);
		lblActionTime.setText("");

		//If the to year is equal to or greater than the from year
		if(Integer.parseInt(txtToYear.getText()) >= Integer.parseInt(txtFromYear.getText())) {
			//Get From date
			Calendar from = new GregorianCalendar();
			int month = cboFromMonth.getSelectedIndex();
			int day = cboFromDay.getSelectedIndex() + 1;
			int year = Integer.parseInt(txtFromYear.getText());
			from.set(year, month, day, 0, 0, 0);
			Date fromDate = from.getTime();

			//Get To date
			Calendar to = new GregorianCalendar();
			month = cboToMonth.getSelectedIndex();
			day = cboToDay.getSelectedIndex() + 1;
			year = Integer.parseInt(txtToYear.getText());
			to.set(year, month, day, 23, 59, 59);
			Date toDate = to.getTime();

			//If the from date is before the to date, execute the SwingWorker. Otherwise, make lblDeleteError visible
			assert fromDate.compareTo(toDate) != 0;			//Because the hours, minutes, and seconds are hard-coded, the from and to dates cannot be equal
			if(fromDate.compareTo(toDate) > 0) {
				//fromDate is after toDate: error
				lblDeleteError.setVisible(true);
			} else {
				//fromDate is before toDate: execute DeleteDateRangeWorker
				this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
				lblClock.setVisible(true);
				lblDeleteError.setVisible(false);
				btnSync.setEnabled(false);
				btnDeleteAllEvents.setEnabled(false);
				btnChangeUser.setEnabled(false);
				btnDeleteDateRange.setEnabled(false);
				new DeleteDateRangeWorker(fromDate, toDate, startTimer).execute();
			}
		} else {									//From year is after to year
			lblDeleteError.setVisible(true);		//Display error
		}
	}

	private class CBOFromMonthActionListener implements ActionListener {
		public void actionPerformed(ActionEvent arg0) {
			cboFromMonthActionPerformed(arg0);
		}
	}


	/**
	 * When the from month is selected, adjust the from date list to reflect valid days of the month
	 * @param evt
	 */
	private void cboFromMonthActionPerformed(ActionEvent evt) {
		updateCboFromDay();
	}

	/**
	 * Adjust the from date list to reflect valid days of the month
	 */
	public void updateCboFromDay() {
		int day = cboFromDay.getSelectedIndex();			//Store the currently selected from day
		switch(cboFromMonth.getSelectedIndex()) {
		case 3:
		case 5:
		case 8:
		case 10:
			//30 days
			cboFromDay.setModel(new DefaultComboBoxModel(new String[] {"1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23", "24", "25", "26", "27", "28", "29", "30"}));
			break;
		case 1:
			//February:28 or 29
			if(Integer.parseInt(txtFromYear.getText()) % 4 == 0) {
				cboFromDay.setModel(new DefaultComboBoxModel(new String[] {"1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23", "24", "25", "26", "27", "28", "29"}));
			} else {
				cboFromDay.setModel(new DefaultComboBoxModel(new String[] {"1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23", "24", "25", "26", "27", "28"}));
			}
			break;
		default:
			//31 days
			cboFromDay.setModel(new DefaultComboBoxModel(new String[] {"1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23", "24", "25", "26", "27", "28", "29", "30", "31"}));
		}
		//Set the from day to what was previously selected
		//If it's not a valid day, selected the last day of the selected month
		try {
			cboFromDay.setSelectedIndex(day);
		} catch(IllegalArgumentException e) {
			cboFromDay.setSelectedIndex(cboFromDay.getItemCount() - 1);
		}
	}

	private class CBOToMonthActionListener implements ActionListener {
		public void actionPerformed(ActionEvent arg0) {
			cboToMonthActionPerformed(arg0);
		}
	}

	private void cboToMonthActionPerformed(ActionEvent evt) {
		updateCboToDay();
	}

	private void updateCboToDay() {
		int day = cboToDay.getSelectedIndex();			//Store the currently selected to day
		switch(cboToMonth.getSelectedIndex()) {
		case 3:
		case 5:
		case 8:
		case 10:
			//30 days
			cboToDay.setModel(new DefaultComboBoxModel(new String[] {"1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23", "24", "25", "26", "27", "28", "29", "30"}));
			break;
		case 1:
			//February:28 or 29
			cboToDay.setModel(new DefaultComboBoxModel(new String[] {"1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23", "24", "25", "26", "27", "28"}));
			break;
		default:
			//31 days
			cboToDay.setModel(new DefaultComboBoxModel(new String[] {"1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23", "24", "25", "26", "27", "28", "29", "30", "31"}));
		}
		//Set the to day to what was previously selected
		//If it's not a valid day, selected the last day of the selected month
		try {
			cboToDay.setSelectedIndex(day);
		} catch(IllegalArgumentException e) {
			cboToDay.setSelectedIndex(cboToDay.getItemCount() - 1);
		}
	}


	/**
	 * Centres the window on the screen
	 * @param jf JFrame for the window to be centred
	 */
	private void centreWindow() {
		Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
		int X = (screen.width / 2) - ((int)this.getPreferredSize().getWidth() / 2); // Center horizontally.
		int Y = (screen.height / 2) - ((int)this.getPreferredSize().getHeight() / 2); // Center vertically.
		this.setBounds(X,Y , (int)this.getPreferredSize().getWidth(), (int)this.getPreferredSize().getHeight());        
	}

	public SyncReturn getSyncReturn() { return syncReturn; }
	public int getMinutes() { return minutes; }
	public int getSeconds() { return seconds; }

}
