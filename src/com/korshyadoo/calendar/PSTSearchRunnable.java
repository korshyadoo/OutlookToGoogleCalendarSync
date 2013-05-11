package com.korshyadoo.calendar;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

public class PSTSearchRunnable implements Runnable {
	private JFrame parent;
	
	public PSTSearchRunnable(JFrame jf) {
		parent = jf;
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
			
			
			//Set pstLocation field
			OutlookToGoogleCalendarSync.pstLocation = results.get(0).toString();
			
			System.out.println(OutlookToGoogleCalendarSync.pstLocation);
			
			//Write pstLocation to settings.ini for future executions
			OutlookToGoogleCalendarSync.setSettings("pstLocation", OutlookToGoogleCalendarSync.pstLocation);

			//Run FirstRunFrame
			java.awt.EventQueue.invokeLater(new Runnable() {
				@Override
				public void run() {
					new FirstRunFrame().setVisible(true);
				}
			});
			parent.dispose();
		} else if(results.size() > 1) {
			//More than one .pst found. Have the user choose one
			((PSTSearchFrame)parent).getLBLMultiple().setVisible(true);
			((PSTSearchFrame)parent).getScrollPane().setVisible(true);
			((PSTSearchFrame)parent).getListPST().setVisible(true);
			((PSTSearchFrame)parent).getBTNOK().setVisible(true);
			
			//Add the results to listPST
			DefaultListModel<String> listModel = new DefaultListModel<>();
			for(int x = 0; x < results.size(); x++) {
				listModel.addElement(results.get(x).toString());
			}
			((PSTSearchFrame)parent).getListPST().setModel(listModel);
		} else {
			//No results found
			((PSTSearchFrame)parent).getLBLNoPST().setVisible(true);
			((PSTSearchFrame)parent).getLBLSearchingForPST().setVisible(false);
		}		
	}
}
