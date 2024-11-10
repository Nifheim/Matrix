package net.nifheim.matrix.common.util;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;
import org.jetbrains.annotations.NotNull;

public final class ReflectionUtils {

    private ReflectionUtils() {
        throw new RuntimeException("Cannot instantiate ReflectionUtils");
    }

    public static @NotNull Map<String, Field> decodeFields(@NotNull Class<?> clazz) throws ReflectiveOperationException {
        Map<String, Field> fields = new HashMap<>();
        for (Field field : clazz.getDeclaredFields()) {
            if (Modifier.isTransient(field.getModifiers())) {
                continue;
            }
            field.setAccessible(true);
            fields.put(field.getName(), field);
        }
        return fields;
    }

    public static void setField(@NotNull Object object, @NotNull Field field, @NotNull Object value) throws ReflectiveOperationException {
        field.set(object, value);
    }

}
