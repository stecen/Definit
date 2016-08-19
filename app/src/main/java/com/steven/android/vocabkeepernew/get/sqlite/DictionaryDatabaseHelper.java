package com.steven.android.vocabkeepernew.get.sqlite;

import android.content.Context;

import com.readystatesoftware.sqliteasset.SQLiteAssetHelper;

/**
 * Created by Steven on 7/16/2016.
 */
public class DictionaryDatabaseHelper extends SQLiteAssetHelper {
    public static final String DATABASE_NAME = "dictionary.db";
    private static final int DATABASE_VERSION = 1;

    public DictionaryDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    //todo: onupgrade for improved dictionaries
}
