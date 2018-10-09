package com.scentric.android.definit.get.glosbe;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.Gson;
import com.scentric.android.definit.get.sqlite.DictionaryDatabaseHelper;
import com.scentric.android.definit.settings.PreferencesActivity;
import com.scentric.android.definit.utility.GlosbePackage;
import com.scentric.android.definit.utility.PearsonAnswer;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLEncoder;

import javax.net.ssl.HttpsURLConnection;

/**
 * Created by Steven on 8/1/2016.
 * <p>
 * <p>
 * <p>
 * Created by Steven on 11/28/2015.
 * <p>
 * Called by both the dialog in the activity (when entering your own word) and the notification (when getting the text from the clipboard)
 * <p>
 * API I'm using: https://glosbe.com/a-api   -- choose all json objects with the "meaning"
 * <p>
 * Yandex API Key: dict.1.1.20151130T032757Z  .c863c538d4343852.98b37b03d9f7a8ba71de4b6ac2c115958f5580fc
 * Each page that uses data from the Yandex.Dictionary service must display the text "Powered by Yandex.Dictionary" with an active hyperlink to the page https://tech.yandex.com/dictionary/.
 * <p>
 * http://www.droidviews.com/install-rooted-lollipop-on-att-galaxy-s5-sm-g900a/
 * <p>
 * http://developer.pearson.com/apis/dictionaries#!//listEntries
 * ^ Pearson is superior
 * <p>
 * https://api.pearson.com/v2/dictionaries/ldoce5/entries?headword=test&apikey=rsGRiugAUCRGAkIGXfAnzkMcBTcuKKtM
 */

public class GlosbeAsyncTask extends AsyncTask<String, Void, PearsonAnswer> {
    Context ctx;
    String wordToDefine;

    boolean didFail = false;

    public final static String GLOSBE_ENG_QUERY = "https://glosbe.com/gapi/translate?from=eng&dest=eng&format=json&pretty=true&phrase=";
    public final static String GLOSBE_SPA_QUERY = "https://glosbe.com/gapi/translate?from=eng&dest=spa&format=json&pretty=true&phrase=";
    public final static String GLOSBE_CHI_QUERY = "https://glosbe.com/gapi/translate?from=eng&dest=zho&format=json&pretty=true&phrase=";

    public final static String DEFAULT_NO_DEFINITION = "No definition found";
    public final static String DEFAULT_NO_EXAMPLE = "No example found";

    public GlosbeResponseInterface glosbeResponseInterface = null;

    public String headWord = "";

