package com.alex.circuitTimer.ui;

import android.support.v7.app.ActionBarActivity;
import android.view.MenuItem;

import com.alex.circuitTimer.R;

public class MainMenuLogic {
	
	public static void processMenuSelection(MenuItem item, ActionBarActivity currentActivity) {
	// Handle action bar item clicks here. The action bar will
			// automatically handle clicks on the Home/Up button, so long
			// as you specify a parent activity in AndroidManifest.xml.
			int id = item.getItemId();
			if (id == R.id.action_exit || id == R.id.action_settings) {
				currentActivity.finish();
			}
			if (id == R.id.action_about) {
				AboutAlertFragment alert = new AboutAlertFragment();
				alert.show(currentActivity.getSupportFragmentManager(), "about dialog");
			}
	}
}
