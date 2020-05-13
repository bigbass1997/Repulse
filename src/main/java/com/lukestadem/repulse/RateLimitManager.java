package com.lukestadem.repulse;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Bucket4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;

public class RateLimitManager {
	
	private static final Logger log = LoggerFactory.getLogger(RateLimitManager.class);
	
	public enum BucketName {
		ALL("all");
		
		public final String id;
		
		BucketName(String id){
			this.id = id;
		}
	}
	
	private ConcurrentHashMap<String, Bucket> remainingMap;
	
	public RateLimitManager(){
		remainingMap = new ConcurrentHashMap<>();
	}
	
	public void registerBucket(BucketName bucketName, int capacity, Duration duration){
		registerBucket(bucketName.id, capacity, duration);
	}
	
	public void registerBucket(String bucketName, int capacity, Duration duration){
		if(bucketName == null || bucketName.isEmpty()){
			log.error("Attempted to register a new bucket with a null or empty name!");
			return;
		}
		
		if(remainingMap.containsKey(bucketName)){
			log.info("Attempted to register a new bucket when one already exists with the given name: " + bucketName);
			return;
		}
		
		remainingMap.put(bucketName, Bucket4j.builder().addLimit( Bandwidth.simple(capacity, duration) ).build());
	}
	
	public boolean tryConsume(BucketName bucketName, int num){
		return tryConsume(bucketName.id, num);
	}
	
	public boolean tryConsume(String bucketName, int num){
		if(!remainingMap.containsKey(bucketName)){
			log.error("No rate limit bucket was found for the key: " + bucketName);
			return false;
		}
		
		if(!remainingMap.get(bucketName).tryConsume(num)){
			log.warn("Rate Limit has been reached for the bucket: " + bucketName);
			return false;
		} else {
			return true;
		}
	}
}
