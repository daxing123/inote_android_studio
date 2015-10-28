package org.dayup.common;

import java.util.Locale;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Handler;
import android.util.Log;
import android.util.TypedValue;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;

public class DialogNotes {
	public static final String TAG = DialogNotes.class.getSimpleName();
	private Communication communication;
	
	private int dialog_note_title_id;
	private int dialog_note_close_id;
	private int dialog_note_dont_show_again_id;
	
	private Handler handler = new Handler();
	
	private String locale;
	
	public DialogNotes(Communication communication) {
		this.communication = communication;
		Locale locale = communication.getContext().getResources().getConfiguration().locale;
		if (locale.getLanguage().length() > 0 && !locale.equals(Locale.ENGLISH)) {
			this.locale = "_" + locale.getLanguage() + "_" + locale.getCountry();
		}
		
	}

	public void setDialog_note_close_id(int dialog_note_close_id) {
		this.dialog_note_close_id = dialog_note_close_id;
	}
	
	public void setDialog_note_dont_show_again_id(int dialog_note_dont_show_again_id) {
		this.dialog_note_dont_show_again_id = dialog_note_dont_show_again_id;
	}
	
	public void setDialog_note_title_id(int dialog_note_title_id) {
		this.dialog_note_title_id = dialog_note_title_id;
	}
	
	public void showFirstDialogForActivity(final Activity activity, final String... ids) {
		handler.postDelayed(new Runnable(){

			@Override
			public void run() {
				for (String id : ids) {
					if (showDialog(id, activity)) {
						return;
					}
				}	
				for (int i = 1; i <= 3; i++) {
					String id = activity.getClass().getSimpleName() + "_" + i;
					if (showDialog(id, activity)) {
						return;
					}
				}
			}}, 1000);
		
	}
	
	public boolean showDialog(String id, final Activity activity) {
		final JSONObject config = communication.getConfig(id);
		if (config == null) {
//			Log.d(TAG, "Can't get note for " + id);
			return false;
		}
		try {
			if (config.has("show") && !config.getBoolean("show")) {
				return false;
			}
			
			CharSequence title = config.has("title") ? config.getString("title") : getActivityText(activity,dialog_note_title_id);
			final AlertDialog dialog =  new AlertDialog.Builder(activity).setTitle(title).create();
			CharSequence positiveButtonName = getActivityText(activity,dialog_note_close_id);;
			
			if (config.has("button1_name")) {
				positiveButtonName = config.getString("button1_name");	
			}
			DialogInterface.OnClickListener positiveButtonListener = null;
			if (config.has("button1_url")) {
				positiveButtonListener = new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						Intent intent;
						try {
							String action = Intent.ACTION_VIEW;
							if(config.has("action_view")){
								action = config.getString("action_view");	
							}
							intent = new Intent(action, Uri.parse(config.getString("button1_url")));
							activity.startActivity(intent);
						} catch (JSONException e) {
						}
					}
					
				};
			}
			
			dialog.setButton(DialogInterface.BUTTON_POSITIVE, positiveButtonName, positiveButtonListener);
			if (config.has("button2_name")) {
				DialogInterface.OnClickListener button2Listener = null;
				if (config.has("button2_url")) {
					button2Listener = createOnClickListener(activity, config, "button2_url");
				}
				dialog.setButton(DialogInterface.BUTTON_NEUTRAL, config.getString("button2_name"), button2Listener);
			}
			
			
			if (config.has("button3_name")) {
				DialogInterface.OnClickListener button3Listener = null;
				if (config.has("button3_url")) {
					button3Listener = createOnClickListener(activity, config, "button3_url");
				}
				dialog.setButton(DialogInterface.BUTTON_NEGATIVE, config.getString("button3_name"), button3Listener);
				
			} else {
			
    			dialog.setButton(DialogInterface.BUTTON_NEGATIVE, getActivityText(activity,dialog_note_dont_show_again_id), new DialogInterface.OnClickListener(){
    
    				@Override
    				public void onClick(DialogInterface dialog, int which) {
    					try {
    						config.put("show", false);
    						communication.saveConfig(config);
    					} catch (JSONException e) {
    						// ignore
    					}
    					dialog.dismiss();
    				}});
			}
			
			if (config.has("content")) {
				TextView textView = new TextView(activity);
				textView.setPadding(10, 0, 0, 0);
				int size = config.has("text_size") ? config.getInt("text_size") : 16;
				textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, size);
				textView.setText(config.has("content" + locale) ? config.getString("content" + locale) : config.getString("content"));
				
				int color = config.has("text_color") ? (Integer.parseInt(config.getString("text_color"), 16) | 0xff000000) : 0xffffffff;
				textView.setTextColor(color);
				dialog.setView(textView);
				dialog.show();
			} else {
				WebView webView = new WebView(activity);
				webView.loadUrl(config.getString("content_url"));
				webView.setWebViewClient(new WebViewClient(){
					private boolean error = false;
					
					@Override
					public void onPageStarted(WebView view, String url, Bitmap favicon) {
						super.onPageStarted(view, url, favicon);
						//Log.d(TAG, "onPageStarted " + url);
					}
					
					@Override
					public void onPageFinished(WebView view, String url) {
						super.onPageFinished(view, url);
						//Log.d(TAG, "onPageFinished " + url);
						if (!error) {
							dialog.show();
						}
					}
					
					@Override
					public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
						super.onReceivedError(view, errorCode, description, failingUrl);
						//Log.d(TAG, "onReceivedError " + errorCode + ", " + description + ", " + failingUrl);
						error = true;
						try {
							config.put("show", false);
							communication.saveConfig(config);
						} catch (JSONException e) {
							//ignore
						}
						
						
					}
				});
				dialog.setView(webView);
			}
			return true;
		} catch (Throwable e) {
			Log.e(TAG, "Can't show dialog for " + id, e);
			return false;
		}
		
	}
	
	private CharSequence getActivityText(Activity activity,int id){
		CharSequence text = null;
		try{
			text = activity.getText(id);
		}catch (Exception e) {
			
		}
		return text;
	}
	
	private DialogInterface.OnClickListener createOnClickListener(final Activity activity, final JSONObject config, final String urlKey) {
		DialogInterface.OnClickListener button2Listener;
		button2Listener = new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				Intent intent;
				try {
					intent = new Intent(Intent.ACTION_VIEW, Uri.parse(config.getString(urlKey)));
					activity.startActivity(intent);
				} catch (JSONException e) {
				}
				
			}
			
		};
		return button2Listener;
	}
}
