package com.alex.alexfirstapp.logic;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;
import java.util.Observable;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class TimerLogic extends Observable {

	public static final String UPDATE_TIMER_DISPLAY = "updateDisplay";
	public static final String TIMER_EVENT_BEGIN_ROUND = "beginRound";
	public static final String TIMER_EVENT_ACTIVE_TIME_FINISHED = "activeTimeFinished";
	public static final String TIMER_EVENT_HALFTIME_REACHED = "halfTimeReached";


	private boolean halfTimeNotificationDone = false;
	private long initialSeconds = 0;
	private long initialSecondsRest = 10;
	private long pausedSeconds = 0;
	private long maxRounds = 1;
	private long currentRound = 1;
	private boolean paused = false;
	private boolean restMode = false;

	Date timerStarted;

	public long getCurrentRound() {
		if (currentRound < maxRounds) {
			return currentRound;
		}
		return maxRounds;
	}

	public boolean isRestMode() {
		return restMode;
	}

	public long getInitialSeconds() {
		return initialSeconds;
	}

	public boolean isPaused() {
		return paused;
	}

	public boolean isHalfTimeNotificationDone() {
		return halfTimeNotificationDone;
	}


	public TimerLogic(String minutes, String seconds, String secondsRest, String rounds) {

		long minutesLong = Long.valueOf(minutes);
		long secondsLong = Long.valueOf(seconds);
		initialSecondsRest = Long.valueOf(secondsRest);
		maxRounds = Long.valueOf(rounds);

		initialSeconds = minutesLong * 60 + secondsLong;
		timerStarted = new Date();
		halfTimeNotificationDone = false;
		restMode = false;
	}

	public void resetTimer() {
		timerStarted = new Date();
		restMode = false;
		currentRound = 1;

		if (paused) {
			pausedSeconds = getSecondsLeft();
		}
		else {
			runTimer();
		}
	}

	public void nextRound() {
		halfTimeNotificationDone = false;
		if (currentRound <= maxRounds) {
			currentRound++;
		}
	}

	public boolean isTimerFinished() {
		if (currentRound > maxRounds) {
			return true;
		}
		return false;
	}

	public boolean isLastRound() {
		if (currentRound >= maxRounds) {
			return true;
		}
		return false;
	}

	public void pauseTimer() {
		this.paused = true;
		pausedSeconds = getSecondsLeft();
	}

	public void resumeTimer() {
		Calendar cal = Calendar.getInstance();
		cal.setTime(new Date());
		long secondsBase = initialSeconds;

		if (restMode) {
			secondsBase = initialSecondsRest;
		}

		// calculate new start time
		int pausedSecondsInt = (new BigDecimal(secondsBase - pausedSeconds)).intValue();
		cal.add(Calendar.SECOND, pausedSecondsInt * (-1));
		timerStarted = cal.getTime();

		paused = false;
		pausedSeconds = 0;
		
		runTimer();
	}

	public long getSecondsLeft() {

		long currentSeconds = (new Date()).getTime() / 1000;
		long startedSeconds = timerStarted.getTime() / 1000;
		long secondsBase = initialSeconds;

		if (restMode) {
			secondsBase = initialSecondsRest;
		}
		
		// add one second to secondsBase to make the displaying smoother when switching between rest and active mode
		if ( (currentSeconds - startedSeconds) >= secondsBase+1) {
			timerStarted = new Date();
			return 0;
		}

		return secondsBase - (currentSeconds - startedSeconds);
	}


	public boolean getHalfTimeReached() {

		if (!restMode && getSecondsLeft() <= initialSeconds / 2) {
			return true;
		}

		return false;
	}

	private void executeEvent(String eventName) {
		notifyObservers(TIMER_EVENT_BEGIN_ROUND);
	}
	
	
	public void runTimer() {
		
		if (!isPaused()) {
		
			Executors.newSingleThreadScheduledExecutor().schedule(
					runnable,
					1000,
					TimeUnit.MILLISECONDS);
		}
	}

	Runnable runnable = new Runnable() {
		@Override
		public void run() {
			
			runTimer();
			
			long secondsLeft = getSecondsLeft();
			setChanged();

			if (secondsLeft <= 0) {
				// this is the moment when we switch from rest mode to active mode
				if (restMode) {
					executeEvent(TIMER_EVENT_BEGIN_ROUND);
					nextRound();
					restMode = false;
				}
				else {
					executeEvent(TIMER_EVENT_ACTIVE_TIME_FINISHED);
		
					// no rest phase after last round, so increase round counter here already
					if (isLastRound()) {
						nextRound();
					}
					restMode = true;
				}
			}
			else {
				// check for half time
				if (getHalfTimeReached() && !halfTimeNotificationDone) {
					executeEvent(TIMER_EVENT_HALFTIME_REACHED);
					halfTimeNotificationDone = true;
				}
					
			}
			
			executeEvent(UPDATE_TIMER_DISPLAY);
			
    	    
		}
	};

}
