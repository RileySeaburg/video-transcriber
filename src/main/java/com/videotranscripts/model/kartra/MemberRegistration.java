package com.videotranscripts.model.kartra;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

/**
 * @author amitb
 *
 */
@Data
public class MemberRegistration {
	
	private String action;
	@JsonProperty("action_details")
	private ActionDetails actionDetails;
	private Lead lead;

}
