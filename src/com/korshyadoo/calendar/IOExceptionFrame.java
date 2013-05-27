package com.korshyadoo.calendar;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import com.google.gdata.data.calendar.CalendarEventEntry;

@SuppressWarnings("serial")
public class IOExceptionFrame extends JFrame {

	private JPanel contentPane;
	private JLabel lblFail;
	private int mode;
	public static int RETRY_SYNC_LOG = 0;
	public static int RETRY_DELETE_DATE_RANGE_LOG = 1;
	private MainFrame mainFrame;
	private int minutes;										//Stores the number of minutes the process takes to complete
	private int seconds;										//Stores the number of seconds the process takes to complete
	private int before;											//Stores the number of events before syncing
	private int after;											//Stores the number of events after syncing
	private List<CalendarEventEntry> deleteDateRangeEvents;		//Stores the list of events that were deleted


	/**
	 * 
	 * @param m the calling frame
	 * @param mode determines which action to take when the retry button is pressed
	 * @param minutes the minutes field to write to the log
	 * @param seconds the seconds field to write to the log
	 * @param before the number of before Google events to write to the log
	 * @param after the number of after Google events to write to the log
	 */
	public IOExceptionFrame(MainFrame m, int mode, int minutes, int seconds, int before, int after) {
		//Set fields
		mainFrame = m;
		this.mode = mode;
		this.minutes = minutes;
		this.seconds = seconds;
		this.before = before;
		this.after = after;
		this.deleteDateRangeEvents = null;
		
		initComponents();
		setVisible(true);
	}
	/**
	 * 
	 * @wbp.parser.constructor
	 * @param m the calling frame
	 * @param mode determines which action to take when the retry button is pressed
	 * @param minutes the minutes field to write to the log
	 * @param seconds the seconds field to write to the log
	 * @param deleteDateRangeEvents a list of events to write to the log
	 */
	public IOExceptionFrame(MainFrame m, int mode, int minutes, int seconds, List<CalendarEventEntry> deleteDateRangeEvents) {
		//Set fields
		mainFrame = m;
		this.mode = mode;
		this.minutes = minutes;
		this.seconds = seconds;
		this.before = 0;
		this.after = 0;
		this.deleteDateRangeEvents = deleteDateRangeEvents;
		
		initComponents();
		setVisible(true);
	}
		
	/**
	 * Initialize frame components. Called from the constructor.
	 */
	private void initComponents() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 271, 225);
		setPreferredSize(new Dimension((int)getBounds().getWidth(), (int)getBounds().getHeight()));
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		JLabel lblIoExceptionFile = new JLabel("<html><center>IO Exception<br> File may be in use<br>Attempt to write log file again?");
		lblIoExceptionFile.setBounds(47, 6, 176, 72);
		contentPane.add(lblIoExceptionFile);
		
		JButton btnCancel = new JButton("Cancel");
		btnCancel.addActionListener(new btnCancelActionListener());
		btnCancel.setBounds(133, 90, 110, 23);
		contentPane.add(btnCancel);
		
		lblFail = new JLabel("<html><font color='red'>FAIL</font></html>");
		lblFail.setVisible(false);
		lblFail.setBounds(101, 162, 46, 14);
		contentPane.add(lblFail);
		
		JButton btnRetry = new JButton("Retry");
		btnRetry.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				btnRetryActionPerformed(arg0);
			}
		});
		btnRetry.setBounds(31, 90, 89, 23);
		contentPane.add(btnRetry);
	}
	
	public class btnCancelActionListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent arg0) {
			btnCancelActionPerformed(arg0);
		}
	}
	
	protected void btnCancelActionPerformed(ActionEvent evt) {
		mainFrame.setVisible(true);
		this.dispose();
	}
	
	private void btnRetryActionPerformed(ActionEvent evt) {
		switch(mode) {
		case 0:
			//RETRY_SYNC_LOG
			try {
				new LogWriter().writeSyncLog(before, after, minutes, seconds);
				
				mainFrame.setVisible(true);
				this.dispose();
			} catch (IOException e1) {
				lblFail.setVisible(true);
			}
			break;
		case 1:
			//RETRY_DELETE_DATE_RANGE_LOG
			try {
				new LogWriter().writeDeleteDateRangeLog(deleteDateRangeEvents, minutes, seconds);
				
				mainFrame.setVisible(true);
				this.dispose();
			} catch (IOException e1) {
				lblFail.setVisible(true);
			}
			break;
		}
	}
}
