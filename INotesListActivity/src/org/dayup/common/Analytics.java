package org.dayup.common;

import java.util.Map;

import android.content.Context;

import com.flurry.android.FlurryAgent;

/**
 * @author Nicky
 * 
 */
public class Analytics {

    public final static String FLURRY_ID = "5H9GTXGCZRTP5JFWXX3W";

    public static void startFlurry(Context context) {
        FlurryAgent.setLogEnabled(false);
        FlurryAgent.onStartSession(context, Analytics.FLURRY_ID);
    }

    public static void endFlurry(Context context) {
        FlurryAgent.onEndSession(context);
    }

    public static void onEvent(String eventId, Map<String, String> parameters) {
        FlurryAgent.onEvent(eventId, parameters);
    }

}
