package contacts.logic;

import com.google.gdata.data.contacts.ContactEntry;

import contacts.warning.Warning;

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
