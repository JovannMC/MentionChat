package me.jovannmc.mentionchat.handlers;

import me.jovannmc.mentionchat.MentionChat;
import me.jovannmc.mentionchat.utils.Utils;
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

public class MentionTypeBossbarHandler {

    // Mention Users
    public MentionTypeBossbarHandler(AsyncPlayerChatEvent e, Player mentioner, HashSet<Player> mentioned, MentionChat plugin) {
        FileConfiguration config = plugin.getConfig();
        FileConfiguration data = plugin.getData();

        String message;
        String color;
        int duration;

        if (data.contains(mentioner.getUniqueId().toString() + ".bossbar.text")) {
            message = ChatColor.translateAlternateColorCodes('&', data.getString(mentioner.getUniqueId().toString() + ".bossbar.text").replace("%player%", mentioner.getName()));
        } else {
            message = ChatColor.translateAlternateColorCodes('&', config.getString(".mentionedBossbar"));
        }

        if (data.contains(mentioner.getUniqueId().toString() + ".bossbar.color")) {
            color = ChatColor.translateAlternateColorCodes('&', data.getString(mentioner.getUniqueId().toString() + ".bossbar.color"));
        } else {
            color = ChatColor.translateAlternateColorCodes('&', config.getString(".mentionedBossbarColor"));
        }

        if (data.contains(mentioner.getUniqueId().toString() + ".duration")) {
            duration = data.getInt(mentioner.getUniqueId().toString() + ".duration");
        } else {
            duration = config.getInt("mentionedDuration");
        }

        for (Player mentionedPlayer : mentioned) {
            plugin.playMentionSound(mentionedPlayer);
            sendBossbar(plugin, mentionedPlayer, message, color, duration);
        }
    }

    // Mention everyone
    public MentionTypeBossbarHandler(AsyncPlayerChatEvent e, Player mentioner, MentionChat plugin) {
        FileConfiguration config = plugin.getConfig();
        FileConfiguration data = plugin.getData();

        String message;
        String color;
        int duration;

        if (data.contains(mentioner.getUniqueId().toString() + ".bossbar.text")) {
            message = ChatColor.translateAlternateColorCodes('&', data.getString(mentioner.getUniqueId().toString() + ".bossbar.text").replace("%player%", mentioner.getName()));
        } else {
            message = ChatColor.translateAlternateColorCodes('&', config.getString(".mentionedBossbar"));
        }

        if (data.contains(mentioner.getUniqueId().toString() + ".bossbar.color")) {
            color = ChatColor.translateAlternateColorCodes('&', data.getString(mentioner.getUniqueId().toString() + ".bossbar.color"));
        } else {
            color = ChatColor.translateAlternateColorCodes('&', config.getString(".mentionedBossbarColor"));
        }

        if (data.contains(mentioner.getUniqueId().toString() + ".duration")) {
            duration = data.getInt(mentioner.getUniqueId().toString() + ".duration");
        } else {
            duration = config.getInt("mentionedDuration");
        }

        for (Player player : Bukkit.getOnlinePlayers()) {
            plugin.playMentionSound(player);
            sendBossbar(plugin, player, message, color, duration);
        }
    }

    private void sendBossbar(MentionChat plugin, Player player, String message, String color, int duration) {
        if (Utils.isLegacyVersion()) {
            // TODO: get bossbar to work on 1.8 with reflection help im actually dying from this bro
            Utils.sendMessage(player, "&cBossbars are not supported on this version.");
        } else {
            BossBar bossBar = Bukkit.createBossBar(message, BarColor.valueOf(color), BarStyle.SOLID);
            bossBar.addPlayer(player);

            new BukkitRunnable() {
                int remainingTime = duration;

                @Override
                public void run() {
                    double progress = (double) remainingTime / duration;

                    bossBar.setProgress(progress);

                    remainingTime--;

                    if (progress <= 0) {
                        bossBar.removeAll();
                        cancel();
                    }
                }
            }.runTaskTimer(plugin, 0,  20); // Convert interval to ticks
        }
    }
}
