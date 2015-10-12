package it.paspiz85.contacts.warning;

public class InvalidPhoneNumberRel extends Warning {

	public InvalidPhoneNumberRel(String name, String phoneNumber, boolean mobile, String rel, String label) {
		super("invalid phone number" + (mobile ? " mobile" : "") + " rel : " + name + " : " + phoneNumber + " : " + rel + " : '" + label+ "'");
	}

}
