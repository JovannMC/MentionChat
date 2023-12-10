package me.jovannmc.mentionchat.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import java.util.HashSet;

public class PlayerMentionEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();

    private boolean isCancelled;

    private Player mentioner;
    private HashSet<Player> mentionedPlayers;
    private String mentionType;

    public PlayerMentionEvent(Player mentioner, HashSet<Player> mentionedPlayers, String mentionType) {
        this.mentioner = mentioner;
        this.mentionedPlayers = mentionedPlayers;
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

    public HashSet<Player> getMentionedPlayers() {
        return this.mentionedPlayers;
    }

    public String getMentionType() {
        return this.mentionType;
    }
}