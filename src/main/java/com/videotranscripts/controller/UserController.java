package com.videotranscripts.controller;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.videotranscripts.entity.UserDetails;
import com.videotranscripts.entity.UserHistory;
import com.videotranscripts.exception.AuthException;
import com.videotranscripts.model.FailureResponse;
import com.videotranscripts.model.kartra.MemberRegistration;
import com.videotranscripts.model.session.AddUserRequest;
import com.videotranscripts.model.session.LoginApiRequest;
import com.videotranscripts.service.NotificationService;
import com.videotranscripts.service.UserService;
import com.videotranscripts.util.Constants;

/**
 * @author amitb
 *
 */
@Controller
public class UserController {
	
	private static final Logger LOGGER = LogManager.getLogger(UserController.class.getName());
	
	@Autowired
	private UserService userService;
	@Autowired
	private NotificationService notificationService;
	
	@GetMapping("/")
	public String login(HttpServletRequest request, String error, Model model){
		if(StringUtils.hasText(error)) {
			model.addAttribute("error", error);
		}
		return "login";
	}
	
	@PostMapping("/login")
	public String doLogin(HttpServletRequest request, @ModelAttribute @Valid LoginApiRequest loginRequest, RedirectAttributes attr){
		LOGGER.info("Request to login from {}", loginRequest.getEmail());
		UserDetails userDetails = userService.login(loginRequest);
		request.getSession().setAttribute(Constants.LOGGEDIN_USER, userDetails);
		LOGGER.info("Login success for user: {}", loginRequest.getEmail());
		attr.addFlashAttribute("username", userDetails.getFirstName());
		return "redirect:/dashboard";
	}
	
	@GetMapping("/dashboard")
	public String showDashboard(HttpServletRequest request, Model model){
		if(request.getSession() == null || request.getSession().getAttribute(Constants.LOGGEDIN_USER) == null) {
			throw new AuthException(new FailureResponse(Constants.SESSION_EXPIRED, HttpStatus.UNAUTHORIZED));
		}
		UserDetails userDetails = (UserDetails) request.getSession().getAttribute(Constants.LOGGEDIN_USER);
		model.addAttribute("user", userDetails);
		if(userDetails.getIsAdmin() == 1) {
			List<UserHistory> userHistoryDetails = userService.getAllUserHistory();
			model.addAttribute("userHistoryDetails", userHistoryDetails);
			model.addAttribute("totalTranscripts", userHistoryDetails.size());
			model.addAttribute("totalUsers", String.valueOf(userService.getUserCount()));
			return "adminDashboard";
		}
		List<UserHistory> userHistoryDetails = userService.getUserHistory(userDetails.getUserId());
		model.addAttribute("userHistoryDetails", userHistoryDetails);
		model.addAttribute("total", userHistoryDetails.size());
		return "dashboard";
	}
	
	@GetMapping("/logout")
	public String doLogout(HttpServletRequest request){
		if(request.getSession() != null && request.getSession().getAttribute(Constants.LOGGEDIN_USER) != null) {
			request.getSession().removeAttribute(Constants.LOGGEDIN_USER);
		}
		LOGGER.info("Logging out success");
		return "redirect:/";
	}
	
	@GetMapping("/users")
	public String showUsersPage(HttpServletRequest request, Model model, String success){
		if(request.getSession() == null || request.getSession().getAttribute(Constants.LOGGEDIN_USER) == null) {
			throw new AuthException(new FailureResponse(Constants.SESSION_EXPIRED, HttpStatus.UNAUTHORIZED));
		}
		UserDetails userDetails = (UserDetails) request.getSession().getAttribute(Constants.LOGGEDIN_USER);
		if(userDetails.getIsAdmin() != 1) {
			throw new AuthException(new FailureResponse(Constants.SESSION_EXPIRED, HttpStatus.UNAUTHORIZED));
		}
		model.addAttribute("user", userDetails);
		if(StringUtils.hasText(success)) {
			model.addAttribute("success", success);
		}
		
		List<UserDetails> allUsersList = userService.getAllUsersList();
		model.addAttribute("allUsers", allUsersList);
		
		LOGGER.info("Showing users to : " + userDetails.getEmail());
		return "users";
	}
	
	@PostMapping("/addUser")
	public String addUser(HttpServletRequest request, @ModelAttribute @Valid AddUserRequest addUserRequest, RedirectAttributes attr){
		if(request.getSession() == null || request.getSession().getAttribute(Constants.LOGGEDIN_USER) == null) {
			throw new AuthException(new FailureResponse(Constants.SESSION_EXPIRED, HttpStatus.UNAUTHORIZED));
		}
		UserDetails userDetails = (UserDetails) request.getSession().getAttribute(Constants.LOGGEDIN_USER);
		LOGGER.info("Request to add user {} from {}, for plan : {}", addUserRequest.getEmail(), userDetails.getEmail(), addUserRequest.getPlan());
		userService.addUser(addUserRequest);
		
		attr.addFlashAttribute("success", Constants.USER_ADDED);
		return "redirect:/users";
	}
	
	@PostMapping("/api/register")
	public ResponseEntity<String> register(HttpServletRequest request, @RequestBody MemberRegistration memberRegistration){
		LOGGER.info("Request to add user via API: {}", memberRegistration);
		UserDetails userDetails = userService.registerUserByApi(memberRegistration);
		notificationService.sendUserRegistrationMail(userDetails);
		return new ResponseEntity<>(HttpStatus.OK);
	}
	
	@PostMapping("/api/revoke-access")
	public ResponseEntity<String> revokeAccess(HttpServletRequest request, @RequestBody MemberRegistration memberRegistration){
		LOGGER.info("Request to revoke user via API: {}", memberRegistration);
		userService.revokeAccess(memberRegistration);
		return new ResponseEntity<>(HttpStatus.OK);
	}
	
	@GetMapping("/api/update-status")
	public ResponseEntity<String> updateUserStatus(HttpServletRequest request){
		LOGGER.info("Renewal status update job started..");
		userService.updateUserStatus();
		LOGGER.info("Renewal status update job ended..");
		return new ResponseEntity<>(HttpStatus.OK);
	}
	
	@GetMapping("/api/send-mail")
	public ResponseEntity<String> sendMail(HttpServletRequest request){
		LOGGER.info("Test mail started..");
		if(request.getSession() == null || request.getSession().getAttribute(Constants.LOGGEDIN_USER) == null) {
			throw new AuthException(new FailureResponse(Constants.SESSION_EXPIRED, HttpStatus.UNAUTHORIZED));
		}
		UserDetails userDetails = (UserDetails) request.getSession().getAttribute(Constants.LOGGEDIN_USER);
		notificationService.sendUserRegistrationMail(userDetails);
		LOGGER.info("Test mail ended..");
		return new ResponseEntity<>(HttpStatus.OK);
	}

}
