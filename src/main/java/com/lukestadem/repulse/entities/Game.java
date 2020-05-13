package com.lukestadem.repulse.entities;

import javax.json.JsonObject;

public class Game {
	
	private String name;
	private String id;
	private String boxArtUrl;
	
	public Game(JsonObject json){
		this(json.getString("name", ""), json.getString("id", ""), json.getString("box_art_url", ""));
	}
	
	public Game(String name, String id, String boxArtUrl){
		this.name = name;
		this.id = id;
		this.boxArtUrl = boxArtUrl;
	}
	
	public String getName(){
		return name;
	}
	
	public String getId(){
		return id;
	}
	
	public String getBoxArtUrl(){
		return boxArtUrl;
	}
	
	@Override
	public String toString(){
		return "{" + name + ", " + id + ", " + boxArtUrl + "}";
	}
}
