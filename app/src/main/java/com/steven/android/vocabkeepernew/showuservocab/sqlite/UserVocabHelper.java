package com.steven.android.vocabkeepernew.showuservocab.sqlite;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.AsyncTask;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.steven.android.vocabkeepernew.get.CallbackAsyncInterface;
import com.steven.android.vocabkeepernew.utility.CustomUVStringAdapter;
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
        KEY_FAVE = "fave",
        KEY_FAVE_DATE = "lastFaveDate"; // last day it was faved
    // used for both tables

    public static int IS_FAVE = 1;
    public static int NOT_FAVE = 0;

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
        String CREATE_WORDS_TABLE = String.format(Locale.US, "CREATE TABLE IF NOT EXISTS %s (%s INTEGER PRIMARY KEY AUTOINCREMENT, %s VARCHAR, %s VARCHAR, %s UNSIGNED BIG INT, %s INTEGER, %s UNSIGNED BIG INT);",
               TABLE_WORDS, KEY_ID, KEY_WORD, KEY_JSON, KEY_DATE, KEY_FAVE, KEY_FAVE_DATE);
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

    public void getHistory50(GetHistoryInterface asyncInterface, int howMany) {
        GetHistoryAsyncTask task = new GetHistoryAsyncTask(getReadableDatabase(), asyncInterface, howMany);
        task.execute();
//        ArrayList<HistoryVocab> historyVocabs = new ArrayList<>();
//
//        // SELECT * FROM POSTS
//        // LEFT OUTER JOIN USERS
//        // ON POSTS.KEY_POST_USER_ID_FK = USERS.KEY_USER_ID
//        String HISTORY_SELECT_QUERY =
//                String.format("SELECT * FROM %s ORDER BY %s DESC;",
//                        TABLE_HISTORY,
//                        KEY_DATE);
//
//
//        SQLiteDatabase db = getReadableDatabase();
//        Log.e("hist", "querying: " + HISTORY_SELECT_QUERY);
//        Cursor cursor = db.rawQuery(HISTORY_SELECT_QUERY, null);
//        try {
//            if (cursor.moveToFirst()) {
//                do {
//                    HistoryVocab histVocab = new HistoryVocab();
//                    histVocab.word = cursor.getString(cursor.getColumnIndex(KEY_WORD));
//                    histVocab.date = cursor.getLong(cursor.getColumnIndex(KEY_DATE));
//
//                    historyVocabs.add(histVocab);
//
//                } while(cursor.moveToNext());
//            }
//        } catch (Exception e) {
//            Log.d("hist", "error getting hist "  + e.toString());
//        } finally {
//            if (cursor != null && !cursor.isClosed()) {
//                cursor.close();
//            }
//        }
//
////        Collections.reverse(historyVocabs);
//        return historyVocabs;
    }

    public void deleteHistory(HistoryVocab historyVocab) {
        // Create and/or open the database for writing
        SQLiteDatabase db = getWritableDatabase();

        Log.e("deleteHistory", "deletingHistory " + historyVocab.word);

        db.beginTransaction();
        try {
            String where= KEY_DATE + '=' + historyVocab.date + " AND " + KEY_WORD + " = \"" + historyVocab.word+"\"";
            // Notice how we haven't specified the primary key. SQLite auto increments the primary key column.
            Log.e("deleteWord", where);
            db.delete(TABLE_HISTORY, where, null);
            db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.d("deleteWord", "\n\n\n\n\n\n\n\n\n\n\n\nEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEError while trying to add post to database + " + e.toString() + "\n\n");
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

                    } while(cursor.moveToNext() && ((howMany == GET_ALL || (++i <= howMany))));
                }
            } catch (Exception e) {
                Log.d("hist", "error getting hist "  + e.toString());
            } finally {
                if (cursor != null && !cursor.isClosed()) {
                    cursor.close();
                }
            }

//        Collections.reverse(historyVocabs);
            return historyVocabs;
        }

        @Override
        protected void onPostExecute(ArrayList<HistoryVocab> historyVocabArrayList) {
            super.onPostExecute(historyVocabArrayList);
            asyncInterface.setHistoryData(historyVocabArrayList);
        }
    }

    //region user vocab

