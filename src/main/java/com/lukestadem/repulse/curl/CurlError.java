package com.lukestadem.repulse.curl;

import javax.json.JsonObject;

public class CurlError {
	
	public final String message;
	public final String error;
	public final int status;
	
	public CurlError(JsonObject json){
		message = json.getString("message", "");
		error = json.getString("error", "");
		status = json.getInt("status", -1);
	}
	
	@Override
	public String toString(){
		return "{" + message + ", " + error + ", " + status + "}";
	}
}
