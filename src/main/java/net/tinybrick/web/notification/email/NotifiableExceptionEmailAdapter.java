package net.tinybrick.web.notification.email;

import javax.mail.MessagingException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import net.tinybrick.utils.mail.Compose;
import net.tinybrick.utils.mail.MailBroker;
import net.tinybrick.web.annotation.ExceptionNotification;

public class NotifiableExceptionEmailAdapter extends AbstractNotifiableExceptionAdapter {
	final Logger logger = LoggerFactory.getLogger(getClass());

	static final String defaultContactId = "default";

	String messageIdentifier;

	public String getMessageIdentifier() {
		return messageIdentifier;
	}

	public void setMessageIdentifier(String messageIdentifier) {
		this.messageIdentifier = messageIdentifier;
	}

	ContactBook contactBook;

	public ContactBook getContactBook() {
		return contactBook;
	}

	public void setContactBook(ContactBook contacts) {
		this.contactBook = contacts;
	}

	String defaultSender;

	public String getDefaultSender() {
		return defaultSender;
	}

	public void setDefaultSender(String defaultSender) {
		this.defaultSender = defaultSender;
	}

	@Autowired MailBroker mailBroker;

	public MailBroker getMailBroker() {
		return mailBroker;
	}

	public void setMailBroker(MailBroker mailBroker) {
		this.mailBroker = mailBroker;
	}

	/**
	 * @param notification
	 * @param troubleMaker
	 * @param troubleMethod
	 * @param ex
	 * @return
	 */
	private Compose compose(ExceptionNotification notification, String name, String troubleMaker, String troubleMethod,
			Throwable ex) {
		Compose compose = new Compose();
		troubleMaker = troubleMaker.replace("$", ".");

		String annotationValue = notification.value().trim();
		Contact contact = getContact(annotationValue, name, troubleMaker, troubleMethod);
		if (null == contact || contact.getEmails().size() == 0) {
			logger.error("No recipient for this error ", ex);
			return null;
		}

		compose.setFrom(null != contact.getSender() ? contact.getSender() : null != defaultSender ? defaultSender
				: null);
		compose.setTo(contact.getEmails());
		String subject = String.format("[%s]-\"%s\"-%s", null != messageIdentifier ? messageIdentifier
				: (annotationValue.length() != 0 ? annotationValue : null != name ? name : troubleMaker + "."
						+ troubleMethod),
				notification.subject().length() > 0 ? notification.subject() : ex.getMessage(),
				null == messageIdentifier ? "" : (annotationValue.length() != 0 ? annotationValue : null != name ? name
						: troubleMaker + "." + troubleMethod));
		compose.setSubject(subject);
		compose.setContent(getLongMessage(notification.body(), ex));

		return compose;
	}

	/**
	 * @param troubleMaker
	 * @param troubleMethod
	 * @return
	 */
	private Contact getContact(String annotationValue, String name, String troubleMaker, String troubleMethod) {
		Contact contact = null;
		if (null != contactBook) {
			// 获得联系人列表，优先顺序依次为：Annotation id > URL > Class name > default
			if (annotationValue.length() != 0) {
				contact = contactBook.getContact(annotationValue);
			}

			if (null == contact && null != name) {
				contact = contactBook.getContact(name);
			}

			if (null == contact) {
				contact = contactBook.getContact(troubleMaker + "." + troubleMethod);
			}

			if (null == contact) {
				contact = contactBook.getContact(defaultContactId);
			}
		}

		return contact;
	}

	/**
	 * 
	 */
	public void sendNotification(Throwable ex) throws Throwable {
		sendNotification(ex, null);
	}

	/**
	 * 
	 */
	@Override
	public void sendNotification(Throwable ex, String name) throws Throwable {
		if (null == mailBroker) {
			logger.info("Email broker is not enabled.");
			return;
		}

		StringBuffer troubleMaker = new StringBuffer();
		StringBuffer troubleMethod = new StringBuffer();
		ExceptionNotification notification = getExceptionNotification(ex, troubleMaker, troubleMethod);

		if (null != notification) {
			Compose compose = compose(notification, name, troubleMaker.toString(), troubleMethod.toString(), ex);
			if (null != compose) {
				try {
					logger.debug("Notification email is sending..");
					mailBroker.send(compose);
					logger.debug("Notification email is sent");
				}
				catch (MessagingException e) {
					logger.error(e.getMessage(), e);
				}
			}
		}

		throw ex;
	}
}
