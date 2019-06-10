package com.videotranscripts.util;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.util.StringUtils;

import com.videotranscripts.entity.UserDetails;
import com.videotranscripts.model.Metadata;

/**
 * @author amitb
 *
 */
public class VideoTranscriptUtils {
	
	public static Metadata getMetadata(Metadata metadata) {
		if (!StringUtils.hasText((metadata.getLanguageCode()))) {
			metadata.setLanguageCode(Constants.DEFAULT_LANGUAGE_CODE);
		}
		if (!StringUtils.hasText((metadata.getAudioTopic()))) {
			metadata.setAudioTopic(Constants.DEFAULT_AUDIO_TOPIC);
		}
		if (!StringUtils.hasText((metadata.getInteractionType()))) {
			metadata.setInteractionType(Constants.DEFAULT_INTERATION_TYPE);
		}
		if (!StringUtils.hasText((metadata.getMicrophoneDistance()))) {
			metadata.setMicrophoneDistance(Constants.DEFAULT_MICROPHONE_DISTANCE);
		}
		if (!StringUtils.hasText((metadata.getRecordingDeviceType()))) {
			metadata.setRecordingDeviceType(Constants.DEFAULT_RECORDING_DEVICE_TYPE);
		}

		return metadata;
	}
	
	public static String getAbsoluteFileName(String filename) {
		String absoluteFileName = "";
		absoluteFileName = filename.substring(0, filename.length() - (FilenameUtils.getExtension(filename).length() + 1));
		return absoluteFileName;
	}
	
	public static String getDateStr() {
		Date date = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm");
		return sdf.format(date);
	}
	
	public static String toDateStr(Date date) {
		SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm");
		return sdf.format(date);
	}
	
	public static boolean isAdmin(UserDetails userDetails) {
		if(userDetails.getIsAdmin() == 1) {
			return true;
		}
		return false;
	}
	
	public static String autoGeneratePassword() {
		return RandomStringUtils.random(15, true, true);
	}
	
	public static Date addMonth(Date inputDate) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(inputDate);
		cal.add(Calendar.MONTH, 1);
		cal.add(Calendar.DAY_OF_MONTH, 1);
		return cal.getTime();
	}
	
	public static Date atStartOfDay(Date date) {
	    LocalDateTime localDateTime = dateToLocalDateTime(date);
	    LocalDateTime startOfDay = localDateTime.with(LocalTime.MIN);
	    return localDateTimeToDate(startOfDay);
	}

	public static Date atEndOfDay(Date date) {
	    LocalDateTime localDateTime = dateToLocalDateTime(date);
	    LocalDateTime endOfDay = localDateTime.with(LocalTime.MAX);
	    return localDateTimeToDate(endOfDay);
	}

	private static LocalDateTime dateToLocalDateTime(Date date) {
	    return LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
	}

	private static Date localDateTimeToDate(LocalDateTime localDateTime) {
	    return Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
	}
	
	public static String getSrtFormattedTime(String seconds) {
		long millis = Integer.parseInt(seconds) * 1000;
		
		String hms = String.format("%02d:%02d:%02d", TimeUnit.MILLISECONDS.toHours(millis),
	            TimeUnit.MILLISECONDS.toMinutes(millis) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(millis)),
	            TimeUnit.MILLISECONDS.toSeconds(millis) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis)));
		
		return hms + ",000";
	}
	
}
