package com.github.beelzebu.matrix.api;

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
    DEATH_TITLES_TITLE("Death Titles.Title", "cYou''re dead! :("),
    DEATH_TITLES_SUBTITLE("Death Titles.Subtitle", ""),
    GENERAL_NO_CONSOLE("General.No Console", "%prefix%&c This command can't be executed from the console."),
    GENERAL_NO_TARGET("General.No Target", "%prefix% &a%player%&7 can't be found."),
    GENERAL_NO_PERMS("General.No Perms", "%prefix%&7 You don't have permissions to do that, you must be &c%rank%&7 or higher."),
    MAINTENANCE("Error.Maintenance", "&cCurrently the server is in maintenance, we'll be back soon!");

    private final String path, defaults;

    Message(String path, String defaults) {
        this.path = path;
        this.defaults = defaults;
    }

    public String getPath() {
        return path;
    }

    public String getDefaults() {
        return defaults;
    }
}
