package com.korshyadoo.calendar;


class LogInFrameRunnable implements Runnable {
	private MainFrame mainFrame;
	
	public LogInFrameRunnable() {
		mainFrame = null;
	}
	public LogInFrameRunnable(MainFrame mainFrame) {
		this.mainFrame = mainFrame;
	}
	@Override
	public void run() {
		new LogInFrame(mainFrame).setLocationRelativeTo(null);
	}
}
