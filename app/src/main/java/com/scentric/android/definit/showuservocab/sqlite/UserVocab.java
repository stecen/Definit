package com.scentric.android.definit.showuservocab.sqlite;

import com.scentric.android.definit.utility.PearsonAnswer;

import java.util.ArrayList;

/**
 * Created by Steven on 8/13/2016.
 */
public class UserVocab { // for sqlite database easiness
    public String word;
    public ArrayList<PearsonAnswer.DefinitionExamples> listOfDefEx;
    public long date, lastFaveDate;
    public String dateText;
    public boolean fave = false;

    public UserVocab(String word, ArrayList<PearsonAnswer.DefinitionExamples> list, long date, String dateText) {
        this.word = word; // should be same as PearsonAnswer.DefinitionExampleslist#wordForm
        this.listOfDefEx = list;
        this.date = date;
        this.dateText = dateText;
    }

    public UserVocab() {
        word = "";
        listOfDefEx = new ArrayList<>();
        dateText = "";
    }

}
