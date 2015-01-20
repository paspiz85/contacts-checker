package contacts;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URL;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Pattern;

import net.fortuna.ical4j.data.ParserException;
import net.fortuna.ical4j.model.ValidationException;
import net.fortuna.ical4j.vcard.Parameter;
import net.fortuna.ical4j.vcard.Property;
import net.fortuna.ical4j.vcard.Property.Id;
import net.fortuna.ical4j.vcard.VCard;
import net.fortuna.ical4j.vcard.VCardBuilder;
import net.fortuna.ical4j.vcard.VCardOutputter;
import net.fortuna.ical4j.vcard.parameter.Encoding;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gdata.client.Query;
import com.google.gdata.client.Service.GDataRequest;
import com.google.gdata.client.contacts.ContactsService;
import com.google.gdata.data.BaseEntry;
import com.google.gdata.data.ExtensionProfile;
import com.google.gdata.data.Link;
import com.google.gdata.data.contacts.Birthday;
import com.google.gdata.data.contacts.ContactEntry;
import com.google.gdata.data.contacts.ContactFeed;
import com.google.gdata.data.contacts.ContactGroupEntry;
import com.google.gdata.data.contacts.ContactGroupFeed;
import com.google.gdata.data.contacts.ContactLink;
import com.google.gdata.data.contacts.Event;
import com.google.gdata.data.contacts.GroupMembershipInfo;
import com.google.gdata.data.contacts.Website;
import com.google.gdata.data.extensions.Email;
import com.google.gdata.data.extensions.Organization;
import com.google.gdata.data.extensions.PhoneNumber;
import com.google.gdata.util.common.xml.XmlWriter;

import contacts.warning.BirthdayMismatch;
import contacts.warning.BirthdayWithoutYear;
import contacts.warning.DuplicateOrganization;
import contacts.warning.DuplicatePrimaryEmailAddress;
import contacts.warning.DuplicatePrimaryPhoneNumber;
import contacts.warning.FacebookFriendBirthdayNotFound;
import contacts.warning.FacebookFriendNotFound;
import contacts.warning.InvalidPhoneNumber;
import contacts.warning.InvalidPhoneNumberRel;
import contacts.warning.ItalyPrefixPhoneNumber;
import contacts.warning.NoEmailAddressRel;
import contacts.warning.NoGroup;
import contacts.warning.NoOrganizationEmailAddress;
import contacts.warning.NoPhoto;
import contacts.warning.NoPrimaryEmailAddress;
import contacts.warning.NoPrimaryPhoneNumber;
import contacts.warning.NullName;
import contacts.warning.OrganizationMismatch;
import contacts.warning.Uncontactable;
import contacts.warning.UnknowNote;
import contacts.warning.UnknowPhoneNumber;
import contacts.warning.UnknowPhoneNumberLandlinePrefix;
import contacts.warning.Warning;

public class Test implements Runnable {

	private static final SimpleDateFormat BIRTHDAY_DATE_FORMAT = new SimpleDateFormat("MM-dd");

	private static final HashSet<String> EMAIL_HOME_PROVIDERS = new HashSet<String>(Arrays.asList(new String[] { "@fastwebnet.it", "@virgilio.it", "@tim.it",
			"@msn.com", "@live.com", "@ovi.com", "@yahoo.com", "@live.it", "@inwind.it", "@outlook.com", "@tin.it", "@aliceposta.it", "@alice.it", "@yahoo.it",
			"@libero.it", "@gmail.com", "@hotmail.com", "@hotmail.it", "@tiscali.it" }));

	private static final String EMAIL_ORG_ALMAVIVA = "@almaviva.it";

	private static final String EMAIL_ORG_EXPEDIA = "@expedia.com";

	private static final String EMAIL_ORG_INMATICA = "@inmatica.com";

	private static final String EMAIL_ORG_OBJECTWAY = "@objectway.it";

