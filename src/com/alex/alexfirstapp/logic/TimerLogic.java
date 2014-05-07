package com.alex.alexfirstapp.logic;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;

public class TimerLogic {

	private boolean halfTimeNotificationDone = false;
	private long initialSeconds = 0;
	private long pausedSeconds = 0;
	private boolean paused = false;
	Date timerStarted;
	
	public boolean isPaused() {
		return paused;
	}
	
	public boolean isHalfTimeNotificationDone() {
		return halfTimeNotificationDone;
	}

	public void setHalfTimeNotificationDone(boolean halfTimeNotificationDone) {
		this.halfTimeNotificationDone = halfTimeNotificationDone;
	}
	
	public void resetTimer() {
		timerStarted = new Date();
		
		if (paused) {
			pausedSeconds = getSecondsLeft();
		}
	}

	public void pauseTimer() {
		this.paused = true;
		pausedSeconds = getSecondsLeft();
	}
	
	public void resumeTimer() {
		Calendar cal = Calendar.getInstance();
		cal.setTime(new Date());
		int pausedSecondsInt = (new BigDecimal(initialSeconds - pausedSeconds)).intValue();
		cal.add(Calendar.SECOND, pausedSecondsInt * (-1));
		timerStarted = cal.getTime();
		paused = false;
		pausedSeconds = 0;
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
