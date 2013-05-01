package com.korshyadoo.calendar;

import java.awt.EventQueue;
import java.nio.file.Path;
import java.util.List;

import javax.swing.AbstractListModel;
import javax.swing.DefaultListModel;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.EmptyBorder;
import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

public class ChoosePSTFrame extends JFrame {

	private JPanel contentPane;
	List<Path> paths;
	private JList<String> listPST;

	/**
	 * Create the frame.
	 */
	public ChoosePSTFrame() {
		paths = OutlookToGoogleCalendarSync.getPSTResults();
		
		setTitle("Choose a .pst file");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 450, 300);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		JLabel lblMultiplepstFiles = new JLabel("Multiple .pst files found. Please choose one:");
		lblMultiplepstFiles.setBounds(97, 54, 211, 14);
		contentPane.add(lblMultiplepstFiles);
		
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBounds(67, 79, 313, 99);
		contentPane.add(scrollPane);
		
		//Create a JList with the list of paths
		DefaultListModel<String> listModel = new DefaultListModel<>();
		for(int x = 0; x < paths.size(); x++) {
			listModel.addElement(paths.get(x).toString());
		}
		listPST = new JList<>(listModel);

		scrollPane.setViewportView(listPST);
		
		JButton btnOk = new JButton("oK");
		btnOk.addActionListener(new BTNOkActionListener());
		btnOk.setBounds(186, 209, 89, 23);
		contentPane.add(btnOk);
	}
	
	private class BTNOkActionListener implements ActionListener {
		public void actionPerformed(ActionEvent arg0) {
			btnOkActionPerformed(arg0);
		}
	}
	
	public void btnOkActionPerformed(ActionEvent evt) {
		OutlookToGoogleCalendarSync.setPSTLocation(listPST.getSelectedValue());
	}
}
