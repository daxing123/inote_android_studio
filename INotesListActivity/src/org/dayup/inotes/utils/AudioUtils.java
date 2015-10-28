package org.dayup.inotes.utils;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.speech.RecognizerIntent;

public class AudioUtils {
    /**
     * Check to see if a recognition activity is present
     * 
     * @param context
     * @return
     */
    public static boolean checkRecAvailable(Context context) {
        PackageManager pm = context.getPackageManager();
        return pm.queryIntentActivities(new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH), 0)
                .size() > 0;
    }

}
