package com.videotranscripts.controller;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FilenameUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.FileCopyUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.videotranscripts.entity.UserDetails;
import com.videotranscripts.entity.UserHistory;
import com.videotranscripts.exception.AuthException;
import com.videotranscripts.model.FailureResponse;
import com.videotranscripts.model.Metadata;
import com.videotranscripts.model.SubscriptionPlan;
import com.videotranscripts.service.UserService;
import com.videotranscripts.service.VideoProcessService;
import com.videotranscripts.util.Constants;
import com.videotranscripts.util.VideoTranscriptUtils;

/**
 * @author amitb
 *
 */
@Controller
public class VideoController {
	
	private static final Logger LOGGER = LogManager.getLogger(VideoController.class.getName());
	
	@Autowired
	private VideoProcessService videoProcessService;
	@Autowired
	private UserService userService;
	
	@GetMapping("/upload")
	public String showDashboard(HttpServletRequest request, Model model){
		if(request.getSession() == null || request.getSession().getAttribute(Constants.LOGGEDIN_USER) == null) {
			throw new AuthException(new FailureResponse(Constants.SESSION_EXPIRED, HttpStatus.UNAUTHORIZED));
		}
		UserDetails userDetails = (UserDetails) request.getSession().getAttribute(Constants.LOGGEDIN_USER);
		model.addAttribute("user", userDetails);
		if(userDetails.getIsAdmin() == 0 && (userDetails.getCredits() == 0 || SubscriptionPlan.Inactive.getName().equalsIgnoreCase(userDetails.getSubscriptionPlan()))) {
			model.addAttribute("upgradeRequired", true);
		}
		return "upload";
	}
	
	@PostMapping("/videoUpload")
	public String uploadVideo(HttpServletRequest request, RedirectAttributes attr, Metadata metadata, MultipartFile file) {
		if(request.getSession() == null || request.getSession().getAttribute(Constants.LOGGEDIN_USER) == null) {
			throw new AuthException(new FailureResponse(Constants.SESSION_EXPIRED, HttpStatus.UNAUTHORIZED));
		}
		UserDetails userDetails = (UserDetails) request.getSession().getAttribute(Constants.LOGGEDIN_USER);
		videoProcessService.uploadAndTranscribeVideo(metadata, file, userDetails);
		//decrement video credits
		if(!VideoTranscriptUtils.isAdmin(userDetails)) {
			userDetails = userService.updateUserCredits(userDetails);
		}
		request.getSession().setAttribute(Constants.LOGGEDIN_USER, userDetails);
		attr.addFlashAttribute("success", true);
		return "redirect:/dashboard";
	}
	
	@GetMapping("/download")
	public void downloadFile(HttpServletRequest request, HttpServletResponse response, String requestId) {
		LOGGER.info("START--Download file");
		if(request.getSession() == null || request.getSession().getAttribute(Constants.LOGGEDIN_USER) == null) {
			throw new AuthException(new FailureResponse(Constants.SESSION_EXPIRED, HttpStatus.UNAUTHORIZED));
		}
		UserDetails userDetails = (UserDetails) request.getSession().getAttribute(Constants.LOGGEDIN_USER);
		UserHistory userHistory = userService.findHistoryByRequestId(Long.valueOf(requestId));
		if(userHistory.getUserId() != userDetails.getUserId()) {
			throw new AuthException(new FailureResponse(Constants.SESSION_EXPIRED, HttpStatus.UNAUTHORIZED));
		}
		
		String fileName = userHistory.getVideoName();
		String transcript = userHistory.getTranscript();
		if (StringUtils.hasText(transcript)) {
			String opFileName = fileName.substring(0, fileName.length() - (FilenameUtils.getExtension(fileName.replaceAll(" ", "_")).length() + 1)) + ".srt";
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
		LOGGER.info("END--Download file");
	}

}
