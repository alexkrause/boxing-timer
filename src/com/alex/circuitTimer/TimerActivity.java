package com.alex.circuitTimer;



import java.util.Observable;
import java.util.Observer;

import com.alex.circuitTimer.R;
import com.alex.circuitTimer.logic.TimerLogic;
import com.alex.circuitTimer.ui.MainMenuLogic;

import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v4.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.media.AudioManager;
import android.media.AudioManager.OnAudioFocusChangeListener;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

public class TimerActivity extends ActionBarActivity implements Observer {

	private static final int SCREEN_UPDATE_INTERVAL = 100;
	private TimerLogic timerLogic = null;
	private TextView timerTextView;
	private TextView roundsTextView;
    private boolean playSounds = true;
    private boolean playHalftimeSounds = true;
    private Handler handler = new Handler();
    private long secondsLeft = 0;
    private String eventType;
    private String roundDisplay;
    
    /**
     * instanciate OnAudioFocusChangeListener for media playback
     */
    OnAudioFocusChangeListener afChangeListener = new OnAudioFocusChangeListener() {
	    public void onAudioFocusChange(int focusChange) {
	        if (focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT) {
	            // Pause playback
	        } else if (focusChange == AudioManager.AUDIOFOCUS_GAIN) {
	            // Resume playback 
	        } else if (focusChange == AudioManager.AUDIOFOCUS_LOSS) {
	            // Stop playback
	        }
	    }
	};

	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_timer);
		setVolumeControlStream(AudioManager.STREAM_NOTIFICATION);
		
		if (savedInstanceState == null) {
			getSupportFragmentManager().beginTransaction()
					.add(R.id.container, new PlaceholderFragment()).commit();
		}
		
		ActionBar actionBar = getSupportActionBar();
	    actionBar.setDisplayHomeAsUpEnabled(true);
	}

	@Override
	protected void onStart() {
		super.onStart();
		timerTextView = (TextView) findViewById(R.id.seconds_display);
		roundsTextView = (TextView) findViewById(R.id.rounds_display);

		// Get the message from the intent
		Intent intent = getIntent();
		String minutes = intent.getStringExtra(MainActivity.EXTRA_TIMERMINUTES);
		String seconds = intent.getStringExtra(MainActivity.EXTRA_TIMERSECONDS);
		String minutesRest = intent.getStringExtra(MainActivity.EXTRA_TIMERMINUTES_REST);
		String secondsRest = intent.getStringExtra(MainActivity.EXTRA_TIMERSECONDS_REST);
		String rounds = intent.getStringExtra(MainActivity.EXTRA_ROUNDS);
		playSounds = intent.getBooleanExtra(MainActivity.EXTRA_PLAYSOUNDS, true);
		playHalftimeSounds = intent.getBooleanExtra(MainActivity.EXTRA_PLAYHALFTIMESOUND, true);

		if (timerLogic == null) {
			timerLogic = new TimerLogic(minutes, seconds, minutesRest, secondsRest, rounds);
			timerLogic.addObserver(this);
		}
		updateTimerDisplay();
		
		timerLogic.runTimer();
		handler.postDelayed(updateTimerRunner, SCREEN_UPDATE_INTERVAL);
	}

	/**
	 * switch between paused and active mode. Paused stops the timer in it's current state and also stops screen refreshes.
	 * Resume restarts it and resumes the updating of the screen.
	 * 
	 * @param view
	 */
	public void toggleTimer(View view) {
		Button toggleButton = (Button) findViewById(R.id.button_toggletimer);
		
		if (timerLogic.isPaused()) {
			timerLogic.resumeTimer();
			timerLogic.runTimer();
			handler.postDelayed(updateTimerRunner, SCREEN_UPDATE_INTERVAL);
			toggleButton.setText(R.string.button_caption_pause);
		    if(timerLogic.getSecondsLeft() == timerLogic.getInitialSeconds()) {
				playSound(R.raw.boxing_bell);
		    }
		}
		else {
			timerLogic.pauseTimer();
			toggleButton.setText(R.string.button_caption_resume);
			handler.removeCallbacks(updateTimerRunner);
		}
	}
	
	
	/**
	 * button action for resetting the timer (reset start time and round counter)
	 * 
	 * @param view
	 */
	public void resetTimer(View view) {
		timerLogic.resetTimer();
		updateTimerDisplay();
	}
	

	/**
	 * repaint the timer screen: remaining time, color of remaining time, round counter
	 */
	private void updateTimerDisplay() {
		secondsLeft = timerLogic.getSecondsLeft();
	    timerTextView.setText(String.format("%02d", secondsLeft / 60) + ":" + String.format("%02d", secondsLeft%60));
	    
	    if (timerLogic.getCurrentRound() == 0) {
	    	timerTextView.setTextColor(getResources().getColor(R.color.timer_grey));
	    }
	    else if (!timerLogic.isRestMode() ) {
	    	timerTextView.setTextColor(getResources().getColor(R.color.timer_green));
	    }
	    else if (timerLogic.getInitialSecondsRest() > 0) {
	    	timerTextView.setTextColor(getResources().getColor(R.color.timer_red));
	    }
	    
	    // show round display depending on mode (countdown, round, rest)
	    if (timerLogic.getCurrentRound() > 0) {
	    	if (timerLogic.isRestMode()) {
	    		roundDisplay = getResources().getString(R.string.label_rest);
	    	}
	    	else {
	    		roundDisplay = getResources().getString(R.string.label_current_round);
	    	}
	    	roundsTextView.setText(roundDisplay + " " + Long.valueOf(timerLogic.getCurrentRoundForDisplay())+"/"+Long.valueOf(timerLogic.getMaxRounds()));
	    }
	    else {
	    	roundsTextView.setText(R.string.label_countdown);
	    }
	}
	
	
	/**
	 * Play the sound defined by it's resource ID if the instance variable playSounds is set to true.
	 * 
	 * @param soundId
	 */
	public void playSound(int soundId){
		
		if (!playSounds)
			return;
		
		AudioManager am = (AudioManager) this.getSystemService(Context.AUDIO_SERVICE);
		
		// Request audio focus for playback
		int result = am.requestAudioFocus(afChangeListener,
		                             // Use the music stream.
		                             AudioManager.STREAM_MUSIC,
		                             // Request permanent focus.
		                             AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK);
		   
		if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
			MediaPlayer mediaPlayer = MediaPlayer.create(this, soundId);
			mediaPlayer.start();
			am.abandonAudioFocus(afChangeListener);
		}
		
		
	}
	
	/* 
	 * Called by timerlogic at events like round begin, round end, halftime reached
	 * @see java.util.Observer#update(java.util.Observable, java.lang.Object)
	 */
	public void update(Observable obj, Object arg) {
		
        if (arg instanceof String) {
            eventType = (String) arg;
            if (TimerLogic.TIMER_EVENT_ACTIVE_TIME_FINISHED.equals(eventType)) {
            	playSound(R.raw.boxing_bell_multiple);
            }
            else if (TimerLogic.TIMER_EVENT_BEGIN_ROUND.equals(eventType)) {
            	playSound(R.raw.boxing_bell);
            }
            else if(TimerLogic.TIMER_EVENT_HALFTIME_REACHED.equals(eventType) && playHalftimeSounds) {
            	playSound(R.raw.halftime_bell);
            }
        }
        
    }
	
	
    @Override
    public void onPause() {
    	super.onPause();
    	handler.removeCallbacks(updateTimerRunner);
    }
    
    /* 
     * called when closing the activity. Makes sure to discard the timer Logic object as it otherwise
     * continue to run in the background
     * @see android.app.Activity#finish()
     */
    @Override
    public void finish() {
    	
    	discardTimerLogic();
    	super.finish();
    }

    
    
	/**
	 * clean up timerLogic instance and delete obervers, so that 
	 * instanciated timers don't create events and play sounds
	 */
	private void discardTimerLogic() {
		if (timerLogic != null) {
    		timerLogic.deleteObservers();
    		timerLogic.pauseTimer();
    		timerLogic = null;
    	}
    	
    	handler.removeCallbacks(updateTimerRunner);
	}	
    
    /* 
     * called when using the back button in the action bar. 
     * @see android.support.v7.app.ActionBarActivity#onSupportNavigateUp()
     */
    @Override
    public boolean onSupportNavigateUp() {
    	finish();
    	return true;
    }
    
    
    /**
     * Runnable for updating the timer display continuously
     */
    final Runnable updateTimerRunner = new Runnable()
    {
        public void run() 
        {
            updateTimerDisplay();
        	handler.postDelayed(this, SCREEN_UPDATE_INTERVAL);
        }
    };
    
   
    
    @Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.timer, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		MainMenuLogic.processMenuSelection(item, this);
		return super.onOptionsItemSelected(item);
	}

	/**
	 * A placeholder fragment containing a simple view.
	 */
	public static class PlaceholderFragment extends Fragment {

		public PlaceholderFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_timer,
					container, false);
			return rootView;
		}
	}
	
	

}
