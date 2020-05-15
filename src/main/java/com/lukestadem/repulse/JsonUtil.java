package com.lukestadem.repulse;

import org.codelibs.curl.CurlResponse;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import java.io.StringReader;

public class JsonUtil {
	
	public static JsonObject parseJson(CurlResponse res){
		if(res == null){
			return null;
		}
		
		return parseJson(res.getContentAsString());
	}
	
	public static JsonObject parseJson(String json){
		if(json == null){
			return null;
		}
		
		final JsonReader jsonReader = Json.createReader(new StringReader(json));
		final JsonObject obj = jsonReader.readObject();
		jsonReader.close();
		
		return obj;
	}
	
	public static boolean hasError(JsonObject json){
		return (json == null || json.containsKey("error") || (json.containsKey("status") && json.getInt("status") >= 400));
	}
}
