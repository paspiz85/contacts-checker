package it.paspiz85.contacts.warning;

public class NoPrimaryEmailAddress extends Warning {

	public NoPrimaryEmailAddress(String name) {
		super("no primary email address : " + name);
	}

}
