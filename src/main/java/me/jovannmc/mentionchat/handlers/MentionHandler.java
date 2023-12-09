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
        // This is done to prevent similar names from causing issues (e.g. JovannMC and JovannMC2 being highlighted when only JovannMC2 was mentioned)
        for (String word : words) {
            if (mentionSymbol.isEmpty()) {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (word.equalsIgnoreCase(player.getName())) {
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
                Player mentionedPlayer = Bukkit.getPlayerExact(playerName);
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

        HashSet<Player> formatEnabled = new HashSet<>();
        HashSet<Player> messageEnabled = new HashSet<>();
        HashSet<Player> titleEnabled = new HashSet<>();
        HashSet<Player> bossbarEnabled = new HashSet<>();
        HashSet<Player> actionbarEnabled = new HashSet<>();

        for (Player player : mentioned) {
            System.out.println("player: " + player.getName());
            if (getConfig().getString("mentionType").toUpperCase().contains("FORMAT") || (plugin.getData().contains(player.getUniqueId().toString() + ".toggle.format") && plugin.getData().getBoolean(player.getUniqueId().toString() + ".toggle.format"))) {
                formatEnabled.add(player);
                System.out.println("format enabled for " + player.getName());
            }
            if (getConfig().getString("mentionType").toUpperCase().contains("MESSAGE") || (plugin.getData().contains(player.getUniqueId().toString() + ".toggle.message") && plugin.getData().getBoolean(player.getUniqueId().toString() + ".toggle.message"))) {
                messageEnabled.add(player);
                System.out.println("message enabled for " + player.getName());
            }
            if (getConfig().getString("mentionType").toUpperCase().contains("TITLE") || (plugin.getData().contains(player.getUniqueId().toString() + ".toggle.title") && plugin.getData().getBoolean(player.getUniqueId().toString() + ".toggle.title"))) {
                titleEnabled.add(player);
                System.out.println("title enabled for " + player.getName());
            }
            if (getConfig().getString("mentionType").toUpperCase().contains("BOSSBAR") || (plugin.getData().contains(player.getUniqueId().toString() + ".toggle.bossbar") && plugin.getData().getBoolean(player.getUniqueId().toString() + ".toggle.bossbar"))) {
                bossbarEnabled.add(player);
                System.out.println("bossbar enabled for " + player.getName());
            }
            if (getConfig().getString("mentionType").toUpperCase().contains("ACTIONBAR") || (plugin.getData().contains(player.getUniqueId().toString() + ".toggle.actionbar") && plugin.getData().getBoolean(player.getUniqueId().toString() + ".toggle.actionbar"))) {
                actionbarEnabled.add(player);
                System.out.println("actionbar enabled for " + player.getName());
            }
        }

        // Check mention type and handle mention accordingly
        if (!formatEnabled.isEmpty()) {
            new MentionTypeFormatHandler(e, formatEnabled, plugin);
            Bukkit.getServer().getScheduler().runTask(plugin, () -> Bukkit.getPluginManager().callEvent(new PlayerMentionEvent(mentioner, formatEnabled, "FORMAT")));
        }
        if (!messageEnabled.isEmpty()) {
            new MentionTypeMessageHandler(e, mentioner, messageEnabled, plugin);
            Bukkit.getServer().getScheduler().runTask(plugin, () -> Bukkit.getPluginManager().callEvent(new PlayerMentionEvent(mentioner, messageEnabled, "MESSAGE")));
        }
        if (!titleEnabled.isEmpty()) {
            new MentionTypeTitleHandler(mentioner, titleEnabled, plugin);
            Bukkit.getServer().getScheduler().runTask(plugin, () -> Bukkit.getPluginManager().callEvent(new PlayerMentionEvent(mentioner, titleEnabled, "TITLE")));
        }
        if (!Utils.isUnsupportedVersion()) {
            if (!bossbarEnabled.isEmpty()) {
                new MentionTypeBossbarHandler(mentioner, bossbarEnabled, plugin);
                Bukkit.getServer().getScheduler().runTask(plugin, () -> Bukkit.getPluginManager().callEvent(new PlayerMentionEvent(mentioner, bossbarEnabled, "BOSSBAR")));
            }
            if (!actionbarEnabled.isEmpty()) {
                new MentionTypeActionbarHandler(mentioner, actionbarEnabled, plugin);
                Bukkit.getServer().getScheduler().runTask(plugin, () -> Bukkit.getPluginManager().callEvent(new PlayerMentionEvent(mentioner, actionbarEnabled, "ACTIONBAR")));
            }
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

        HashSet<Player> formatEnabled = new HashSet<>();
        HashSet<Player> messageEnabled = new HashSet<>();
        HashSet<Player> titleEnabled = new HashSet<>();
        HashSet<Player> bossbarEnabled = new HashSet<>();
        HashSet<Player> actionbarEnabled = new HashSet<>();

        for (Player player : Bukkit.getOnlinePlayers()) {
            System.out.println("player: " + player.getName());
            if (getConfig().getString("mentionType").toUpperCase().contains("FORMAT") || (plugin.getData().contains(player.getUniqueId().toString() + ".toggle.format") && plugin.getData().getBoolean(player.getUniqueId().toString() + ".toggle.format"))) {
                formatEnabled.add(player);
                System.out.println("format enabled for " + player.getName());
            }
            if (getConfig().getString("mentionType").toUpperCase().contains("MESSAGE") || (plugin.getData().contains(player.getUniqueId().toString() + ".toggle.message") && plugin.getData().getBoolean(player.getUniqueId().toString() + ".toggle.message"))) {
                messageEnabled.add(player);
                System.out.println("message enabled for " + player.getName());
            }
            if (getConfig().getString("mentionType").toUpperCase().contains("TITLE") || (plugin.getData().contains(player.getUniqueId().toString() + ".toggle.title") && plugin.getData().getBoolean(player.getUniqueId().toString() + ".toggle.title"))) {
                titleEnabled.add(player);
                System.out.println("title enabled for " + player.getName());
            }
            if (getConfig().getString("mentionType").toUpperCase().contains("BOSSBAR") || (plugin.getData().contains(player.getUniqueId().toString() + ".toggle.bossbar") && plugin.getData().getBoolean(player.getUniqueId().toString() + ".toggle.bossbar"))) {
                bossbarEnabled.add(player);
                System.out.println("bossbar enabled for " + player.getName());
            }
            if (getConfig().getString("mentionType").toUpperCase().contains("ACTIONBAR") || (plugin.getData().contains(player.getUniqueId().toString() + ".toggle.actionbar") && plugin.getData().getBoolean(player.getUniqueId().toString() + ".toggle.actionbar"))) {
                actionbarEnabled.add(player);
                System.out.println("actionbar enabled for " + player.getName());
            }
        }

        // Check mention type and handle mention accordingly
        if (!formatEnabled.isEmpty()) {
            new MentionTypeFormatHandler(e, plugin);
            Bukkit.getServer().getScheduler().runTask(plugin, () -> Bukkit.getPluginManager().callEvent(new PlayerMentionEvent(mentioner, formatEnabled, "FORMAT")));
        }
        if (!messageEnabled.isEmpty()) {
            new MentionTypeMessageHandler(e, mentioner, plugin);
            Bukkit.getServer().getScheduler().runTask(plugin, () -> Bukkit.getPluginManager().callEvent(new PlayerMentionEvent(mentioner, messageEnabled, "MESSAGE")));
        }
        if (!titleEnabled.isEmpty()) {
            new MentionTypeTitleHandler(mentioner, plugin);
            Bukkit.getServer().getScheduler().runTask(plugin, () -> Bukkit.getPluginManager().callEvent(new PlayerMentionEvent(mentioner, titleEnabled, "TITLE")));
        }
        if (!Utils.isUnsupportedVersion()) {
            if (!bossbarEnabled.isEmpty()) {
                new MentionTypeBossbarHandler(mentioner, plugin);
                Bukkit.getServer().getScheduler().runTask(plugin, () -> Bukkit.getPluginManager().callEvent(new PlayerMentionEvent(mentioner, bossbarEnabled, "BOSSBAR")));
            }
            if (!actionbarEnabled.isEmpty()) {
                new MentionTypeActionbarHandler(mentioner, plugin);
                Bukkit.getServer().getScheduler().runTask(plugin, () -> Bukkit.getPluginManager().callEvent(new PlayerMentionEvent(mentioner, actionbarEnabled, "ACTIONBAR")));
            }
        }
        nextMention.put(mentioner.getUniqueId(), System.currentTimeMillis());
    }

    private FileConfiguration getConfig() {
        return plugin.getConfig();
    }
}
