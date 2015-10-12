package it.paspiz85.contacts;

import it.paspiz85.contacts.auth.GoogleAuthSupport;
import it.paspiz85.contacts.logic.Constants;
import it.paspiz85.contacts.logic.Context;
import it.paspiz85.contacts.logic.EmailPlugin;
import it.paspiz85.contacts.logic.PhoneNumberPlugin;
import it.paspiz85.contacts.logic.UncontactablePlugin;
import it.paspiz85.contacts.warning.BirthdayMismatch;
import it.paspiz85.contacts.warning.BirthdayWithoutYear;
import it.paspiz85.contacts.warning.DuplicateOrganization;
import it.paspiz85.contacts.warning.FacebookFriendBirthdayNotFound;
import it.paspiz85.contacts.warning.FacebookFriendNotFound;
import it.paspiz85.contacts.warning.NoGroup;
import it.paspiz85.contacts.warning.NoOrganizationEmailAddress;
import it.paspiz85.contacts.warning.NoPhoto;
import it.paspiz85.contacts.warning.NoRelation;
import it.paspiz85.contacts.warning.NullName;
import it.paspiz85.contacts.warning.OrganizationMismatch;
import it.paspiz85.contacts.warning.OtherGroup;
import it.paspiz85.contacts.warning.UnknowNote;
import it.paspiz85.contacts.warning.Warning;

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
import java.lang.reflect.Field;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

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

import com.google.api.client.auth.oauth2.Credential;
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
import com.google.gdata.data.contacts.Relation;
import com.google.gdata.data.contacts.Website;
import com.google.gdata.data.extensions.Email;
import com.google.gdata.data.extensions.Organization;
import com.google.gdata.util.common.xml.XmlWriter;

public class Test implements Constants, Runnable {

    public static void main(String[] args) throws Exception {
        try (FileReader reader = new FileReader("checks_report.properties")) {
            Properties props = new Properties();
            props.load(reader);
            for (String key : props.stringPropertyNames()) {
                System.setProperty(key, props.getProperty(key));
            }
        }
        new Test().run();
    }

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    private Map<String, String> organizationEmails = new HashMap<String, String>();

    private Map<String, String> organizations = new HashMap<String, String>();

    private Map<String, String> otherGroups = new HashMap<String, String>();

