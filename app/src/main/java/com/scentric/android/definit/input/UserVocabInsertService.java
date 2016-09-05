package com.scentric.android.definit.input;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.scentric.android.definit.showuservocab.sqlite.UserVocab;
import com.scentric.android.definit.showuservocab.sqlite.UserVocabHelper;

import java.util.ArrayList;

/**
 * Created by Steven on 8/14/2016.
 */
public class UserVocabInsertService extends IntentService {
    public final static String JSON_KEY = "jsonKey";

    public UserVocabInsertService() {
        super("UserVocabInsertService");
    }

    //todo: option for user to enter their own definitions




    // the UserVocab  sent by DisplayDefinitionPopupActivity are "malformed". All of their defExLists should have > 1 length if the user selects multiple defnitions
    // of the same wordForm, but they don't so it is up to this class to combine these defExLists if their words are the same



    @Override
    protected void onHandleIntent(Intent intent) {
//        UserVocab userVocab = (new Gson()).fromJson(intent.getStringExtra(JSON_KEY), UserVocab.class);
        ArrayList<UserVocab> userVocabList = (new Gson()).fromJson(intent.getStringExtra(JSON_KEY), new TypeToken<ArrayList<UserVocab>>(){}.getType());

        Log.e("intent", "received intent for " + intent.getStringExtra(JSON_KEY));

        UserVocabHelper helper = UserVocabHelper.getInstance(getApplicationContext());

//        for (int i = 0; i < userVocabList.size(); i++) {
//            helper.addWord(userVocabList.get(i));
//        }



        String lastWord = userVocabList.get(0).word.trim();

        UserVocab toSend = new UserVocab(); // the us
        toSend.word = lastWord;
        toSend.date = userVocabList.get(0).date;
        toSend.dateText = userVocabList.get(0).dateText;// these should be the same anyway... they're combined so just choose the first time to display and save :)
        toSend.listOfDefEx = new ArrayList<>(); // redundant
        for (int i = 0; i < userVocabList.size(); i++) {
            if (userVocabList.get(i).word.trim().equals(lastWord)) { // same word so package into the same UserVocab for the database so it display as one word. but if it's the last word just send it anyway
                toSend.listOfDefEx.add(userVocabList.get(i).listOfDefEx.get(0)); // length 1
            }
            else { // new wordform, so send this one off and create a new toSend
                helper.addWord(toSend);



                //reset
                lastWord = userVocabList.get(i).word.trim(); // assume
                Log.e("service", "new word " + lastWord);
                toSend = new UserVocab(); /// assume that the old toSend thrown away by the garbage collector?
                toSend.word = lastWord;
                toSend.date = userVocabList.get(i).date;
                toSend.dateText = userVocabList.get(i).dateText;
                toSend.listOfDefEx = (new ArrayList<>());
                toSend.listOfDefEx.add(userVocabList.get(i).listOfDefEx.get(0));
            }

            if (i == userVocabList.size()-1) { // last one lol
                helper.addWord(toSend);
            }
        }

    }

}

