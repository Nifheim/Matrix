package com.github.beelzebu.matrix.util.adapter;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;
import net.md_5.bungee.api.ChatColor;

/**
 * @author Beelzebu
 */
public class ChatColorTypeAdapter extends TypeAdapter<ChatColor> {

    @Override
    public void write(JsonWriter out, ChatColor value) throws IOException {
        out.value(value.name());
    }

    @Override
    public ChatColor read(JsonReader in) throws IOException {
        return ChatColor.valueOf(in.nextString());
    }
}
