package me.jovannmc.mentionchat;

import me.jovannmc.mentionchat.commands.MentionChatCommand;
import me.jovannmc.mentionchat.utils.UpdateChecker;
import me.jovannmc.mentionchat.utils.Utils;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.logging.Level;

public final class MentionChat extends JavaPlugin implements Listener {
    /*
        Hiya! Welcome to the source code of this plugin.
        Of course any contributions are welcome, just make a pull request or something lol, roast my horrible code.
        -JovannMC
     */

    public void onEnable() {
        configTasks();

        Bukkit.getPluginCommand("mentionchat").setExecutor(new MentionChatCommand());
        Bukkit.getPluginManager().registerEvents(new MentionHandler(), this);
        new Metrics(this, 19327);

        Bukkit.getLogger().log(Level.INFO, "MentionChat v" + getDescription().getVersion() + " has been enabled! Server version: " + Utils.getServerVersion());

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
    }

    private void configTasks() {
        File configFile = new File(getDataFolder(), "config.yml");
        if (!configFile.exists()) {
            saveResource("config.yml", false);

            // If on a legacy server version and using the default sound, warn the console.
            if (Utils.isLegacyVersion() && getConfig().getString("mentionedSound").equals("ENTITY_ARROW_HIT_PLAYER")) {
                Bukkit.getLogger().log(Level.WARNING,
                        "You are using the default sound (ENTITY_ARROW_HIT_PLAYER), which isn't supported on this legacy version. Please update your config.yml to use a supported sound. An alternative sound (SUCCESSFUL_HIT) will be used instead.");
            }
        }

        // Check config version
        if (getConfig().getInt("configVersion") != 2) {
            Bukkit.getLogger().log(Level.SEVERE,
                    "Your config.yml is outdated. Please regenerate the config.yml and reconfigure MentionChat.");
            Bukkit.getPluginManager().disablePlugin(this);
        }
    }
}