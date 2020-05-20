package com.lukestadem.repulse.chat;

import org.java_websocket.handshake.ServerHandshake;

public abstract class IrcAdapter implements IrcListener {
	
	@Override
	public void onOpen(ServerHandshake handshakedata){
		
	}
	
	@Override
	public void onMessage(String message){
		
	}
	
	@Override
	public void onClose(int code, String reason, boolean remote){
		
	}
	
	@Override
	public void onError(Exception ex){
		
	}
}
