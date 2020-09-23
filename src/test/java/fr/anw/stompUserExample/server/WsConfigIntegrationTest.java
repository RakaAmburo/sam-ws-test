package fr.anw.stompUserExample.server;

import fr.anw.stompUserExample.server.config.WsConfig;
import fr.anw.stompUserExample.server.controllers.RegisterController;
import fr.anw.stompUserExample.server.entities.Message;
import org.junit.*;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.socket.messaging.WebSocketStompClient;

import java.lang.invoke.MethodHandles;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.stream.IntStream;

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = RANDOM_PORT)
public class WsConfigIntegrationTest {
    private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    @Value("${local.server.port}")
    private int port;

    private WebSocketStompClient stompClient1;
    private StompSession stompSession1;

    private WebSocketStompClient stompClient2;
    private StompSession stompSession2;

    private WsTestUtils wsTestUtils = new WsTestUtils();

    @Before
    public void setUp() throws Exception {
        String wsUrl = "ws://127.0.0.1:" + port + WsConfig.ENDPOINT_CONNECT;

        /*stompClient1 = wsTestUtils.createWebSocketClient();
        stompSession1 = stompClient1.connect(wsUrl, new ClientSessionHandler()).get();

        stompClient2 = wsTestUtils.createWebSocketClient();
        stompSession2 = stompClient2.connect(wsUrl, new ClientSessionHandler()).get();*/
    }

    @After
    public void tearDown() throws Exception {
       /* stompSession1.disconnect();
        stompClient1.stop();

        stompSession2.disconnect();
        stompClient2.stop();*/
    }

    @Ignore
    @Test
    public void testWs() throws InterruptedException {
        String wsUrl = "ws://127.0.0.1:" + port + WsConfig.ENDPOINT_CONNECT;
        int loop = 3000;
        CountDownLatch cdl = new CountDownLatch(loop);
        CountDownLatch cdl2 = new CountDownLatch(loop);
        Map<Integer, StompSession> container = new HashMap<Integer, StompSession>();
        IntStream.range(0, loop).parallel().forEach(x -> {
            WebSocketStompClient stompClient = wsTestUtils.createWebSocketClient();
            try {
                StompSession stompSession = stompClient.connect(wsUrl, new ClientSessionHandler()).get();
                stompSession.subscribe(WsConfig.SUBSCRIBE_USER_PREFIX + WsConfig.SUBSCRIBE_USER_REPLY,
                        new ClientFrameHandler((payload) -> {
                            cdl2.countDown();
                            log.info("--> " + WsConfig.SUBSCRIBE_USER_PREFIX + WsConfig.SUBSCRIBE_USER_REPLY + " (cli1) : " + payload.toString());
                        }));
                stompSession.send(RegisterController.ENDPOINT_REGISTER, Message.builder().content("am here").build());
                container.put(x, stompSession);
                cdl.countDown();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        });

        log.info("***************");
        log.info("***************");

        cdl.await();
        log.info("end loop");
        Thread.sleep(1000);

        container.entrySet().stream().limit(1).forEach(c -> {
            c.getValue().send("/start", Message.builder().content(c.getKey() + " says hello guys").build());
        });

        cdl2.await();


    }

    @Test
    @Ignore
    public void receivesMessageFromSubscribedQueue() throws Exception {

        log.info("### client1 subscribes");
        BlockingQueue<String> queue1 = new LinkedBlockingDeque<>();
        BlockingQueue<String> userQueue1 = new LinkedBlockingDeque<>();
        stompSession1.subscribe(WsConfig.SUBSCRIBE_QUEUE,
                new ClientFrameHandler((payload) -> {
                    log.info("--> " + WsConfig.SUBSCRIBE_QUEUE + " (cli1) : " + payload);
                    queue1.offer(payload.toString());
                }));
        stompSession1.subscribe(WsConfig.SUBSCRIBE_USER_PREFIX + WsConfig.SUBSCRIBE_USER_REPLY,
                new ClientFrameHandler((payload) -> {
                    log.info("--> " + WsConfig.SUBSCRIBE_USER_PREFIX + WsConfig.SUBSCRIBE_USER_REPLY + " (cli1) : " + payload);
                    userQueue1.offer(payload.toString());
                }));
        Thread.currentThread().sleep(100);

        log.info("### client2 subscribes");
        BlockingQueue<String> queue2 = new LinkedBlockingDeque<>();
        BlockingQueue<String> userQueue2 = new LinkedBlockingDeque<>();
        stompSession2.subscribe(WsConfig.SUBSCRIBE_QUEUE,
                new ClientFrameHandler((payload) -> {
                    log.info("--> " + WsConfig.SUBSCRIBE_QUEUE + " (cli2) : " + payload);
                    queue2.offer(payload.toString());
                }));
        stompSession2.subscribe(WsConfig.SUBSCRIBE_USER_PREFIX + WsConfig.SUBSCRIBE_USER_REPLY,
                new ClientFrameHandler((payload) -> {
                    log.info("--> " + WsConfig.SUBSCRIBE_USER_PREFIX + WsConfig.SUBSCRIBE_USER_REPLY + " (cli2) : " + payload);
                    userQueue2.offer(payload.toString());
                }));

        Thread.currentThread().sleep(100);

        log.info("### client1 registers");
        stompSession1.send(RegisterController.ENDPOINT_REGISTER, "hello guys");
        Thread.currentThread().sleep(100);
        Assert.assertEquals("Thanks for your registration!", userQueue1.poll());
        Assert.assertEquals("Someone just registered saying: hello guys", queue1.poll());
        Assert.assertEquals("Someone just registered saying: hello guys", queue2.poll());

        Thread.currentThread().sleep(100);

        log.info("### client2 registers");
        stompSession2.send(RegisterController.ENDPOINT_REGISTER, "yo!");
        Thread.currentThread().sleep(100);
        Assert.assertEquals("Thanks for your registration!", userQueue2.poll());
        Assert.assertEquals("Someone just registered saying: yo!", queue1.poll());
        Assert.assertEquals("Someone just registered saying: yo!", queue2.poll());
    }

}