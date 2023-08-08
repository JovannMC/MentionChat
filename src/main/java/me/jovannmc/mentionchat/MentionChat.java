package me.jovannmc.mentionchat;

import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.HashSet;
import java.util.UUID;
import java.util.logging.Level;

public final class MentionChat extends JavaPlugin implements Listener {
    /*
        Hiya! Welcome to the source code of this plugin.
        Of course any contributions are welcome, just make a pull request or something lol, roast my horrible code.
        -JovannMC
     */

    private HashMap<UUID, Long> nextMention = new HashMap<UUID, Long>();
    private boolean unsupportedCheck = false;
    private Long nextMentionTime;

    public void onEnable() {
        saveDefaultConfig();
        Bukkit.getPluginCommand("mentionchat").setExecutor(new MentionChatCommand());
        Bukkit.getPluginManager().registerEvents(this, this);
        Metrics metrics = new Metrics(this, 19327);

        Bukkit.getLogger().log(Level.INFO, "MentionChat v" + getDescription().getVersion() + " has been enabled! Server version: " + getServerVersion());

        // Check for updates
        if (getConfig().getBoolean("checkForUpdates")) {
            new UpdateChecker(this, 111656).getVersion(version -> {
                if (this.getDescription().getVersion().equals(version)) {
                    Bukkit.getLogger().info("You are on the latest version of MentionChat. (v" + version + ")");
                } else {
                    Bukkit.getLogger().info("There is a new update available for MentionChat (v" + version + "). Please update at https://www.spigotmc.org/resources/111656/");
                }
            });
        }

        // Check config version
        if (getConfig().getInt("configVersion") != 2) {
            Bukkit.getLogger().log(Level.SEVERE,
                    "Your config.yml is outdated. Please regenerate the config.yml and reconfigure MentionChat.");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

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

    /*
        Mentioning code
    */

    @EventHandler
    public void playerChatEvent(AsyncPlayerChatEvent e) {
        // A HashSet is used as it prevents duplicate entries and is more efficient than an ArrayList
        HashSet<Player> mentionedPlayers = new HashSet<>();
        String[] words = e.getMessage().split(" ");

        // Split the message into words and check if any of them are a player's name
        // This is done to prevent similar names from causing issues (eg JovannMC and JovannMC2 being highlighted when only JovannMC2 was mentioned)
        for (String word : words) {
            if (word.equalsIgnoreCase("@everyone")) {
                if (e.getPlayer().hasPermission("mentionchat.mention.everyone")) {
                    mentionEveryone(e, e.getPlayer());
                } else {
                    e.getPlayer().sendMessage(ChatColor.translateAlternateColorCodes('&',
                            getConfig().getString("noPermissionMessage")));
                }
                return;
            } else if (word.startsWith("@")) {
                String playerName = word.substring(1);
                Player mentionedPlayer = Bukkit.getPlayerExact(playerName);

                if (mentionedPlayer != null && mentionedPlayer.isOnline() && !mentionedPlayers.contains(mentionedPlayer)) {
                    mentionedPlayers.add(mentionedPlayer);
                }
            }
        }

        if (mentionedPlayers.size() > 0) {
            mentionUser(e, e.getPlayer(), mentionedPlayers);
        }
    }

    private void mentionUser(AsyncPlayerChatEvent e, Player mentioner, HashSet<Player> mentioned) {
        // Cooldown logic
        if (nextMention.containsKey(mentioner.getUniqueId())) {
            if (!mentioner.hasPermission("mentionchat.mention.bypass")) {
                nextMentionTime = getConfig().getLong("cooldown");
                long secondsLeft = ((nextMention.get(mentioner.getUniqueId()) / 1000) + nextMentionTime)
                        - (System.currentTimeMillis() / 1000);
                if (secondsLeft > 0) {
                    mentioner.sendMessage(ChatColor.translateAlternateColorCodes('&',
                            getConfig().getString("cooldownMessage")));
                    return;
                }
            }
        }

        // Remove all recipients to send custom messages to each player, but lets the message still be logged in console
        e.getRecipients().removeAll(Bukkit.getOnlinePlayers());
        String type = getConfig().getString("mentionType");

        // We use a HashSet here to track which players have already been sent a message, to prevent duplicate messages
        HashSet<Player> sentMessages = new HashSet<>();

        if (type.equalsIgnoreCase("MESSAGE")) {
            // Small bug here where with multiple mentions, the "mentionedMessage" may appear after the message instead of before.
            // It's inconsistency caused by the loop but doesn't really need to be fixed.
            for (Player mentionedPlayer : mentioned) {
                mentionedPlayer.sendMessage(ChatColor.translateAlternateColorCodes('&',
                        getConfig().getString("mentionedMessage").replace("%player%", mentioner.getName())));
                playMentionSound(mentionedPlayer);

                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (!sentMessages.contains(player)) {
                        // Add the player to the HashSet so they don't get sent the same message multiple times
                        player.sendMessage(e.getFormat().replace("%1$s", mentioner.getDisplayName()).replace("%2$s", e.getMessage()));
                        sentMessages.add(player);
                    }
                }
            }
        } else if (type.equalsIgnoreCase("FORMAT")) {
            for (Player mentionedPlayer : mentioned) {
                String mentionPattern = "(?i)@" + mentionedPlayer.getName() + "\\b";
                String mentionMessage = ChatColor.translateAlternateColorCodes('&',
                        getConfig().getString("mentionFormat").replace("%mention%", "@" + mentionedPlayer.getName()));

                // Like previously, we split the message into words and check if any of them are a player's name to prevent duplicates
                String[] words = e.getMessage().split("\\s+");
                StringBuilder newMessageBuilder = new StringBuilder();
                for (String word : words) {
                    if (word.matches(mentionPattern)) {
                        newMessageBuilder.append(" ").append(mentionMessage);
                    } else {
                        newMessageBuilder.append(" ").append(word);
                    }
                }

                String newMessage = newMessageBuilder.toString().trim();

                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (!mentioned.contains(player) && !sentMessages.contains(player)) {
                        // Add the player to the HashSet so they don't get sent the same message multiple times
                        player.sendMessage(e.getFormat().replace("%1$s", mentioner.getDisplayName()).replace("%2$s", e.getMessage()));
                        sentMessages.add(player);
                    }
                }

                playMentionSound(mentionedPlayer);
                mentionedPlayer.sendMessage(e.getFormat().replace("%1$s", mentioner.getDisplayName()).replace("%2$s", newMessage));
            }

            nextMention.put(mentioner.getUniqueId(), System.currentTimeMillis());
        } else {
            Bukkit.getLogger().log(Level.SEVERE, "Invalid mention type in MentionChat's config. (" + type + ")");
        }
    }

