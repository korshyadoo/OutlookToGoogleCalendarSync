===============================
Outlook to Google Calendar Sync
===============================

This application can be run from the runnable jar OutlookToGoogleCalendarSync.jar.

This is an application I wrote to demonstrate my abilities as a Java developer. It synchronizes appointments from a Microsoft Outlook calendar to a Google calendar. All appointments in the Outlook calendar that do not exist in the Google calendar are added, and any appointments in the Google calendar that do not exist in the Outlook calendar are deleted. It only works in one direction; the Outlook calendar is never modified. Currently, it is only compatible with Outlook 2010, but will be updated to also work with Outlook 2003 (and possibly others) at some point. If the .pst file (the file used by Outlook to store email and calendar information) is located on a drive other than the "C:" drive, the application will not be able to find it. This will be corrected in a future build.

If you are using the test Google account provided to you by me, please be aware that others do have access to that account so calendar data may change unexpectedly. 

When the sync button is pressed, the Google calendar is queried to retrieve an ArrayList containing all appointments with a start time six months prior to run time and 100 years ahead. Then, each appointment in the .pst file is compared to each appointment in the query ArrayList to look for matches. If an appointment from the .pst is found in the query ArrayList, it is removed from the ArrayList. If a .pst appointment is not in the query ArrayList, it is added to a second, insert queue, ArrayList. When all .pst files in the time frame have been checked, a batch request is sent to Google to delete all appointments remaining in the query ArrayList and insert all appointments in the insert queue ArrayList.

The application currently does not support appointments with recurrence. The first appointment will synchronize, but no subsequent appointments in the series will. 

Depending on the number of events in the Google calendar, the delete all events function can take upwards of 20 minutes to complete.

In its current form, there are potential security problems with the way the password is handled. It is stored in encrypted form in the settings.ini file. However, during runtime it is stored unencrypted in memory as a java.lang.String instead of the preferred char array, which could pose a security problem. I am working towards resolving this issue, but in the meantime, if you use your own Google account you do so at your own risk. Also, the password text box always shows the password being typed; a proper password box will be implemented in the future. 

It is important to note that if an appointment is created in Outlook and synchronization is performed immediately afterwards, it may not appear in the Google calendar because Outlook does not immediately write to the .pst file upon alteration of the calendar. Closing Outlook or waiting approximately thirty seconds after creating an appointment will ensure it synchronizes properly. 