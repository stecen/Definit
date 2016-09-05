package com.scentric.android.definit.useless;

import android.app.IntentService;
import android.content.ClipData;
import android.content.ClipDescription;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteCursorDriver;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQuery;
import android.util.Log;

import com.google.gson.Gson;
import com.scentric.android.definit.utility.GlosbePackage;
import com.scentric.android.definit.get.sqlite.DictionaryDatabaseHelper;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLEncoder;

import javax.net.ssl.HttpsURLConnection;

/**
 * Created by Steven on 11/28/2015.
 *
 * Called by both the dialog in the activity (when entering your own word) and the notification (when getting the text from the clipboard)
 *
 * API I'm using: https://glosbe.com/a-api   -- choose all json objects with the "meaning"
 *
 * Yandex API Key: dict.1.1.20151130T032757Z  .c863c538d4343852.98b37b03d9f7a8ba71de4b6ac2c115958f5580fc
 * Each page that uses data from the Yandex.Dictionary service must display the text "Powered by Yandex.Dictionary" with an active hyperlink to the page https://tech.yandex.com/dictionary/.
 *
 * http://www.droidviews.com/install-rooted-lollipop-on-att-galaxy-s5-sm-g900a/
 *
 * http://developer.pearson.com/apis/dictionaries#!//listEntries
 * ^ PEARSON IS DA BES!!!!!! :D
 *
 *
 * */

// todo: section in the app for "preliminary" vocab words

public class VocabService extends IntentService {
    public final static String GET_FROM_LOCATION = "GET_FROM_LOCATION"; // extra key for location to get the word to define and record from (represented by int)
    public final static int GET_FROM_CLIPBOARD = 1;
    public final static int GET_FROM_DIALOG = 2;
    public final static int GET_FROM_SENT = 3; // everything else - i added this later, to catch the strings within the intent

    public final static String WORD_TO_DEFINE_FROM_DIALOG = "Word to define"; // key

    public static final String SENT_WORD = "sent_word";
    public static final String SENT_DEF = "send_def";

    public final static String DIALOG_TEXT = "DIALOG_TEXT"; // extra key to get the word that the user entered into a dialog

    public final static String SHOW_POPUP = "show_popup"; // key on whether to show popup or definition or not (probably yes) boolean

    // SQLite

    public final static String DEFAULT_NO_DEFINITION = "No definition found";
    public final static String DEFAULT_NO_EXAMPLE = "No example found";



    // Glosbse API
    public final static String GLOSBE_QUERY = "https://glosbe.com/gapi/translate?from=eng&dest=eng&format=json&pretty=true&phrase=";

    public VocabService() {
        super("VocabService");
    }

    //todo: option for user to enter their own definitions

    @Override
    protected void onHandleIntent(Intent intent) {
        // check to make sure clip data is text

        // check where to get the text
        int where = intent.getIntExtra(GET_FROM_LOCATION, GET_FROM_CLIPBOARD+1123923); // make sure u dont get fmo clipboard
        boolean popup = intent.getBooleanExtra(SHOW_POPUP, false);
        if (where == GET_FROM_CLIPBOARD) {
            ClipboardManager clipboardManager = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
            ClipData clipData = clipboardManager.getPrimaryClip();
            String mimeType = clipData.getDescription().getMimeType(0);
            ClipData.Item item = clipData.getItemAt(0);

            // get text and toast / insert into sqlite database
            if (mimeType.equals(ClipDescription.MIMETYPE_TEXT_PLAIN) || mimeType.equals(ClipDescription.MIMETYPE_TEXT_HTML)) {
                String textToDefine = item.coerceToText(this).toString();
                Log.d("lol", "You pressed it and have pasted \"" + textToDefine + "\"");

//                String d1 = VocabService.getDefinition(textToDefine.replace("\\", "").trim(), this); // it gets trimmed later but why not
                GlosbePackage defPackage = getDefinition(textToDefine.replace("\\", "").trim(), this);

                //insertIntoDatabase(textToDefine, d1); // todo : reneable database entry

                if (popup) { // if u wanna popup it up popup the jam yas
                    createPopup(defPackage);
                }
            } else {
                Log.d("lol", "Not plain text but is a " + mimeType + ": " + item.coerceToText(this));
            }

        } else if (where == GET_FROM_DIALOG){ // take the text from the edit text of the custom input
            Log.e("lol", "Defining: " + intent.getStringExtra(WORD_TO_DEFINE_FROM_DIALOG));
            //insertIntoDatabase(intent.getStringExtra(WORD_TO_DEFINE_FROM_DIALOG), VocabService.getDefinition(intent.getStringExtra(WORD_TO_DEFINE_FROM_DIALOG), this));


        } else if (where == GET_FROM_SENT) {
            String textToDefine = intent.getStringExtra(SENT_WORD);
//            String d1 = VocabService.getDefinition(textToDefine.replace("\\", "").trim(), this); // todo: get from sent...it gets trimmed later but why not

            GlosbePackage defPackage = getDefinition(textToDefine.replace("\\", "").trim(), this);
//            insertIntoDatabase(textToDefine, d1);

            if (popup) {
//                createPopup(textToDefine, d1); //todo createpopup
                createPopup(defPackage);
            }
        }

    }

