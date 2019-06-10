package com.videotranscripts.exception;

import com.videotranscripts.model.FailureResponse;

import lombok.Getter;

/**
 * @author amitb
 *
 */
public class AuthException extends RuntimeException {

	private static final long serialVersionUID = -6670447546710022066L;
	
	@Getter
	private FailureResponse failureResponse;

	public AuthException(FailureResponse failureResponse) {
		this.failureResponse = failureResponse;
	}

}
