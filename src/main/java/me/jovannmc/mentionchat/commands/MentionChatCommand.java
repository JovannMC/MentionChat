package me.jovannmc.mentionchat.commands;

import me.jovannmc.mentionchat.MentionChat;
import me.jovannmc.mentionchat.utils.UpdateChecker;
import me.jovannmc.mentionchat.utils.Utils;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.concurrent.atomic.AtomicBoolean;

public class MentionChatCommand implements CommandExecutor {
    private final JavaPlugin plugin = MentionChat.getPlugin(MentionChat.class);

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length > 1) { Utils.sendMessage(sender, "&cInvalid usage. /mentionchat <help/settings/info/reload>"); return false; }

        if (args.length == 0 || args[0].equalsIgnoreCase("info")) {
            infoSubcommand(sender);
        } else if (args[0].equalsIgnoreCase("reload")) {
            reloadSubcommand(sender);
        } else if (args[0].equalsIgnoreCase("help")){
            helpSubcommand(sender);
        } else if (args[0].equalsIgnoreCase("settings")) {
            settingsSubcommand(sender, args);
        } else {
            Utils.sendMessage(sender, " &cInvalid usage. /mentionchat <help/settings/info/reload>");
        }

        return false;
    }

    /*
        Reload subcommand
    */

    private void reloadSubcommand(CommandSender sender) {
        if (!sender.hasPermission("mentionchat.command.reload")) { Utils.sendMessage(sender, "&cYou don't have permission to use that command."); return; }
        String prefix = plugin.getConfig().getString("prefix");
        plugin.reloadConfig();
        Utils.sendMessage(sender, prefix + " &aReloaded the config.");
    }

    /*
        Help subbcommand
    */

    private void helpSubcommand(CommandSender sender) {
        if (!sender.hasPermission("mentionchat.command.help")) { Utils.sendMessage(sender, "&cYou don't have permission to use that command."); return; }
        String prefix = plugin.getConfig().getString("prefix");
        // Use TextComponents?
    }

    /*
        Settings subcommand
    */

    private void settingsSubcommand(CommandSender sender, String[] args) {
        if (!sender.hasPermission("mentionchat.command.settings")) { Utils.sendMessage(sender, "&cYou don't have permission to use that command."); return; }
        if (args.length == 1) { Utils.sendMessage(sender, "&cInvalid usage. /mentionchat settings <toggle/format/sound>"); }
        String prefix = plugin.getConfig().getString("prefix");

        if ((args.length == 2) && (args[1].equalsIgnoreCase("toggle"))) {
            return;
        }

        if (args[1].equalsIgnoreCase("format")) {
            return;
        } else if (args[1].equalsIgnoreCase("sound")) {
            return;
        }

        Utils.sendMessage(sender, "&cInvalid usage. /mentionchat settings <toggle/format/sound>");
    }

    /*
        Info subcommand (no arguments)
    */

    private void infoSubcommand(CommandSender sender) {
        if (!(sender instanceof Player)) { Utils.sendMessage(sender, "&cYou must be a player to use that command."); return; }
        if (!sender.hasPermission("mentionchat.command.info")) { Utils.sendMessage(sender, "&cYou don't have permission to use that command."); return; }

        String currentVersion = plugin.getDescription().getVersion();
        AtomicBoolean isUpToDate = new AtomicBoolean(true);
        new UpdateChecker(plugin, 111656).getVersion(version -> {
            isUpToDate.set(currentVersion.equals(version));
            sendPluginInfo((Player) sender, currentVersion, isUpToDate.get());
        });
    }

    private void sendPluginInfo(Player player, String currentVersion, boolean isUpToDate) {
        // yes, i manually did the padding to center the text
        // too bad
        String lineSeparator = "\n";
        String separatorLine = "&8&m-----------------------------------------------------";

        String headerPadding = "                     ";
        String descPadding = "         ";
        String authorPadding = "                             ";
        String contactPadding = "                        ";
        String linksPadding = "                             ";

        TextComponent infoMessage = new TextComponent("");

        infoMessage.addExtra(new TextComponent(Utils.color(separatorLine + lineSeparator)));

        TextComponent headerComponent = new TextComponent(Utils.color("&a&lMentionChat &7v" + currentVersion + (isUpToDate ? ChatColor.GREEN + " (Latest)" : ChatColor.RED + " (Outdated)") + lineSeparator));
        headerComponent.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://www.spigotmc.org/resources/mentionchat.111656/"));
        infoMessage.addExtra(new TextComponent(headerPadding));
        infoMessage.addExtra(headerComponent);

        TextComponent descComponent = new TextComponent(ChatColor.GREEN + plugin.getDescription().getDescription() + lineSeparator);
        infoMessage.addExtra(new TextComponent(descPadding));
        infoMessage.addExtra(descComponent);

        TextComponent authorComponent = new TextComponent(ChatColor.GRAY + "Author: " );
        TextComponent authorNameComponent = createClickableMessage(ChatColor.AQUA + "JovannMC" + lineSeparator, "https://femboyfurry.net");
        infoMessage.addExtra(new TextComponent(authorPadding));
        infoMessage.addExtra(authorComponent);
        infoMessage.addExtra(authorNameComponent);

        TextComponent contactComponent = new TextComponent(ChatColor.GRAY + "Contact: ");
        TextComponent discordContactComponent = createClickableMessage(ChatColor.AQUA + "Discord", "https://discord.gg/XdfnKD9QVM");
        TextComponent otherContactComponent = createClickableMessage(ChatColor.AQUA + "Website", "https://femboyfurry.net");

        TextComponent contactGroupComponent = new TextComponent("");
        contactGroupComponent.addExtra(contactComponent);
        contactGroupComponent.addExtra(discordContactComponent);
        contactGroupComponent.addExtra(new TextComponent(ChatColor.GRAY + " - "));
        contactGroupComponent.addExtra(otherContactComponent);

        infoMessage.addExtra(new TextComponent(contactPadding));
        infoMessage.addExtra(contactGroupComponent);

        infoMessage.addExtra(new TextComponent(lineSeparator + lineSeparator));

        TextComponent websiteComponent = createClickableMessage(ChatColor.AQUA + "SpigotMC", "https://www.spigotmc.org/resources/mentionchat.111656/");
        TextComponent repoComponent = createClickableMessage(ChatColor.AQUA + "GitHub", "https://github.com/JovannMC/MentionChat");

        TextComponent linksGroupComponent = new TextComponent("");
        linksGroupComponent.addExtra(websiteComponent);
        linksGroupComponent.addExtra(new TextComponent(ChatColor.GRAY + " - "));
        linksGroupComponent.addExtra(repoComponent);

        infoMessage.addExtra(new TextComponent(linksPadding));
        infoMessage.addExtra(linksGroupComponent);

        infoMessage.addExtra(new TextComponent(Utils.color(lineSeparator + separatorLine)));

        player.spigot().sendMessage(infoMessage);
    }

    private TextComponent createClickableMessage(String text, String url) {
        TextComponent component = new TextComponent(Utils.color(text));
        component.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, url));
        return component;
    }
}