package me.jovannmc.mentionchat.handlers;

import me.jovannmc.mentionchat.MentionChat;
import me.jovannmc.mentionchat.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.HashSet;

public class MentionTypeTitleHandler {

    // Mention Users
    public MentionTypeTitleHandler(AsyncPlayerChatEvent e, Player mentioner, HashSet<Player> mentioned, MentionChat plugin) {
        FileConfiguration config = plugin.getConfig();
        FileConfiguration data = plugin.getData();

        // Get the title and subtitle from user data, if not found, use the default title and subtitle from config and then send the title/subtitle to player
        String title;
        String subtitle;
        int duration;
        if (data.contains(mentioner.getUniqueId().toString() + ".title")) {
            title = ChatColor.translateAlternateColorCodes('&', data.getString(mentioner.getUniqueId().toString() + ".title").replace("%player%", mentioner.getName()));
        } else {
            title = ChatColor.translateAlternateColorCodes('&', config.getString(".mentionedTitle").replace("%player%", mentioner.getName()));
        }

        if (data.contains(mentioner.getUniqueId().toString() + ".subtitle")) {
            subtitle = ChatColor.translateAlternateColorCodes('&', data.getString(mentioner.getUniqueId().toString() + ".subtitle").replace("%player%", mentioner.getName()));
        } else {
            subtitle = ChatColor.translateAlternateColorCodes('&', config.getString(".mentionedSubtitle").replace("%player%", mentioner.getName()));
        }

        if (data.contains(mentioner.getUniqueId().toString() + ".duration")) {
            duration = data.getInt(mentioner.getUniqueId().toString() + ".duration");
        } else {
            duration = config.getInt("mentionedDuration");
        }

        // send title
        for (Player mentionedPlayer : mentioned) {
            plugin.playMentionSound(mentionedPlayer);
            Utils.sendTitle(mentionedPlayer, title, subtitle, duration);
        }
    }

    // Mention everyone
    public MentionTypeTitleHandler(AsyncPlayerChatEvent e, Player mentioner, MentionChat plugin) {
        FileConfiguration config = plugin.getConfig();
        FileConfiguration data = plugin.getData();

        // Get the title and subtitle from user data, if not found, use the default title and subtitle from config and then send the title/subtitle to player
        String title;
        String subtitle;
        int duration;
        if (data.contains(mentioner.getUniqueId().toString() + ".title")) {
            title = ChatColor.translateAlternateColorCodes('&', data.getString(mentioner.getUniqueId().toString() + ".title").replace("%player%", mentioner.getName()));
        } else {
            title = ChatColor.translateAlternateColorCodes('&', config.getString(".mentionedTitle").replace("%player%", mentioner.getName()));
        }

        if (data.contains(mentioner.getUniqueId().toString() + ".subtitle")) {
            subtitle = ChatColor.translateAlternateColorCodes('&', data.getString(mentioner.getUniqueId().toString() + ".subtitle").replace("%player%", mentioner.getName()));
        } else {
            subtitle = ChatColor.translateAlternateColorCodes('&', config.getString(".mentionedSubtitle").replace("%player%", mentioner.getName()));
        }

        if (data.contains(mentioner.getUniqueId().toString() + ".duration")) {
            duration = data.getInt(mentioner.getUniqueId().toString() + ".duration");
        } else {
            duration = config.getInt("mentionedDuration");
        }

        // send title
        for (Player player : Bukkit.getOnlinePlayers()) {
            plugin.playMentionSound(player);
            Utils.sendTitle(player, title, subtitle, duration);
        }
    }
}
