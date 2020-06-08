package com.github.beelzebu.matrix.command.staff;

import com.github.beelzebu.matrix.api.command.MatrixCommand;
import com.github.beelzebu.matrix.api.util.StringUtils;
import java.util.stream.Stream;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * @author Beelzebu
 */
public class PluginsCommand extends MatrixCommand {

    public PluginsCommand() {
        super("plugins", null, "pl", "bukkit:plugins", "bukkit:pl");
    }

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        if (sender instanceof Player) {
            sender.sendMessage(StringUtils.replace("Plugins (12): &aCoins&f, &aDamageIndicator&f, &bEssentialsXL&f, &aFactionsUtils&f, &aLoncoUtils&f, &aLuckPerms&f, &bMatrix-Bukkit&f, &bMatrix-Chat&f, &aSuperNametags&f, &aPlaceholderAPI&f, &aProtocolLib&f, &aViaVersion"));
        } else {
            StringBuilder plugins = new StringBuilder();
            Stream.of(Bukkit.getPluginManager().getPlugins()).map(plugin -> (plugin.isEnabled() ? "&a" : "&c") + plugin.getName() + "&f, ").forEach(plugins::append);
            sender.sendMessage(StringUtils.replace("Plugins (" + Bukkit.getPluginManager().getPlugins().length + "): " + plugins.substring(0, plugins.length() - 2)));
        }
    }
}
