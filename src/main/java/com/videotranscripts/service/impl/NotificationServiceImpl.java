package com.videotranscripts.service.impl;

import java.io.UnsupportedEncodingException;

import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import com.videotranscripts.entity.UserDetails;
import com.videotranscripts.service.NotificationService;
import com.videotranscripts.util.VideoTranscriptUtils;

/**
 * @author amitb
 *
 */
@Service
public class NotificationServiceImpl implements NotificationService {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(NotificationServiceImpl.class);
	
	@Autowired
	private JavaMailSender javaMailSender;
	@Autowired
	private TemplateEngine templateEngine;

	@Override
	public boolean sendNotificationMail(UserDetails userDetails, String videoName) {
		LOGGER.info("Sending notification mail START to {}", userDetails.getEmail());
		try {
			MimeMessage mimeMessage = javaMailSender.createMimeMessage();
			mimeMessage.setReplyTo(InternetAddress.parse("support@videotranscriber.io", false));
			MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);
			helper.setTo(userDetails.getEmail());
			helper.setFrom(new InternetAddress("support@videotranscriber.io", "support@videotranscriber.io"));
			helper.setSubject("Your transcripts are ready!");
			helper.setText(prepareTextForNotificationEmail(userDetails, videoName), true);
			javaMailSender.send(mimeMessage);
		} catch (MessagingException | UnsupportedEncodingException e) {
			LOGGER.error("Sending notification mail ERROR to {}", userDetails.getEmail());
		}
		LOGGER.info("Sending notification mail END to {}", userDetails.getEmail());
		return true;
	}
	
	private String prepareTextForNotificationEmail(UserDetails userDetails, String videoName) {
		Context context = new Context();
        context.setVariable("username", userDetails.getFirstName() + " " + userDetails.getLastName());
        context.setVariable("videoName", videoName);
        context.setVariable("transcriptDate", VideoTranscriptUtils.getDateStr());
        return templateEngine.process("notificationEmail", context);
	}

	@Override
	public boolean sendUserRegistrationMail(UserDetails userDetails) {
		LOGGER.info("Sending registration mail START to {}", userDetails.getEmail());
		try {
			MimeMessage mimeMessage = javaMailSender.createMimeMessage();
			mimeMessage.setReplyTo(InternetAddress.parse("support@videotranscriber.io", false));
			MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);
			helper.setTo(userDetails.getEmail());
			helper.setFrom(new InternetAddress("support@videotranscriber.io", "support@videotranscriber.io"));
			helper.setSubject("Your login details!");
			helper.setText(prepareTextForUserRegistration(userDetails), true);
			javaMailSender.send(mimeMessage);
		} catch (MessagingException | UnsupportedEncodingException e) {
			LOGGER.error("Sending registration mail ERROR to {}", userDetails.getEmail());
		}
		LOGGER.info("Sending registration mail END to {}", userDetails.getEmail());
		return true;
	}
	
	private String prepareTextForUserRegistration(UserDetails userDetails) {
		Context context = new Context();
        context.setVariable("email", userDetails.getEmail());
        context.setVariable("password", userDetails.getPassword());
        context.setVariable("plan", userDetails.getSubscriptionPlan());
        context.setVariable("subEndDate", VideoTranscriptUtils.toDateStr(userDetails.getSubscriptionEndDate()));
        return templateEngine.process("registrationEmail", context);
	}
	
}
