package com.videotranscripts.service.impl;

import java.io.FileInputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.google.api.gax.core.FixedCredentialsProvider;
import com.google.api.gax.longrunning.OperationFuture;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.auth.oauth2.ServiceAccountCredentials;
import com.google.cloud.speech.v1p1beta1.LongRunningRecognizeMetadata;
import com.google.cloud.speech.v1p1beta1.LongRunningRecognizeResponse;
import com.google.cloud.speech.v1p1beta1.RecognitionAudio;
import com.google.cloud.speech.v1p1beta1.RecognitionConfig;
import com.google.cloud.speech.v1p1beta1.RecognitionMetadata;
import com.google.cloud.speech.v1p1beta1.RecognitionMetadata.InteractionType;
import com.google.cloud.speech.v1p1beta1.RecognitionMetadata.MicrophoneDistance;
import com.google.cloud.speech.v1p1beta1.RecognitionMetadata.RecordingDeviceType;
import com.google.cloud.speech.v1p1beta1.SpeechClient;
import com.google.cloud.speech.v1p1beta1.SpeechContext;
import com.google.cloud.speech.v1p1beta1.SpeechRecognitionAlternative;
import com.google.cloud.speech.v1p1beta1.SpeechRecognitionResult;
import com.google.cloud.speech.v1p1beta1.SpeechSettings;
import com.google.cloud.speech.v1p1beta1.WordInfo;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import com.videotranscripts.exception.GenericException;
import com.videotranscripts.model.FailureResponse;
import com.videotranscripts.model.Metadata;
import com.videotranscripts.service.GoogleCloudService;
import com.videotranscripts.util.Constants;
import com.videotranscripts.util.VideoTranscriptUtils;

/**
 * @author amitb
 *
 */
@Service
public class GoogleCloudServiceImpl implements GoogleCloudService {

	private static final Logger LOGGER = LoggerFactory.getLogger(GoogleCloudServiceImpl.class);

	@Value("${google.storage.bucket.name}")
	private String googleStorageBucketName;

	@Value("${google.cloud.credentials.path}")
	private String googleCredentialsPath;

	@Override
	public String uploadToBucket(String audioFilePath, String fileName) {
		LOGGER.info("***Start upload***");

		Storage storage;
		BlobInfo blobInfo = BlobInfo.newBuilder(googleStorageBucketName, fileName).setContentType("audio/wav").build();

		byte[] bytes;
		try {
			storage = StorageOptions.newBuilder()
					.setCredentials(ServiceAccountCredentials.fromStream(new FileInputStream(googleCredentialsPath)))
					.build().getService();
			bytes = Files.readAllBytes(Paths.get(audioFilePath));
			storage.create(blobInfo, bytes);
		} catch (Exception e) {
			e.printStackTrace();
			throw new GenericException(new FailureResponse(Constants.INTERNAL_ERROR, HttpStatus.INTERNAL_SERVER_ERROR));
		}

		LOGGER.info("***End upload***");
		return blobInfo.getName();
	}

	@Override
	public String getTranscript(Metadata metadata, String blobName) {
		LOGGER.info("***START transcription***");
		StringBuilder transcript = new StringBuilder();
		try {
			// authenticate
			FileInputStream credentialsStream = new FileInputStream(googleCredentialsPath);
			GoogleCredentials credentials = GoogleCredentials.fromStream(credentialsStream);
			FixedCredentialsProvider credentialsProvider = FixedCredentialsProvider.create(credentials);

			SpeechSettings speechSettings = SpeechSettings.newBuilder().setCredentialsProvider(credentialsProvider)
					.build();
			SpeechClient speechClient = SpeechClient.create(speechSettings);

			RecognitionConfig.AudioEncoding encoding = RecognitionConfig.AudioEncoding.LINEAR16;

			// construct speech contexts
			SpeechContext.Builder speechBuilder = SpeechContext.newBuilder();
			if (StringUtils.hasText((metadata.getSpeechContext()))) {
				String[] phrases = metadata.getSpeechContext().split(",");
				for (String phrase : phrases) {
					speechBuilder.addPhrases(phrase);
				}
			}

			RecognitionMetadata rMetadata = RecognitionMetadata.newBuilder().setAudioTopic(metadata.getAudioTopic())
					.setRecordingDeviceType(RecordingDeviceType.valueOf(metadata.getRecordingDeviceType()))
					.setMicrophoneDistance(MicrophoneDistance.valueOf(metadata.getMicrophoneDistance()))
					.setInteractionType(InteractionType.valueOf(metadata.getInteractionType())).build();

			RecognitionConfig config = RecognitionConfig.newBuilder().setEncoding(encoding).setModel("video")
					.setProfanityFilter(true).setEnableWordTimeOffsets(true).setSampleRateHertz(44100)
					.setLanguageCode(metadata.getLanguageCode()).setEnableAutomaticPunctuation(true)
					.setUseEnhanced(false).setMetadata(rMetadata).addSpeechContexts(speechBuilder).build();

			String uri = "gs://" + googleStorageBucketName + "/" + blobName;
			LOGGER.info("URI is: " + uri);

			RecognitionAudio rAudio = RecognitionAudio.newBuilder().setUri(uri).build();
			OperationFuture<LongRunningRecognizeResponse, LongRunningRecognizeMetadata> response = speechClient
					.longRunningRecognizeAsync(config, rAudio);

			while (!response.isDone()) {
				Thread.sleep(10000);
			}

			LOGGER.info("START--Getting transcription result");
			int lines = 1;
			String startSecond = "0";
			String endSecond = "0";
			List<SpeechRecognitionResult> results = response.get().getResultsList();
			for (SpeechRecognitionResult result : results) {
				SpeechRecognitionAlternative alternative = result.getAlternativesList().get(0);
				
				StringBuilder sb = new StringBuilder();
				
				for(WordInfo wordInfo : alternative.getWordsList()) {
					if(Integer.parseInt(startSecond) < Integer.parseInt(String.valueOf(wordInfo.getStartTime().getSeconds()))) {
						transcript.append(lines).append("\n");
						transcript.append(VideoTranscriptUtils.getSrtFormattedTime(startSecond) + " --> " + VideoTranscriptUtils.getSrtFormattedTime(endSecond)).append("\n");
						transcript.append(sb.toString()).append("\n\n");
						sb = new StringBuilder(wordInfo.getWord()).append(" ");
						startSecond = String.valueOf(wordInfo.getStartTime().getSeconds());
						endSecond = String.valueOf(wordInfo.getEndTime().getSeconds());
						lines++;
					} else {
						sb.append(wordInfo.getWord()).append(" ");
						startSecond = String.valueOf(wordInfo.getStartTime().getSeconds());
						endSecond = String.valueOf(wordInfo.getEndTime().getSeconds());
					}
				}
				if(!StringUtils.isEmpty(sb.toString())) {
					transcript.append(lines).append("\n");
					transcript.append(VideoTranscriptUtils.getSrtFormattedTime(startSecond) + " --> " + VideoTranscriptUtils.getSrtFormattedTime(endSecond)).append("\n");
					transcript.append(sb.toString()).append("\n");
				}
			}

			LOGGER.info("END--Getting transcription result");
			speechClient.close();
		} catch (Exception e) {
			e.printStackTrace();
			throw new GenericException(new FailureResponse(Constants.INTERNAL_ERROR, HttpStatus.INTERNAL_SERVER_ERROR));
		}

		LOGGER.info("***END transcription***");
		return transcript.toString();
	}

}
