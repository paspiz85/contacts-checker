package it.paspiz85.contacts.warning;

public class OrganizationMismatch extends Warning {

	public OrganizationMismatch(String name, String org, String orgName, String orgTitle) {
		super("organization mismatch : " + name + " : " + org + " VS " + orgName + " (" + orgTitle+")");
	}

}
