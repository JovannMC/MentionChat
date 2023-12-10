package me.jovannmc.mentionchat.handlers;

import me.jovannmc.mentionchat.MentionChat;
import me.jovannmc.mentionchat.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.HashSet;
import java.util.Set;

public class MentionTypeMessageHandler {

    // Mention users/everyone
    public MentionTypeMessageHandler(AsyncPlayerChatEvent e, Player mentioner, HashSet<Player> mentioned, MentionChat plugin) {
        System.out.println("MentionTypeMessageHandler for users");
        FileConfiguration config = plugin.getConfig();
        FileConfiguration data = plugin.getData();

        // Create a new HashSet for recipients
        // interacting with event's recipients causes issues for some reason, so we create a new hashset with the recipients
        Set<Player> recipients = new HashSet<>(e.getRecipients());

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
                System.out.println("send message to " + mentionedPlayer.getName());

                // Remove mentioned players from the recipients set
                recipients.remove(mentionedPlayer);
            }
        }

        for (Player player : recipients) {
            if (!plugin.getConfig().getString("mentionType").toUpperCase().contains("FORMAT") | !plugin.getData().getBoolean(player.getUniqueId().toString() + ".toggle.format")) {
                player.sendMessage(e.getFormat().replace("%1$s", mentioner.getDisplayName()).replace("%2$s", e.getMessage()));
                System.out.println("send normal to " + player.getName());
            }
        }
    }
}
