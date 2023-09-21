package me.jovannmc.mentionchat.utils;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Optional;
import java.util.Scanner;
import java.util.function.Consumer;

public class UpdateChecker {
    private final JavaPlugin plugin;
    private final int resourceId;

    public UpdateChecker(JavaPlugin plugin, int resourceId) {
        this.plugin = plugin;
        this.resourceId = resourceId;
    }

    public void getVersion(final Consumer<Optional<String>> consumer) {
        Bukkit.getScheduler().runTaskAsynchronously(this.plugin, () -> {
            try (InputStream is = new URL("https://api.spigotmc.org/legacy/update.php?resource=" + this.resourceId).openStream(); Scanner scan = new Scanner(is)) {
                if (scan.hasNext()) {
                    consumer.accept(Optional.of(scan.next()));
                } else {
                    consumer.accept(Optional.empty()); // No version information found
                }
            } catch (IOException e) {
                consumer.accept(Optional.empty()); // Error occurred while checking for updates
                plugin.getLogger().info("Unable to check for updates: " + e.getMessage());
            }
        });
    }
}
