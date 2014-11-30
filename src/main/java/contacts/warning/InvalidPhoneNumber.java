package contacts.warning;

public class InvalidPhoneNumber extends Warning {

	public InvalidPhoneNumber(String name, String phoneNumber) {
		super("invalid phone number : " + name + " : " + phoneNumber);
	}

}
