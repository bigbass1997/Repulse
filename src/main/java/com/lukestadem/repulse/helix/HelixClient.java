package com.lukestadem.repulse.helix;

import com.lukestadem.repulse.Constants;
import com.lukestadem.repulse.JsonUtil;
import com.lukestadem.repulse.RateLimitManager;
import com.lukestadem.repulse.TwitchClient;
import com.lukestadem.repulse.curl.CurlTemplates;
import com.lukestadem.repulse.curl.ExpandedCurlResponse;
import com.lukestadem.repulse.entities.Game;
import com.lukestadem.repulse.entities.BitsLeaderboard;
import org.codelibs.curl.CurlRequest;
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
		
		final CurlRequest req = CurlTemplates.get(twitch, Constants.HELIX + "bits/leaderboard");
		
		if(count != null){
			req.param("count", count.toString());
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
			final ExpandedCurlResponse res = CurlTemplates.performRequest(req);
			
			if(res.isValidJson() && res.hasData()){
				return new BitsLeaderboard(res.json);
			} else if(res.hasError()){
				log.error(res.getError().toString());
			}
		}
		
		return null;
	}
	
	//TODO public Something createClip(){}
	
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
		
		final CurlRequest req = CurlTemplates.get(twitch, Constants.HELIX + "games");
		
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
			final ExpandedCurlResponse res = CurlTemplates.performRequest(req);
			
			if(res.isValidJson() && res.hasData()){
				final JsonArray data = res.getData().asJsonArray();
				
				final List<Game> games = new ArrayList<>();
				data.forEach(value -> {
					if(value instanceof JsonObject){
						if(!JsonUtil.hasError(value.asJsonObject())){
							games.add(new Game(value.asJsonObject()));
						}
					}
				});
				
				return games;
			} else if(res.hasError()){
				log.error(res.getError().toString());
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
