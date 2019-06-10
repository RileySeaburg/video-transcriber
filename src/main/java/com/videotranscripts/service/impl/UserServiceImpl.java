package com.videotranscripts.service.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.videotranscripts.entity.UserDetails;
import com.videotranscripts.entity.UserHistory;
import com.videotranscripts.exception.AuthException;
import com.videotranscripts.model.FailureResponse;
import com.videotranscripts.model.SubscriptionPlan;
import com.videotranscripts.model.kartra.Lead;
import com.videotranscripts.model.kartra.MemberRegistration;
import com.videotranscripts.model.session.AddUserRequest;
import com.videotranscripts.model.session.LoginApiRequest;
import com.videotranscripts.repo.UserDetailsRepository;
import com.videotranscripts.repo.UserHistoryRepository;
import com.videotranscripts.service.UserService;
import com.videotranscripts.util.Constants;
import com.videotranscripts.util.VideoTranscriptUtils;

/**
 * @author amitb
 *
 */
@Service
public class UserServiceImpl implements UserService{
	
	private static final Logger LOGGER = LogManager.getLogger(UserServiceImpl.class.getName());
	
	@Autowired
	private UserDetailsRepository userDetailsRepo;
	@Autowired
	private UserHistoryRepository userHistoryRepo;

	@Override
	public UserDetails login(LoginApiRequest loginRequest) {
		UserDetails userDetails = userDetailsRepo.findByEmailAndPassword(loginRequest.getEmail(), loginRequest.getPassword());
		if(userDetails == null) {
			throw new AuthException(new FailureResponse(Constants.LOGIN_FAILED, HttpStatus.UNAUTHORIZED));
		}
		
		return userDetails;
	}

	@Override
	public UserHistory addInHistory(UserHistory userHistory) {
		return userHistoryRepo.save(userHistory);
	}

	@Override
	public UserHistory findHistoryByRequestId(Long requestId) {
		return userHistoryRepo.findById(requestId).orElse(null);
	}

	@Override
	public List<UserHistory> getUserHistory(Integer userId) {
		return userHistoryRepo.findByUserIdOrderByCreatedAtDesc(userId);
	}

	@Override
	public List<UserDetails> getAllUsersList() {
		return userDetailsRepo.getAllUsers();
	}

	@Override
	public UserDetails addUser(AddUserRequest addUserRequest) {
		UserDetails userDetails = new UserDetails();
		userDetails.setFirstName(addUserRequest.getFirstName());
		userDetails.setLastName(addUserRequest.getLastName());
		userDetails.setEmail(addUserRequest.getEmail());
		userDetails.setPassword(addUserRequest.getPassword());
		userDetails.setCreatedAt(new Date());
		userDetails.setUpdatedAt(new Date());
		userDetails.setSubscriptionEndDate(VideoTranscriptUtils.addMonth(new Date()));
		userDetails.setIsAdmin(0);
		userDetails.setCredits(SubscriptionPlan.valueOf(addUserRequest.getPlan()).getCredits());
		userDetails.setSubscriptionPlan(SubscriptionPlan.valueOf(addUserRequest.getPlan()).getName());
		
		return userDetailsRepo.save(userDetails);
	}

	@Override
	public UserDetails updateUserCredits(UserDetails userDetails) {
		userDetails.setCredits(userDetails.getCredits() - 1);
		return userDetailsRepo.save(userDetails);
	}

	@Override
	public List<UserHistory> getAllUserHistory() {
		List<UserHistory> allUserHistoryList = new ArrayList<>();
		userHistoryRepo.findAll().forEach(e -> allUserHistoryList.add(e));
		return allUserHistoryList;
	}

	@Override
	public Long getUserCount() {
		return userDetailsRepo.count();
	}

