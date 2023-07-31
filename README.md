# MentionChat

A lightweight Spigot plugin that allows players to mention other players in chat.

## Description

This is a simple and open source plugin that allows players to mention other players in chat, which is in basically every chat platform. You can configure the messages, mention sound, and cooldown time.
<details>
    <summary>config.yml</summary>

    # Thanks for downloading my plugin.
    
    # When you are mentioned, play this sound. Putting an invalid sound will result in no sound played when someone is mentioned, and an error will appear in the console.
    # IMPORTANT!
    # If you are on 1.8.x, this sound will NOT work, and SUCCESSFUL_HIT will be played instead.
    # Please choose a sound from here:
    # https://helpch.at/docs/1.8/index.html?org/bukkit/Sound.html
    mentionedSound: ENTITY_ARROW_HIT_PLAYER
    
    # When you are mentioned, send this message.
    # %player% is the placeholder of the player's name
    # You are allowed to use color codes such as &4
    mentionedMessage: "&6You were mentioned by &e%player%"
    
    # When you don't have permission to mention someone, send this message:
    # You are allowed to use color codes such as &4
    noPermissionMessage: "&4You don't have permission to mention them!"
    
    # How much time (in seconds) should there be in between mentions?
    # Used to prevent mention spam.
    cooldown: 3
    
    # What should you see when you try to mention people during the cooldown?
    # You are allowed to use color codes such as &4
    cooldownMessage: "&4Please don't try to spam mention people."
    
    # DO NOT TOUCH THIS
    configVersion: 1
</details>

## Permissions

- `mentionchat.*` - Gives access to all of MentionChat's permissions. Default is `OP`.
- `mentionchat.mention.others` - Allows players to mention other players in chat. Default is `everyone`.
- `mentionchat.mention.everyone` - Allows players to mention everyone in chat. Default is `OP`.
- `mentionchat.mention.bypass` - Allows players to bypass the cooldown between mentions. Default is `OP`.

## License

This project is licensed under the [MIT](https://opensource.org/license/mit/) License - see the [LICENSE.md](LICENSE.md) file for details.

![bStats Metrics](https://bstats.org/signatures/bukkit/mentionchat.svg)