package com.korshyadoo.calendar;

import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.MalformedURLException;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

import org.jasypt.util.text.BasicTextEncryptor;

import com.google.gdata.util.AuthenticationException;

public class FirstRunFrame extends JFrame {

	private JPanel contentPane;
	private JLabel lblAuthenticationFailed;
	private JTextField txtInputEmailAddress;
	private JTextField txtInputPassword;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					FirstRunFrame frame = new FirstRunFrame();
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
	public FirstRunFrame() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
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
				System.exit(1);
			}
		});
		jbClose.setBounds(201, 189, 89, 23);
		contentPane.add(jbClose);
		
		JCheckBox chckbxShowPassword = new JCheckBox("Show Password");
		chckbxShowPassword.setBounds(128, 157, 117, 20);
		contentPane.add(chckbxShowPassword);
		
		lblAuthenticationFailed = new JLabel("<html><font color='red'>Authentication Failed");
		lblAuthenticationFailed.setVisible(false);
		lblAuthenticationFailed.setBounds(138, 20, 124, 16);
		contentPane.add(lblAuthenticationFailed);
		
		centreWindow();
	}
	
	public void jbOkActionPerformed(java.awt.event.ActionEvent evt) {
		//Set the username and password static fields
		OutlookToGoogleCalendarSync.setUsername(txtInputEmailAddress.getText());
		OutlookToGoogleCalendarSync.setPassword(txtInputPassword.getText());
		
		try {
			//Set the credentials for myService. If this fails, the credentials aren't written to settings.ini
			//Throws an exception if authentication fails
			OutlookToGoogleCalendarSync.getMyService().setUserCredentials(OutlookToGoogleCalendarSync.getUserrname(), OutlookToGoogleCalendarSync.getPassword());
			
			//Store un + pass in OutlookToGmailCalendarSync.settings, encrypt OutlookToGmailCalendarSync.settings, and write it to settings.ini
			OutlookToGoogleCalendarSync.setSettings("username=" + txtInputEmailAddress.getText() 
					+ "\npassword=" + txtInputPassword.getText() + "\n");
			BasicTextEncryptor encryptor = new BasicTextEncryptor();
			encryptor.setPassword(OutlookToGoogleCalendarSync.ENCRYPTOR_PASS);
			String encryptedBuffer = encryptor.encrypt(OutlookToGoogleCalendarSync.getSettings().toString());
			PrintWriter outFile = new PrintWriter(new File(OutlookToGoogleCalendarSync.SETTINGS_INI_LOCATION));
			outFile.print(encryptedBuffer);
			outFile.close();
			
			OutlookToGoogleCalendarSync.createURLObjects();
			
			//Run MainFrame and close this window
			java.awt.EventQueue.invokeLater(new Runnable() {
				@Override
				public void run() {
					new MainFrame().setVisible(true);
				}
			});
			this.dispose();
		} catch(MalformedURLException e) {
			// Bad URL
			System.err.println("Uh oh - you've got an invalid URL.");
		} catch (AuthenticationException e) {
			lblAuthenticationFailed.setVisible(true);
			txtInputEmailAddress.requestFocus();
			txtInputEmailAddress.selectAll();
		} catch(IOException e) {
			System.out.println("IOException when writing encrypted un and pass to settings.ini");
		}
	}
	
	public class JBOKActionListener implements ActionListener {
		public void actionPerformed(ActionEvent arg0) {
			jbOkActionPerformed(arg0);
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
