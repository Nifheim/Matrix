package io.github.beelzebu.matrix.interfaces;

import io.github.beelzebu.matrix.player.MatrixPlayer;
import io.github.beelzebu.matrix.utils.MessagesManager;
import java.io.File;
import java.io.InputStream;
import java.util.UUID;
import java.util.logging.Logger;
import net.md_5.bungee.api.chat.BaseComponent;

public interface IMethods {

    IConfiguration getConfig();

    /**
     * Obtiene un archivo de mensajes.
     *
     * @param lang idioma para el archivo.
     * @return archivo de mensajes para el idioma especificado, si el idioma no
     * existe, el archivo por defecto.
     */
    MessagesManager getMessages(String lang);

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
     * @param log objeto para convertir a string.
     */
    void log(Object log);

    /**
     * Obtiene el nombre de un usuario online en base a su UUID.
     *
     * @param uuid usuario para obtener el nombre.
     * @return nombre del usuario o null si no está conectado.
     */
    String getNick(UUID uuid);

    /**
     * Obtiene la UUID de un usuario online en base a su nick.
     *
     * @param player usuario para obtener el UUID.
     * @return UUID del usuario o null si no está conectado.
     */
    UUID getUUID(String player);

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
     * Obtiene la carpeta del plugin.
     *
     * @return carpeta del plugin.
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
     * Obtiene la versión del plugin, especificada en el pom.
     *
     * @return versión del plugin.
     */
    String getVersion();

    /**
     * Obtiene si un usuario está conectado en base a su nombre.
     *
     * @param name usuario para revisar.
     * @return <i>true</i> en caso de que esté conectado, <i>false</i> de otra
     * forma.
     * @see #isOnline(java.lang.String, boolean)
     * @deprecated -
     */
    @Deprecated
    boolean isOnline(String name);

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
     * @return <i>true</i> en caso de que esté conectado, <i>false</i> de otra
     * forma.
     * @see #isOnline(java.util.UUID, boolean)
     * @deprecated -
     */
    @Deprecated
    boolean isOnline(UUID uuid);

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
     * Obtiene una nueva instancia de MatrixPlayer.
     *
     * @param uuid UUID para generar la instancia.
     * @return nueva instancia creada.
     */
    MatrixPlayer getPlayer(UUID uuid);

    /**
     * Obtiene el logger del servidor.
     *
     * @return logger del servidor.
     */
    Logger getLogger();
}
