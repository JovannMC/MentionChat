package me.jovannmc.mentionchat.commands;

import me.jovannmc.mentionchat.MentionChat;
import me.jovannmc.mentionchat.utils.UpdateChecker;
import me.jovannmc.mentionchat.utils.Utils;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.concurrent.atomic.AtomicBoolean;

public class MentionChatCommand implements CommandExecutor {

    MentionChat plugin = MentionChat.getPlugin(MentionChat.class);

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length == 0 || args[0].equalsIgnoreCase("info")) {
            infoSubcommand(sender);
        } else if (args[0].equalsIgnoreCase("reload")) {
            reloadSubcommand(sender);
        } else if (args[0].equalsIgnoreCase("help")){
            helpSubcommand(sender, args);
        } else if (args[0].equalsIgnoreCase("settings")) {
            settingsSubcommand(sender, args);
        } else {
            Utils.sendMessage(sender, "&cInvalid usage. /mentionchat <help/settings/info/reload>");
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
        Help subcommand
    */

    private void helpSubcommand(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            Utils.sendMessage(sender, "&cYou must be a player to use that command.");
            return;
        }
        if (!sender.hasPermission("mentionchat.command.help")) {
            Utils.sendMessage(sender, "&cYou don't have permission to use that command.");
            return;
        }

        TextComponent infoMessage = new TextComponent("");
        final TextComponent hoverText = new TextComponent("Click to enter the command");
        final String lineSeparator = "\n";

        if (args.length == 1 || (args.length == 2 && args[1].equalsIgnoreCase("1"))) {
            String header = Utils.isLegacyVersion() ? "&8-----------&a&lMentionChat General &7(1/3)&8-------------" : "&8-------------&a&lMentionChat General &7(1/3)&8--------------";
            infoMessage.addExtra(Utils.color(header));

            infoMessage.addExtra(lineSeparator + lineSeparator);
            infoMessage.addExtra(createHoverableCommand("&6/mentionchat (info): &rView MentionChat's info and perform an update check", "/mentionchat info", hoverText));
            infoMessage.addExtra(lineSeparator);
            infoMessage.addExtra(createHoverableCommand("&6/mentionchat help (1-3): &rView this help page", "/mentionchat help", hoverText));
            infoMessage.addExtra(lineSeparator);
            infoMessage.addExtra(createHoverableCommand("&6/mentionchat reload: &rReload MentionChat's config", "/mentionchat reload", hoverText));
            infoMessage.addExtra(lineSeparator);
            infoMessage.addExtra(createHoverableCommand("&6/mentionchat settings <toggle/type/sound>: &rChange your MentionChat settings", "/mentionchat settings ", hoverText));
            infoMessage.addExtra(lineSeparator + lineSeparator);

            String footer = Utils.isLegacyVersion() ? "&8--------------------------------------------------" : "&8-----------------------------------------------------";
            infoMessage.addExtra(new TextComponent(Utils.color(footer)));
        } else if (args.length == 2) {
            if (args[1].equalsIgnoreCase("2")) {
                String header = Utils.isLegacyVersion() ? "&8-----------&a&lMentionChat General &7(2/3)&8-------------" : "&8-------------&a&lMentionChat General &7(2/3)&8--------------";
                infoMessage.addExtra(Utils.color(header));

                infoMessage.addExtra(lineSeparator + lineSeparator);
                infoMessage.addExtra(createHoverableCommand("&6..toggle: &rChange if you can be mentioned or not", "/mentionchat settings toggle", hoverText));
                infoMessage.addExtra(lineSeparator);
                infoMessage.addExtra(createHoverableCommand("&6..type <format/message/title/bossbar>:&r Toggle and change settings for mention types", "/mentionchat settings type ", hoverText));
                infoMessage.addExtra(lineSeparator);
                infoMessage.addExtra(createHoverableCommand("&6..sound {sound}: &rChange your mention sound", "/mentionchat settings sound ", hoverText));
                infoMessage.addExtra(lineSeparator + lineSeparator);

                String footer = Utils.isLegacyVersion() ? "&8--------------------------------------------------" : "&8-----------------------------------------------------";
                infoMessage.addExtra(Utils.color(footer));
            } else if (args[1].equalsIgnoreCase("3")) {
                String header = Utils.isLegacyVersion() ? "&8-----------&a&lMentionChat General &7(3/3)&8-------------" : "&8-------------&a&lMentionChat General &7(3/3)&8--------------";
                infoMessage.addExtra(Utils.color(header));

                infoMessage.addExtra(lineSeparator + lineSeparator);
                infoMessage.addExtra(createHoverableCommand("&6..type format {format}: &rChange your mention format", "/mentionchat settings type format ", hoverText));
                infoMessage.addExtra(lineSeparator);
                infoMessage.addExtra(createHoverableCommand("&6..type message {message}: &rChange your mention message", "/mentionchat settings type message  ", hoverText));
                infoMessage.addExtra(lineSeparator);
                infoMessage.addExtra(createHoverableCommand("&6..type title <title/subtitle/actionbar>: &rChange mention title settings", "/mentionchat settings type title ", hoverText));
                infoMessage.addExtra(lineSeparator);
                infoMessage.addExtra(createHoverableCommand("&6..type bossbar <color/time>: &rChange your mention bossbar settings", "/mentionchat settings type bossbar ", hoverText));
                infoMessage.addExtra(lineSeparator + lineSeparator);

                String footer = Utils.isLegacyVersion() ? "&8--------------------------------------------------" : "&8-----------------------------------------------------";
                infoMessage.addExtra(Utils.color(footer));
            } else {
                Utils.sendMessage(sender, "&cInvalid usage. /mentionchat help (1-3)");
                return;
            }
        } else {
            Utils.sendMessage(sender, "&cInvalid usage. /mentionchat help (page)");
            return;
        }

        Player player = (Player) sender;
        player.spigot().sendMessage(infoMessage);
    }

    /*
        Settings subcommand
    */

    private void settingsSubcommand(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) { Utils.sendMessage(sender, "&cYou must be a player to use that command."); return; }
        if (!sender.hasPermission("mentionchat.command.settings")) { Utils.sendMessage(sender, "&cYou don't have permission to use that command."); return; }
        if (args.length == 1) { Utils.sendMessage(sender, "&cInvalid usage. /mentionchat settings <toggle/type/sound>"); return; }
        Player player = (Player) sender;
        String prefix = plugin.getConfig().getString("prefix");

        if ((args.length == 2) && (args[1].equalsIgnoreCase("toggle"))) {
            if (plugin.getData().contains(player.getUniqueId().toString() + ".toggle")) {
                plugin.getData().set(player.getUniqueId().toString() + ".toggle", !plugin.getData().getBoolean(player.getUniqueId().toString() + ".toggle"));
            } else {
                // Player hasn't toggled mentions yet. By default mentions are on, so set it to false
                plugin.getData().set(player.getUniqueId().toString() + ".toggle", false);
            }

            plugin.saveData();
            Utils.sendMessage(sender, prefix + " &aToggled mentions " + (plugin.getData().getBoolean(player.getUniqueId().toString() + ".toggle") ? "on" : "off") + ".");
            return;
        } else if (args[1].equalsIgnoreCase("sound")) {
            if (args.length != 3) {
                Utils.sendMessage(sender, "&cInvalid usage. /mentionchat settings sound <sound>");
                Utils.sendMessage(sender, "&cDefault sound: &7" + plugin.getConfig().getString("mentionedSound"));
                return;
            }

            try {
                Sound.valueOf(args[2].toUpperCase());
            } catch (IllegalArgumentException e) {
                Utils.sendMessage(sender, "&cInvalid sound. /mentionchat settings sound <sound>");
                Utils.sendMessage(sender, "&cDefault sound: &7" + plugin.getConfig().getString("mentionedSound"));
                return;
            }

            plugin.getData().set(player.getUniqueId().toString() + ".sound", args[2]);
            plugin.saveData();

            Utils.sendMessage(sender, prefix + " &aSet your mention sound to: " + args[2]);
            return;
        } else if (args[1].equalsIgnoreCase("type")) {
            typeSettingsSubcommand(sender, args);
            return;
        }

        Utils.sendMessage(sender, "&cInvalid usage. /mentionchat settings <toggle/type/sound>");
    }

    /*
        Type settings subcommand (/mentionchat settings type)
    */

    private void typeSettingsSubcommand(CommandSender sender, String[] args) {
        if (args.length == 2) {
            Utils.sendMessage(sender, "&cInvalid usage. /mentionchat settings type <format/message/title/bossbar>");
            return;
        }
        Player player = (Player) sender;
        String prefix = plugin.getConfig().getString("prefix");

        if (args[2].equalsIgnoreCase("format")) {
            if (args.length == 3) {
                Utils.sendMessage(sender, "&cInvalid usage. /mentionchat settings type format <format>");
                sender.sendMessage(ChatColor.RED + "Default format: " + ChatColor.RESET + plugin.getConfig().get("mentionFormat"));
                return;
            }

            String format = Utils.buildString(args, 3);
            Utils.sendMessage(sender, prefix + " &aSet your mention format to: &r" + format.replace("%mention%", player.getName()));
            plugin.getData().set(player.getUniqueId().toString() + ".format", format);
            plugin.saveData();
        } else if (args[2].equalsIgnoreCase("message")) {
            if (args.length == 3) {
                Utils.sendMessage(sender, "&cInvalid usage. /mentionchat settings type message <message>");
                sender.sendMessage(ChatColor.RED + "Default message: " + ChatColor.RESET + plugin.getConfig().get("mentionedMessage"));
                return;
            }

            String message = Utils.buildString(args, 3);
            Utils.sendMessage(sender, prefix + " &aSet your mention message to: &r" + message.replace("%player%", player.getName()));
            plugin.getData().set(player.getUniqueId().toString() + ".message", message);
            plugin.saveData();
        } else if (args[2].equalsIgnoreCase("title")) {
            if (args.length == 3) {
                Utils.sendMessage(sender, "&cInvalid usage. /mentionchat settings type title <title/subtitle/actionbar>");
                sender.sendMessage(ChatColor.RED + "Default title: " + ChatColor.RESET + plugin.getConfig().get("mentionedTitle"));
                sender.sendMessage(ChatColor.RED + "Default subtitle: " + ChatColor.RESET + plugin.getConfig().get("mentionedSubtitle"));
                sender.sendMessage(ChatColor.RED + "Default actionbar: " + ChatColor.RESET + plugin.getConfig().get("mentionedActionbar"));
                return;
            }

            if (args[3].equalsIgnoreCase("title")) {
                if (args.length == 4) {
                    Utils.sendMessage(sender, "&cInvalid usage. /mentionchat settings type title title <title>");
                    sender.sendMessage(ChatColor.RED + "Default title: " + ChatColor.RESET + plugin.getConfig().get("mentionedTitle"));
                    return;
                }

                String title = Utils.buildString(args, 4);
                Utils.sendMessage(sender, prefix + " &aSet your mention title to: &r" + title.replace("%player%", player.getName()));
                plugin.getData().set(player.getUniqueId().toString() + ".title", title);
                plugin.saveData();
            } else if (args[3].equalsIgnoreCase("subtitle")) {
                if (args.length == 4) {
                    Utils.sendMessage(sender, "&cInvalid usage. /mentionchat settings type title subtitle <subtitle>");
                    sender.sendMessage(ChatColor.RED + "Default subtitle: " + ChatColor.RESET + plugin.getConfig().get("mentionedSubtitle"));
                    return;
                }

                String subtitle = Utils.buildString(args, 4);
                Utils.sendMessage(sender, prefix + " &aSet your mention subtitle to: &r" + subtitle.replace("%player%", player.getName()));
                plugin.getData().set(player.getUniqueId().toString() + ".subtitle", subtitle);
                plugin.saveData();
            } else if (args[3].equalsIgnoreCase("actionbar")) {
                if (args.length == 4) {
                    Utils.sendMessage(sender, "&cInvalid usage. /mentionchat settings type title actionbar <actionbar>");
                    sender.sendMessage(ChatColor.RED + "Default actionbar: " + ChatColor.RESET + plugin.getConfig().get("mentionedActionbar"));
                    return;
                }

                String actionbar = Utils.buildString(args, 4);
                Utils.sendMessage(sender, prefix + " &aSet your mention actionbar to: &r" + actionbar.replace("%player%", player.getName()));

            } else {
                Utils.sendMessage(sender, "&cInvalid usage. /mentionchat settings type <format/message/title/bossbar>");
            }
        }
    }

    /*
        Info subcommand (/mentionchat OR /mentionchat info)
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
        String separatorLine = "&8-----------------------------------------------------";

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

    private TextComponent createHoverableCommand(String text, String command, TextComponent hoverText) {
        TextComponent component = new TextComponent(Utils.color(text));
        component.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponent[]{hoverText}));
        component.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, command));
        return component;
    }
}