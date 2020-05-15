package com.lukestadem.repulse.curl;

import com.lukestadem.repulse.Constants;
import com.lukestadem.repulse.RateLimitManager;
import com.lukestadem.repulse.TwitchClient;
import org.codelibs.curl.Curl;
import org.codelibs.curl.CurlException;
import org.codelibs.curl.CurlRequest;

import java.util.ArrayList;
import java.util.List;

public final class CurlTemplates {
	
	public static CurlRequest get(TwitchClient cli, String url){
		return addCommonHeaders(cli, Curl.get(url));
	}
	
	public static CurlRequest post(TwitchClient cli, String url){
		return addCommonHeaders(cli, Curl.post(url));
	}
	
	public static CurlRequest put(TwitchClient cli, String url){
		return addCommonHeaders(cli, Curl.put(url));
	}
	
	public static CurlRequest delete(TwitchClient cli, String url){
		return addCommonHeaders(cli, Curl.delete(url));
	}
	
	public static CurlRequest head(TwitchClient cli, String url){
		return addCommonHeaders(cli, Curl.head(url));
	}
	
	public static CurlRequest options(TwitchClient cli, String url){
		return addCommonHeaders(cli, Curl.options(url));
	}
	
	public static CurlRequest connect(TwitchClient cli, String url){
		return addCommonHeaders(cli, Curl.connect(url));
	}
	
	public static ReusableCurlRequest resuable(TwitchClient cli, Curl.Method method, String url){
		return (ReusableCurlRequest) addCommonHeaders(cli, new ReusableCurlRequest(method, url));
	}
	
	private static CurlRequest addCommonHeaders(TwitchClient cli, CurlRequest req){
		req.header(Constants.CLIENT_ID, cli.getClientId());
		req.header(Constants.AUTHORIZATION, cli.getBearerAccess());
		req.header(Constants.USER_AGENT, cli.getUserAgent());
		
		return req;
	}
	
	
	public static ExpandedCurlResponse performRequest(CurlRequest req){
		try {
			return new ExpandedCurlResponse(req.execute());
		} catch(CurlException e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	/**
	 * Should be used when it's possible for the response to require pagination. If the twitch client and bucket name are
	 * provided, this will check rate limits before requesting the next page. If a rate limit is reached, it will
	 * return what data it has so far.
	 * 
	 * @see #performRequest(CurlRequest) 
	 * @see ExpandedCurlResponse#hasPaginationCursor()
	 * @see RateLimitManager#tryConsume(String, int) 
	 * 
	 * @param req the curl request that will be recycled for each execution
	 * @param ratelimit the rate limit manager
	 * @param bucketName name of the rate limit bucket
	 * @return a list of responses from each request performed, never null but may be empty
	 */
	public static List<ExpandedCurlResponse> performPaginationRequest(final ReusableCurlRequest req, final RateLimitManager ratelimit, final String bucketName){
		final List<ExpandedCurlResponse> responses = new ArrayList<>();
		
		if(ratelimit.tryConsume(bucketName, 1)){
			final ExpandedCurlResponse initalRes = performRequest(req);
			if(initalRes != null && initalRes.isValidJson()){
				responses.add(initalRes);
				
				ExpandedCurlResponse lastRes = initalRes;
				while(lastRes != null && lastRes.hasPaginationCursor()){
					req.removeParams("after");
					req.param("after", lastRes.getPaginationCursor());
					if(ratelimit.tryConsume(bucketName, 1)){
						final ExpandedCurlResponse res = performRequest(req);
						
						if(res == null){
							break;
						}
						
						responses.add(res);
						
						lastRes = res;
					} else {
						break;
					}
				}
			}
		}
		
		return responses;
	}
}
