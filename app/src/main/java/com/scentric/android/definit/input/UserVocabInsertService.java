package com.scentric.android.definit.input;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.scentric.android.definit.utility.UserVocab;
import com.scentric.android.definit.sqlite.VocabSQLHelper;

import java.util.ArrayList;

/**
 * Created by Steven on 8/14/2016.
 *
 * Finalizes and inserts vocabulary definition list into SQL
 *
 */
public class UserVocabInsertService extends IntentService {
    public final static String JSON_KEY = "jsonKey";

    public UserVocabInsertService() {
        super("UserVocabInsertService");
    }

    // todo: option for user to enter their own definitions

    // the UserVocab sent by DisplayDefinitionPopupActivity/PearsonAdapter are "malformed". All of their defExLists should have > 1 length if the user selects multiple definitions
    // of the same wordForm, but they don't so it is up to this class to combine these defExLists if their words are the same

    // Deal with the incoming list of words (possibly distinct)and definitions, and prepare for a SQL insertion
    @Override
    protected void onHandleIntent(Intent intent) {
//        UserVocab userVocab = (new Gson()).fromJson(intent.getStringExtra(JSON_KEY), UserVocab.class);
        ArrayList<UserVocab> userVocabList = (new Gson()).fromJson(intent.getStringExtra(JSON_KEY), new TypeToken<ArrayList<UserVocab>>() {
        }.getType());

        Log.e("intent", "received intent for " + intent.getStringExtra(JSON_KEY));

        VocabSQLHelper sqlHelper = VocabSQLHelper.getInstance(getApplicationContext());


        String lastWord = userVocabList.get(0).word.trim();

        UserVocab toSend = new UserVocab();
        toSend.word = lastWord;
        toSend.tag = userVocabList.get(0).tag;
        toSend.date = userVocabList.get(0).date;
        toSend.dateText = userVocabList.get(0).dateText;// these should be the same anyway... they're combined so just choose the first time to display and save :)
        toSend.listOfDefEx = new ArrayList<>(); // redundant
        for (int i = 0; i < userVocabList.size(); i++) {
            if (userVocabList.get(i).word.trim().equals(lastWord)) { // same word so package into the same UserVocab for the database so it display as one word. but if it's the last word just send it anyway
                toSend.listOfDefEx.add(userVocabList.get(i).listOfDefEx.get(0)); // length 1
            } else { // new wordform, so send this one off and create a new toSend
                sqlHelper.addWord(toSend);

                // reset
                lastWord = userVocabList.get(i).word.trim(); // assume
                Log.e("service", "new word " + lastWord);
                toSend = new UserVocab(); // the old toSend thrown away by the garbage collector
                toSend.word = lastWord;
                toSend.tag = userVocabList.get(i).tag;
                toSend.date = userVocabList.get(i).date;
                toSend.dateText = userVocabList.get(i).dateText;
                toSend.listOfDefEx = (new ArrayList<>());
                toSend.listOfDefEx.add(userVocabList.get(i).listOfDefEx.get(0));
            }

            if (i == userVocabList.size() - 1) { // last one
                sqlHelper.addWord(toSend);
            }
        }

    }

}

