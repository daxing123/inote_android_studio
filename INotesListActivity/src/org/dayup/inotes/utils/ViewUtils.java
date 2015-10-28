package org.dayup.inotes.utils;

import java.net.URI;
import java.net.URISyntaxException;

import android.text.TextUtils;
import android.widget.TextView;

public class ViewUtils {
    public static boolean isTextViewNotEmpty(TextView v) {
        if (v == null) {
            return false;
        }
        return !TextUtils.isEmpty(v.getText());
    }

    public static boolean isServerNameValid(TextView view) {
        return isServerNameValid(view.getText().toString());
    }

    public static boolean isServerNameValid(String serverName) {
        serverName = serverName.trim();
        if (TextUtils.isEmpty(serverName)) {
            return false;
        }
        try {
            URI uri = new URI("http", null, serverName, -1, null, // path
                    null, // query
                    null);
            return true;
        } catch (URISyntaxException e) {
            return false;
        }
    }

    public static boolean isPortFieldValid(TextView view) {
        CharSequence chars = view.getText();
        if (TextUtils.isEmpty(chars))
            return false;
        Integer port;
        // In theory, we can't get an illegal value here, since the field is
        // monitored for valid
        // numeric input. But this might be used elsewhere without such a check.
        try {
            port = Integer.parseInt(chars.toString());
        } catch (NumberFormatException e) {
            return false;
        }
        return port > 0 && port < 65536;
    }

}
