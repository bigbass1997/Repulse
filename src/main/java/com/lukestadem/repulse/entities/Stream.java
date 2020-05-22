package com.lukestadem.repulse.entities;

import com.lukestadem.repulse.TwitchClient;

import javax.json.JsonObject;
import javax.json.JsonString;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class Stream {
	
	public String type;
	public String streamId;
	public String gameId;
	public String userId;
	public String username;
	public String language;
	public String title;
	public String thumbnailUrl;
	public int viewerCount;
	public Instant startedAt;
	public List<String> tagIds;
	
	public Stream(){
		this("", "", "", "", "", "", "", "", 0, Instant.EPOCH, new ArrayList<>());
	}
	
	public Stream(String type, String streamId, String gameId, String userId, String username, String language, String title, String thumbnailUrl, int viewerCount, Instant startedAt, List<String> tagIds){
		this.type = type;
		this.streamId = streamId;
		this.gameId = gameId;
		this.userId = userId;
		this.username = username;
		this.language = language;
		this.title = title;
		this.thumbnailUrl = thumbnailUrl;
		this.viewerCount = viewerCount;
		this.startedAt = startedAt;
		this.tagIds = tagIds;
	}
	
	public Stream(JsonObject json){
		type = json.getString("type", "");
		streamId = json.getString("id", "");
		gameId = json.getString("game_id", "");
		userId = json.getString("user_id", "");
		username = json.getString("user_name", "");
		language = json.getString("language", "");
		title = json.getString("title", "");
		thumbnailUrl = json.getString("thumbnail_url", "");
		viewerCount = json.getInt("viewer_count", 0);
		startedAt = Instant.parse(json.getString("started_at", "1970-01-01T00:00:00Z"));
		tagIds = new ArrayList<>();
		if(json.containsKey("tag_ids") && !json.isNull("tag_ids")){
			json.getJsonArray("tag_ids").forEach(value -> {
				if(value instanceof JsonString){
					tagIds.add(((JsonString) value).getString());
				}
			});
		}
	}
	
	public String getStreamPreviewUrl(){
		return "https://static-cdn.jtvnw.net/previews-ttv/live_user_" + username.toLowerCase() + "-1920x1080.jpg";
	}
	
	public boolean isLive(){
		return type.equals("live");
	}
	
	public Game getGame(TwitchClient cli){
		final List<Game> games = cli.helix().getGames(gameId, null);
		if(games != null && games.size() > 0){
			return games.get(0);
		}
		
		return null;
	}
	
	public User getUser(TwitchClient cli){
		final List<User> users = cli.helix().getUsers(userId, null);
		if(users != null && users.size() > 0){
			return users.get(0);
		}
		
		return null;
	}
	
	public List<Tag> getTags(TwitchClient cli){
		return cli.helix().getStreamTags(userId);
	}
}
