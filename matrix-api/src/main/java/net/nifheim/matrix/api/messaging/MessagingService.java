package net.nifheim.matrix.api.messaging;

import net.nifheim.matrix.api.messaging.message.Message;
import net.nifheim.matrix.api.service.MatrixService;

/**
 * Service for send and receive messages from other matrix instances.
 *
 * @author Jaime Suárez
 */
public interface MessagingService extends MatrixService {

    /**
     * Send a message using the message broker created by the implementation, this message has a json content to allow
     * complex data to be transmitted to other instances connected to the same message broker.
     *
     * @param message {@link Message} to send through the message broker.
     */
    void sendMessage(Message message);

    /**
     * Register a listener for this message broker to consume/read the incoming messages.
     *
     * @param messageListener the listener to consume incoming messages.
     */
    void registerListener(MessageListener<? extends Message> messageListener);

    // TODO: handle message listeners with generics
}
