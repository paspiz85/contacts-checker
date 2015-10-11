package it.paspiz85.contacts.logic;

import it.paspiz85.contacts.warning.DuplicatePrimaryEmailAddress;
import it.paspiz85.contacts.warning.NoEmailAddressRel;
import it.paspiz85.contacts.warning.NoPrimaryEmailAddress;

import java.util.List;

import com.google.gdata.data.contacts.ContactEntry;
import com.google.gdata.data.extensions.Email;

public class EmailPlugin extends Plugin {
	
	private boolean checkEmails(ContactEntry contact, String name,
			boolean isMyContact) {
		boolean changed = false;
		boolean asPrimary = false;
		List<Email> emailAddresses = contact.getEmailAddresses();
		for (Email e : emailAddresses) {
			String address = e.getAddress();
			String provider = address.substring(address.indexOf("@"));
			if (e.getRel() == null) {
				if (EMAIL_HOME_PROVIDERS.contains(provider)) {
					e.setRel(GENERIC_HOME_REL);
					e.setLabel(null);
					changed = true;
					reportChange(contact, "email : " + name + " : " + address);
				} else if (EMAIL_WORK_PROVIDERS.contains(provider)) {
					e.setRel(GENERIC_WORK_REL);
					e.setLabel(null);
					changed = true;
					reportChange(contact, "email : " + name + " : " + address);
				} else {
					report(contact, new NoEmailAddressRel(name, address));
				}
			} else if (EMAIL_HOME_PROVIDERS.contains(provider)) {
				if (!e.getRel().equals(GENERIC_HOME_REL)) {
					e.setRel(GENERIC_HOME_REL);
					changed = true;
					reportChange(contact, "email address home rel : " + name
							+ " : " + address);
				}
			} else if (EMAIL_WORK_PROVIDERS.contains(provider)) {
				if (!e.getRel().equals(GENERIC_WORK_REL)) {
					e.setRel(GENERIC_WORK_REL);
					changed = true;
					reportChange(contact, "email address work rel : " + name
							+ " : " + address);
				}
			}
			if (e.getPrimary()) {
				if (asPrimary) {
					report(contact, new DuplicatePrimaryEmailAddress(name,
							address));
				} else {
					asPrimary = true;
				}
			}
		}
		if (isMyContact && emailAddresses.size() > 1 && !asPrimary) {
			report(contact, new NoPrimaryEmailAddress(name));
		}
		return changed;
	}
	

	@Override
	public void run(Context context) {
		super.run(context);
		context.addChange(checkEmails(context.getContact(), context.getName(), Boolean.parseBoolean(context.get(Context.IS_MY_CONTACT))));
	}



}
