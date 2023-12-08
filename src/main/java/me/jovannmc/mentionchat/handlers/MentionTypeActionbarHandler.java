package me.jovannmc.mentionchat.handlers;

import me.jovannmc.mentionchat.MentionChat;
import me.jovannmc.mentionchat.utils.Utils;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.HashSet;

public class MentionTypeActionbarHandler {

    // Mention single user
    public MentionTypeActionbarHandler(AsyncPlayerChatEvent e, Player mentioned, MentionChat plugin) {
        FileConfiguration config = plugin.getConfig();
        FileConfiguration data = plugin.getData();
        Player mentioner = e.getPlayer();

        String message;

        if (data.contains(mentioner.getUniqueId().toString() + ".actionbar")) {
            message = ChatColor.translateAlternateColorCodes('&', data.getString(mentioner.getUniqueId().toString() + ".actionbar").replace("%player%", mentioner.getName()));
        } else {
            message = ChatColor.translateAlternateColorCodes('&', config.getString(".mentionedActionbar"));
        }

        plugin.playMentionSound(mentioned);
        sendActionbar(mentioned, message);
    }

    // Mention multiple users
    public MentionTypeActionbarHandler(AsyncPlayerChatEvent e, HashSet<Player> mentioned, MentionChat plugin) {
        FileConfiguration config = plugin.getConfig();
        FileConfiguration data = plugin.getData();
        Player mentioner = e.getPlayer();

        String message;

        if (data.contains(mentioner.getUniqueId().toString() + ".actionbar")) {
            message = ChatColor.translateAlternateColorCodes('&', data.getString(mentioner.getUniqueId().toString() + ".actionbar").replace("%player%", mentioner.getName()));
        } else {
            message = ChatColor.translateAlternateColorCodes('&', config.getString(".mentionedActionbar"));
        }

        for (Player mentionedPlayer : mentioned) {
            if (data.getBoolean(mentionedPlayer.getUniqueId().toString() + ".toggle.actionbar") || (data.get(mentionedPlayer.getUniqueId().toString() + ".toggle.actionbar") == null && config.getString("mentionType").contains("ACTIONBAR"))) {
                plugin.playMentionSound(mentionedPlayer);
                sendActionbar(mentionedPlayer, message);
            }
        }
    }

    // Mention everyone
    public MentionTypeActionbarHandler(AsyncPlayerChatEvent e, MentionChat plugin) {
        FileConfiguration config = plugin.getConfig();
        FileConfiguration data = plugin.getData();
        Player mentioner = e.getPlayer();

        String message;

        if (data.contains(mentioner.getUniqueId().toString() + ".actionbar")) {
            message = ChatColor.translateAlternateColorCodes('&', data.getString(mentioner.getUniqueId().toString() + ".actionbar.text").replace("%player%", mentioner.getName()));
        } else {
            message = ChatColor.translateAlternateColorCodes('&', config.getString(".mentionedActionbar"));
        }

        for (Player player : Bukkit.getOnlinePlayers()) {
            plugin.playMentionSound(player);
            sendActionbar(player, message);
        }
    }

    // doesn't support duration, at least without NMS
    private void sendActionbar(Player player, String message) {
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(Utils.color(message.replace("%player%", player.getName()))));
    }
}
