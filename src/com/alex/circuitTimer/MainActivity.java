package com.alex.circuitTimer;

import com.alex.alexfirstapp.R;

import android.support.v7.app.ActionBarActivity;
import android.support.v4.app.Fragment;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.Spinner;

public class MainActivity extends ActionBarActivity {
	
	private static final String SELECTED_SECONDS_REST_ITEM = "selectedRestSeconds";
    private static final String SELECTED_SECONDS_ITEM = "selectedSeconds";
	private static final String SELECTED_MINUTES_ITEM = "selectedMinutes";
	private static final String SELECTED_ROUNDS_ITEM = "selectedRounds";
	private static final String PLAY_SOUNDS = "playSounds";
	private static final String PLAY_HALFTIME_SOUND = "playHalftimeSound";
	public final static String EXTRA_TIMERSECONDS_REST = "com.alex.alexfirstapp.SECONDS_REST";
	public final static String EXTRA_TIMERSECONDS = "com.alex.alexfirstapp.SECONDS";
    public final static String EXTRA_TIMERMINUTES = "com.alex.alexfirstapp.MINUTES";
    public final static String EXTRA_ROUNDS = "com.alex.alexfirstapp.ROUNDS";
    public final static String EXTRA_PLAYSOUNDS = "com.alex.alexfirstapp.PLAYSOUNDS";
    public final static String EXTRA_PLAYHALFTIMESOUND = "com.alex.alexfirstapp.PLAYHALFTIMESOUND";

    private static final String TIMER_SETTINGS = "timerSettings";
    private Spinner minutesSpinner;
    private Spinner secondsSpinner;
    private Spinner secondsRestSpinner;
    private Spinner roundsSpinner;
    private CheckBox playSoundsCheckbox;
    private CheckBox playHalftimeSoundCheckbox;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		if (savedInstanceState == null) {
			getSupportFragmentManager().beginTransaction()
					.add(R.id.container, new PlaceholderFragment()).commit();
		}
	}
	
	@Override
	protected void onStart() {
		super.onStart();
		
		// load last preferences
		SharedPreferences settings = getSharedPreferences(TIMER_SETTINGS, 0);
	    int selectedMinutesItem = Integer.valueOf(settings.getString(SELECTED_MINUTES_ITEM, "1")).intValue();
	    int selectedSecondsItem = Integer.valueOf(settings.getString(SELECTED_SECONDS_ITEM, "0")).intValue();
	    int selectedSecondsRestItem = Integer.valueOf(settings.getString(SELECTED_SECONDS_REST_ITEM, "1")).intValue();
	    int selectedRoundsItem = Integer.valueOf(settings.getString(SELECTED_ROUNDS_ITEM, "9")).intValue();
	    boolean playSounds = settings.getBoolean(PLAY_SOUNDS, true);
	    boolean playHalftimeSound = settings.getBoolean(PLAY_HALFTIME_SOUND, true);
	    
		
		minutesSpinner = (Spinner) findViewById(R.id.minutes_spinner);
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.minutes_array, android.R.layout.simple_spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		minutesSpinner.setAdapter(adapter);
		minutesSpinner.setSelection(selectedMinutesItem);
		
		secondsSpinner = (Spinner) findViewById(R.id.seconds_spinner);
		adapter = ArrayAdapter.createFromResource(this, R.array.seconds_array, android.R.layout.simple_spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		secondsSpinner.setAdapter(adapter);
		secondsSpinner.setSelection(selectedSecondsItem);
		
		secondsRestSpinner = (Spinner) findViewById(R.id.seconds_rest_spinner);
		adapter = ArrayAdapter.createFromResource(this, R.array.seconds_array, android.R.layout.simple_spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		secondsRestSpinner.setAdapter(adapter);
		secondsRestSpinner.setSelection(selectedSecondsRestItem);
		
		roundsSpinner = (Spinner) findViewById(R.id.rounds_spinner);
		adapter = ArrayAdapter.createFromResource(this, R.array.rounds_array, android.R.layout.simple_spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		roundsSpinner.setAdapter(adapter);
		roundsSpinner.setSelection(selectedRoundsItem);
		
		playSoundsCheckbox = (CheckBox) findViewById(R.id.checkbox_play_sounds);
		playSoundsCheckbox.setChecked(playSounds);
	
		playHalftimeSoundCheckbox = (CheckBox) findViewById(R.id.checkbox_play_halftime_sound);
		playHalftimeSoundCheckbox.setChecked(playHalftimeSound);

	}


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
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
	
	
	
	/** Called when the user clicks the start button */
	public void startTimer(View view) {
		Intent intent = new Intent(this, TimerActivity.class);
		
		String minutes = (String) minutesSpinner.getItemAtPosition(minutesSpinner.getSelectedItemPosition());
		String seconds = (String) secondsSpinner.getItemAtPosition(secondsSpinner.getSelectedItemPosition());
		String secondsRest = (String) secondsRestSpinner.getItemAtPosition(secondsRestSpinner.getSelectedItemPosition());
		String rounds = (String) roundsSpinner.getItemAtPosition(roundsSpinner.getSelectedItemPosition());
		boolean playSounds = playSoundsCheckbox.isChecked();
		boolean playHalftimeSound = playHalftimeSoundCheckbox.isChecked();
		
//		if("0".equals(minutes) && "0".equals(seconds)) {
//			AlertDialog.Builder builder = new AlertDialog.Builder(this);
//			builder.setMessage("minimum time is 10 seconds").setTitle("validation error");
//			builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
//				public void onClick(DialogInterface dialog, int id) {
//					return;
//				}
//			});
//			AlertDialog dialog = builder.create();
//
//		}
		
		
		intent.putExtra(EXTRA_TIMERMINUTES, minutes);
		intent.putExtra(EXTRA_TIMERSECONDS, seconds);
		intent.putExtra(EXTRA_TIMERSECONDS_REST, secondsRest);
		intent.putExtra(EXTRA_ROUNDS, rounds);
		intent.putExtra(EXTRA_PLAYSOUNDS, playSounds);
		intent.putExtra(EXTRA_PLAYHALFTIMESOUND, playHalftimeSound);
		
		startActivity(intent);
	}
	
	@Override
	public void onPause() {
		super.onPause();
		
		// save settings
		SharedPreferences settings = getSharedPreferences(TIMER_SETTINGS, 0);
	    SharedPreferences.Editor editor = settings.edit();
	    editor.putString(SELECTED_MINUTES_ITEM, Integer.toString(minutesSpinner.getSelectedItemPosition()));
	    editor.putString(SELECTED_SECONDS_ITEM, Integer.toString(secondsSpinner.getSelectedItemPosition()));
	    editor.putString(SELECTED_SECONDS_REST_ITEM, Integer.toString(secondsRestSpinner.getSelectedItemPosition()));
	    editor.putString(SELECTED_ROUNDS_ITEM, Integer.toString(roundsSpinner.getSelectedItemPosition()));
	    editor.putBoolean(PLAY_SOUNDS, playSoundsCheckbox.isChecked());
	    editor.putBoolean(PLAY_HALFTIME_SOUND, playHalftimeSoundCheckbox.isChecked());

	    editor.commit();
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
			View rootView = inflater.inflate(R.layout.fragment_main, container,
					false);
			return rootView;
		}
	}
	
	

}
