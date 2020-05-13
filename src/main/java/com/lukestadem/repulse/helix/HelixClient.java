package com.lukestadem.repulse.helix;

import com.lukestadem.repulse.Constants;
import com.lukestadem.repulse.RateLimitManager;
import com.lukestadem.repulse.TwitchClient;
import com.lukestadem.repulse.Util;
import com.lukestadem.repulse.entities.Game;
import org.codelibs.curl.Curl;
import org.codelibs.curl.CurlRequest;
import org.codelibs.curl.CurlResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.json.JsonArray;
import javax.json.JsonObject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class HelixClient {
	
	private static final Logger log = LoggerFactory.getLogger(HelixClient.class);
	
	private final TwitchClient twitch;
	
	public HelixClient(TwitchClient twitch){
		this.twitch = twitch;
	}
	
	public List<Game> getGames(String id, String name){
		return getGames(Collections.singletonList(id), Collections.singletonList(name));
	}
	
	public List<Game> getGames(List<String> idList, List<String> nameList){
		if(idList == null && nameList == null){
			return null;
		}
		
		if(twitch.hasTokenExpired()){
			log.warn("getGames() could not complete due to an expired auth token!");
			return null;
		}
		
		final CurlRequest req = Curl.get(Constants.HELIX + "games");
		
		idList.forEach(id -> {
			if(!isNullOrEmpty(id)){
				req.param("id", id);
			}
		});
		
		nameList.forEach(name -> {
			if(!isNullOrEmpty(name)){
				req.param("name", name);
			}
		});
		
		req.header(Constants.CLIENT_ID, twitch.getClientId());
		req.header(Constants.AUTHORIZATION, twitch.getBearerAccess());
		req.header(Constants.USER_AGENT, twitch.getUserAgent());
		
		if(twitch.ratelimit().tryConsume(RateLimitManager.BucketName.ALL, 1)){
			final CurlResponse res = req.execute();
			
			if(res != null){
				final String content = res.getContentAsString();
				
				if(content != null && !content.isEmpty() && content.startsWith("{") && content.endsWith("}")){
					final JsonObject json = Util.parseJson(res.getContentAsString());
					if(!json.containsKey("data")){
						log.warn("Json \"data\" was not found! " + json.toString());
						return null;
					}
					
					final JsonArray data = json.getJsonArray("data");
					
					final List<Game> games = new ArrayList<>();
					data.forEach(value -> {
						if(value instanceof JsonObject){
							if(!Util.hasError(value.asJsonObject())){
								games.add(new Game(value.asJsonObject()));
							}
						}
					});
					
					return games;
				}
			}
		}
		
		return null;
	}
	
	private boolean isNullOrEmpty(String str){
		return (str == null || str.isEmpty());
	}
}
