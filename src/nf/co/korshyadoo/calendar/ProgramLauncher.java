package nf.co.korshyadoo.calendar;

import java.awt.EventQueue;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

import javax.swing.JOptionPane;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import nf.co.korshyadoo.dataIo.DataIo;
import nf.co.korshyadoo.dataIo.XmlFileIo;

import org.springframework.beans.factory.BeanInitializationException;

import com.google.api.client.auth.oauth2.TokenErrorResponse;
import com.google.api.client.auth.oauth2.TokenResponseException;

public class ProgramLauncher {
	private static DataIo dataIo;
	private static GoogleCalendarV3Utility calendarUtility;
	private static final String DEFAULT_PROPERTIES = "#property=value\n\n" +
			"#The location of the XML file used for storing user information\n" +
			"xmlFileLocation=data.xml\n\n" +
			"#The start date is determined by the number of months back set in monthsBack.\n" +
			"#i.e. If it is currently September and monthsBack=2, the calendar will sync\n" +
			"#appointments starting in July\n" +
			"monthsBack=3\n\n" +
			"#The end date is determined by the number of months forward set in monthsForward.\n" +
			"#i.e. If it is currently September and monthsForward=12, the calendar will sync\n" +
			"#appointments up to August next year.\n" +
			"monthsForward=200\n";
	private static final String DEFAULT_MYSQL_PROPERTIES = "#property=value\n" +
			"database=OutlookToGoogleCalendarSync\n" +
			"databaseHost=jdbc:mysql://localhost:3306/OutlookToGoogleCalendarSync\n" +
			"databaseUsername=root\n" +
			"databasePassword=";

	//Initialize dataIo
	static {
		try {
			ApplicationContextFactory factory = ApplicationContextFactory.getInstance();
			dataIo = (DataIo)factory.getApplicationContext().getBean("dataIo");
		} catch (BeanInitializationException e) {
			JOptionPane.showMessageDialog(null, e.getMessage());
			StringBuilder error = new StringBuilder();
			StackTraceElement[] elements = e.getStackTrace();
			for(StackTraceElement element : elements) {
				error.append(element.toString() + "\n");
			}
			error.delete(error.length() - 2, error.length());
			JOptionPane.showMessageDialog(null, error);
			if(e.getCause() instanceof FileNotFoundException) {
				/*A FileNotFoundException occurred because a file referenced in the bean file was not found 
				(e.g. properties.properties, MySqlConnection.properties, etc.). Attempt to create the file*/
				//The name of the missing file is found between square brackets in the message
				String cause = e.getCause().getMessage();
				int start = cause.indexOf('[') + 1;
				int end = cause.indexOf(']');
				String fileName = cause.substring(start, end);
				System.out.println(fileName + " not found. Recreating...");
				createMissingFile(fileName);
				System.out.println("New file created. Please relaunch the program.");
				JOptionPane.showMessageDialog(null, "A missing file has been recreated. Please re-launch the program");
				System.exit(0);
			}
		}
	}

	//Constructor
	public ProgramLauncher() {

	}

