package me.jovannmc.mentionchat.handlers;

import me.jovannmc.mentionchat.MentionChat;
import me.jovannmc.mentionchat.utils.Utils;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashSet;

public class MentionTypeActionbarHandler {

    // Mention Users
    public MentionTypeActionbarHandler(AsyncPlayerChatEvent e, Player mentioner, HashSet<Player> mentioned, MentionChat plugin) {
        FileConfiguration config = plugin.getConfig();
        FileConfiguration data = plugin.getData();

        String message;
        int duration;

        if (data.contains(mentioner.getUniqueId().toString() + ".actionbar")) {
            message = ChatColor.translateAlternateColorCodes('&', data.getString(mentioner.getUniqueId().toString() + ".actionbar").replace("%player%", mentioner.getName()));
        } else {
            message = ChatColor.translateAlternateColorCodes('&', config.getString(".mentionedActionbar"));
        }

        if (data.contains(mentioner.getUniqueId().toString() + ".duration")) {
            duration = data.getInt(mentioner.getUniqueId().toString() + ".duration");
        } else {
            duration = config.getInt("mentionedDuration");
        }

        for (Player mentionedPlayer : mentioned) {
            plugin.playMentionSound(mentionedPlayer);
            sendActionbar(plugin, mentionedPlayer, message, duration);
        }
    }

    // Mention everyone
    public MentionTypeActionbarHandler(AsyncPlayerChatEvent e, Player mentioner, MentionChat plugin) {
        FileConfiguration config = plugin.getConfig();
        FileConfiguration data = plugin.getData();

        String message;
        int duration;

        if (data.contains(mentioner.getUniqueId().toString() + ".actionbar")) {
            message = ChatColor.translateAlternateColorCodes('&', data.getString(mentioner.getUniqueId().toString() + ".actionbar.text").replace("%player%", mentioner.getName()));
        } else {
            message = ChatColor.translateAlternateColorCodes('&', config.getString(".mentionedActionbar"));
        }

        if (data.contains(mentioner.getUniqueId().toString() + ".duration")) {
            duration = data.getInt(mentioner.getUniqueId().toString() + ".duration");
        } else {
            duration = config.getInt("mentionedDuration");
        }

        for (Player player : Bukkit.getOnlinePlayers()) {
            plugin.playMentionSound(player);
            sendActionbar(plugin, player, message, duration);
        }
    }

    private void sendActionbar(MentionChat plugin, Player player, String message, int duration) {
        if (Utils.isLegacyVersion()) {
            // TODO: get actionbars to work on 1.8 with reflection help im actually dying from this bro
            Utils.sendMessage(player, "&Actionbars are not supported on this version.");
        } else {
            // doesn't support duration
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(Utils.color(message.replace("%player%", player.getName()))));
        }
    }
}
