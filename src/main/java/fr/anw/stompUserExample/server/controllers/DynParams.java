package fr.anw.stompUserExample.server.controllers;

public class DynParams {
    public boolean isDoWait() {
        return doWait;
    }

    public int getSleepTime() {
        return sleepTime;
    }

    private boolean doWait = false;

    public void setDoWait(boolean doWait) {
        this.doWait = doWait;
    }

    public void setSleepTime(int sleepTime) {
        this.sleepTime = sleepTime;
    }

    private int sleepTime = 1000;
}
