package com.videotranscripts.model.session;

import javax.validation.constraints.NotNull;

import lombok.Data;

/**
 * @author amitb
 *
 */
@Data
public class LoginApiRequest {
	
	@NotNull
	private String email;
	@NotNull
	private String password;

}
