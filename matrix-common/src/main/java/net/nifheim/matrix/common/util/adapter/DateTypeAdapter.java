package net.nifheim.matrix.common.util.adapter;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;
import java.util.Date;

/**
 * This class is used to serialize and deserialize Date objects to and from JSON.
 *
 * @author Jaime Suárez
 * @see Date
 */
public class DateTypeAdapter extends TypeAdapter<Date> {

    @Override
    public void write(JsonWriter jsonWriter, Date date) throws IOException {
        jsonWriter.value(date.getTime());
    }

    @Override
    public Date read(JsonReader jsonReader) throws IOException {
        return new Date(jsonReader.nextLong());
    }
}
