package com.videotranscripts.service;

import java.io.File;

import org.springframework.web.multipart.MultipartFile;

import com.videotranscripts.entity.UserDetails;
import com.videotranscripts.model.Metadata;

/**
 * @author amitb
 *
 */
public interface VideoProcessService {
	
	void uploadAndTranscribeVideo(Metadata metadata, MultipartFile file, UserDetails userDetails);
	String preprocessAndSeparateAudio(File videoFile);

}
