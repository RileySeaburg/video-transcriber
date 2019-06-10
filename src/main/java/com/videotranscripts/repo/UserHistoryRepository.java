package com.videotranscripts.repo;

import java.util.List;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.videotranscripts.entity.UserHistory;

/**
 * @author amitb
 *
 */
@Repository
public interface UserHistoryRepository extends CrudRepository<UserHistory, Long> {
	
	public List<UserHistory> findByUserIdOrderByCreatedAtDesc(Integer userId);

}