    private void createPopup(GlosbePackage defPackage) {
        Intent disDefIntent = new Intent(getApplicationContext(), DisplayDefinitionPopupActivity.class);
        disDefIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NO_HISTORY);

        Gson gson = new Gson();
        String defPackageJson = gson.toJson(defPackage);

        disDefIntent.putExtra(DisplayDefinitionPopupActivity.SENT_PACKAGE_JSON, defPackageJson);
//        disDefIntent.putExtra(DisplayDefinitionPopupActivity.SENT_WORD, word);
//        disDefIntent.putExtra(DisplayDefinitionPopupActivity.SENT_DEF, def);
        //todo: check if word has spaces, and / or whether it exists in the dictionary
        startActivity(disDefIntent);
    }

    private void insertIntoDatabase(String wordText) {
        insertIntoDatabase(wordText, null);
    }

    private void insertIntoDatabase(String wordText, String d1) {
        wordText = wordText.replace("\\", "").trim();

        if (d1 == null) {
            d1 = DEFAULT_NO_DEFINITION;
        }

        SQLiteDatabase vocabDB = openOrCreateDatabase("vocab.db", MODE_PRIVATE, new SQLiteDatabase.CursorFactory() { // what's this last thing?
            @Override
            public Cursor newCursor(SQLiteDatabase sqLiteDatabase, SQLiteCursorDriver sqLiteCursorDriver, String s, SQLiteQuery sqLiteQuery) {
                return null;
            }
        });

        String exe = "CREATE TABLE IF NOT EXISTS words(_id INTEGER PRIMARY KEY AUTOINCREMENT, word VARCHAR, d1 VARCHAR, e1 VARCHAR, d2 VARCHAR, e2 VARCHAR);";
        vocabDB.execSQL(exe); // word, 2 definitions, 2 examples
//        Log.d("lol", "executing SQL -- " + exe);

        String randomDefinition = "a foo that calls a bar";
        exe = "INSERT INTO words VALUES(NULL, \"" + wordText + "\", \"" + d1 + "\", \""+ DEFAULT_NO_EXAMPLE + "\", \"" + DEFAULT_NO_DEFINITION + "\", \"" + DEFAULT_NO_EXAMPLE + "\");";
        vocabDB.execSQL(exe);
        Log.d("lol", "executing SQL -- " + exe);

    }

    // todo: return not a string, but a collection of words and definitions and examples
    public GlosbePackage getDefinition(String wordText, Context context) {
        String localDefinition = getDefinitionLocal(wordText, context);
        String onlineDefinition = getDefinitionOnline(wordText); //todo: dont leak databases

        GlosbePackage returnPackage = new GlosbePackage();
        if (returnPackage != null) {
            Log.e("rekt", "i am not null");

            returnPackage.localDef.add(localDefinition);
            returnPackage.onlineDef.add(onlineDefinition);

            returnPackage.word = wordText;

            return returnPackage;//todo: web definition returning
        } else {
            return returnPackage;
        }

    }

    public static String getDefinitionLocal(String wordText, Context context) {
        // Dictionary database
        DictionaryDatabaseHelper dictDbHelper = new DictionaryDatabaseHelper(context);
        SQLiteDatabase dictDb = dictDbHelper.getReadableDatabase();

        try {
            wordText = wordText.toUpperCase();
            Log.e("db", "wordText = " +wordText);
            String query = "SELECT * FROM words WHERE word='" + wordText.toUpperCase() + "';";
            Log.e("db", query);
            Cursor cursor = dictDb.rawQuery(query, null);

            Log.e("db", "does cursor exist: " + cursor.getCount());



            if (cursor.moveToFirst()) {
                return java.net.URLDecoder.decode(cursor.getString(2), "UTF-8"); //todo: replace "2" with a cursor.getColumnIndexOrThrow("definition")
            }
        } catch (Exception e) {
            Log.e("db", e.toString());
        }

        return "";
    }

    public static String getDefinitionOnline(String wordText) {
        BufferedReader reader = null;
        URL url;

        try {
            wordText = URLEncoder.encode(wordText.trim(), "ascii");

            String completeURL = GLOSBE_QUERY + wordText;

            url = new URL(completeURL);
            HttpsURLConnection con = (HttpsURLConnection) url.openConnection();
            con.setConnectTimeout(15000);
            con.setDoInput(true);
            con.setDoOutput(false);

            int rc = con.getResponseCode();
            Log.d("lol", "response code = " + String.valueOf(rc) + " for " + completeURL);


            if (rc == HttpsURLConnection.HTTP_OK) { // http://stackoverflow.com/questions/3432263/java-io-ioexception-server-returned-http-response-code-500
                StringBuilder sb = new StringBuilder();
                reader = new BufferedReader(new InputStreamReader(con.getInputStream()));

                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line).append("\n");
                }

                String jsonString = sb.toString();
                Log.d("lol", "jsonString: " + jsonString);

                // Get the definitions out
                JSONObject resObj = new JSONObject(jsonString);
                if (resObj.has("result") && resObj.getString("result").trim().equals("ok")) { // if retrieved a good result
                    if (resObj.has("tuc")) { // has a definition
                        JSONArray tuc = resObj.getJSONArray("tuc");

                        Log.d("lol", "tuc count = " + String.valueOf(tuc.length()));
                        if (tuc.length() > 0) {
                            for (int i = 0; i < tuc.length(); i++) {
                                JSONObject phraseMeaning = tuc.getJSONObject(i);
                                if (phraseMeaning.has("phrase")) { // FOUND A DEFINITION IN THE "PHRASE"
                                    JSONObject phrase = phraseMeaning.getJSONObject("phrase");
                                    if (phrase.has("text")) {
                                        String retVal = phrase.getString("text").trim().replace("\"", "").replace("'", "").replace("\\", "");
                                        Log.d("lol", "returning (phrase) " + retVal + " for definition of " + wordText);
                                        return retVal;
                                    }
                                } else if (phraseMeaning.has("meanings")) {
                                    JSONArray meanings = phraseMeaning.getJSONArray("meanings");
                                    if (meanings.length() > 0) {
                                        for (int m = 0; m < meanings.length(); m++) {
                                            JSONObject mObj = meanings.getJSONObject(m);
                                            if (mObj.has("text")) { // FOUND A DEFINITION IN THE "MEANINGS" ARRAY UNDER "TEXT"
                                                String retVal = mObj.getString("text").trim().replace("\"", "").replace("'", "").replace("\\", "");
                                                Log.d("lol", "returning (meaning text) " + retVal + " for definition of " + wordText);
                                                return retVal;
                                            }
                                        }
                                    }
                                }
                            }
                        } else {
                            return DEFAULT_NO_DEFINITION;
                        }

                    } else { // no definition found
                        return DEFAULT_NO_DEFINITION;
                    }
                } else {
                    Log.d("lol", "Server error");
                    return "Something went wrong with the server.";
                }


            } else {
                StringBuilder sb = new StringBuilder();
                reader = new BufferedReader(new InputStreamReader(con.getErrorStream()));

                String line;
                while ((line = reader.readLine()) != null) {
//                    Log.d("lol", "error line read: " +line);
                    sb.append(line).append("\n");
                }

                Log.d("lol", "error stream: \n" + sb.toString());
            }


        } catch (Exception e) {
            Log.d("lol", e.toString());
        } finally {
            if (reader != null) {
                try {
                    Log.e("Closing reader", "Success");
                    reader.close();
                } catch (IOException e) {
                    Log.e("IOException", e.toString());
                }
            }
        }

        return DEFAULT_NO_DEFINITION; // todo: nothing should rly come here...
    }
}
