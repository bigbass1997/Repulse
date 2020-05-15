package com.lukestadem.repulse.curl;

import com.lukestadem.repulse.Constants;
import com.lukestadem.repulse.TwitchClient;
import org.codelibs.curl.Curl;
import org.codelibs.curl.CurlException;
import org.codelibs.curl.CurlRequest;

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
}
