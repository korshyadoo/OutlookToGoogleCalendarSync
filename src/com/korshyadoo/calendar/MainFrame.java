package com.korshyadoo.calendar;

import java.awt.ComponentOrientation;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JSeparator;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.LookAndFeel;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.border.EmptyBorder;

import com.google.gdata.data.calendar.CalendarEventEntry;
import com.google.gdata.data.calendar.CalendarEventFeed;
import com.google.gdata.util.AuthenticationException;
import com.google.gdata.util.ServiceException;
import com.pff.PSTAppointment;
import com.pff.PSTException;
import com.pff.PSTFolder;

/**
 * The main frame of the application
 *
 */
public class MainFrame extends JFrame {
	private static final long serialVersionUID = 1L;
	private JPanel contentPane;
	private JLabel lblClock;
	private JLabel lblCheck;
	private JLabel lblSuccess;
	private JLabel lblNumEvents;
	private JButton btnSync;
	private JButton btnDeleteAllEvents;
	private JButton btnChangeUser;
	private JComboBox<String> cboFromMonth;
	private JComboBox<String> cboToMonth;
	private JComboBox<Integer> cboFromDay;
	private JComboBox<Integer> cboToDay;
	private JLabel lblDeleteError;
	private JButton btnDeleteDateRange;
	private JTextField txtFromYear;
	private JTextField txtToYear;
	private JLabel lblActionTime;
	private JLabel lblUsername;
	private JProgressBar progressBar;
	private JTextArea txtTaskOutput;
	private DeleteAllEventsWorker daew;
	private SyncWorker sworker;
	private DeleteDateRangeWorker ddrw;
	protected OutlookToGoogleCalendarSync mySync;
	private PSTInterface pstInterface;
	
	//Constructor
	public MainFrame(OutlookToGoogleCalendarSync mySync) throws IOException {
		//Pass the pstLocation in settings.ini to the constructor of a new PSTInterface and pass it to other constructor for MainFrame along with mySync
		this(mySync, new PSTInterface(new SettingsIO().getSettingsField(SettingsIO.PST_LOCATION)));
	}
	public MainFrame(OutlookToGoogleCalendarSync mySync, PSTInterface pstInterface) {
		//Create the frame
		setTitle("Outlook To Google Calendar Sync Build 0007");
		setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 725, 362);
		setPreferredSize(new Dimension((int)getBounds().getWidth(), (int)getBounds().getHeight()));
		
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		this.mySync = mySync;
		this.pstInterface = pstInterface;

		initSwingComponents();
		
