package me.jovannmc.mentionchat.handlers;

import me.jovannmc.mentionchat.MentionChat;
import me.jovannmc.mentionchat.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.HashSet;

public class MentionTypeMessageHandler {

    // Mention Users
    public MentionTypeMessageHandler(AsyncPlayerChatEvent e, Player mentioner, HashSet<Player> mentioned, MentionChat plugin) {
        // TODO: make sure it respects player's format
        FileConfiguration config = plugin.getConfig();
        FileConfiguration data = plugin.getData();

        // Remove all recipients to send custom messages to each player, but lets the message still be logged in the console
        e.getRecipients().removeAll(Bukkit.getOnlinePlayers());

        // We use a HashSet here to track which players have already been sent a message, to prevent duplicate messages
        HashSet<Player> sentMessages = new HashSet<>();

        // Send the message to each mentioned player
        for (Player mentionedPlayer : mentioned) {
            // Small bug here where with multiple mentions, the "mentionedMessage" may appear after the message instead of before.
            // It's inconsistency caused by the loop but doesn't really need to be fixed.
            if (!sentMessages.contains(mentionedPlayer)) {
                // Add the player to the HashSet, so they don't get sent the same message multiple times
                plugin.playMentionSound(mentionedPlayer);
                sentMessages.add(mentionedPlayer);
                Utils.sendMessage(mentionedPlayer, config.getString("mentionedMessage").replace("%player%", mentioner.getName()));
                mentionedPlayer.sendMessage(e.getFormat().replace("%1$s", mentioner.getDisplayName()).replace("%2$s", e.getMessage()));
            }
        }

        // Send normal message to everyone else
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (!mentioned.contains(player)) {
                player.sendMessage(e.getFormat().replace("%1$s", mentioner.getDisplayName()).replace("%2$s", e.getMessage()));
            }
        }
    }

    // Mention everyone
    public MentionTypeMessageHandler(AsyncPlayerChatEvent e, Player mentioner, MentionChat plugin) {
        // TODO: make sure it respects player's format
        FileConfiguration config = plugin.getConfig();
        FileConfiguration data = plugin.getData();

        // Remove all recipients to send custom messages to each player, but lets the message still be logged in the console
        e.getRecipients().removeAll(Bukkit.getOnlinePlayers());

        // We use a HashSet here to track which players have already been sent a message, to prevent duplicate messages
        HashSet<Player> sentMessages = new HashSet<>();

        // Small bug here where with multiple mentions, the "mentionedMessage" may appear after the message instead of before.
        // It's inconsistency caused by the loop but doesn't really need to be fixed.
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (!sentMessages.contains(player)) {
                // Add the player to the HashSet, so they don't get sent the same message multiple times
                sentMessages.add(player);
                Utils.sendMessage(player, config.getString("mentionedMessage").replace("%player%", mentioner.getName()));
                plugin.playMentionSound(player);
                // Not a mentioned player, so send the normal message
                player.sendMessage(e.getFormat().replace("%1$s", mentioner.getDisplayName()).replace("%2$s", e.getMessage()));
            }
        }
    }
}
