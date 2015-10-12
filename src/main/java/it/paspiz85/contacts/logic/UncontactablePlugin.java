package it.paspiz85.contacts.logic;

import it.paspiz85.contacts.warning.Uncontactable;

import com.google.gdata.data.contacts.ContactEntry;

public class UncontactablePlugin extends Plugin {
	

	@Override
	public void run(Context context) {
		super.run(context);
		ContactEntry contact = context.getContact();
		if (contact.getPhoneNumbers().isEmpty()
				&& contact.getEmailAddresses().isEmpty()
				&& contact.getImAddresses().isEmpty()) {
			report(contact, new Uncontactable(context.getName()));
		}
	}

}
