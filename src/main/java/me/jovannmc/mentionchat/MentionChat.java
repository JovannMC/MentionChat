package me.jovannmc.mentionchat;

import me.jovannmc.mentionchat.commands.MentionChatCommand;
import me.jovannmc.mentionchat.handlers.MentionHandler;
import me.jovannmc.mentionchat.tabcompleters.MentionChatCommandTabCompleter;
import me.jovannmc.mentionchat.utils.UpdateChecker;
import me.jovannmc.mentionchat.utils.Utils;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.logging.Level;

public class MentionChat extends JavaPlugin implements Listener {
    /*
        Hiya! Welcome to the source code of this plugin.
        Of course any contributions are welcome, just make a pull request or something lol, roast my horrible code.
        -JovannMC
     */

    private final File configFile = new File(getDataFolder() + File.separator, "config.yml");
    private final File dataFile = new File(getDataFolder() + File.separator, "data.yml");

    private final FileConfiguration data = YamlConfiguration.loadConfiguration(dataFile);

    public void onEnable() {
        configTasks();

        Bukkit.getPluginCommand("mentionchat").setExecutor(new MentionChatCommand());
        Bukkit.getPluginCommand("mentionchat").setTabCompleter(new MentionChatCommandTabCompleter());
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
        if (!configFile.exists()) {
            configFile.getParentFile().mkdirs();
            if (Utils.isLegacyVersion()) {
                // Save config-legacy.yml
                saveResource("config-legacy.yml", false);
                // Copy contents from config-legacy.yml to config.yml
                try {
                    Files.copy(new File(getDataFolder(), "config-legacy.yml").toPath(), new File(getDataFolder(), "config.yml").toPath(), StandardCopyOption.REPLACE_EXISTING);

                    // Delete the config-legacy.yml file
                    File legacyConfigFile = new File(getDataFolder(), "config-legacy.yml");
                    if (legacyConfigFile.exists()) {
                        legacyConfigFile.delete();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                // Save config.yml
                saveResource("config.yml", false);
            }
            firstRun = true;
        }

        if (!dataFile.exists()) {
            dataFile.getParentFile().mkdir();
            saveData();
        }

        // Check config version
        if (getConfig().getInt("configVersion") != 4 && !firstRun) {
            Bukkit.getLogger().log(Level.SEVERE, "Your config.yml is outdated. Please regenerate the config.yml and reconfigure MentionChat.");
            Bukkit.getPluginManager().disablePlugin(this);
        }
    }

    public void playMentionSound(Player mentioned) {
        boolean customSound = false;
        try {
            String mentionedSound;

            if (getData().contains(mentioned.getUniqueId().toString() + ".sound")) {
                mentionedSound = getData().getString(mentioned.getUniqueId().toString() + ".sound");
                customSound = true;
            } else {
                mentionedSound = getConfig().getString("mentionedSound");
            }

            Sound soundEnum;

            if (Utils.isLegacyVersion() && mentionedSound.equalsIgnoreCase("ENTITY_ARROW_HIT_PLAYER")) {
                // On legacy version and is using default sound which isn't supported
                soundEnum = Sound.valueOf("SUCCESSFUL_HIT");
            } else {
                soundEnum = Sound.valueOf(mentionedSound);
            }

            if (mentionedSound != null && !mentionedSound.equalsIgnoreCase("NONE")) {
                mentioned.playSound(mentioned.getLocation(), soundEnum, 1.0f, 1.0f);
            }
        } catch (Exception e) {
            if (customSound) {
                Bukkit.getLogger().log(Level.SEVERE, "An error occurred while trying to play the sound (" + getData().getString(mentioned.getUniqueId().toString() + ".sound") + ") for the player " + mentioned.getName() + " with UUID " + mentioned.getUniqueId(), e);
                Utils.sendMessage(mentioned, "&cAn error occurred while trying to play your custom sound (" + getData().getString(mentioned.getUniqueId().toString() + ".sound") + "). Please contact an administrator.");
            } else {
                Bukkit.getLogger().log(Level.SEVERE, "An error occurred while trying to play the config sound (" + getConfig().getString("mentionedSound") + ").");
                Bukkit.getLogger().log(Level.SEVERE, "The sound set in the config may be invalid.", e);
                Utils.sendMessage(mentioned, "&cAn error occurred while trying to play the mention sound. (" + getConfig().getString("mentionedSound") + "). Please contact an administrator.");
            }
        }
    }

    public FileConfiguration getData() {
        return data;
    }

    public void saveData() {
        try {
            data.save(dataFile);
        } catch (Exception e) {
            Bukkit.getLogger().log(Level.SEVERE, "An error occurred while trying to save the data.yml file.", e);
        }
    }
}