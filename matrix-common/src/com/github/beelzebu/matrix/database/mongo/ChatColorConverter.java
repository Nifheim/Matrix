package com.github.beelzebu.matrix.database.mongo;

import net.md_5.bungee.api.ChatColor;
import org.mongodb.morphia.converters.SimpleValueConverter;
import org.mongodb.morphia.converters.TypeConverter;
import org.mongodb.morphia.mapping.MappedField;

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
