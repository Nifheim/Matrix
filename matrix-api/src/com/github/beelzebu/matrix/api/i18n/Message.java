package com.github.beelzebu.matrix.api.i18n;

public enum Message {

    CHAT_GLOBALMUTE_MUTE("Chat.GlobalMute.Mute", "%prefix% &eThe chat is now muted!"),
    CHAT_GLOBALMUTE_UNMUTE("Chat.GlobalMute.Unmute", "%prefix% &eYou can speak again."),
    CHAT_GLOBALMUTE_NOBYPASS("Chat.GlobalMute.No Bypass", "%prefix% &7You can not speak when the chat is muted."),
    CHAT_NO_CAPS("Chat.No Caps", "%prefix% &cHey! &7avoid the use of caps."),
    CHAT_CHATCLEAR_SILENT("Chat.ChatClear.Silent", "%prefix% &cThe chat was cleared."),
    CHAT_CHATCLEAR_NORMAL("Chat.ChatClear.Normal", "%prefix% &cThe chat was cleared by %player%."),
    CHAT_COMMAND_WATCHER_ENABLED("Chat.Command Watcher.Enabled", "%prefix% &aYou enabled your command watcher."),
    CHAT_COMMAND_WATCHER_DISABLED("Chat.Command Watcher.Disabled", "%prefix% &cYou disabled your command watcher."),
    CHAT_COMMAND_WATCHER_FORMAT("Chat.Command Watcher.Format", "&4[CW]&c %player%: &6%msg%"),
    ITEM_UTILS_ADD_LORE("Item Utils.AddLore.Successful", "%prefix% &7You successful added the lore &r%line% &7to this item."),
    ITEM_UTILS_ADD_LORE_USAGE("Item Utils.AddLore.Help", "%prefix% &7Please use &c\"/alore <message>\"&7."),
    ITEM_UTILS_REMOVE_LORE("Item Utils.RemoveLore.Successful", "%prefix% &7You successful removed the lore &r%line% &7from this item."),
    ITEM_UTILS_REMOVE_NO_LINE("Item Utils.RemoveLore.No Line", "%prefix% &7There is no lore at the specified line."),
    ITEM_UTILS_REMOVE_NO_LORE("Item Utils.RemoveLore.No Lore", "%prefix% &7This item has no lore."),
    ITEM_UTILS_REMOVE_LORE_USAGE("Item Utils.RemoveLore.Help", "%prefix% &7Please use &c\"/rlore <line>\"&7."),
    ITEM_UTILS_RENAME("Item Utils.Rename.Successful", "%prefix% &7The new name of this item is &r%name%&7."),
    ITEM_UTILS_RENAME_USAGE("Item Utils.Rename.Help", "%prefix% &7Please use &c\"/ren <name>\"&7."),
    ITEM_UTILS_NO_ITEM("Item Utils.No Item", "%prefix% &c&lSorry! &7You need an item in your hand."),
    ITEM_UTILS_NO_NUMBER("Item Utils.No Number", "%prefix% &c&lSorry! &6%arg% &7isn''t a number."),
    ESSENTIALS_VANISH_USAGE("Essentials.Vanish.Usage", "%prefix% &7Please use &c\"/vanish (player)\"&7."),
    ESSENTIALS_VANISH_ENABLED("Essentials.Vanish.Enabled", "%prefix% &aYou enabled vanish."),
    ESSENTIALS_VANISH_ENABLED_OTHER("Essentials.Vanish.Enabled Other", "%prefix% &aYou enabled vanish to &f%player%&a."),
    ESSENTIALS_VANISH_DISABLED("Essentials.Vanish.Disabled", "%prefix% &cYou disabled vanish."),
    ESSENTIALS_VANISH_DISABLED_OTHER("Essentials.Vanish.Disabled Other", "%prefix% &cYou disabled vanish to &f%player%&c."),
    ESSENTIALS_TELEPORT_USAGE("Essentials.Teleport.Help", "%prefix% &7Please use &c\"/tp <user>\"&7."),
    ESSENTIALS_TELEPORTING("Essentials.Teleport.Teleporting", "%prefix% &7Teleporting...."),
    ESSENTIALS_FREEZE_USAGE("Essentials.Freeze.Usage", "%prefix% &7Please use &c\"/freeze <player> (message)\"&7."),
    ESSENTIALS_FREEZE_TARGET("Essentials.Freeze.Target", "&7You have been frozen, please wait for staff instructions."),
    ESSENTIALS_UNFREEZE_TARGET("Essentials.Unfreeze.Target", "&7You have been unfrozen, thanks for your patience."),
    ESSENTIALS_FREEZE_TARGET_TITLE("Essentials.Freeze.Title", "&4&lDon't disconnect"),
    ESSENTIALS_FREEZE_TARGET_SUBTITLE("Essentials.Freeze.Subtitle", "&cPlease wait for staff instructions"),
    ESSENTIALS_FREEZE_SENDER("Essentials.Freeze.Sender", "&7Player frozen"),
    ESSENTIALS_UNFREEZE_SENDER("Essentials.Unfreeze.Sender", "&7Player unfrozen"),
    DEATH_TITLES_TITLE("Death Titles.Title", "cYou're dead! :("),
    DEATH_TITLES_SUBTITLE("Death Titles.Subtitle", ""),
    GENERAL_NO_CONSOLE("General.No Console", "%prefix%&c This command can't be executed from the console."),
    GENERAL_NO_TARGET("General.No Target", "%prefix% &a%player%&7 can't be found."),
    GENERAL_NO_PERMS("General.No Perms", "%prefix%&7 You don't have permissions to do that, you must be &c%rank%&7 or higher."),
    MAINTENANCE("Error.Maintenance", "&cCurrently the server is in maintenance, we'll be back soon!"),
    CHANNEL_JOIN("Channel.Join", "&7Now all your messages will be sent to &6%channel%&7."),
    CHANNEL_LEAVE("Channel.Leave", "&7Your chat is back to normal."),
    CHANNEL_MESSAGE_CHANNEL("Channel.Message.Channel", "&f[&6%channel%&f] &f[&a%server_name%&f] "),
    CHANNEL_MESSAGE_PREFIX("Channel.Message.Prefix", "%player_prefix%"),
    CHANNEL_MESSAGE_NAME("Channel.Message.Name", "%player_name%"),
    CHANNEL_MESSAGE_SUFFIX("Channel.Message.Suffix", "%player_suffix%&f: "),
    CHANNEL_MESSAGE_MESSAGE("Channel.Message.Message", "%message%"),
    HELPOP_HELP_USAGE("Helpop.Help.Usage", "&7Please use &6/helpop <message>"),
    HELPOP_COOLDOWN("Helpop.Cooldown", "&7Please wait &6%seconds%&7 before using this command."),
    HELPOP_FORMAT("Helpop.Format", "&4&l[HelpOP] &8[&a&o%server%&8] &c%player_name%&f: &e%message%"),
    PREMIUM_ERROR_LOGGED_OUT("Premium.Error.Logged out", "&7You must be logged in to use this command."),
    PREMIUM_KICK("Premium.Kick", "&7Now we will try to validate your login with Mojang servers, please come back in 10 seconds."),
    PREMIUM_WARNING("Premium.Warning", "&c&lWARNING!", "&7Since this server is &6&lcracked&7, we will need to &c&lerase&7 all your current data to use your mojang information,", "&7this will allow you to join &ewithout using /login command&7 and change your name in minecraft.net without loosing any data on this server."),
    PREMIUM_SUGGESTION("Premium.Suggestion", "&7Hey, if you're joining with a premium account, you can use &6/premium&7 to skip login."),
    MENU_UTIL_CONFIRM_TITLE("Utils.GUI.Confirm.Title", "&8Please confirm"),
    MENU_UTIL_CONFIRM_ACCEPT_NAME("Utils.GUI.Confirm.Accept.Name", "&aAccept"),
    MENU_UTIL_CONFIRM_ACCEPT_LORE("Utils.GUI.Confirm.Accept.Lore", "", "&7Click to accept"),
    MENU_UTIL_CONFIRM_DECLINE_NAME("Utils.GUI.Confirm.Decline.Name", "&cDecline"),
    MENU_UTIL_CONFIRM_DECLINE_LORE("Utils.GUI.Confirm.Decline.Lore", "", "&7Click to decline"),
    MENU_OPTIONS_TITLE("Options.Title", "&6Options GUI"),
    MENU_OPTIONS_SPEED_NAME("Options.Speed.Name", "&6Speed"),
    MENU_OPTIONS_SPEED_LORE("Options.Speed.Lore", "", "&7Click to enable/disable", "&7your speed.", "", "&7&oCurrently: %status%"),
    MENU_OPTIONS_FLY_NAME("Options.Fly.Name", "&6Fly"),
    MENU_OPTIONS_FLY_LORE("Options.Fly.Lore", "", "&7Click to enable/disable", "&7your fly status.", "", "&7&oCurrently: %status%"),
    MENU_OPTIONS_HIDE_NAME("Options.Hide.Name", "&6Hide players"),
    MENU_OPTIONS_HIDE_LORE("Options.Hide.Lore", "", "&7Click to change your", "&7hide players settings"),
    MENU_OPTIONS_STATUS_ENABLED("Options.Status.Enabled", "&aEnabled"),
    MENU_OPTIONS_STATUS_DISABLED("Options.Status.Disabled", "&cDisabled"),
    MENU_SOCIAL_TITLE("Social.Profile.Name", "&cMy Profile"),
    MENU_SOCIAL_PROFILE_NAME("Social.Profile.Name", "&cMy Profile"),
    MENU_SOCIAL_PROFILE_LORE("Social.Profile.Lore", "", " &4ᐅ&7 Rank: &6%vault_group%", " &4ᐅ&7 Clan: &6", " &4ᐅ&7 Level: &6%networklevels_level%", " &4ᐅ&7 Coins: &6%coins_amount%"),
    MENU_SOCIAL_FRIENDS_NAME("Social.Friends.Name", "&aFriends"),
    MENU_SOCIAL_FRIENDS_LORE("Social.Friends.Lore", "", "&7See your friend list and", "&7interact with them."),
    MENU_SOCIAL_PARTY_NAME("Social.Party.Name", "&6Party"),
    MENU_SOCIAL_PARTY_LORE("Social.Party.Lore", "", "&7Create a party to play", "&7with more users."),
    MENU_SOCIAL_CLAN_NAME("Social.Clan.Name", "&cClan"),
    MENU_SOCIAL_CLAN_LORE("Social.Clan.Lore", "", "&7&oComming soon..."),
    LOBBY_ITEMS_SERVER_SELECTOR("Lobby items.Server selector.Name", "&6Game selector &7(Right Click)"),
    LOBBY_ITEMS_OPTIONS("Lobby items.Options.Name", "&6Options &7(Right Click)"),
    LOBBY_ITEMS_PROFILE("Lobby items.Profile.Name", "&cMy Profile &7(Right Click)"),
    COMMAND_SOCIAL_TWITTER("Basic Commands.Twitter", "&8&m----------------------------------------", "&7Nos alegra que te intereses en nuestras redes sociales", "&7puedes visitar nuestro Twitter desde el siguiente enlace:", "", "&6https://twitter.com/NifheimNetwork", "&8&m----------------------------------------"),
    COMMAND_SOCIAL_FACEBOOK("Basic Commands.Facebook", "&8&m----------------------------------------", "&7Nos alegra que te intereses en nuestras redes sociales", "&7puedes visitar nuestro Facebook desde el siguiente enlace:", "", "&6https://www.facebook.com/NifheimNetwork", "&8&m----------------------------------------"),
    COMMAND_SOCIAL_INSTAGRAM("Basic Commands.Instagram", "&8&m----------------------------------------", "&7Nos alegra que te intereses en nuestras redes sociales", "&7puedes visitar nuestro Instagram desde el siguiente enlace:", "", "&6https://www.instagram.com/NifheimNetwork", "&8&m----------------------------------------"),
    COMMAND_SOCIAL_DISCORD("Basic Commands.Discord", "&8&m----------------------------------------", "&7Nos alegra que te intereses en nuestro discord", "&7puedes unirte con el siguiente enlace:", "", "&6https://www.nifheim.net/discord", "", "&7Para obtener un código y verificar tu cuenta usa:", "&e/discord verify", "&8&m----------------------------------------");

    private final String path;
    private String defaults = null;
    private String[] defaultsArray = new String[0];

    Message(String path, String defaults) {
        this.path = path;
        this.defaults = defaults;
    }

    Message(String path, String... defaultsArray) {
        this.path = path;
        this.defaultsArray = defaultsArray;
    }

    protected String getPath() {
        return path;
    }

    protected String getDefault() {
        if (defaults != null) {
            return defaults;
        }
        return String.join("\n", defaultsArray);
    }

    protected String[] getDefaults() {
        if (defaultsArray != null && defaultsArray.length > 0) {
            return defaultsArray;
        }
        return defaults != null ? defaults.split("\\n") : new String[0];
    }
}
