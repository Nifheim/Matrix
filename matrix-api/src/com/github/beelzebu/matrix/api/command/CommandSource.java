package com.github.beelzebu.matrix.api.command;

/**
 * @author Beelzebu
 */
public interface CommandSource {

    String getName();

    void execute(String command);

    void sendMessage(String message);
}
