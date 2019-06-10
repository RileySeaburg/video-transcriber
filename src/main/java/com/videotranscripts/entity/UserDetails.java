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
@Table(name = "user_details")
public class UserDetails implements Serializable {
	
	private static final long serialVersionUID = 5525711225354185977L;
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "user_id")
	private int userId;
	
	@Column(name = "email")
	private String email;
	
	@Column(name = "first_name")
	private String firstName;
	
	@Column(name = "last_name")
	private String lastName;
	
	@Column(name = "password")
	private String password;
	
	@Column(name = "is_admin")
	private int isAdmin;
	
	@Column(name = "created_at")
	private Date createdAt;
	
	@Column(name = "updated_at")
	private Date updatedAt;
	
	@Column(name = "subscription_end_date")
	private Date subscriptionEndDate;
	
	@Column(name = "subscription_plan")
	private String subscriptionPlan;
	
	@Column(name = "credits")
	private int credits;

}
