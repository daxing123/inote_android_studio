package org.dayup.inotes.utils;

import org.dayup.inotes.R;

import android.content.Context;
import android.content.Intent;

public class ShareUtils {
    private Context context;

    public ShareUtils(Context context) {
        this.context = context;
    }

    public void shareNotes(String content, int count) {
        Intent i = new Intent();
        i.setAction(Intent.ACTION_SEND);
        i.setType("text/plain");
        if (count > 1) {
            i.putExtra(Intent.EXTRA_SUBJECT, count + context.getString(R.string.bacth_share_notes));
        }
        i.putExtra(Intent.EXTRA_TEXT, content.toString());
        Utils.startUnknowActivity(context, Intent.createChooser(i, "Share"),
                R.string.msg_can_t_share);
    }

    public void shareNote(String content) {
        Intent i = new Intent();
        i.setAction(Intent.ACTION_SEND);
        i.setType("text/plain");
        i.putExtra(Intent.EXTRA_TEXT, content.toString());
        i.putExtra("sms_body", content);
        Utils.startUnknowActivity(context, Intent.createChooser(i, "Share"),
                R.string.msg_can_t_share);
    }
}
