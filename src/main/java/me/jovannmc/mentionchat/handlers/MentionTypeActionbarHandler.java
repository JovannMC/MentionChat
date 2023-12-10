package me.jovannmc.mentionchat.handlers;

import me.jovannmc.mentionchat.MentionChat;
import me.jovannmc.mentionchat.utils.Utils;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.HashSet;

public class MentionTypeActionbarHandler {

    // Mention users/everyone
    public MentionTypeActionbarHandler(Player mentioner, HashSet<Player> mentioned, MentionChat plugin) {
        System.out.println("MentionTypeActionbarHandler");
        FileConfiguration config = plugin.getConfig();
        FileConfiguration data = plugin.getData();

        String message;

        if (data.contains(mentioner.getUniqueId().toString() + ".actionbar")) {
            message = ChatColor.translateAlternateColorCodes('&', data.getString(mentioner.getUniqueId().toString() + ".actionbar").replace("%player%", mentioner.getName()));
        } else {
            message = ChatColor.translateAlternateColorCodes('&', config.getString(".mentionedActionbar"));
        }

        for (Player mentionedPlayer : mentioned) {
            plugin.playMentionSound(mentionedPlayer);
            sendActionbar(mentionedPlayer, message);
            System.out.println("send actionbar to " + mentionedPlayer.getName());
        }
    }

    // doesn't support duration, at least without NMS
    private void sendActionbar(Player player, String message) {
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(Utils.color(message.replace("%player%", player.getName()))));
    }
}
