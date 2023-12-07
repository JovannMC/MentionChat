package me.jovannmc.mentionchat.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.HashSet;

public class PlayerMentionEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();

    private boolean isCancelled;

    private Player mentioner;
    private Player mentionedPlayer;
    private String mentionType;

    public PlayerMentionEvent(AsyncPlayerChatEvent e, Player mentionedPlayer, String mentionType) {
        this.mentioner = e.getPlayer();
        this.mentionedPlayer = mentionedPlayer;
        this.mentionType = mentionType;
    }

    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    /*
    public boolean isCancelled() {

        return this.isCancelled;
    }


    public void setCancelled(boolean isCancelled) {
        this.isCancelled = isCancelled;
    }*/

    public Player getMentioner() {
        return this.mentioner;
    }

    public Player getMentionedPlayer() {
        return this.mentionedPlayer;
    }

    public String getMentionType() {
        return this.mentionType;
    }
}
