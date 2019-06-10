package com.videotranscripts.model.kartra;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

/**
 * @author amitb
 *
 */
@Data
public class Lead {
	
	@JsonProperty("first_name")
	private String firstName;
	@JsonProperty("last_name")
	private String lastName;
	private String email;

}
