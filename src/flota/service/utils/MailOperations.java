package flota.service.utils;

import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class MailOperations {

	public static void sendMail(String mailMessage) {

		String to = "florin.brasoveanu@arabesque.ro";
		String from = "FlotaService";
		String host = "mail.arabesque.ro";

		Properties properties = System.getProperties();
		properties.setProperty("mail.smtp.host", host);

		Session session = Session.getDefaultInstance(properties);

		try {
			MimeMessage message = new MimeMessage(session);

			message.setFrom(new InternetAddress(from));

			message.addRecipient(Message.RecipientType.TO, new InternetAddress(to));

			message.setSubject("Flota Service");

			message.setText(mailMessage);

			Transport.send(message);

		} catch (MessagingException mex) {
			System.out.println(mex.toString());
		}
	}

}
