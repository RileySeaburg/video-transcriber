package com.videotranscripts.repo;

import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.videotranscripts.entity.UserDetails;

/**
 * @author amitb
 *
 */
@Repository
public interface UserDetailsRepository extends CrudRepository<UserDetails, Integer> {
	
	public UserDetails findByEmailAndPassword(String email, String password);
	public UserDetails findByEmail(String email);
	public List<UserDetails> findBySubscriptionEndDateBetween(Date startDate, Date endDate);
	
	@Query("select u from UserDetails u where u.isAdmin = 0")
	public List<UserDetails> getAllUsers();

}