	private static final HashSet<String> EMAIL_WORK_PROVIDERS = new HashSet<String>(Arrays.asList(new String[] { "@villadelrosario.it", "@bitmedia.it",
			"@kpmg.it", "@reply.it", "@venere.com", "@oracle.com", "@accenture.com", "@unisa.it", "@falesia.it", EMAIL_ORG_EXPEDIA, EMAIL_ORG_INMATICA,
			EMAIL_ORG_ALMAVIVA, EMAIL_ORG_OBJECTWAY }));

	private static final String GENERIC_HOME_REL = "http://schemas.google.com/g/2005#home";

	private static final String GENERIC_MAIN_REL = "http://schemas.google.com/g/2005#main";

	private static final String GENERIC_WORK_REL = "http://schemas.google.com/g/2005#work";

	private static final String GROUP_MY_CONTACTS = "System Group: My Contacts";

	private static final String ORG_ALMAVIVA = "Almaviva";

	private static final String ORG_ALMAVIVA_NAME = "Almaviva S.p.A.";

	private static final String ORG_INMATICA = "Inmatica";

	private static final String ORG_INMATICA_NAME = "Inmatica S.p.A.";

	private static final String ORG_OBJECTWAY = "ObjectWay";

	private static final String ORG_OBJECTWAY_NAME = "ObjectWay iTech S.p.A.";

	private static final String ORG_VENERE = "Venere";

	private static final String ORG_VENERE_NAME = "Venere Net S.r.l.";

	private static final HashSet<String> PHONE_HOME_RELS = new HashSet<String>(Arrays.asList(new String[] { GENERIC_MAIN_REL, GENERIC_HOME_REL,
			GENERIC_WORK_REL, "http://schemas.google.com/g/2005#work_fax" }));

	private static final HashSet<String> PHONE_MOBILE_RELS = new HashSet<String>(Arrays.asList(new String[] { GENERIC_MAIN_REL,
			"http://schemas.google.com/g/2005#mobile", GENERIC_WORK_REL, "http://schemas.google.com/g/2005#pager" }));

	private static final String[] PHONE_PREFIXES = { "06", "0828", "089", "081", "02", "0773", "0577", "051", "059" };

	private static final String[] NOTE_PREFIXES = { "Anno nascita", "CF", "Citofono", "Gruppo sanguigno", "Lavoro", "Orario", "Studia", "Targa auto" };

	static final String URL_FACEBOOK = "https://www.facebook.com/";

	private static final String URL_GOOGLE_CONTACT_GROUPS_QUERY = "https://www.google.com/m8/feeds/groups/default/full";

	private static final String URL_GOOGLE_CONTACTS_QUERY = "https://www.google.com/m8/feeds/contacts/default/full";

	public static void main(String[] args) throws Exception {
		Properties props = new Properties();
		props.load(new FileReader("checks_report.properties"));
		for (String key : props.stringPropertyNames()) {
			System.setProperty(key, props.getProperty(key));
		}
		new Test().run();
	}

	protected final Logger logger = LoggerFactory.getLogger(getClass());

	private Map<String, String> organizationEmails = new HashMap<String, String>();

	private Map<String, String> organizations = new HashMap<String, String>();

	private boolean checkContent(ContactEntry contact, String name) throws IOException {
		boolean changed = false;
		if (contact.getContent() != null) {
			BufferedReader reader = new BufferedReader(new StringReader(contact.getTextContent().getContent().getPlainText()));
			String line = null;
			while ((line = reader.readLine()) != null) {
				String prefix = null;
				for (String pre : NOTE_PREFIXES) {
					if (line.startsWith(pre+":")) {
						prefix = pre;
						break;
					}
				}
				if (prefix == null) {
					report(contact, new UnknowNote(name, line));
				}
			}
		}
		return changed;
	}

