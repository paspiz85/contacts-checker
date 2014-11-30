package contacts.warning;

public class FacebookFriendNotFound extends Warning {

	public FacebookFriendNotFound(String name, String url) {
		super("facebook friend not found : " + name + " : " + url);
	}

}
