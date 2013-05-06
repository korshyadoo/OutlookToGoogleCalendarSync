package com.korshyadoo.calendar;

public class PSTSearchFrameRunnable implements Runnable {
	@Override
	public void run() {
		new PSTSearchFrame().setVisible(true);
	}
}
