package com.videotranscripts.service.impl;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.videotranscripts.entity.UserDetails;
import com.videotranscripts.entity.UserHistory;
import com.videotranscripts.exception.GenericException;
import com.videotranscripts.model.FailureResponse;
import com.videotranscripts.model.Metadata;
import com.videotranscripts.model.TranscriptionStatus;
import com.videotranscripts.service.UserService;
import com.videotranscripts.service.VideoProcessService;
import com.videotranscripts.util.Constants;

import it.sauronsoftware.jave.AudioAttributes;
import it.sauronsoftware.jave.Encoder;
import it.sauronsoftware.jave.EncodingAttributes;

/**
 * @author amitb
 *
 */
@Service
public class VideoProcessServiceImpl implements VideoProcessService {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(VideoProcessServiceImpl.class);
	private static ExecutorService executorService = Executors.newFixedThreadPool(10);
	
	@Value("${videofile.upload.folder}")
	private String UPLOAD_FOLDER;
	
	@Autowired
	private VideoTranscripterTask asyncTranscripterTask;
	@Autowired
	private UserService userService;

	@Override
	public void uploadAndTranscribeVideo(Metadata metadata, MultipartFile file, UserDetails userDetails) {
		LOGGER.info("START--uploadAndTranscribeVideo");
		//adds entry in IN_PROGRESS status
		String videoFileName = file.getOriginalFilename().replaceAll(" ", "_");
		UserHistory userHistory = insertToUserHistory(userDetails, videoFileName);
		//upload file
		if (file != null) {
			// convert multipart file to java.io.File
			File convFile = new File(UPLOAD_FOLDER + userDetails.getFirstName().replaceAll(" ", "_") + System.currentTimeMillis() + "." + FilenameUtils.getExtension(file.getOriginalFilename().replaceAll(" ", "_")));
			//File convFile = new File("D:\\video_transcripts\\test.mp4");
			LOGGER.info("Video file path: " + convFile.getAbsolutePath());
			if (file.getSize() > 0) {
				try {
					byte[] bytes = file.getBytes();
					Path path2 = Paths.get(UPLOAD_FOLDER + convFile.getName() + "." + FilenameUtils.getExtension(file.getOriginalFilename().replaceAll(" ", "_")));
					Files.write(path2, bytes);
					LOGGER.info("Video uploaded on server");
					//starts async task for video transcription
					asyncTranscripterTask.setRequestId(userHistory.getRequestId());
					asyncTranscripterTask.setMetadata(metadata);
					asyncTranscripterTask.setUserDetails(userDetails);
					asyncTranscripterTask.setVideoFileName(videoFileName);
					asyncTranscripterTask.setVideoFile(path2.toFile());
					executorService.submit(asyncTranscripterTask);
				} catch(Exception e) {
					e.printStackTrace();
				}
			}
		}
		LOGGER.info("END--uploadAndTranscribeVideo");
	}
	
	private UserHistory insertToUserHistory(UserDetails userDetails, String fileName) {
		UserHistory userHistory = new UserHistory();
		userHistory.setUserId(userDetails.getUserId());
		userHistory.setVideoName(fileName);
		userHistory.setTranscriptStatus(TranscriptionStatus.IN_PROGRESS.toString());
		userHistory.setCreatedAt(new Date());
		
		return userService.addInHistory(userHistory);
	}

	@Override
	public String preprocessAndSeparateAudio(File videoFile) {
		LOGGER.info("START--preprocessAndSeparateAudio");
		//File audioFile = new File(videoFile.getAbsolutePath().substring(0,videoFile.getAbsolutePath().length()-4) + ".wav");
		File audioFile = new File(videoFile.getAbsolutePath().substring(0, videoFile.getAbsolutePath().length() - (FilenameUtils.getExtension(videoFile.getAbsolutePath().replaceAll(" ", "_")).length() + 1)) + ".wav");
		AudioAttributes audio = new AudioAttributes();
		audio.setCodec(Constants.CODEC);
		audio.setBitRate(new Integer(Constants.BIT_RATE));
		audio.setChannels(new Integer(Constants.CHANNEL)); //mono
		audio.setSamplingRate(new Integer(Constants.SAMPLING_RATE));
		EncodingAttributes attrs = new EncodingAttributes();
		attrs.setFormat(Constants.AUDIO_FORMAT);
		attrs.setAudioAttributes(audio);
		
		String audioFilePath = "";
		Encoder encoder = new Encoder();
		try {
			encoder.encode(videoFile, audioFile, attrs);
			audioFilePath = audioFile.getAbsolutePath();
			LOGGER.info("Audio file path: " + audioFilePath);
		} catch (Exception e) {
			e.printStackTrace();
			throw new GenericException(new FailureResponse(Constants.INTERNAL_ERROR, HttpStatus.INTERNAL_SERVER_ERROR));
		}
		LOGGER.info("END--preprocessAndSeparateAudio");
		return audioFilePath;
	}

}
