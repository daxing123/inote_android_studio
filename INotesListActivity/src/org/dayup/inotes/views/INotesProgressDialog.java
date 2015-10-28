package org.dayup.inotes.views;

import org.dayup.inotes.R;
import org.dayup.inotes.constants.Constants.Themes;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;

public class INotesProgressDialog extends ProgressDialog {

	public INotesProgressDialog(Context context,int theme) {
		super(context, theme == Themes.THEME_LIGHT ? R.style.INotesDialog_Light
                : R.style.INotesDialog);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.progress_dialog);
		
		WindowManager m = this.getWindow().getWindowManager();
		Display d = m.getDefaultDisplay();
		android.view.WindowManager.LayoutParams p = this.getWindow()
				.getAttributes();
		p.width = (int) ((d.getWidth() < d.getHeight() ? d.getWidth() : d
				.getHeight()) * 0.92);
		getWindow().setAttributes(p);
		LayoutInflater inflater = getLayoutInflater();
		View view = inflater.inflate(R.layout.progress_dialog, null);
		this.setView(view);
		this.setOnShowListener(new OnShowListener() {

			@Override
			public void onShow(DialogInterface dialog) {
			}
		});
	}
}