    private boolean checkContent(ContactEntry contact, String name) throws IOException {
        boolean changed = false;
        if (contact.getContent() != null) {
            BufferedReader reader = new BufferedReader(new StringReader(contact.getTextContent().getContent()
                    .getPlainText()));
            String line = null;
            while ((line = reader.readLine()) != null) {
                String prefix = null;
                // for (String pre : NOTE_PREFIXES) {
                for (Field field : NoteField.class.getDeclaredFields()) {
                    String pre = null;
                    try {
                        pre = (String) field.get(null);
                    } catch (RuntimeException e) {
                        throw e;
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                    if (line.startsWith(pre + ":")) {
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

    private void checkEventFacebookBirthday(ContactEntry contact, FacebookBirthdayCalendar facebookCalendar,
            String name, Object facebookContact) {
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

    void executeVCards(Map<String, byte[]> photoMap) throws FileNotFoundException, IOException, ParserException,
            ValidationException {
        if (!Boolean.parseBoolean(System.getProperty("vcards.process"))) {
            return;
        }
        String filename = System.getProperty("vcards.input");
        StringWriter buffer = new StringWriter();
        PrintWriter writer = new PrintWriter(buffer);
        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(filename),
                Charset.forName("UTF-8")));
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
        List<VCard> vCards = builder.buildAll();
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

    private Context context = new Context();

    private void report(ContactEntry contact, Warning warning) {
        context.report(contact, warning);
    }

    private void reportChange(ContactEntry contact, String message) {
        context.reportChange(contact, message);
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
                System.err.println("Facebook Birthday Calendar disabled: " + ex.getClass().getName() + ": "
                        + ex.getMessage());
            }
            ContactsService service = new ContactsService("Google-contactsExampleApp-3");
            Credential googleCredentials = getGoogleCredentials();
            googleCredentials.refreshToken();
			service.setOAuth2Credentials(googleCredentials);
            Query query = new Query(new URL(URL_GOOGLE_CONTACT_GROUPS_QUERY));
            query.setMaxResults(1000);
            List<ContactGroupEntry> contactGroups = service.query(query, ContactGroupFeed.class).getEntries();
            String myContactsGroupId = null;
            String myOtherContactsGroupId = null;
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
                final String groupName = contactGroup.getTitle().getPlainText();
                if (groupName.equals(GROUP_MY_CONTACTS)) {
                    myContactsGroupId = contactGroup.getId();
                }
                if (groupName.equals("Other/Contacts")) {
                    myOtherContactsGroupId = contactGroup.getId();
                }
                if (groupName.startsWith("Other/")) {
                    otherGroups.put(groupName, contactGroup.getId());
                }
                for (String orgName : organizations.keySet()) {
                    if (groupName.equals(orgName)) {
                        organizationMap.put(orgName, contactGroup.getId());
                    }
                }
            }
            query = new Query(new URL(URL_GOOGLE_CONTACTS_QUERY));
            query.setMaxResults(1000);
            List<ContactEntry> contacts = service.query(query, ContactFeed.class).getEntries();
            for (ContactEntry contact : contacts) {
                context.setContact(contact);
                final String name = context.getName();
                boolean changed = false;
                boolean isMyContact = false;
                String otherGroupId = null;
                boolean isOtherContacts = false;
                Set<String> contactOrganizations = new HashSet<String>();
                List<GroupMembershipInfo> groupMembershipInfos = contact.getGroupMembershipInfos();
                for (GroupMembershipInfo groupMembershipInfo : groupMembershipInfos) {
                    final String groupMembershipId = groupMembershipInfo.getHref();
                    if (groupMembershipId.equals(myContactsGroupId)) {
                        isMyContact = true;
                    }
                    if (groupMembershipId.equals(myOtherContactsGroupId)) {
                        isOtherContacts = true;
                    }
                    if (otherGroups.values().contains(groupMembershipId)) {
                        otherGroupId = groupMembershipId;
                    }
                    for (Entry<String, String> org : organizationMap.entrySet()) {
                        if (groupMembershipId.equals(org.getValue())) {
                            contactOrganizations.add(org.getKey());
                        }
                    }
                }
                if (isMyContact && otherGroupId != null) {
                    for (Entry<String, String> e : otherGroups.entrySet()) {
                        if (e.getValue().equals(otherGroupId)) {
                            report(contact, new OtherGroup(name, e.getKey()));
                        }
                    }
                }
                if (name.length() == 0) {
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
                if (isOtherContacts) {
                    List<Relation> relations = contact.getRelations();
                    if (relations.isEmpty()) {
                        report(contact, new NoRelation(name));
                    }
                }
                context.put(Context.IS_MY_CONTACT, Boolean.toString(isMyContact));
                new PhoneNumberPlugin().run(context);
                new EmailPlugin().run(context);
                new UncontactablePlugin().run(context);
                changed |= context.isChanged();
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
                    report(null, new FacebookFriendBirthdayNotFound(facebookCalendar.getName(obj), URL_FACEBOOK
                            + facebookCalendar.getId(obj)));
                }
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        } finally {
            System.out.println("Completed in " + ((System.currentTimeMillis() - t) / 1000) + " sec");
        }
    }

    private Credential getGoogleCredentials() throws IOException {
        GoogleAuthSupport googleAuthSupport = new GoogleAuthSupport(new FileReader(".client_secrets.json"),
                Collections.singleton(GoogleAuthSupport.API_CONTACTS_SCOPE), new File(".credentials"));
        Credential credential = googleAuthSupport.authorize("paspiz85", (u) -> System.out.println(u), () -> {
            BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
            try {
                return in.readLine();
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        });
        return credential;
    }
}
