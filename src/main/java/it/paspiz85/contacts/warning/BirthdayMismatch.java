package it.paspiz85.contacts.warning;

public class BirthdayMismatch extends Warning {

	public BirthdayMismatch(String name, Object contactDate, Object calendarDate) {
		super("birthday mismatch : " + name + " : " + contactDate + " VS " + calendarDate);
	}

}
