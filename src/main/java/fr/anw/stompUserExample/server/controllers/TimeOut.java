package fr.anw.stompUserExample.server.controllers;

import java.security.SecureRandom;
import java.util.Random;
import java.util.stream.IntStream;

public class TimeOut {

	// probar con numeros grandes para no hacer trabajar al director
	// private static final int LOW = 200;
	// private static final int HIGHT = 1000;

	private int low;
	private int high;
	private SecureRandom r;

	public TimeOut(int l, int h) {
		this.low = l;
		this.high = h;
		this.r = new SecureRandom();
	}

	public int setRandomTimeOut() {



		int time = r.nextInt(this.high - this.low + 1) + this.low;

		try {
			Thread.sleep(time);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return time;

	}

	public void setRandomTimeOut(int times) {

		IntStream.rangeClosed(1, times).forEach(a -> this.setRandomTimeOut());

	}

}