	@Override
	public UserDetails registerUserByApi(MemberRegistration memberRegistration) {
		if(Constants.MEMBERSHIP_GRANTED.equalsIgnoreCase(memberRegistration.getAction())) {
			LOGGER.info("Registering user via API START");
			Lead lead = memberRegistration.getLead();
			
			UserDetails userDetails = findUserByEmail(lead.getEmail());
			if(userDetails == null) {
				LOGGER.info("New user.");
				userDetails = new UserDetails();
				userDetails.setFirstName(lead.getFirstName());
				userDetails.setLastName(lead.getLastName());
				userDetails.setEmail(lead.getEmail());
				userDetails.setPassword(VideoTranscriptUtils.autoGeneratePassword());
				userDetails.setCreatedAt(new Date());
				userDetails.setUpdatedAt(new Date());
				userDetails.setSubscriptionEndDate(VideoTranscriptUtils.addMonth(new Date()));
				userDetails.setIsAdmin(0);
				userDetails.setCredits(SubscriptionPlan.valueOf(memberRegistration.getActionDetails().getMembership().getLevelName()).getCredits());
				userDetails.setSubscriptionPlan(SubscriptionPlan.valueOf(memberRegistration.getActionDetails().getMembership().getLevelName()).getName());

				LOGGER.info("Registering user via API END");
				return userDetailsRepo.save(userDetails);
			} else {
				LOGGER.info("Existing user. Only updating credits and end date");
				userDetails.setCredits(userDetails.getCredits() + SubscriptionPlan.valueOf(memberRegistration.getActionDetails().getMembership().getLevelName()).getCredits());
				userDetails.setUpdatedAt(new Date());
				userDetails.setSubscriptionEndDate(VideoTranscriptUtils.addMonth(new Date()));
				userDetails.setSubscriptionPlan(SubscriptionPlan.valueOf(memberRegistration.getActionDetails().getMembership().getLevelName()).getName());
				
				LOGGER.info("Registering user via API END");
				return userDetailsRepo.save(userDetails);
			}
		}
		
		return null;
	}

	@Override
	public UserDetails findUserByEmail(String email) {
		return userDetailsRepo.findByEmail(email);
	}

	@Override
	public List<UserDetails> findBySubscriptionEndDate() {
		return userDetailsRepo.findBySubscriptionEndDateBetween(VideoTranscriptUtils.atStartOfDay(new Date()), VideoTranscriptUtils.atEndOfDay(new Date()));
	}

	@Override
	public UserDetails save(UserDetails userDetails) {
		return userDetailsRepo.save(userDetails);
	}

	@Override
	public void updateUserStatus() {
		List<UserDetails> userDetailsList = findBySubscriptionEndDate();
		for(UserDetails userDetails : userDetailsList) {
			if(userDetails.getIsAdmin() !=1) {
				SubscriptionPlan plan = SubscriptionPlan.valueOf(userDetails.getSubscriptionPlan());
				if(!SubscriptionPlan.Inactive.getName().equalsIgnoreCase(plan.getName())) {
					LOGGER.info("User picked up for renewal:{} ", userDetails.getEmail());
					
					userDetails.setUpdatedAt(new Date());
					//roll over credits
					if(SubscriptionPlan.Business.getName().equalsIgnoreCase(plan.getName())) {
						userDetails.setCredits(SubscriptionPlan.Business.getCredits());
					} else {
						userDetails.setCredits(userDetails.getCredits() + plan.getCredits());
					}
					
					userDetails.setSubscriptionEndDate(VideoTranscriptUtils.addMonth(new Date()));
					
					userDetailsRepo.save(userDetails);
				}
			}
		}
	}

	@Override
	public void revokeAccess(MemberRegistration memberRegistration) {
		if(Constants.MEMBERSHIP_REVOKED.equalsIgnoreCase(memberRegistration.getAction())) {
			LOGGER.info("Revoking user via API START");
			Lead lead = memberRegistration.getLead();
			
			UserDetails userDetails = findUserByEmail(lead.getEmail());
			if(userDetails != null) {
				userDetails.setCredits(0);
				userDetails.setUpdatedAt(new Date());
				userDetails.setSubscriptionEndDate(new Date());
				userDetails.setSubscriptionPlan(SubscriptionPlan.Inactive.getName());
				
				userDetailsRepo.save(userDetails);
			}
			LOGGER.info("Revoking user via API END");
		}
		
	}

}
