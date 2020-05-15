package com.lukestadem.repulse.curl;

import org.codelibs.curl.Curl;
import org.codelibs.curl.CurlRequest;

public class ReusableCurlRequest extends CurlRequest {
	
	public ReusableCurlRequest(Curl.Method method, String url){
		super(method, url);
	}
	
	public ReusableCurlRequest removeParams(String key){
		paramList.removeIf(param -> {
			return param.startsWith(encode(key));
		});
		
		return this;
	}
}
