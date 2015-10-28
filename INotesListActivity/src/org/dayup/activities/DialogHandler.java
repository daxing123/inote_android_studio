package org.dayup.activities;

import android.app.Dialog;

public abstract class DialogHandler {
    protected final BaseActivity activity;
    private int id = -1;
    private Dialog dialog = null;

    public Dialog getDialog() {
        return dialog;
    }

    public DialogHandler(BaseActivity activity) {
        this.activity = activity;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void show() {
        if (id == -1) {
            activity.addDialogHandler(this);
        }
        activity.showDialog(getId());
    }

    public void hide() {
        activity.dismissDialog(getId());
    }

    public abstract Dialog onCreateDialog();

    public void onPrepareDialog(Dialog dialog) {
        this.dialog = dialog;
    }

    public boolean isShowing() {
        if (dialog != null) {
            return dialog.isShowing();
        }
        return false;
    }

    public void remove() {
        if (id != -1) {
            activity.removeDialog(id);
        }
    }
}
