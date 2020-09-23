package fr.anw.stompUserExample.server;

import fr.anw.stompUserExample.server.config.WsConfig;
import fr.anw.stompUserExample.server.controllers.RegisterController;
import fr.anw.stompUserExample.server.entities.Message;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.web.socket.messaging.WebSocketStompClient;

import java.lang.invoke.MethodHandles;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.stream.IntStream;
import org.javatuples.Pair;

public class MainTest {
    private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    public static void main(String[] args) throws InterruptedException {
        WsTestUtils wsTestUtils = new WsTestUtils();
        String wsUrl = "ws://127.0.0.1:" + "8080" + WsConfig.ENDPOINT_CONNECT;
        int loop = 3000;
        CountDownLatch cdl = new CountDownLatch(loop);
        CountDownLatch cdl2 = new CountDownLatch(loop);
        Map<Integer, Pair> container = new HashMap<Integer, Pair>();
        IntStream.range(0, loop).parallel().forEach(x -> {
            WebSocketStompClient stompClient = wsTestUtils.createWebSocketClient();
            try {
                StompSession stompSession = stompClient.connect(wsUrl, new ClientSessionHandler()).get();
                stompSession.subscribe(WsConfig.SUBSCRIBE_USER_PREFIX + WsConfig.SUBSCRIBE_USER_REPLY,
                        new ClientFrameHandler((payload) -> {
                            log.info("--> " + WsConfig.SUBSCRIBE_USER_PREFIX + WsConfig.SUBSCRIBE_USER_REPLY + " (cli1) : " + payload.toString());
                            cdl2.countDown();
                        }));
                stompSession.send(RegisterController.ENDPOINT_REGISTER, Message.builder().content("am here").build());
                Pair<WebSocketStompClient, StompSession> pair = new Pair<WebSocketStompClient, StompSession>(stompClient, stompSession);
                container.put(x, pair);
                cdl.countDown();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        });

        log.info("***************");
        log.info("***************");

        //cdl.await();
        log.info("end loop");
        Thread.sleep(1000);

        container.entrySet().stream().limit(1).forEach(c -> {
            ((StompSession)c.getValue().getValue1()).send("/start", Message.builder().content(c.getKey() + " says hello guys").build());
        });


        cdl2.await();
        log.info("all responses from server received");

        container.entrySet().stream().forEach(c ->{
            ((StompSession)c.getValue().getValue1()).disconnect();
            ((WebSocketStompClient)c.getValue().getValue0()).stop();
        });

    }

}
