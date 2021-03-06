package com.alex.circuitTimer.ui;

import com.alex.circuitTimer.R;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.Html;

public class AboutAlertFragment extends DialogFragment {

	@Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setMessage(Html.fromHtml(getResources().getString(R.string.about)));
		builder.setTitle(getResources().getString(R.string.title_about));
		builder.setPositiveButton(getResources().getString(R.string.button_caption_ok), new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				return;
			}
		});
		AlertDialog dialog = builder.create();
		return dialog;
    }
	
}
