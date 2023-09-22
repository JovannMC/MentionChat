package me.jovannmc.mentionchat.handlers;

import me.jovannmc.mentionchat.MentionChat;
import me.jovannmc.mentionchat.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.*;
import java.util.logging.Level;

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
                if (plugin.getData().contains(mentionedPlayer.getUniqueId().toString() + ".toggle") && !plugin.getData().getBoolean(mentionedPlayer.getUniqueId().toString() + ".toggle")) {
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
                nextMentionTime = getConfig().getLong("cooldown");
                long secondsLeft = ((nextMention.get(mentioner.getUniqueId()) / 1000) + nextMentionTime) - (System.currentTimeMillis() / 1000);
                if (secondsLeft > 0) { Utils.sendMessage(mentioner, getConfig().getString("cooldownMessage")); return; }
            }
        }

        // Remove all recipients to send custom messages to each player, but lets the message still be logged in the console
        e.getRecipients().removeAll(Bukkit.getOnlinePlayers());

        // We use a HashSet here to track which players have already been sent a message, to prevent duplicate messages
        HashSet<Player> sentMessages = new HashSet<>();

        for (Player mentionedPlayer : mentioned) {
            String mentionSymbol = getConfig().getString("mentionSymbol");
            String mentionPattern = "(?i)" + mentionSymbol + mentionedPlayer.getName() + "\\b";
            String mentionMessage;

            if (plugin.getData().contains(mentionedPlayer.getUniqueId().toString() + ".format")) {
                mentionMessage = ChatColor.translateAlternateColorCodes('&', plugin.getData().getString(mentionedPlayer.getUniqueId().toString() + ".format").replace("%mention%", mentionSymbol + mentionedPlayer.getName()));
            } else {
                mentionMessage = ChatColor.translateAlternateColorCodes('&', getConfig().getString("mentionFormat").replace("%mention%", mentionSymbol + mentionedPlayer.getName()));
            }

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
                    // Add the player to the HashSet, so they don't get sent the same message multiple times
                    player.sendMessage(e.getFormat().replace("%1$s", mentioner.getDisplayName()).replace("%2$s", e.getMessage()));
                    sentMessages.add(player);
                }
            }
            playMentionSound(mentionedPlayer);
            mentionedPlayer.sendMessage(e.getFormat().replace("%1$s", mentioner.getDisplayName()).replace("%2$s", newMessage));
        }
        nextMention.put(mentioner.getUniqueId(), System.currentTimeMillis());
    }

    private void mentionEveryone(AsyncPlayerChatEvent e, Player mentioner) {
        // Cooldown logic
        if (nextMention.containsKey(mentioner.getUniqueId())) {
            if (!mentioner.hasPermission("mentionchat.mention.bypass")) {
                nextMentionTime = getConfig().getLong("cooldown");
                long secondsLeft = ((nextMention.get(mentioner.getUniqueId()) / 1000) + nextMentionTime) - (System.currentTimeMillis() / 1000);
                if (secondsLeft > 0) {
                    Utils.sendMessage(mentioner, getConfig().getString("cooldownMessage"));
                    return;
                }
            }
        }

        // Remove all recipients to send custom messages to each player, but lets the message still be logged in the console
        e.getRecipients().removeAll(Bukkit.getOnlinePlayers());

        for (Player p : Bukkit.getOnlinePlayers()) {
            String mentionSymbol = getConfig().getString("mentionSymbol");
            String mentionPattern = "(?i)" + mentionSymbol + "everyone\\b";
            String mentionMessage = ChatColor.translateAlternateColorCodes('&', getConfig().getString("mentionFormat").replace("%mention%", mentionSymbol + "everyone"));

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

            nextMention.put(mentioner.getUniqueId(), System.currentTimeMillis());
            playMentionSound(p);
        }
    }

    private void playMentionSound(Player mentioned) {
        boolean customSound = false;
        try {
            String mentionedSound;

            if (plugin.getData().contains(mentioned.getUniqueId().toString() + ".sound")) {
                mentionedSound = plugin.getData().getString(mentioned.getUniqueId().toString() + ".sound");
                customSound = true;
            } else {
                mentionedSound = getConfig().getString("mentionedSound");
            }

            Sound soundEnum;

            if (Utils.isLegacyVersion() && mentionedSound.equalsIgnoreCase("ENTITY_ARROW_HIT_PLAYER")) {
                // On legacy version and is using default sound which isn't supported
                soundEnum = Sound.valueOf("SUCCESSFUL_HIT");
            } else {
                soundEnum = Sound.valueOf(mentionedSound);
            }

            if (mentionedSound != null && !mentionedSound.equalsIgnoreCase("NONE")) {
                mentioned.playSound(mentioned.getLocation(), soundEnum, 1.0f, 1.0f);
            }
        } catch (Exception e) {
            if (customSound) {
                Bukkit.getLogger().log(Level.SEVERE, "An error occurred while trying to play the sound (" + plugin.getData().getString(mentioned.getUniqueId().toString() + ".sound") + ") for the player " + mentioned.getName() + " with UUID " + mentioned.getUniqueId(), e);
                Utils.sendMessage(mentioned, "&cAn error occurred while trying to play your custom sound (" + plugin.getData().getString(mentioned.getUniqueId().toString() + ".sound") + "). Please contact an administrator.");
            } else {
                Bukkit.getLogger().log(Level.SEVERE, "An error occurred while trying to play the config sound (" + getConfig().getString("mentionedSound") + ").");
                Bukkit.getLogger().log(Level.SEVERE, "The sound set in the config may be invalid.", e);
                Utils.sendMessage(mentioned, "&cAn error occurred while trying to play the mention sound. (" + getConfig().getString("mentionedSound") + "). Please contact an administrator.");
            }
        }
    }

    private FileConfiguration getConfig() {
        return plugin.getConfig();
    }
}
