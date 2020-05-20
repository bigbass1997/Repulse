package com.lukestadem.repulse.chat;

import com.lukestadem.repulse.TwitchClient;
import com.lukestadem.repulse.events.IrcMessageEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class ChatClient {
	
	private static final Logger log = LoggerFactory.getLogger(ChatClient.class);
	
	private static final Pattern MSG_PARSE_PATTERN = Pattern.compile("^.*:.*!.*@.*\\.tmi\\.twitch\\.tv PRIVMSG #[a-zA-Z_1-9]+ :");
	private static final Pattern JOIN_PARSE_PATTERN = Pattern.compile("^:.*!.*@.*tv JOIN #");
	private static final Pattern PART_PARSE_PATTERN = Pattern.compile("^:.*!.*@.*tv PART #");
	
	private final TwitchClient twitch;
	private final IrcClient irc;
	
	private List<String> joinedChannels;
	
	public ChatClient(final TwitchClient twitch){
		this.twitch = twitch;
		
		joinedChannels = new ArrayList<>();
		
		irc = new IrcClient(this.twitch);
		irc.addListener(new IrcAdapter(){
			@Override
			public void onMessage(String message){
				if(message.contains(twitch.retrieveUsername() + ".tmi.twitch.tv JOIN #")){
					final String channel = JOIN_PARSE_PATTERN.matcher(message).replaceFirst("");
					joinedChannels.add(channel);
					log.debug("Joined channel #" + channel);
				} else if(message.contains(twitch.retrieveUsername() + ".tmi.twitch.tv PART #")){
					final String channel = PART_PARSE_PATTERN.matcher(message).replaceFirst("");
					joinedChannels.remove(channel);
					log.debug("Left channel #" + channel);
				}
				
				if(message.matches(".*tmi\\.twitch\\.tv PRIVMSG #.*")){
					String s = MSG_PARSE_PATTERN.matcher(message).replaceFirst("");
					twitch.events().post(new IrcMessageEvent(s));
				}
			}
			
			@Override
			public void onClose(int code, String reason, boolean remote){
				//joinedChannels.clear();
			}
		});
	}
	
	public void joinChannel(String channel){
		if(isNotEmpty(channel)){
			irc.send("JOIN #" + channel.toLowerCase());
		}
	}
	
	public void leaveChannel(String channel){
		if(isNotEmpty(channel)){
			irc.send("PART #" + channel.toLowerCase());
		}
	}
	
	public void sendMessage(String message, String channel){
		if(isNotEmpty(message) && isNotEmpty(channel)){
			irc.send("PRIVMSG #" + channel + " :" + message);
		}
	}
	
	public String[] getJoinedChannels(){
		return joinedChannels.toArray(new String[0]);
	}
	
	public void dispose(){
		irc.dispose();
	}
	
	private boolean isNotEmpty(String str){
		return (str != null && !str.isEmpty());
	}
}