    private void mentionEveryone(AsyncPlayerChatEvent e, Player mentioner) {
        // Cooldown logic
        if (nextMention.containsKey(mentioner.getUniqueId())) {
            if (!mentioner.hasPermission("mentionchat.mention.bypass")) {
                nextMentionTime = getConfig().getLong("cooldown");
                long secondsLeft = ((nextMention.get(mentioner.getUniqueId()) / 1000) + nextMentionTime)
                        - (System.currentTimeMillis() / 1000);
                if (secondsLeft > 0) {
                    mentioner.sendMessage(ChatColor.translateAlternateColorCodes('&',
                            getConfig().getString("cooldownMessage")));
                    return;
                }
            }
        }

        // Remove all recipients to send custom messages to each player, but lets the message still be logged in console
        e.getRecipients().removeAll(Bukkit.getOnlinePlayers());
        String type = getConfig().getString("mentionType");

        for (Player p : Bukkit.getOnlinePlayers()) {
            if (type.equalsIgnoreCase("MESSAGE")) {
                p.sendMessage(ChatColor.translateAlternateColorCodes('&',
                        getConfig().getString("mentionedMessage").replace("%player%", mentioner.getName())));
                p.sendMessage(e.getFormat().replace("%1$s", mentioner.getDisplayName()).replace("%2$s", e.getMessage()));
            } else if (type.equalsIgnoreCase("FORMAT")) {
                String mentionPattern = "(?i)@everyone\\b";
                String mentionMessage = ChatColor.translateAlternateColorCodes('&',
                        getConfig().getString("mentionFormat").replace("%mention%", "@everyone"));

                // Like previously, we split the message into words so that it has to be @everyone, and also case-insensitive
                String[] words = e.getMessage().split("\\s+");
                StringBuilder newMessageBuilder = new StringBuilder();
                for (String word : words) {
                    if (word.matches(mentionPattern)) {
                        newMessageBuilder.append(" ").append(mentionMessage);
                    } else {
                        newMessageBuilder.append(" ").append(word);
                    }
                }

                String newMessage = newMessageBuilder.toString().trim();

                p.sendMessage(e.getFormat().replace("%1$s", mentioner.getDisplayName()).replace("%2$s", newMessage));
            } else {
                Bukkit.getLogger().log(Level.SEVERE, "Invalid mention type in MentionChat's config. (" + type + ")");
            }
            nextMention.put(mentioner.getUniqueId(), System.currentTimeMillis());
            playMentionSound(p);
        }
    }

    private void playMentionSound(Player mentioned) {
        try {
            String mentionedSound = getConfig().getString("mentionedSound");
            Class<?> soundEnumClass = Class.forName("org.bukkit.Sound");
            Object soundEnum;

            if (unsupportedCheck) {
                soundEnum = Enum.valueOf((Class<Enum>) soundEnumClass, "SUCCESSFUL_HIT");
            } else {
                soundEnum = Enum.valueOf((Class<Enum>) soundEnumClass, mentionedSound);
            }

            if (mentionedSound != null && !mentionedSound.equalsIgnoreCase("NONE")) {
                mentioned.playSound(mentioned.getLocation(), (Sound) soundEnum, 1.0f, 1.0f);
            }
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