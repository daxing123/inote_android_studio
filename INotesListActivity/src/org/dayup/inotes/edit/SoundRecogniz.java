package org.dayup.inotes.edit;

import org.dayup.inotes.constants.Constants.RequestCode;

import android.content.Intent;
import android.speech.RecognizerIntent;
import android.view.View;
import android.view.View.OnClickListener;

public class SoundRecogniz implements OnClickListener {
    private final INoteDetailStartActivity callBack;

    public SoundRecogniz(INoteDetailStartActivity callBack) {
        this.callBack = callBack;
    }

    @Override
    public void onClick(View v) {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        callBack.startActivityForResult(intent, RequestCode.REQUEST_CODE_VOICE_RECOGNITION);
    }
}
