package contacts.warning;

public class NoEmailAddressRel extends Warning {

	public NoEmailAddressRel(String name, String emailAddress) {
		super("no email address rel : " + name + " : " + emailAddress);
	}

}
