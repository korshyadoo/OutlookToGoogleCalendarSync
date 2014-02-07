===============================
Outlook to Google Calendar Sync
===============================

To run: unzip the archive OutlookToGoogleCalendarSync0011.zip to
a folder on your hard drive and run OutlookToGoogleCalendarSync0011.jar.

This is an application I wrote to demonstrate my abilities as a 
Java developer. It synchronizes appointments from a Microsoft 
Outlook calendar to a Google calendar. All appointments in the 
Outlook calendar that do not exist in the Google calendar are 
added, and any appointments in the Google calendar that do not 
exist in the Outlook calendar are deleted. It only works in one 
direction; the Outlook calendar is never modified. Currently, it 
is only compatible with Outlook 2010 on Windows.

If the Outlook.pst file (the file used by Outlook to store email 
and calendar information) is located on a drive other than the 
"C:" drive, the application will not be able to find it. This 
will be corrected in a future build.

When the sync button is pressed, the Google calendar is queried
to retrieve the appointments in a given time range. The time
range can be adjusted by editing properties.properties. This list
of appointments is compared to the appointments found in 
Outlook.pst and synchronized so that what's in the Google
calendar matches what is found in the Outlook.pst file.

When an event in the Google calendar is to be deleted because it 
was not found in the Outlook calendar and its creation date is 
after the last synchronization date, a VCard file is created so 
that it can be easily added to the Outlook calendar. Also, if 
an event is missing from the Google calendar and its creation date 
in Outlook is before the last synchronization time, a VCard with a 
green category is created to indicate that it should be deleted 
from the Outlook calendar.

The application currently does not support appointments with 
recurrence. The first appointment will synchronize, but no 
subsequent appointments in the series will. 

It is important to note that if an appointment is created in 
Outlook and synchronization is performed immediately afterwards, 
it may not appear in the Google calendar because Outlook does 
not immediately write to the .pst file upon alteration of the 
calendar. Closing Outlook or waiting approximately thirty 
seconds after creating an appointment will ensure it 
synchronizes properly. 

If you are using the test Google account provided to you by me, 
please be aware that others do have access to that account so 
calendar data may change unexpectedly. 

The project uses Google Calendar API, the Jasypt library, the 
Spring Framework, and the java-libpst library, which can be 
found at the respective websites:

https://developers.google.com/google-apps/calendar/
http://www.jasypt.org
https://github.com/rjohnsondev/java-libpst
http://projects.spring.io/spring-framework/