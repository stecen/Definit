package com.steven.android.vocabkeepernew.input;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.widget.Toast;

import com.steven.android.vocabkeepernew.R;
import com.steven.android.vocabkeepernew.show.SearchAndShowActivity;

import java.util.ArrayList;
import java.util.Locale;

/**
 * Created by Steven on 8/21/2016.
 */
public class RelaySpeechActivity extends ActionBarActivity {
    public final static int REQ_CODE_SPEECH_INPUT = 92;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_relay);

//        final Handler handler = new Handler();
//        handler.postDelayed(new Runnable() {
//            @Override
//            public void run() {
                recognizeSpeech();
//            }
//        }, 900);

    }

    public void recognizeSpeech() { // only called from notification action
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Say something!");
        try {
            startActivityForResult(intent, REQ_CODE_SPEECH_INPUT);
        } catch (ActivityNotFoundException a) {
            Toast.makeText(getApplicationContext(), "Oops. Speech recognition is not supported on this device.",
                    Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Log.e("speechlol", "onActivityResult...");

        switch (requestCode) {
            case REQ_CODE_SPEECH_INPUT: {
                if (resultCode == RESULT_OK && null != data) {

                    ArrayList<String> result = data
                            .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);

//                    String newString = wordEdit.getText().toString() + result.get(0);
//                    wordEdit.setText(result.get(0));
//                    wordEdit.setSelectAllOnFocus(true);
//                    searchView.setQuery(result.get(0), true);
                    Intent intent = new Intent(this, SearchAndShowActivity.class);
                    intent.putExtra(SearchAndShowActivity.SENT_WORD, result.get(0).trim());
                    Log.e("relay", "sending... " + result.get(0).trim());
                    startActivity(intent);
                    finish();
                }
                break;
            }

        }
    }

    @Override
    protected  void onDestroy() {
        Log.e("speech", "onDestroy");
        super.onDestroy();
    }
}
