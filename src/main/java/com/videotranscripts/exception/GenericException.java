package com.videotranscripts.exception;

import com.videotranscripts.model.FailureResponse;

import lombok.Getter;

/**
 * @author amitb
 *
 */
public class GenericException extends RuntimeException {

	private static final long serialVersionUID = 8729878093601396272L;
	
	@Getter
	private FailureResponse failureResponse;

	public GenericException(FailureResponse failureResponse) {
		this.failureResponse = failureResponse;
	}

}
