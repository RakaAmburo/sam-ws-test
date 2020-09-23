package fr.anw.stompUserExample.server.controllers;

import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;

public class WsTestUtils {

    public WebSocketStompClient createWebSocketClient() {
        WebSocketStompClient stompClient = new WebSocketStompClient(new StandardWebSocketClient());
        //stompClient.setMessageConverter(new StringMessageConverter());
        stompClient.setMessageConverter(new MappingJackson2MessageConverter());
        return stompClient;
    }
}
