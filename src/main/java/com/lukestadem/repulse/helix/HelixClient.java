package com.lukestadem.repulse.helix;

import com.lukestadem.repulse.Constants;
import com.lukestadem.repulse.RateLimitManager;
import com.lukestadem.repulse.TwitchClient;
import com.lukestadem.repulse.Util;
import com.lukestadem.repulse.entities.Game;
import com.lukestadem.repulse.entities.BitsLeaderboard;
import org.codelibs.curl.Curl;
import org.codelibs.curl.CurlRequest;
import org.codelibs.curl.CurlResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.json.JsonArray;
import javax.json.JsonObject;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class HelixClient {
	
	private static final Logger log = LoggerFactory.getLogger(HelixClient.class);
	
	private final TwitchClient twitch;
	
	public enum Period {
		DAY, WEEK, MOTH, YEAR, ALL
	}
	
	public HelixClient(TwitchClient twitch){
		this.twitch = twitch;
	}
	
	/**
	 * @see <a href="https://dev.twitch.tv/docs/api/reference#get-bits-leaderboard">https://dev.twitch.tv/docs/api/reference#get-bits-leaderboard</a>
	 */
	public BitsLeaderboard getBitsLeaderboard(Integer count, Period period, Instant startedAt, String userId){
		if(count == null || count < 1){
			count = 10;
		}
		if(count > 100){
			count = 100;
		}
		
		if(twitch.hasTokenExpired()){
			logAuthToken("getBitsLeaderboard()");
			return null;
		}
		
		final CurlRequest req = Curl.get(Constants.HELIX + "bits/leaderboard");
		twitch.applyCommonHeaders(req);
		
		if(count != null){
			req.param("count", Integer.toString(count));
		}
		if(period != null){
			req.param("period", period.name().toLowerCase());
		}
		if(startedAt != null){
			req.param("started_at", startedAt.toString());
		}
		if(!isNullOrEmpty(userId)){
			req.param("user_id", userId);
		}
		
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
					
					return new BitsLeaderboard(json);
				}
			}
		}
		
		return null;
	}
	
	public List<Game> getGames(String id, String name){
		return getGames(Collections.singletonList(id), Collections.singletonList(name));
	}
	
	public List<Game> getGames(List<String> idList, List<String> nameList){
		if(idList == null && nameList == null){
			return null;
		}
		
		if(twitch.hasTokenExpired()){
			logAuthToken("getGames()");
			return null;
		}
		
		final CurlRequest req = Curl.get(Constants.HELIX + "games");
		twitch.applyCommonHeaders(req);
		
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
	
	private void logAuthToken(String funcName){
		log.warn(funcName + " could not complete due to an expired auth token!");
	}
}
