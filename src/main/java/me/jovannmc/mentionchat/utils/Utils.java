package me.jovannmc.mentionchat.utils;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

public class Utils {
    public static String color(String string) { return ChatColor.translateAlternateColorCodes('&', string); }

    public static void sendMessage(CommandSender sender,  String message) { sender.sendMessage(color(message)); }

    public static String getServerVersion() {
        String packageName = Bukkit.getServer().getClass().getPackage().getName();
        return packageName.substring(packageName.lastIndexOf('.') + 1);
    }

    public static boolean isLegacyVersion() {
        String[] legacyVersions = {"v1_11", "v1_10", "v1_9"};
        for (String legacyVersion : legacyVersions) {
            if (getServerVersion().startsWith(legacyVersion)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isUnsupportedVersion() {
        String[] unsupportedVersions = {"v1_8", "v1_7", "v1_6", "v1_5", "v1_4", "v1_3"};
        for (String unsupportedVersion : unsupportedVersions) {
            if (getServerVersion().startsWith(unsupportedVersion)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isOldVersion() {
        String[] oldVersions = {"v1_11", "v1_10", "v1_9", "v1_8", "v1_7", "v1_6", "v1_5", "v1_4", "v1_3"};
        for (String oldVersion : oldVersions) {
            if (getServerVersion().startsWith(oldVersion)) {
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

}
