package com.lukestadem.repulse.curl;

import com.lukestadem.repulse.JsonUtil;
import org.codelibs.curl.CurlResponse;

import javax.json.JsonObject;
import javax.json.JsonValue;

public class ExpandedCurlResponse {
	
	private CurlError error;
	
	public final CurlResponse res;
	public final String content;
	public final JsonObject json;
	
	public ExpandedCurlResponse(final CurlResponse res){
		this.res = res;
		
		if(res != null){
			content = res.getContentAsString();
			
			if(content != null && !content.isEmpty() && content.startsWith("{") && content.endsWith("}")){
				json = JsonUtil.parseJson(res.getContentAsString());
				
				if(hasError()){
					error = new CurlError(json);
				}
			} else {
				json = null;
			}
		} else {
			content = null;
			json = null;
		}
	}
	
	public boolean isValidJson(){
		return (json != null);
	}
	
	//=== methods below this point aren't absolutely necessary, but are called commonly enough to be beneficial ===\\
	
	public boolean hasData(){
		return json.containsKey("data");
	}
	
	public JsonValue getData(){
		return json.get("data");
	}
	
	public boolean hasError(){
		return (error == null);
	}
	
	public CurlError getError(){
		return error;
	}
	
	public boolean hasPaginationCursor(){
		return (json.containsKey("pagination") && json.getJsonObject("pagination").containsKey("cursor"));
	}
	
	public String getPaginationCursor(){
		return json.getJsonObject("pagination").getString("cursor", "");
	}
}
