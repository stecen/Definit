package com.scentric.android.definit.utility;

import com.scentric.android.definit.utility.PearsonAnswer;

import java.util.ArrayList;

/**
 * Created by Steven on 8/13/2016.
 */
public class UserVocab { // for sqlite database easiness
    public String word;
    public ArrayList<PearsonAnswer.DefinitionExamples> listOfDefEx;
    public String tag; // for context. not named context to not be confused with Context in Android
    public long date, lastFaveDate;
    public String dateText;
    public boolean fave = false;

    public final static String TAG_FOR_NOW = "HI I AM CONTEXT HOW R U";

    public UserVocab(String word, ArrayList<PearsonAnswer.DefinitionExamples> list, String tag, long date, String dateText) {
        this.word = word; // should be same as PearsonAnswer.DefinitionExampleslist#wordForm
        this.listOfDefEx = list;
        this.tag = tag;
        this.date = date;
        this.dateText = dateText;
    }

    public UserVocab() {
        word = "";
        listOfDefEx = new ArrayList<>();
        dateText = "";
        tag = TAG_FOR_NOW;
    }

}
