package it.paspiz85.contacts.logic;

import it.paspiz85.contacts.warning.Warning;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import com.google.gdata.data.contacts.ContactEntry;

public final class Context extends HashMap<String, String> {

	public Context() {
		File suppFile = new File("checks_suppressions.txt");
		if (suppFile.exists()) {
			try (BufferedReader reader = new BufferedReader(new FileReader(
					suppFile))) {
				String line = null;
				while ((line = reader.readLine()) != null) {
					suppressions.add(line);
				}
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
	}

	@Deprecated
	public static final String IS_MY_CONTACT = "isMyContact";

	private static final long serialVersionUID = 1L;

	private Set<String> suppressions = new HashSet<String>();

	public String getName() {
		return contact.hasName() ? contact.getName().getFullName().getValue()
				: "";
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
		if (!("ignore".equalsIgnoreCase(System.getProperty(className)) || suppressions
				.contains(warning.getMessage()))) {
			// printXml(contact);
			System.out.println("WARN: " + warning.getMessage());
		}
	}

	public void reportChange(ContactEntry contact, String message) {
		System.out.println("CHANGE " + message);
	}

}
