package com.thizthizzydizzy.treefeller.core.event;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
public class TreeFellerEvents{
    private static final HashMap<Class<? extends TreeFellerEvent>, List<EventListener>> eventListeners = new HashMap<>();
    public static <T extends TreeFellerEvent> void on(Class<T> eventClass, EventListener<T> eventHandler){
        eventListeners.computeIfAbsent(eventClass, k -> new ArrayList<>()).add(eventHandler);
    }
    public static void fireEvent(TreeFellerEvent event){
        List<EventListener> listeners = eventListeners.get(event.getClass());
        if(listeners==null)return; // nobody was listening, sorry :(
        for(EventListener listener : listeners){
            listener.onEvent(event);
        }
    }
}
