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
        String[] legacyVersions = {"v1_8", "v1_7", "v1_6", "v1_5", "v1_4", "v1_3", "v1_2", "v1_1", "v1_0"};
        for (String legacyVersion : legacyVersions) {
            if (getServerVersion().startsWith(legacyVersion)) {
                return true;
            }
        }
        return false;
    }
}
