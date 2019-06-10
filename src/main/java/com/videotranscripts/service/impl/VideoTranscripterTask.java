package com.videotranscripts.service.impl;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.videotranscripts.entity.UserDetails;
import com.videotranscripts.entity.UserHistory;
import com.videotranscripts.model.Metadata;
import com.videotranscripts.model.TranscriptionStatus;
import com.videotranscripts.service.GoogleCloudService;
import com.videotranscripts.service.NotificationService;
import com.videotranscripts.service.UserService;
import com.videotranscripts.service.VideoProcessService;
import com.videotranscripts.util.VideoTranscriptUtils;

import lombok.Data;

/**
 * @author amitb
 *
 */
@Service
@Scope("prototype")
@Data
public class VideoTranscripterTask implements Runnable {

	private static final Logger LOGGER = LoggerFactory.getLogger(VideoTranscripterTask.class);

	private Metadata metadata;
	private UserDetails userDetails;
	private Long requestId;
	private File videoFile;
	private String videoFileName;

	@Autowired
	private UserService userService;
	@Autowired
	private VideoProcessService videoProcessService;
	@Autowired
	private GoogleCloudService googleCloudService;
	@Autowired
	private NotificationService notificationService;

	@Value("${outputfile.path}")
	private String outputFilePath;
	@Value("${videofile.upload.folder}")
	private String UPLOAD_FOLDER;

	@Override
	public void run() {
		LOGGER.info("Processing started in new thread..");
		String outputFileName = "";
		VideoTranscriptUtils.getMetadata(metadata);
		try {
			// generate audio file
			String audioPath = videoProcessService.preprocessAndSeparateAudio(videoFile);
			File audioFile = new File(audioPath);
			// upload the audiofile to bucket
			String blobName = googleCloudService.uploadToBucket(audioPath, audioFile.getName());
			// Get transcript for successfully uploaded file
			String transcript = googleCloudService.getTranscript(metadata, blobName);
			//updating userHistory table
			updateUserHistory(transcript, requestId);
			//write to file
			outputFileName = FilenameUtils.getExtension(audioFile.getAbsolutePath().replaceAll(" ", "_"));
			writeOutputFile(transcript, outputFileName);
			
			//send mail notification
			notificationService.sendNotificationMail(userDetails, videoFileName);
		} catch (Exception e) {
			e.printStackTrace();
			updateErrorUserHistory(requestId);
		}

		LOGGER.info("Processing ended in new thread..");
	}
	
	private void updateUserHistory(String transcript, Long requestId) {
		LOGGER.info("Updating user history to merk transcription- complete");
		UserHistory userHistory = userService.findHistoryByRequestId(requestId);
		if(userHistory != null) {
			userHistory.setTranscript(transcript);
			userHistory.setTranscriptStatus(TranscriptionStatus.COMPLETE.toString());
			userService.addInHistory(userHistory);
		}
	}
	
	private void updateErrorUserHistory(Long requestId) {
		LOGGER.info("Updating user history to merk transcription- complete");
		UserHistory userHistory = userService.findHistoryByRequestId(requestId);
		if(userHistory != null) {
			userHistory.setTranscriptStatus(TranscriptionStatus.ERROR.toString());
			userService.addInHistory(userHistory);
		}
	}

	private void writeOutputFile(String transcript, String fileName) {
		File file = new File(outputFilePath + fileName + ".txt");
		LOGGER.info("START--Generating transcript file at: " + file.getAbsolutePath());
		BufferedWriter bw = null;
		try {
			bw = new BufferedWriter(new FileWriter(file));
			bw.write(transcript);
			bw.flush();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (bw != null) {
				try {
					bw.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		LOGGER.info("END--Generating transcript file");
	}

}