	private boolean checkEmails(ContactEntry contact, String name, boolean isMyContact) {
		boolean changed = false;
		boolean asPrimary = false;
		List<Email> emailAddresses = contact.getEmailAddresses();
		for (Email e : emailAddresses) {
			String address = e.getAddress();
			String provider = address.substring(address.indexOf("@"));
			if (e.getRel() == null) {
				if (EMAIL_HOME_PROVIDERS.contains(provider)) {
					e.setRel(GENERIC_HOME_REL);
					e.setLabel(null);
					changed = true;
					reportChange(contact, "email : " + name + " : " + address);
				} else if (EMAIL_WORK_PROVIDERS.contains(provider)) {
					e.setRel(GENERIC_WORK_REL);
					e.setLabel(null);
					changed = true;
					reportChange(contact, "email : " + name + " : " + address);
				} else {
					report(contact, new NoEmailAddressRel(name, address));
				}
			} else if (EMAIL_HOME_PROVIDERS.contains(provider)) {
				if (!e.getRel().equals(GENERIC_HOME_REL)) {
					e.setRel(GENERIC_HOME_REL);
					changed = true;
					reportChange(contact, "email address home rel : " + name + " : " + address);
				}
			} else if (EMAIL_WORK_PROVIDERS.contains(provider)) {
				if (!e.getRel().equals(GENERIC_WORK_REL)) {
					e.setRel(GENERIC_WORK_REL);
					changed = true;
					reportChange(contact, "email address work rel : " + name + " : " + address);
				}
			}
			if (e.getPrimary()) {
				if (asPrimary) {
					report(contact, new DuplicatePrimaryEmailAddress(name, address));
				} else {
					asPrimary = true;
				}
			}
		}
		if (isMyContact && emailAddresses.size() > 1 && !asPrimary) {
			report(contact, new NoPrimaryEmailAddress(name));
		}
		return changed;
	}

	private void checkEventFacebookBirthday(ContactEntry contact, FacebookBirthdayCalendar facebookCalendar, String name, Object facebookContact) {
		Birthday birthday = contact.getBirthday();
		facebookCalendar.remove(facebookContact);
		String contactDate = birthday.getWhen();
		if (contactDate.startsWith("-")) {
			report(contact, new BirthdayWithoutYear(name, contactDate));
		}
		contactDate = contactDate.substring(1);
		contactDate = contactDate.substring(contactDate.indexOf("-") + 1, contactDate.length());
		String calendarDate = BIRTHDAY_DATE_FORMAT.format(facebookCalendar.getBirthday(facebookContact));
		if (!contactDate.equals(calendarDate)) {
			report(contact, new BirthdayMismatch(name, contactDate, calendarDate));
		}
	}

	private boolean checkEvents(FacebookBirthdayCalendar facebookCalendar, ContactEntry contact, String name) {
		boolean changed = false;
		if (contact.hasBirthday() && facebookCalendar != null) {
			Object facebookContact = facebookCalendar.getByName(name);
			if (facebookContact != null) {
				checkEventFacebookBirthday(contact, facebookCalendar, name, facebookContact);
			} else {
				for (Website website : contact.getWebsites()) {
					if (website.getHref().startsWith(URL_FACEBOOK)) {
						facebookContact = facebookCalendar.getById(website.getHref().substring(URL_FACEBOOK.length()));
						if (facebookContact != null) {
							checkEventFacebookBirthday(contact, facebookCalendar, name, facebookContact);
						}
					}
				}
			}
		}
		for (Event e : contact.getEvents()) {
			logger.debug(name + " event " + e.getRel() + " " + e.getLabel() + " " + e.getWhen().getStartTime());
		}
		return changed;
	}

