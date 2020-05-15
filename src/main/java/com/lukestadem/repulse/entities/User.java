package com.lukestadem.repulse.entities;

import com.lukestadem.repulse.TwitchClient;

import javax.json.JsonObject;
import java.util.List;

public class User {
	
	public String id;
	public String name;
	public String displayName;
	public String type;
	public String broadcasterType;
	public String description;
	public String profileImageUrl;
	public String offlineImageUrl;
	public int viewCount;
	public String email;
	
	public User(){
		this("", "", "", "", "", "", "", "", 0, "");
	}
	
	public User(String id, String name, String displayName, String type, String broadcasterType, String description, String profileImageUrl, String offlineImageUrl, int viewCount, String email){
		this.id = id;
		this.name = name;
		this.displayName = displayName;
		this.type = type;
		this.broadcasterType = broadcasterType;
		this.description = description;
		this.profileImageUrl = profileImageUrl;
		this.offlineImageUrl = offlineImageUrl;
		this.viewCount = viewCount;
		this.email = email;
	}
	
	public User(JsonObject json){
		id = json.getString("id", "");
		name = json.getString("login", "");
		displayName = json.getString("display_name", "");
		type = json.getString("type", "");
		broadcasterType = json.getString("broadcaster_type", "");
		description = json.getString("description", "");
		profileImageUrl = json.getString("profile_image_url", "");
		offlineImageUrl = json.getString("offline_image_url", "");
		viewCount = json.getInt("view_count", 0);
		email = json.getString("email", "");
	}
	
	public Stream getStream(TwitchClient cli){
		final List<Stream> streams = cli.helix().getStreams(null, id, null, null);
		if(streams != null && streams.size() > 0){
			return streams.get(0);
		}
		
		return null;
	}
}
