package com.korshyadoo.calendar;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.net.MalformedURLException;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

import com.google.gdata.util.AuthenticationException;

/**
 * Frame used to enter Google credentials.
 * @author korshyadoo
 *
 */
@SuppressWarnings("serial")
public class LogInFrame extends JFrame {
	private JPanel contentPane;
	private JLabel lblAuthenticationFailed;
	private JTextField txtInputEmailAddress;
	private JTextField txtInputPassword;
	private MainFrame mainFrame;
	
	/**
	 * Create the frame. If mainFrame is not null, after the frame has completed its function, 
	 * it will set mainFrame visible. If mainFrame is null, it will run a MainFrame object.
	 * @param mainFrame The calling MainFrame
	 */
	public LogInFrame(MainFrame mainFrame) {
		this.mainFrame = mainFrame;
		initComponents();
		setVisible(true);
	}
	
	private void initComponents() {
		
		//Add a listener to delete settings.ini if the login window is closed
		addWindowListener(new java.awt.event.WindowAdapter(){
			public void windowClosing(WindowEvent we) {
				//Attempt to delete settings.ini before exiting to avoid an exception on next program launch
				SettingsIO sio;
				try {
					sio = SettingsIO.getInstance();
					sio.deleteSettingsINI();
				} catch (IOException e) {
					e.printStackTrace();
				}
				
				System.exit(0);
			}
		});
		
		setBounds(100, 100, 472, 285);
		setPreferredSize(new Dimension((int)getBounds().getWidth(), (int)getBounds().getHeight()));
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		JLabel lblEnterGoogleAddress = new JLabel("Enter Google address and password");
		lblEnterGoogleAddress.setBounds(88, 46, 203, 18);
		contentPane.add(lblEnterGoogleAddress);
		
		JLabel lblEmailAddress = new JLabel("Email address:");
		lblEmailAddress.setBounds(31, 79, 89, 14);
		contentPane.add(lblEmailAddress);
		
		JLabel lblPassword = new JLabel("Password:");
		lblPassword.setBounds(41, 125, 79, 14);
		contentPane.add(lblPassword);
		
		txtInputEmailAddress = new JTextField();
		txtInputEmailAddress.setBounds(118, 76, 271, 37);
		contentPane.add(txtInputEmailAddress);
		txtInputEmailAddress.setColumns(10);
		
		txtInputPassword = new JTextField();
		txtInputPassword.setColumns(10);
		txtInputPassword.setBounds(118, 118, 271, 37);
		contentPane.add(txtInputPassword);
		
		JButton jbOK = new JButton("oK");
		
		jbOK.addActionListener(new JBOKActionListener());
		jbOK.setBounds(102, 189, 89, 23);
		contentPane.add(jbOK);
		
		JButton jbClose = new JButton("Close");
		jbClose.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				//Attempt to delete settings.ini before exiting to avoid an exception on next program launch
				SettingsIO sio;
				try {
					sio = SettingsIO.getInstance();
					sio.deleteSettingsINI();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				
				System.exit(1);
			}
		});
		jbClose.setBounds(201, 189, 89, 23);
		contentPane.add(jbClose);
		
		JCheckBox chckbxShowPassword = new JCheckBox("Show Password");
		chckbxShowPassword.setVisible(false);
		chckbxShowPassword.setBounds(128, 157, 117, 20);
		contentPane.add(chckbxShowPassword);
		
		lblAuthenticationFailed = new JLabel("<html><font color='red'>Authentication Failed");
		lblAuthenticationFailed.setVisible(false);
		lblAuthenticationFailed.setBounds(138, 20, 124, 16);
		contentPane.add(lblAuthenticationFailed);
	}
	
	public void jbOkActionPerformed(java.awt.event.ActionEvent evt) {
		OutlookToGoogleCalendarSync sync = new OutlookToGoogleCalendarSync();
		
		//Set the username and password static fields
		sync.setUsername(txtInputEmailAddress.getText());
		sync.setPassword(txtInputPassword.getText());
		
		try {
			//Set the credentials for connecting to Google. If this fails, the credentials aren't written to settings.ini
			//Throws an exception if authentication fails
			sync.setUserCredentials();				//Authenticate on Google server
			
			//Write username and password to settings.ini
			SettingsIO logInFrameSettingsIO = null;
			try {
				logInFrameSettingsIO = SettingsIO.getInstance();
			} catch (IOException e) {
				// TODO Use IOException frame to prevent exiting
				JOptionPane.showMessageDialog(null,"There was a problem reading settings.ini. File may be in use.");
				System.exit(0);
			}
			logInFrameSettingsIO.setSettingsField(SettingsIO.USERNAME, txtInputEmailAddress.getText());
			logInFrameSettingsIO.setSettingsField(SettingsIO.PASSWORD, txtInputPassword.getText());
			
			sync.createURLObjects();				//Form the URLs needed to use Google feeds
			
			if(mainFrame == null) {
				//Run new MainFrame object
				java.awt.EventQueue.invokeLater(new MainFrameRunnable(sync));
			} else {
				//Update the OutlookToGoogleCalendarSync object used by mainFrame
				mainFrame.mySync = sync;
				
				//Set mainFrame visible and update username label
				mainFrame.setVisible(true);
				mainFrame.setLBLUsernameText(("Logged in as " + sync.getUsername()));
			}
			this.dispose();
		} catch(MalformedURLException e) {
			// Bad URL
			JOptionPane.showMessageDialog(null,"Malformed URL");
			System.exit(0);
		} catch (AuthenticationException e) {
			lblAuthenticationFailed.setVisible(true);
			txtInputEmailAddress.requestFocus();
			txtInputEmailAddress.selectAll();
		}
	}
	
	public class JBOKActionListener implements ActionListener {
		public void actionPerformed(ActionEvent arg0) {
			jbOkActionPerformed(arg0);
		}
	}
	
	private class MainFrameRunnable implements Runnable {
		private OutlookToGoogleCalendarSync o;

		public MainFrameRunnable(OutlookToGoogleCalendarSync o) {
			this.o = o;
		}

		@Override
		public void run() {
			new MainFrame(o).setLocationRelativeTo(null);
		}
	}

}
