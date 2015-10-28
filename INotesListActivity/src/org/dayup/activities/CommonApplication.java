package org.dayup.activities;

import org.dayup.common.Communication;
import org.dayup.common.DialogNotes;

import android.app.Application;

public abstract class CommonApplication extends Application {

    protected Communication communication;

    protected DialogNotes dialogNotes;

    public volatile int activeActivities = 0;

    public Communication getCommunication() {
        return communication;
    }

    public DialogNotes getDialogNotes() {
        return dialogNotes;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        initCommunication();
        initDialogNotes();
        if (communication != null) {
            communication.pull();
        }
    }

    protected abstract void initCommunication();

    protected abstract void initDialogNotes();

    public abstract int getOptionMenuHelpStringId();
}
