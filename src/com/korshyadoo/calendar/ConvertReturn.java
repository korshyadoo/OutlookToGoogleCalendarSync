package com.korshyadoo.calendar;

import com.google.gdata.data.calendar.CalendarEventEntry;

public class ConvertReturn {
	private CalendarEventEntry cee;
	private boolean nullTZ;
	
	public ConvertReturn() {
		nullTZ = false;
	}
	public ConvertReturn(CalendarEventEntry c) {
		cee = c;
		nullTZ = false;
	}
	public ConvertReturn(CalendarEventEntry c, boolean n) {
		cee = c;
		nullTZ = n;
	}
	
	public boolean getNullTZ() { return nullTZ; }
	public CalendarEventEntry getCEE() { return cee; }
}
