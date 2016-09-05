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
//        Toast.makeText(this, "OnStartCommand called...", Toast.LENGTH_SHORT).show();
        isServiceActive=true;

        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void performClipboardCheck() {
        if (!isServiceActive) return; // for some reason stopping the service won't stop the listener


//        Toast.makeText(this, "Performing clipboard check ...", Toast.LENGTH_SHORT).show();
        ClipboardManager cb = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        if (cb.hasPrimaryClip()) {
            ClipData cd = cb.getPrimaryClip();
            if (cd.getDescription().hasMimeType(ClipDescription.MIMETYPE_TEXT_PLAIN) || cd.getDescription().hasMimeType(ClipDescription.MIMETYPE_TEXT_HTML)) {

                String word = cd.getItemAt(0).coerceToText(this).toString().trim();
//                if (previousText.equals(word)) {
//                    return; // don't duplicate
//                }


                previousText = word;

                Log.e("tag", "Kyle Landry just copied " + word);

                if (word.contains(" ")) {
                    Log.e("tag", "This word + '" + word + "' contains space(s).");
                } else {
//                    Toast.makeText(this, "Getting definition for " + word, Toast.LENGTH_SHORT).show();
                }

                // todo: make sure only has 1 word, make sure actually exists in dict.
//                Intent vocabServiceIntent = new Intent(this, VocabService.class);
//                vocabServiceIntent.putExtra(VocabService.GET_FROM_LOCATION, VocabService.GET_FROM_SENT);
//                vocabServiceIntent.putExtra(VocabService.SENT_WORD, word);
//                vocabServiceIntent.putExtra(VocabService.SHOW_POPUP, true);
//                startService(vocabServiceIntent);


                Intent popupIntent = new Intent(ClipboardWatcherService.this, FloatingWindowService.class);
                popupIntent.putExtra(FloatingWindowService.KEY_WORD, word.toLowerCase());
                startService(popupIntent);


//                // start popup
//                Intent relayIntent = new Intent(this, RelayActivity.class);
//                relayIntent.putExtra(DisplayDefinitionPopupActivity.SENT_WORD, word);
//                relayIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                startActivity(relayIntent);

            }
        }
    }

    @Override
    public void onDestroy() {
        isServiceActive=false;
        Toast.makeText(this, "Stopping service...", Toast.LENGTH_SHORT).show();
        super.onDestroy();
    }

}