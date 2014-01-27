package nf.co.korshyadoo.calendar;

import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
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
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import javax.swing.border.EmptyBorder;

import nf.co.korshyadoo.dataIo.DataIo;
import nf.co.korshyadoo.dataIo.XmlFileIo;

import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;
import com.ibm.icu.text.DateFormat;
import com.pff.PSTAppointment;
import com.pff.PSTException;
import com.pff.PSTFolder;


public class MainFrame extends JFrame {
	private static final long serialVersionUID = -7529292760103565977L;
	private JLabel lblClock;
	private JLabel lblCheck;
	private JLabel lblSuccess;
	private JLabel lblNumEvents;
	private JLabel lblActionTime;
	private JLabel lblUsername;
	private JLabel lblDeleteError;
	private JButton btnSync;
	private JButton btnDeleteAllEvents;
	private JButton btnChangeUser;
	private JButton btnDeleteDateRange;
	private JComboBox<String> cboFromMonth;
	private JComboBox<String> cboToMonth;
	private JComboBox<Integer> cboFromDay;
	private JComboBox<Integer> cboToDay;
	private JTextField txtFromYear;
	private JTextField txtToYear;
	private JTextArea txtTaskOutput;
	private JProgressBar progressBar;
	private DeleteAllEventsWorker deleteAllEventsWorker;
	private SyncWorker syncWorker;
	private DeleteDateRangeWorker deleteDateRangeWorker;

	public MainFrame() {
		setTitle("Outlook To Google Calendar Sync Build 0010");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setResizable(false);
		setBounds(100, 100, 725, 377);
		setPreferredSize(new Dimension((int)getBounds().getWidth(), (int)getBounds().getHeight()));
		
		createAndShowGui();
		
		pack();
		setVisible(true);
	}
	
	private void createAndShowGui() {
		
		setContentPane(createPanel());
	}
	
