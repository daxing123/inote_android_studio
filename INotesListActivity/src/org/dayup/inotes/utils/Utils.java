package org.dayup.inotes.utils;

import java.util.UUID;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.view.ActionMode;
import android.widget.Toast;

public class Utils {

    public static String getRandomUUID36() {
        return UUID.randomUUID().toString();
    }


    public static void startUnknowActivity(Context context, Intent intent, int toast) {
        try {
            context.startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(context, toast, Toast.LENGTH_SHORT).show();
        }
    }

}
