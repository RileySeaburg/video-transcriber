package com.videotranscripts.model;

/**
 * @author amitb
 *
 */
public enum SubscriptionPlan {
	
	Inactive(0, "Inactive"),
	Basic(10, "Basic"),
	Premium(30, "Premium"),
	Business(999, "Business");
	
	private int credits;
	private String name;
	
	SubscriptionPlan(int credits, String name) {
		this.credits = credits;
		this.name = name;
	}
	
	public int getCredits() {
		return credits;
	}
	
	public String getName() {
		return name;
	}

}
