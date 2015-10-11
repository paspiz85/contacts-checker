package it.paspiz85.contacts.logic;

import it.paspiz85.contacts.warning.Warning;

import com.google.gdata.data.contacts.ContactEntry;

public abstract class Plugin implements Constants {

	private Context context;


	public void run(Context context) {
		this.context = context;
	}
	
	protected final void report(ContactEntry contact, Warning warning) {
		context.report(contact, warning);
	}

	
	protected final void reportChange(ContactEntry contact, String message) {
		context.reportChange(contact, message);
	}


}