	private boolean checkOrganization(ContactEntry contact, String name, String org) {
		boolean changed = false;
		Organization organization = null;
		if (contact.getOrganizations().size() > 1) {
			report(contact, new DuplicateOrganization(name));
		} else if (contact.getOrganizations().size() == 1) {
			organization = contact.getOrganizations().get(0);
		}
		String orgName = null;
		String orgTitle = null;
		if (organization != null && organization.getOrgName() != null) {
			orgName = organization.getOrgName().getValue();
		}
		if (organization != null && organization.getOrgTitle() != null) {
			orgTitle = organization.getOrgTitle().getValue();
		}
		if (!organizations.get(org).equals(orgName)) {
			report(contact, new OrganizationMismatch(name, organizations.get(org), orgName, orgTitle));
		} else {
			if (organizationEmails.get(org) != null) {
				boolean found = false;
				List<Email> emailAddresses = contact.getEmailAddresses();
				for (Email e : emailAddresses) {
					String address = e.getAddress();
					String provider = address.substring(address.indexOf("@"));
					if (organizationEmails.get(org).equals(provider)) {
						found = true;
					}
				}
				if (!found) {
					report(contact, new NoOrganizationEmailAddress(name, orgName));
				}
			}
		}
		return changed;
	}

	private boolean checkPhoneNumbers(ContactEntry contact, String name, boolean isMyContact) {
		boolean changed = false;
		int mobileCounter = 0;
		boolean asPrimary = false;
		List<PhoneNumber> phoneNumbers = contact.getPhoneNumbers();
		PhoneNumber lastMobileNum = null;
		for (PhoneNumber p : phoneNumbers) {
			String phoneNumber = p.getPhoneNumber();
			if (phoneNumber.startsWith("+") || phoneNumber.startsWith("00")) {
				if (phoneNumber.startsWith("+39") || phoneNumber.startsWith("0039")) {
					report(contact, new ItalyPrefixPhoneNumber(name, phoneNumber));
				} else {
					logger.debug("international phone number : " + name + " : " + phoneNumber);
				}
				if (p.getRel() == null) {
					report(contact, new InvalidPhoneNumberRel(name, phoneNumber, false, p.getRel(), p.getLabel()));
				}
			} else if (phoneNumber.startsWith("3")) {
				if (!Pattern.matches("3[0-9][0-9] [0-9][0-9][0-9] [0-9][0-9][0-9][0-9]", phoneNumber)) {
					phoneNumber = phoneNumber.replace((char) 8236, ' ');
					phoneNumber = phoneNumber.replace(" ", "");
					if (phoneNumber.length() != 10) {
						report(contact, new InvalidPhoneNumber(name, phoneNumber));
					} else {
						StringBuilder builder = new StringBuilder(phoneNumber);
						builder.insert(3, " ");
						builder.insert(7, " ");
						phoneNumber = builder.toString();
						p.setPhoneNumber(phoneNumber);
						changed = true;
						reportChange(contact, "phone number : " + name + " : " + phoneNumber);
					}
				}
				if (p.getRel() == null || !PHONE_MOBILE_RELS.contains(p.getRel())) {
					report(contact, new InvalidPhoneNumberRel(name, phoneNumber, true, p.getRel(), p.getLabel()));
				}
				mobileCounter++;
				lastMobileNum = p;
			} else if (phoneNumber.startsWith("0")) {
				String prefix = null;
				for (String pre : PHONE_PREFIXES) {
					if (phoneNumber.startsWith(pre)) {
						prefix = pre;
						break;
					}
				}
				if (prefix == null) {
					report(contact, new UnknowPhoneNumberLandlinePrefix(name, phoneNumber));
				} else {
					phoneNumber = phoneNumber.substring(prefix.length());
					phoneNumber = phoneNumber.replace((char) 8236, ' ');
					phoneNumber = phoneNumber.replace(" ", "");
					if (!Pattern.matches("[0-9]*", phoneNumber)) {
						report(contact, new InvalidPhoneNumber(name, phoneNumber));
					} else {
						if (phoneNumber.length() > 7) {
							StringBuilder builder = new StringBuilder(phoneNumber);
							builder.insert(4, " ");
							builder.insert(0, prefix + " ");
							phoneNumber = builder.toString();
						} else if (phoneNumber.length() == 7) {
							StringBuilder builder = new StringBuilder(phoneNumber);
							builder.insert(3, " ");
							builder.insert(0, prefix + " ");
							phoneNumber = builder.toString();
						} else {
							phoneNumber = prefix + " " + phoneNumber;
						}
						if (!phoneNumber.equals(p.getPhoneNumber())) {
							p.setPhoneNumber(phoneNumber);
							changed = true;
							reportChange(contact, "phone number : " + name + " : " + phoneNumber);
						}
					}
				}
				if ((p.getRel() == null && !"Genitori".equals(p.getLabel())) || (p.getRel() != null && !PHONE_HOME_RELS.contains(p.getRel()))) {
					report(contact, new InvalidPhoneNumberRel(name, phoneNumber, false, p.getRel(), p.getLabel()));
				}
			} else if (phoneNumber.startsWith("800")) {
				if (p.getRel() == null) {
					report(contact, new InvalidPhoneNumberRel(name, phoneNumber, false, p.getRel(), p.getLabel()));
				}
			} else if (phoneNumber.startsWith("*123")) {
				if (p.getRel() == null) {
					report(contact, new InvalidPhoneNumberRel(name, phoneNumber, false, p.getRel(), p.getLabel()));
				}
			} else {
				report(contact, new UnknowPhoneNumber(name, phoneNumber));
				if (p.getRel() == null) {
					report(contact, new InvalidPhoneNumberRel(name, phoneNumber, false, p.getRel(), p.getLabel()));
				}
			}
			if (p.getPrimary()) {
				if (asPrimary) {
					report(contact, new DuplicatePrimaryPhoneNumber(name, phoneNumber));
				} else {
					asPrimary = true;
				}
			}
		}
		if (isMyContact && phoneNumbers.size() > 1 && !asPrimary) {
			if (mobileCounter == 1) {
				lastMobileNum.setPrimary(true);
				changed = true;
				reportChange(contact, "mobile as main number : " + name);
			} else {
				report(contact, new NoPrimaryPhoneNumber(name));
			}
		}
		return changed;
	}

