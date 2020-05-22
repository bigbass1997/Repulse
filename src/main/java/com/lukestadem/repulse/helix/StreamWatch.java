package com.lukestadem.repulse.helix;

import com.lukestadem.repulse.Disposable;
import com.lukestadem.repulse.TwitchClient;
import com.lukestadem.repulse.entities.Stream;
import com.lukestadem.repulse.events.StreamOfflineEvent;
import com.lukestadem.repulse.events.StreamOnlineEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class StreamWatch extends Thread implements Disposable {
	
	private static final Logger log = LoggerFactory.getLogger(StreamWatch.class);
	
	private long offlineWait = 15000;
	private long onlineWait = 60000;
	private long runInterval = 5000;
	
	private final TwitchClient twitch;
	
	private boolean isRunning;
	
	private ConcurrentHashMap<String, ChannelWatch> watchedChannels;
	private List<String> toCheck;
	
	public StreamWatch(TwitchClient twitch){
		this.twitch = twitch;
		
		watchedChannels = new ConcurrentHashMap<>();
		toCheck = new ArrayList<>();
		
		isRunning = true;
		
		setName("StreamWatch-Thread");
		start();
	}
	
	/**
	 * <p>Register a twitch channel to watch. Watched channels will generate either a {@link StreamOnlineEvent} or
	 * {@link StreamOfflineEvent} when their state changes from online to offline or vis versa.</p>
	 * 
	 * <p>By default, a previously offline channel will be checked every 15 seconds, and a previously online
	 * channel every 60 seconds. The watch loop/thread runs every 5 seconds. All of these intervals can be changed
	 * via {@link #setOfflineWait(long, TimeUnit)}, {@link #setOnlineWait(long, TimeUnit)}, and
	 * {@link #setRunInterval(long, TimeUnit)}</p>
	 * 
	 * @param channel name of the channel to be watched
	 */
	public void registerChannel(String channel){
		if(!watchedChannels.containsKey(channel)){
			watchedChannels.put(channel, new ChannelWatch(channel));
			log.debug("Channel registered: " + channel);
		}
	}
	
	/**
	 * Unregister a channel.
	 * 
	 * @see #registerChannel(String) 
	 * @param channel name of the channel to stop watching
	 */
	public void unregisterChannel(String channel){
		watchedChannels.remove(channel);
		log.debug("Channel unregistered: " + channel);
	}
	
	/**
	 * The main loop for checking the stream status of {@link #watchedChannels}. Each loop runs, by default, every
	 * 5 seconds, however this can be changed using {@link #setRunInterval(long, TimeUnit)}. When the loop runs, first
	 * it checks how long it has been since the channel was last checked. If this time period is longer than
	 * {@link #offlineWait} or {@link #onlineWait} (depending on previous stream status), the channel will be checked
	 * against the Helix API to see if the state has changed.
	 */
	@Override
	public void run(){
		while(isRunning){
			try {
				sleep(runInterval);
			} catch (InterruptedException e) {
				log.error("", e);
			}
			
			toCheck.clear();
			watchedChannels.forEach((channel, watch) -> {
				final long time = System.currentTimeMillis();
				final long waitTime = watch.state ? onlineWait : offlineWait;
				
				if(time - watch.lastCheck > waitTime){
					toCheck.add(channel);
					watch.lastCheck = time;
				}
			});
			
			//TODO Make this compatible for situations where there are more than 100 registered channels
			
			final List<Stream> liveStreams = twitch.helix().getStreams(null, null, toCheck, null);
			if(liveStreams == null){
				continue;
			}
			
			//===== slower method =====\\
			/*toCheck.forEach(channel -> {
				Stream chanStream = null;
				boolean isLive = false;
				for(Stream stream : liveStreams){
					if(stream.isLive() && stream.username.equalsIgnoreCase(channel)){
						isLive = stream.isLive();
						chanStream = stream;
					}
				}
				
				final ChannelWatch watch = watchedChannels.get(channel);
				if(isLive && !watch.state){
					twitch.events().post(new StreamOnlineEvent(chanStream));
				} else if(!isLive && watch.state){
					twitch.events().post(new StreamOfflineEvent(channel));
				}
			});*/
			//=========================\\
			
			//===== faster method =====\\
			liveStreams.forEach(stream -> {
				final ChannelWatch watch = watchedChannels.get(stream.username.toLowerCase());
				if(stream.isLive()){
					if(!watch.state){
						twitch.events().post(new StreamOnlineEvent(stream));
					}
					toCheck.remove(stream.username.toLowerCase());
					watch.state = true;
				}
			});
			
			toCheck.forEach(offlineChannel -> {
				final ChannelWatch watch = watchedChannels.get(offlineChannel);
				if(watch.state){
					twitch.events().post(new StreamOfflineEvent(offlineChannel));
				}
				watch.state = false;
			});
			//=========================\\
		}
	}
	
	/**
	 * Set the wait period before checking channels that were previously offline. 15s is the default. <b>Durations less than 1000ms will be
	 * set to 1000ms.</b>
	 * 
	 * @param duration duration
	 * @param unit time unit
	 */
	public void setOfflineWait(long duration, TimeUnit unit){
		offlineWait = unit.toMillis(duration);
		
		if(offlineWait < 1000){
			offlineWait = 1000;
		}
	}
	
	/**
	 * Set the wait period before checking channels that were previously online. <b>Durations less than 1000ms will be
	 * set to 1000ms.</b>
	 * 
	 * @param duration duration
	 * @param unit time unit
	 */
	public void setOnlineWait(long duration, TimeUnit unit){
		onlineWait = unit.toMillis(duration);
		
		if(onlineWait < 1000){
			onlineWait = 1000;
		}
	}
	
	/**
	 * Set the rate at which the run loop is executed. 5000ms is the default. <b>Durations less than 10ms will be
	 * set to 10ms.</b>
	 * 
	 * @param duration duration
	 * @param unit time unit
	 */
	public void setRunInterval(long duration, TimeUnit unit){
		runInterval = unit.toMillis(duration);
		
		if(runInterval < 10){
			runInterval = 10;
		}
	}
	
	@Override
	public void dispose(){
		isRunning = false;
	}
	
	private static class ChannelWatch {
		
		public final String name;
		
		public long lastCheck;
		public boolean state;
		
		public ChannelWatch(String name){
			this.name = name;
			
			lastCheck = 0;
			state = false;
		}
	}
}
