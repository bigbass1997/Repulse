package com.lukestadem.repulse.events;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class EventManager {
	
	private static final Logger log = LoggerFactory.getLogger(EventManager.class);
	
	/** <Event.class, < */
	private final ConcurrentHashMap<Class<?>, ConcurrentHashMap<Method, CopyOnWriteArrayList<Object>>> listeners;
	
	public EventManager(){
		listeners = new ConcurrentHashMap<>();
	}
	
	public void addListener(Object listener){
		Arrays.stream(listener.getClass().getMethods())
				.filter(method -> method.getParameterCount() == 1)
				.filter(method -> Event.class.isAssignableFrom(method.getParameterTypes()[0]))
				.filter(method -> method.isAnnotationPresent(SubscribeEvent.class))
				.forEach(method -> {
					method.setAccessible(true);
					
					final Class<?> eventClass = method.getParameterTypes()[0];
					
					if(!listeners.containsKey(eventClass)){
						listeners.put(eventClass, new ConcurrentHashMap<>());
					}
					
					if(!listeners.get(eventClass).containsKey(method)){
						listeners.get(eventClass).put(method, new CopyOnWriteArrayList<>());
					}
					
					if(listeners.get(eventClass).get(method).add(listener)){
						log.debug("Registered method listener " + listener.getClass().getSimpleName() + "#" + method.getName());
					}
				});
	}
	
	public void removeListener(Object listener){
		Arrays.stream(listener.getClass().getMethods())
				.filter(method -> method.getParameterCount() == 1)
				.filter(method -> Event.class.isAssignableFrom(method.getParameterTypes()[0]))
				.filter(method -> method.isAnnotationPresent(SubscribeEvent.class))
				.forEach(method -> {
					method.setAccessible(true);
					
					final Class<?> eventClass = method.getParameterTypes()[0];
					
					if(listeners.containsKey(eventClass) && listeners.get(eventClass).containsKey(method)){
						final CopyOnWriteArrayList<Object> list = listeners.get(eventClass).get(method);
						if(list.contains(listener)){
							if(list.remove(listener)){
								log.debug("Unregistered method listener " + listener.getClass().getSimpleName() + "#" + method.getName());
							}
						}
					}
				});
	}
	
	public void post(Event event){
		if(listeners.size() > 0){
			listeners.entrySet().stream()
					.filter(mapEntry -> mapEntry.getKey().isAssignableFrom(event.getClass()))
					.map(Map.Entry::getValue)
					.forEach(eventClass -> {
						eventClass.forEach((method, listenerList) -> {
							listenerList.forEach(listener -> {
								try {
									method.invoke(listener, event);
								} catch (IllegalAccessException | InvocationTargetException e) {
									log.error("Error dispatching event: " + event.getClass().getSimpleName(), e);
								} catch (Exception e) {
									log.error("Unhandled exception while invoking event listener method!", e);
								}
							});
						});
					});
		}
	}
}
