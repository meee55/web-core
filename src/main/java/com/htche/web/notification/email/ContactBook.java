package com.htche.web.notification.email;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ContactBook {
	final Logger logger = LoggerFactory.getLogger(getClass());
	static final String separater = ";";

	Map<String, Contact> contactsMap = null;

	List<String> contacts;

	public List<String> getContacts() {
		return contacts;
	}

	public void setContacts(List<String> contacts) {
		this.contacts = contacts;
	}

	public Contact getContact(String id) {
		if (null == contactsMap) {
			if (null != contacts && 0 < contacts.size()) {
				contactsMap = new HashMap<String, Contact>();
				for (String contactString : contacts) {
					String[] fields = contactString.split(separater);
					if (fields.length > 5 || fields.length < 2) {
						logger.error("\"" + contactString + "\" is not a valid contact item.");
					}
					else {
						if (!Contact.isValidEmailAddress(fields[1])) {
							logger.error("Receiver address(es) is not valid." + fields[1]);
							continue;
						}

						Contact contact = new Contact();
						contact.setEmails(fields[1]);

						if (3 <= fields.length) {
							contact.setSender(fields[2]);
						}

						contactsMap.put(fields[0], contact);
					}
				}
			}
			else {
				return null;
			}
		}

		return contactsMap.get(id);
	}
}
