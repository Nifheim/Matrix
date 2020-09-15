package com.github.beelzebu.matrix.database.mongo;

import dev.morphia.converters.SimpleValueConverter;
import dev.morphia.converters.TypeConverter;
import dev.morphia.mapping.MappedField;
import net.md_5.bungee.api.ChatColor;

/**
 * Compatibility layer between 1.16 ChatColor and previous versions.
 *
 * @author Beelzebu
 */
@SuppressWarnings("deprecation")
public class ChatColorConverter extends TypeConverter implements SimpleValueConverter {

    public ChatColorConverter() {
        super(ChatColor.class);
    }

    @Override
    public Object decode(final Class targetClass, final Object fromDBObject, final MappedField optionalExtraInfo) {
        try {
            return fromDBObject == null ? ChatColor.RESET : ChatColor.valueOf((String) fromDBObject);
        } catch (ClassCastException e) {
            return ChatColor.RESET;
        }
    }

    @Override
    public Object encode(final Object value, final MappedField optionalExtraInfo) {
        return value == null ? null : ((ChatColor) value).name();
    }
}