    public GlosbeAsyncTask(Context context, String word, GlosbeResponseInterface adr) {
        ctx = context;
        wordToDefine = word;
        glosbeResponseInterface = adr;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected PearsonAnswer doInBackground(String... strings) {
        Log.d("async", "You sent " + wordToDefine);

        GlosbePackage defPackage = getDefinition(wordToDefine.replace("\\", "").trim(), ctx);

        Log.e("glosbe", (new Gson()).toJson(defPackage));

        PearsonAnswer pearsonAnswer = new PearsonAnswer();

        pearsonAnswer.word = wordToDefine.trim();
        PearsonAnswer.DefinitionExamples definitionExamples = new PearsonAnswer.DefinitionExamples();
        definitionExamples.partOfSpeech = "---";
        definitionExamples.definition = defPackage.onlineDef.get(0); // only one element right now
        definitionExamples.examples.add(DEFAULT_NO_EXAMPLE);
        definitionExamples.wordForm = headWord; //
        pearsonAnswer.definitionExamplesList.add(definitionExamples);

        // adapt defpack to pearson answer here

        return pearsonAnswer;
    }

    @Override
    protected void onPostExecute(PearsonAnswer pearsonAnswer) {
        // adapt defpack to pearson answer here
        super.onPostExecute(pearsonAnswer);
        if (didFail) {
            Toast.makeText(ctx, "Oops! :(", Toast.LENGTH_SHORT).show();
        }

        glosbeResponseInterface.afterGlosbeDefine(pearsonAnswer);
    }


    public GlosbePackage getDefinition(String wordText, Context context) {
        String onlineDefinition = getDefinitionOnline(wordText);

        GlosbePackage returnPackage = new GlosbePackage();
        if (returnPackage != null) {
            returnPackage.onlineDef.add(onlineDefinition);

            returnPackage.word = wordText;

            return returnPackage;
        } else {
            return returnPackage;
        }

    }

    public String getDefinitionLocal(String wordText, Context context) {
        // Dictionary database
        DictionaryDatabaseHelper dictDbHelper = new DictionaryDatabaseHelper(context);
        SQLiteDatabase dictDb = dictDbHelper.getReadableDatabase();

        try {
            wordText = wordText.toUpperCase();
            Log.e("db", "wordText = " + wordText);
            String query = "SELECT * FROM words WHERE word='" + wordText.toUpperCase() + "';";
            Log.e("db", query);
            Cursor cursor = dictDb.rawQuery(query, null);

            Log.e("db", "does cursor exist: " + cursor.getCount());


            if (cursor.moveToFirst()) {
                return java.net.URLDecoder.decode(cursor.getString(2), "UTF-8");
            }
        } catch (Exception e) {
            Log.e("db", e.toString());
        }

        return "";
    }


    public String getDefinitionOnline(String wordText) {
        BufferedReader reader = null;
        URL url;

        SharedPreferences SP = PreferenceManager.getDefaultSharedPreferences(ctx.getApplicationContext());
        String language = SP.getString("lang", "1");
        Log.e("languageglosbe", language);
        String queryUrl = GLOSBE_ENG_QUERY;
        int langInt = Integer.parseInt(language.trim());
        if (langInt == PreferencesActivity.ENGLISH_KEY) {
            queryUrl = GLOSBE_ENG_QUERY; // redundant
        } else if (langInt == PreferencesActivity.CHINESE_KEY) {
            queryUrl = GLOSBE_CHI_QUERY;
        } else if (langInt == PreferencesActivity.SPANISH_KEY) {
            queryUrl = GLOSBE_SPA_QUERY;
        }

        try {
            wordText = URLEncoder.encode(wordText.trim(), "ascii");

            String completeURL = queryUrl + wordText;

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
                while ((line = reader.readLine()) != null) { // todo check for null
                    sb.append(line).append("\n");
                }

                String jsonString = sb.toString();
                Log.d("lol", "jsonString: " + jsonString);

                // Get the definitions out
                JSONObject resObj = new JSONObject(jsonString);
                if (resObj.has("result") && !(resObj.isNull("result")) && resObj.getString("result").trim().equals("ok")) { // if retrieved a good result

                    if (resObj.has("phrase") && !resObj.isNull("phrase")) {
                        headWord = resObj.getString("phrase");
                    } else {
                        headWord = wordToDefine;
                    }

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
                                        retVal = retVal.replaceAll("\\[(.*?)\\]", ""); // todo: modularize
                                        Log.d("lol", "returning (phrase) " + retVal + " for definition of " + wordText);
                                        return retVal;
                                    }
                                } else if (phraseMeaning.has("meanings") && !phraseMeaning.isNull("meanings")) {
                                    JSONArray meanings = phraseMeaning.getJSONArray("meanings");
                                    if (meanings.length() > 0) {
                                        for (int m = 0; m < meanings.length(); m++) {
                                            JSONObject mObj = meanings.getJSONObject(m);
                                            if (mObj.has("text") && !mObj.isNull("text")) { // FOUND A DEFINITION IN THE "MEANINGS" ARRAY UNDER "TEXT"
                                                String retVal = mObj.getString("text").trim().replace("\"", "").replace("'", "").replace("\\", "");
                                                retVal = retVal.replaceAll("\\[(.*?)\\]", "");
                                                Log.d("glosbe", "returning (meaning text) " + retVal + " for definition of " + wordText);
                                                return retVal;
                                            } else {
                                                return DEFAULT_NO_DEFINITION;
                                            }
                                        }
                                    } else {
                                        return DEFAULT_NO_DEFINITION;
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
                    Log.d("Server", "Server error");
                    return "Something went wrong with the server.";
                }


            } else {
                StringBuilder sb = new StringBuilder();
                reader = new BufferedReader(new InputStreamReader(con.getErrorStream()));

                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line).append("\n");
                }

                Log.d("lol", "error stream: \n" + sb.toString());

                throw new IOException();
            }


        } catch (JSONException e) {
            Log.d("lol", e.toString());

            didFail = true;
        } catch (IOException e) {
            Log.d("lol", e.toString());

            didFail = true;
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

        return DEFAULT_NO_DEFINITION;
    }


}
