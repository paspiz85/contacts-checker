package contacts.logic;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.HashSet;

public interface Constants {

	SimpleDateFormat BIRTHDAY_DATE_FORMAT = new SimpleDateFormat(
			"MM-dd");

	HashSet<String> EMAIL_HOME_PROVIDERS = new HashSet<String>(
			Arrays.asList(new String[] { "@fastwebnet.it", "@virgilio.it",
					"@tim.it", "@msn.com", "@live.com", "@ovi.com",
					"@yahoo.com", "@live.it", "@inwind.it", "@outlook.com",
					"@tin.it", "@aliceposta.it", "@alice.it", "@yahoo.it",
					"@libero.it", "@gmail.com", "@hotmail.com", "@hotmail.it",
					"@tiscali.it" }));

	String EMAIL_ORG_ALMAVIVA = "@almaviva.it";

	String EMAIL_ORG_EXPEDIA = "@expedia.com";

	String EMAIL_ORG_INMATICA = "@inmatica.com";

	String EMAIL_ORG_OBJECTWAY = "@objectway.it";

	HashSet<String> EMAIL_WORK_PROVIDERS = new HashSet<String>(
			Arrays.asList(new String[] { "@villadelrosario.it", "@bitmedia.it",
					"@kpmg.it", "@reply.it", "@venere.com", "@oracle.com",
					"@accenture.com", "@unisa.it", "@falesia.it",
					EMAIL_ORG_EXPEDIA, EMAIL_ORG_INMATICA, EMAIL_ORG_ALMAVIVA,
					EMAIL_ORG_OBJECTWAY }));

	String GENERIC_HOME_REL = "http://schemas.google.com/g/2005#home";

	String GENERIC_MAIN_REL = "http://schemas.google.com/g/2005#main";

	String GENERIC_WORK_REL = "http://schemas.google.com/g/2005#work";

	String GROUP_MY_CONTACTS = "System Group: My Contacts";

	String ORG_ALMAVIVA = "Almaviva";

	String ORG_ALMAVIVA_NAME = "Almaviva S.p.A.";

	String ORG_INMATICA = "Inmatica";

	String ORG_INMATICA_NAME = "Inmatica S.p.A.";

	String ORG_OBJECTWAY = "ObjectWay";

	String ORG_OBJECTWAY_NAME = "ObjectWay iTech S.p.A.";

	String ORG_VENERE = "Venere";

	String ORG_VENERE_NAME = "Venere Net S.r.l.";

	HashSet<String> PHONE_HOME_RELS = new HashSet<String>(
			Arrays.asList(new String[] { GENERIC_MAIN_REL, GENERIC_HOME_REL,
					GENERIC_WORK_REL,
					"http://schemas.google.com/g/2005#work_fax" }));

	HashSet<String> PHONE_MOBILE_RELS = new HashSet<String>(
			Arrays.asList(new String[] { GENERIC_MAIN_REL,
					"http://schemas.google.com/g/2005#mobile",
					GENERIC_WORK_REL,
					"http://schemas.google.com/g/2005#home_fax",
					"http://schemas.google.com/g/2005#pager" }));

	String[] PHONE_PREFIXES = { "06", "0828", "089",
			"081", "02", "0773", "0577", "051", "059" };

	static final String URL_FACEBOOK = "https://www.facebook.com/";

	String URL_GOOGLE_CONTACT_GROUPS_QUERY = "https://www.google.com/m8/feeds/groups/default/full";

	String URL_GOOGLE_CONTACTS_QUERY = "https://www.google.com/m8/feeds/contacts/default/full";

}
