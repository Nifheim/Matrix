package io.github.beelzebu.matrix.api.commands;

import java.lang.reflect.Field;
import java.util.Map;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.plugin.Plugin;

public class CommandAPI {

    public static void registerCommand(Plugin plugin, MatrixCommand cmd) {
        registerCommand(plugin.getName(), cmd);
    }

    @Deprecated
    public static void registerCommand(String plugin, MatrixCommand cmd) {
        unregisterCommand(cmd);
        getCommandMap().register(plugin, cmd);
    }

    public static void unregisterCommand(Command cmd) {
        Map<String, Command> knownCommands = getKnownCommandsMap();
        knownCommands.remove(cmd.getName());
        cmd.getAliases().forEach((alias) -> {
            knownCommands.remove(alias);
        });
    }

    private static Object getPrivateField(Object object, String field) throws SecurityException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
        Class<?> clazz = object.getClass();
        Field objectField = clazz.getDeclaredField(field);
        objectField.setAccessible(true);
        return objectField.get(object);
    }

    private static CommandMap getCommandMap() {
        try {
            return (CommandMap) getPrivateField(Bukkit.getPluginManager(), "commandMap");
        } catch (IllegalAccessException | IllegalArgumentException | NoSuchFieldException | SecurityException ex) {
            return new SimpleCommandMap(Bukkit.getServer());
        }
    }

    private static Map<String, Command> getKnownCommandsMap() {
        return getCommandMap().getKnownCommands();
    }
}