//    public Cursor getAllUserVocabCursor() { // make sure to close cursor!
//        // SELECT * FROM POSTS
//        // LEFT OUTER JOIN USERS
//        // ON POSTS.KEY_POST_USER_ID_FK = USERS.KEY_USER_ID
//        String USER_VOCAB_SELECT_QUERY =
//                String.format("SELECT * FROM %s;",
//                        TABLE_WORDS/*,
//                        KEY_DATE*/);
//
//
//        SQLiteDatabase db = getReadableDatabase();
//        Log.e("userVocab", "querying for cursor: " + USER_VOCAB_SELECT_QUERY);
//        Cursor cursor = db.rawQuery(USER_VOCAB_SELECT_QUERY, null);
//
//        return cursor;
//    }

    public static int GET_ALL = -1;

    public void getAllUserVocab(GetAllWordsAsyncInterface asyncInterface, int howMany) {
        GetAllWordsAsyncTask task = new GetAllWordsAsyncTask(getReadableDatabase(), asyncInterface, howMany);
        task.execute();

//        ArrayList<UserVocab> userVocabs = new ArrayList<>();
//
//        // SELECT * FROM POSTS
//        // LEFT OUTER JOIN USERS
//        // ON POSTS.KEY_POST_USER_ID_FK = USERS.KEY_USER_ID
//        String USER_VOCAB_SELECT_QUERY =
//                String.format("SELECT * FROM %s ORDER BY %s DESC;",
//                        TABLE_WORDS,
//                        KEY_DATE);
//
//
//        SQLiteDatabase db = getReadableDatabase();
//        Log.e("userVocab", "querying: " + USER_VOCAB_SELECT_QUERY);
//        Cursor cursor = db.rawQuery(USER_VOCAB_SELECT_QUERY, null);
//        try {
//            if (cursor.moveToFirst()) {
//                do {
//                    UserVocab userVocab = new UserVocab();
//                    userVocab.word = cursor.getString(cursor.getColumnIndex(KEY_WORD));
//                    String json = cursor.getString(cursor.getColumnIndex(KEY_JSON));
//                    Log.e("getAllUserVocab", json);
//                    userVocab.listOfDefEx = (new Gson()).fromJson(json, new TypeToken<ArrayList<PearsonAnswer.DefinitionExamples>>(){}.getType());
//                    Log.e("getAllUserVocab", ""+ userVocab.listOfDefEx.size());
//                    userVocab.date = cursor.getLong(cursor.getColumnIndex(KEY_DATE));
//
//                    int faveInt = cursor.getInt(cursor.getColumnIndex(KEY_FAVE));
//                    userVocab.fave = (faveInt == IS_FAVE);
////                    Log.e("byte", (long)cursor.getLong(cursor.getColumnIndex(KEY_DATE)) + "");
////                    userVocab.dateText = cursor.getString(cursor.getColumnIndex(KEY_DATETEXT));
//
//                    userVocabs.add(userVocab);
//
//                } while(cursor.moveToNext());
//            }
//        } catch (Exception e) {
//            Log.d("userVocab", "error getting user vocab "  + e.toString());
//        } finally {
//            if (cursor != null && !cursor.isClosed()) {
//                cursor.close();
//            }
//        }
////        Collections.reverse(userVocabs);
//        return userVocabs;
    }


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
//                        Log.e("getAllUserVocab", json);
//                        userVocab.listOfDefEx = (new Gson()).fromJson(json, new TypeToken<ArrayList<PearsonAnswer.DefinitionExamples>>(){}.getType());
                        userVocab.listOfDefEx = CustomUVStringAdapter.fromString(json);
//                        Log.e("getAllUserVocab", ""+ userVocab.listOfDefEx.size());
                        userVocab.date = cursor.getLong(cursor.getColumnIndex(KEY_DATE));

                        int faveInt = cursor.getInt(cursor.getColumnIndex(KEY_FAVE));
                        userVocab.fave = (faveInt == IS_FAVE);
