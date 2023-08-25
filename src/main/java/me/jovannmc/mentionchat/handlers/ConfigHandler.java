package me.jovannmc.mentionchat.handlers;

import me.jovannmc.mentionchat.MentionChat;
import me.jovannmc.mentionchat.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.logging.Level;

public class ConfigHandler {
    private JavaPlugin plugin = MentionChat.getPlugin(MentionChat.class);

    private File configFile = new File(plugin.getDataFolder() + File.separator, "config.yml");
    private File dataFile = new File(plugin.getDataFolder() + File.separator, "data.yml");

    private FileConfiguration config = YamlConfiguration.loadConfiguration(configFile);
    private FileConfiguration data = YamlConfiguration.loadConfiguration(dataFile);

    public void configTasks() {
        if (!configFile.exists() && configFile != null) {
            configFile.getParentFile().mkdirs();
            save(plugin.getResource("config.yml"), configFile);
        }

        if (!dataFile.exists() && dataFile != null	) {
            dataFile.getParentFile().mkdir();
            save(plugin.getResource("data.yml"), dataFile);
        }

        // If on a legacy server version and using the default sound, warn the console.
        if (Utils.isLegacyVersion() && getConfig().getString("mentionedSound").equals("ENTITY_ARROW_HIT_PLAYER")) {
            Bukkit.getLogger().log(Level.WARNING,
                    "You are using the default sound (ENTITY_ARROW_HIT_PLAYER), which isn't supported on this legacy version. Please update your config.yml to use a supported sound. An alternative sound (SUCCESSFUL_HIT) will be used instead.");
        }

        // Check config version
        if (getConfig().getInt("configVersion") != 2) {
            Bukkit.getLogger().log(Level.SEVERE,
                    "Your config.yml is outdated. Please regenerate the config.yml and reconfigure MentionChat.");
            Bukkit.getPluginManager().disablePlugin(plugin);
        }
    }

    public FileConfiguration getConfig() {
        return config;
    }

    public FileConfiguration getData() {
        return data;
    }

    public void save(InputStream in, File file) {
        try {
            OutputStream out = new FileOutputStream(file);
            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
            out.close();
            in.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
