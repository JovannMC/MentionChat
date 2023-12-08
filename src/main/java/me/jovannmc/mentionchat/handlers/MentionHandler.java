package me.jovannmc.mentionchat.handlers;

import me.jovannmc.mentionchat.MentionChat;
import me.jovannmc.mentionchat.events.EveryoneMentionEvent;
import me.jovannmc.mentionchat.events.MultiPlayerMentionEvent;
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

    private void mentionUser(AsyncPlayerChatEvent e, Player mentioner, HashSet<Player> mentionedPlayers) {
        // Check if player has mentions disabled
        List<Player> playersToRemove = new ArrayList<>();
        if (!mentioner.hasPermission("mentionchat.mention.bypass") || !mentioner.hasPermission("mentionchat.mention.bypass.toggle")) {
            for (Player mentionedPlayer : mentionedPlayers) {
                if (plugin.getData().contains(mentionedPlayer.getUniqueId().toString() + ".toggle.mentions") && !plugin.getData().getBoolean(mentionedPlayer.getUniqueId().toString() + ".toggle.mentions")) {
                    playersToRemove.add(mentionedPlayer);
                } else if (mentionedPlayer.hasPermission("mentionchat.mention.exempt") && (!mentioner.hasPermission("mentionchat.mention.bypass.exempt") || !mentioner.hasPermission("mentionchat.mention.bypass"))) {
                    playersToRemove.add(mentionedPlayer);
                }
            }
            if (!playersToRemove.isEmpty()) {
                playersToRemove.forEach(mentionedPlayers::remove);
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

        // Single player mentioned
        if (mentionedPlayers.size() == 1) {
            Player mentioned = mentionedPlayers.iterator().next();
            String uuid = mentioned.getUniqueId().toString();

            // Check player's data if mention type is toggled, if not found, check config for defaults
            if ((plugin.getData().contains(uuid + ".toggle.format") && plugin.getData().getBoolean(uuid + ".toggle.format")) || (getConfig().getString("mentionType").contains("FORMAT") && !plugin.getData().contains(uuid + ".toggle.format"))) {
                new MentionTypeFormatHandler(e, mentioned, plugin);
                Bukkit.getServer().getScheduler().runTask(plugin, () -> {
                    Bukkit.getPluginManager().callEvent(new PlayerMentionEvent(e, mentioned, "FORMAT"));
                });
            }

            if ((plugin.getData().getBoolean(uuid + ".toggle.message") && plugin.getData().getBoolean(uuid + ".toggle.message")) || (getConfig().getString("mentionType").contains("MESSAGE") && !plugin.getData().contains(uuid + ".toggle.message"))) {
                new MentionTypeMessageHandler(e, mentioned, plugin);
                Bukkit.getServer().getScheduler().runTask(plugin, () -> {
                    Bukkit.getPluginManager().callEvent(new PlayerMentionEvent(e, mentioned, "MESSAGE"));
                });
            }

            if ((plugin.getData().getBoolean(uuid + ".toggle.title") && plugin.getData().getBoolean(uuid + ".toggle.title")) || (getConfig().getString("mentionType").contains("TITLE") && !plugin.getData().contains(uuid + ".toggle.title"))) {
                new MentionTypeTitleHandler(e, mentioned, plugin);
                Bukkit.getServer().getScheduler().runTask(plugin, () -> {
                    Bukkit.getPluginManager().callEvent(new PlayerMentionEvent(e, mentioned, "TITLE"));
                });
            }

            if (!Utils.isUnsupportedVersion()) {
                if ((plugin.getData().getBoolean(uuid + ".toggle.bossbar") && plugin.getData().getBoolean(uuid + ".toggle.bossbar")) || (getConfig().getString("mentionType").contains("BOSSBAR") && !plugin.getData().contains(uuid + ".toggle.bossbar"))) {
                    new MentionTypeBossbarHandler(e, mentioned, plugin);
                    Bukkit.getServer().getScheduler().runTask(plugin, () -> {
                        Bukkit.getPluginManager().callEvent(new PlayerMentionEvent(e, mentioned, "BOSSBAR"));
                    });
                }

                if ((plugin.getData().getBoolean(uuid + ".toggle.actionbar") && plugin.getData().getBoolean(uuid + ".toggle.actionbar")) || (getConfig().getString("mentionType").contains("ACTIONBAR") && !plugin.getData().contains(uuid + ".toggle.actionbar"))) {
                    new MentionTypeActionbarHandler(e, mentioned, plugin);
                    Bukkit.getServer().getScheduler().runTask(plugin, () -> {
                        Bukkit.getPluginManager().callEvent(new PlayerMentionEvent(e, mentioned, "ACTIONBAR"));
                    });
                }
            }
        }

        boolean formatCalled = false;
        boolean messageCalled = false;
        boolean titleCalled = false;
        boolean bossbarCalled = false;
        boolean actionbarCalled = false;

        // Multiple players mentioned
        if (mentionedPlayers.size() > 1) {
            for (Player mentioned : mentionedPlayers) {
                String uuid = mentioned.getUniqueId().toString();
                // Check player's data if mention type is toggled, if not found, check config for defaults
                if ((plugin.getData().contains(uuid + ".toggle.format") && plugin.getData().getBoolean(uuid + ".toggle.format")) || (getConfig().getString("mentionType").contains("FORMAT") && !plugin.getData().contains(uuid + ".toggle.format"))) {
                    if (!formatCalled) {
                        new MentionTypeFormatHandler(e, mentionedPlayers, plugin);
                        Bukkit.getServer().getScheduler().runTask(plugin, () -> {
                            Bukkit.getPluginManager().callEvent(new MultiPlayerMentionEvent(e, mentionedPlayers, "FORMAT"));
                        });
                        formatCalled = true;
                    }
                }

                if ((plugin.getData().getBoolean(uuid + ".toggle.message") && plugin.getData().getBoolean(uuid + ".toggle.message")) || (getConfig().getString("mentionType").contains("MESSAGE") && !plugin.getData().contains(uuid + ".toggle.message"))) {
                    if (!messageCalled) {
                        new MentionTypeMessageHandler(e, mentionedPlayers, plugin);
                        Bukkit.getServer().getScheduler().runTask(plugin, () -> {
                            Bukkit.getPluginManager().callEvent(new MultiPlayerMentionEvent(e, mentionedPlayers, "MESSAGE"));
                        });
                        messageCalled = true;
                    }
                }

                if ((plugin.getData().getBoolean(uuid + ".toggle.title") && plugin.getData().getBoolean(uuid + ".toggle.title")) || (getConfig().getString("mentionType").contains("TITLE") && !plugin.getData().contains(uuid + ".toggle.title"))) {
                    if (!titleCalled) {
                        new MentionTypeTitleHandler(e, mentionedPlayers, plugin);
                        Bukkit.getServer().getScheduler().runTask(plugin, () -> {
                            Bukkit.getPluginManager().callEvent(new MultiPlayerMentionEvent(e, mentionedPlayers, "TITLE"));
                        });
                        titleCalled = true;
                    }
                }

                if (!Utils.isUnsupportedVersion()) {
                    if ((plugin.getData().getBoolean(uuid + ".toggle.bossbar") && plugin.getData().getBoolean(uuid + ".toggle.bossbar")) || (getConfig().getString("mentionType").contains("BOSSBAR") && !plugin.getData().contains(uuid + ".toggle.bossbar"))) {
                        if (!bossbarCalled) {
                            new MentionTypeBossbarHandler(e, mentionedPlayers, plugin);
                            Bukkit.getServer().getScheduler().runTask(plugin, () -> {
                                Bukkit.getPluginManager().callEvent(new MultiPlayerMentionEvent(e, mentionedPlayers, "BOSSBAR"));
                            });
                            bossbarCalled = true;
                        }
                    }

                    if ((plugin.getData().getBoolean(uuid + ".toggle.actionbar") && plugin.getData().getBoolean(uuid + ".toggle.actionbar")) || (getConfig().getString("mentionType").contains("ACTIONBAR") && !plugin.getData().contains(uuid + ".toggle.actionbar"))) {
                        if (!actionbarCalled) {
                            new MentionTypeActionbarHandler(e, mentionedPlayers, plugin);
                            Bukkit.getServer().getScheduler().runTask(plugin, () -> {
                                Bukkit.getPluginManager().callEvent(new MultiPlayerMentionEvent(e, mentionedPlayers, "ACTIONBAR"));
                            });
                            actionbarCalled = true;
                        }
                    }
                }
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

        HashSet<Player> onlinePlayers = new HashSet<>(Bukkit.getOnlinePlayers());
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (plugin.getData().contains(player.getUniqueId().toString() + ".toggle.mentions") && !plugin.getData().getBoolean(player.getUniqueId().toString() + ".toggle.mentions")) {
                onlinePlayers.remove(player);
            }
        }

        // Check mention type and handle mention accordingly
        if (getConfig().getString("mentionType").contains("FORMAT")) {
            new MentionTypeFormatHandler(e, plugin);
            Bukkit.getServer().getScheduler().runTask(plugin, () -> {
                Bukkit.getPluginManager().callEvent(new EveryoneMentionEvent(e, onlinePlayers, "FORMAT"));
            });
        }
        if (getConfig().getString("mentionType").contains("MESSAGE")) {
            new MentionTypeMessageHandler(e, plugin);
            Bukkit.getServer().getScheduler().runTask(plugin, () -> {
                Bukkit.getPluginManager().callEvent(new EveryoneMentionEvent(e, onlinePlayers, "MESSAGE"));
            });
        }
        if (getConfig().getString("mentionType").contains("TITLE")) {
            new MentionTypeTitleHandler(e, plugin);
            Bukkit.getServer().getScheduler().runTask(plugin, () -> {
                Bukkit.getPluginManager().callEvent(new EveryoneMentionEvent(e, onlinePlayers, "TITLE"));
            });
        }
        if (!Utils.isUnsupportedVersion()) {
            if (getConfig().getString("mentionType").contains("BOSSBAR")) {
                new MentionTypeBossbarHandler(e, plugin);
                Bukkit.getServer().getScheduler().runTask(plugin, () -> {
                    Bukkit.getPluginManager().callEvent(new EveryoneMentionEvent(e, onlinePlayers, "BOSSBAR"));
                });
            }
            if (getConfig().getString("mentionType").contains("ACTIONBAR")) {
                new MentionTypeActionbarHandler(e, plugin);
                Bukkit.getServer().getScheduler().runTask(plugin, () -> {
                    Bukkit.getPluginManager().callEvent(new EveryoneMentionEvent(e, onlinePlayers, "ACTIONBAR"));
                });
            }
        }
        nextMention.put(mentioner.getUniqueId(), System.currentTimeMillis());
    }

    private FileConfiguration getConfig() {
        return plugin.getConfig();
    }
}
