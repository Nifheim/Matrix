package net.nifheim.matrix.common.util.adapter;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;
import java.util.Objects;
import org.bson.types.ObjectId;

/**
 * This class is used to serialize and deserialize ObjectId objects to and from JSON.
 *
 * @author Jaime Suárez
 * @see ObjectId
 */
public class ObjectIdTypeAdapter extends TypeAdapter<ObjectId> {

    @Override
    public void write(JsonWriter jsonWriter, ObjectId objectId) throws IOException {
        if (Objects.isNull(objectId)) {
            jsonWriter.nullValue();
        } else {
            jsonWriter.value(objectId.toHexString());
        }
    }

    @Override
    public ObjectId read(JsonReader jsonReader) throws IOException {
        if (jsonReader.peek() == JsonToken.NULL) {
            return null;
        }
        String hexId = jsonReader.nextString();
        if (ObjectId.isValid(hexId)) {
            return new ObjectId(hexId);
        } else {
            throw new IOException("Invalid ObjectId");
        }
    }
}
