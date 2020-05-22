package com.lukestadem.repulse.events;

import com.lukestadem.repulse.entities.Stream;

public class StreamOnlineEvent extends Event {
	
	public final Stream stream;
	
	public StreamOnlineEvent(Stream stream){
		this.stream = stream;
	}
}
