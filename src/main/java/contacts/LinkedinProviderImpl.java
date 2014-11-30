package contacts;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.scribe.builder.api.LinkedInApi;
import org.scribe.model.OAuthRequest;
import org.scribe.model.Response;
import org.scribe.model.Verb;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

public class LinkedinProviderImpl extends BaseOAuthProvider implements LinkedinProvider {

	public static void main(String[] args) throws Exception {
		LinkedinProviderImpl instance = new LinkedinProviderImpl();
		instance.getPersons();
	}

	public LinkedinProviderImpl() {
		super(LinkedInApi.class, System.getProperty("linkedin.apiKey"),System.getProperty("linkedin.apiSecret"));
	}

	public Collection<Person> getPersons() throws Exception {
		ArrayList<Person> result = new ArrayList<Person>();
		OAuthRequest request = new OAuthRequest(Verb.GET,
				"http://api.linkedin.com/v1/people/~/connections:(id,first-name,last-name,formatted-name,api-standard-profile-request)");
		service.signRequest(getToken(), request);
		Response response = request.send();
		DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		Document document = db.parse(new InputSource(new StringReader(response.getBody())));
		NodeList persons = document.getElementsByTagName("person");
		for (int i = 0; i < persons.getLength(); i++) {
			Person person = new Person();
			Element elem = (Element) persons.item(i);
			NodeList lastname = elem.getElementsByTagName("last-name");
			if (lastname.getLength() > 0) {
				person.setLastname(lastname.item(0).getFirstChild().getNodeValue());
			}
			result.add(person);
		}
		return result;
	}

}
