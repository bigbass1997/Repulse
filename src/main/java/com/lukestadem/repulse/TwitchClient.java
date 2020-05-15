package com.lukestadem.repulse;

import com.lukestadem.repulse.helix.HelixClient;
import com.lukestadem.repulse.pubsub.PubSubClient;
import org.codelibs.curl.Curl;
import org.codelibs.curl.CurlRequest;
import org.codelibs.curl.CurlResponse;

import javax.json.JsonObject;
import java.time.Duration;

public class TwitchClient {
	
	private final String clientId;
	private final String clientSecret;
	
	private OAuthToken auth;
	private boolean autoRefresh;
	
	private String userAgent;
	private String redirectUrl;
	
	private int timeout;
	
	private HelixClient helix;
	private PubSubClient pubsub;
	
	private RateLimitManager ratelimit;
	
	public TwitchClient(String clientId, String clientSecret, OAuthToken auth, boolean autoRefresh, String userAgent, String redirectUrl, int timeout){
		this.clientId = clientId;
		this.clientSecret = clientSecret;
		
		this.auth = auth;
		this.autoRefresh = autoRefresh;
		
		this.userAgent = userAgent;
		this.redirectUrl = redirectUrl;
		
		this.timeout = timeout;
		
		helix = new HelixClient(this);
		pubsub = new PubSubClient();
		
		ratelimit = new RateLimitManager();
		ratelimit.registerBucket(RateLimitManager.BucketName.ALL, 800, Duration.ofMinutes(1));
	}
	
	/**
	 * Attempts to retrieve and update this client's auth token.
	 * 
	 * @return the new token, or the existing token if an error occurs
	 */
	public OAuthToken refreshToken(){
		if(auth.refresh == null || auth.refresh.isEmpty()){
			return auth;
		}
		
		final CurlResponse res = Curl.post(Constants.OAUTH2 + "token")
				.param("grant_type", "refresh_token")
				.param("refresh_token", auth.refresh)
				.param("client_id", clientId)
				.param("client_secret", clientSecret)
				.execute();
		
		final JsonObject json = Util.parseJson(res);
		if(json == null){
			return auth;
		}
		
		auth.access = json.getString("access_token", auth.access);
		auth.refresh = json.getString("refresh_token", auth.refresh);
		
		if(json.containsKey("expires_in")){
			auth.setExpiresIn(json.getInt("expires_in"));
		}
		
		return auth;
	}
	
	/**
	 * Returns whether or not the auth token has expired. If {@link #autoRefresh} is enabled, and the token happens to
	 * be expired, this will also attempt to refresh the token before returning.
	 * 
	 * @return whether or not the auth token is expired
	 */
	public boolean hasTokenExpired(){
		return hasTokenExpired(autoRefresh);
	}
	
	/**
	 * Returns whether or not the auth token has expired. If {@code shouldRefresh == true} and the token has expired,
	 * then {@link #refreshToken()} will be called.
	 * 
	 * @param shouldRefresh if the token should be automatically refreshed if the token is also expired
	 * @return whether or not the auth token is expired
	 */
	public boolean hasTokenExpired(boolean shouldRefresh){
		if(auth.hasExpired() && shouldRefresh){
			refreshToken();
		}
		
		return auth.hasExpired();
	}
	
	public CurlRequest applyCommonHeaders(CurlRequest req){
		req.header(Constants.CLIENT_ID, getClientId());
		req.header(Constants.AUTHORIZATION, getBearerAccess());
		req.header(Constants.USER_AGENT, getUserAgent());
		
		return req;
	}
	
	public HelixClient helix(){
		return helix;
	}
	
	public PubSubClient pubsub(){
		return pubsub;
	}
	
	public RateLimitManager ratelimit(){
		return ratelimit;
	}
	
	public String getClientId(){
		return clientId;
	}
	
	public String getClientSecret(){
		return clientSecret;
	}
	
	public OAuthToken getOAuthToken(){
		return auth;
	}
	
	/**
	 * Prefixes the access token with "Bearer ". Useful when creating curl requests.
	 * 
	 * @return "Bearer " + accessToken
	 */
	public String getBearerAccess(){
		return "Bearer " + auth.access;
	}
	
	public String getUserAgent(){
		return userAgent;
	}
	
	public String getRedirectUrl(){
		return redirectUrl;
	}
	
	public int getTimeout(){
		return timeout;
	}
}
