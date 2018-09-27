package com.scentric.android.definit.sqlite;

import com.scentric.android.definit.utility.HistoryVocab;

import java.util.ArrayList;

/**
 * Created by Steven on 9/1/2016.
 *
 * Callback interface for to send when getting user history
 */
public interface GetHistoryInterface {
    public void setHistoryData(ArrayList<HistoryVocab> historyVocabList);
}
