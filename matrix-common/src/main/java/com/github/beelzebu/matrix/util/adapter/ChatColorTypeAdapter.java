package com.github.beelzebu.matrix.util.adapter;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;
import net.md_5.bungee.api.ChatColor;
import org.jetbrains.annotations.NotNull;

/**
 * @author Beelzebu
 */
public class ChatColorTypeAdapter extends TypeAdapter<ChatColor> {

    @Override
    public void write(@NotNull JsonWriter out, @NotNull ChatColor value) throws IOException {
        out.value(value.name());
    }

    @Override
    public ChatColor read(@NotNull JsonReader in) throws IOException {
        return ChatColor.valueOf(in.nextString());
    }
}
