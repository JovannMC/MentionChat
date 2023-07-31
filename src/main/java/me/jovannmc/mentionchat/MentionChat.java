package me.jovannmc.mentionchat;

import java.util.HashMap;
import java.util.UUID;
import java.util.logging.Level;

import org.bstats.bukkit.Metrics;
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
    private boolean unsupportedCheck = false;

    public void onEnable() {
        saveDefaultConfig();
        nextMentionTime = getConfig().getLong("cooldown");
        Bukkit.getPluginManager().registerEvents(this, this);
        Metrics metrics = new Metrics(this, 19327);

        Bukkit.getLogger().log(Level.INFO, "MentionChat has been enabled! Server version: " + getServerVersion());

        // If on a legacy server version and using the default sound, warn the console.
        String serverVersion = getServerVersion();
        if (serverVersion.startsWith("v1_8") || serverVersion.startsWith("v1_7")) {
            if (getConfig().getString("mentionedSound").equals("ENTITY_ARROW_HIT_PLAYER")) {
                unsupportedCheck = true;
                Bukkit.getLogger().log(Level.WARNING,
                        "You are using the default sound (ENTITY_ARROW_HIT_PLAYER), which isn't supported on this legacy version. Please update your config.yml to use a supported sound. An alternative sound (SUCCESSFUL_HIT) will be used instead.");
            }
        }
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
                        mentionUser(e.getPlayer(), Bukkit.getPlayerExact(playerName));
                    }
                } else {
                    e.getPlayer().sendMessage(ChatColor.translateAlternateColorCodes('&',
                            getConfig().getString("noPermissionMessage")));
                }
            } else if (e.getMessage().toLowerCase().contains("@everyone")) {
                if (e.getPlayer().hasPermission("mentionchat.mention.everyone")) {
                    mentionEveryone(e.getPlayer());
                } else {
                    e.getPlayer().sendMessage(ChatColor.translateAlternateColorCodes('&',
                            getConfig().getString("noPermissionMessage")));
                    return;
                }
            }
        }
    }

    private void mentionUser(Player mentioner, Player mentioned) {
        if (mentioned == null) {
            return;
        }

        if (nextMention.containsKey(mentioner.getUniqueId())) {
            if (!mentioner.hasPermission("mentionchat.mention.bypass")) {
                long secondsLeft = ((nextMention.get(mentioner.getUniqueId()) / 1000) + nextMentionTime)
                        - (System.currentTimeMillis() / 1000);
                if (secondsLeft > 0) {
                    mentioner.sendMessage(
                            ChatColor.translateAlternateColorCodes('&', getConfig().getString("cooldownMessage")));
                    return;
                }
            }
        }

        nextMention.put(mentioner.getUniqueId(), System.currentTimeMillis());
        mentioned.sendMessage(ChatColor.translateAlternateColorCodes('&',
                getConfig().getString("mentionedMessage").replace("%player%", mentioner.getName())));
        playMentionSound(mentioned);
    }

    private void mentionEveryone(Player mentioner) {
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (p.getName() != mentioner.getName()) {
                playMentionSound(p);
                p.sendMessage(ChatColor.translateAlternateColorCodes('&',
                        getConfig().getString("mentionedMessage").replace("%player%", mentioner.getName())));
            }
        }
    }

    private void playMentionSound(Player mentioned) {
        try {
            Class<?> soundEnumClass = Class.forName("org.bukkit.Sound");
            Object soundEnum;

            if (unsupportedCheck) {
                soundEnum = Enum.valueOf((Class<Enum>) soundEnumClass, "SUCCESSFUL_HIT");
            } else {
                soundEnum = Enum.valueOf((Class<Enum>) soundEnumClass, getConfig().getString("mentionedSound"));
            }

            mentioned.playSound(mentioned.getLocation(), (Sound) soundEnum, 1.0f, 1.0f);
        } catch (Exception exception) {
            Bukkit.getLogger().log(Level.SEVERE,
                    "An error occurred while trying to play the sound set in the config. This is most likely caused by an invalid sound in the config. Check the stacktrace for more info:");
            exception.printStackTrace();
        }
    }

    public String getServerVersion() {
        String packageName = Bukkit.getServer().getClass().getPackage().getName();
        return packageName.substring(packageName.lastIndexOf('.') + 1);
    }

}