package com.alex.alexfirstapp.logic;

import java.util.Date;

public class TimerLogic {

	private boolean halfTimeNotificationDone = false;
	private long initialSeconds = 0;
	Date timerStarted;
	
	public boolean isHalfTimeNotificationDone() {
		return halfTimeNotificationDone;
	}

	public void setHalfTimeNotificationDone(boolean halfTimeNotificationDone) {
		this.halfTimeNotificationDone = halfTimeNotificationDone;
	}
	
	
	public TimerLogic(String minutes, String seconds) {
		
		long minutesLong = Long.valueOf(minutes);
		long secondsLong = Long.valueOf(seconds);
		
		initialSeconds = minutesLong * 60 + secondsLong;
		timerStarted = new Date();
		halfTimeNotificationDone = false;
	}
	
	public long getSecondsLeft() {
		
		long currentSeconds = (new Date()).getTime() / 1000;
		long startedSeconds = timerStarted.getTime() / 1000;
		
		if ( (currentSeconds - startedSeconds) > initialSeconds)
			return 0;
		
		return initialSeconds - (currentSeconds - startedSeconds);
	}
	
	public boolean getHalfTimeReached() {
		
		if (getSecondsLeft() <= initialSeconds / 2)
			return true;
		
		return false;
	}
	
}
