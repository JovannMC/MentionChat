package me.jovannmc.mentionchat;

import java.util.HashMap;
import java.util.UUID;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.plugin.java.JavaPlugin;

public final class MentionChat extends JavaPlugin implements Listener {

    private HashMap<UUID, Long> nextMention = new HashMap<UUID, Long>();
    private Long nextMentionTime;

    public void onEnable() {
        saveDefaultConfig();
        nextMentionTime = getConfig().getLong("cooldown");
        Bukkit.getPluginManager().registerEvents(this, this);

        /* TODO: Add bStats support
        int pluginId = ;
        MetricsLite metrics = new MetricsLite(this, pluginId);
        if (metrics.isEnabled()) {
            Bukkit.getLogger().info("Metrics has been enabled for MentionChat. To opt-out, disable 'enabled' in plugins/bStats/config.yml");
        }*/
    }

    public void onDisable() {
        nextMention.clear();
    }

    @EventHandler
    public void playerChatEvent(AsyncPlayerChatEvent e) {
        for (Player p : Bukkit.getOnlinePlayers()) {
            String playerName = p.getName();

            if (e.getMessage().contains("@" + playerName)) {
                if (e.getPlayer().hasPermission("mentionchat.mention.others")) {
                    Player mentioned = Bukkit.getPlayerExact(playerName);

                    if (mentioned.isOnline() && mentioned != null) {
                        mention(e.getPlayer(), Bukkit.getPlayerExact(playerName));
                    }

                } else {
                    e.getPlayer().sendMessage(ChatColor.translateAlternateColorCodes('&',
                            getConfig().getString("noPermissionMessage").replace("%player%", e.getPlayer().getName())));
                }
            } else if (e.getMessage().toLowerCase().contains("@everyone")) {
                if (e.getPlayer().hasPermission("mentionchat.mention.everyone")) {
                    if (p.getName() != e.getPlayer().getName()) {
                        p.sendMessage(ChatColor.translateAlternateColorCodes('&', getConfig()
                                .getString("mentionedMessage").replace("%player%", e.getPlayer().getName())));
                        try {
                            p.playSound(p.getLocation(), Sound.valueOf(getConfig().getString("mentionedSound")), 1.0F,
                                    1.0F);
                        } catch (Exception exception) {
                            Bukkit.getLogger().log(Level.WARNING,
                                    "An error occurred while trying to play the sound set in the config. This is most likely caused by an invalid sound in the config. Check the stacktrace for more info:");
                            exception.printStackTrace();
                        }
                        return;

                    }
                } else {
                    e.getPlayer().sendMessage(ChatColor.translateAlternateColorCodes('&',
                            getConfig().getString("noPermissionMessage").replace("%player%", e.getPlayer().getName())));
                    return;
                }
            }
        }
    }

    public boolean mention(Player mentioner, Player mentioned) {
        if (mentioned != null) {
            if (nextMention.containsKey(mentioner.getUniqueId())) {
                if (!mentioner.hasPermission("mentionchat.mention.bypass")) {
                    long secondsLeft = ((nextMention.get(mentioner.getUniqueId()) / 1000) + nextMentionTime)
                            - (System.currentTimeMillis() / 1000);
                    if (secondsLeft > 0) {
                        mentioner.sendMessage(
                                ChatColor.translateAlternateColorCodes('&', getConfig().getString("cooldownMessage")));
                        return true;
                    }
                }
            }

            nextMention.put(mentioner.getUniqueId(), System.currentTimeMillis());
            mentioned.sendMessage(ChatColor.translateAlternateColorCodes('&',
                    getConfig().getString("mentionedMessage").replace("%player%", mentioner.getName())));
            try {
                mentioned.playSound(mentioned.getLocation(), Sound.valueOf(getConfig().getString("mentionedSound")),
                        1.0F, 1.0F);
            } catch (Exception exception) {
                Bukkit.getLogger().log(Level.SEVERE,
                        "An error occurred while trying to play the sound set in the config, check the stacktrace:");
                exception.printStackTrace();
            }

        }
        return false;

    }

}