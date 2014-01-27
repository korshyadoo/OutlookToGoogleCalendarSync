package nf.co.korshyadoo.calendar;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.SocketTimeoutException;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.google.api.client.auth.oauth2.TokenResponseException;

import nf.co.korshyadoo.dataIo.DataIo;


public class LoginFrame extends JFrame {
	private static final long serialVersionUID = -7348926732389986413L;
	
	private static boolean running;
	private JTextField txtAuthCode;
	private JTextField txtUsername;
	private String username;					//The username entered by the user
	private String authUrl;						//The URL used to obtain the authorization code
	private MainFrame mainFrame;				//A reference to the calling MainFrame




	public LoginFrame() {
		this("");
	}
	
	public LoginFrame(String title) {
		this(title, null);
	}
	
	public LoginFrame(MainFrame mf) {
		this("", mf);
	}
	
	public LoginFrame(String title, MainFrame mf) {
		mainFrame = mf;
		running = true;
		setTitle(title);
		createAndShowGui();
		
		pack();
		setVisible(true);
	}
	
	
	protected void createAndShowGui() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 411, 301);
		setContentPane(createUsernamePanel());
	}
	
	/**
	 * Create a main panel with BorderLayout. It contains three panels: west and east are used to create gaps
	 * on the right left sides of the panel, and usernamePanel holds all the components. The panel
	 * contains a label that prompts the user to enter their username, a text field for entering 
	 * the username, and an ok button.
	 * @return
	 */
	protected JPanel createUsernamePanel() {
		JPanel mainPanel = new JPanel(new BorderLayout());
		JPanel westGap = new JPanel();
		westGap.setPreferredSize(new Dimension(50, 50));
		mainPanel.add(westGap, BorderLayout.WEST);
		JPanel eastGap = new JPanel();
		eastGap.setPreferredSize(new Dimension(50, 50));
		mainPanel.add(eastGap, BorderLayout.EAST);
		
		JPanel usernamePanel = new JPanel();
		usernamePanel.setLayout(new BoxLayout(usernamePanel, BoxLayout.Y_AXIS));
		
		JLabel lblUsername = new JLabel("Enter your Google Username");
		lblUsername.setAlignmentX(Component.CENTER_ALIGNMENT);
		usernamePanel.add(lblUsername);
		
		txtUsername = new JTextField();
		txtUsername.setAlignmentX(Component.CENTER_ALIGNMENT);
		usernamePanel.add(txtUsername);
		
		JButton btnUsername = new JButton("oK");
		btnUsername.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				try {
					username = txtUsername.getText();						//Store the username for later use  

					//If a username was entered, create a GoogleCalendarV3Utility object and switch 
					//to the enterAuthPanel
					if(!username.equals("")) {
						//Create a new calendar utility with the provided username
						GoogleCalendarV3Utility calendarUtil = new GoogleCalendarV3Utility(username);
						ProgramLauncher.setCalendarUtility(calendarUtil);
						DataIo dataIo = ProgramLauncher.getDataIo();
						if(dataIo.hasUser(username)) {
							calendarUtil.setAccessToken(dataIo.getField(username, Fields.REFRESH_TOKEN));
							dataIo.setDefaultUser(username);
							running = false;
							if(mainFrame != null) {
								mainFrame.getLblUsername().setText(username);
								mainFrame.setVisible(true);
							}
							LoginFrame.this.dispose();
						} else {
							//Change JPanels
							LoginFrame.this.setTitle("Enter Authorization Code");
							LoginFrame.this.setContentPane(createEnterAuthPanel());
							LoginFrame.this.pack();
							LoginFrame.this.setVisible(true);
						}
					} else {
						//There was no username entered. Put the focus back on the text field
						txtUsername.requestFocus();
					}
				} catch (SocketTimeoutException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (TokenResponseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
		usernamePanel.add(btnUsername);
		
		mainPanel.add(usernamePanel, BorderLayout.CENTER);
		
		return mainPanel;
	}
	
	
	/**
	 * Create a main panel with BorderLayout. It contains three panels: west and east are used to create gaps
	 * on the right left sides of the panel, and usernamePanel holds all the compoenents. The panel contains
	 * a label prompting the user to go to a URL, a label containing the URL, a text field for entering
	 * the auth code, and an ok button
	 * @return
	 */
	protected JPanel createEnterAuthPanel() {
		JPanel mainPanel = new JPanel(new BorderLayout());
		JPanel westGap = new JPanel();
		westGap.setPreferredSize(new Dimension(10, 100));
		mainPanel.add(westGap, BorderLayout.WEST);
		JPanel eastGap = new JPanel();
		eastGap.setPreferredSize(new Dimension(10, 100));
		mainPanel.add(eastGap, BorderLayout.EAST);
		
		JPanel enterAuthPanel = new JPanel();
		enterAuthPanel.setLayout(new BoxLayout(enterAuthPanel, BoxLayout.Y_AXIS));
		
		JLabel lblPromptA = new JLabel("<html><b>Go to the following webpage address in your web browser: </html>");
		enterAuthPanel.add(lblPromptA);
		
		JLabel lblLineBreakA = new JLabel(" ");
		enterAuthPanel.add(lblLineBreakA);
		
		//Build the string that holds the authorization URL with line breaks
		authUrl = ProgramLauncher.getCalendarUtility().getAuthorizationUrl();
		StringBuilder formatedAuthUrl = new StringBuilder(authUrl);
		for(int i = 100; i < formatedAuthUrl.length(); i += 100) {
			formatedAuthUrl.insert(i, "<br>");
		}
		formatedAuthUrl.insert(0, "<html>");
		formatedAuthUrl.insert(formatedAuthUrl.length() - 1, "</html>");

		JLabel lblUrl = new JLabel(formatedAuthUrl.toString());
		enterAuthPanel.add(lblUrl);
		
		JLabel lblLineBreakB = new JLabel(" ");
		enterAuthPanel.add(lblLineBreakB);
		
		JLabel lblPromptB = new JLabel("<html><b>And paste the authorization code into this text box.</html>");
		enterAuthPanel.add(lblPromptB);

		//Copy authUrl to Windows clipboard
		StringSelection stringSelection = new StringSelection (authUrl);
		Clipboard clpbrd = Toolkit.getDefaultToolkit ().getSystemClipboard ();
		clpbrd.setContents (stringSelection, null);
		
		//The text field where the user can enter the authorization code
		txtAuthCode = new JTextField();
		txtAuthCode.setPreferredSize(new Dimension(200, (int)txtAuthCode.getPreferredSize().getHeight()));
		enterAuthPanel.add(txtAuthCode);
		
		//Ok button
		JButton btnOk = new JButton("oK");
		btnOk.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				//Retrieve the access and refresh codes from the given auth code
				GoogleCalendarV3Utility googleUtil = ProgramLauncher.getCalendarUtility();
				if(googleUtil.setAccessAndRefreshTokens(LoginFrame.this.txtAuthCode.getText())) {
					//TODO Auth code accepted. Write the new user (containing the refresh token) to 
					//the data source and set as the default user 
					
					DataIo dataIo = ProgramLauncher.getDataIo();
					String[] fields = {Fields.REFRESH_TOKEN};
					String[] values = {googleUtil.getRefreshToken()};
					
					//Create the user in the data source, make it the default user, and set the newly retrieved refresh token
					dataIo.createUser(LoginFrame.this.username, fields, values);
					dataIo.setDefaultUser(LoginFrame.this.username);
					dataIo.setField(LoginFrame.this.username, Fields.REFRESH_TOKEN, googleUtil.getRefreshToken());
					
					running = false;					//Inform the other thread that this panel is finished
					if(mainFrame != null) {
						mainFrame.setVisible(true);
						mainFrame.getLblUsername().setText(username);
					}
					LoginFrame.this.dispose();
				} else {
					JOptionPane.showMessageDialog(null, "Invalid auth code");
					System.exit(5);
				}
			}
		});
		enterAuthPanel.add(btnOk);
		
		//A button used to copy the authUrl to the Windows clipboard
		JButton btnCopy = new JButton("Copy URL to clipboard");
		btnCopy.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				// TODO Auto-generated method stub
				StringSelection stringSelection = new StringSelection (authUrl);
				Clipboard clpbrd = Toolkit.getDefaultToolkit ().getSystemClipboard ();
				clpbrd.setContents (stringSelection, null);
			}
		});
		enterAuthPanel.add(btnCopy);
		
		mainPanel.add(enterAuthPanel);
		
		return mainPanel;
	}

	public static boolean isRunning() {
		return running;
	}

	public static void setRunning(boolean running) {
		LoginFrame.running = running;
	}
}
