package com.github.beelzebu.matrix.api.commands;

import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import javax.annotation.Nullable;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.plugin.Plugin;

public class CommandAPI {

    private static WeakReference<SimpleCommandMap> simpleCommandMapWeakReference = null;

    private CommandAPI() {
    }

    public static void registerCommand(Plugin plugin, MatrixCommand command) {
        unregisterCommand(plugin, command);
        getCommandMap().register(/*fallback prefix*/plugin.getName(), command);
    }

    public static void unregisterCommand(Plugin plugin, @Nullable MatrixCommand command) {
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

    public static SimpleCommandMap getCommandMap() {
        if (simpleCommandMapWeakReference != null) {
            SimpleCommandMap simpleCommandMap = simpleCommandMapWeakReference.get();
            if (simpleCommandMap != null) {
                return simpleCommandMap;
            }
        }
        try {
            return (simpleCommandMapWeakReference = new WeakReference<>((SimpleCommandMap) getPrivateField(Bukkit.getPluginManager(), "commandMap"))).get();
        } catch (ReflectiveOperationException e) {
            e.printStackTrace();
            return new SimpleCommandMap(Bukkit.getServer());
        }
    }

    public static Map<String, Command> getKnownCommandsMap() {
        try {
            return (Map<String, Command>) getPrivateField(getCommandMap(), "knownCommands", true);
        } catch (ReflectiveOperationException e) {
            e.printStackTrace();
            return new HashMap<>();
        }
    }
}
