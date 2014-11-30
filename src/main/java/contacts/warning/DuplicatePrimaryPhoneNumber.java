package contacts.warning;

public class DuplicatePrimaryPhoneNumber extends Warning {

	public DuplicatePrimaryPhoneNumber(String name, String phoneNumber) {
		super("duplicate primary phone number : " + name + " : " + phoneNumber);
	}

}
