package com.github.beelzebu.matrix.api.command;

import com.github.beelzebu.matrix.util.CompatUtil;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.plugin.Plugin;

/**
 * @author Beelzebu
 */
public class CommandAPI {

    private static SimpleCommandMap commandMap;

    private CommandAPI() {
    }

    public static void registerCommand(Plugin plugin, MatrixCommand command) {
        unregisterCommand(plugin, command);
        getCommandMap().register(/*fallback prefix*/plugin.getName(), command);
    }

    public static void unregisterCommand(Plugin plugin, MatrixCommand command) {
        if (command == null) {
            return;
        }
        command.unregister(getCommandMap());
        Map<String, Command> knownCommands = getKnownCommandsMap();
        knownCommands.remove(plugin.getName() + ":" + command.getName().toLowerCase(Locale.ENGLISH).trim());
        command.getAliases().forEach(knownCommands::remove);
        command.setLabel(command.getName());
    }

    private static Object getPrivateField(Object object, String field) throws ReflectiveOperationException {
        return getPrivateField(object, field, false);
    }

    private static Object getPrivateField(Object object, String field, boolean superClass) throws ReflectiveOperationException {
        Class<?> clazz = superClass ? object.getClass().getSuperclass() : object.getClass();
        Field objectField = clazz.getDeclaredField(field);
        objectField.setAccessible(true);
        return objectField.get(object);
    }

    private static SimpleCommandMap getCommandMap() {
        if (commandMap != null) {
            return commandMap;
        }
        try {
            return (commandMap = (SimpleCommandMap) getPrivateField(Bukkit.getPluginManager(), "commandMap"));
        } catch (ReflectiveOperationException e) {
            e.printStackTrace();
            return new SimpleCommandMap(Bukkit.getServer());
        }
    }


    @SuppressWarnings("unchecked")
    private static Map<String, Command> getKnownCommandsMap() {
        try {
            return (Map<String, Command>) getPrivateField(getCommandMap(), "knownCommands", CompatUtil.VERSION.isAfterOrEq(CompatUtil.MinecraftVersion.MINECRAFT_1_13));
        } catch (ReflectiveOperationException e) {
            e.printStackTrace();
            return new HashMap<>();
        }
    }
}
