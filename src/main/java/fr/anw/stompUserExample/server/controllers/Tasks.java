package fr.anw.stompUserExample.server.controllers;

import fr.anw.stompUserExample.server.config.WsConfig;
import fr.anw.stompUserExample.server.entities.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.web.socket.messaging.WebSocketStompClient;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;

public class Tasks {

  private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
  private TimeOut timeOut = new TimeOut(1, 8);

  public Runnable removerThread(BlockingQueue<Pair> container) {

    return () -> {
      try {
        List<Pair> delete = new ArrayList<>();
        log.info("removing start");
        container.removeIf(connection -> (connection.isConnected()) ? false : true);
      } catch (Exception e) {
        log.error("DELETING THREAD FAILED", e);
        System.exit(1);
      }
    };
  }

  public Runnable mainThread(
      ExecutorService threadPool, BlockingQueue<Pair> container, DynParams dynParams) {
    return () -> {
      Tasks tasks = new Tasks();
      int loop = 20;
      WsTestUtils wsTestUtils = new WsTestUtils();
      String wsUrl = "ws://192.168.1.132:" + "8080" + WsConfig.ENDPOINT_CONNECT;
      synchronized (this) {
        while (true) {
          if (dynParams.isDoWait()) {
            dynParams.setHangAt(container.size());
            try {
              wait();
            } catch (InterruptedException e) {
              e.printStackTrace();
              log.error("WAITING ERROR");
              System.exit(1);
            }
          }
          Pair pair = new Pair();
          if (!container.add(pair)) {
            log.error("CAN NOT ADD TO CONTAINER");
            System.exit(1);
          }
          threadPool.execute(tasks.start(wsUrl, wsTestUtils, pair, dynParams));
          try {
            Thread.sleep(dynParams.getSleepTime());
          } catch (InterruptedException e) {
            e.printStackTrace();
            log.error("SLEEP ERROR");
            System.exit(1);
          }
        }
      }
    };
  }

  public Runnable sendResponseBack(
      SimpMessagingTemplate messagingTemplate, String username, int delay) {
    return () -> {
      int possiblity = timeOut.setRandomTimeOut();
      Message message = null;
      if (possiblity == 1) {
        message = Message.builder().haveIt(false).content("dontHave " + delay).build();
      } else {
        message = Message.builder().haveIt(true).content("Dispatching! in " + delay).build();
      }
      messagingTemplate.convertAndSendToUser(username, WsConfig.SUBSCRIBE_USER_REPLY, message);
    };
  }

  public Runnable sendAskFoodRetry(StompSession session, int delay) {
    return () -> {
      Message message = null;
      message = Message.builder().haveIt(true).content("and whatAbout " + delay).build();
      session.send(RegisterController.ENDPOINT_REGISTER, message);
    };
  }

  public Runnable start(
      String wsUrl, WsTestUtils wsTestUtils, Pair connectionPair, DynParams dynParams) {
    return () -> {
      try {
        WebSocketStompClient stompClient = wsTestUtils.createWebSocketClient();
        StompSession stompSession = stompClient.connect(wsUrl, new ClientSessionHandler()).get();
        connectionPair.setWebSocketStompClient(stompClient);
        connectionPair.setStompSession(stompSession);
        connectionPair.setConnected(true);
        stompSession.subscribe(
            WsConfig.SUBSCRIBE_USER_PREFIX + WsConfig.SUBSCRIBE_USER_REPLY,
            new ClientFrameHandler(
                (payload) -> {
                  log.info(
                      "-> "
                          + WsConfig.SUBSCRIBE_USER_PREFIX
                          + WsConfig.SUBSCRIBE_USER_REPLY
                          + "(cli): "
                          + ((Message) payload).getContent());
                },
                connectionPair));
        stompSession.send(
            RegisterController.ENDPOINT_REGISTER,
            Message.builder().content("I what that food").build());

      } catch (InterruptedException e) {
        e.printStackTrace();
        log.error("INTERRUPTED EX");
        System.exit(1);
      } catch (ExecutionException e) {
        log.error("Execution exception", e);
        dynParams.setDoWait(true);
      }
    };
  }
}
