package com.alex.alexfirstapp;

import android.support.v7.app.ActionBarActivity;
import android.support.v4.app.Fragment;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

public class MainActivity extends ActionBarActivity {
	
    private static final String SELECTED_SECONDS_ITEM = "selectedSeconds";
	private static final String SELECTED_MINUTES_ITEM = "selectedMinutes";
	public final static String EXTRA_TIMERSECONDS = "com.alex.alexfirstapp.SECONDS";
    public final static String EXTRA_TIMERMINUTES = "com.alex.alexfirstapp.MINUTES";
	private static final String TIMER_SETTINGS = "timerSettings";
    private Spinner minutesSpinner;
    private Spinner secondsSpinner;


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
	    int selectedMinutesItem = Integer.valueOf(settings.getString(SELECTED_MINUTES_ITEM, "0")).intValue();
	    int selectedSecondsItem = Integer.valueOf(settings.getString(SELECTED_SECONDS_ITEM, "0")).intValue();
		
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
		
		intent.putExtra(EXTRA_TIMERMINUTES, minutes);
		intent.putExtra(EXTRA_TIMERSECONDS, seconds);

		startActivity(intent);
	}
	
	@Override
	public void onPause() {
		super.onPause();
		
		// save settings
		SharedPreferences settings = getSharedPreferences(TIMER_SETTINGS, 0);
	    SharedPreferences.Editor editor = settings.edit();
	    editor.putString(SELECTED_MINUTES_ITEM, Integer.toString(minutesSpinner.getSelectedItemPosition()));
	    editor.putString(SELECTED_SECONDS_ITEM, Integer.toString(minutesSpinner.getSelectedItemPosition()));
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
