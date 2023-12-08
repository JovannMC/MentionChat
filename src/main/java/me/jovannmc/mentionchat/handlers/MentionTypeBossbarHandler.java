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

    // Mention single user
    public MentionTypeBossbarHandler(AsyncPlayerChatEvent e, Player mentioned, MentionChat plugin) {
        FileConfiguration config = plugin.getConfig();
        FileConfiguration data = plugin.getData();
        Player mentioner = e.getPlayer();

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

        plugin.playMentionSound(mentioned);
        sendBossbar(plugin, mentioned, message, color, duration);
    }

    // Mention multiple users
    public MentionTypeBossbarHandler(AsyncPlayerChatEvent e, HashSet<Player> mentioned, MentionChat plugin) {
        FileConfiguration config = plugin.getConfig();
        FileConfiguration data = plugin.getData();
        Player mentioner = e.getPlayer();

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
            if (data.getBoolean(mentionedPlayer.getUniqueId().toString() + ".toggle.bossbar") || (data.get(mentionedPlayer.getUniqueId().toString() + ".toggle.bossbar") == null && config.getString("mentionType").contains("BOSSBAR"))) {
                System.out.println("Player " + mentionedPlayer.getName() + " has bossbar toggled on");
                plugin.playMentionSound(mentionedPlayer);
                sendBossbar(plugin, mentionedPlayer, message, color, duration);
            }
        }
    }

    // Mention everyone
    public MentionTypeBossbarHandler(AsyncPlayerChatEvent e, MentionChat plugin) {
        FileConfiguration config = plugin.getConfig();
        FileConfiguration data = plugin.getData();
        Player mentioner = e.getPlayer();

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
        BossBar bossBar = Bukkit.createBossBar(Utils.color(message.replace("%player%", player.getName())), BarColor.valueOf(color), BarStyle.SEGMENTED_10);
        bossBar.addPlayer(player);

        new BukkitRunnable() {
            int remainingTime = duration + 1;

            @Override
            public void run() {
                double progress = (double) remainingTime / (duration + 1);

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