package net.nifheim.matrix.common.util;

import com.google.gson.JsonSyntaxException;
import java.lang.reflect.Type;
import net.nifheim.matrix.common.api.MatrixCommon;

public final class JsonUtils {

    private JsonUtils() {
        throw new RuntimeException("Cannot instantiate JsonUtils");
    }


    public static Object parseJsonValue(String json, Type type) throws JsonSyntaxException {
        return MatrixCommon.GSON.fromJson(json, type);
    }
}
