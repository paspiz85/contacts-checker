package contacts.warning;

public class DuplicatePrimaryEmailAddress extends Warning {

	public DuplicatePrimaryEmailAddress(String name, String emailAddress) {
		super("duplicate primary email address : " + name + " : " + emailAddress);
	}

}
