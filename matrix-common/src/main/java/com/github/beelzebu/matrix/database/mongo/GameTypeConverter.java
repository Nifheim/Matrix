package com.github.beelzebu.matrix.database.mongo;

import com.github.beelzebu.matrix.api.server.GameType;
import com.github.beelzebu.matrix.server.GameTypeImpl;
import dev.morphia.converters.SimpleValueConverter;
import dev.morphia.converters.TypeConverter;
import dev.morphia.mapping.MappedField;

/**
 * @author Beelzebu
 */
public class GameTypeConverter extends TypeConverter implements SimpleValueConverter {

    public GameTypeConverter() {
        super(GameType.class);
    }

    @Override
    public Object decode(Class<?> targetClass, Object fromDBObject, MappedField optionalExtraInfo) {
        try {
            return fromDBObject == null ? GameType.NONE : GameTypeImpl.getByName(String.valueOf(fromDBObject));
        } catch (ClassCastException e) {
            return GameType.NONE;
        }
    }

    @Override
    public Object encode(Object value, MappedField optionalExtraInfo) {
        return value == null ? null : ((GameType) value).getGameName();
    }
}
