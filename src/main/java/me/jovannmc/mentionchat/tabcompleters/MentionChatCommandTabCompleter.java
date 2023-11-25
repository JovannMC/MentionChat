package me.jovannmc.mentionchat.tabcompleters;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.Sound;

import java.util.ArrayList;
import java.util.List;

public class MentionChatCommandTabCompleter implements TabCompleter {
    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            // /mentionchat <help/settings/info/reload>
            String[] subCommands = {"help", "settings", "info", "reload"};
            for (String subCommand : subCommands) {
                if (subCommand.startsWith(args[0])) {
                    completions.add(subCommand);
                }
            }
        } else if (args.length == 2 && args[0].equalsIgnoreCase("help")) {
            // /mentionchat help <1/2>
            String[] helpPages = {"1", "2", "3", "4"};
            for (String page : helpPages) {
                if (page.startsWith(args[1])) {
                    completions.add(page);
                }
            }
        } else if (args.length == 2 && args[0].equalsIgnoreCase("settings")) {
            // /mentionchat settings <toggle/type/sound>
            String[] settingOptions = {"toggle", "type", "sound"};
            for (String option : settingOptions) {
                if (option.startsWith(args[1])) {
                    completions.add(option);
                }
            }
        } else if (args.length == 3 && args[0].equalsIgnoreCase("settings")) {
            if (args[1].equalsIgnoreCase("sound")) {
                // /mentionchat settings sound <sound>
                for (Sound sound : Sound.values()) {
                    completions.add(sound.name());
                }
            } else if (args[1].equalsIgnoreCase("type")) {
                // /mentionchat settings type <format/message/title/bossbar>
                String[] mentionTypes = {"format", "message", "title", "bossbar"};
                for (String mentionType : mentionTypes) {
                    if (mentionType.startsWith(args[2])) {
                        completions.add(mentionType);
                    }
                }
            }
        } else if (args.length == 4 && args[1].equalsIgnoreCase("type")) {
            // /mentionchat settings type <format/message/title/bossbar> {args}
            if (args[2].equalsIgnoreCase("title")) {
                String[] titleOptions = {"title", "subtitle", "actionbar"};
                for (String titleOption : titleOptions) {
                    if (titleOption.startsWith(args[3])) {
                        completions.add(titleOption);
                    }
                }
            } else if (args[2].equalsIgnoreCase("bossbar")) {
                String[] bossbarOptions = {"color", "time"};
                for (String bossbarOption : bossbarOptions) {
                    if (bossbarOption.startsWith(args[3])) {
                        completions.add(bossbarOption);
                    }
                }
            }
        }
        return completions;
    }
}
