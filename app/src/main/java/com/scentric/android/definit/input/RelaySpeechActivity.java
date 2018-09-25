package com.scentric.android.definit.input;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.scentric.android.definit.R;
import com.scentric.android.definit.show.SearchAndShowActivity;

import java.util.ArrayList;
import java.util.Locale;

/**
 * Created by Steven on 8/21/2016.
 */
public class RelaySpeechActivity extends Activity {
    public final static int REQ_CODE_SPEECH_INPUT = 92;
    LinearLayout linearLayout;
    View view;
    boolean hasReocgnized = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_relay);

        linearLayout = (LinearLayout) findViewById(R.id.relay_linear);

        linearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.e("relay", "ll clicked");
                finish();
            }
        });

        recognizeSpeech();
    }

    public void recognizeSpeech() { // only called from notification action
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak now");
        try {
            startActivityForResult(intent, REQ_CODE_SPEECH_INPUT);
        } catch (ActivityNotFoundException a) {
            Toast.makeText(getApplicationContext(), "Oops. Speech recognition is not supported on this device.",
                    Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    public void onResume() {
        Log.e("speechlog", "onResume...");
        if (hasReocgnized) {
            finish();
        }
        super.onResume();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Log.e("speechlog", "onActivityResult... " + requestCode + " " + resultCode);

        switch (requestCode) {
            case REQ_CODE_SPEECH_INPUT: {
                if (resultCode == RESULT_OK && null != data) {

                    hasReocgnized = true;

                    ArrayList<String> result = data
                            .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);

                    Intent intent = new Intent(this, SearchAndShowActivity.class);
                    intent.putExtra(SearchAndShowActivity.SENT_WORD, result.get(0).trim());
                    Log.e("relay", "sending... " + result.get(0).trim());
                    startActivity(intent);
                    finish();
                } else if (resultCode == RESULT_CANCELED /*&& data != null*/) {
                    Log.e("relay", "finishing because user cancelled");
                    finish();
                }
                break;
            }

        }
    }

    @Override
    protected void onDestroy() {
        Log.e("speech", "onDestroy");
        super.onDestroy();
    }
}