		this.setVisible(true);
	}

	/**
	 * Initialize the Swing components for the frame
	 */
	private void initSwingComponents() {
		lblUsername = new JLabel("Logged in as " + mySync.getUsername());
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
		btnChangeUser.addActionListener(new BTNChangeUserActionListener());
		btnChangeUser.setBounds(113, 196, 120, 23);
		contentPane.add(btnChangeUser);

		cboFromMonth = new JComboBox<>();
		cboFromMonth.addActionListener(new CBOFromMonthActionListener());
		cboFromMonth.setModel(new DefaultComboBoxModel<String>(new String[] {"January",
				"February", "March", "April", "May", "June", "July", "August", "September",
				"October", "November", "December"}));
		cboFromMonth.setBounds(418, 53, 120, 23);
		contentPane.add(cboFromMonth);

		cboFromDay = new JComboBox<>();
		cboFromDay.setModel(new DefaultComboBoxModel<Integer>(new Integer[] {1, 2, 3, 4,
				5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 
				24, 25, 26, 27, 28, 29, 30, 31}));
		cboFromDay.setBounds(550, 52, 62, 25);
		contentPane.add(cboFromDay);

		cboToMonth = new JComboBox<>();
		cboToMonth.addActionListener(new CBOToMonthActionListener());
		cboToMonth.setModel(new DefaultComboBoxModel<String>(new String[] {"January", 
				"February", "March", "April", "May", "June", "July", "August", "September", 
				"October", "November", "December"}));
		cboToMonth.setBounds(418, 85, 120, 23);
		contentPane.add(cboToMonth);

		cboToDay = new JComboBox<>();
		cboToDay.setModel(new DefaultComboBoxModel<Integer>(new Integer[] {1, 2, 3, 4, 
				5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 
				24, 25, 26, 27, 28, 29, 30, 31}));
		cboToDay.setBounds(550, 84, 62, 25);
		contentPane.add(cboToDay);

		btnDeleteDateRange = new JButton("Delete date range");
		btnDeleteDateRange.addActionListener(new BTNDeleteDateRangeActionListener());
		btnDeleteDateRange.setBounds(452, 120, 130, 28);
		contentPane.add(btnDeleteDateRange);

		JLabel lblFrom = new JLabel("From:");
		lblFrom.setBounds(380, 54, 37, 16);
		contentPane.add(lblFrom);

		JLabel lblTo = new JLabel("To:");
		lblTo.setBounds(380, 88, 37, 16);
		contentPane.add(lblTo);

		lblDeleteError = new JLabel("<html><font color='red'>From date is before To date</font></html>");
		lblDeleteError.setBounds(441, 160, 156, 14);
		lblDeleteError.setVisible(false);
		contentPane.add(lblDeleteError);

		txtFromYear = new JTextField();
		txtFromYear.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent arg0) {
				//When focus is lost, if February is selected as the from month, update the number of days in cboFromDay
				if(cboFromMonth.getSelectedItem().equals("February")) {
					updateCboFromDay();
				}
			}
		});
		txtFromYear.setText("2013");
		txtFromYear.setBounds(630, 50, 47, 28);
		contentPane.add(txtFromYear);
		txtFromYear.setColumns(10);

		txtToYear = new JTextField();
		txtToYear.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent e) {
				//When focus is lost, if February is selected as the to month, update the number of days in cboToDay
				if(cboToMonth.getSelectedItem().equals("February")) {
					updateCboToDay();
				}
			}
		});
		txtToYear.setText("2013");
		txtToYear.setColumns(10);
		txtToYear.setBounds(630, 85, 47, 28);
		contentPane.add(txtToYear);

		lblActionTime = new JLabel("");
		lblActionTime.setBounds(22, 267, 211, 20);
		contentPane.add(lblActionTime);
		
		progressBar = new JProgressBar();
		progressBar.setVisible(false);
		progressBar.setBounds(20, 137, 150, 19);
		progressBar.setStringPainted(true);
		contentPane.add(progressBar);
		
		txtTaskOutput = new JTextArea();
		txtTaskOutput.setVisible(false);
		txtTaskOutput.setBounds(380, 214, 297, 73);
		contentPane.add(txtTaskOutput);
		
		JSeparator separator = new JSeparator();
		separator.setOrientation(SwingConstants.VERTICAL);
		separator.setBounds(333, 11, 12, 307);
		contentPane.add(separator);
	}
	
	/**
	 * Determine if settings.ini is missing or empty. If no, read it and create a MainFrame object.
	 * If yes, search for the Outlook.pst file and prompt user for UN and Pass.
	 * @param args
	 */
	public static void main(String[] args) {
		//Set look and feel
		try {
			UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
		} catch (Throwable e) {
			//LookAndFeel not found on user's system
			try {
				LookAndFeel laf = null;
				UIManager.setLookAndFeel(laf);
			} catch (UnsupportedLookAndFeelException e1) {}
		}

		//If settings.ini is missing or empty (possibly because this is the first time running)
		//locate the .pst file and run LogInFrame, otherwise run MainFrame. 
		//If authentication exception is thrown running MainFrame, run LogInFrame
		SettingsIO settingsIO = null;
		try {
			settingsIO = new SettingsIO();
		} catch (IOException e1) {
			// TODO Use IOException frame to prevent exiting
			JOptionPane.showMessageDialog(null,"There was a problem accessing settings.ini. File may be inaccessible or in use.");
			System.exit(0);
		}
		try {
			if(settingsIO.isEmpty()) {												//Missing or empty settings.ini
				PSTInterface pstInterfacePrime = new PSTInterface();				//The default constructor looks for the .pst file in the default locations
				if(pstInterfacePrime.foundPST()) {
					//A .pst file was found
					//Write pstLocation to settings.ini for future executions
					settingsIO.setSettingsField(SettingsIO.PST_LOCATION, pstInterfacePrime.getPSTLocation());

					//Run LogInFrame
					java.awt.EventQueue.invokeLater(new LogInFrameRunnable());
				} else {
					//No .pst file was found in the default locations
					//Run PSTSearchFrame
					java.awt.EventQueue.invokeLater(new PSTSearchFrameRunnable());
				}
			} else {		//settings.ini exists and is not empty
				OutlookToGoogleCalendarSync mySyncPrime = new OutlookToGoogleCalendarSync(); 
				mySyncPrime.setUsername(settingsIO.getSettingsField(SettingsIO.USERNAME));								//Set the username for mySync
				mySyncPrime.setPassword(settingsIO.getSettingsField(SettingsIO.PASSWORD));								//Set the password for mySync
				PSTInterface pstInterfacePrime = new PSTInterface(settingsIO.getSettingsField(SettingsIO.PST_LOCATION));	//Create PSTInterface object from pstLocation in settings.ini
				mySyncPrime.setUserCredentials();																//Authenticate on Google server
				mySyncPrime.createURLObjects();																	//Form the URLs needed to use Google feeds
				java.awt.EventQueue.invokeLater(new MainFrameRunnable(mySyncPrime, pstInterfacePrime));
			}
		} catch(MalformedURLException e) {
			// Bad URL
			// This shouldn't be reachable because the user is authenticated before forming the URLs
			JOptionPane.showMessageDialog(null,"Uh oh - you've got an invalid URL");
			System.exit(0);
		} catch(AuthenticationException e) {
			if(e.getCause().toString().equals("java.net.UnknownHostException: www.google.com")) {
				JOptionPane.showMessageDialog(null,"Unable to reach host www.google.com. Please check your internet connection and try again");
				System.exit(0);
			}
			java.awt.EventQueue.invokeLater(new LogInFrameRunnable());
		}
	}
	
	/**
	 * A SwingWorker that allows UI updates to occur while using the delete date range feature.
	 *
	 */
	private class DeleteDateRangeWorker extends SwingWorker<Boolean, Void> {
		private Date from;											//The beginning of the range to be deleted
		private Date to;											//The end of the range to be deleted
		private Date startTimer;									//Tracks how long the process takes to complete
		private int minutes;										//Stores the number of minutes the process takes to complete
		private int seconds;										//Stores the number of seconds the process takes to complete
		private List<CalendarEventEntry> deleteDateRangeEvents;		//Stores the list of events that were deleted

		//Constructor
		public DeleteDateRangeWorker(Date f, Date t, Date st) {
			from = f;
			to = t;
			startTimer = st;
		}
		
		/**
		 * Retrieves the CalendarEventEntry objects from the Google calendar that are within the selected time range,
		 * sends a batch request to have them deleted, and reports the length of time the process took.
		 */
		@Override
		public Boolean doInBackground() {
			deleteDateRangeEvents = null;
			try {
				//Retrieve the events within the selected date range and send batch request to delete them
				deleteDateRangeEvents = mySync.timeQuery(from, to);
				mySync.deleteEvents(deleteDateRangeEvents);
				
				//Display success checkmark and label
				lblCheck.setVisible(true);
				lblSuccess.setVisible(true);
				
				//Find the time it took to perform the action
				Date endTimer = new Date();
				int totalTime = (int)((endTimer.getTime() - startTimer.getTime()) / 1000);
				minutes = (int)(totalTime / 60);
				seconds = (int)totalTime - (minutes * 60);
				lblActionTime.setText("Action time: " + minutes + " minutes, " + seconds + " seconds");
			} catch (ServiceException e) {
				JOptionPane.showMessageDialog(null,"Query request failed or system error retrieving feed");
				deleteDateRangeEvents = null;
			} catch (IOException e) {
				JOptionPane.showMessageDialog(null,"Error communicating with the GData service. Check internet connection and try again.");
				deleteDateRangeEvents = null;
			} finally {
				//Update UI: 
				//reset mouse cursor, turn off clock .gif, enable all buttons, and hide progress bar
				MainFrame.this.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
				lblClock.setVisible(false);
				btnSync.setEnabled(true);
				btnDeleteAllEvents.setEnabled(true);
				btnChangeUser.setEnabled(true);
				btnDeleteDateRange.setEnabled(true);
				progressBar.setVisible(false);
			}
			//Write log file entry recording all events deleted
			if(deleteDateRangeEvents != null && deleteDateRangeEvents.size() > 0) {							//If at least one event was deleted
				try {
					new LogWriter().writeDeleteDateRangeLog(deleteDateRangeEvents, minutes, seconds);
				} catch (IOException e) {
					//Run IOExceptionFrame
					try {
						java.awt.EventQueue.invokeLater(new Runnable() {
							@Override
							public void run() {
								new IOExceptionFrame(MainFrame.this, IOExceptionFrame.RETRY_DELETE_DATE_RANGE_LOG, minutes, seconds, deleteDateRangeEvents).setLocationRelativeTo(null);
							}
						});
						MainFrame.this.setVisible(false);
					} catch(Throwable ev) {
						JOptionPane.showMessageDialog(null,"Failed to write log file. settings.ini may be in use");
					}
				}
			}
			return true;
		}
	}

	class SyncWorker extends SwingWorker<Boolean, Void> {
		private Date startTimer;									//Tracks how long the process takes to complete
		private int minutes;										//Stores the number of minutes the process takes to complete
		private int seconds;										//Stores the number of seconds the process takes to complete
		private int before;											//Stores the number of events before syncing
		private int after;											//Stores the number of events after syncing

		//Constructor
		public SyncWorker(Date start) {
			startTimer = start;
		}

		/**
		 * The Google calendar is queried to retrieve an ArrayList containing 
		 * all appointments within the time range determined by the startDate and endDate 
		 * fields in the OutlookToGoogleCalendarSync object. Then, each appointment in the .pst file 
		 * is compared to each appointment in events to look for matches. 
		 * If an appointment from the .pst is found in events, it is removed 
		 * from events. If a .pst appointment is not in events, it is added to 
		 * a second, insertQueue, ArrayList. When all .pst files in the time 
		 * frame have been checked, a batch request is sent to Google to delete 
		 * all appointments remaining in events and insert all appointments 
		 * in insertQueue.
		 * @return Has no use.
		 * @throws PSTException
		 * @throws ServiceException query request failed, or service is unable to handle the request
		 * @throws FileNotFoundException from getOutlookFolders()
		 * @throws IOException from timeRangeQuery(): communicating with the GData service, from getOutlookFolders() and PSTFolder.getNextChild(): I/O error reading .pst 
		 */
		@Override
		public Boolean doInBackground() {
			//Retrieve all gmail events within the timerange
			List<CalendarEventEntry> events = null;
			try {
				events = mySync.timeRangeQuery(this);
			} catch (ServiceException | IOException e1) {
				try {
					Thread.sleep(5000);
					events = mySync.timeRangeQuery(this);
				} catch (InterruptedException e) { 
				} catch (ServiceException e) {
					JOptionPane.showMessageDialog(null,"Query request failed");
					enableUI();
					return false;
				} catch (IOException e) {
					JOptionPane.showMessageDialog(null,"Error communicating with the GData service");
					enableUI();
					return false;
				}
			}

			//Retrieve the number of events in the Google calendar before the sync
			before = events.size();

			List<CalendarEventEntry> insertQueue = new ArrayList<>();                                  		//List of CEEs to be added to gmail calendar

			//For each PSTAppointment, check if it's in events; delete it from events if it is; add it to addQueue if it's not
			
			//Obtain the list of outlook folders. Exit the method if it fails due to FileNotFoundException or PSTException
			List<PSTFolder> outlookFolders = null;
			try {
				outlookFolders = pstInterface.getOutlookFolders();
			} catch (PSTException | IOException e1) {
				try {
					Thread.sleep(5000);
					outlookFolders = pstInterface.getOutlookFolders();
				} catch (InterruptedException e) {
				} catch (FileNotFoundException | PSTException e) {
					JOptionPane.showMessageDialog(null,"Error accessing Outlook.pst. File may be missing or in use");
					enableUI();
					return false;
				} catch (IOException e) {
					JOptionPane.showMessageDialog(null,"I/O error accessing Outlook.pst");
					enableUI();
					return false;
				}
			}
			if(outlookFolders == null) {
				enableUI();
				return false;							
			}
			for(int x = 0; x < outlookFolders.size(); x++) {                                                //For each outlook folder
				if(outlookFolders.get(x).getDisplayName().equals("Calendar")) {                       		//If the folder is named Calendar
					for(int l = 0; l < outlookFolders.get(x).getContentCount(); l++) {                		//For each appointment
						PSTAppointment appointment = null;
						try {
							appointment = (PSTAppointment)outlookFolders.get(x).getNextChild();
						} catch (PSTException | IOException e) {
							try {
								Thread.sleep(5000);
								appointment = (PSTAppointment)outlookFolders.get(x).getNextChild();
							} catch (PSTException | IOException e1) {
								JOptionPane.showMessageDialog(null,"Error accessing Outlook.pst. File may be missing or in use");
								enableUI();
								return false;
							} catch (InterruptedException e1) {}
						}

						//Check if the appointment is in the time range
						//If it is not, continue to next appointment
						//If it is, and events is not empty, compare it to each event to find a match
						//If a match if found, remove the corresponding event from events
						boolean found = false;
						if(appointment.getStartTime().getTime() >= mySync.getStartDate().getTime() && 
								appointment.getStartTime().getTime() <= mySync.getEndDate().getTime()) {	//If appointment is within the desired time range
							System.out.println("app start " + appointment.getStartTime().toString());
							if(!events.isEmpty()) {
								for(int y = 0; y < events.size(); y++) {									//For each event CEE
									//Compare appointment to event[y]
									//If they match, delete event[y]
									if(pstInterface.compareCEEToPST(events.get(y), appointment)) {
										events.remove(y);
										found = true;														//Prevents adding appointment to insertQueue
										break;
									}
								}
							}
							//If no match was found in events, or if events is empty, add appointment to insertqueue
							if(!found) {
								insertQueue.add(pstInterface.convertPSTToCEE(appointment));
								System.out.println("Adding PST \"" + appointment.getSubject() + "\"" + ", TZ = " +
										appointment.getStartTimeZone().getDaylightBias() + 
										" bias " + appointment.getStartTimeZone().getBias() + 
										" time " + appointment.getStartTime() + " to insert queue" );
							}
						}
					}
				}
			}

			setProgress(90);

			//Delete events from gmail calendar
			if(events.isEmpty()) {
				System.out.println("no events to delete");
			} else {
				for(int x = 0; x < events.size(); x++) {
					System.out.println("deleting: ");
					System.out.println("" + new Date(events.get(x).getTimes().get(0).getStartTime().getValue()).toString());
				}
				
				//Send delete request to Google server. If it fails, retry in 5 seconds and then report error if it fails again
				try {
					mySync.deleteEvents(events);
				} catch (ServiceException | IOException e) {
					try {
						Thread.sleep(5000);
						mySync.deleteEvents(events);
					} catch (ServiceException e1) {
						JOptionPane.showMessageDialog(null,"Query request failed");
						enableUI();
						return false;
					} catch (IOException e1) {
						JOptionPane.showMessageDialog(null,"Error communicating with the GData service");
						enableUI();
						return false;
					} catch (InterruptedException e1) {}
				}
				after = after - before;
			}

			//Add events in insertQueue
			if(insertQueue.isEmpty()) {
				System.out.println("no events to insert");
			} else {
				for(int x = 0; x < insertQueue.size(); x++) {
					System.out.println("inserting: ");
					System.out.println(new Date(insertQueue.get(x).getTimes().get(0).getStartTime().getValue()).toString() +
							" / " + insertQueue.get(x).getTitle().getPlainText());
					System.out.println("CEE Tzshift = " + insertQueue.get(x).getTimes().get(0).getStartTime().getTzShift());
				}
				
				//Send insert request to Google server. If it fails, retry in 5 seconds and then report error if it fails again
				try {
					mySync.insertEvents(insertQueue);
				} catch (ServiceException | IOException e) {
					try {
						Thread.sleep(5000);
						mySync.insertEvents(insertQueue);
					} catch (ServiceException e1) {
						JOptionPane.showMessageDialog(null,"Query request failed");
						enableUI();
						return false;
					} catch (IOException e1) {
						JOptionPane.showMessageDialog(null,"Error communicating with the GData service");
						enableUI();
						return false;
					} catch (InterruptedException e1) {}
				}
				
				after = after + insertQueue.size();
			} 
			
			enableUI();
			lblCheck.setVisible(true);
			lblSuccess.setVisible(true);

			//Find the time it took to perform the action
			Date endTimer = new Date();
			int totalTime = (int)((endTimer.getTime() - startTimer.getTime()) / 1000);
			minutes = (int)(totalTime / 60);
			seconds = (int)totalTime - (minutes * 60);

			after = before - events.size() + insertQueue.size();			//Find the number of events after sync

			//Update label
			lblNumEvents.setText("In Google: " + before + " events before, " + after + " after");
			lblActionTime.setText("Action Time: " + minutes + " minutes, " + seconds + " seconds");


			//Write log file
			try {
				new LogWriter().writeSyncLog(minutes, seconds, before, after);
			} catch (IOException e) {
				//IOException can be caused by file in use
				//Run IOExceptionFrame
				try {
					java.awt.EventQueue.invokeLater(new Runnable() {
						@Override
						public void run() {
							new IOExceptionFrame(MainFrame.this, IOExceptionFrame.RETRY_SYNC_LOG, minutes, seconds, before, after).setLocationRelativeTo(null);
						}
					});
					MainFrame.this.setVisible(false);
				} catch(Throwable ev) {
					JOptionPane.showMessageDialog(null,"Failed to write log file. settings.ini may be in use");
				}
			}

			return true;
		}
		
		public void publicSetProgress(int progress) {
			this.setProgress(progress);
		}
		
		/**
		 * Re-enables the UI on the frame. The cursor is set to default, 
		 * the clock gif and progress bar are hidden, and all buttons are enabled.
		 */
		private void enableUI() {
			MainFrame.this.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
			lblClock.setVisible(false);
			progressBar.setVisible(false);
			btnSync.setEnabled(true);
			btnDeleteAllEvents.setEnabled(true);
			btnChangeUser.setEnabled(true);
			btnDeleteDateRange.setEnabled(true);
		}
	}	

	private class DeleteAllEventsWorker extends SwingWorker<Boolean, Void> {
		private Date startTimer;			//Tracks how long the process takes
		private int minutes;										//Stores the number of minutes the process takes to complete
		private int seconds;										//Stores the number of seconds the process takes to complete
		
		public DeleteAllEventsWorker(Date d) {
			startTimer = d;
		}

		@Override
		public Boolean doInBackground() {
			try {
				CalendarEventFeed resultFeed = mySync.getMyService().getFeed(mySync.getEventFeedURL(),
						CalendarEventFeed.class);
				double progress = 0;
				double totalResults = resultFeed.getTotalResults();
				setProgress(0);
				System.out.println("umber of e: " + totalResults);
				ArrayList<CalendarEventEntry> allCEE = new ArrayList<>();
				int total = 0;
				int page = 1;
				do {
					for (int i = 0; i < resultFeed.getEntries().size(); i++) {          //For each entry in the resultFeed
						CalendarEventEntry entry = resultFeed.getEntries().get(i);
						allCEE.add(entry);
						progress += ((1 / totalResults) * 100);							//Each entry increases the progress bar
						setProgress((int)progress);
					}
					System.out.println("page " + page + "; " + allCEE.size() + " items");
					total += allCEE.size();
					mySync.deleteEvents(allCEE);
					allCEE.clear();
					if(resultFeed.getNextLink() != null) {                              //If there's another page of results in the feed, get it
						resultFeed = mySync.getMyService().getFeed(new URL(resultFeed.getNextLink().getHref()), CalendarEventFeed.class);
					} else {															//If there are no more pages of results, start the feed over
						resultFeed = mySync.getMyService().getFeed(mySync.getEventFeedURL(),
								CalendarEventFeed.class);
						if(resultFeed.getEntries().size() == 0) {						//If the new feed is empty, all the events are deleted
							resultFeed = null;
						} else {														
							//The new feed has more events to delete
							System.out.println("Retrieving a new feed of events to delete");
						}
						if (allCEE.size() > 0) {	
							//Delete the retrieved events
							mySync.deleteEvents(allCEE);
							total += allCEE.size();
							allCEE.clear();
						}
					}
					page++;
				} while(resultFeed != null);
				System.out.println("Deleted " + total + " events");

				//Display success checkmark and label
				lblCheck.setVisible(true);
				lblSuccess.setVisible(true);
				
				//Find the time it took to perform the action
				Date endTimer = new Date();
				int totalTime = (int)((endTimer.getTime() - startTimer.getTime()) / 1000);
				minutes = (int)(totalTime / 60);
				seconds = (int)totalTime - (minutes * 60);
				lblActionTime.setText("Action time: " + minutes + " minutes, " + seconds + " seconds");
			} catch (MalformedURLException e) {
				JOptionPane.showMessageDialog(null,"MalformedURL. Action failed.");
			} catch (IOException e) {
				System.out.println("IO exception deleting");
				e.printStackTrace();
			} catch (ServiceException e) {
				System.out.println("Service exception deleting");
				e.printStackTrace();
			} finally {
				//Update UI: 
				//reset mouse cursor, turn off clock .gif, enable all buttons, and hide progress bar
				MainFrame.this.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
				lblClock.setVisible(false);
				btnSync.setEnabled(true);
				btnDeleteAllEvents.setEnabled(true);
				btnChangeUser.setEnabled(true);
				btnDeleteDateRange.setEnabled(true);
				progressBar.setVisible(false);
			}
			return true;
		}
	}

	/**
	 * Fires when a property of the DeleteAllEventsWorker is changed.
	 * Used to update the progress bar
	 * 
	 */
	private class BTNDeleteAllEventsPropertyChangeListener implements PropertyChangeListener {
	    /**
	     * Invoked when task's progress property changes.
	     */
	    public void propertyChange(PropertyChangeEvent evt) {
	        if ("progress" == evt.getPropertyName()) {
	            int progress = (Integer)evt.getNewValue();
	            progressBar.setValue(progress);
	            txtTaskOutput.append(String.format(
	                    "Completed %d%% of task.\n", MainFrame.this.daew.getProgress()));
	        } 
	    }
	}
	
	private class BTNSyncPropertyChangesListener implements PropertyChangeListener {
	    /**
	     * Invoked when task's progress property changes.
	     */
	    public void propertyChange(PropertyChangeEvent evt) {
	        if ("progress" == evt.getPropertyName()) {
	            int progress = (Integer)evt.getNewValue();
	            progressBar.setValue(progress);
	            txtTaskOutput.append(String.format(
	                    "Completed %d%% of task.\n", MainFrame.this.sworker.getProgress()));
	        } 
	    }
	}
	
	private class BTNDeleteDateRangePropertyChangeListener implements PropertyChangeListener {
	    /**
	     * Invoked when task's progress property changes.
	     */
	    public void propertyChange(PropertyChangeEvent evt) {
	        if ("progress" == evt.getPropertyName()) {
	            int progress = (Integer)evt.getNewValue();
	            progressBar.setValue(progress);
	            txtTaskOutput.append(String.format(
	                    "Completed %d%% of task.\n", MainFrame.this.ddrw.getProgress()));
	        } 
	    }
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
			cboFromDay.setModel(new DefaultComboBoxModel<Integer>(new Integer[] {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30}));
			break;
		case 1:
			//February:28 or 29
			if(Integer.parseInt(txtFromYear.getText()) % 4 == 0) {
				cboFromDay.setModel(new DefaultComboBoxModel<Integer>(new Integer[] {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29}));
			} else {
				cboFromDay.setModel(new DefaultComboBoxModel<Integer>(new Integer[] {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28}));
			}
			break;
		default:
			//31 days
			cboFromDay.setModel(new DefaultComboBoxModel<Integer>(new Integer[] {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31}));
		}
		//Set the from day to what was previously selected
		//If it's not a valid day, selected the last day of the selected month
		try {
			cboFromDay.setSelectedIndex(day);
		} catch(IllegalArgumentException e) {
			cboFromDay.setSelectedIndex(cboFromDay.getItemCount() - 1);
		}
	}

	private void updateCboToDay() {
		int day = cboToDay.getSelectedIndex();			//Store the currently selected to day
		switch(cboToMonth.getSelectedIndex()) {
		case 3:
		case 5:
		case 8:
		case 10:
			//30 days
			cboToDay.setModel(new DefaultComboBoxModel<Integer>(new Integer[] {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30}));
			break;
		case 1:
			//February:28 or 29
			if(Integer.parseInt(txtToYear.getText()) % 4 == 0) {
				cboToDay.setModel(new DefaultComboBoxModel<Integer>(new Integer[] {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29}));
			} else {
				cboToDay.setModel(new DefaultComboBoxModel<Integer>(new Integer[] {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28}));
			}
			break;
		default:
			//31 days
			cboToDay.setModel(new DefaultComboBoxModel<Integer>(new Integer[] {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31}));
		}
		//Set the to day to what was previously selected
		//If it's not a valid day, selected the last day of the selected month
		try {
			cboToDay.setSelectedIndex(day);
		} catch(IllegalArgumentException e) {
			cboToDay.setSelectedIndex(cboToDay.getItemCount() - 1);
		}
	}
	
	private void btnChangeUserActionPerformed(java.awt.event.ActionEvent evt) {
		//Run LogInFrame and close this window
		java.awt.EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				new LogInFrame(MainFrame.this).setLocationRelativeTo(null);
			}
		});
		this.setVisible(false);  
	}
	
	/**
	 * Starts the timer to time how long it takes to sync.
	 * Updates the UI to have applicable labels set to not visible, makes the 
	 * clock visible, changes to the wait mouse cursor, deactivates all buttons, 
	 * makes the progress bar visible, and then calls the SwingWorker.
	 * Called by BTNDeleteAllEventsActionListener.
	 * @param evt
	 */
	private void btnSyncActionPerformed(ActionEvent evt) {
		//Start the timer that times how long until the action is completed
		Date startTimer = new Date();

		////Update UI: 
		//change to wait cursor, turn on clock .gif, hide success check and label,
		//hide delete error, hide action time, hide number of events label, 
		//disable all buttons, and show progress bar
		this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		lblClock.setVisible(true);
		lblCheck.setVisible(false);
		lblSuccess.setVisible(false);
		lblDeleteError.setVisible(false);
		lblNumEvents.setText("");
		lblActionTime.setText("");
		btnSync.setEnabled(false);
		btnDeleteAllEvents.setEnabled(false);
		btnChangeUser.setEnabled(false);
		btnDeleteDateRange.setEnabled(false);
		progressBar.setVisible(true);
		progressBar.setValue(0);			//Reset progress bar to 0%
		sworker = new SyncWorker(startTimer);
		sworker.addPropertyChangeListener(new BTNSyncPropertyChangesListener());
		sworker.execute();
	}
	
	/**
	 * Starts the timer to time how long it takes to delete date range.
	 * Updates the UI to have applicable labels set to not visible, makes the 
	 * clock visible, changes to the wait mouse cursor, deactivates all buttons, 
	 * makes the progress bar visible, and then calls the SwingWorker.
	 * Called by BTNDeleteAllEventsActionListener.
	 * @param evt
	 */
	private void btnDeleteDateRangeActionPerformed (ActionEvent evt) {
		//Start the timer that times how long until the action is completed
		Date startTimer = new Date();

		//Hide the success image and label and clear label showing the number of events
		lblCheck.setVisible(false);
		lblSuccess.setVisible(false);
		lblActionTime.setText("");
		lblNumEvents.setText("");

		//If the to year is equal to or greater than the from year
		if(Integer.parseInt(txtToYear.getText()) >= Integer.parseInt(txtFromYear.getText())) {
			//Get From date
			Calendar from = new GregorianCalendar();
			int month = cboFromMonth.getSelectedIndex();
			int day = (Integer)cboFromDay.getSelectedItem();
			int year = Integer.parseInt(txtFromYear.getText());
			from.set(year, month, day, 0, 0, 0);
			Date fromDate = from.getTime();

			//Get To date
			Calendar to = new GregorianCalendar();
			month = cboToMonth.getSelectedIndex();
			day = (Integer)cboToDay.getSelectedItem();
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
				
				////Update UI: 
				//change to wait cursor, turn on clock .gif,
				//hide delete error, hide number of events label, hide action time, 
				//hide number of events label, disable all buttons, and show progress bar
				this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
				lblClock.setVisible(true);
				lblDeleteError.setVisible(false);
				lblNumEvents.setText("");
				lblActionTime.setText("");
				btnSync.setEnabled(false);
				btnDeleteAllEvents.setEnabled(false);
				btnChangeUser.setEnabled(false);
				btnDeleteDateRange.setEnabled(false);
				//progressBar.setVisible(true);
				progressBar.setValue(0);			//Reset progress bar to 0%
				ddrw = new DeleteDateRangeWorker(fromDate, toDate, startTimer);
				ddrw.addPropertyChangeListener(new BTNDeleteDateRangePropertyChangeListener());
				ddrw.addPropertyChangeListener(new BTNSyncPropertyChangesListener());
				ddrw.execute();
			}
		} else {									//From year is after to year
			lblDeleteError.setVisible(true);		//Display error
		}
	}
	
	/**
	 * Starts the timer to time how long it takes to delete all events.
	 * Updates the UI to have applicable labels set to not visible, makes the 
	 * clock visible, changes to the wait mouse cursor, deactivates all buttons, 
	 * makes the progress bar visible, and then calls the SwingWorker.
	 * Called by BTNDeleteAllEventsActionListener.
	 * @param evt
	 */
	private void btnDeleteAllEventsActionPerformed(ActionEvent evt) {
		//Start the timer that times how long until the action is completed
		Date startTimer = new Date();

		////Update UI: 
		//change to wait cursor, turn on clock .gif, hide success check and label,
		//hide delete error, hide action time, hide number of events label, 
		//disable all buttons, and show progress bar
		this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		lblClock.setVisible(true);
		lblCheck.setVisible(false);
		lblSuccess.setVisible(false);
		lblDeleteError.setVisible(false);
		lblActionTime.setText("");
		lblNumEvents.setText("");
		btnSync.setEnabled(false);
		btnDeleteAllEvents.setEnabled(false);
		btnChangeUser.setEnabled(false);
		btnDeleteDateRange.setEnabled(false);
		progressBar.setVisible(true);
		progressBar.setValue(0);			//Reset progress bar to 0%
		daew = new DeleteAllEventsWorker(startTimer);
		daew.addPropertyChangeListener(new BTNDeleteAllEventsPropertyChangeListener());
		daew.execute();
	}
	
	/**
	 * When the from month is selected, adjust the from date list to reflect valid days of the month
	 * @param evt
	 */
	private void cboFromMonthActionPerformed(ActionEvent evt) {
		updateCboFromDay();
	}
	
	/**
	 * Fires when btnSync is pressed
	 *
	 */
	public class BTNSyncActionListener implements ActionListener {
		public void actionPerformed(ActionEvent arg0) {
			btnSyncActionPerformed(arg0);
		}
	}
	
	/**
	 * Fires when btnDeleteAllEvents is pressed 
	 *
	 */
	private class BTNDeleteAllEventsActionListener implements ActionListener {
		public void actionPerformed(ActionEvent arg0) {
			btnDeleteAllEventsActionPerformed(arg0);
		}
	}
	
	/**
	 * Fires when btnDeleteDateRange is pressed
	 *
	 */
	private class BTNDeleteDateRangeActionListener implements ActionListener {
		public void actionPerformed(ActionEvent arg0) {
			btnDeleteDateRangeActionPerformed(arg0);
		}
	}
	
	/**
	 * Fires when cboFromMonth is changed
	 *
	 */
	private class CBOFromMonthActionListener implements ActionListener {
		public void actionPerformed(ActionEvent arg0) {
			cboFromMonthActionPerformed(arg0);
		}
	}
	
	/**
	 * Fires when cboToMonth is changed
	 *
	 */
	private class CBOToMonthActionListener implements ActionListener {
		public void actionPerformed(ActionEvent arg0) {
			cboToMonthActionPerformed(arg0);
		}
	}

	/**
	 * Called by CBOToMonthActionListener
	 * @param evt
	 */
	private void cboToMonthActionPerformed(ActionEvent evt) {
		updateCboToDay();
	}
	
	/**
	 * Fires when btnChangeUser is pressed
	 *
	 */
	public class BTNChangeUserActionListener implements ActionListener {
		public void actionPerformed(ActionEvent arg0) {
			btnChangeUserActionPerformed(arg0);
		}
	}
	
	/**
	 * Sets username label text
	 * @param s
	 */
	public void setLBLUsernameText(String s) { 
		lblUsername.setText(s);
	}
	
	/**
	 * Returns a PSTInterface object that uses the same pstLocation as the MainFrame object
	 * @return
	 */
	public PSTInterface getPSTInterface() {
		return new PSTInterface(pstInterface.getPSTLocation());
	}
	
	static class PSTSearchFrameRunnable implements Runnable {
		@Override
		public void run() {
			new PSTSearchFrame().setLocationRelativeTo(null);
		}
	}
	
	static class MainFrameRunnable implements Runnable {
		private OutlookToGoogleCalendarSync o;
		private PSTInterface p;
		
		public MainFrameRunnable(OutlookToGoogleCalendarSync o, PSTInterface p) {
			this.o = o;
			this.p = p;
		}
		
		@Override
		public void run() {
			new MainFrame(o, p).setLocationRelativeTo(null);
		}
	}
	
	
	
}
