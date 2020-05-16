package com.lukestadem.repulse.helix;

import com.lukestadem.repulse.Constants;
import com.lukestadem.repulse.JsonUtil;
import com.lukestadem.repulse.RateLimitManager;
import com.lukestadem.repulse.TwitchClient;
import com.lukestadem.repulse.curl.CurlTemplates;
import com.lukestadem.repulse.curl.ExpandedCurlResponse;
import com.lukestadem.repulse.curl.ReusableCurlRequest;
import com.lukestadem.repulse.entities.BitsLeaderboard;
import com.lukestadem.repulse.entities.Clip;
import com.lukestadem.repulse.entities.Game;
import com.lukestadem.repulse.entities.Stream;
import com.lukestadem.repulse.entities.Tag;
import com.lukestadem.repulse.entities.User;
import org.codelibs.curl.Curl;
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
	
	/**
	 * @see <a href="https://dev.twitch.tv/docs/api/reference#get-clips">https://dev.twitch.tv/docs/api/reference#get-clips</a>
	 */
	public List<Clip> getClips(String userId, String gameId, List<String> clipIds, Instant startedAt, Instant endedAt){
		if(isNullOrEmpty(userId) && isNullOrEmpty(gameId) && (clipIds == null || clipIds.size() == 0)){
			throw new IllegalArgumentException("One of the following arguments must be specified: userId, gameId, or clipId!");
		}
		
		if(twitch.hasTokenExpired()){
			logAuthToken("getClips()");
			return null;
		}
		
		final ReusableCurlRequest req = CurlTemplates.resuable(twitch, Curl.Method.GET, Constants.HELIX + "clips");
		
		if(!isNullOrEmpty(userId)){
			req.param("broadcaster_id", userId);
		} else if(!isNullOrEmpty(gameId)){
			req.param("game_id", gameId);
		} else if(clipIds.size() > 0){
			clipIds.forEach(clipId -> {
				if(!clipId.isEmpty()){
					req.param("id", clipId);
				}
			});
		}
		
		if(startedAt != null && endedAt != null){ // both must be included, otherwise Twitch ignores whichever param that was given
			req.param("started_at", startedAt.toString());
			req.param("ended_at", endedAt.toString());
		}
		
		req.param("first", "100");
		
		final List<Clip> clips = new ArrayList<>();
		
		final List<ExpandedCurlResponse> responses = CurlTemplates.performPaginationRequest(req, twitch.ratelimit(), RateLimitManager.BucketName.ALL.id);
		responses.forEach(res -> {
			if(res.isValidJson() && res.hasData()){
				res.getData().asJsonArray().forEach(value -> {
					if(value instanceof JsonObject){
						clips.add(new Clip(value.asJsonObject()));
					}
				});
			}
		});
		
		return clips;
	}
	
	public List<Game> getGames(String id, String name){
		return getGames(Collections.singletonList(id), Collections.singletonList(name));
	}
	
	public List<Game> getGames(List<String> gameIds, List<String> gameNames){
		if(gameIds == null && gameNames == null){
			return null;
		}
		
		if(twitch.hasTokenExpired()){
			logAuthToken("getGames()");
			return null;
		}
		
		final CurlRequest req = CurlTemplates.get(twitch, Constants.HELIX + "games");
		
		gameIds.forEach(id -> {
			if(!isNullOrEmpty(id)){
				req.param("id", id);
			}
		});
		
		gameNames.forEach(name -> {
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
	
	public List<Stream> getStreams(String gameId, String userId, String username, String language){
		return getStreams(Collections.singletonList(gameId), Collections.singletonList(userId), Collections.singletonList(username), Collections.singletonList(language));
	}
	
	public List<Stream> getStreams(List<String> gameIds, List<String> userIds, List<String> usernames, List<String> languages){
		if(gameIds == null && userIds == null && usernames == null && languages == null){
			throw new IllegalArgumentException("getStreams() was provided all null data");
		}
		
		if(twitch.hasTokenExpired()){
			logAuthToken("getStreams()");
			return null;
		}
		
		final ReusableCurlRequest req = CurlTemplates.resuable(twitch, Curl.Method.GET, Constants.HELIX + "streams");
		
		if(gameIds != null && gameIds.size() > 10){
			gameIds = gameIds.subList(0, 10);
		}
		if(userIds != null && userIds.size() > 100){
			userIds = userIds.subList(0, 100);
		}
		if(usernames != null && usernames.size() > 100){
			usernames = usernames.subList(0, 100);
		}
		if(languages != null && languages.size() > 100){
			languages = languages.subList(0, 100);
		}
		
		gameIds.forEach(id -> {
			if(!isNullOrEmpty(id)){
				req.param("game_id", id);
			}
		});
		userIds.forEach(id -> {
			if(!isNullOrEmpty(id)){
				req.param("user_id", id);
			}
		});
		usernames.forEach(name -> {
			if(!isNullOrEmpty(name)){
				req.param("user_login", name);
			}
		});
		languages.forEach(language -> {
			if(!isNullOrEmpty(language)){
				req.param("language", language);
			}
		});
		
		final List<Stream> streams = new ArrayList<>();
		
		final List<ExpandedCurlResponse> responses = CurlTemplates.performPaginationRequest(req, twitch.ratelimit(), RateLimitManager.BucketName.ALL.id);
		responses.forEach(res -> {
			if(res.isValidJson() && res.hasData()){
				res.getData().asJsonArray().forEach(value -> {
					if(value instanceof JsonObject){
						streams.add(new Stream(value.asJsonObject()));
					}
				});
			}
		});
		
		return streams;
	}
	
	public List<Tag> getStreamTags(String userId){
		if(isNullOrEmpty(userId)){
			return null;
		}
		
		if(twitch.hasTokenExpired()){
			logAuthToken("getGames()");
			return null;
		}
		
		final CurlRequest req = CurlTemplates.get(twitch, Constants.HELIX + "streams/tags");
		
		req.param("broadcaster_id", userId);
		
		if(twitch.ratelimit().tryConsume(RateLimitManager.BucketName.ALL, 1)){
			final ExpandedCurlResponse res = CurlTemplates.performRequest(req);
			
			if(res.isValidJson() && res.hasData()){
				final List<Tag> tags = new ArrayList<>();
				res.getData().asJsonArray().forEach(value -> {
					if(value instanceof JsonObject){
						tags.add(new Tag(value.asJsonObject()));
					}
				});
				return tags;
			} else if(res.hasError()){
				log.error(res.getError().toString());
			}
		}
		
		return null;
	}
	
	public List<User> getUsers(String userId, String username){
		return getUsers(Collections.singletonList(userId), Collections.singletonList(username));
	}
	
	public List<User> getUsers(List<String> userIds, List<String> usernames){
		if(userIds == null && usernames == null){
			throw new IllegalArgumentException("getUsers() was provided all null data");
		}
		
		if(twitch.hasTokenExpired()){
			logAuthToken("getUsers()");
			return null;
		}
		
		final CurlRequest req = CurlTemplates.get(twitch, Constants.HELIX + "users");
		
		int totalCount = 0;
		if(userIds != null){
			if(userIds.size() > 100){
				userIds = userIds.subList(0, 100);
			}
			totalCount += userIds.size();
		}
		if(usernames != null && totalCount + usernames.size() > 100){
			usernames = usernames.subList(0, 100 - totalCount);
		}
		
		userIds.forEach(userId -> {
			if(!isNullOrEmpty(userId)){
				req.param("id", userId);
			}
		});
		usernames.forEach(username -> {
			if(!isNullOrEmpty(username)){
				req.param("login", username);
			}
		});
		
		if(twitch.ratelimit().tryConsume(RateLimitManager.BucketName.ALL, 1)){
			final ExpandedCurlResponse res = CurlTemplates.performRequest(req);
			
			if(res.isValidJson() && res.hasData()){
				final List<User> users = new ArrayList<>();
				res.getData().asJsonArray().forEach(value -> {
					if(value instanceof JsonObject){
						users.add(new User(value.asJsonObject()));
					}
				});
				return users;
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
