package com.videotranscripts.model.kartra;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

/**
 * @author amitb
 *
 */
@Data
public class Membership {
	
	@JsonProperty("level_name")
	private String levelName;

}
