package com.korshyadoo.calendar;

public class SyncReturn {
	public int before;
	public int after;
	
	SyncReturn() {
		before = 0;
		after = 0;
	}
	SyncReturn(int x) {
		before = x;
		after = x;
	}
	SyncReturn(int b, int a) {
		before = b;
		after = a;
	}
}