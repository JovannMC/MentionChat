package me.jovannmc.mentionchat.handlers;

import me.jovannmc.mentionchat.MentionChat;
import me.jovannmc.mentionchat.events.PlayerMentionEvent;
import me.jovannmc.mentionchat.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.*;

public class MentionHandler implements Listener {
    private final HashMap<UUID, Long> nextMention = new HashMap<>();
    private Long nextMentionTime;

    private final MentionChat plugin = MentionChat.getPlugin(MentionChat.class);

    @EventHandler
    public void playerChatEvent(AsyncPlayerChatEvent e) {
        String mentionSymbol = getConfig().getString("mentionSymbol");
        // A HashSet is used as it prevents duplicate entries and is more efficient than an ArrayList
        HashSet<Player> mentionedPlayers = new HashSet<>();
        String[] words = e.getMessage().split(" ");

        Player mentioner = e.getPlayer();

        // Split the message into words and check if any of them are a player's name
        // This is done to prevent similar names from causing issues (eg JovannMC and JovannMC2 being highlighted when only JovannMC2 was mentioned)
        for (String word : words) {
            if (mentionSymbol.isEmpty()) {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (word.equalsIgnoreCase(player.getName())) {
                        // TODO: fix possible bug with no mention symbols, where players could be mentioned with only part of their name
                        // seems like this also plays the mention sound, but doesn't format/highlight message for the mentioned player
                        mentionedPlayers.add(player);
                    }
                }
            }

            if (word.equalsIgnoreCase(mentionSymbol + "everyone")) {
                if (e.getPlayer().hasPermission("mentionchat.mention.everyone")) {
                    mentionEveryone(e, mentioner);
                } else {
                    Utils.sendMessage(mentioner, getConfig().getString("noPermissionMessage"));
                }
                return;
            } else if (word.startsWith(mentionSymbol)) {
                String playerName = word.substring(mentionSymbol.length());
                Player mentionedPlayer = Bukkit.getPlayer(playerName);
                if (mentionedPlayer != null) {
                    mentionedPlayers.add(mentionedPlayer);
                }
            }
        }

