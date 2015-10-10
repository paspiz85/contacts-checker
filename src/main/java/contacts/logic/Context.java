package contacts.logic;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import com.google.gdata.data.contacts.ContactEntry;

import contacts.warning.Warning;

public final class Context extends HashMap<String, String> {

	@Deprecated
	public static final String IS_MY_CONTACT = "isMyContact";

	private static final long serialVersionUID = 1L;
	


	public String getName() {
		return contact.hasName() ? contact.getName()
				.getFullName().getValue() : "";
	}
	
	private ContactEntry contact;
	
	public ContactEntry getContact() {
		return contact;
	}

	public void setContact(ContactEntry contact) {
		this.contact = contact;
		this.changed = false;
	}

	private boolean changed;

	public boolean isChanged() {
		return changed;
	}

	public void addChange(boolean changed) {
		this.changed |= changed;
	}

	public void report(ContactEntry contact, Warning warning) {
		String className = warning.getClass().getName();
		if (!("ignore".equalsIgnoreCase(System.getProperty(className)))) {
			// printXml(contact);
			System.out.println("WARN: " + warning.getMessage());
		}
	}
	
	public void reportChange(ContactEntry contact, String message) {
		System.out.println("CHANGE " + message);
	}

}
