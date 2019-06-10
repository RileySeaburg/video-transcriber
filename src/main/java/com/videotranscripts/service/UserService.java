package com.videotranscripts.service;

import java.util.List;

import com.videotranscripts.entity.UserDetails;
import com.videotranscripts.entity.UserHistory;
import com.videotranscripts.model.kartra.MemberRegistration;
import com.videotranscripts.model.session.AddUserRequest;
import com.videotranscripts.model.session.LoginApiRequest;

/**
 * @author amitb
 *
 */
public interface UserService {
	
	UserDetails login(LoginApiRequest loginRequest);
	UserHistory findHistoryByRequestId(Long requestId);
	UserHistory addInHistory(UserHistory userHistory);
	List<UserHistory> getUserHistory(Integer userId);
	List<UserDetails> getAllUsersList();
	UserDetails addUser(AddUserRequest addUserRequest);
	UserDetails updateUserCredits(UserDetails userDetails);
	List<UserHistory> getAllUserHistory();
	Long getUserCount();
	UserDetails registerUserByApi(MemberRegistration memberRegistration);
	UserDetails findUserByEmail(String email);
	List<UserDetails> findBySubscriptionEndDate();
	UserDetails save(UserDetails userDetails);
	void updateUserStatus();
	void revokeAccess(MemberRegistration memberRegistration);

}
