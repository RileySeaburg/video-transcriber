package com.videotranscripts.controller;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.FileCopyUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author amitb
 *
 */
@Controller
public class TestController {
	
	private static final Logger logger = LoggerFactory.getLogger(TestController.class);
	
	@GetMapping("/health")
	public ResponseEntity<String> health(HttpServletRequest request){
		return new ResponseEntity<>("Hi from Amit!", HttpStatus.OK);
	}
	
	@GetMapping("/index")
	public String index(HttpServletRequest request){
		//throw new GenericException(new FailureResponse("Error occured", HttpStatus.UNAUTHORIZED));
		return "starter-kit";
	}
	
	@GetMapping("/register")
	public String register(HttpServletRequest request){
		return "register";
	}
	
	@GetMapping("/testVideo")
	public String testVideoTranscript(HttpServletRequest request, String fileName, Model model){
		if (fileName == null) {
			model.addAttribute("isDownload", false);
		} else {
			model.addAttribute("isDownload", true);
			model.addAttribute("downloadLink", "/getFile?outputFileName=" + fileName);
		}
		return "dashboard";
	}
	
	@PostMapping("/upload")
	public String testUploadVideo(HttpServletRequest request, MultipartFile file) {
		//String transcriptFileName = videoTranscriptService.uploadAndTranscribeVideo(new Metadata(), file, request);
		//String transcriptFileName = "test";
		//return "redirect:/testVideo?fileName=" + transcriptFileName;
		return "index";
	}
	
	@GetMapping(value = { "/getFile" })
	public void downloadFile(HttpServletRequest request, HttpServletResponse response, String outputFileName) {
		logger.info("START--Download file");
		String transcript = String.valueOf(request.getSession().getAttribute("transcript"));
		if (StringUtils.hasText(transcript)) {
			String opFileName = outputFileName + ".txt";
			BufferedWriter bw = null;
			try {
				File downloadFile = new File(opFileName);
				bw = new BufferedWriter(new FileWriter(downloadFile));

				bw.write(transcript);
				bw.flush();
				FileInputStream fis = new FileInputStream(downloadFile);
				response.setHeader("Content-Disposition", "attachment; filename=\"" + downloadFile.getName() + "\"");
				response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
				ServletOutputStream outputStream = response.getOutputStream();
				FileCopyUtils.copy(fis, outputStream);
				outputStream.close();
				fis.close();
			} catch (IOException e) {
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
		}
		logger.info("END--Download file");
	}

}
