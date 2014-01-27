package nf.co.korshyadoo.calendar;

public abstract class Writer {
	
	/**
	 * If the passed String is only 1 character long, it is returned with a "0" appended to the
	 * beginning. Otherwise, a substring of the first two characters is returned. If the passed
	 * String is two characters long, the return will be the same as the passed String. 
	 * @param str
	 * @return A two character version of the passed String. If the passed String is only 1
	 * character long, a "0" is appended to the beginning. 
	 */
	protected static String twoDigit(String str) {
		String result = "0" + str;
		if(str.length() == 1) {
			return result;
		} else {
			return str.substring(0, 2);
		}
	}
}
