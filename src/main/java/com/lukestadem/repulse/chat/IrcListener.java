package com.lukestadem.repulse.chat;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.framing.CloseFrame;
import org.java_websocket.handshake.ServerHandshake;

public interface IrcListener {
	
	/**
	 * Called after an opening handshake has been performed and the given websocket is ready to be written on.
	 * 
	 * @see WebSocketClient#onOpen(ServerHandshake) 
	 * @param handshakedata The handshake of the websocket instance
	 */
	void onOpen(ServerHandshake handshakedata);
	
	/**
	 * Callback for string messages received from the remote host
	 * 
	 * @see WebSocketClient#onMessage(String)
	 * @param message The UTF-8 decoded message that was received.
	 **/
	void onMessage(String message);
	
	/**
	 * Called after the websocket connection has been closed.
	 * 
	 * @see WebSocketClient#onClose(int, String, boolean) 
	 * @param code The codes can be looked up here: {@link CloseFrame}
	 * @param reason Additional information string
	 * @param remote Returns whether or not the closing of the connection was initiated by the remote host.
	 **/
	void onClose(int code, String reason, boolean remote);
	
	/**
	 * Called when errors occurs. If an error causes the websocket connection to fail {@link #onClose(int, String, boolean)} will be called additionally.<br>
	 * This method will be called primarily because of IO or protocol errors.<br>
	 * If the given exception is an RuntimeException that probably means that you encountered a bug.<br>
	 * 
	 * @see WebSocketClient#onError(Exception) 
	 * @param ex The exception causing this error
	 **/
	void onError(Exception ex);
}
