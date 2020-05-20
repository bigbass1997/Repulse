package com.lukestadem.repulse.events;

public class IrcMessageEvent extends Event {
	
	public final String message;
	
	public IrcMessageEvent(String message){
		this.message = message;
	}
}
