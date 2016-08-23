package com.steven.android.vocabkeepernew.showuservocab.sqlite;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.steven.android.vocabkeepernew.utility.PearsonAnswer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Locale;

/**
 * Created by Steven on 8/12/2016.
 */
public class UserVocabHelper extends SQLiteOpenHelper {
    private static UserVocabHelper sInstance; // singleton

    // Database Info
    private static final String DATABASE_NAME = "userVocab";
    private static final int DATABASE_VERSION = 1;

    // Tables
    private static final String TABLE_WORDS = "words";
    private static final String TABLE_HISTORY = "history";

    // Columns
    private static final String KEY_ID = "_id",
        KEY_WORD = "word",
        KEY_JSON = "json",
        KEY_DATE = "date",
        KEY_DATETEXT = "dateText";
    // used for both tables

    public UserVocabHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public static synchronized UserVocabHelper getInstance(Context context) {
        // Use the application context, which will ensure that you
        // don't accidentally leak an Activity's context.
        // See this article for more information: http://bit.ly/6LRzfx
        if (sInstance == null) {
            sInstance = new UserVocabHelper(context.getApplicationContext());
        }
        return sInstance;
    }

    // Called when the database is created for the FIRST time.
    // If a database already exists on disk with the same DATABASE_NAME, this method will NOT be called.
    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_WORDS_TABLE = String.format(Locale.US, "CREATE TABLE IF NOT EXISTS %s (%s INTEGER PRIMARY KEY AUTOINCREMENT, %s VARCHAR, %s VARCHAR, %s UNSIGNED BIG INT, %s VARCHAR);",
               TABLE_WORDS, KEY_ID, KEY_WORD, KEY_JSON, KEY_DATE, KEY_DATETEXT);
//                "CREATE TABLE IF NOT EXISTS " +TABLE_WORDS+ "(" +KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +KEY_WORD+
//                        " VARCHAR, " +KEY_JSON+ " VARCHAR, " +KEY_DATE+ " INTEGER, " +KEY_DATETEXT+ " VARCHAR);";
        Log.e("userVocab", "executing sql: " + CREATE_WORDS_TABLE);
        db.execSQL(CREATE_WORDS_TABLE); // create user saved words table

//
//
        String CREATE_HISTORY_TABLE = String.format(Locale.US, "CREATE TABLE IF NOT EXISTS %s (%s INTEGER PRIMARY KEY AUTOINCREMENT, %s VARCHAR, %s UNSIGNED BIG INT);",
                TABLE_HISTORY, KEY_ID, KEY_WORD, KEY_DATE);
        Log.e("userVocab", "executing sql: " + CREATE_HISTORY_TABLE);
        db.execSQL(CREATE_HISTORY_TABLE); // create all user searched words
    }

    //region history
    public void addHistory(HistoryVocab historyVocab) {
        SQLiteDatabase db = getWritableDatabase();

        Log.e("addWordHist", historyVocab.word + " " + historyVocab.date);

        // It's a good idea to wrap our insert in a transaction. This helps with performance and ensures
        // consistency of the database.
        db.beginTransaction();
        try {
            // The user might already exist in the database (i.e. the same user created multiple posts).
            //long userId = addOrUpdateWord(userVocab.word); //todo: check for duplicates

            ContentValues values = new ContentValues();
            values.put(KEY_WORD, historyVocab.word.trim());
            values.put(KEY_DATE, historyVocab.date);

            String queryString = String.format(Locale.US, "INSERT INTO %s VALUES (%s, %s) VALUES (\"%s\", %d);",
                    TABLE_HISTORY,
                    KEY_WORD, KEY_DATE,
                    historyVocab.word.trim(), historyVocab.date);

            Log.e("addWordHist", "adding: " + queryString);

            // Notice how we haven't specified the primary key. SQLite auto increments the primary key column.
            db.insertOrThrow(TABLE_HISTORY, null, values);
            db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.d("userVocab", "\n\n\n\n\n\n\n\n\n\n\n\nEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEError while trying to add post to database + " + e.toString() + "\n\n");
        } finally {
            db.endTransaction();
        }
    }

    public ArrayList<HistoryVocab> getHistory50() {
        ArrayList<HistoryVocab> historyVocabs = new ArrayList<>();

        // SELECT * FROM POSTS
        // LEFT OUTER JOIN USERS
        // ON POSTS.KEY_POST_USER_ID_FK = USERS.KEY_USER_ID
        String HISTORY_SELECT_QUERY =
                String.format("SELECT * FROM %s ORDER BY %s DESC;",
                        TABLE_HISTORY,
                        KEY_DATE);


        SQLiteDatabase db = getReadableDatabase();
        Log.e("hist", "querying: " + HISTORY_SELECT_QUERY);
        Cursor cursor = db.rawQuery(HISTORY_SELECT_QUERY, null);
        try {
            if (cursor.moveToFirst()) {
                do {
                    HistoryVocab histVocab = new HistoryVocab();
                    histVocab.word = cursor.getString(cursor.getColumnIndex(KEY_WORD));
                    histVocab.date = cursor.getLong(cursor.getColumnIndex(KEY_DATE));

                    historyVocabs.add(histVocab);

                } while(cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.d("hist", "error getting hist "  + e.toString());
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }

        Collections.reverse(historyVocabs);
        return historyVocabs;
    }






    //region user vocab

    public Cursor getAllUserVocabCursor() { // make sure to close cursor!
        // SELECT * FROM POSTS
        // LEFT OUTER JOIN USERS
        // ON POSTS.KEY_POST_USER_ID_FK = USERS.KEY_USER_ID
        String USER_VOCAB_SELECT_QUERY =
                String.format("SELECT * FROM %s;",
                        TABLE_WORDS/*,
                        KEY_DATE*/);


        SQLiteDatabase db = getReadableDatabase();
        Log.e("userVocab", "querying for cursor: " + USER_VOCAB_SELECT_QUERY);
        Cursor cursor = db.rawQuery(USER_VOCAB_SELECT_QUERY, null);

        return cursor;
    }

    public ArrayList<UserVocab> getAllUserVocab() {
        ArrayList<UserVocab> userVocabs = new ArrayList<>();

        // SELECT * FROM POSTS
        // LEFT OUTER JOIN USERS
        // ON POSTS.KEY_POST_USER_ID_FK = USERS.KEY_USER_ID
        String USER_VOCAB_SELECT_QUERY =
                String.format("SELECT * FROM %s ORDER BY %s DESC;",
                        TABLE_WORDS,
                        KEY_DATE);


        SQLiteDatabase db = getReadableDatabase();
        Log.e("userVocab", "querying: " + USER_VOCAB_SELECT_QUERY);
        Cursor cursor = db.rawQuery(USER_VOCAB_SELECT_QUERY, null);
        try {
            if (cursor.moveToFirst()) {
                do {
                    UserVocab userVocab = new UserVocab();
                    userVocab.word = cursor.getString(cursor.getColumnIndex(KEY_WORD));
                    String json = cursor.getString(cursor.getColumnIndex(KEY_JSON));
                    Log.e("getAllUserVocab", json);
                    userVocab.listOfDefEx = (new Gson()).fromJson(json, new TypeToken<ArrayList<PearsonAnswer.DefinitionExamples>>(){}.getType());
                    Log.e("getAllUserVocab", ""+ userVocab.listOfDefEx.size());
                    userVocab.date = cursor.getLong(cursor.getColumnIndex(KEY_DATE));
                    Log.e("byte", (long)cursor.getLong(cursor.getColumnIndex(KEY_DATE)) + "");
                    userVocab.dateText = cursor.getString(cursor.getColumnIndex(KEY_DATETEXT));

                    userVocabs.add(userVocab);

                } while(cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.d("userVocab", "error getting user vocab "  + e.toString());
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }
//        Collections.reverse(userVocabs);
        return userVocabs;
    }


    // todo: upsert SQLite
    // Insert a post into the database
    public void addWord(UserVocab userVocab) {
        // Create and/or open the database for writing
        SQLiteDatabase db = getWritableDatabase();

        Log.e("addWordUV", (new Gson()).toJson(userVocab));

        // It's a good idea to wrap our insert in a transaction. This helps with performance and ensures
        // consistency of the database.
        db.beginTransaction();
        try {
            // The user might already exist in the database (i.e. the same user created multiple posts).
            //long userId = addOrUpdateWord(userVocab.word); //todo: check for duplicates

            ContentValues values = new ContentValues();
            values.put(KEY_WORD, userVocab.word.trim());

            String json =  (new Gson()).toJson(userVocab.listOfDefEx);
            values.put(KEY_JSON, json);
            Log.e("adding word json", json);

            values.put(KEY_DATE, userVocab.date);
            values.put(KEY_DATETEXT, userVocab.dateText);

            String queryString = String.format(Locale.US, "INSERT INTO %s VALUES (%s, %s, %s, %s) VALUES (\"%s\", \"%s\", \"%d\", \"%s\");",
                    TABLE_WORDS,
                    KEY_WORD, KEY_JSON, KEY_DATE, KEY_DATETEXT,
                    userVocab.word.trim(), (new Gson()).toJson(userVocab.listOfDefEx), userVocab.date, userVocab.dateText);


            Log.e("userVocab", "adding: " + queryString);

            // Notice how we haven't specified the primary key. SQLite auto increments the primary key column.
            db.insertOrThrow(TABLE_WORDS, null, values);
            db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.d("userVocab", "\n\n\n\n\n\n\n\n\n\n\n\nEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEError while trying to add post to database + " + e.toString() + "\n\n");
        } finally {
            db.endTransaction();
        }
    }

    //endregion


// region check duplicate
// Insert or update a user in the database
//    // Since SQLite doesn't support "upsert" we need to fall back on an attempt to UPDATE (in case the
//    // user already exists) optionally followed by an INSERT (in case the user does not already exist).
//    // Unfortunately, there is a bug with the insertOnConflict method
//    // (https://code.google.com/p/android/issues/detail?id=13045) so we need to fall back to the more
//    // verbose option of querying for the user's primary key if we did an update.
//    public long addOrUpdateWord(String word) {
//        // The database connection is cached so it's not expensive to call getWriteableDatabase() multiple times.
//        SQLiteDatabase db = getWritableDatabase();
//        long userId = -1;
//
//        word = word.trim();
//
//        db.beginTransaction();
//        try {
//            ContentValues values = new ContentValues();
//            values.put(KEY_WORD, word);
//
//            // First try to update the user in case the user already exists in the database
//            // This assumes userNames are unique
//            int rows = db.update(TABLE_WORDS, values, KEY_WORD + "= ?", new String[]{word});
//
//            Log.e("userVocab", "rows = " + rows);
//
//            // Check if update succeeded
//            if (rows == 1) {
//                // Get the primary key of the user we just updated
//                String usersSelectQuery = String.format("SELECT %s FROM %s WHERE %s = ?",
//                        KEY_ID, TABLE_WORDS, KEY_WORD);
//                Cursor cursor = db.rawQuery(usersSelectQuery, new String[]{String.valueOf(word)});
//                try {
//                    if (cursor.moveToFirst()) {
//                        userId = cursor.getInt(0);
//                        db.setTransactionSuccessful();
//                    }
//                } finally {
//                    if (cursor != null && !cursor.isClosed()) {
//                        cursor.close();
//                    }
//                }
//            } else {
//                // user with this userName did not already exist, so insert new user. todo... what?
//                userId = db.insertOrThrow(TABLE_WORDS, null, values);
//                db.setTransactionSuccessful();
//            }
//        } catch (Exception e) {
//            Log.d("userVocab", "Error while trying to add or update user");
//        } finally {
//            db.endTransaction();
//        }
//        return userId;
//    }

    public void deleteAllUserVocab() {
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        try {
            // Order of deletions is important when foreign key relationships exist.
//            db.delete(TABLE_WORDS, null, null);
            db.execSQL(String.format("DELETE FROM %s;", TABLE_WORDS));
            db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.d("userVocab", "error deleting userVocab");
        } finally {
            db.endTransaction();
        }
    }



    // Called when the database needs to be upgraded.
    // This method will only be called if a database already exists on disk with the same DATABASE_NAME,
    // but the DATABASE_VERSION is different than the version of the database that exists on disk.
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion != newVersion) {
            Log.e("userVocab", "new database version????");
            // Simplest implementation is to drop all old tables and recreate them
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_WORDS);
            onCreate(db);
        }
    }
}
