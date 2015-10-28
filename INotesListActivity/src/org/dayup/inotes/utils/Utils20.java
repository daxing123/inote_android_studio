package org.dayup.inotes.utils;

import android.app.Activity;
import android.content.Intent;

/**
 * need version 2.0 +
 * 
 * @author jackwang
 * 
 */
public class Utils20 {
    public static final int FLAG_ACTIVITY_NO_ANIMATION = Intent.FLAG_ACTIVITY_NO_ANIMATION;

    public static void overridePendingTransition(Activity activity, int enterAnim, int exitAnim) {
        activity.overridePendingTransition(enterAnim, exitAnim);
    }
}
