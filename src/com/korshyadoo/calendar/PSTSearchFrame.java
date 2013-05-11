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
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.EmptyBorder;
import java.awt.Rectangle;

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
		
		lblSearchingForPST = new JLabel("Searching for .pst file...");
		int x = 124;
		lblSearchingForPST.setBounds((int)(this.getBounds().getWidth() / 2) - (x / 2), 32, x, 14);
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
			//Set pstLocation field
			OutlookToGoogleCalendarSync.pstLocation = listPST.getSelectedValue();
			
			//Write pstLocation to settings.ini for future executions
			OutlookToGoogleCalendarSync.setSettings("pstLocation", OutlookToGoogleCalendarSync.pstLocation);

			//Run FirstRunFrame
			java.awt.EventQueue.invokeLater(new Runnable() {
				@Override
				public void run() {
					new FirstRunFrame().setVisible(true);
				}
			});
			PSTSearchFrame.this.dispose();
		}
	}
	
	//Getters
	public JLabel getLBLMultiple() { return lblMultiple; }
	public JLabel getLBLSearchingForPST() { return lblSearchingForPST; }
	public JLabel getLBLNoPST() { return lblNoPST; }
	public JButton getBTNOK() { return btnOK; }
	public JScrollPane getScrollPane() { return scrollPane; }
	public JList<String> getListPST() { return listPST; }
}
