package com.lukestadem.repulse.events;

public class IrcMessageEvent extends Event {
	
	/** raw irc data message */
	public final String raw;
	/** message posted by a user, parsed from the raw data, and does not include any extra irc data */
	public final String message;
	/** name of the channel that the message was posted in */
	public final String channel;
	
	public IrcMessageEvent(String raw, String message, String channel){
		this.raw = raw;
		this.message = message;
		this.channel = channel;
	}
}
