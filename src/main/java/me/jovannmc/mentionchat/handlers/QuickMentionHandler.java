package me.jovannmc.mentionchat.handlers;

import me.jovannmc.mentionchat.MentionChat;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import me.jovannmc.mentionchat.events.PlayerMentionEvent;

public class QuickMentionHandler implements Listener {

    MentionChat plugin = MentionChat.getPlugin(MentionChat.class);

    private boolean mentionEventOccurred = false;

    @EventHandler
    public void playerChatEvent(AsyncPlayerChatEvent e) {
        if (mentionEventOccurred) {
            mentionEventOccurred = false;
            return;
        }

        // Remove all recipients to send custom messages to each player, but lets the message still be logged in the console
        e.getRecipients().removeAll(Bukkit.getOnlinePlayers());

        String mentionSymbol = plugin.getConfig().getString("mentionSymbol");

        TextComponent quickMention = new TextComponent("<" + e.getPlayer().getDisplayName() + ">");
        quickMention.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Mention " + e.getPlayer().getName()).create()));
        quickMention.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, mentionSymbol + e.getPlayer().getName()));

        TextComponent finalMessage = new TextComponent("");
        finalMessage.addExtra(quickMention);
        finalMessage.addExtra(" " + e.getMessage());

        for (Player player : Bukkit.getOnlinePlayers()) {
            player.spigot().sendMessage(finalMessage);
        }
    }

    @EventHandler
    public void onPlayerMention(PlayerMentionEvent event) {
        mentionEventOccurred = true;
    }
}
