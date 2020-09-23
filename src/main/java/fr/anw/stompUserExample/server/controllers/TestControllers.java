package fr.anw.stompUserExample.server.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@RestController
public class TestControllers {

  private ExecutorService threadPool = Executors.newFixedThreadPool(4);
  private ExecutorService mainThread = Executors.newSingleThreadExecutor();
  private ScheduledExecutorService remover = Executors.newSingleThreadScheduledExecutor();
  private Runnable main;
  private DynParams dynParams = new DynParams();

  private List<Pair> container = Collections.synchronizedList(new ArrayList<Pair>());
  private Tasks tasks = new Tasks();

  @GetMapping("/startTest")
  @ResponseStatus(HttpStatus.OK)
  public void start() {

    main = tasks.mainThread(threadPool, container, dynParams);
    mainThread.execute(main);
    Runnable rt = tasks.removerThread(container);
    remover.scheduleAtFixedRate(rt, 60, 15, TimeUnit.SECONDS);
  }

  @GetMapping("/closeTest")
  @ResponseStatus(HttpStatus.OK)
  public void close() {
    container.stream()
        .forEach(
            c -> {
              c.getStompSession().disconnect();
              c.getWebSocketStompClient().stop();
            });
    container.clear();
  }

  @GetMapping("/playTest")
  @ResponseStatus(HttpStatus.OK)
  public void play() {
    if (dynParams.isDoWait()) {
      dynParams.setDoWait(false);
      synchronized (tasks) {
        tasks.notify();
      }
    } else {
      dynParams.setDoWait(true);
    }
  }

  @GetMapping("/statusTest")
  @ResponseStatus(HttpStatus.OK)
  public Integer status() {
    return container.size();
  }

  @GetMapping("/changeWaitingTime/{time}")
  @ResponseStatus(HttpStatus.OK)
  public void changeWaitingTime(@PathVariable("time") Integer time) {
    dynParams.setSleepTime(time);
  }

  @GetMapping("/shutDown")
  @ResponseStatus(HttpStatus.OK)
  public void shutDown() {}
}
