package com.lukestadem.repulse.entities;

import javax.json.JsonObject;
import javax.json.JsonString;
import java.util.HashMap;
import java.util.Map;

public class Tag {
	
	public String id;
	public boolean isAuto;
	public Map<String, Localization> locales;
	
	public Tag(){
		this("", false, new HashMap<>());
	}
	
	public Tag(String id, boolean isAuto, Map<String, Localization> locales){
		this.id = id;
		this.isAuto = isAuto;
		this.locales = locales;
	}
	
	public Tag(JsonObject json){
		id = json.getString("tag_id", "");
		isAuto = json.getBoolean("is_auto", false);
		locales = new HashMap<>();
		
		if(json.containsKey("localization_descriptions") && json.containsKey("localization_names")){
			final JsonObject descipts = json.getJsonObject("localization_descriptions");
			final JsonObject names = json.getJsonObject("localization_names");
			
			descipts.forEach((key, value) -> {
				if(value instanceof JsonString){
					locales.put(key, new Localization(key, names.getString(key, ""), ((JsonString) value).getString()));
				}
			});
		}
	}
	
	
	public static final class Localization {
		
		public String language;
		public String name;
		public String description;
		
		public Localization(){
			this("", "", "");
		}
		
		public Localization(String language, String name, String description){
			this.language = language;
			this.name = name;
			this.description = description;
		}
	}
}
