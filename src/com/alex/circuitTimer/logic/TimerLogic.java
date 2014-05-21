package com.alex.circuitTimer.logic;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;
import java.util.Observable;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author SKR
 *
 */
/**
 * @author SKR
 *
 */
public class TimerLogic extends Observable {

	private static final int TIMER_UPDATE_INTERVAL = 100;
	private static final int COUNTDOUW_SECONDS = 5;
	public static final String TIMER_EVENT_BEGIN_ROUND = "beginRound";
	public static final String TIMER_EVENT_ACTIVE_TIME_FINISHED = "activeTimeFinished";
	public static final String TIMER_EVENT_HALFTIME_REACHED = "halfTimeReached";

	private boolean halfTimeNotificationDone = false;
	private long initialSeconds = 0;
	private long initialSecondsRest = 10;
	private long pausedSeconds = 0;
	private long maxRounds = 1;
	private long currentRound = 0;
	private boolean paused = false;
	private boolean restMode = false;
	
	// helper instance variables for memory saving
	// when these variables had method scope the timer app
	// would crash after 5 rounds due to an out of memory Exception on an HTC One Mini
	long currentSeconds = 0;
	long startedSeconds = 0;
	long secondsBase = 0;
	long secondsLeft = 0;
	int pausedSecondsInt = 0;
	
	// Thred executor for update timer thread
	ScheduledExecutorService threadExecuter;

	Date timerStarted;


	public long getCurrentRound() {
		return currentRound;
	}
	
	public long getMaxRounds() {
		return maxRounds;
	}

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
		threadExecuter = Executors.newSingleThreadScheduledExecutor();
	}

	/**
	 * reset the timer to it's initial settings (as defined in the constructor)
	 */
	public void resetTimer() {
		timerStarted = new Date();
		restMode = false;
		currentRound = 0;

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
	 * Notify the observers, so that the sound playback can be triggered accordingly.
	 */
	public void nextRound() {
		halfTimeNotificationDone = false;
		if (currentRound <= maxRounds) {
			currentRound++;
		}
		notifyObservers(TIMER_EVENT_BEGIN_ROUND);
	}

	/**
	 * true if the timer has stepped through all rounds.
	 * @return
	 */
	public boolean isTimerFinished() {
		if (currentRound > maxRounds) {
			return true;
		}
		return false;
	}

	/**
	 * True if the current round is the last one.
	 * Needed to check whether or not a rest phase is needed (no rest phase after last round).
	 * @return
	 */
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
		secondsBase = getSecondsBase();

		// calculate new start time
		pausedSecondsInt = (new BigDecimal(secondsBase - pausedSeconds)).intValue();
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

		
		currentSeconds = Calendar.getInstance().getTime().getTime() / 1000;
		startedSeconds = timerStarted.getTime() / 1000;
		secondsBase = getSecondsBase();
		
		// subtract one seconds for smoothening out the displaying of seconds
		secondsLeft = secondsBase - (currentSeconds - startedSeconds);
		if (secondsLeft < 0) {
			return 0;
		}
		if (secondsLeft > secondsBase) {
			return secondsBase;
		}
		return secondsLeft;
	}

	/**
	 * @return Number of seconds to perform calculations on.
	 * Either initial active time, initial rest time or countdown time.
	 */
	private long getSecondsBase() {
		if (currentRound == 0) {
			return COUNTDOUW_SECONDS;
		}
		
		if (restMode) {
			return initialSecondsRest;
		}
		return initialSeconds;
	}


	/**
	 * @return true if the half of the active time has been reached.
	 * Always false for rest mode.
	 */
	public boolean getHalfTimeReached() {

		if (currentRound!= 0 && !restMode && getSecondsLeft() <= initialSeconds / 2) {
			return true;
		}

		return false;
	}

	
	
	/**
	 * schedule the next loop of the timer Runnable that controls the timer.
	 */
	public void runTimer() {
		
		if (!paused && !isTimerFinished()) {
		
			threadExecuter.schedule(
					runnable,
					TIMER_UPDATE_INTERVAL,
					TimeUnit.MILLISECONDS);
		}
	}

	
	/**
	 * Method for controlling the timer within a separate thread.
	 * Makes sure the timer is in the right state (countdown, active, rest).
	 * Triggers playback of sounds by notifying observers at events like round begin, half time or round end.
	 */
	Runnable runnable = new Runnable() {
		@Override
		public void run() {
			
			secondsLeft = getSecondsLeft();
			setChanged();
			
			// countdown before workout begin

			if (secondsLeft <= 0) {
				// this is the moment when we switch from rest mode to active mode
				if (restMode) {
					nextRound();
					restMode = false;
				}
				else {
					// only play sound when rest time is > 5 sec
					if (currentRound > 0 && initialSecondsRest > 5) {
						notifyObservers(TIMER_EVENT_ACTIVE_TIME_FINISHED);
					}
		
					// no rest phase after last round and after countdown, so increase round counter here already
					if (isLastRound() || currentRound == 0) {
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
