package com.github.beelzebu.matrix.util.adapter;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;
import org.bson.types.ObjectId;

public class ObjectIdTypeAdapter extends TypeAdapter<ObjectId> {

    @Override
    public void write(JsonWriter jsonWriter, ObjectId objectId) throws IOException {
        jsonWriter.value(objectId.toHexString());
    }

    @Override
    public ObjectId read(JsonReader jsonReader) throws IOException {
        if (ObjectId.isValid(jsonReader.nextString())) {
            return new ObjectId(jsonReader.nextString());
        } else {
            throw new IOException("Invalid ObjectId");
        }
    }
}
