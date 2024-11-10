package net.nifheim.matrix.common.config;


import java.util.function.Function;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * This is a wrapper for the file configuration.
 * It is used to provide a common interface for the different file configuration implementations.
 *
 * @author Jaime Suárez
 */
public interface FileConfigurationWrapper {

    /**
     * Get a string from the configuration.
     *
     * @param path The path to the string.
     * @return The string or null if it does not exist.
     */
    @Nullable
    default String getString(String path) {
        return getString(path, null);
    }

    /**
     * Get a string from the configuration.
     *
     * @param path The path to the string.
     * @param def  The default value.
     * @return The string or the default value if it does not exist.
     */
    @NotNull
    default String getString(String path, String def) {
        return get(path, String.class, def);
    }

    /**
     * Get an integer from the configuration.
     *
     * @param path The path to the integer.
     * @return The integer or 0 if it does not exist.
     */
    default int getInt(String path) {
        return getInt(path, 0);
    }

    /**
     * Get an integer from the configuration.
     *
     * @param path The path to the integer.
     * @param def  The default value.
     * @return The integer or the default value if it does not exist.
     */
    default int getInt(String path, int def) {
        Integer value = get(path, Integer.class, def);
        return value != null ? value : def;
    }

    /**
     * Get a double from the configuration.
     *
     * @param path The path to the double.
     * @return The double or 0 if it does not exist.
     */
    default double getDouble(String path) {
        return getDouble(path, 0);
    }

    /**
     * Get a double from the configuration.
     *
     * @param path The path to the double.
     * @param def  The default value.
     * @return The double or the default value if it does not exist.
     */
    default double getDouble(String path, double def) {
        Double value = get(path, Double.class, def);
        return value != null ? value : def;
    }

    /**
     * Get a boolean from the configuration.
     *
     * @param path The path to the boolean.
     * @return The boolean or false if it does not exist.
     */
    default boolean getBoolean(String path) {
        return getBoolean(path, false);
    }

    /**
     * Get a boolean from the configuration.
     *
     * @param path The path to the boolean.
     * @param def  The default value.
     * @return The boolean or the default value if it does not exist.
     */
    default boolean getBoolean(String path, boolean def) {
        Boolean value = get(path, Boolean.class, def);
        return value != null ? value : def;
    }

    /**
     * Get an object from the configuration.
     *
     * @param path The path to the object.
     * @param type The type of the object.
     * @return The object or null if it does not exist.
     */
    @Nullable <T> T get(String path, Class<T> type);

    /**
     * Get an object from the configuration.
     *
     * @param path The path to the object.
     * @param type The type of the object.
     * @param def  The default value.
     * @return The object or the default value if it does not exist.
     */
    @Nullable <T> T get(String path, Class<T> type, T def);


    /**
     * FileConfigurationWrapper implementation for reading values from a file.
     */
    class FileConfigurationWrapperImpl implements FileConfigurationWrapper {

        private final Function<String, Object> fn;

        private FileConfigurationWrapperImpl(Function<String, Object> fn) {
            this.fn = fn;
        }

        public static FileConfigurationWrapperImpl of(Function<String, Object> fn) {
            return new FileConfigurationWrapperImpl(fn);
        }

        @Override
        public <T> @Nullable T get(String path, Class<T> type) {
            Object value = fn.apply(path);
            if (value == null) {
                return null;
            }
            if (type.isInstance(value)) {
                return type.cast(value);
            }
            return null;
        }

        @Override
        public <T> @Nullable T get(String path, Class<T> type, T def) {
            Object value = fn.apply(path);
            if (value == null) {
                return def;
            }
            if (type.isInstance(value)) {
                return type.cast(value);
            }
            return def;
        }
    }
}
