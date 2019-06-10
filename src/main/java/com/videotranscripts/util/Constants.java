package com.videotranscripts.util;

/**
 * @author amitb
 *
 */
public class Constants {
	
	//common
	public static final String LOGGEDIN_USER = "LOGGEDIN_USER";
	
	//success messages
	public static final String USER_ADDED = "User added successfully!";
	//error messages
	public static final String INTERNAL_ERROR = "Error occurred. Please try again.";
	public static final String LOGIN_FAILED = "Login Failed.";
	public static final String SESSION_EXPIRED = "Session expired.";
	
	//audio processing
	public static final String AUDIO_FORMAT = "wav";
	public static final String CODEC = "pcm_s16le";
	public static final Integer BIT_RATE = 16;
	public static final Integer CHANNEL = 1;
	public static final Integer SAMPLING_RATE = 44100;
	
	//metadata constants
	public static final String DEFAULT_LANGUAGE_CODE = "en-US";
	public static final String DEFAULT_AUDIO_TOPIC = "Test";
	public static final String DEFAULT_INTERATION_TYPE = "PROFESSIONALLY_PRODUCED";
	public static final String DEFAULT_MICROPHONE_DISTANCE = "MICROPHONE_DISTANCE_UNSPECIFIED";
	public static final String DEFAULT_RECORDING_DEVICE_TYPE = "RECORDING_DEVICE_TYPE_UNSPECIFIED";
	
	//KARTRA
	public static final String MEMBERSHIP_GRANTED = "membership_granted";
	public static final String MEMBERSHIP_REVOKED = "membership_revoked";

}
