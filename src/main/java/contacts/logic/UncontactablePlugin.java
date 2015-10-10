package contacts.logic;

import com.google.gdata.data.contacts.ContactEntry;

import contacts.warning.Uncontactable;

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
