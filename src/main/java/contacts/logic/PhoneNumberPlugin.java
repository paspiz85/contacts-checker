package contacts.logic;

import java.util.List;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gdata.data.contacts.ContactEntry;
import com.google.gdata.data.extensions.PhoneNumber;

import contacts.warning.DuplicatePrimaryPhoneNumber;
import contacts.warning.InvalidPhoneNumber;
import contacts.warning.InvalidPhoneNumberRel;
import contacts.warning.ItalyPrefixPhoneNumber;
import contacts.warning.NoPrimaryPhoneNumber;
import contacts.warning.UnknowPhoneNumber;
import contacts.warning.UnknowPhoneNumberLandlinePrefix;

public class PhoneNumberPlugin extends Plugin {

	@Override
	public void run(Context context) {
		super.run(context);
		context.addChange(checkPhoneNumbers(context.getContact(), context.getName(), Boolean.parseBoolean(context.get(Context.IS_MY_CONTACT))));
	}



	protected final Logger logger = LoggerFactory.getLogger(getClass());
	
	private boolean checkPhoneNumbers(ContactEntry contact, String name,
			boolean isMyContact) {
		boolean changed = false;
		int mobileCounter = 0;
		boolean asPrimary = false;
		List<PhoneNumber> phoneNumbers = contact.getPhoneNumbers();
		PhoneNumber lastMobileNum = null;
		for (PhoneNumber p : phoneNumbers) {
			String phoneNumber = p.getPhoneNumber();
			if (phoneNumber.startsWith("+") || phoneNumber.startsWith("00")) {
				if (phoneNumber.startsWith("+39")
						|| phoneNumber.startsWith("0039")) {
					report(contact, new ItalyPrefixPhoneNumber(name,
							phoneNumber));
				} else {
					logger.debug("international phone number : " + name + " : "
							+ phoneNumber);
				}
				if (p.getRel() == null) {
					report(contact, new InvalidPhoneNumberRel(name,
							phoneNumber, false, p.getRel(), p.getLabel()));
				}
			} else if (phoneNumber.startsWith("3")) {
				if (!Pattern.matches(
						"3[0-9][0-9] [0-9][0-9][0-9] [0-9][0-9][0-9][0-9]",
						phoneNumber)) {
					phoneNumber = phoneNumber.replace((char) 8236, ' ');
					phoneNumber = phoneNumber.replace(" ", "");
					if (phoneNumber.length() != 10) {
						report(contact, new InvalidPhoneNumber(name,
								phoneNumber));
					} else {
						StringBuilder builder = new StringBuilder(phoneNumber);
						builder.insert(3, " ");
						builder.insert(7, " ");
						phoneNumber = builder.toString();
						p.setPhoneNumber(phoneNumber);
						changed = true;
						reportChange(contact, "phone number : " + name + " : "
								+ phoneNumber);
					}
				}
				if (p.getRel() == null
						|| !PHONE_MOBILE_RELS.contains(p.getRel())) {
					report(contact, new InvalidPhoneNumberRel(name,
							phoneNumber, true, p.getRel(), p.getLabel()));
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
					report(contact, new UnknowPhoneNumberLandlinePrefix(name,
							phoneNumber));
				} else {
					phoneNumber = phoneNumber.substring(prefix.length());
					phoneNumber = phoneNumber.replace((char) 8236, ' ');
					phoneNumber = phoneNumber.replace(" ", "");
					if (!Pattern.matches("[0-9]*", phoneNumber)) {
						report(contact, new InvalidPhoneNumber(name,
								phoneNumber));
					} else {
						if (phoneNumber.length() > 7) {
							StringBuilder builder = new StringBuilder(
									phoneNumber);
							builder.insert(4, " ");
							builder.insert(0, prefix + " ");
							phoneNumber = builder.toString();
						} else if (phoneNumber.length() == 7) {
							StringBuilder builder = new StringBuilder(
									phoneNumber);
							builder.insert(3, " ");
							builder.insert(0, prefix + " ");
							phoneNumber = builder.toString();
						} else {
							phoneNumber = prefix + " " + phoneNumber;
						}
						if (!phoneNumber.equals(p.getPhoneNumber())) {
							p.setPhoneNumber(phoneNumber);
							changed = true;
							reportChange(contact, "phone number : " + name
									+ " : " + phoneNumber);
						}
					}
				}
				if ((p.getRel() == null && !"Genitori".equals(p.getLabel()))
						|| (p.getRel() != null && !PHONE_HOME_RELS.contains(p
								.getRel()))) {
					report(contact, new InvalidPhoneNumberRel(name,
							phoneNumber, false, p.getRel(), p.getLabel()));
				}
			} else if (phoneNumber.startsWith("800")) {
				if (p.getRel() == null) {
					report(contact, new InvalidPhoneNumberRel(name,
							phoneNumber, false, p.getRel(), p.getLabel()));
				}
			} else if (phoneNumber.startsWith("*123")) {
				if (p.getRel() == null) {
					report(contact, new InvalidPhoneNumberRel(name,
							phoneNumber, false, p.getRel(), p.getLabel()));
				}
			} else {
				report(contact, new UnknowPhoneNumber(name, phoneNumber));
				if (p.getRel() == null) {
					report(contact, new InvalidPhoneNumberRel(name,
							phoneNumber, false, p.getRel(), p.getLabel()));
				}
			}
			if (p.getPrimary()) {
				if (asPrimary) {
					report(contact, new DuplicatePrimaryPhoneNumber(name,
							phoneNumber));
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
}
