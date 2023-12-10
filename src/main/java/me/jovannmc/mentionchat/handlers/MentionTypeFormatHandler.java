package me.jovannmc.mentionchat.handlers;

import me.jovannmc.mentionchat.MentionChat;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.HashSet;
import java.util.Set;

public class MentionTypeFormatHandler {

    // Mention users
    public MentionTypeFormatHandler(AsyncPlayerChatEvent e, HashSet<Player> mentioned, MentionChat plugin) {
        System.out.println("MentionTypeFormatHandler for users");
        FileConfiguration config = plugin.getConfig();
        FileConfiguration data = plugin.getData();

        // Remove all recipients to send custom messages to each player, but lets the message still be logged in the console
        e.getRecipients().removeAll(Bukkit.getOnlinePlayers());

        // We use a HashSet here to track which players have already been sent a message, to prevent duplicate messages
        HashSet<Player> sentMessages = new HashSet<>();

        for (Player mentionedPlayer : mentioned) {
            String mentionSymbol = config.getString("mentionSymbol");
            String mentionPattern = "(?i)" + mentionSymbol + mentionedPlayer.getName() + "\\b";
            String mentionMessage;

            if (data.contains(mentionedPlayer.getUniqueId().toString() + ".format")) {
                mentionMessage = ChatColor.translateAlternateColorCodes('&', data.getString(mentionedPlayer.getUniqueId().toString() + ".format").replace("%mention%", mentionSymbol + mentionedPlayer.getName()));
            } else {
                mentionMessage = ChatColor.translateAlternateColorCodes('&', config.getString("mentionedFormat").replace("%mention%", mentionSymbol + mentionedPlayer.getName()));
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

            TextComponent quickMention = new TextComponent("<" + e.getPlayer().getDisplayName() + ">");
            quickMention.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Mention " + e.getPlayer().getName()).create()));
            quickMention.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, mentionSymbol + e.getPlayer().getName()));

            TextComponent finalMessage = new TextComponent("");
            finalMessage.addExtra(quickMention);

            for (Player player : Bukkit.getOnlinePlayers()) {
                if (!mentioned.contains(player) && !sentMessages.contains(player)) {
                    TextComponent clonedMessage = new TextComponent(finalMessage);
                    clonedMessage.addExtra(" " + e.getMessage());
                    player.spigot().sendMessage(clonedMessage);
                    sentMessages.add(player);
                    System.out.println("send normal message to " + player.getName());
                }
            }

            plugin.playMentionSound(mentionedPlayer);

            finalMessage = new TextComponent("");
            finalMessage.addExtra(quickMention);
            finalMessage.addExtra(" " + newMessage);

            mentionedPlayer.spigot().sendMessage(finalMessage);
            System.out.println("send message to " + mentionedPlayer.getName());
        }
    }

}
