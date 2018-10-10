package com.scentric.android.definit.utility;

import android.util.Log;

import com.scentric.android.definit.utility.PearsonAnswer;

import java.util.ArrayList;

/**
 * Created by Steven on 8/13/2016.
 */
public class UserVocab { // for sqlite database easiness
    public String word;
    public ArrayList<PearsonAnswer.DefinitionExamples> listOfDefEx;
    public String tag; // for context. not named context to not be confused with Context in Android
    public int wordIdx = -1; // position of the word in the context
    public long date, lastFaveDate;
    public String dateText;
    public boolean fave = false;

    public final static String TAG_FOR_NOW = "HI I AM CONTEXT HOW R U";

    public UserVocab(String word, ArrayList<PearsonAnswer.DefinitionExamples> list, String tag, int wordIdx, long date, String dateText) {
        this.word = word; // should be same as PearsonAnswer.DefinitionExampleslist#wordForm
        this.listOfDefEx = list;
        this.tag = tag;
        Log.e("uservocab", ""+wordIdx);
        this.wordIdx = wordIdx;
        this.date = date;
        this.dateText = dateText;
    }

    public UserVocab() {
        word = "";
        listOfDefEx = new ArrayList<>();
        dateText = "";
        tag = TAG_FOR_NOW;
    }

    public boolean hasContext() {
        return !tag.equals(TAG_FOR_NOW);
    }

}
