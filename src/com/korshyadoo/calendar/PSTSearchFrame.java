package com.korshyadoo.calendar;

import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.EmptyBorder;

/**
 * Frame used for searching for the .pst file. 
 * @author korshyadoo
 *
 */
public class PSTSearchFrame extends JFrame {

	private static final long serialVersionUID = 1L;
	private JPanel contentPane;
	private JList<String> listPST;
	private JScrollPane scrollPane;
	private JButton btnOK;
	private JLabel lblMultiple;
	private JLabel lblNoPST;
	private JLabel lblSearchingForPST;
	private JLabel lblSearchingDir;

	/**
	 * Create the frame.
	 */
	public PSTSearchFrame() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 411, 301);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		lblSearchingForPST = new JLabel("<html><center>Searching for Outlook.pst file...<br>This may take a few minutes");
		int x = 124;
		lblSearchingForPST.setBounds(114, 6, 182, 55);
		contentPane.add(lblSearchingForPST);
		
		lblNoPST = new JLabel("<html><center>No .pst file was found.<br>Please ensure Microsoft Outlook is installed.");
		lblNoPST.setVisible(false);
		x = 182;
		lblNoPST.setBounds(114, 118, x, 89);
		contentPane.add(lblNoPST);
		
		lblMultiple = new JLabel("Multiple .pst files found. Please choose one:");
		lblMultiple.setVisible(false);
		lblMultiple.setBounds(95, 86, 263, 14);
		contentPane.add(lblMultiple);
		
		scrollPane = new JScrollPane();
		scrollPane.setVisible(false);
		scrollPane.setBounds(45, 111, 313, 99);
		contentPane.add(scrollPane);
		
		listPST = new JList<String>();
		listPST.setVisible(false);
		scrollPane.setViewportView(listPST);
		
		btnOK = new JButton("oK");
		btnOK.setVisible(false);
		btnOK.addActionListener(new BTNOKActionListener());
		btnOK.setBounds(145, 221, 89, 23);
		contentPane.add(btnOK);
		
		lblSearchingDir = new JLabel();
		lblSearchingDir.setBounds(26, 56, 342, 18);
		contentPane.add(lblSearchingDir);
		
		setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		
		setVisible(true);
		
		//Execute the search for the .pst file in another thread
		PSTSearchRunnable search = new PSTSearchRunnable(this);
		Thread t = new Thread(search);
		t.start();
	}
	
	/**
	 * Fires when the ok button is pressed
	 *
	 */
	private class BTNOKActionListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent arg0) {
			if(!(listPST.getSelectedIndex() == -1)) {					//If a selection has been made from the list
				//Write pstLocation to settings.ini and update PSTInterface object
				try {
					SettingsIO.getInstance().setSettingsField(SettingsIO.PST_LOCATION, listPST.getSelectedValue());
					PSTInterface.refresh();
				} catch (IOException e) {
					// TODO Use IOException frame to prevent exiting
					JOptionPane.showMessageDialog(null,"There was a problem reading settings.ini. File may be in use.");
					System.exit(0);
				}

				//Run LogInFrame and dispose of this frame
				java.awt.EventQueue.invokeLater(new LogInFrameRunnable());
				PSTSearchFrame.this.dispose();
			}
		}
	}
	
	private class PSTSearchRunnable implements Runnable {
		//Could have this return a List. null if no results, size 1 if 1 result, size > 1 if more than 1 result.
		//Then, this could be put into the PSTInterface and call from the frame.
		//Also, work on building the frame without extending JFrame.
		
		
		private PSTSearchFrame pstSearchFrame;

		public PSTSearchRunnable(PSTSearchFrame pstSearchFrame) {
			this.pstSearchFrame = pstSearchFrame;
		}

		@Override
		public void run() {
			//Search for *.pst on c:\
			Path startingDir = Paths.get("c:\\");
			String pattern = "*.pst";
			Finder finder = new Finder(pattern, PSTSearchFrame.this);
			try {
				Files.walkFileTree(startingDir, finder);
			} catch (IOException e1) {
				JOptionPane.showMessageDialog(null,"IOException when searching for .pst file");
				System.exit(0);
			}
			List<Path> results = finder.getResults();

			//Check the results
			if(results.size() == 1) {
				//Only one pst file found

				//Write pstLocation to settings.ini
				try {
					SettingsIO.getInstance().setSettingsField(SettingsIO.PST_LOCATION, results.get(0).toString());
					PSTInterface.refresh();
				} catch (IOException e) {
					// TODO Use IOExceptionFrame to prevent exiting
					JOptionPane.showMessageDialog(null,"There was a problem reading settings.ini. File may be in use.");
					System.exit(0);
				}

				//DEBUG
				System.out.println(results.get(0).toString());

				//Run LogInFrame and dispose of this frame
				java.awt.EventQueue.invokeLater(new LogInFrameRunnable());
				pstSearchFrame.dispose();
			} else if(results.size() > 1) {
				//More than one .pst found. Have the user choose one
				
				//Make visible the list box, label, and ok button; hide lblSearchingForPST and lblSearchingDir; and change mouse cursor back to default
				scrollPane.setVisible(true);
				listPST.setVisible(true);
				lblMultiple.setVisible(true);
				btnOK.setVisible(true);
				lblSearchingForPST.setVisible(false);
				lblSearchingDir.setVisible(false);
				setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));

				//Add the results to listPST
				DefaultListModel<String> listModel = new DefaultListModel<>();
				for(int x = 0; x < results.size(); x++) {
					listModel.addElement(results.get(x).toString());
				}
				listPST.setModel(listModel);
			} else {
				//No results found. Delete settings.ini and display error message
				try {
					SettingsIO.getInstance().deleteSettingsINI();
				} catch (IOException e) {
					e.printStackTrace();
				}
				lblNoPST.setVisible(true);
				lblSearchingForPST.setVisible(false);
				lblSearchingDir.setVisible(false);
				setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
			}		
		}
	}
	
	public void setLBLSearchingFileText(String text) {
		lblSearchingDir.setText(text);
	}
}
