package me.jovannmc.mentionchat.handlers;

import me.jovannmc.mentionchat.MentionChat;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.HashSet;

public class MentionTypeFormatHandler {

    public MentionTypeFormatHandler(AsyncPlayerChatEvent e, Player mentioner, HashSet<Player> mentioned, FileConfiguration config, MentionChat plugin) {
        // Remove all recipients to send custom messages to each player, but lets the message still be logged in the console
        e.getRecipients().removeAll(Bukkit.getOnlinePlayers());

        // We use a HashSet here to track which players have already been sent a message, to prevent duplicate messages
        HashSet<Player> sentMessages = new HashSet<>();

        for (Player mentionedPlayer : mentioned) {
            String mentionSymbol = config.getString("mentionSymbol");
            String mentionPattern = "(?i)" + mentionSymbol + mentionedPlayer.getName() + "\\b";
            String mentionMessage;

            if (plugin.getData().contains(mentionedPlayer.getUniqueId().toString() + ".format")) {
                mentionMessage = ChatColor.translateAlternateColorCodes('&', plugin.getData().getString(mentionedPlayer.getUniqueId().toString() + ".format").replace("%mention%", mentionSymbol + mentionedPlayer.getName()));
            } else {
                mentionMessage = ChatColor.translateAlternateColorCodes('&', config.getString("mentionFormat").replace("%mention%", mentionSymbol + mentionedPlayer.getName()));
            }

            // Like previously, we split the message into words and check if any of them are a player's name to prevent duplicates
            String[] words = e.getMessage().split("\\s+");
            StringBuilder newMessageBuilder = new StringBuilder();
            for (String word : words) {
                if (word.matches(mentionPattern)) {
                    newMessageBuilder.append(" ").append(mentionMessage);
                } else {
                    newMessageBuilder.append(" ").append(word);
                }
            }
            String newMessage = newMessageBuilder.toString().trim();

            for (Player player : Bukkit.getOnlinePlayers()) {
                if (!mentioned.contains(player) && !sentMessages.contains(player)) {
                    // Add the player to the HashSet, so they don't get sent the same message multiple times
                    player.sendMessage(e.getFormat().replace("%1$s", mentioner.getDisplayName()).replace("%2$s", e.getMessage()));
                    sentMessages.add(player);
                }
            }
            plugin.playMentionSound(mentionedPlayer);
            mentionedPlayer.sendMessage(e.getFormat().replace("%1$s", mentioner.getDisplayName()).replace("%2$s", newMessage));
        }
    }

    public MentionTypeFormatHandler(AsyncPlayerChatEvent e, Player mentioner, FileConfiguration config, MentionChat plugin) {
        // Remove all recipients to send custom messages to each player, but lets the message still be logged in the console
        e.getRecipients().removeAll(Bukkit.getOnlinePlayers());

        for (Player p : Bukkit.getOnlinePlayers()) {
            String mentionSymbol = config.getString("mentionSymbol");
            String mentionPattern = "(?i)" + mentionSymbol + "everyone\\b";
            String mentionMessage = ChatColor.translateAlternateColorCodes('&', config.getString("mentionFormat").replace("%mention%", mentionSymbol + "everyone"));

            // Like previously, we split the message into words so that it has to be @everyone, and also case-insensitive
            String[] words = e.getMessage().split("\\s+");
            StringBuilder newMessageBuilder = new StringBuilder();
            for (String word : words) {
                if (word.matches(mentionPattern)) {
                    newMessageBuilder.append(" ").append(mentionMessage);
                } else {
                    newMessageBuilder.append(" ").append(word);
                }
            }
            String newMessage = newMessageBuilder.toString().trim();
            p.sendMessage(e.getFormat().replace("%1$s", mentioner.getDisplayName()).replace("%2$s", newMessage));

            plugin.playMentionSound(p);
        }
    }


}
