package fr.anw.stompUserExample.server.controllers;

import fr.anw.stompUserExample.server.entities.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompHeaders;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.Type;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class ClientFrameHandler implements StompFrameHandler {
  private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
  private final Consumer<Message> frameHandler;
  private final Pair connectionPair;
  ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
  private TimeOut timeOut = new TimeOut(60, 120);

  public ClientFrameHandler(Consumer<Message> frameHandler, Pair connectionPair) {
    this.frameHandler = frameHandler;
    this.connectionPair = connectionPair;
  }

  @Override
  public Type getPayloadType(StompHeaders headers) {
    return Message.class;
  }

  @Override
  public void handleFrame(StompHeaders headers, Object payload) {
    frameHandler.accept((Message) payload);
    if (((Message) payload).getHaveIt()) {
      log.info("they got my food, disconnect");
      this.connectionPair.getStompSession().disconnect();
      this.connectionPair.getWebSocketStompClient().stop();
      this.connectionPair.setConnected(false);
    } else {
      log.info("NO FOOD, chose another");
      Tasks tasks = new Tasks();
      int delay = timeOut.setRandomTimeOut();
      Runnable msgBack =
          tasks.sendAskFoodRetry(this.connectionPair.getStompSession(), delay);
      scheduler.schedule(msgBack, delay, TimeUnit.SECONDS);
    }
  }
}
