package com.scentric.android.definit.sqlite;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.scentric.android.definit.utility.CustomUVStringAdapter;
import com.scentric.android.definit.utility.HistoryVocab;
import com.scentric.android.definit.utility.UserVocab;

import java.util.ArrayList;
import java.util.Locale;

/**
 * Created by Steven on 8/12/2016.
 * Deals with the database management of both history and user voca
 */
public class VocabSQLHelper extends SQLiteOpenHelper {
    private static VocabSQLHelper sInstance; // singleton

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
            KEY_TAG = "tag",
            KEY_WORD_IDX = "idx",
            KEY_DATE = "date",
            KEY_FAVE = "fave",
            KEY_FAVE_DATE = "lastFaveDate"; // last day it was faved
    // used for both tables

    public static int IS_FAVE = 1;
    public static int NOT_FAVE = 0;

    public static int GET_ALL = -1; // "amount" of words to retrieve to retrieve all of them

    public VocabSQLHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public static synchronized VocabSQLHelper getInstance(Context context) {
        // use app context to prevent memory leak
        if (sInstance == null) {
            sInstance = new VocabSQLHelper(context.getApplicationContext());
        }
        return sInstance;
    }

    // Called when the database is created for the FIRST time.
    // If a database already exists on disk with the same DATABASE_NAME, this method will NOT be called.
    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_WORDS_TABLE = String.format(Locale.US,
                "CREATE TABLE IF NOT EXISTS %s (%s INTEGER PRIMARY KEY AUTOINCREMENT, %s VARCHAR, %s VARCHAR, %s VARCHAR, %s INTEGER, %s UNSIGNED BIG INT, %s INTEGER, %s UNSIGNED BIG INT);",
                TABLE_WORDS, KEY_ID, KEY_WORD, KEY_JSON, KEY_TAG, KEY_WORD_IDX, KEY_DATE, KEY_FAVE, KEY_FAVE_DATE);
        Log.e("userVocab", "executing sql: " + CREATE_WORDS_TABLE);
        db.execSQL(CREATE_WORDS_TABLE); // create user saved words table

