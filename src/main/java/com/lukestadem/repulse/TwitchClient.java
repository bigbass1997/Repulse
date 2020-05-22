package com.lukestadem.repulse;

import com.lukestadem.repulse.chat.ChatClient;
import com.lukestadem.repulse.events.EventManager;
import com.lukestadem.repulse.helix.HelixClient;
import com.lukestadem.repulse.helix.StreamWatch;
import com.lukestadem.repulse.pubsub.PubSubClient;
import org.codelibs.curl.Curl;
import org.codelibs.curl.CurlResponse;

import javax.json.JsonObject;
import java.time.Duration;

public class TwitchClient implements Disposable {
	
	private final String clientId;
	private final String clientSecret;
	
	private OAuthToken auth;
	private boolean autoRefresh;
	
	private String userAgent;
	private String redirectUrl;
	
	private int timeout;
	
	private EventManager eventManager;
	
	private HelixClient helix;
	private PubSubClient pubsub;
	private ChatClient chat;
	
	private RateLimitManager ratelimit;
	
	private StreamWatch streamWatch;
	
	public TwitchClient(String clientId, String clientSecret, OAuthToken auth, boolean autoRefresh, String userAgent, String redirectUrl, int timeout){
		this.clientId = clientId;
		this.clientSecret = clientSecret;
		
		this.auth = auth;
		this.autoRefresh = autoRefresh;
		
		this.userAgent = userAgent;
		this.redirectUrl = redirectUrl;
		
		this.timeout = timeout;
		
		eventManager = new EventManager();
		
		if(!hasTokenExpired()){
			retrieveUsername();
		}
		
		helix = new HelixClient(this);
		pubsub = new PubSubClient();
		
		if(this.auth.username == null || this.auth.username.isEmpty()){
			this.auth.username = retrieveUsername();
		}
		chat = new ChatClient(this);
		
		ratelimit = new RateLimitManager();
		ratelimit.registerBucket(RateLimitManager.BucketName.ALL, 800, Duration.ofMinutes(1));
		
		streamWatch = new StreamWatch(this);
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
		
		final JsonObject json = JsonUtil.parseJson(res);
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
	
	/**
	 * Attempts to retrieve the username linked to the client's auth token via the validation endpoint. If the client's
	 * auth token already contains a username, that will be returned immediately and no validation will
	 * 
	 * @return the twitch username that matches the client's token, may return null if the token is null or no username was found
	 */
	public String retrieveUsername(){
		if(auth == null){
			return null;
		}
		
		if(auth.username != null && !auth.username.isEmpty()){
			return auth.username;
		}
		
		if(auth.access == null || auth.access.isEmpty()){
			return null;
		}
		
		final CurlResponse res = Curl.get(Constants.OAUTH2 + "validate")
				.header(Constants.AUTHORIZATION, "OAuth " + auth.access)
				.execute();
		
		final JsonObject json = JsonUtil.parseJson(res);
		if(json == null || !json.containsKey("login")){
			return null;
		}
		
		return json.getString("login", null);
	}
	
	public EventManager events(){
		return eventManager;
	}
	
	public HelixClient helix(){
		return helix;
	}
	
	public PubSubClient pubsub(){
		return pubsub;
	}
	
	public ChatClient chat(){
		return chat;
	}
	
	public RateLimitManager ratelimit(){
		return ratelimit;
	}
	
	public StreamWatch watch(){
		return streamWatch;
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
	
	@Override
	public void dispose(){
		streamWatch.dispose();
		chat().dispose();
	}
}
