package contacts.warning;

public class BirthdayWithoutYear extends Warning {

	public BirthdayWithoutYear(String name, Object contactDate) {
		super("birthday without year : " + name + " : " + contactDate);
	}

}