        String CREATE_HISTORY_TABLE = String.format(Locale.US, "CREATE TABLE IF NOT EXISTS %s (%s INTEGER PRIMARY KEY AUTOINCREMENT, %s VARCHAR, %s UNSIGNED BIG INT);",
                TABLE_HISTORY, KEY_ID, KEY_WORD, KEY_DATE);
        Log.e("userVocab", "executing sql: " + CREATE_HISTORY_TABLE);
        db.execSQL(CREATE_HISTORY_TABLE); // create all user searched words
    }

    // Called when the database needs to be upgraded.
    // This method will only be called if a database already exists on disk with the same DATABASE_NAME,
    // but the DATABASE_VERSION is different than the version of the database that exists on disk
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion != newVersion) {
            Log.e("userVocab", "new database version????");
            // Simplest implementation is to drop all old tables and recreate them
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_WORDS);
            onCreate(db);
        }
    }

    // region history
    public void addHistory(HistoryVocab historyVocab) {
        SQLiteDatabase db = getWritableDatabase();

        Log.e("addWordHist", historyVocab.word + " " + historyVocab.date);


        // wrap our insert in a transaction to help with performance and ensuring database consistency
        db.beginTransaction();
        try {
            // The user might already exist in the database (i.e. the same user created multiple posts).
            //long userId = addOrUpdateWord(userVocab.word); // todo: check for duplicates

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
            Log.d("userVocab", "\nError while trying to add post to database + " + e.toString() + "\n\n");
        } finally {
            db.endTransaction();
        }
    }

    public void getHistory50(GetHistoryInterface asyncInterface, int howMany) {
        GetHistoryAsyncTask task = new GetHistoryAsyncTask(getReadableDatabase(), asyncInterface, howMany);
        task.execute();
    }

    public void deleteHistory(HistoryVocab historyVocab) {
        // Create and/or open the database for writing
        SQLiteDatabase db = getWritableDatabase();

        Log.e("deleteHistory", "deletingHistory " + historyVocab.word);

        db.beginTransaction();
        try {
            String where = KEY_DATE + '=' + historyVocab.date + " AND " + KEY_WORD + " = \"" + historyVocab.word + "\"";
            // Notice how we haven't specified the primary key. SQLite auto increments the primary key column.
            Log.e("deleteWord", where);
            db.delete(TABLE_HISTORY, where, null);
            db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.d("deleteWord", "Error while trying to add post to database + " + e.toString() + "\n\n");
        } finally {
            db.endTransaction();
        }
    }

    private class GetHistoryAsyncTask extends AsyncTask<Void, Void, ArrayList<HistoryVocab>> { // class to allow the get all query to happen on a seperate thread
        GetHistoryInterface asyncInterface;
        SQLiteDatabase db;
        ArrayList<HistoryVocab> historyVocabs;
        int howMany; // how many elements to load

        public GetHistoryAsyncTask(SQLiteDatabase db, GetHistoryInterface asyncInterface, int howMany) {
            super();
            this.db = db;
            this.asyncInterface = asyncInterface;

            historyVocabs = new ArrayList<>();

            this.howMany = howMany;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }


        @Override
        protected ArrayList<HistoryVocab> doInBackground(Void... voids) {

            int i = 0;

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

                    } while (cursor.moveToNext() && ((howMany == GET_ALL || (++i <= howMany))));
                }
            } catch (Exception e) {
                Log.d("hist", "error getting hist " + e.toString());
            } finally {
                if (cursor != null && !cursor.isClosed()) {
                    cursor.close();
                }
            }

            return historyVocabs;
        }

        @Override
        protected void onPostExecute(ArrayList<HistoryVocab> historyVocabArrayList) {
            super.onPostExecute(historyVocabArrayList);
            asyncInterface.setHistoryData(historyVocabArrayList);
        }
    }

    // endregion

    public void getAllUserVocab(GetAllWordsAsyncInterface asyncInterface, int howMany) {
        GetAllWordsAsyncTask task = new GetAllWordsAsyncTask(getReadableDatabase(), asyncInterface, howMany);
        task.execute();
    }

    // database retrieval
    private class GetAllWordsAsyncTask extends AsyncTask<Void, Void, ArrayList<UserVocab>> { // class to allow the get all query to happen on a seperate thread
        GetAllWordsAsyncInterface asyncInterface;
        SQLiteDatabase db;
        ArrayList<UserVocab> userVocabs;
        int howMany; // how many elements to load

        String USER_VOCAB_SELECT_QUERY =
                String.format("SELECT * FROM %s ORDER BY %s DESC;",
                        TABLE_WORDS,
                        KEY_DATE);

        public GetAllWordsAsyncTask(SQLiteDatabase db, GetAllWordsAsyncInterface asyncInterface, int howMany) {
            super();
            this.db = db;
            this.asyncInterface = asyncInterface;

            userVocabs = new ArrayList<>();

            this.howMany = howMany;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }


        @Override
        protected ArrayList<UserVocab> doInBackground(Void... voids) {
            Log.e("userVocab", "querying: " + USER_VOCAB_SELECT_QUERY);
            Cursor cursor = db.rawQuery(USER_VOCAB_SELECT_QUERY, null);

            int i = 0;
            try {
                if (cursor.moveToFirst()) {
                    do {
                        UserVocab userVocab = new UserVocab();
                        userVocab.word = cursor.getString(cursor.getColumnIndex(KEY_WORD));
                        String json = cursor.getString(cursor.getColumnIndex(KEY_JSON));
                        userVocab.listOfDefEx = CustomUVStringAdapter.fromString(json);
                        userVocab.tag = cursor.getString(cursor.getColumnIndex(KEY_TAG));
                        userVocab.wordIdx = cursor.getInt(cursor.getColumnIndex(KEY_WORD_IDX));
                        userVocab.date = cursor.getLong(cursor.getColumnIndex(KEY_DATE));

                        int faveInt = cursor.getInt(cursor.getColumnIndex(KEY_FAVE));
                        userVocab.fave = (faveInt == IS_FAVE);
                        userVocabs.add(userVocab);

                    }
                    while (cursor.moveToNext() && ((howMany == GET_ALL || (++i <= howMany)))); // limit to 25 the first time so that the user is guaranteed to see something on their screen.


                }
            } catch  (RuntimeException e) {
                Log.d("userVocab", "error getting user vocab " + "");
                e.printStackTrace();
            } finally {
                if (cursor != null && !cursor.isClosed()) {
                    cursor.close();
                }
            }
            return userVocabs;
        }

        @Override
        protected void onPostExecute(ArrayList<UserVocab> userVocabArrayList) {
            super.onPostExecute(userVocabArrayList);
            asyncInterface.setWordsData(userVocabArrayList);
        }
    }

    public void getFaveVocabList(GetAllWordsAsyncInterface asyncInterface, int howMany) {
        GetFaveAsyncTask task = new GetFaveAsyncTask(getReadableDatabase(), asyncInterface, howMany);
        task.execute();

    }

    private class GetFaveAsyncTask extends AsyncTask<Void, Void, ArrayList<UserVocab>> { // class to allow the get all query to happen on a seperate thread
        GetAllWordsAsyncInterface asyncInterface;
        SQLiteDatabase db;
        ArrayList<UserVocab> userVocabs;
        int howMany; // how many elements to load

        String USER_VOCAB_SELECT_QUERY =
                String.format(Locale.US, "SELECT * FROM %s WHERE %s = %d ORDER BY %s DESC ;",
                        TABLE_WORDS, KEY_FAVE,
                        IS_FAVE,
                        KEY_FAVE_DATE);

        public GetFaveAsyncTask(SQLiteDatabase db, GetAllWordsAsyncInterface asyncInterface, int howMany) {
            super();
            this.db = db;
            this.asyncInterface = asyncInterface;

            userVocabs = new ArrayList<>();

            this.howMany = howMany;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }


        @Override
        protected ArrayList<UserVocab> doInBackground(Void... voids) {

            int i = 0;

            Log.e("userVocab", "fave querying: " + USER_VOCAB_SELECT_QUERY);
            Cursor cursor = db.rawQuery(USER_VOCAB_SELECT_QUERY, null);
            try {
                if (cursor.moveToFirst()) {
                    do {
                        UserVocab userVocab = new UserVocab();
                        userVocab.word = cursor.getString(cursor.getColumnIndex(KEY_WORD));
                        String json = cursor.getString(cursor.getColumnIndex(KEY_JSON));

                        userVocab.listOfDefEx = CustomUVStringAdapter.fromString(json);
                        userVocab.date = cursor.getLong(cursor.getColumnIndex(KEY_DATE));

                        int faveInt = cursor.getInt(cursor.getColumnIndex(KEY_FAVE));
                        userVocab.fave = (faveInt == IS_FAVE);

                        userVocabs.add(userVocab);

                    } while (cursor.moveToNext() && ((howMany == GET_ALL || (++i <= howMany))));
                }
            } catch (Exception e) {
                Log.d("userVocab", "fave error getting user vocab " + e.toString());
            } finally {
                if (cursor != null && !cursor.isClosed()) {
                    cursor.close();
                }
            }

            return userVocabs;
        }

        @Override
        protected void onPostExecute(ArrayList<UserVocab> userVocabArrayList) {
            super.onPostExecute(userVocabArrayList);
            asyncInterface.setWordsData(userVocabArrayList);
        }
    }


    // database update

    // todo: upsert SQLite
    // Insert a post into the database, for user vocabulary
    public void addWord(UserVocab userVocab) {
        // todo: make favorite and addword async
        // Create and/or open the database for writing
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        try {
            // The user might already exist in the database (i.e. the same user created multiple posts).
            //long userId = addOrUpdateWord(userVocab.word); // todo: check for duplicates

            ContentValues values = new ContentValues();
            values.put(KEY_WORD, userVocab.word.trim());

//                String json = (new Gson()).toJson(userVocab.listOfDefEx);
            String json = CustomUVStringAdapter.toString(userVocab.listOfDefEx);
            values.put(KEY_JSON, json);
            Log.e("addingjson", json);

            values.put(KEY_TAG, userVocab.tag);
            Log.e("sqltag", ""+userVocab.wordIdx);
            values.put(KEY_WORD_IDX, userVocab.wordIdx);

            values.put(KEY_DATE, userVocab.date/* + (long)i*/);
            values.put(KEY_FAVE, (userVocab.fave) ? IS_FAVE : NOT_FAVE);

            db.insertOrThrow(TABLE_WORDS, null, values);
            db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.d("userVocab", "Error while trying to add post to database + " + e.toString() + "\n\n");
        } finally {
            db.endTransaction();
        }
//        }
    }

    // delete user vocabulary
    public void deleteWord(UserVocab userVocab) {
        // Create and/or open the database for writing
        SQLiteDatabase db = getWritableDatabase();

        Log.e("deleteWord", "deletingword " + userVocab.word);

        db.beginTransaction();
        try {
            String where = KEY_DATE + '=' + userVocab.date + " AND " + KEY_WORD + " = \"" + userVocab.word + "\"";
            // Notice how we haven't specified the primary key. SQLite auto increments the primary key column.
            Log.e("deleteWord", where);
            db.delete(TABLE_WORDS, where, null);
            db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.d("deleteWord", "Error while trying to add post to database + " + e.toString() + "\n\n");
        } finally {
            db.endTransaction();
        }
    }

    public void toggleFavorite(UserVocab userVocab) {
        // Create and/or open the database for writing
        SQLiteDatabase db = getWritableDatabase();

        Log.e("toggleFavorite", "toggling " + userVocab.word + " to " + !userVocab.fave);

        db.beginTransaction();
        try {

            ContentValues values = new ContentValues();
            values.put(KEY_FAVE, (!userVocab.fave) ? IS_FAVE : NOT_FAVE);
            values.put(KEY_FAVE_DATE, System.currentTimeMillis()); // when this item was last favorited, to sort by favorited date

            String where = KEY_DATE + '=' + userVocab.date + " AND " + KEY_WORD + " = \"" + userVocab.word + "\"";
            // Notice how we haven't specified the primary key. SQLite auto increments the primary key column.
            Log.e("toggleFavorite", where);
            db.update(TABLE_WORDS, values, where, null);
            db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.d("userVocab", "Error while trying to add post to database + " + e.toString() + "\n\n");
        } finally {
            db.endTransaction();
        }
    }

    // region import

    public void importNative(ArrayList<UserVocab> userVocabArrayList) {
//        ArrayList<UserVocab> userVocabArrayList = (new Gson()).fromJson(json, new TypeToken<ArrayList<UserVocab>>(){}.getType());
        SQLiteDatabase db = getWritableDatabase();

        for (int i = 0; i < userVocabArrayList.size(); i++) {
//            db.beginTransaction();
            try {
                ContentValues values = new ContentValues();
                values.put(KEY_WORD, userVocabArrayList.get(i).word.trim());
                values.put(KEY_DATE, userVocabArrayList.get(i).date);

                String whereClause = String.format(Locale.US, "%s = \"%s\" AND %s = %d", KEY_WORD, userVocabArrayList.get(i).word.trim(), KEY_DATE, userVocabArrayList.get(i).date);
                Log.e("upsert", whereClause);

                // First try to update the user in case the user already exists in the database
                // This assumes userNames are unique
                int rows = db.update(TABLE_WORDS, values, whereClause
                        /*KEY_WORD + "= ?"*/, /*new String[]{word}*/ null);

                Log.e("upsert", "rows = " + rows);

                // Check if update succeeded
                if (rows < 1) {

                    addWord(userVocabArrayList.get(i));
                }
            } catch (Exception e) {
                Log.d("userVocab", "Error while trying to add or update user");
            }
        }
    }

    // endregion

    // region check duplicate
    // Insert or update a user in the database
    public long addOrUpdateWord(String word) {
        // The database connection is cached so it's not expensive to call getWriteableDatabase() multiple times.
        SQLiteDatabase db = getWritableDatabase();
        long userId = -1;

        word = word.trim();

        db.beginTransaction();
        try {
            ContentValues values = new ContentValues();
            values.put(KEY_WORD, word);

            // First try to update the user in case the user already exists in the database
            // This assumes userNames are unique
            int rows = db.update(TABLE_WORDS, values, KEY_WORD + "= ?", new String[]{word});

            Log.e("userVocab", "rows = " + rows);

            // Check if update succeeded
            if (rows == 1) {
                // Get the primary key of the user we just updated
                String usersSelectQuery = String.format("SELECT %s FROM %s WHERE %s = ?",
                        KEY_ID, TABLE_WORDS, KEY_WORD);
                Cursor cursor = db.rawQuery(usersSelectQuery, new String[]{String.valueOf(word)});
                try {
                    if (cursor.moveToFirst()) {
                        userId = cursor.getInt(0);
                        db.setTransactionSuccessful();
                    }
                } finally {
                    if (cursor != null && !cursor.isClosed()) {
                        cursor.close();
                    }
                }
            } else {
                // user with this userName did not already exist, so insert new user. todo... what?
                userId = db.insertOrThrow(TABLE_WORDS, null, values);
                db.setTransactionSuccessful();
            }
        } catch (Exception e) {
            Log.d("userVocab", "Error while trying to add or update user");
        } finally {
            db.endTransaction();
        }
        return userId;
    }


    public void deleteAllUserVocab() {
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        try {
            // Order of deletions is important when foreign key relationships exist.
            // // db.delete(TABLE_WORDS, null, null);
            db.execSQL(String.format("DELETE FROM %s;", TABLE_WORDS));
            db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.d("userVocab", "error deleting userVocab");
        } finally {
            db.endTransaction();
        }
    }

    //endregion
}
