package nf.co.korshyadoo.calendar;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
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
import javax.swing.JTextField;
import javax.swing.LookAndFeel;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import nf.co.korshyadoo.dataIo.DataIo;


/**
 * Frame used for searching for the .pst file. 
 * @author korshyadoo
 *
 */
public class PstSearchFrame extends JFrame {
	private static final long serialVersionUID = 1L;
	private JList<String> listPST;
	private JScrollPane scrollPane;
	private JButton btnOK;
	private JLabel lblMultiple;
	private JLabel lblNoPST;
	private JLabel lblSearchingDir;


	/**
	 * Create the frame.
	 */
	public PstSearchFrame() {
		createAndShowGui();

		pack();
		setVisible(true);

	}

	public PstSearchFrame(String title) {
		setTitle(title);
		setResizable(false);
		createAndShowGui();

		pack();
		setVisible(true);
	}

	private void createAndShowGui() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		//Create content pane
		MyContentPane contentPane = new MyContentPane(700, 50);
		setContentPane(contentPane);

		JPanel mainPanel = new JPanel(new GridBagLayout());
		contentPane.add(mainPanel, BorderLayout.CENTER);

		//In order to centre the text, a JTextField has to be used and made to look like a label.
		//But, in order for the background to look right, the JTextField has to be created in the
		//Metal LookAndFeel
		LookAndFeel previousLF = UIManager.getLookAndFeel();
		JTextField[] txtSearchingForPst = new JTextField[2];
		try {
			UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
			txtSearchingForPst[0] = new JTextField("Searching for Outlook.pst file...");
			txtSearchingForPst[1] = new JTextField("This may take a few minutes");
			UIManager.setLookAndFeel(previousLF);
		} catch (IllegalAccessException | UnsupportedLookAndFeelException | InstantiationException | ClassNotFoundException e) {
			//TODO log the exception
		}
		for(JTextField tf : txtSearchingForPst) {
			tf.setBorder(null);
			tf.setEditable(false);
			tf.setCursor(null);
			tf.setOpaque(false);
			tf.setFocusable(false);
			tf.setHorizontalAlignment(JTextField.CENTER);
			Font font = tf.getFont();
			Font bold = new Font(font.getName(), Font.BOLD, font.getSize());
			tf.setFont(bold);
		}
		GridBagConstraints constraints1 = new GridBagConstraints();
		constraints1.fill = GridBagConstraints.HORIZONTAL;
		constraints1.weightx = 1.0;
		constraints1.weighty = 1.0;
		constraints1.gridx = 3;				//x location of component
		constraints1.gridy = 2;				//y location of component
		constraints1.gridwidth = 3;			//number of columns wide this component is
		constraints1.gridheight = 4;			//number of rows high this component is
		mainPanel.add(txtSearchingForPst[0], constraints1);
		constraints1.gridy = 5;
		mainPanel.add(txtSearchingForPst[1], constraints1);

		lblSearchingDir = new JLabel();
		GridBagConstraints constraints2 = new GridBagConstraints();
		constraints2.fill = GridBagConstraints.HORIZONTAL;
		constraints2.weightx = 0.5;
		constraints2.weighty = 0.5;
		constraints2.gridx = 2;				//x location of component
		constraints2.gridy = 7;				//y location of component
		constraints2.gridwidth = 6;			//number of columns wide this component is
		constraints2.gridheight = 2;			//number of rows high this component is
		mainPanel.add(lblSearchingDir, constraints2);

		setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

