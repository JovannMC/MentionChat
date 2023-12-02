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
        FileConfiguration config = plugin.getConfig();
        FileConfiguration data = plugin.getData();

        // Remove all recipients to send custom messages to each player, but lets the message still be logged in the console
        e.getRecipients().removeAll(Bukkit.getOnlinePlayers());

        // We use a HashSet here to track which players have already been sent a message, to prevent duplicate messages
        HashSet<Player> sentMessages = new HashSet<>();

        // Send the message to each mentioned player
        for (Player mentionedPlayer : mentioned) {
            if (!sentMessages.contains(mentionedPlayer)) {
                // Add the player to the HashSet, so they don't get sent the same message multiple times
                plugin.playMentionSound(mentionedPlayer);
                sentMessages.add(mentionedPlayer);

                if (data.contains(mentionedPlayer.getUniqueId().toString() + ".message")) {
                    Utils.sendMessage(mentionedPlayer, data.getString(mentionedPlayer.getUniqueId().toString() + ".message").replace("%player%", mentioner.getName()));
                } else {
                    Utils.sendMessage(mentionedPlayer, config.getString("mentionedMessage").replace("%player%", mentioner.getName()));
                }

                if (plugin.getData().get(mentionedPlayer.getUniqueId().toString() + ".toggle.format") != null && !plugin.getData().getBoolean(mentionedPlayer.getUniqueId().toString() + ".toggle.format")) {
                    mentionedPlayer.sendMessage(e.getFormat().replace("%1$s", mentioner.getDisplayName()).replace("%2$s", e.getMessage()));
                }
            }
        }

        // Send normal message to everyone else
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (!mentioned.contains(player) && plugin.getData().get(player.getUniqueId().toString() + ".toggle.format") != null && !plugin.getData().getBoolean(player.getUniqueId().toString() + ".toggle.format")) {
                player.sendMessage(e.getFormat().replace("%1$s", mentioner.getDisplayName()).replace("%2$s", e.getMessage()));
            }
        }
    }

    // Mention everyone
    public MentionTypeMessageHandler(AsyncPlayerChatEvent e, Player mentioner, MentionChat plugin) {
        FileConfiguration config = plugin.getConfig();
        FileConfiguration data = plugin.getData();

        // Remove all recipients to send custom messages to each player, but lets the message still be logged in the console
        e.getRecipients().removeAll(Bukkit.getOnlinePlayers());

        // We use a HashSet here to track which players have already been sent a message, to prevent duplicate messages
        HashSet<Player> sentMessages = new HashSet<>();

        // Send the message to each player
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (!sentMessages.contains(player)) {
                // Add the player to the HashSet, so they don't get sent the same message multiple times
                sentMessages.add(player);
                plugin.playMentionSound(player);

                if (data.contains(player.getUniqueId().toString() + ".message")) {
                    Utils.sendMessage(player, data.getString(player.getUniqueId().toString() + ".message").replace("%player%", mentioner.getName()));
                } else {
                    Utils.sendMessage(player, config.getString("mentionedMessage").replace("%player%", mentioner.getName()));
                }

                if (plugin.getData().get(player.getUniqueId().toString() + ".toggle.format") != null && !plugin.getData().getBoolean(player.getUniqueId().toString() + ".toggle.format")) {
                    player.sendMessage(e.getFormat().replace("%1$s", mentioner.getDisplayName()).replace("%2$s", e.getMessage()));
                }
            }
        }
    }
}
