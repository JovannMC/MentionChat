package me.jovannmc.mentionchat;

import me.jovannmc.mentionchat.commands.MentionChatCommand;
import me.jovannmc.mentionchat.handlers.MentionHandler;
import me.jovannmc.mentionchat.utils.UpdateChecker;
import me.jovannmc.mentionchat.utils.Utils;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.logging.Level;

public class MentionChat extends JavaPlugin implements Listener {
    /*
        Hiya! Welcome to the source code of this plugin.
        Of course any contributions are welcome, just make a pull request or something lol, roast my horrible code.
        -JovannMC
     */

    private File configFile = new File(getDataFolder() + File.separator, "config.yml");
    private File dataFile = new File(getDataFolder() + File.separator, "data.yml");

    private FileConfiguration config = YamlConfiguration.loadConfiguration(configFile);
    private FileConfiguration data = YamlConfiguration.loadConfiguration(dataFile);

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

    public void configTasks() {
        boolean firstRun = false;

        if (!configFile.exists() && configFile != null) {
            configFile.getParentFile().mkdirs();
            saveDefaultConfig();
            firstRun = true;
        }

        if (!dataFile.exists() && dataFile != null	) {
            dataFile.getParentFile().mkdir();
            saveData();
        }

        // If on a legacy server version and using the default sound, warn the console.
        if (Utils.isLegacyVersion() && getConfig().getString("mentionedSound").equals("ENTITY_ARROW_HIT_PLAYER")) {
            Bukkit.getLogger().log(Level.WARNING,
                    "You are using the default sound (ENTITY_ARROW_HIT_PLAYER), which isn't supported on this legacy version. Please update your config.yml to use a supported sound. An alternative sound (SUCCESSFUL_HIT) will be used instead.");
        }

        // Check config version
        if (getConfig().getInt("configVersion") != 2 && !firstRun) {
            Bukkit.getLogger().log(Level.SEVERE,
                    "Your config.yml is outdated. Please regenerate the config.yml and reconfigure MentionChat.");
            Bukkit.getPluginManager().disablePlugin(this);
        }
    }

    public FileConfiguration getData() {
        return data;
    }

    public File getDataFile() {
        return dataFile;
    }

    public void saveData() {
        try {
            data.save(dataFile);
        } catch (Exception e) {
            Bukkit.getLogger().log(Level.SEVERE, "An error occurred while trying to save the data.yml file.", e);
        }
    }
}