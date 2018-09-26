package com.scentric.android.definit.x;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.ClipDescription;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.speech.RecognizerIntent;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import com.scentric.android.definit.R;

import java.util.ArrayList;
import java.util.Locale;

/**
 * Created by Steven on 11/30/2015.
 */
public class TypeWordPopupActivity extends AppCompatActivity {
    EditText wordEdit;
    public final static int REQ_CODE_SPEECH_INPUT = 92;
    public static TypeWordPopupActivity typeWordPopupActivity; // singleton

    public static final String KEY_RECOG_NOW = "recognow";
    public static final String YES = "yes";
    public static final String NO = "no";

    boolean isFromSpeech = false; // if isfromspeech, dont show keyboard (since this activity is being used mainly for speech recognition)
    int countResume = 0;

    @Override
    public void onNewIntent(Intent intent) { // from recognize speech action of notification
        Log.e("shouldspeech", "onnewintent called");
        if (intent != null && intent.hasExtra(KEY_RECOG_NOW)) {
            String shouldSpeech = intent.getStringExtra(KEY_RECOG_NOW);
            Log.e("type", "shouldspeech received " + shouldSpeech);
            if (shouldSpeech.equals(YES) && ++countResume <= 1) {
                recognizeSpeech();
                isFromSpeech = true;
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) { // todo: add alert dialog/fragment and make the background opacity stuff
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_typeword);

        wordEdit = (EditText) findViewById(R.id.word_edit);

        wordEdit.setFocusable(true);
        wordEdit.setFocusableInTouchMode(true);
        wordEdit.requestFocus();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null) {
            Log.e("tool", "Setting support toolbar...");
            setSupportActionBar(toolbar);
        }

        Log.e("shouldspeech", "oncreate called");

        Intent intent = getIntent();
        if (intent != null && intent.hasExtra(KEY_RECOG_NOW)) { // from recognize speech action of notification
            String shouldSpeech = intent.getStringExtra(KEY_RECOG_NOW);
            Log.e("type", "shouldspeech received " + shouldSpeech);
            if (shouldSpeech.equals(YES)) {
                recognizeSpeech();
                isFromSpeech = true;
            }
        }

        typeWordPopupActivity = this;
        showKeyboardAndHighlight();


    }


    public void showKeyboardAndHighlight() {
//        for (int i = 0; i < 1000;i ++) {
        wordEdit.requestFocus();
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(wordEdit, InputMethodManager.SHOW_IMPLICIT);
//        }

        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(wordEdit, InputMethodManager.SHOW_IMPLICIT);
            }
        }, 50);
    }

    // Add the word into the database--take from the input text
    public void showDefPopup(View v) {
//        Toast.makeText(this, wordEdit.getText(), Toast.LENGTH_SHORT).show();

        Intent addWordIntent = new Intent(this, DisplayDefinitionPopupActivity.class);
        addWordIntent.putExtra(DisplayDefinitionPopupActivity.SENT_WORD, wordEdit.getText().toString());

        Log.e("lol", "sending word: " + wordEdit.getText());

        startActivity(addWordIntent);

//        finish();
    }

    public void recognizeSpeech() {
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

    public void recognizeSpeech(View v) {
        recognizeSpeech();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Log.e("speech", "onActivityResult...");

        switch (requestCode) {
            case REQ_CODE_SPEECH_INPUT: {
                if (resultCode == RESULT_OK && null != data) {

                    ArrayList<String> result = data
                            .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);

//                    String newString = wordEdit.getText().toString() + result.get(0);
                    wordEdit.setText(result.get(0));
                    wordEdit.setSelectAllOnFocus(true);
                }
                break;
            }

        }
    }

    public void finishMe() {
        finish();
    }

    @Override
    public void onResume() {
        super.onResume();

        Log.e("speech", "onResume" + isFromSpeech);

        showKeyboardAndHighlight();

//        if (!DisplayDefinitionPopupActivity.shouldShowPreviousTypeWordPopup) {
//            DisplayDefinitionPopupActivity.shouldShowPreviousTypeWordPopup = true;
//            finish();
//        } else {

        //get keyboard

////
//            InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
//            inputMethodManager.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
////
////            inputMethodManager.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
//        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
//        imm.showSoftInput(wordEdit, InputMethodManager.SHOW_FORCED);
        if (!isFromSpeech) {

        }

//        }
    }


    //todo: make check icon hdpi, mdpi, xhdpi, etc. support different screen densities

    public static void showKeyboard(EditText mEtSearch, Context context) {
        mEtSearch.requestFocus();
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Activity.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
    }

    public void pasteIntoEdit(View v) {
        ClipboardManager cb = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        if (cb.hasPrimaryClip()) {
            ClipData cd = cb.getPrimaryClip();
            if (cd.getDescription().hasMimeType(ClipDescription.MIMETYPE_TEXT_PLAIN) || cd.getDescription().hasMimeType(ClipDescription.MIMETYPE_TEXT_HTML)) {

                //todo :insert what i want to do here / database stuff

                String word = cd.getItemAt(0).coerceToText(this).toString().trim();
//                String newString = wordEdit.getText().toString() + word;
                wordEdit.setText(word);
                wordEdit.setSelectAllOnFocus(true);
            }
        }
    }

    @Override
    public void onPause() {
        Log.e("speech", "pausing");

        //hide keyboard
//        if (isFromSpeech) {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(wordEdit.getWindowToken(), 0);
//        }
        isFromSpeech = false;
        super.onPause();
    }

    @Override
    public void onDestroy() {
        Log.e("speech", "destroy");
        super.onDestroy();
    }
}
