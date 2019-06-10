package com.videotranscripts.service;

import com.videotranscripts.entity.UserDetails;

/**
 * @author amitb
 *
 */
public interface NotificationService {
	boolean sendNotificationMail(UserDetails userDetails, String videoName);
	boolean sendUserRegistrationMail(UserDetails userDetails);
}
