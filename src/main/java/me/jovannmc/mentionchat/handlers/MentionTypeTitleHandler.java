package me.jovannmc.mentionchat.handlers;

import me.jovannmc.mentionchat.MentionChat;
import me.jovannmc.mentionchat.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.HashSet;
import java.util.logging.Level;

public class MentionTypeTitleHandler {

    // Mention Users
    public MentionTypeTitleHandler(Player mentioner, HashSet<Player> mentioned, MentionChat plugin) {
        System.out.println("MentionTypeTitleHandler for users");
        FileConfiguration config = plugin.getConfig();
        FileConfiguration data = plugin.getData();

        // Get the title and subtitle from user data, if not found, use the default title and subtitle from config and then send the title/subtitle to player
        String title;
        String subtitle;
        int duration;
        if (data.contains(mentioner.getUniqueId().toString() + ".title.title")) {
            title = ChatColor.translateAlternateColorCodes('&', data.getString(mentioner.getUniqueId().toString() + ".title.title").replace("%player%", mentioner.getName()));
        } else {
            title = ChatColor.translateAlternateColorCodes('&', config.getString(".mentionedTitle").replace("%player%", mentioner.getName()));
        }

        if (data.contains(mentioner.getUniqueId().toString() + ".title.subtitle")) {
            subtitle = ChatColor.translateAlternateColorCodes('&', data.getString(mentioner.getUniqueId().toString() + ".title.subtitle").replace("%player%", mentioner.getName()));
        } else {
            subtitle = ChatColor.translateAlternateColorCodes('&', config.getString(".mentionedSubtitle").replace("%player%", mentioner.getName()));
        }

        if (data.contains(mentioner.getUniqueId().toString() + ".duration")) {
            duration = data.getInt(mentioner.getUniqueId().toString() + ".duration");
        } else {
            duration = config.getInt("mentionedDuration");
        }

        // send title
        for (Player mentionedPlayer : mentioned) {
            plugin.playMentionSound(mentionedPlayer);
            sendTitle(mentionedPlayer, title, subtitle, duration);
        }
    }

    // Mention everyone
    public MentionTypeTitleHandler(Player mentioner, MentionChat plugin) {
        System.out.println("MentionTypeTitleHandler for everyone");
        FileConfiguration config = plugin.getConfig();
        FileConfiguration data = plugin.getData();

        // Get the title and subtitle from user data, if not found, use the default title and subtitle from config and then send the title/subtitle to player
        String title;
        String subtitle;
        int duration;
        if (data.contains(mentioner.getUniqueId().toString() + ".title.title")) {
            title = ChatColor.translateAlternateColorCodes('&', data.getString(mentioner.getUniqueId().toString() + ".title.title").replace("%player%", mentioner.getName()));
        } else {
            title = ChatColor.translateAlternateColorCodes('&', config.getString(".mentionedTitle").replace("%player%", mentioner.getName()));
        }

        if (data.contains(mentioner.getUniqueId().toString() + ".title.subtitle")) {
            subtitle = ChatColor.translateAlternateColorCodes('&', data.getString(mentioner.getUniqueId().toString() + ".title.subtitle").replace("%player%", mentioner.getName()));
        } else {
            subtitle = ChatColor.translateAlternateColorCodes('&', config.getString(".mentionedSubtitle").replace("%player%", mentioner.getName()));
        }

        if (data.contains(mentioner.getUniqueId().toString() + ".duration")) {
            duration = data.getInt(mentioner.getUniqueId().toString() + ".duration");
        } else {
            duration = config.getInt("mentionedDuration");
        }

        // send title
        for (Player player : Bukkit.getOnlinePlayers()) {
            plugin.playMentionSound(player);
            sendTitle(player, title, subtitle, duration);
        }
    }

    public void sendTitle(Player player, String title, String subtitle, int stayTime) {
        String[] legacyVersions = {"v1_10", "v1_9"};
        for (String legacyVersion : legacyVersions) {
            if (Utils.getServerVersion().startsWith(legacyVersion)) {
                sendTitleLegacy(player, title, subtitle, stayTime);
                System.out.println("send legacy title to " + player.getName());
                return;
            }
        }

        player.sendTitle(Utils.color(title), Utils.color(subtitle), 10, stayTime * 20, 20);
        System.out.println("send title to " + player.getName());
    }

