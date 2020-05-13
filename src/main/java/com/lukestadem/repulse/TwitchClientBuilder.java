package com.lukestadem.repulse;

public class TwitchClientBuilder {
	
	private String clientId;
	private String clientSecret;
	
	private OAuthToken auth;
	private boolean autoRefresh;
	
	private String userAgent;
	private String redirectUrl;
	
	private int timeout;
	
	private TwitchClientBuilder(){
		clientId = "";
		clientSecret = "";
		auth = new OAuthToken();
		autoRefresh = false;
		userAgent = "Default Repulse Agent";
		redirectUrl = "http://localhost";
		timeout = 10000;
	}
	
	public static TwitchClientBuilder builder(){
		return new TwitchClientBuilder();
	}
	
	public TwitchClientBuilder withClientId(String clientId){
		this.clientId = clientId;
		return this;
	}
	
	public TwitchClientBuilder withClientSecret(String clientSecret){
		this.clientSecret = clientSecret;
		return this;
	}
	
	public TwitchClientBuilder withToken(String accessToken){
		auth.access = accessToken;
		return this;
	}
	
	public TwitchClientBuilder withToken(String accessToken, String refreshToken, int expiresIn){
		auth.access = accessToken;
		auth.refresh = refreshToken;
		auth.setExpiresIn(expiresIn);
		return this;
	}
	
	public TwitchClientBuilder withToken(String accessToken, String refreshToken, long expiresAt){
		auth.access = accessToken;
		auth.refresh = refreshToken;
		auth.expiresAt = expiresAt;
		return this;
	}
	
	/**
	 * Provide {@code true} if you want the {@link TwitchClient} to automatically refresh the auth token in the
	 * case that it has expired whenever {@link TwitchClient#hasTokenExpired()} is called.
	 * 
	 * @param autoRefresh
	 * @return builder for chaining
	 */
	public TwitchClientBuilder withAutoRefresh(boolean autoRefresh){
		this.autoRefresh = autoRefresh;
		return this;
	}
	
	public TwitchClientBuilder withUserAgent(String userAgent){
		this.userAgent = userAgent;
		return this;
	}
	
	public TwitchClientBuilder withRedirectUrl(String redirectUrl){
		this.redirectUrl = redirectUrl;
		return this;
	}
	
	public TwitchClientBuilder withTimeout(int timeout){
		this.timeout = timeout;
		return this;
	}
	
	public TwitchClient build(){
		return new TwitchClient(clientId, clientSecret, auth, autoRefresh, userAgent, redirectUrl, timeout);
	}
}