//                    Log.e("byte", (long)cursor.getLong(cursor.getColumnIndex(KEY_DATE)) + "");
//                    userVocab.dateText = cursor.getString(cursor.getColumnIndex(KEY_DATETEXT));

                        userVocabs.add(userVocab);

                    } while(cursor.moveToNext() && ((howMany == GET_ALL || (++i <= howMany)))); // limit to 25 the first time so that the user is guaranteed to see something on their screen.



                }
            } catch (Exception e) {
                Log.d("userVocab", "error getting user vocab "  + "" );e.printStackTrace();
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

//        ArrayList<UserVocab> userVocabs = new ArrayList<>();
//
//        // SELECT * FROM POSTS
//        // LEFT OUTER JOIN USERS
//        // ON POSTS.KEY_POST_USER_ID_FK = USERS.KEY_USER_ID
//        String USER_VOCAB_SELECT_QUERY =
//                String.format(Locale.US, "SELECT * FROM %s WHERE %s = %d ORDER BY %s DESC ;",
//                        TABLE_WORDS, KEY_FAVE,
//                        IS_FAVE,
//                        KEY_FAVE_DATE);
//
//
//        SQLiteDatabase db = getReadableDatabase();
//        Log.e("userVocab", "fave querying: " + USER_VOCAB_SELECT_QUERY);
//        Cursor cursor = db.rawQuery(USER_VOCAB_SELECT_QUERY, null);
//        try {
//            if (cursor.moveToFirst()) {
//                do {
//                    UserVocab userVocab = new UserVocab();
//                    userVocab.word = cursor.getString(cursor.getColumnIndex(KEY_WORD));
//                    String json = cursor.getString(cursor.getColumnIndex(KEY_JSON));
////                    userVocab.listOfDefEx = (new Gson()).fromJson(json, new TypeToken<ArrayList<PearsonAnswer.DefinitionExamples>>(){}.getType());
//                    userVocab.listOfDefEx = CustomUVStringAdapter.fromString(json);
//                    userVocab.date = cursor.getLong(cursor.getColumnIndex(KEY_DATE));
//
//                    int faveInt = cursor.getInt(cursor.getColumnIndex(KEY_FAVE));
//                    userVocab.fave = (faveInt == IS_FAVE);
////                    Log.e("byte", (long)cursor.getLong(cursor.getColumnIndex(KEY_DATE)) + "");
////                    userVocab.dateText = cursor.getString(cursor.getColumnIndex(KEY_DATETEXT));
//
//                    userVocabs.add(userVocab);
//
//                } while(cursor.moveToNext());
//            }
//        } catch (Exception e) {
//            Log.d("userVocab", "fave error getting user vocab "  + e.toString());
//        } finally {
//            if (cursor != null && !cursor.isClosed()) {
//                cursor.close();
//            }
//        }
//        return userVocabs;
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
//                    userVocab.listOfDefEx = (new Gson()).fromJson(json, new TypeToken<ArrayList<PearsonAnswer.DefinitionExamples>>(){}.getType());
                        userVocab.listOfDefEx = CustomUVStringAdapter.fromString(json);
                        userVocab.date = cursor.getLong(cursor.getColumnIndex(KEY_DATE));

                        int faveInt = cursor.getInt(cursor.getColumnIndex(KEY_FAVE));
                        userVocab.fave = (faveInt == IS_FAVE);
//                    Log.e("byte", (long)cursor.getLong(cursor.getColumnIndex(KEY_DATE)) + "");
//                    userVocab.dateText = cursor.getString(cursor.getColumnIndex(KEY_DATETEXT));

                        userVocabs.add(userVocab);

                    } while(cursor.moveToNext() && ((howMany == GET_ALL || (++i <= howMany))));
                }
            } catch (Exception e) {
                Log.d("userVocab", "fave error getting user vocab "  + e.toString());
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


    // todo: upsert SQLite
    // Insert a post into the database
    public void addWord(UserVocab userVocab) { // todo: make favorite and addword async
        // Create and/or open the database for writing
        SQLiteDatabase db = getWritableDatabase();

//        Log.e("addWordUV", (new Gson()).toJson(userVocab));


//        for (int i = 0; i < 1000; i++) {

            // It's a good idea to wrap our insert in a transaction. This helps with performance and ensures
            // consistency of the database.
            db.beginTransaction();
            try {
                // The user might already exist in the database (i.e. the same user created multiple posts).
                //long userId = addOrUpdateWord(userVocab.word); //todo: check for duplicates

                ContentValues values = new ContentValues();
                values.put(KEY_WORD, userVocab.word.trim());

//                String json = (new Gson()).toJson(userVocab.listOfDefEx);
                String json = CustomUVStringAdapter.toString(userVocab.listOfDefEx);
                values.put(KEY_JSON, json);
                Log.e("adding word json", json);

                values.put(KEY_DATE, userVocab.date/* + (long)i*/);
                values.put(KEY_FAVE, (userVocab.fave) ? IS_FAVE : NOT_FAVE);

                String queryString = String.format(Locale.US, "INSERT INTO %s VALUES (%s, %s, %s, %s, %s) VALUES (\"%s\", \"%s\", \"%d\", \"%d\");",
                        TABLE_WORDS,
                        KEY_WORD, KEY_JSON, KEY_DATE, KEY_FAVE, KEY_FAVE_DATE,
                        userVocab.word.trim(), json, userVocab.date, (userVocab.fave) ? IS_FAVE : NOT_FAVE, 1+"");


                Log.e("userVocab", "adding: " + queryString);

                // Notice how we haven't specified the primary key. SQLite auto increments the primary key column.
                db.insertOrThrow(TABLE_WORDS, null, values);
                db.setTransactionSuccessful();
            } catch (Exception e) {
                Log.d("userVocab", "\n\n\n\n\n\n\n\n\n\n\n\nEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEError while trying to add post to database + " + e.toString() + "\n\n");
            } finally {
                db.endTransaction();
            }
//        }
    }

    public void deleteWord(UserVocab userVocab) {
        // Create and/or open the database for writing
        SQLiteDatabase db = getWritableDatabase();

        Log.e("deleteWord", "deletingword " + userVocab.word);

        db.beginTransaction();
        try {
            String where= KEY_DATE + '=' + userVocab.date + " AND " + KEY_WORD + " = \"" + userVocab.word+"\"";
            // Notice how we haven't specified the primary key. SQLite auto increments the primary key column.
            Log.e("deleteWord", where);
            db.delete(TABLE_WORDS, where, null);
            db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.d("deleteWord", "\n\n\n\n\n\n\n\n\n\n\n\nEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEError while trying to add post to database + " + e.toString() + "\n\n");
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

            String where= KEY_DATE + '=' + userVocab.date + " AND " + KEY_WORD + " = \"" + userVocab.word+"\"";
            // Notice how we haven't specified the primary key. SQLite auto increments the primary key column.
            Log.e("toggleFavorite", where);
            db.update(TABLE_WORDS, values, where, null);
            db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.d("userVocab", "\n\n\n\n\n\n\n\n\n\n\n\nEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEError while trying to add post to database + " + e.toString() + "\n\n");
        } finally {
            db.endTransaction();
        }
    }

    //endregion
    public void importNative(ArrayList<UserVocab> userVocabArrayList) {
//        ArrayList<UserVocab> userVocabArrayList = (new Gson()).fromJson(json, new TypeToken<ArrayList<UserVocab>>(){}.getType());
        SQLiteDatabase db = getWritableDatabase();

        for (int i = 0 ; i < userVocabArrayList.size() ; i++) {
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
//                    // Get the primary key of the user we just updated
//                    String usersSelectQuery = String.format("SELECT %s FROM %s WHERE %s = ?",
//                            KEY_ID, TABLE_WORDS, KEY_WORD);
//                    Cursor cursor = db.rawQuery(usersSelectQuery, new String[]{String.valueOf(word)});
//                    try {
//                        if (cursor.moveToFirst()) {
//                            userId = cursor.getInt(0);
//                            db.setTransactionSuccessful();
//                        }
//                    } finally {
//                        if (cursor != null && !cursor.isClosed()) {
//                            cursor.close();
//                        }
//                    }
                } /*else {
                    // user with this userName did not already exist, so insert new user. todo... what?
                    userId = db.insertOrThrow(TABLE_WORDS, null, values);
                    db.setTransactionSuccessful();
                }*/
            } catch (Exception e) {
                Log.d("userVocab", "Error while trying to add or update user");
            } /*finally {
                db.endTransaction();
            }*/
        }
    }

// region check duplicate
// Insert or update a user in the database

    // Since SQLite doesn't support "upsert" we need to fall back on an attempt to UPDATE (in case the
    // user already exists) optionally followed by an INSERT (in case the user does not already exist).
    // Unfortunately, there is a bug with the insertOnConflict method
    // (https://code.google.com/p/android/issues/detail?id=13045) so we need to fall back to the more
    // verbose option of querying for the user's primary key if we did an update.
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
    //endregion

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
