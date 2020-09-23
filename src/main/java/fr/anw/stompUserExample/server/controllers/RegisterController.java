package fr.anw.stompUserExample.server.controllers;

import fr.anw.stompUserExample.server.config.WsConfig;
import fr.anw.stompUserExample.server.entities.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.lang.invoke.MethodHandles;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Controller
public class RegisterController {
  public static final String ENDPOINT_REGISTER = "/register";
  private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
  ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
  private SimpMessagingTemplate messagingTemplate;
  private List<String> usersIds = new ArrayList<>();
  private TimeOut timeOut = new TimeOut(60, 300);

  @Autowired
  public RegisterController(SimpMessagingTemplate messagingTemplate) {
    this.messagingTemplate = messagingTemplate;
  }

  @MessageMapping(ENDPOINT_REGISTER)
  public void register(@Payload Message payload, Principal principal) throws Exception {
    String username = principal.getName();
    log.info("register:user=" + username + ",pl=" + payload.getContent());
    usersIds.add(username);
    Tasks tasks = new Tasks();
    int delay = timeOut.setRandomTimeOut();
    Runnable msgBack = tasks.sendResponseBack(messagingTemplate, username, delay);
    scheduler.schedule(msgBack, delay, TimeUnit.SECONDS );
    /*messagingTemplate.convertAndSendToUser(
        username,
        WsConfig.SUBSCRIBE_USER_REPLY,
        Message.builder().content("Thanks for your registration!").build());*/
    // messagingTemplate.convertAndSend(WsConfig.SUBSCRIBE_QUEUE, "Someone just registered saying:
    // "+payload);
  }

  @MessageMapping("/start")
  public void start(@Payload Message payload, Principal principal) {
    log.info("starting send");
    log.info(String.valueOf(usersIds.size()));
    usersIds
        .parallelStream()
        .forEach(
            u -> {
              messagingTemplate.convertAndSendToUser(
                  u,
                  WsConfig.SUBSCRIBE_USER_REPLY,
                  Message.builder().content("Thanks mister " + u).build());
            });
    log.info("finished sent");
  }
}
