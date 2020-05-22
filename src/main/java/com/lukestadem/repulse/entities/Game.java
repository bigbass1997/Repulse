package com.lukestadem.repulse.entities;

import javax.json.JsonObject;

public class Game {
	
	public final String name;
	public final String id;
	public final String boxArtUrl;
	
	public Game(JsonObject json){
		this(json.getString("name", ""), json.getString("id", ""), json.getString("box_art_url", ""));
	}
	
	public Game(String name, String id, String boxArtUrl){
		this.name = name;
		this.id = id;
		this.boxArtUrl = boxArtUrl;
	}
	
	@Override
	public String toString(){
		return "{" + name + ", " + id + ", " + boxArtUrl + "}";
	}
}
