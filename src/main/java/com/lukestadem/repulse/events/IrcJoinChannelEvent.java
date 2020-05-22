package com.lukestadem.repulse.events;

public class IrcJoinChannelEvent extends Event {
	
	/** raw irc data message */
	public final String raw;
	/** username of account that joined the channel */
	public final String username;
	/** name of the channel that the user joined */
	public final String channel;
	
	public IrcJoinChannelEvent(String raw, String username, String channel){
		this.raw = raw;
		this.username = username;
		this.channel = channel;
	}
}
