package com.steven.android.vocabkeepernew.input;

import android.app.IntentService;
import android.content.ClipData;
import android.content.ClipDescription;
import android.content.ClipboardManager;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.steven.android.vocabkeepernew.useless.DisplayDefinitionPopupActivity;

/**
 * Created by Steven on 8/17/2016.
 */
public class ClipboardInputService extends IntentService { // from the notification.
    public ClipboardInputService() {
        super("ClipboardInputService");
    }

    @Override
    public void onHandleIntent(Intent intent) {
        Log.e("clipinput","handling...");
        ClipboardManager cb = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        if (cb.hasPrimaryClip()) {
            ClipData cd = cb.getPrimaryClip();
            if (cd.getDescription().hasMimeType(ClipDescription.MIMETYPE_TEXT_PLAIN) || cd.getDescription().hasMimeType(ClipDescription.MIMETYPE_TEXT_HTML)) {
                String word = cd.getItemAt(0).coerceToText(this).toString().trim();

                Intent popupIntent = new Intent(ClipboardInputService.this, DisplayDefinitionPopupActivity.class);
                popupIntent.putExtra(DisplayDefinitionPopupActivity.SENT_WORD, word);
                popupIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(popupIntent);

                Intent it = new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS); // close notification panels
                sendBroadcast(it);
            } else {
                notText();
            }
        } else {
            notText();
        }
    }

    public void notText() {
        Toast.makeText(this, "There's no text on your clipboard!", Toast.LENGTH_SHORT).show();
    }

}