		//		Execute the search for the .pst file in another thread
		PSTSearchRunnable search = new PSTSearchRunnable();
		Thread t = new Thread(search);
		t.start();
	}



	//Fires when the ok button is pressed after the user selects a .pst file from the list
	private class BTNOKActionListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent arg0) {
			if(!(listPST.getSelectedIndex() == -1)) {					//If a selection has been made from the list
				//Write pstLocation to data source and update PffPstUtility
				DataIo dataIO = ProgramLauncher.getDataIo();
				dataIO.setPstLocation(listPST.getSelectedValue());
				PstUtility.getInstance(listPST.getSelectedValue());
				PstUtility.setSearching(false);

//				//Run MainFrame
//				EventQueue.invokeLater(new Runnable() {
//					@Override
//					public void run() {
//						new MainFrame();
//					}
//				});

				//Close this frame
				PstSearchFrame.this.dispose();
			}
		}
	}

	private void createListContentPane() {
		MyContentPane contentPane = new MyContentPane(600, 200);
		this.setContentPane(contentPane);
		JPanel mainPanel = new JPanel(new GridBagLayout());
		contentPane.add(mainPanel, BorderLayout.CENTER);

		lblMultiple = new JLabel("Multiple .pst files found. Please choose one:");
		GridBagConstraints constraints1 = new GridBagConstraints();
		constraints1.fill = GridBagConstraints.NONE;
		constraints1.weightx = 0.5;
		constraints1.weighty = 0.5;
		constraints1.gridx = 5;				//x location of component
		constraints1.gridy = 0;				//y location of component
		constraints1.gridwidth = 400;			//number of columns wide this component is
		constraints1.gridheight = 1;			//number of rows high this component is
		mainPanel.add(lblMultiple, constraints1);



		scrollPane = new JScrollPane();
		GridBagConstraints constraints2 = new GridBagConstraints();
		constraints2.fill = GridBagConstraints.HORIZONTAL;
		constraints2.weightx = 1;
		constraints2.weighty = 1;
		constraints2.gridx = 0;				//x location of component
		constraints2.gridy = 4;				//y location of component
		constraints2.gridwidth = 10;			//number of columns wide this component is
		constraints2.gridheight = 10;			//number of rows high this component is
		mainPanel.add(scrollPane, constraints2);

		listPST = new JList<String>();
		scrollPane.setViewportView(listPST);




		btnOK = new JButton("oK");
		btnOK.addActionListener(new BTNOKActionListener());
		GridBagConstraints constraints4 = new GridBagConstraints();
		constraints4.fill = GridBagConstraints.NONE;
		constraints4.weightx = 0.5;
		constraints4.gridx = 5;				//x location of component
		constraints4.gridy = 16;				//y location of component
		constraints4.gridwidth = 2;			//number of columns wide this component is
		constraints4.gridheight = 1;			//number of rows high this component is
		mainPanel.add(btnOK, constraints4);

		contentPane.revalidate();
		contentPane.repaint();
		this.pack();


	}

	private void createErrorPane() {
		MyContentPane contentPane = new MyContentPane(300, 300);
		this.setContentPane(contentPane);
		JPanel mainPanel = new JPanel(new BorderLayout());
		contentPane.add(mainPanel, BorderLayout.CENTER);

		lblNoPST = new JLabel("<html><center>No .pst file was found.<br>Please ensure Microsoft Outlook is installed.");
		mainPanel.add(lblNoPST, BorderLayout.CENTER);

		contentPane.revalidate();
		contentPane.repaint();
	}

	/**
	 * A JPanel that is used as the content pane for this JFrame. It always has a BorderLayout
	 * and contains four JPanels that act as a border. The width and height of the bordering
	 * panels are set by the arguments passed to the constructor
	 * @author korshyadoo
	 *
	 */
	private class MyContentPane extends JPanel {
		private static final long serialVersionUID = -7177768318232997951L;

		public MyContentPane(int width, int height) {
			super(new BorderLayout());

			//EAST
			JPanel eastGap = new JPanel();
			eastGap.setPreferredSize(new Dimension(20, height));
			this.add(eastGap, BorderLayout.EAST);

			//WEST
			JPanel westGap = new JPanel();
			westGap.setPreferredSize(new Dimension(20, height));
			this.add(westGap, BorderLayout.WEST);

			//NORTH
			JPanel northGap = new JPanel();
			northGap.setPreferredSize(new Dimension(width, 20));
			this.add(northGap, BorderLayout.NORTH);

			//SOUTH
			JPanel southGap = new JPanel();
			southGap.setPreferredSize(new Dimension(width, 20));
			this.add(southGap, BorderLayout.SOUTH);
		}
	}

	private class PSTSearchRunnable implements Runnable {
		//Could have this return a List. null if no results, size 1 if 1 result, size > 1 if more than 1 result.
		//Then, this could be put into the PSTInterface and call from the frame.
		//Also, work on building the frame without extending JFrame.

		//Default constructor
		public PSTSearchRunnable() {
		}

		private List<Path> userSearch() {
			Path startingDir = Paths.get("c:\\users\\");
			String pattern = "*.pst";
			Finder finder = new Finder(pattern, PstSearchFrame.this);
			try {
				Files.walkFileTree(startingDir, finder);
				System.out.println();
			} catch (IOException e1) {
				JOptionPane.showMessageDialog(null,"IOException when searching for .pst file");
				System.exit(0);
			}
			return finder.getResults();
		}

		private List<Path> rootSearch() {
			Path startingDir = Paths.get("c:\\");
			String pattern = "*.pst";
			Finder finder = new Finder(pattern, PstSearchFrame.this);
			try {
				Files.walkFileTree(startingDir, finder);
			} catch (IOException e1) {
				JOptionPane.showMessageDialog(null,"IOException when searching for .pst file");
				System.exit(0);
			}
			return finder.getResults();
		}

		@Override
		public void run() {
			//Search for *.pst in "c:\ users"
			List<Path> results = userSearch();

			if(!checkSearchResults(results)) {
				//No results found. Try full search of C:\
				results = rootSearch();
				if(!checkSearchResults(results)) {
					//No results found. Display error message
					setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
					createErrorPane();
				}
			}


		}

		private boolean checkSearchResults(List<Path> results) {
			//Check the results
			if(results.size() == 1) {
				//Only one pst file found

				//Write pstLocation to data source and update PffPstUtility
				DataIo dataIO = ProgramLauncher.getDataIo();
				dataIO.setPstLocation(results.get(0).toString());
				PstUtility.getInstance(results.get(0).toString());

				PstUtility.setSearching(false);
				PstSearchFrame.this.dispose();
				return true;
			} else if(results.size() > 1) {
				//More than one .pst found. Have the user choose one

				//Change mouse cursor back to default and switch the contentpane
				//to display the list box with the list of pst files found 
				setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
				System.out.println("create content pane");
				createListContentPane();

				//Add the results to listPST
				DefaultListModel<String> listModel = new DefaultListModel<>();
				for(int x = 0; x < results.size(); x++) {
					listModel.addElement(results.get(x).toString());
				}
				listPST.setModel(listModel);
				return true;
			} else {
				//No results found
				
				return false;
			}	
		}
	}


	public void setLBLSearchingFileText(String text) {
		lblSearchingDir.setText(text);
	}
}
