package com.korshyadoo.calendar;

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

public class PSTSearchFrame extends JFrame {

	private static final long serialVersionUID = 1L;
	private JPanel contentPane;
	private JList<String> listPST;
	private JScrollPane scrollPane;
	private JButton btnOK;
	private JLabel lblMultiple;
	private JLabel lblNoPST;
	private JLabel lblSearchingForPST;

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
		
		lblSearchingForPST = new JLabel("Searching for Outlook.pst file...");
		int x = 124;
		lblSearchingForPST.setBounds(114, 32, 182, 14);
		contentPane.add(lblSearchingForPST);
		
		lblNoPST = new JLabel("<html><center>No .pst file was found.<br>Please ensure Microsoft Outlook is installed.");
		lblNoPST.setVisible(false);
		x = 182;
		lblNoPST.setBounds((int)(this.getBounds().getWidth() / 2) - (x / 2), 105, x, 89);
		contentPane.add(lblNoPST);
		
		lblMultiple = new JLabel("Multiple .pst files found. Please choose one:");
		lblMultiple.setVisible(false);
		lblMultiple.setBounds(95, 73, 263, 14);
		contentPane.add(lblMultiple);
		
		scrollPane = new JScrollPane();
		scrollPane.setVisible(false);
		scrollPane.setBounds(45, 98, 313, 99);
		contentPane.add(scrollPane);
		
		listPST = new JList<String>();
		listPST.setVisible(false);
		scrollPane.setViewportView(listPST);
		
		btnOK = new JButton("oK");
		btnOK.setVisible(false);
		btnOK.addActionListener(new BTNOKActionListener());
		btnOK.setBounds(145, 208, 89, 23);
		contentPane.add(btnOK);
		
		setVisible(true);
		
		//Execute the search for the .pst file in another thread
		PSTSearchRunnable search = new PSTSearchRunnable(this);
		Thread t = new Thread(search);
		t.start();
	}
	
	/**
	 * Fires when the user has selected a .pst from the list
	 *
	 */
	private class BTNOKActionListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent arg0) {
			//Write pstLocation to settings.ini
			try {
				new SettingsIO().setSettingsField(SettingsIO.PST_LOCATION, listPST.getSelectedValue());
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
	
	private class PSTSearchRunnable implements Runnable {
		private PSTSearchFrame pstSearchFrame;

		public PSTSearchRunnable(PSTSearchFrame pstSearchFrame) {
			this.pstSearchFrame = pstSearchFrame;
		}

		@Override
		public void run() {
			//Search for *.pst on c:\
			Path startingDir = Paths.get("c:\\");
			String pattern = "*.pst";
			Finder finder = new Finder(pattern);
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
					new SettingsIO().setSettingsField(SettingsIO.PST_LOCATION, results.get(0).toString());
				} catch (IOException e) {
					// TODO Use IOException frame to prevent exiting
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
				
				//Make visible the list box, label, and ok button
				scrollPane.setVisible(true);
				listPST.setVisible(true);
				lblMultiple.setVisible(true);
				btnOK.setVisible(true);

				//Add the results to listPST
				DefaultListModel<String> listModel = new DefaultListModel<>();
				for(int x = 0; x < results.size(); x++) {
					listModel.addElement(results.get(x).toString());
				}
				listPST.setModel(listModel);
			} else {
				//No results found
				lblNoPST.setVisible(true);
				lblSearchingForPST.setVisible(false);
			}		
		}
	}
}
