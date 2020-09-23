package fr.anw.stompUserExample.server.controllers;

import fr.anw.stompUserExample.server.entities.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.Type;

/**
 * Logs various events for debugging purpose.
 */
public class ClientSessionHandler extends StompSessionHandlerAdapter {
    private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    @Override
    public void afterConnected(StompSession session, StompHeaders connectedHeaders) {
        String username = connectedHeaders.get("user-name").iterator().next();
        log.info("afterConnected: username="+username);
        super.afterConnected(session, connectedHeaders);
    }

    @Override
    public void handleException(StompSession session, StompCommand command, StompHeaders headers, byte[] payload, Throwable exception) {
        log.info("handleException: exception="+exception);
        super.handleException(session, command, headers, payload, exception);
    }

    @Override
    public Type getPayloadType(StompHeaders headers) {
        return Message.class;
    }
}