    // For versions 1.10 and 1.9
    private void sendTitleLegacy(Player player, String title, String subtitle, int stayTime) {
        try {
            String nmsPackage = "net.minecraft.server." + Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
            Class<?> mainTitlePacket = Class.forName(nmsPackage + ".PacketPlayOutTitle");
            Class<?> chatSerializer = Class.forName(nmsPackage + ".IChatBaseComponent").getDeclaredClasses()[0];

            Object titleText = chatSerializer.getMethod("a", String.class).invoke(null, "{\"text\":\"" + title.replace("&", "§") + "\"}");
            Object subtitleText = chatSerializer.getMethod("a", String.class).invoke(null, "{\"text\":\"" + subtitle.replace("&", "§") + "\"}");

            Object titlePacket = mainTitlePacket
                    .getConstructor(mainTitlePacket.getDeclaredClasses()[0], Class.forName(nmsPackage + ".IChatBaseComponent"), int.class, int.class, int.class)
                    .newInstance(mainTitlePacket.getDeclaredClasses()[0].getField("TITLE").get(null), titleText, 0, stayTime, 0);

            Object subtitlePacket = mainTitlePacket
                    .getConstructor(mainTitlePacket.getDeclaredClasses()[0], Class.forName(nmsPackage + ".IChatBaseComponent"), int.class, int.class, int.class)
                    .newInstance(mainTitlePacket.getDeclaredClasses()[0].getField("SUBTITLE").get(null), subtitleText, 0, stayTime, 0);

            sendPacket(player, titlePacket, stayTime);
            sendPacket(player, subtitlePacket, stayTime);
        } catch (ClassNotFoundException | NoSuchMethodException | NoSuchFieldException | IllegalAccessException |
                 InvocationTargetException | InstantiationException e) {
            Bukkit.getLogger().log(Level.SEVERE, "An error occurred while trying to get the NMS classes for titles.", e);
        }
    }

    // I don't know how any of this works help
    private void sendPacket(Player player, Object packet, int stayTime) {
        int stayTimeSeconds = stayTime * 20;
        try {
            Class<?> craftPlayerClass = Class.forName("org.bukkit.craftbukkit." + Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3] + ".entity.CraftPlayer");
            Object craftPlayer = craftPlayerClass.cast(player);
            Object handle = craftPlayerClass.getMethod("getHandle").invoke(craftPlayer);
            Object playerConnection = handle.getClass().getField("playerConnection").get(handle);

            if (stayTimeSeconds > 0) {
                Class<?> packetClass = packet.getClass();
                Class<?> enumTitleActionClass = Class.forName("net.minecraft.server." + Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3] + ".PacketPlayOutTitle$EnumTitleAction");

                Object[] enumValues = enumTitleActionClass.getEnumConstants();

                Object enumTitleAction = null;
                for (Object enumValue : enumValues) {
                    if (enumValue.toString().equals("TIMES")) {
                        enumTitleAction = enumValue;
                        break;
                    }
                }

                if (enumTitleAction != null) {
                    Constructor<?> packetTimesConstructor = packetClass.getConstructor(int.class, int.class, int.class);
                    Object packetTimes = packetTimesConstructor.newInstance(10, stayTimeSeconds, 20);

                    Field actionField = packetClass.getDeclaredField("a");
                    actionField.setAccessible(true);
                    actionField.set(packetTimes, enumTitleAction);

                    sendPacket(player, packetTimes, 0);
                }
            }

            playerConnection.getClass().getMethod("sendPacket", Class.forName("net.minecraft.server." + Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3] + ".Packet")).invoke(playerConnection, packet);
        } catch (Exception e) {
            Bukkit.getLogger().log(Level.SEVERE, "An error occurred while trying to send a packet to the player " + player.getName() + " with UUID " + player.getUniqueId(), e);
        }
    }
}
