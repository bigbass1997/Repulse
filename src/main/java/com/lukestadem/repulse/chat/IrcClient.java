package com.lukestadem.repulse.chat;

import com.lukestadem.repulse.Constants;
import com.lukestadem.repulse.TwitchClient;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.enums.ReadyState;
import org.java_websocket.handshake.ServerHandshake;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class IrcClient extends WebSocketClient {
	
	private static final Logger log = LoggerFactory.getLogger(IrcClient.class);
	
	private final TwitchClient twitch;
	
	/** time in milliseconds, since last reconnect attempt */
	private long lastReconnect;
	
	/** how long to wait in milliseconds, until next reconnect attempt */
	private long reconnectWait;
	
	private List<IrcListener> listeners;
	
	private boolean isDisposed;
	
	public IrcClient(TwitchClient twitch){
		super(URI.create(Constants.IRC_SECURE_URL));
		
		this.twitch = twitch;
		
		lastReconnect = System.currentTimeMillis();
		reconnectWait = 1000;
		
		listeners = Collections.synchronizedList(new ArrayList<>());
		
		isDisposed = false;
		
		connect();
	}
	
	public synchronized void addListener(IrcListener listener){
		if(listener != null){
			listeners.add(listener);
		}
	}
	
	public synchronized void removeListener(IrcListener listener){
		if(listener != null){
			listeners.remove(listener);
		}
	}
	
	@Override
	public void onOpen(ServerHandshake handshakedata){
		log.debug(handshakedata.getHttpStatus() + " : " + handshakedata.getHttpStatusMessage());
		
		if(twitch.hasTokenExpired()){
			log.warn("Auth token is expired, IRC will fail to authenticate!");
		}
		
		send("CAP REQ :twitch.tv/tags twitch.tv/commands twitch.tv/membership");
		send("CAP END");
		
		send("PASS oauth:" + twitch.getOAuthToken().access);
		send("NICK " + twitch.getOAuthToken().username);
		
		listeners.forEach(listener -> {
			listener.onOpen(handshakedata);
		});
	}
	
	@Override
	public void onMessage(String message){
		final String trimmedMessage = message.trim();
		
		log.debug(trimmedMessage);
		
		if(trimmedMessage.equals("PING :tmi.twitch.tv")){
			send("PONG :tmi.twitch.tv");
		}
		
		listeners.forEach(listener -> {
			listener.onMessage(trimmedMessage);
		});
	}
	
	/**
	 * Called when websocket is closed. Refer to <a href="https://github.com/Luka967/websocket-close-codes#websocket-close-codes">https://github.com/Luka967/websocket-close-codes#websocket-close-codes</a>
	 * for what each code stands for.
	 * 
	 * @param code
	 * @param reason
	 * @param remote
	 */
	@Override
	public void onClose(int code, String reason, boolean remote){
		log.debug("Close: " + code + " " + reason);
		
		listeners.forEach(listener -> {
			listener.onClose(code, reason, remote);
		});
		
		if(!isDisposed){
			attemptReconnect();
		}
	}
	
	private void attemptReconnect() {
		if(System.currentTimeMillis() - lastReconnect > 1000 * 60 * 60 * 24){
			reconnectWait = 1000;
		}
		
		lastReconnect = System.currentTimeMillis();
		
		try {
			closeBlocking();
		} catch (InterruptedException e) {
			log.error("", e);
		}
		try {
			Thread.sleep(reconnectWait);
		} catch (InterruptedException e) {
			log.error("", e);
			
			if(getReadyState() != ReadyState.OPEN){
				attemptReconnect();
			}
		}
		
		try {
			if(!reconnectBlocking()){
				reconnectWait *= 2;
				
				attemptReconnect();
			}
		} catch (InterruptedException e) {
			log.error("", e);
			
			if(getReadyState() != ReadyState.OPEN){
				attemptReconnect();
			}
		}
	}
	
	@Override
	public void onError(Exception ex){
		log.error("Socket Error!", ex);
		
		listeners.forEach(listener -> {
			listener.onError(ex);
		});
	}
	
	public void dispose(){
		isDisposed = true;
		
		try {
			closeBlocking();
		} catch (InterruptedException e) {
			log.error("", e);
		}
	}
}
