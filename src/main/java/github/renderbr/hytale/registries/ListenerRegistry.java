package github.renderbr.hytale.registries;

import com.hypixel.hytale.event.EventRegistry;
import github.renderbr.hytale.listeners.ChatListener;
import github.renderbr.hytale.listeners.PlayerStateListener;
import github.renderbr.hytale.listeners.ServerStateListener;

public class ListenerRegistry {
    public static void registerListeners(EventRegistry eventRegistry){
        ServerStateListener.register(eventRegistry);
        ChatListener.registerChatListeners(eventRegistry);
        PlayerStateListener.register(eventRegistry);
    }
}
