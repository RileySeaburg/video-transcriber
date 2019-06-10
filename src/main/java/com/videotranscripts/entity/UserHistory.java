package com.videotranscripts.entity;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Data;

/**
 * @author amitb
 *
 */
@Data
@Entity
@Table(name = "user_history")
public class UserHistory implements Serializable{
	
	private static final long serialVersionUID = -1297046662180534190L;
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "request_id")
	private Long requestId;
	
	@Column(name = "user_id")
	private Integer userId;
	
	@Column(name = "video_name")
	private String videoName;
	
	@Column(name = "transcript_status")
	private String transcriptStatus;
	
	@Column(name="transcript", columnDefinition = "TEXT", length=10000)
	private String transcript;
	
	@Column(name = "created_at")
	private Date createdAt;

}