        if (!mentionedPlayers.isEmpty()) {
            mentionUser(e, mentioner, mentionedPlayers);
        }
    }

    private void mentionUser(AsyncPlayerChatEvent e, Player mentioner, HashSet<Player> mentioned) {
        // Check if player has mentions disabled
        List<Player> playersToRemove = new ArrayList<>();
        if (!mentioner.hasPermission("mentionchat.mention.bypass") || !mentioner.hasPermission("mentionchat.mention.bypass.toggle")) {
            for (Player mentionedPlayer : mentioned) {
                if (plugin.getData().contains(mentionedPlayer.getUniqueId().toString() + ".toggle.mentions") && !plugin.getData().getBoolean(mentionedPlayer.getUniqueId().toString() + ".toggle.mentions")) {
                    playersToRemove.add(mentionedPlayer);
                } else if (mentionedPlayer.hasPermission("mentionchat.mention.exempt") && (!mentioner.hasPermission("mentionchat.mention.bypass.exempt") || !mentioner.hasPermission("mentionchat.mention.bypass"))) {
                    playersToRemove.add(mentionedPlayer);
                }
            }
            if (!playersToRemove.isEmpty()) {
                playersToRemove.forEach(mentioned::remove);
                List<String> names = new ArrayList<>();
                for (Player player : playersToRemove) {
                    names.add(player.getName());
                }
                Utils.sendMessage(mentioner, getConfig().getString("playerMentionDisabled").replace("%mention%", String.join(", ", names)));
            }
        }

        // Check if player has permission to mention
        if (!mentioner.hasPermission("mentionchat.mention.others")) {
            Utils.sendMessage(mentioner, getConfig().getString("noPermissionMessage"));
            return;
        }

        // Cooldown logic
        if (nextMention.containsKey(mentioner.getUniqueId())) {
            if (!mentioner.hasPermission("mentionchat.mention.bypass") || !mentioner.hasPermission("mentionchat.mention.bypass.cooldown")) {
                nextMentionTime = getConfig().getLong("mentionCooldown");
                long secondsLeft = ((nextMention.get(mentioner.getUniqueId()) / 1000) + nextMentionTime) - (System.currentTimeMillis() / 1000);
                if (secondsLeft > 0) { Utils.sendMessage(mentioner, getConfig().getString("cooldownMessage")); return; }
            }
        }

        // Check mention type and handle mention accordingly
        if (getConfig().getString("mentionType").contains("FORMAT")) {
            new MentionTypeFormatHandler(e, mentioner, mentioned, plugin);
            Bukkit.getPluginManager().callEvent(new PlayerMentionEvent(mentioner, mentioned, "FORMAT"));
        }
        if (getConfig().getString("mentionType").contains("MESSAGE")) {
            new MentionTypeMessageHandler(e, mentioner, mentioned, plugin);
            Bukkit.getPluginManager().callEvent(new PlayerMentionEvent(mentioner, mentioned, "MESSAGE"));
        }
        if (getConfig().getString("mentionType").contains("TITLE")) {
            new MentionTypeTitleHandler(mentioner, mentioned, plugin);
            Bukkit.getPluginManager().callEvent(new PlayerMentionEvent(mentioner, mentioned, "TITLE"));
        }
        if (getConfig().getString("mentionType").contains("BOSSBAR")) {
            new MentionTypeBossbarHandler(mentioner, mentioned, plugin);
            Bukkit.getPluginManager().callEvent(new PlayerMentionEvent(mentioner, mentioned, "BOSSBAR"));
        }
        if (getConfig().getString("mentionType").contains("ACTIONBAR")) {
            new MentionTypeActionbarHandler(mentioner, mentioned, plugin);
            Bukkit.getPluginManager().callEvent(new PlayerMentionEvent(mentioner, mentioned, "ACTIONBAR"));
        }
        nextMention.put(mentioner.getUniqueId(), System.currentTimeMillis());
    }

    private void mentionEveryone(AsyncPlayerChatEvent e, Player mentioner) {
        // Cooldown logic
        if (nextMention.containsKey(mentioner.getUniqueId())) {
            if (!mentioner.hasPermission("mentionchat.mention.bypass")) {
                nextMentionTime = getConfig().getLong("mentionCooldown");
                long secondsLeft = ((nextMention.get(mentioner.getUniqueId()) / 1000) + nextMentionTime) - (System.currentTimeMillis() / 1000);
                if (secondsLeft > 0) {
                    Utils.sendMessage(mentioner, getConfig().getString("cooldownMessage"));
                    return;
                }
            }
        }

        HashSet<Player> onlinePlayers = new HashSet<>(Bukkit.getOnlinePlayers());

        // Check mention type and handle mention accordingly
        if (getConfig().getString("mentionType").contains("FORMAT")) {
            new MentionTypeFormatHandler(e, mentioner, plugin);
            Bukkit.getPluginManager().callEvent(new PlayerMentionEvent(mentioner, onlinePlayers, "FORMAT"));
        }
        if (getConfig().getString("mentionType").contains("MESSAGE")) {
            new MentionTypeMessageHandler(e, mentioner, plugin);
            Bukkit.getPluginManager().callEvent(new PlayerMentionEvent(mentioner, onlinePlayers, "MESSAGE"));
        }
        if (getConfig().getString("mentionType").contains("TITLE")) {
            new MentionTypeTitleHandler(mentioner, plugin);
            Bukkit.getPluginManager().callEvent(new PlayerMentionEvent(mentioner, onlinePlayers, "TITLE"));
        }
        if (getConfig().getString("mentionType").contains("BOSSBAR")) {
            new MentionTypeBossbarHandler(mentioner, plugin);
            Bukkit.getPluginManager().callEvent(new PlayerMentionEvent(mentioner, onlinePlayers, "BOSSBAR"));
        }
        if (getConfig().getString("mentionType").contains("ACTIONBAR")) {
            new MentionTypeActionbarHandler(mentioner, plugin);
            Bukkit.getPluginManager().callEvent(new PlayerMentionEvent(mentioner, onlinePlayers, "ACTIONBAR"));
        }
        nextMention.put(mentioner.getUniqueId(), System.currentTimeMillis());
    }


    private FileConfiguration getConfig() {
        return plugin.getConfig();
    }
}
