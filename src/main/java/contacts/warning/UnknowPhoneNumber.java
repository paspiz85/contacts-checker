package contacts.warning;

public class UnknowPhoneNumber extends Warning {

	public UnknowPhoneNumber(String name, String phoneNumber) {
		super("unknow phone number : " + name + " : " + phoneNumber);
	}

}
