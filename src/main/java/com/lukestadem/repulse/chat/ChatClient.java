package com.lukestadem.repulse.chat;

import com.lukestadem.repulse.TwitchClient;
import com.lukestadem.repulse.events.IrcMessageEvent;
import org.java_websocket.handshake.ServerHandshake;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CopyOnWriteArrayList;
import java.util.regex.Pattern;

public class ChatClient {
	
	private static final Logger log = LoggerFactory.getLogger(ChatClient.class);
	
	private static final Pattern MSG_PARSE_PATTERN = Pattern.compile("^.*:.*!.*@.*\\.tmi\\.twitch\\.tv PRIVMSG #[a-zA-Z_1-9]+ :");
	private static final Pattern CHANNEL_PARSE_PATTERN = Pattern.compile("^.*:.*!.*@.*\\.tmi\\.twitch\\.tv PRIVMSG #");
	private static final Pattern USERNAME_SUFFIX_PARSE_PATTERN = Pattern.compile("!.*@.*\\.tmi\\.twitch\\.tv PRIVMSG #.*$");
	private static final Pattern USERNAME_PREFIX_PARSE_PATTERN = Pattern.compile("^.* :");
	
	private static final Pattern JOIN_PARSE_PATTERN = Pattern.compile("^:.*!.*@.*tv JOIN #");
	private static final Pattern PART_PARSE_PATTERN = Pattern.compile("^:.*!.*@.*tv PART #");
	
	private final TwitchClient twitch;
	private final IrcWebsocket irc;
	
	private final CopyOnWriteArrayList<String> joinedChannels;
	
	public ChatClient(final TwitchClient twitch){
		this.twitch = twitch;
		
		joinedChannels = new CopyOnWriteArrayList<>();
		
		irc = new IrcWebsocket(this.twitch);
		irc.addListener(new IrcAdapter(){
			@Override
			public void onOpen(ServerHandshake handshakedata){
				// rejoin channels after reconnect
				joinedChannels.forEach(channel -> {
					joinChannel(channel);
				});
			}
			
			@Override
			public void onMessage(String message){
				if(message.contains(twitch.retrieveUsername() + ".tmi.twitch.tv JOIN #")){
					final String channel = JOIN_PARSE_PATTERN.matcher(message).replaceFirst("");
					if(!joinedChannels.contains(channel)){ // avoid duplicates
						joinedChannels.add(channel);
					}
					log.debug("Bot joined channel #" + channel);
				} else if(message.contains(twitch.retrieveUsername() + ".tmi.twitch.tv PART #")){
					final String channel = PART_PARSE_PATTERN.matcher(message).replaceFirst("");
					joinedChannels.remove(channel);
					log.debug("Bot left channel #" + channel);
				} else if(message.matches(".*tmi\\.twitch\\.tv PRIVMSG #.*")){
					final String parsedMessage = MSG_PARSE_PATTERN.matcher(message).replaceFirst("");
					String parsedChannel = CHANNEL_PARSE_PATTERN.matcher(message).replaceFirst("");
					parsedChannel = parsedChannel.substring(0, parsedChannel.indexOf(" :"));
					String parsedUsername = USERNAME_SUFFIX_PARSE_PATTERN.matcher(message).replaceAll("");
					parsedUsername = USERNAME_PREFIX_PARSE_PATTERN.matcher(parsedUsername).replaceAll("");
					
					twitch.events().post(new IrcMessageEvent(message, parsedMessage, parsedUsername, parsedChannel));
				}
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
