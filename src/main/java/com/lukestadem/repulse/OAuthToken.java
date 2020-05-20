package com.lukestadem.repulse;

public class OAuthToken {
	
	public String access;
	public String refresh;
	
	/** epoch time at which the access token expires */
	public long expiresAt;
	
	/** the username linked to this token */
	public String username;
	
	/**
	 * Creates an empty OAuthToken, with {@code expiresAt} set to {@code Long.MAX_VALUE}.
	 */
	public OAuthToken(){
		access = "";
		refresh = "";
		expiresAt = Long.MAX_VALUE;
		username = "";
	}
	
	/**
	 * @param access access token
	 * @param refresh refresh token
	 * @param expiresIn time in <b>seconds</b> when access token expires
	 * @param username username linked to this token
	 */
	public OAuthToken(String access, String refresh, int expiresIn, String username){
		this(access, refresh, System.currentTimeMillis() + (expiresIn * 1000), username);
	}
	
	/**
	 * @param access access token
	 * @param refresh refresh token
	 * @param expiresAt epoch time when access token expires
	 * @param username username linked to this token
	 */
	public OAuthToken(String access, String refresh, long expiresAt, String username){
		this.access = access;
		this.refresh = refresh;
		this.expiresAt = expiresAt;
		this.username = username;
	}
	
	/**
	 * Sets this token's {@code expiresAt} field based on the current epoch plus the provided number of <b>seconds</b>.
	 * 
	 * @param expiresIn time in <b>seconds</b> when access token expires
	 */
	public void setExpiresIn(int expiresIn){
		this.expiresAt = System.currentTimeMillis() + (expiresIn * 1000);
	}
	
	public boolean hasExpired(){
		return (System.currentTimeMillis() >= expiresAt);
	}
	
	@Override
	public String toString(){
		return "{" + access + ", " + refresh + ", " + expiresAt + ", " + username + "}";
	}
}
