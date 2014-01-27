package nf.co.korshyadoo.calendar;

import javax.swing.JOptionPane;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * 
 * @author korshyadoo
 *
 */
public class ApplicationContextFactory {
	private static ApplicationContextFactory applicationContextFactory;
	private ApplicationContext ctx;
	
	private ApplicationContextFactory() {
		try {
			ctx = new ClassPathXmlApplicationContext("dataIo-beans.xml");
		} catch (Exception e) {
			JOptionPane.showMessageDialog(null, e.getMessage());
			StringBuilder error = new StringBuilder();
			StackTraceElement[] elements = e.getStackTrace();
			for(StackTraceElement element : elements) {
				error.append(element.toString() + "\n");
			}
			error.delete(error.length() - 2, error.length());
			JOptionPane.showMessageDialog(null, error);
		}
	}
	
	public static ApplicationContextFactory getInstance() {
		if (applicationContextFactory == null) {
			applicationContextFactory = new ApplicationContextFactory();
		}
		return applicationContextFactory;
	}
	
	public ApplicationContext getApplicationContext() {
		return ctx;
	}
	
}
