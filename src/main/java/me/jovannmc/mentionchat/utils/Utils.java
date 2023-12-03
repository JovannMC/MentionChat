package me.jovannmc.mentionchat.utils;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
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

}