	private JPanel createPanel() {
		JPanel contentPane = new JPanel();
		contentPane.setLayout(null);
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		
		//A label showing the currently logged in user
		setLblUsername(new JLabel("Logged in as " + ProgramLauncher.getCalendarUtility().getUsername()));
		getLblUsername().setBounds(22, 11, 346, 14);
		contentPane.add(getLblUsername());
		
		//An animated gif of a clock. Used to indicate a process is in progress
		lblClock = new JLabel("");
		lblClock.setVisible(false);
		lblClock.setIcon(new ImageIcon(MainFrame.class.getResource("/resources/clock_e0.gif")));
		lblClock.setBounds(59, 50, 75, 75);
		contentPane.add(lblClock);

		//An image of a green check mark. Indicates a process has completed successfully
		lblCheck = new JLabel("");
		lblCheck.setVisible(false);
		lblCheck.setIcon(new ImageIcon(MainFrame.class.getResource("/resources/check_mark_green.jpg")));
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
		btnSync.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				btnSyncActionPerformed(arg0);
			}
		});
		btnSync.setBounds(57, 162, 89, 23);
		contentPane.add(btnSync);

		btnDeleteAllEvents = new JButton("Delete All Events");
		btnDeleteAllEvents.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				btnDeleteAllEventsActionPerformed(arg0);
			}
		});
		btnDeleteAllEvents.setBounds(166, 161, 120, 24);
		contentPane.add(btnDeleteAllEvents);

		btnChangeUser = new JButton("Change User");
		btnChangeUser.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				btnChangeUserActionPerformed(arg0);
			}
		});
		btnChangeUser.setBounds(113, 196, 120, 23);
		contentPane.add(btnChangeUser);
		
		btnDeleteDateRange = new JButton("Delete date range");
		btnDeleteDateRange.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				btnDeleteDateRangeActionPerformed(arg0);
			}
		});
		btnDeleteDateRange.setBounds(452, 165, 130, 28);
		contentPane.add(btnDeleteDateRange);
		
		JButton btnOpenVcards = new JButton("Open VCards");
		btnOpenVcards.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				File file = new File ("VCards/");
				Desktop desktop = Desktop.getDesktop();
				try {
					desktop.open(file);
				} catch (IOException e) {
				}
			}
		});
		btnOpenVcards.setBounds(67, 319, 120, 23);
		contentPane.add(btnOpenVcards);
		
		JButton btnOpenLogs = new JButton("Open Logs");
		btnOpenLogs.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				File file = new File ("logs/");
				Desktop desktop = Desktop.getDesktop();
				try {
					desktop.open(file);
				} catch (IOException e) {
				}
			}
		});
		btnOpenLogs.setBounds(197, 319, 89, 23);
		contentPane.add(btnOpenLogs);

		cboFromDay = new JComboBox<>();
		cboFromDay.setModel(new DefaultComboBoxModel<Integer>(thirtyOneDays()));
		cboFromDay.setBounds(550, 97, 62, 25);
		contentPane.add(cboFromDay);
		
		cboToDay = new JComboBox<>();
		cboToDay.setModel(new DefaultComboBoxModel<Integer>(thirtyOneDays()));
		cboToDay.setBounds(550, 129, 62, 25);
		contentPane.add(cboToDay);

		cboFromMonth = new JComboBox<>();
		cboFromMonth.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				cboFromMonthActionPerformed(arg0);
			}
		});
		String[] months = {"January",
				"February", "March", "April", "May", "June", "July", "August", "September",
				"October", "November", "December"};
		cboFromMonth.setModel(new DefaultComboBoxModel<>(months));
		cboFromMonth.setBounds(418, 98, 120, 23);
		contentPane.add(cboFromMonth);
		
		cboToMonth = new JComboBox<>();
		cboToMonth.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				cboToMonthActionPerformed(arg0);
			}
		});
		cboToMonth.setModel(new DefaultComboBoxModel<String>(months));
		cboToMonth.setBounds(418, 130, 120, 23);
		contentPane.add(cboToMonth);

		JLabel lblFrom = new JLabel("From:");
		lblFrom.setBounds(380, 99, 37, 16);
		contentPane.add(lblFrom);

		JLabel lblTo = new JLabel("To:");
		lblTo.setBounds(380, 133, 37, 16);
		contentPane.add(lblTo);

		lblDeleteError = new JLabel("<html><font color='red'>From date is before To date</font></html>");
		lblDeleteError.setBounds(441, 205, 156, 14);
		lblDeleteError.setVisible(false);
		contentPane.add(lblDeleteError);

		txtFromYear = new JTextField();
		txtFromYear.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent arg0) {
				//When focus is lost, if February is selected as the from month, update the number of days in cboFromDay
				if(cboFromMonth.getSelectedItem().equals("February")) {
					updateCboDay(cboFromMonth, cboFromDay, txtFromYear);
				}
			}
		});
		txtFromYear.setText("2013");
		txtFromYear.setBounds(630, 95, 47, 28);
		contentPane.add(txtFromYear);
		txtFromYear.setColumns(10);

		txtToYear = new JTextField();
		txtToYear.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent e) {
				//When focus is lost, if February is selected as the to month, update the number of days in cboToDay
				if(cboToMonth.getSelectedItem().equals("February")) {
					updateCboDay(cboToMonth, cboToDay, txtToYear);
				}
			}
		});
		txtToYear.setText("2013");
		txtToYear.setColumns(10);
		txtToYear.setBounds(630, 130, 47, 28);
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
		txtTaskOutput.setBounds(380, 263, 297, 73);
		contentPane.add(txtTaskOutput);
		
		JSeparator separator = new JSeparator();
		separator.setOrientation(SwingConstants.VERTICAL);
		separator.setBounds(350, 13, 12, 307);
		contentPane.add(separator);
		
		
		
		return contentPane;
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
	                    "Completed %d%% of task.\n", MainFrame.this.syncWorker.getProgress()));
	        } 
	    }
	}
	
	//TODO Fix javadoc
	/**
	 * Adjust the date list to reflect valid days of the month
	 */
	public void updateCboDay(JComboBox<String> cboMonth, JComboBox<Integer> cboDay, JTextField txtYear) {
		int day = cboDay.getSelectedIndex();			//Store the currently selected day
		
		//Based on which month is currently selected, change the number of days in cboDays to reflect the number of days in that month
		switch(cboMonth.getSelectedIndex()) {
		case 3:
		case 5:
		case 8:
		case 10:
			//30 days
			cboDay.setModel(new DefaultComboBoxModel<Integer>(thirtyDays()));
			break;
		case 1:
			//February:28 or 29
			if(Integer.parseInt(txtYear.getText()) % 4 == 0) {
				//Leap year
				cboDay.setModel(new DefaultComboBoxModel<Integer>(twentyNineDays()));
			} else {
				//Not a leap year
				cboDay.setModel(new DefaultComboBoxModel<Integer>(twentyEightDays()));
			}
			break;
		default:
			//31 days
			cboDay.setModel(new DefaultComboBoxModel<Integer>(thirtyOneDays()));
		}
		//Set the day to what was previously selected
		//If it's not a valid day, selected the last day of the selected month
		if(day < cboDay.getItemCount()) {
			cboDay.setSelectedIndex(day);
		} else {
			cboDay.setSelectedIndex(cboDay.getItemCount() - 1);
		}
	}

	/**
	 * Creates an Integer array containing numbers 1 through 28 inclusive, representing the days of the month.
	 * Called by updateCboDay(-) for updating combo box models
	 * @return An Integer array containing numbers 1 through 28 inclusive
	 */
	private Integer[] twentyEightDays() {
		Integer[] result = new Integer[28];
		for(int i = 0; i < result.length; i++) {
			result[i] = (i + 1);
		}
		
		return result;
	}
	
	/**
	 * Creates an Integer array containing numbers 1 through 29 inclusive, representing the days of the month.
	 * Called by updateCboDay(-) for updating combo box models
	 * @return An Integer array containing numbers 1 through 29 inclusive
	 */
	private Integer[] twentyNineDays() {
		Integer[] result = new Integer[29];
		Integer[] twentyEight = twentyEightDays();
		System.arraycopy(twentyEight, 0, result, 0, twentyEight.length);
		result[28] = 29;
		
		return result;
	}
	
	/**
	 * Creates an Integer array containing numbers 1 through 30 inclusive, representing the days of the month.
	 * Called by updateCboDay(-) for updating combo box models
	 * @return An Integer array containing numbers 1 through 30 inclusive
	 */	
	private Integer[] thirtyDays() {
		Integer[] result = new Integer[30];
		Integer[] twentyNine = twentyNineDays();
		System.arraycopy(twentyNine, 0, result, 0, twentyNine.length);
		result[29] = 30;
		
		return result;
	}
	
	/**
	 * Creates an Integer array containing numbers 1 through 31 inclusive, representing the days of the month.
	 * Called by updateCboDay(-) for updating combo box models and called by createAndShowGui() for initializing 
	 * the {@code JComboBox}s
	 * @return An Integer array containing numbers 1 through 31 inclusive
	 */	
	private Integer[] thirtyOneDays() {
		Integer[] result = new Integer[31];
		Integer[] thirty = thirtyDays();
		System.arraycopy(thirty, 0, result, 0, thirty.length);
		result[30] = 31;
		
		return result;
	}

	private void btnChangeUserActionPerformed(java.awt.event.ActionEvent evt) {
		this.setVisible(false);
		//Run LogInFrame and close this window
		java.awt.EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				//Hide labels when changing users
				MainFrame.this.lblCheck.setVisible(false);
				MainFrame.this.lblSuccess.setVisible(false);
				MainFrame.this.lblActionTime.setText("");
				MainFrame.this.lblNumEvents.setText("");
				
				
				new LoginFrame(MainFrame.this).setLocationRelativeTo(null);
			}
		});
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
		syncWorker = new SyncWorker(startTimer);
		syncWorker.addPropertyChangeListener(new BTNSyncPropertyChangesListener());
		syncWorker.execute();
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
		
		//This method is currently not usable
		if(true) {
			JOptionPane.showMessageDialog(null, "This function is currently out of order.");
			return;
		}
		
		
		
		
		
		
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
				deleteDateRangeWorker = new DeleteDateRangeWorker(fromDate, toDate, startTimer);
				deleteDateRangeWorker.addPropertyChangeListener(new PropertyChangeListener() {
					/**
				     * Invoked when task's progress property changes.
				     */
				    public void propertyChange(PropertyChangeEvent evt) {
				        if ("progress" == evt.getPropertyName()) {
				            int progress = (Integer)evt.getNewValue();
				            progressBar.setValue(progress);
				            txtTaskOutput.append(String.format(
				                    "Completed %d%% of task.\n", MainFrame.this.deleteDateRangeWorker.getProgress()));
				        } 
				    }
				});
				deleteDateRangeWorker.addPropertyChangeListener(new BTNSyncPropertyChangesListener());
				deleteDateRangeWorker.execute();
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
		
		//Start the SyncWorker
		deleteAllEventsWorker = new DeleteAllEventsWorker(startTimer);
		deleteAllEventsWorker.addPropertyChangeListener(new PropertyChangeListener() {
			/**
			 * Invoked when task's progress property changes.
			 */
			public void propertyChange(PropertyChangeEvent evt) {
				if ("progress" == evt.getPropertyName()) {
					int progress = (Integer)evt.getNewValue();
					progressBar.setValue(progress);
					txtTaskOutput.append(String.format(
							"Completed %d%% of task.\n", MainFrame.this.deleteAllEventsWorker.getProgress()));
				} 
			}
		});
		deleteAllEventsWorker.execute();
	}

	/**
	 * When the from month is selected, adjust the from date list to reflect valid days of the month
	 * @param evt
	 */
	private void cboFromMonthActionPerformed(ActionEvent evt) {
		updateCboDay(cboFromMonth, cboFromDay, txtFromYear);
	}

	/**
	 * Called by CBOToMonthActionListener
	 * @param evt
	 */
	private void cboToMonthActionPerformed(ActionEvent evt) {
		updateCboDay(cboToMonth, cboToDay, txtToYear);
	}
	
	/**
	 * Sets username label text
	 * @param s
	 */
	public void setLBLUsernameText(String s) { 
		getLblUsername().setText(s);
	}
	
	private class DeleteDateRangeWorker extends SwingWorker<Boolean, Void> {
		private Date from;											//The beginning of the range to be deleted
		private Date to;											//The end of the range to be deleted
		private Date startTimer;									//Tracks how long the process takes to complete
		private int minutes;										//Stores the number of minutes the process takes to complete
		private int seconds;										//Stores the number of seconds the process takes to complete
//		private List<CalendarEventEntry> deleteDateRangeEvents;		//Stores the list of events that were deleted

		//Constructor
		public DeleteDateRangeWorker(Date f, Date t, Date st) {
			from = f;
			to = t;
			startTimer = st;
		}
		
		@Override
		protected Boolean doInBackground() throws Exception {
			// TODO Auto-generated method stub
			return null;
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
		
		@Override
		protected Boolean doInBackground() throws Exception {
			//Retrieve all Google calendar events within the time range
			
			GoogleCalendarV3Utility calendarUtility = ProgramLauncher.getCalendarUtility();
			
			//Get all events from Google that are in the date range
			List<Event> events = calendarUtility.dateRangeQuery(this);
			
			before = events.size();
			List<Event> insertQueue = new ArrayList<>();			//List of Events to be added to Google calendar
			
			//For each PSTAppointment, check if it's in events; delete it from events if it is; add it to insertQueue if it's not
			
			//Obtain the list of outlook folders. Exit the method if it fails due to FileNotFoundException or PSTException
			DataIo dataIo = ProgramLauncher.getDataIo();
			String pstLocation = dataIo.getPstLocation();
			PstUtility pstUtility = PstUtility.getInstance(pstLocation);
			List<PSTFolder> outlookFolders = null;
			try {
				outlookFolders = pstUtility.getOutlookFolders();
			} catch (PSTException | IOException e1) {
				try {
					Thread.sleep(5000);
					outlookFolders = pstUtility.getOutlookFolders();
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
			
			//Stores PSTAppointments that have been modified since they've been created.
			//This aides in determining if a VCard should be created if the appointment
			//is put in the insert queue
			List<PSTAppointment> modified = new ArrayList<>();			
			
			for(int x = 0; x < outlookFolders.size(); x++) {                                                //For each outlook folder
				if(outlookFolders.get(x).getDisplayName().equals("Calendar")) {                       		//If the folder is named Calendar
					for(int z = 0; z < outlookFolders.get(x).getContentCount(); z++) {                		//For each appointment
						//Get a reference to the next appointment
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
						
						System.out.println(new Date(appointment.getStartTime().getTime()).toString());
						

						if(appointment.getStartTime().getTime() >= calendarUtility.getStartDate().getTime() && 
								appointment.getStartTime().getTime() <= calendarUtility.getEndDate().getTime()) {	//If appointment is within the desired time range
							
							if(!events.isEmpty()) {
								for(int y = 0; y < events.size(); y++) {									//For each Google Event
									//Compare appointment to event[y]
									//If they match, delete event[y]
									if(pstUtility.comparePstToEvent(appointment, events.get(y))) {
										System.out.println("match");
										events.remove(y);
										found = true;														//Prevents adding appointment to insertQueue
										break;
									}
									
								}
							}
							
							//If no match was found in events, or if events is empty, add appointment to insertqueue
							if(!found) {
								insertQueue.add(pstUtility.convertPstToEvent(appointment));
								
								//If the appointment has been modified since its creation time and the modification time is after the last sync time,
								//add it to the modified List
								if(appointment.getLastModificationTime().getTime() != appointment.getCreationTime().getTime()) {
									String lastSync = dataIo.getField(ProgramLauncher.getCalendarUtility().getUsername(), Fields.LAST_SYNC_MILLI);
									if(lastSync != null && appointment.getLastModificationTime().getTime() > Long.parseLong(lastSync)) {
										modified.add(appointment);
									}
								}
								
//								DEBUG: causing null pointer exceptions when getStartTimeZone() returns null
//								System.out.println("Adding PST \"" + appointment.getSubject() + "\"" + ", TZ = " +
//										appointment.getStartTimeZone().getDaylightBias() + 
//										" bias " + appointment.getStartTimeZone().getBias() + 
//										" time " + appointment.getStartTime() + " to insert queue" );
							}
						}
					}
				}
			}
			
			setProgress(90);
			LogWriter logWriter = new LogWriter();

			//Delete events from Google calendar
			if(events.isEmpty()) {
				System.out.println("no events to delete");
			} else {
				for(int x = 0; x < events.size(); x++) {		//For each Event in events
					if("Storm".equals(events.get(x).getSummary())) {
						System.out.println(new Date(events.get(x).getCreated().getValue()));
						System.out.println(new Date(Long.parseLong(dataIo.getField(calendarUtility.getUsername(), Fields.LAST_SYNC_MILLI))));
						System.out.println(events.get(x).getCreated().getValue());
						System.out.println(Long.parseLong(dataIo.getField(calendarUtility.getUsername(), Fields.LAST_SYNC_MILLI)));
						System.out.println(events.get(x).getCreated().getValue() > Long.parseLong(dataIo.getField(calendarUtility.getUsername(), Fields.LAST_SYNC_MILLI)));
					}
					//If the created time for the Google Event is after the last sync time, then create a VCard
					String lastSyncString = dataIo.getField(calendarUtility.getUsername(), Fields.LAST_SYNC_MILLI);
					if(lastSyncString != null) {
						if(events.get(x).getCreated().getValue() > Long.parseLong(lastSyncString)) {
							VcsWriter.create(events.get(x));			//Create a vcard file
						}
					}
					
					System.out.println("deleting: ");
					EventDateTime edt = events.get(x).getStart();
					DateTime dt = (DateTime)(edt.get("date"));
					if(dt == null) {
						System.out.println("" + new Date(GoogleCalendarV3Utility.getStartDateTime(events.get(x))).toString());
					} else {
						System.out.println("" + new Date(dt.getValue()));
					}
				}
				
				
				//Send delete request to Google server and write the event details to the log. 
				//If it fails, retry in 5 seconds and then report error if it fails again
				if(calendarUtility.deleteEvents(events)) {
					//Write the details of each deleted event to the log file
					logWriter.append(events, LogWriter.DELETE);
				}
				
				after = after - before;
			}
			
			//Add events in insertQueue
			if(insertQueue.isEmpty()) {
				System.out.println("no events to insert");
			} else {
				for(int x = 0; x < insertQueue.size(); x++) {			//For each event in the insertQueue
					System.out.println("inserting: ");
					System.out.println(new Date(GoogleCalendarV3Utility.getStartDateTime(insertQueue.get(x))).toString() +
							" / " + insertQueue.get(x).getSummary());
					System.out.println("Event timezone = " + insertQueue.get(x).getStart().getTimeZone());
					
					//If the event's created time (the time it was created in Outlook) is before the last sync time,
					//and the appointment hasn't been modified since its creation,
					//a VCard should be created with a green category to indicate the appointment should be removed from Outlook
					long created = insertQueue.get(x).getCreated().getValue();
					String lastSyncString = dataIo.getField(calendarUtility.getUsername(), Fields.LAST_SYNC_MILLI);
					if(lastSyncString != null) {
						Long lastSyncLong = new Long(lastSyncString);
						if(created < lastSyncLong.longValue()) {
							boolean found = false;
							for(PSTAppointment pst : modified) {
								if(pstUtility.comparePstToEvent(pst, insertQueue.get(x))) {
									found = true;
								}
							}
							
							//If the Event hasn't been modified, create the VCard
							if(!found) {
								//Create a VCard for the event with a green category
								VcsWriter.create(insertQueue.get(x), Colours.Green);
							}
						}
					}
				}
				
				//Send insert request to Google server and write the event details to the log.
				//If it fails, retry in 5 seconds and then report error if it fails again
				if(calendarUtility.insertEvents(insertQueue)) {
					//Write the details of each inserted event to the log file
					logWriter.append(insertQueue, LogWriter.INSERT);
				}
				
				after = after + insertQueue.size();
			} 

			//Find the time it took to perform the action
			Date endTimer = new Date();
			int totalTime = (int)((endTimer.getTime() - startTimer.getTime()) / 1000);
			minutes = (int)(totalTime / 60);
			seconds = (int)totalTime - (minutes * 60);

			//Find the number of events after sync
			after = before - events.size() + insertQueue.size();			

			//Update UI
			enableUI();
			lblCheck.setVisible(true);
			lblSuccess.setVisible(true);
			lblNumEvents.setText("In Google: " + before + " events before, " + after + " after");
			lblActionTime.setText("Action Time: " + minutes + " minutes, " + seconds + " seconds");

			//Flush the log file so that it gets written to the disk and clears the buffer
			try {
				logWriter.flush(minutes, seconds, before, after);
			} catch (IOException e) {
				e.printStackTrace();
				System.out.println("error:" +  e.getMessage());
				//IOException can be caused by file in use
				//Run IOExceptionFrame
				try {
					java.awt.EventQueue.invokeLater(new Runnable() {
						@Override
						public void run() {
							System.out.println("run ioexception frame. logWriter.flush()");
//							new IOExceptionFrame(MainFrame.this, IOExceptionFrame.RETRY_SYNC_LOG, minutes, seconds, before, after).setLocationRelativeTo(null);
						}
					});
					MainFrame.this.setVisible(false);
				} catch(Throwable ev) {
					JOptionPane.showMessageDialog(null,"Failed to write log file. settings.ini may be in use");
				}
			}
			
			//Record sync time
			DateFormat df = DateFormat.getDateTimeInstance(DateFormat.DEFAULT, DateFormat.DEFAULT);
			Date date = new Date();
			long latestCreatedDate = calendarUtility.getLatestCreatedDate();
			if(latestCreatedDate > date.getTime()) {
				date = new Date(latestCreatedDate);
			}
			dataIo.setField(calendarUtility.getUsername(), Fields.LAST_SYNC_TIME_DATETIME, df.format(date));
			dataIo.setField(calendarUtility.getUsername(), Fields.LAST_SYNC_MILLI, date.getTime() + "");
			
			//DEBUG
			String test = ((XmlFileIo)(dataIo)).getMemoryBuffer();
			System.out.println(test);
			
			return true;
		}
		
		public void publicSetProgress(int progress) {
			this.setProgress(progress);
		}
		
		public void publicSetProgress(double progress) {
			this.setProgress((int)progress);
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
		
		@Override
		public void done() {
			try {
				syncWorker.get();
			} catch (Exception e) {
				e.printStackTrace();
				JOptionPane.showMessageDialog(null, e);
			} finally {
				enableUI();
			}
		}
		
		
	}
	
	/**
	 * Used for debugging. Prints details on each event in the passed List
	 * @param events The list of {@code Event}s to get the details of
	 */
	private void debugPrintEventDetails(List<Event> events) {
		for(Event e : events) {
			System.out.println("{");
			System.out.println("\tName: " + e.getSummary());
			System.out.println("\tStart Date: " + new Date(GoogleCalendarV3Utility.getStartDateTime(e)).toString());
			System.out.println("\tLocation: " + e.getLocation());
			System.out.println("\tContent: " + e.getDescription());
			System.out.println("\tCreated: " + new Date(e.getCreated().getValue()).toString());
			System.out.println("}");
		}
	}
	
	private class DeleteAllEventsWorker extends SwingWorker<Boolean, Void> {
		private Date startTimer;			//Tracks how long the process takes
		private int minutes;				//Stores the number of minutes the process takes to complete
		private int seconds;				//Stores the number of seconds the process takes to complete
		
		public DeleteAllEventsWorker(Date d) {
			startTimer = d;
		}
		
		
		@Override
		protected Boolean doInBackground() throws Exception {
			GoogleCalendarV3Utility calendarUtility = ProgramLauncher.getCalendarUtility();
			
			List<Event> events = calendarUtility.allEventsQuery();			//Get all Google events
			setProgress(20);
			if(events.size() > 0) {
				calendarUtility.deleteEvents(events);							//Delete the events
			}
			setProgress(80);
			
			//Display success checkmark and label
			lblCheck.setVisible(true);
			lblSuccess.setVisible(true);
			
			//Find the time it took to perform the action
			Date endTimer = new Date();
			int totalTime = (int)((endTimer.getTime() - startTimer.getTime()) / 1000);
			minutes = (int)(totalTime / 60);
			seconds = (int)totalTime - (minutes * 60);
			lblActionTime.setText("Action time: " + minutes + " minutes, " + seconds + " seconds");
			
			//Update UI: 
			//reset mouse cursor, turn off clock .gif, enable all buttons, and hide progress bar
			MainFrame.this.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
			lblClock.setVisible(false);
			btnSync.setEnabled(true);
			btnDeleteAllEvents.setEnabled(true);
			btnChangeUser.setEnabled(true);
			btnDeleteDateRange.setEnabled(true);
			progressBar.setVisible(false);
			
			return true;
		}
		
	}
	
	/**
	 * Creates a Date object with the specified date.
	 * @param month the value used to set the month. Month value is 0-based
	 * @param day the value used to set the day of the month
	 * @param year the value used to set the year
	 * @return
	 */
	private Date createDate(int month, int day, int year) {
		java.util.Calendar from = new GregorianCalendar();
		from.set(year, month, day, 0, 0, 0);
		return from.getTime();
	}

	public JLabel getLblUsername() {
		return lblUsername;
	}

	public void setLblUsername(JLabel lblUsername) {
		this.lblUsername = lblUsername;
	}
}