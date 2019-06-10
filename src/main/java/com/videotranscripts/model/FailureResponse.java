package com.videotranscripts.model;

import org.springframework.http.HttpStatus;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @author amitb
 *
 */
@Data
@AllArgsConstructor
public class FailureResponse {
	
	private String errorMessage;
	private HttpStatus status;

}
