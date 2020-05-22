package com.lukestadem.repulse.events;

public class IrcLeaveChannelEvent extends Event {
	
	/** raw irc data message */
	public final String raw;
	/** username of account that left the channel */
	public final String username;
	/** name of the channel that the user left */
	public final String channel;
	
	public IrcLeaveChannelEvent(String raw, String username, String channel){
		this.raw = raw;
		this.username = username;
		this.channel = channel;
	}
}
