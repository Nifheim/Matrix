package io.github.beelzebu.matrix.api.plugin;

import io.github.beelzebu.matrix.api.config.AbstractConfig;
import io.github.beelzebu.matrix.api.config.MatrixConfig;
import io.github.beelzebu.matrix.api.player.MatrixPlayer;
import java.io.File;
import java.io.InputStream;
import java.util.UUID;
import net.md_5.bungee.api.chat.BaseComponent;

public interface MatrixPlugin {

    /**
     * Disconnect this player from this server, if this server is a proxy the player will be disconnected from the
     * network, but if this server is a spigot server the player will be sent to main lobby.
     *
     * @param uniqueId UUID of the player that must be disconnected.
     */
    default void kickPlayer(UUID uniqueId) {
        kickPlayer(uniqueId, "");
    }

    /**
     * Disconnect this player from this server, if this server is a proxy the player will be disconnected from the
     * network, but if this server is a spigot server the player will be sent to main lobby.
     *
     * @param name Name of the player that must be disconnected.
     */
    default void kickPlayer(String name) {
        kickPlayer(name, "");
    }

    /**
     * Disconnect this player from this server, if this server is a proxy the player will be disconnected from the
     * network, but if this server is a spigot server the player will be sent to main lobby.
     *
     * @param matrixPlayer Player that must be disconnected.
     */
    default void kickPlayer(MatrixPlayer matrixPlayer) {
        kickPlayer(matrixPlayer, "");
    }

    MatrixConfig getConfig();

    /**
     * Carga un archivo como si fuera una configuración.
     *
     * @param file archivo para intentar cargar.
     * @return configuración en base al archivo.
     */
    AbstractConfig getFileAsConfig(File file);

    /**
     * Ejecuta una tarea en un nuevo hilo.
     *
     * @param runnable tarea para ejecutar.
     */
    void runAsync(Runnable runnable);

    /**
     * Ejecuta una tarea en un nuevo hilo durante un tiempo determinado.
     *
     * @param runnable tarea para ejecutar
     * @param timer    tiempo de ejecución en segundos.
     */
    void runAsync(Runnable runnable, Integer timer);

    /**
     * Ejecuta una tarea en el hilo principal.
     *
     * @param runnable
     */
    void runSync(Runnable runnable);

    /**
     * Ejecuta un comando desde la consola.
     *
     * @param command comando a ejecutar.
     */
    void executeCommand(String command);

    /**
     * Envía un mensaje a la consola con el prefijo del log.
     *
     * @param message mensaje para enviar.
     */
    void log(String message);

    /**
     * Obtiene la consola del servidor.
     *
     * @return -
     */
    Object getConsole();

    /**
     * Envía un mensaje a un CommandSender (consola o jugador).
     *
     * @param CommandSender commandsender para enviar el mensaje.
     * @param message       mensaje a enviar.
     */
    void sendMessage(Object CommandSender, BaseComponent[] message);

    /**
     * Envía un mensaje a un usuario online en base a su nombre.
     *
     * @param name    usuario para enviar el mensaje.
     * @param message mensaje para enviar.
     */
    void sendMessage(String name, String message);

    /**
     * Envía un mensaje a un usuario online en base a su UUID.
     *
     * @param uuid    usuario para enviar el mensaje.
     * @param message mensaje para enviar.
     */
    void sendMessage(UUID uuid, String message);

    /**
     * Obtiene la carpeta del matrixPlugin.
     *
     * @return carpeta del matrixPlugin.
     */
    File getDataFolder();

    /**
     * Obtiene un archivo dentro del jar en base a su nombre.
     *
     * @param filename archivo para obtener.
     * @return archivo encontrado o null en caso de que no exista.
     */
    InputStream getResource(String filename);

    /**
     * Obtiene la versión del matrixPlugin, especificada en el pom.
     *
     * @return versión del matrixPlugin.
     */
    String getVersion();

    /**
     * Obtiene si un usuario está conectado en base a su nombre.
     *
     * @param name usuario para revisar.
     * @param here si se debe revisar solamente en este servidor o en la network
     *             entera.
     * @return <i>true</i> en caso de que esté conectado, <i>false</i> de otra
     * forma.
     */
    boolean isOnline(String name, boolean here);

    /**
     * Obtiene si un usuario está conectado en base a su UUID.
     *
     * @param uuid usuario para revisar.
     * @param here si se debe revisar solamente en este servidor o en la network
     *             entera.
     * @return <i>true</i> en caso de que esté conectado, <i>false</i> de otra
     * forma.
     */
    boolean isOnline(UUID uuid, boolean here);

    /**
     * Ejecuta el evento de subida de nivel.
     *
     * @param uuid   usuario para ejecutar el evento.
     * @param newexp nueva xp del usuario luego de subir de nivel.
     * @param oldexp xp antes de subir de nivel.
     */
    void callLevelUPEvent(UUID uuid, long newexp, long oldexp);

    /**
     * Obtiene el idioma del juego del usuario.
     *
     * @param uuid usuario para revisar
     * @return idioma del usuario en ISO code.
     */
    String getLocale(UUID uuid);

    /**
     * Banea a un usuario del servidor.
     *
     * @param name
     */
    void ban(String name);

    /**
     * Get the uuid for a online player.
     *
     * @param name Player name to lookup.
     * @return UUID for the player if was found, null otherwise.
     */
    UUID getUniqueId(String name);

    /**
     * Disconnect this player from this server with the given reason, if this server is a proxy the player will be
     * disconnected from the network and the reason will be the kick message seen in the screen, but if this server is a
     * spigot server the player will be sent to main lobby and the provided reason will be sent as a chat message.
     *
     * @param uniqueId UUID of the player that must be disconnected.
     * @param reason   Reason for this disconnection.
     */
    void kickPlayer(UUID uniqueId, String reason);

    /**
     * Disconnect this player from this server with the given reason, if this server is a proxy the player will be
     * disconnected from the network and the reason will be the kick message seen in the screen, but if this server is a
     * spigot server the player will be sent to main lobby and the provided reason will be sent as a chat message.
     *
     * @param name   Name of the player that must be disconnected.
     * @param reason Reason for this disconnection.
     */
    void kickPlayer(String name, String reason);

    /**
     * Disconnect this player from this server with the given reason, if this server is a proxy the player will be
     * disconnected from the network and the reason will be the kick message seen in the screen, but if this server is a
     * spigot server the player will be sent to main lobby and the provided reason will be sent as a chat message.
     *
     * @param matrixPlayer Player that must be disconnected.
     * @param reason       Reason for this disconnection.
     */
    void kickPlayer(MatrixPlayer matrixPlayer, String reason);

    /**
     * Get the bootstrap (matrixPlugin main class that is loaded by the implementation matrixPlugin manager) that loaded
     * this
     * matrixPlugin, it never will be null.
     *
     * @return bootstrap instance.
     */
    MatrixBootstrap getBootstrap();
}
