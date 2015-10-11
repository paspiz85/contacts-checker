package it.paspiz85.contacts.warning;

public class NoOrganizationEmailAddress extends Warning {

	public NoOrganizationEmailAddress(String name, String orgName) {
		super("no organization email address : " + name + " : " + orgName);
	}

}
