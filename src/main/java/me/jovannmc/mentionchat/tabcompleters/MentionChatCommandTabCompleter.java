package me.jovannmc.mentionchat.tabcompleters;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.Sound;

import java.util.ArrayList;
import java.util.List;

public class MentionChatCommandTabCompleter implements TabCompleter {
    // TODO: add tab completion for new subcommands /mentionchat settings ..
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
        }

        if (args.length == 2) {
            if (args[0].equalsIgnoreCase("help")) {
                // /mentionchat help <1/2/3/4>
                String[] helpPages = {"1", "2", "3", "4"};
                for (String page : helpPages) {
                    if (page.startsWith(args[1])) {
                        completions.add(page);
                    }
                }
            } else if (args[0].equalsIgnoreCase("settings")) {
                // /mentionchat settings <toggle/type/duration/sound>
                String[] settingOptions = {"toggle", "type", "duration", "sound"};
                for (String option : settingOptions) {
                    if (option.startsWith(args[1])) {
                        completions.add(option);
                    }
                }
            }
        }

        if (args.length == 3) {
            if (args[0].equalsIgnoreCase("settings")) {
                if (args[1].equalsIgnoreCase("sound")) {
                    // /mentionchat settings sound <sound>
                    for (Sound sound : Sound.values()) {
                        completions.add(sound.name());
                    }
                } else if (args[1].equalsIgnoreCase("type")) {
                    // /mentionchat settings type <format/message/title/actionbar/bossbar>
                    String[] mentionTypes = {"format", "message", "title", "actionbar", "bossbar"};
                    for (String mentionType : mentionTypes) {
                        if (mentionType.startsWith(args[2])) {
                            completions.add(mentionType);
                        }
                    }
                }
            }
        }

        if (args.length == 4) {
            if (args[1].equalsIgnoreCase("type")) {
                // /mentionchat settings type <format/message/title/actionbar/bossbar> {args}
                if (args[2].equalsIgnoreCase("title")) {
                    String[] titleOptions = {"title", "subtitle"};
                    for (String titleOption : titleOptions) {
                        if (titleOption.startsWith(args[3])) {
                            completions.add(titleOption);
                        }
                    }
                } else if (args[2].equalsIgnoreCase("bossbar")) {
                    String[] bossbarOptions = {"text", "color"};
                    for (String bossbarOption : bossbarOptions) {
                        if (bossbarOption.startsWith(args[3])) {
                            completions.add(bossbarOption);
                        }
                    }
                }
            }
        }
        return completions;
    }
}
