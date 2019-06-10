package com.videotranscripts.service;

import com.videotranscripts.model.Metadata;

/**
 * @author amitb
 *
 */
public interface GoogleCloudService {
	
	public String uploadToBucket(String audioFilePath, String fileName);
	public String getTranscript(Metadata metadata, String blobName);

}
