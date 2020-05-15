package com.lukestadem.repulse.entities;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.json.JsonArray;
import javax.json.JsonObject;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

public class BitsLeaderboard {
	
	private static final Logger log = LoggerFactory.getLogger(BitsLeaderboard.class);
	
	public List<BitsLeaderboardEntry> entries;
	public Instant startedAt;
	public Instant endedAt;
	
	public BitsLeaderboard(){
		entries = new ArrayList<>();
		startedAt = Instant.EPOCH;
		endedAt = Instant.EPOCH;
	}
	
	public BitsLeaderboard(List<BitsLeaderboardEntry> entries, Instant startedAt, Instant endedAt){
		this.entries = entries;
		this.startedAt = startedAt;
		this.endedAt = endedAt;
	}
	
	public BitsLeaderboard(JsonObject json){
		this();
		
		final JsonArray jsonEntries = json.getJsonArray("data");
		if(jsonEntries != null){
			jsonEntries.forEach(value -> {
				if(value instanceof JsonObject){
					entries.add(new BitsLeaderboardEntry(value.asJsonObject()));
				}
			});
		}
		
		final JsonObject dateRange = json.getJsonObject("date_range");
		if(dateRange != null){
			try {
				startedAt = Instant.parse(dateRange.getString("started_at", "1970-01-01T00:00:00Z"));
				endedAt = Instant.parse(dateRange.getString("ended_at", "1970-01-01T00:00:00Z"));
			} catch(DateTimeParseException e) {
				log.warn("Date parsing failed. Json is likely corrupt or formatted incorrectly: " + dateRange.toString());
				e.printStackTrace();
			}
		}
	}
	
	
	
	public static final class BitsLeaderboardEntry {
		
		public String userId;
		public String username;
		public int rank;
		public int score;
		
		public BitsLeaderboardEntry(){
			this("", "", -1, -1);
		}
		
		public BitsLeaderboardEntry(String userId, String username, int rank, int score){
			this.userId = userId;
			this.username = username;
			this.rank = rank;
			this.score = score;
		}
		
		public BitsLeaderboardEntry(JsonObject json){
			userId = json.getString("user_id", "");
			username = json.getString("user_name", "");
			rank = json.getInt("rank", -1);
			score = json.getInt("score", -1);
		}
	}
}
