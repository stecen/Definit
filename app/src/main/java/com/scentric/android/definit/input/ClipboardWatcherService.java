package com.scentric.android.definit.input;

import android.app.Service;
import android.content.ClipData;
import android.content.ClipDescription;
import android.content.ClipboardManager;
import android.content.ClipboardManager.OnPrimaryClipChangedListener;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

public class ClipboardWatcherService extends Service {
    static boolean isServiceActive = false;

    private final String tag = "[[ClipboardWatcherService]] ";
    private String previousText = ""; // to prevent multiple events listened to :-\
    private OnPrimaryClipChangedListener listener = new OnPrimaryClipChangedListener() {
        public void onPrimaryClipChanged() {
            performClipboardCheck();
        }
    };

    @Override
    public void onCreate() {
        ((ClipboardManager) getSystemService(CLIPBOARD_SERVICE)).addPrimaryClipChangedListener(listener);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        isServiceActive = true;

        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void performClipboardCheck() {
        if (!isServiceActive)
            return; // for some reason stopping the service won't stop the listener
        ClipboardManager cb = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE); // notifies us when the user copies something to the pasteboard
        if (cb.hasPrimaryClip()) {
            ClipData cd = cb.getPrimaryClip();
            if (cd.getDescription().hasMimeType(ClipDescription.MIMETYPE_TEXT_PLAIN) || cd.getDescription().hasMimeType(ClipDescription.MIMETYPE_TEXT_HTML)) {

                String word = cd.getItemAt(0).coerceToText(this).toString().trim();

                previousText = word;

                Log.e("tag", "Just copied " + word);

                if (word.contains(" ")) {
                    Log.e("tag", "This word + '" + word + "' contains space(s).");
                }

                // todo: make sure only has 1 word, make sure actually exists in dict.

                // call the Messenger-like popup
                Intent popupIntent = new Intent(ClipboardWatcherService.this, FloatingWindowService.class);
                popupIntent.putExtra(FloatingWindowService.KEY_WORD, word);
                startService(popupIntent);

            }
        }
    }

    @Override
    public void onDestroy() {
        isServiceActive = false;
        super.onDestroy();
    }

}