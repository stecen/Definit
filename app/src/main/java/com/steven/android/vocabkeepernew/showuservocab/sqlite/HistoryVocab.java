package com.steven.android.vocabkeepernew.showuservocab.sqlite;

/**
 * Created by Steven on 8/22/2016.
 */
public class HistoryVocab {
    public String word;
    public long date;

    public HistoryVocab(String word, long date) {
        this.word = word;
        this.date = date;
    }

    public HistoryVocab() {
        word = " ";
    }
}
