package me.jovannmc.mentionchat.utils;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.logging.Level;

public class Utils {
    public static String color(String string) { return ChatColor.translateAlternateColorCodes('&', string); }

    public static void sendMessage(CommandSender sender,  String message) { sender.sendMessage(color(message)); }

    public static String getServerVersion() {
        String packageName = Bukkit.getServer().getClass().getPackage().getName();
        return packageName.substring(packageName.lastIndexOf('.') + 1);
    }

    public static boolean isLegacyVersion() {
        String[] legacyVersions = {"v1_8", "v1_7", "v1_6"};
        for (String legacyVersion : legacyVersions) {
            if (getServerVersion().startsWith(legacyVersion)) {
                return true;
            }
        }
        return false;
    }

    public static String buildString(String[] args, int start) {
        StringBuilder formatBuilder = new StringBuilder();
        for (int i = start; i < args.length; i++) {
            formatBuilder.append(args[i]);
            if (i < args.length - 1) {
                formatBuilder.append(" ");
            }
        }
        return formatBuilder.toString();
    }

    public static void sendTitle(Player player, String title, String subtitle, int stayTime) {
        String[] legacyVersions = {"1_11", "1_10", "1_11", "v1_8", "v1_7", "v1_6"};
        for (String legacyVersion : legacyVersions) {
            if (getServerVersion().startsWith(legacyVersion)) {
                sendTitleLegacy(player, title, subtitle, stayTime);
                return;
            }
        }

        player.sendTitle(color(title), color(subtitle), 10, stayTime, 20);
    }

    private static void sendTitleLegacy(Player player, String title, String subtitle, int stayTime) {
        try {
            String nmsPackage = "net.minecraft.server." + Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
            Class<?> mainTitlePacket = Class.forName(nmsPackage + ".PacketPlayOutTitle");
            Class<?> chatSerializer = Class.forName(nmsPackage + ".IChatBaseComponent").getDeclaredClasses()[0];

            Object titleText = chatSerializer.getMethod("a", String.class).invoke(null, "{\"text\":\"" + title.replace("&", "§") + "\"}");
            Object subtitleText = chatSerializer.getMethod("a", String.class).invoke(null, "{\"text\":\"" + subtitle.replace("&", "§") + "\"}");

            // Use EnumTitleAction.TITLE and EnumTitleAction.SUBTITLE directly
            Object titlePacket = mainTitlePacket
                    .getConstructor(mainTitlePacket.getDeclaredClasses()[0], Class.forName(nmsPackage + ".IChatBaseComponent"), int.class, int.class, int.class)
                    .newInstance(mainTitlePacket.getDeclaredClasses()[0].getField("TITLE").get(null), titleText, 0, stayTime, 0);

            Object subtitlePacket = mainTitlePacket
                    .getConstructor(mainTitlePacket.getDeclaredClasses()[0], Class.forName(nmsPackage + ".IChatBaseComponent"), int.class, int.class, int.class)
                    .newInstance(mainTitlePacket.getDeclaredClasses()[0].getField("SUBTITLE").get(null), subtitleText, 0, stayTime, 0);

            sendPacket(player, titlePacket, stayTime);
            sendPacket(player, subtitlePacket, stayTime);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    // Send packet without stayTime
    private static void sendPacket(Player player, Object packet) {
        try {
            Class<?> craftPlayerClass = Class.forName("org.bukkit.craftbukkit." + Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3] + ".entity.CraftPlayer");
            Object craftPlayer = craftPlayerClass.cast(player);
            Object handle = craftPlayerClass.getMethod("getHandle").invoke(craftPlayer);
            Object playerConnection = handle.getClass().getField("playerConnection").get(handle);

            playerConnection.getClass().getMethod("sendPacket", Class.forName("net.minecraft.server." + Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3] + ".Packet")).invoke(playerConnection, packet);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Send packet with stayTime
    // also i dont know how any of this works help
    private static void sendPacket(Player player, Object packet, int stayTime) {
        int stayTimeSeconds = stayTime * 20;
        try {
            Class<?> craftPlayerClass = Class.forName("org.bukkit.craftbukkit." + Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3] + ".entity.CraftPlayer");
            Object craftPlayer = craftPlayerClass.cast(player);
            Object handle = craftPlayerClass.getMethod("getHandle").invoke(craftPlayer);
            Object playerConnection = handle.getClass().getField("playerConnection").get(handle);

            if (stayTimeSeconds > 0) {
                Class<?> packetClass = packet.getClass();
                Class<?> enumTitleActionClass = Class.forName("net.minecraft.server." + Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3] + ".PacketPlayOutTitle$EnumTitleAction");

                // Get the enum values directly
                Object[] enumValues = enumTitleActionClass.getEnumConstants();

                // Find the EnumTitleAction with the name "TIMES"
                Object enumTitleAction = null;
                for (Object enumValue : enumValues) {
                    if (enumValue.toString().equals("TIMES")) {
                        enumTitleAction = enumValue;
                        break;
                    }
                }

                // If EnumTitleAction is found, proceed
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
