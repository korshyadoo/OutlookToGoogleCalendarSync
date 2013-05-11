package com.korshyadoo.calendar;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Toolkit;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import java.awt.GridLayout;
import javax.swing.JLabel;
import javax.swing.JButton;

import com.google.gdata.data.calendar.CalendarEventEntry;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;
import java.util.List;

public class IOExceptionFrame extends JFrame {

	private JPanel contentPane;
	private JLabel lblFail;
	private int mode;
	public static int RETRY_SYNC = 0;
	public static int RETRY_DELETE_DATE_RANGE = 1;
	private MainFrame mainFrame;

	/**
	 * Create the frame.
	 */
	public IOExceptionFrame(int mode, MainFrame m) {
		mainFrame = m;
		this.mode = mode;
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 271, 225);
		setPreferredSize(new Dimension((int)getBounds().getWidth(), (int)getBounds().getHeight()));
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		centreWindow();
		
		JLabel lblIoExceptionFile = new JLabel("<html><center>IO Exception<br> File may be in use<br>Attempt to write log file again?");
		lblIoExceptionFile.setBounds(47, 6, 176, 72);
		contentPane.add(lblIoExceptionFile);
		
		JButton btnCancel = new JButton("Cancel");
		btnCancel.addActionListener(new btnCancelActionListener());
		btnCancel.setBounds(133, 90, 110, 23);
		contentPane.add(btnCancel);
		
		//TODO
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
			//RETRY_SYNC
			
			try (BufferedWriter log = new BufferedWriter(new FileWriter(new File(OutlookToGoogleCalendarSync.LOG_TXT), true))) {
				log.newLine();
				log.write(OutlookToGoogleCalendarSync.SEPARATOR);
				log.newLine();
				Date now = new Date();
				log.write("SYNC at " + now.toString() + "\nACTION TIME: " + mainFrame.getMinutes() + " minutes, " + mainFrame.getSeconds() + " seconds");
				log.newLine();
				log.write("In Google: " + mainFrame.syncReturn.before + " events before, " + mainFrame.syncReturn.after + " after");
			} catch (IOException e1) {
				lblFail.setVisible(true);
			}
			mainFrame.setVisible(true);
			this.dispose();
			break;
		case 1:
			//RETRY_DELETE_DATE_RANGE
			
			assert(mainFrame.deleteDateRangeEvents.size() != 0);
			assert(mainFrame.deleteDateRangeEvents != null);
			
			try (BufferedWriter log = new BufferedWriter(new FileWriter(new File(OutlookToGoogleCalendarSync.LOG_TXT), true))) {
	    		//Add formatting and record current Date
				Date now = new Date();
				log.write("\n" + OutlookToGoogleCalendarSync.SEPARATOR + 
						"\n" + "DELETED the following events at " + now.toString() + 
						"\nACTION TIME: " + mainFrame.getMinutes() + " minutes, " + mainFrame.getSeconds() + " seconds");
				
				//Write each deleted event to the log
				for(CalendarEventEntry cee : mainFrame.deleteDateRangeEvents) {													//For each deleted appointment
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
				mainFrame.setVisible(true);
				this.dispose();
			} catch (IOException e1) {
				lblFail.setVisible(true);
			}
			break;
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
}