	private boolean checkWebsites(Map<String, String> facebookUrls, ContactEntry contact, String name) {
		boolean changed = false;
		if (facebookUrls != null) {
			boolean facebookFound = false;
			for (Website website : contact.getWebsites()) {
				if (facebookUrls.remove(website.getHref()) != null) {
					facebookFound = true;
				}
			}
			if (!facebookFound && name != null) {
				String facebookUrl = null;
				for (Entry<String, String> e : facebookUrls.entrySet()) {
					if (name.equalsIgnoreCase(e.getValue())) {
						facebookUrl = e.getKey();
					}
				}
				if (facebookUrl != null) {
					contact.addWebsite(new Website(facebookUrl, null, Boolean.FALSE, Website.Rel.PROFILE));
					facebookUrls.remove(facebookUrl);
					reportChange(contact, "added facebook site : " + name + " : " + facebookUrl);
					changed = true;
				}
			}
		}
		return changed;
	}

	void executeVCards(Map<String, byte[]> photoMap) throws FileNotFoundException, IOException, ParserException, ValidationException {
		if (!Boolean.parseBoolean(System.getProperty("vcards.process"))) {
			return;
		}
		String filename = System.getProperty("vcards.input");
		StringWriter buffer = new StringWriter();
		PrintWriter writer = new PrintWriter(buffer);
		BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(filename), Charset.forName("UTF-8")));
		String line = null;
		while ((line = reader.readLine()) != null) {
			line = line.replace("\\:", ":");
			line = line.replace("BDAY:-", "BDAY:1970");
			writer.println(line);
		}
		reader.close();
		writer.close();
		VCardBuilder builder = new VCardBuilder(new StringReader(buffer.toString()));
		buffer = null;
		List<VCard> vCards =  builder.buildAll();
		File file = new File(System.getProperty("vcards.output"));
		file.mkdir();
		final FileOutputStream output = new FileOutputStream(new File(file, "all.vcf"));
		VCardOutputter outputter = new VCardOutputter();
		for (VCard vCard : vCards) {
			String name = vCard.getProperty(Id.FN).getValue();
			final byte[] photo = photoMap.get(name);
			if (photo != null) {
				Property p = new Property(Id.PHOTO) {

					private static final long serialVersionUID = 1L;

					@Override
					public String getValue() {
						return Base64.encodeBase64String(photo);
					}

					@Override
					public void validate() throws ValidationException {
					}

				};
				p.getParameters().add(new Encoding("BASE64"));
				p.getParameters().add(new Parameter("") {

					private static final long serialVersionUID = 1L;

					@Override
					public String getValue() {
						return "JPEG";
					}

				});
				vCard.getProperties().add(p);
			}
			outputter.output(vCard, new OutputStream() {

				@Override
				public void close() throws IOException {
				}

				@Override
				public void write(int b) throws IOException {
					output.write(b);
				}
			});
			outputter.output(vCard, new FileWriter(new File(file, name.replace("?", "") + ".vcf")));
		}
		output.close();
	}

	void printXml(BaseEntry<?> entry) {
		try {
			StringWriter sw = new StringWriter();
			entry.generate(new XmlWriter(sw), new ExtensionProfile());
			logger.info(sw.toString());
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
		}
	}

	private void report(ContactEntry contact, Warning warning) {
		if (!"ignore".equalsIgnoreCase(System.getProperty(warning.getClass().getName()))) {
			// printXml(contact);
			System.out.println("WARN: " + warning.getMessage());
		}
	}

	private void reportChange(ContactEntry contact, String message) {
		System.out.println("CHANGE " + message);
	}

	public void run() {
		long t = System.currentTimeMillis();
		try {
			Map<String, byte[]> photoMap = new HashMap<String, byte[]>();
			Map<String, String> facebookUrls = null;
			try {
				facebookUrls = new Facebook().readByUrl(new BufferedReader(new FileReader("fbids.txt")));
			} catch (Exception ex) {
				System.err.println("Facebook URLS disabled: " + ex.getClass().getName() + ": " + ex.getMessage());
			}
			FacebookBirthdayCalendar facebookCalendar = null;
			try {
				facebookCalendar = new FacebookBirthdayCalendar();
			} catch (Exception ex) {
				System.err.println("Facebook Birthday Calendar disabled: " + ex.getClass().getName() + ": " + ex.getMessage());
			}
			String googleUsername = System.getProperty("google.username");
			if (googleUsername == null) {
				System.out.print("Google username > ");
				googleUsername = System.console().readLine();
			}
			String googlePassword = System.getProperty("google.password");
			if (googlePassword == null) {
				System.out.print("Google password > ");
				googlePassword = new String(System.console().readPassword());
			}
			ContactsService service = new ContactsService("Google-contactsExampleApp-3");
			service.setUserCredentials(googleUsername, googlePassword);
			Query query = new Query(new URL(URL_GOOGLE_CONTACT_GROUPS_QUERY));
			query.setMaxResults(1000);
			List<ContactGroupEntry> contactGroups = service.query(query, ContactGroupFeed.class).getEntries();
			String myContactsGroupId = null;
			organizations.put(ORG_INMATICA, ORG_INMATICA_NAME);
			organizations.put(ORG_ALMAVIVA, ORG_ALMAVIVA_NAME);
			organizations.put(ORG_OBJECTWAY, ORG_OBJECTWAY_NAME);
			organizations.put(ORG_VENERE, ORG_VENERE_NAME);
			organizationEmails.put(ORG_INMATICA, EMAIL_ORG_INMATICA);
			organizationEmails.put(ORG_ALMAVIVA, EMAIL_ORG_ALMAVIVA);
			organizationEmails.put(ORG_OBJECTWAY, EMAIL_ORG_OBJECTWAY);
			organizationEmails.put(ORG_VENERE, EMAIL_ORG_EXPEDIA);
			HashMap<String, String> organizationMap = new HashMap<String, String>();
			for (ContactGroupEntry contactGroup : contactGroups) {
				if (contactGroup.getTitle().getPlainText().equals(GROUP_MY_CONTACTS)) {
					myContactsGroupId = contactGroup.getId();
				}
				for (String orgName : organizations.keySet()) {
					if (contactGroup.getTitle().getPlainText().equals(orgName)) {
						organizationMap.put(orgName, contactGroup.getId());
					}
				}
			}
			query = new Query(new URL(URL_GOOGLE_CONTACTS_QUERY));
			query.setMaxResults(1000);
			List<ContactEntry> contacts = service.query(query, ContactFeed.class).getEntries();
			for (ContactEntry contact : contacts) {
				boolean changed = false;
				boolean isMyContact = false;
				Set<String> contactOrganizations = new HashSet<String>();
				List<GroupMembershipInfo> groupMembershipInfos = contact.getGroupMembershipInfos();
				for (GroupMembershipInfo groupMembershipInfo : groupMembershipInfos) {
					if (groupMembershipInfo.getHref().equals(myContactsGroupId)) {
						isMyContact = true;
					}
					for (Entry<String, String> org : organizationMap.entrySet()) {
						if (groupMembershipInfo.getHref().equals(org.getValue())) {
							contactOrganizations.add(org.getKey());
						}
					}
				}
				String name = contact.hasName() ? contact.getName().getFullName().getValue() : null;
				if (name == null) {
					if (isMyContact) {
						report(contact, new NullName());
						printXml(contact);
					}
				} else if (name.equals("Pasquale Pizzutii")) {
					printXml(contact);
				}
				if (isMyContact) {
					if (groupMembershipInfos.size() <= 1) {
						report(contact, new NoGroup(name));
					}
					Link photo = null;
					for (Link l : contact.getLinks()) {
						if (ContactLink.Rel.CONTACT_PHOTO.equals(l.getRel()) && l.getEtag() != null) {
							photo = l;
						}
					}
					if (photo == null) {
						report(contact, new NoPhoto(name));
					} else {
						if (name != null) {
							Thread.sleep(10);
							GDataRequest photoQuery = service.createLinkQueryRequest(photo);
							photoQuery.execute();
							byte[] binary = IOUtils.toByteArray(photoQuery.getResponseStream());
							photoMap.put(name, binary);
						}
					}
				}
				changed |= checkPhoneNumbers(contact, name, isMyContact);
				changed |= checkEmails(contact, name, isMyContact);
				if (contact.getPhoneNumbers().isEmpty() && contact.getEmailAddresses().isEmpty() && contact.getImAddresses().isEmpty()) {
					report(contact, new Uncontactable(name));
				}
				changed |= checkWebsites(facebookUrls, contact, name);
				changed |= checkEvents(facebookCalendar, contact, name);
				for (String contactOrganization : contactOrganizations) {
					changed |= checkOrganization(contact, name, contactOrganization);
				}
				changed |= checkContent(contact, name);
				if (changed) {
					service.update(new URL(contact.getEditLink().getHref()), contact);
				}
			}
			executeVCards(photoMap);
			if (facebookUrls != null) {
				for (Entry<String, String> e : facebookUrls.entrySet()) {
					report(null, new FacebookFriendNotFound(e.getValue(), e.getKey()));
				}
			}
			if (facebookCalendar != null) {
				for (Object obj : facebookCalendar) {
					report(null, new FacebookFriendBirthdayNotFound(facebookCalendar.getName(obj), URL_FACEBOOK + facebookCalendar.getId(obj)));
				}
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		} finally {
			System.out.println("Completed in " + ((System.currentTimeMillis() - t) / 1000) + " sec");
		}
	}

}
