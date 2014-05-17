package com.alex.circuitTimer.logic;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;
import java.util.Observable;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class TimerLogic extends Observable {

	private static final int TIMER_UPDATE_INTERVAL = 100;
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


	public boolean isRestMode() {
		return restMode;
	}

	public long getInitialSeconds() {
		return initialSeconds;
	}
	
	public long getInitialSecondsRest() {
		return initialSecondsRest;
	}

	public boolean isPaused() {
		return paused;
	}

	public boolean isHalfTimeNotificationDone() {
		return halfTimeNotificationDone;
	}


	/**
	 * Constructor for TimerLogic.
	 * 
	 * @param minutes Duration of rounds in minutes
	 * @param seconds Duration of rounds in seconds (on top of minutes)
	 * @param secondsRest Duration of rest period
	 * @param rounds Number of rounds
	 */
	public TimerLogic(String minutes, String seconds, String minutesRest, String secondsRest, String rounds) {

		long minutesLong = Long.valueOf(minutes);
		long secondsLong = Long.valueOf(seconds);
		long minutesRestLong = Long.valueOf(minutesRest);
		long secondsRestLong = Long.valueOf(secondsRest);
		maxRounds = Long.valueOf(rounds);

		initialSeconds = minutesLong * 60 + secondsLong;
		initialSecondsRest = minutesRestLong * 60 + secondsRestLong;
		timerStarted = new Date();
		halfTimeNotificationDone = false;
		restMode = false;
	}

	/**
	 * reset the timer to it's initial settings (as defined in the constructor)
	 */
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

	/**
	 * @return current round number. If currentRound > maxRound use the latter for displaying.
	 */
	public long getCurrentRoundForDisplay() {
		if (currentRound < maxRounds) {
			return currentRound;
		}
		return maxRounds;
	}
	
	/**
	 * switch to the next round in case the current one is not the last one.
	 */
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

	/**
	 * Pause the timer and remember the remaining time, so that it can proceed later.
	 */
	public void pauseTimer() {
		this.paused = true;
		pausedSeconds = getSecondsLeft();
	}

	/**
	 * resume a paused timer
	 */
	public void resumeTimer() {
		Calendar cal = Calendar.getInstance();
		cal.setTime(new Date());
		long secondsBase = getSecondsBase();

		// calculate new start time
		int pausedSecondsInt = (new BigDecimal(secondsBase - pausedSeconds)).intValue();
		cal.add(Calendar.SECOND, pausedSecondsInt * (-1));
		timerStarted = cal.getTime();

		paused = false;
		pausedSeconds = 0;
		
		runTimer();
	}

	/**
	 * @return Number of seconds left for the current state of the timer (active time or rest time).
	 */
	public long getSecondsLeft() {

		long currentSeconds = (new Date()).getTime() / 1000;
		long startedSeconds = timerStarted.getTime() / 1000;
		long secondsBase = getSecondsBase();
		
		// subtract one seconds for smoothening out the displaying of seconds
		long secondsLeft = secondsBase - (currentSeconds - startedSeconds  -1);
		if (secondsLeft < 0) {
			return 0;
		}
		if (secondsLeft > secondsBase) {
			return secondsBase;
		}
		return secondsLeft;
	}

	/**
	 * @return Number of seconds to start with. Either initial active time or initial rest time.
	 */
	private long getSecondsBase() {
		if (restMode) {
			return initialSecondsRest;
		}
		return initialSeconds;
	}


	/**
	 * @return true if the half of the active time has been reached. Always false for rest mode.
	 */
	public boolean getHalfTimeReached() {

		if (!restMode && getSecondsLeft() <= initialSeconds / 2) {
			return true;
		}

		return false;
	}

	
	
	public void runTimer() {
		
		if (!paused && !isTimerFinished()) {
		
			Executors.newSingleThreadScheduledExecutor().schedule(
					runnable,
					TIMER_UPDATE_INTERVAL,
					TimeUnit.MILLISECONDS);
		}
	}

	Runnable runnable = new Runnable() {
		@Override
		public void run() {
			
			long secondsLeft = getSecondsLeft();
			setChanged();

			if (secondsLeft <= 0) {
				// this is the moment when we switch from rest mode to active mode
				if (restMode) {
					notifyObservers(TIMER_EVENT_BEGIN_ROUND);
					nextRound();
					restMode = false;
				}
				else {
					// only play sound when rest time is > 5 sec
					if (initialSecondsRest > 5) {
						notifyObservers(TIMER_EVENT_ACTIVE_TIME_FINISHED);
					}
		
					// no rest phase after last round, so increase round counter here already
					if (isLastRound()) {
						nextRound();
					}
					else {
						restMode = true;
					}
				}
				
				if ( !isTimerFinished()) {
					timerStarted = new Date();
				}
			}
			else {
				// check for half time
				if (getHalfTimeReached() && !halfTimeNotificationDone) {
					notifyObservers(TIMER_EVENT_HALFTIME_REACHED);
					halfTimeNotificationDone = true;
				}
					
			}
			runTimer();
		}
	};


	

}
