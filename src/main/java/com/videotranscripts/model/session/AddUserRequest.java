package com.videotranscripts.model.session;

import javax.validation.constraints.NotNull;

import lombok.Data;

/**
 * @author amitb
 *
 */
@Data
public class AddUserRequest {
	
	@NotNull
	private String firstName;
	@NotNull
	private String lastName;
	@NotNull
	private String email;
	@NotNull
	private String password;
	@NotNull
	private String plan;
	private String authKey;

}
