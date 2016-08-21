package com.steven.android.vocabkeepernew.input;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.steven.android.vocabkeepernew.R;
import com.steven.android.vocabkeepernew.show.SearchAndShowActivity;

import java.util.ArrayList;
import java.util.Locale;

/**
 * Created by Steven on 8/21/2016.
 */
public class RelaySpeechActivity extends Activity {
    public final static int REQ_CODE_SPEECH_INPUT = 92;
    LinearLayout linearLayout;
    View view;
    boolean hasReocgnized =false;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_relay);

//        linearLayout = (LinearLayout) findViewById(R.id.relay_linear);
//        linearLayout.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Log.e("speechlol", "finish me");
//            }
//        });
//        view = findViewById(R.id.relay_view);
//        view.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Log.e("speechlol", "finish me");
//                finish();
//            }
//        });



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
        Log.e("speechlol", "onResume...");
        if (hasReocgnized) {
            finish();
        }
        super.onResume();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Log.e("speechlol", "onActivityResult...");

        switch (requestCode) {
            case REQ_CODE_SPEECH_INPUT: {
                if (resultCode == RESULT_OK && null != data) {

                    hasReocgnized = true;

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
//
//    public void finishMe(View v) { // touch linear layout
//        Log.e("speechlol", "finish me");
//        finish();
//    }

    @Override
    protected  void onDestroy() {
        Log.e("speech", "onDestroy");
        super.onDestroy();
    }
}
