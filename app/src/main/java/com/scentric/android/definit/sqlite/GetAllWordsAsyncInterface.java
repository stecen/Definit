package com.scentric.android.definit.sqlite;

import com.scentric.android.definit.utility.UserVocab;

import java.util.ArrayList;

/**
 * Created by Steven on 8/31/2016.
 *
 * Callback interface for to send when getting user vocabulary
 *
 */
public interface GetAllWordsAsyncInterface {
    public void setWordsData(ArrayList<UserVocab> userVocabList);
}
