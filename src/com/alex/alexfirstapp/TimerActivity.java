package com.alex.alexfirstapp;


import java.util.Observable;
import java.util.Observer;

import com.alex.alexfirstapp.logic.TimerLogic;

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

	private TimerLogic timerLogic = null;
	private TextView timerTextView;
	private TextView roundsTextView;
    private Handler timerHandler = new Handler();
    boolean playSounds = true;
    boolean playHalftimeSounds = true;

    
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
		
		if (savedInstanceState == null) {
			getSupportFragmentManager().beginTransaction()
					.add(R.id.container, new PlaceholderFragment()).commit();
		}
		
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
		String secondsRest = intent.getStringExtra(MainActivity.EXTRA_TIMERSECONDS_REST);
		String rounds = intent.getStringExtra(MainActivity.EXTRA_ROUNDS);
		playSounds = intent.getBooleanExtra(MainActivity.EXTRA_PLAYSOUNDS, true);
		playHalftimeSounds = intent.getBooleanExtra(MainActivity.EXTRA_PLAYHALFTIMESOUND, true);

		timerLogic = new TimerLogic(minutes, seconds, secondsRest, rounds);
		playSound(R.raw.boxing_bell);
		timerHandler.postDelayed(timerRunnable, 0);

	}

	public void toggleTimer(View view) {
		Button toggleButton = (Button) findViewById(R.id.button_toggletimer);
		
		if (timerLogic.isPaused()) {
			timerLogic.resumeTimer();
		    timerHandler.postDelayed(timerRunnable, 0);
		    toggleButton.setText(R.string.button_caption_pause);
		    if(timerLogic.getSecondsLeft() == timerLogic.getInitialSeconds()) {
				playSound(R.raw.boxing_bell);
		    }
		}
		else {
			timerLogic.pauseTimer();
			timerHandler.removeCallbacks(timerRunnable);
			toggleButton.setText(R.string.button_caption_resume);
		}
	}
	
	public void resetTimer(View view) {
		timerLogic.resetTimer();
		updateTimerDisplay();
		if (!timerLogic.isPaused()) {
			timerHandler.postDelayed(timerRunnable, 0);
		}
	}
	

	private void updateTimerDisplay() {
		updateTimerDisplay(timerLogic);
	}
	
	private void updateTimerDisplay(TimerLogic timerLogic) {
		long secondsLeft = timerLogic.getSecondsLeft();
	    timerTextView.setText(String.format("%02d", secondsLeft / 60) + ":" + String.format("%02d", secondsLeft%60));
	    
	    if (!timerLogic.isRestMode()) {
	    	timerTextView.setTextColor(getResources().getColor(R.color.timer_green));
	    }
	    else {
	    	timerTextView.setTextColor(getResources().getColor(R.color.timer_red));
	    }
	    Resources res = getResources();
	    String currentRound = res.getString(R.string.label_current_round) + " " + Long.valueOf(timerLogic.getCurrentRound());
		roundsTextView.setText(currentRound);
	}
	
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
	
	public void update(Observable obj, Object arg) {
        if (arg instanceof String) {
            String eventType = (String) arg;
            if (timerLogic.TIMER_EVENT_ACTIVE_TIME_FINISHED.equals(eventType)) {
            	playSound(R.raw.boxing_bell_multiple);
            }
            else if (timerLogic.TIMER_EVENT_BEGIN_ROUND.equals(eventType)) {
            	playSound(R.raw.boxing_bell);
            }
            else if(timerLogic.TIMER_EVENT_HALFTIME_REACHED.equals(eventType)) {
            	playSound(R.raw.boxing_bell);
            }
        }
    }
	
	
    Runnable timerRunnable = new Runnable() {

        @Override
        public void run() {
        	long secondsLeft = timerLogic.getSecondsLeft();
    	    
    	    if (secondsLeft > 0) {
    	    	timerHandler.postDelayed(this, 100);
    	    }
    	    else {
    		    if (!timerLogic.isRestMode()) {
    		    	playSound(R.raw.boxing_bell_multiple);
    		    }
    		    if (!timerLogic.isTimerFinished()) {
    		    	timerHandler.postDelayed(this, 100);
    		    }
    	    }
    	    
    	    updateTimerDisplay(timerLogic);
        }
    };
    

    @Override
    public void onPause() {
          super.onPause();
          timerHandler.removeCallbacks(timerRunnable);
    }	
    
    
    /* here comes the boilerplate code */
    
    @Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.timer, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
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