	public static void main(String[] args) {
		boolean failure = false;
		//Set look and feel
		try {
			UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
		} catch (ClassNotFoundException e) {
			System.err.println("Couldn't find class for Nimbus look and feel:");
			failure = true;
			//TODO add error log
		} catch (UnsupportedLookAndFeelException e) {
			System.err.println("Can't use Nimbus look and feel on this platform.");
			failure = true;
			//TODO add error log
		} catch (Exception e) {
			System.err.println("Could change the LookAndFeel for some reason.");
			failure = true;
			//TODO add error log
		} finally {
			if(failure) {
				try {
					UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}


		ProgramLauncher launcher = new ProgramLauncher();
		launcher.serious();
	}
	
	private synchronized void serious() {
		String defaultUserName = dataIo.getDefaultUserName();
		String refreshToken;

		if(defaultUserName != null) {
			//Get user's refresh token
			refreshToken = dataIo.getField(defaultUserName, Fields.REFRESH_TOKEN);

			if(refreshToken != null) {
				//<!--Create calendar service from refresh token-->
				try {
					calendarUtility = new GoogleCalendarV3Utility(defaultUserName, refreshToken);
				} catch (SocketTimeoutException e) {
					e.printStackTrace();
					JOptionPane.showMessageDialog(null, "Timeout trying to reach Google server. Check connection and try again");
					System.exit(0);
				} catch (TokenResponseException e) {
					//If the exception is caused by invalid clientID or client secret, throw IllegalStateException
					TokenErrorResponse tokenErrorResponse = e.getDetails();
					if(tokenErrorResponse != null) {
						if("invalid_client".equals(tokenErrorResponse.getError())) {
							JOptionPane.showMessageDialog(null, "A critical error has occurred. Invalid Google API credentials. Please contact program administrator.");
							throw new IllegalStateException("invalid ClientID or client secrets");
						} else {
							//Invalid refresh token
							//Delete the default user and fun LoginFrame
							dataIo.deleteUser(defaultUserName);
							EventQueue.invokeLater(new Runnable() {
								@Override
								public void run() {
									new LoginFrame();
								}
							});
							
							//Don't continue until the LoginFrame is finished
							int counter = 0;
							while(LoginFrame.isRunning()) {
								try {
									//Every minute, prompt the user to continue
									if(counter % 60 == 0) {
										System.out.println("Continue waiting? (y/n): ");
										BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
										String response = in.readLine();
										if("n".equals(response)) {
											//The user chose to terminate the search
											System.exit(5);
										}
									}
									this.wait(2000);
									counter += 2;
								} catch (InterruptedException | IOException e2) {
									// TODO Auto-generated catch block
									e2.printStackTrace();
								}
							}
						}
					}
					
					
					//TODO what if the loginframe isn't successful?
					//Default user has been changed so retrieve it again and get the refresh token
					defaultUserName = dataIo.getDefaultUserName();
					refreshToken = dataIo.getField(defaultUserName, Fields.REFRESH_TOKEN);
				} catch (UnknownHostException e) {
					e.printStackTrace();
					JOptionPane.showMessageDialog(null, "No internet connection");
					System.exit(6);
				} catch (IOException e) {
					e.printStackTrace();
					JOptionPane.showMessageDialog(null, "IO Exception / " + e.getMessage());
					System.exit(0);
				}
			} else {
				//do nothing
			}
		} else {
			refreshToken = null;
		}

		//If there was no default user found, if there was no refresh token found, or if there was a problem creating the calendar service
		//run the login frame
		if(defaultUserName == null || refreshToken == null || calendarUtility == null) {
			//Login frame is launched and execution doesn't continue until user input is received
			launchLoginFrame();
		}

		String pstLocation = dataIo.getPstLocation();
		PstUtility pstUtility;

		if(pstLocation == null) {
			pstUtility = PstUtility.getInstance();			//This performs a search for the .pst file and launches the PstSearchFrame if one is not found in the default locations

			//Don't launch MainFrame until the pst file was found.
			int counter = 0;
			while(PstUtility.isSearching()) {
				System.out.println("running " + counter);
				counter++;
				try {
					this.wait(2000);
					counter += 2;
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			
			//Searching is finished. If the pstLocation was found, write it to the data source
			if(pstUtility.getPstLocation() == null) {
				//pstLocation not found
				JOptionPane.showMessageDialog(null, "No Outlook calendar file found");
			} else {
				//pstLocation found. Write it to the data source
				dataIo.setPstLocation(pstUtility.getPstLocation());
			}
		} else {
			//Create the pst utility singleton object with the pstLocation from the data source
			PstUtility.getInstance(pstLocation);
		}

		//Run MainFrame
		EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				new MainFrame();
			}
		});


	}
	
	private synchronized void serious2() {
		/////////////////////////////////////////////////////////////////////////////////
		//Retrieve the pst file location and use it to create an instance of PstUtility//
		/////////////////////////////////////////////////////////////////////////////////
		String pstLocation = dataIo.getPstLocation();
		PstUtility pstUtility;
		if(pstLocation == null) {
			pstUtility = PstUtility.getInstance();			//This performs a search for the .pst file and launches the PstSearchFrame if one is not found in the default locations

			//Don't launch MainFrame until the pst file was found.
			int counter = 0;
			while(pstUtility.isSearching()) {
				try {
					//Every minute, prompt the user to continue
					if(counter % 60 == 0) {
						System.out.println("Continue waiting? (y/n): ");
						BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
						String response = in.readLine();
						if("n".equals(response)) {
							//The user chose to terminate the search
							System.exit(5);
						}
					}
					this.wait(2000);
					counter += 2;
				} catch (InterruptedException | IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
			//Searching is finished. Write pst location to the data source
			assert pstUtility.getPstLocation() != null : "PstSearchFrame should have shown an error if the pst location was not found"; 
			dataIo.setPstLocation(pstUtility.getPstLocation());
		} else {
			//Create the pst utility singleton object with the pstLocation from the data source
			PstUtility.getInstance(pstLocation);
		}
		
		////////////////////
		//Get default user//
		////////////////////
		String defaultUserName = dataIo.getDefaultUserName();
		String refreshToken;
		try {
			if(defaultUserName != null) {
				//Get refresh token for default user
				refreshToken = dataIo.getField(defaultUserName, Fields.REFRESH_TOKEN);
				
				if(refreshToken != null) {
					//<!--Create calendar service from refresh token-->
					calendarUtility = new GoogleCalendarV3Utility(defaultUserName, refreshToken);
				}
			} else {
				refreshToken = null;
			}
			
		} catch (SocketTimeoutException e) {
			JOptionPane.showMessageDialog(null, "Timeout trying to reach Google server. Check connection and try again");
			System.exit(0);
		} catch (TokenResponseException e) {
			//Error getting access token or invalid refresh token. Run LoginFrame
			final LoginFrame loginFrame;
			EventQueue.invokeLater(new Runnable() {
				@Override
				public void run() {
//					loginFrame = new LoginFrame();
				}
			});
			
			//Don't continue until the LoginFrame is finished
			int counter = 0;
			while(LoginFrame.isRunning()) {
				try {
					//Every minute, prompt the user to continue
					if(counter % 60 == 0) {
						System.out.println("Continue waiting? (y/n): ");
						BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
						String response = in.readLine();
						if("n".equals(response)) {
							//The user chose to terminate the search
							System.exit(5);
						}
					}
					this.wait(2000);
					counter += 2;
				} catch (InterruptedException | IOException e2) {
					// TODO Auto-generated catch block
					e2.printStackTrace();
				}
			}
			
			//TODO what if the loginframe isn't successful?
			//Default user has been changed so retrieve it again and get the refresh token
			defaultUserName = dataIo.getDefaultUserName();
			refreshToken = dataIo.getField(defaultUserName, Fields.REFRESH_TOKEN);
		} catch (IOException e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(null, "IO Exception");
			System.exit(0);
		}
		
	}
	
	
	
	private static void createMissingFile(String fileName) {
		//Choose what contents to put in the file being created
		String contents;
		switch(fileName) {
		case "properties.properties":
			contents = DEFAULT_PROPERTIES;
			break;
		case "MySqlConnection.properties":
			contents = DEFAULT_MYSQL_PROPERTIES;
			break;
		default:
			contents = "";
			break;
		}
		
		//TODO DEBUG The file has to be put in the bin folder. Because properties.properties has to be read from the bin folder and can't be
		//put in the project root folder for some reason. Not sure how this will work when deploying
		File file = new File("bin\\" + fileName);
		PrintWriter pw = null;
		try {
			file.createNewFile();
			
			//Load the default information into the newly created file
			pw = new PrintWriter(file);
			pw.print(contents);
		} catch (IOException e1) {
			JOptionPane.showMessageDialog(null, "IO exception trying to create " + fileName + ". Closing program.");
			e1.printStackTrace();
		} finally {
			if(pw != null) {
				pw.close();
			}
		}
	}

	
	private void launchLoginFrame() {
		try {
			EventQueue.invokeAndWait(new Runnable(){
				@Override
				public void run() {
					new LoginFrame("Enter username");
				}
			});
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println("***invocation");
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println("***Interrupted");
		}
		System.out.println(LoginFrame.isRunning());

		//Don't continue until the LoginFrame has finished running
		int counter = 0;
		while(LoginFrame.isRunning()) {
			try {
				//Every minute, prompt the user to continue
				if(counter != 0 && counter % 60 == 0) {
					System.out.println("Continue waiting? (y/n): ");
					BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
					String response = in.readLine();
					if("n".equals(response)) {
						//The user chose to terminate the search
						System.exit(5);
					}
				}
				this.wait(2000);
				counter += 2;
			} catch (InterruptedException | IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public void createUser() {
		String[] fields = {"password", "calendarID"};
		String[] values = {"123456", "kremenski"};
		dataIo.createUser("kremenski@gmail.com", fields, values);
	}

	public static void printResultsForXML() {
		System.out.println(((XmlFileIo)ProgramLauncher.dataIo).memoryString);
		System.out.println("=========================");
	}

	public static DataIo getDataIo() {
		return dataIo;
	}

	public static GoogleCalendarV3Utility getCalendarUtility() {
		return calendarUtility;
	}

	public static void setCalendarUtility(GoogleCalendarV3Utility calendarUtility) {
		ProgramLauncher.calendarUtility = calendarUtility;
	}
	
	private void debugPstSearch() {
		try {
			EventQueue.invokeAndWait(new Runnable(){
				@Override
				public void run() {
					new PstSearchFrame("Searching");
				}
			});
		} catch (InvocationTargetException | InterruptedException e) {
			e.printStackTrace();
		}
	}


}
