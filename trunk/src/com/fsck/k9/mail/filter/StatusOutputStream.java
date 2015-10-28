package com.fsck.k9.mail.filter;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import android.util.Config;
import android.util.Log;

public class StatusOutputStream extends FilterOutputStream {
	
	private static final String TAG = StatusOutputStream.class.getSimpleName();
    private long mCount = 0;

    public StatusOutputStream(OutputStream out) {
        super(out);
    }

    @Override
    public void write(int oneByte) throws IOException {
        super.write(oneByte);
        mCount++;
        if (Config.LOGV) {
            if (mCount % 1024 == 0) {
                Log.v(TAG, "# " + mCount);
            }
        }
    }
}
