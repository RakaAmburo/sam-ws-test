package fr.anw.stompUserExample.server.controllers;

import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.web.socket.messaging.WebSocketStompClient;

public class Pair {
  private WebSocketStompClient webSocketStompClient;
  private StompSession stompSession;
  private boolean connected = false;

  public boolean isConnected() {
    return connected;
  }

  public void setConnected(boolean connected) {
    this.connected = connected;
  }

  public WebSocketStompClient getWebSocketStompClient() {
    return webSocketStompClient;
  }

  public void setWebSocketStompClient(WebSocketStompClient webSocketStompClient) {
    this.webSocketStompClient = webSocketStompClient;
  }

  public StompSession getStompSession() {
    return stompSession;
  }

  public void setStompSession(StompSession stompSession) {
    this.stompSession = stompSession;
  }
}
