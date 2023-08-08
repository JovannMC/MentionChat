# MentionChat

A lightweight Spigot plugin that allows players to mention other players in chat.<br><br>
![MentionChat in action](showcase.png)

## Description

This is a simple and open source plugin that allows players to mention other players in chat, which is in basically every chat platform. You can configure the messages, mention sound, and cooldown time.


<details>
    <summary>config.yml</summary>

    # MentionChat config.yml
    # Thanks for downloading my plugin!
    
    # Prefix for MentionChat commands.
    prefix: "&6[&eMentionChat&6]&r"
    
    # Should the plugin check for updates on startup?
    # You can always check for updates manually with /mentionchat.
    checkForUpdates: true
    
    # What should the plugin do when you mention someone?
    # Options:
    # MESSAGE - Send a message to the mentioned player
    # FORMAT - Format the message to highlight the mentioned player
    mentionType: "FORMAT"
    
    # If you chose FORMAT, what should the format for the message be?
    # %mention% is the placeholder of the mentioned player or everyone.
    mentionFormat: "&6&l%mention%&r"
    
    # If you chose MESSAGE, what message should be sent?
    # %player% is the placeholder of the player's name
    mentionedMessage: "&6You were mentioned by &e%player%"
    
    # When you are mentioned, play this sound. No sound will be played when someone is mentioned and an error will appear if an invalid sound is chosen.
    # To disable the sound, put NONE.
    # IMPORTANT!
    # If you are on 1.8.x, this sound will NOT work, and SUCCESSFUL_HIT will be played instead.
    # Please choose a sound from here: https://helpch.at/docs/1.8/index.html?org/bukkit/Sound.html
    mentionedSound: ENTITY_ARROW_HIT_PLAYER
    
    # How much time (in seconds) should there be in between mentions?
    # Used to prevent mention spam.
    # To disable the cooldown, put 0.
    cooldown: 3
    
    # What should you see when you try to mention people during the cooldown?
    cooldownMessage: "&4Please don't try to spam mention people."
    
    # When you don't have permission to mention someone, send this message:
    noPermissionMessage: "&4You don't have permission to mention them!"
    
    # DO NOT TOUCH THIS
    configVersion: 2
</details>

## Commands

- `/mentionchat` - View MentionChat's info and perform an update check.
- `/mentionchat reload` - Reload MentionChat's config.

## Permissions

- `mentionchat.*` - Gives access to all of MentionChat's permissions. Default is `OP`.
- `mentionchat.command.info` - Allows players to view MentionChat's info and perform an update check. Default is `everyone`.
- `mentionchat.command.reload` - Allows players to reload MentionChat's config. Default is `OP`.
- `mentionchat.mention.others` - Allows players to mention other players in chat. Default is `everyone`.
- `mentionchat.mention.everyone` - Allows players to mention everyone in chat. Default is `OP`.
- `mentionchat.mention.bypass` - Allows players to bypass the cooldown between mentions. Default is `OP`.

## License

This project is licensed under the [MIT](https://opensource.org/license/mit/) License - see the [LICENSE.md](LICENSE.md) file for details.

![bStats Metrics](https://bstats.org/signatures/bukkit/mentionchat.svg)
