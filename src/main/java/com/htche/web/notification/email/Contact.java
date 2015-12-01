package com.htche.web.notification.email;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Contact {
	final Logger logger = LoggerFactory.getLogger(getClass());
	static final String separator = ",";
	String sender;

	public String getSender() {
		return sender;
	}

	public void setSender(String sender) {
		this.sender = sender;
	}

	List<String> emailList;
	String emails;

	public List<String> getEmails() {
		if (null == emailList && null != emails && emails.trim().length() > 0) {
			emailList = new ArrayList<String>();
			String[] emailArray = emails.split(separator);
			if (null != emailArray) {
				for (String email : emailArray) {
					if (isValidEmailAddress(email))
						emailList.add(email);
					else
						logger.error(email + " is not a valid email address.");
				}
			}
		}
		return emailList;
	}

	public void setEmails(String emails) {
		this.emails = emails;
	}

	public static boolean isValidEmailAddress(String addr) {
		String[] addresses = addr.split(separator);
		for (String address : addresses) {
			if (false == address
					.matches("^([a-zA-Z0-9_\\-\\.]+)@((\\[[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.)|(([a-zA-Z0-9\\-]+\\.)+))([a-zA-Z]{2,4}|[0-9]{1,3})(\\]?)$"))
				return false;
		}
		return true;
	}
}
