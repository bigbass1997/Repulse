package com.lukestadem.repulse.entities;

import javax.json.JsonObject;
import java.time.Instant;

public class Clip {
	
	public String clipId;
	public String url;
	public String embedUrl;
	public String broadcasterId;
	public String broadcasterName;
	public String creatorId;
	public String creatorName;
	public String videoId;
	public String gameId;
	public String language;
	public String title;
	public int viewCount;
	public Instant createdAt;
	public String thumbnailUrl;
	
	public Clip(){
		this("", "", "", "", "", "", "", "", "", "", "", "", 0, Instant.EPOCH);
	}
	
	public Clip(String clipId, String url, String embedUrl, String broadcasterId, String broadcasterName,
				String creatorId, String creatorName, String videoId, String gameId, String language,
				String title, String thumbnailUrl, int viewCount, Instant createdAt){
		
		this.clipId = clipId;
		this.url = url;
		this.embedUrl = embedUrl;
		this.broadcasterId = broadcasterId;
		this.broadcasterName = broadcasterName;
		this.creatorId = creatorId;
		this.creatorName = creatorName;
		this.videoId = videoId;
		this.gameId = gameId;
		this.language = language;
		this.title = title;
		this.thumbnailUrl = thumbnailUrl;
		this.viewCount = viewCount;
		this.createdAt = createdAt;
	}
	
	public Clip(JsonObject json){
		clipId = json.getString("id", "");
		url = json.getString("url", "");
		embedUrl = json.getString("embed_url", "");
		broadcasterId = json.getString("broadcaster_id", "");
		broadcasterName = json.getString("broadcaster_name", "");
		creatorId = json.getString("creator_id", "");
		creatorName = json.getString("creator_name", "");
		videoId = json.getString("video_id", "");
		gameId = json.getString("game_id", "");
		language = json.getString("language", "");
		title = json.getString("title", "");
		thumbnailUrl = json.getString("thumbnail_url", "");
		viewCount = json.getInt("view_count", 0);
		createdAt = Instant.parse(json.getString("created_at", "1970-01-01T00:00:00Z"));
	}
}
