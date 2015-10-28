package org.dayup.inotes.edit;

import android.view.View;

public interface INoteEditController extends INoteDetailStartActivity, INoteSwitch {
    void onAttachCountClickListener(View v);
}
