package com.videotranscripts.model;

import lombok.Data;

/**
 * @author amitb
 *
 */
@Data
public class Metadata {
	
	private String speechContext;
	private String languageCode;
	private String audioTopic;
	private String interactionType;
	private String microphoneDistance;
	private String recordingDeviceType;

}
