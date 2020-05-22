package com.lukestadem.repulse.events;

public class StreamOfflineEvent extends Event {
	
	public final String channelName;
	
	public StreamOfflineEvent(String channelName){
		this.channelName = channelName;
	}
}
