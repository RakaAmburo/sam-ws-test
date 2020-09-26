package fr.anw.stompUserExample.server.controllers;

public class DynParams {
  private boolean doWait = false;
  private int sleepTime = 1000;
  private int hangAt;

  public int getHangAt() {
    return hangAt;
  }

  public void setHangAt(int hangAt) {
    this.hangAt = hangAt;
  }

  public boolean isDoWait() {
    return doWait;
  }

  public void setDoWait(boolean doWait) {
    this.doWait = doWait;
  }

  public int getSleepTime() {
    return sleepTime;
  }

  public void setSleepTime(int sleepTime) {
    this.sleepTime = sleepTime;
  }
}
