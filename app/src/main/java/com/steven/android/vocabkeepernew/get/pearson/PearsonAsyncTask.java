package com.steven.android.vocabkeepernew.get.pearson;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.gson.Gson;
import com.steven.android.vocabkeepernew.settings.PreferencesActivity;
import com.steven.android.vocabkeepernew.utility.CustomUVStringAdapter;
import com.steven.android.vocabkeepernew.utility.PearsonAnswer;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLEncoder;

import javax.net.ssl.HttpsURLConnection;

/**
 * Created by Steven on 8/1/2016.
 *
 *
 *
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
 * https://api.pearson.com/v2/dictionaries/ldoce5/entries?headword=test&apikey=rsGRiugAUCRGAkIGXfAnzkMcBTcuKKtM
 *
 *
 * */

public class PearsonAsyncTask extends AsyncTask<String, Void, PearsonAnswer>{
    public final static String PEARSON_ENG_QUERY = "https://api.pearson.com/v2/dictionaries/ldoce5/entries?apikey=rsGRiugAUCRGAkIGXfAnzkMcBTcuKKtM&headword=";
    public final static String PEARSON_CHN_QUERY = "https://api.pearson.com/v2/dictionaries/ldec/entries?apikey=rsGRiugAUCRGAkIGXfAnzkMcBTcuKKtM&headword=";
    public final static String PEARSON_SPA_QUERY = "https://api.pearson.com/v2/dictionaries/laes/entries?apikey=rsGRiugAUCRGAkIGXfAnzkMcBTcuKKtM&headword=";

    Context ctx;
    String wordToDefine;

    public PearsonResponseInterface pearsonResponseInterface = null;

    public PearsonAsyncTask(Context context, String word, PearsonResponseInterface pri) {
        ctx = context;
        wordToDefine = word;
        pearsonResponseInterface = pri;
        Log.e("asynctag", "constructor");
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }


    @Override
    protected PearsonAnswer doInBackground(String... strings) {
        Log.e("asynctag","You sent " + wordToDefine);

        PearsonAnswer defPackage = getDefinition(wordToDefine.replace("\\", "").trim(), ctx);
//            insertIntoDatabase(textToDefine, d1);
//        try {
//            Thread.sleep(5000);
//        } catch (Exception e) {
//            Log.e("eeeee", e.toString());
//        }
        return defPackage;
    }

    @Override
    protected void onPostExecute(PearsonAnswer pearsonAnswer) {
        super.onPostExecute(pearsonAnswer);
        pearsonResponseInterface.afterPearsonDefine(pearsonAnswer);
    }


    public PearsonAnswer getDefinition(String wordText, Context context) {
        if (wordText.trim().contains(" ")) {
            //todo: return no thing somehow
            //todo: reject spaces
        }

        PearsonAnswer pearsonDefinition = getDefinitionPearson(wordText);

        Gson gson = new Gson();
        Log.e("obj", gson.toJson(pearsonDefinition));

        return pearsonDefinition;

    }

    public PearsonAnswer getDefinitionPearson(String wordText) {
        BufferedReader reader = null;
        URL url;
        PearsonAnswer pearsonAnswer = new PearsonAnswer();
        pearsonAnswer.word = wordText;

        SharedPreferences SP = PreferenceManager.getDefaultSharedPreferences(ctx.getApplicationContext());
        String language = SP.getString("lang", "Lmao");
        Log.e("language", language);
        String queryUrl = PEARSON_ENG_QUERY;
        int langInt = Integer.parseInt(language.trim());
        if (langInt == PreferencesActivity.ENGLISH_KEY) {
            queryUrl = PEARSON_ENG_QUERY; // redundant
        } else if (langInt == PreferencesActivity.CHINESE_KEY) {
            queryUrl = PEARSON_CHN_QUERY;
        } else if (langInt == PreferencesActivity.SPANISH_KEY) {
            queryUrl = PEARSON_SPA_QUERY;
        }

        try {
            wordText = URLEncoder.encode(wordText.trim(), "ascii");

            String completeURL = queryUrl + wordText; // different url based on language

            url = new URL(completeURL);
            HttpsURLConnection con = (HttpsURLConnection) url.openConnection();
            con.setConnectTimeout(10000);
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
                Log.d("lolpearson", "jsonString: " + jsonString);

                JSONObject resObject = new JSONObject(jsonString);
                if (resObject.has("count") && !resObject.isNull("count") && resObject.getInt("count") > 0) {
                    int count = resObject.getInt("count");
                    Log.e("lolpearson", "count is " + Integer.toString(count));

                    if (resObject.has("results") && !resObject.isNull("results") && resObject.getJSONArray("results").length() > 0) {
                        JSONArray results = resObject.getJSONArray("results");
                        // length should = count

                        for (int i = 0; i < results.length(); i++) { //corresponds to elements of PearsonAnswer#definitionExamplesList
                            PearsonAnswer.DefinitionExamples definitionExamples = new PearsonAnswer.DefinitionExamples(); // needs to be static

                            JSONObject answer = results.getJSONObject(i);

                            definitionExamples.wordForm = answer.getString("headword"); //todo make sure it exists

//                            if (!answer.getString("headword").equals(wordText) && i != 0) { // other definitions are related words
//                                continue;
//                            }

                            if (answer.has("part_of_speech")) {
                                definitionExamples.partOfSpeech = answer.getString("part_of_speech");
                            } else {
                                definitionExamples.partOfSpeech = ("---"); // no part of speech
                            }

                            if (answer.has("senses") && !answer.isNull("senses") && answer.getJSONArray("senses").length() != 0) {
                                JSONArray senses = answer.getJSONArray("senses");

                                if (senses.length() > 0) {
                                    JSONObject sense0 = senses.getJSONObject(0);

                                    if (langInt == PreferencesActivity.ENGLISH_KEY) { // get english stuff
                                        if (sense0.has("definition") && !sense0.isNull("definition")) { // "definition" or "translation" depending on the language
                                            JSONArray definitions = sense0.getJSONArray("definition");
                                            if (definitions.length() > 0) {
                                                definitionExamples.definition = definitions.getString(0).replace(CustomUVStringAdapter.BETWEEN_BREAK, "");
                                            } else {
                                                definitionExamples.definition = PearsonAnswer.DEFAULT_NO_DEFINITION;
                                            }
                                        } else {
                                            definitionExamples.definition = PearsonAnswer.DEFAULT_NO_DEFINITION;
                                        }

                                        if (sense0.has("examples") && !sense0.isNull("examples")) {
                                            JSONArray examples = sense0.getJSONArray("examples");
                                            if (examples.length() > 0) {
                                                if (examples.getJSONObject(0).has("text") && !examples.getJSONObject(0).isNull("text")) {
                                                    definitionExamples.examples.add(examples.getJSONObject(0).getString("text").replace(CustomUVStringAdapter.BETWEEN_BREAK, ""));
                                                } else {
                                                    definitionExamples.examples.add(PearsonAnswer.DEFAULT_NO_EXAMPLE);
                                                }
                                            } else {
                                                definitionExamples.examples.add(PearsonAnswer.DEFAULT_NO_EXAMPLE);
                                            }
                                        } else {
                                            definitionExamples.examples.add(PearsonAnswer.DEFAULT_NO_EXAMPLE);
                                        }
                                    } else if (langInt == PreferencesActivity.CHINESE_KEY) {
                                        if (sense0.has("translation") && !sense0.isNull("translation")) {
                                            String translation = sense0.getString("translation");
                                            if (translation != null) {
                                                definitionExamples.definition = translation.replace(CustomUVStringAdapter.BETWEEN_BREAK, "");
                                            } else {
                                                definitionExamples.definition = PearsonAnswer.DEFAULT_NO_DEFINITION;
                                            }
                                        } else {
                                            definitionExamples.definition = PearsonAnswer.DEFAULT_NO_DEFINITION;
                                        }

                                        if (sense0.has("examples") && !sense0.isNull("examples")) {
                                            JSONArray examples = sense0.getJSONArray("examples");
                                            if (examples.length() > 0) {
                                                if (examples.getJSONObject(0).has("text") && !examples.getJSONObject(0).isNull("text")) {
                                                    definitionExamples.examples.add(examples.getJSONObject(0).getString("text").replace(CustomUVStringAdapter.BETWEEN_BREAK, ""));
                                                } else {
                                                    definitionExamples.examples.add(PearsonAnswer.DEFAULT_NO_EXAMPLE);
                                                }
                                            } else {
                                                definitionExamples.examples.add(PearsonAnswer.DEFAULT_NO_EXAMPLE);
                                            }
                                        } else {
                                            definitionExamples.examples.add(PearsonAnswer.DEFAULT_NO_EXAMPLE);
                                        }

                                    } else if (langInt == PreferencesActivity.SPANISH_KEY) {
                                        if (sense0.has("translations") && !sense0.isNull("translations")) { // "definition" or "translation" depending on the language
                                            JSONArray translations = sense0.getJSONArray("translations");
                                            if (translations.length() > 0) {
                                                if (translations.getJSONObject(0).has("text") && !translations.getJSONObject(0).isNull("text")) {
                                                    JSONArray text = translations.getJSONObject(0).getJSONArray("text");
                                                    if (text.length() > 0 && !text.isNull(0)) {
                                                        definitionExamples.definition = text.getString(0).replace(CustomUVStringAdapter.BETWEEN_BREAK, "");
                                                    } else {
                                                        definitionExamples.definition = PearsonAnswer.DEFAULT_NO_DEFINITION;
                                                    }
                                                } else {
                                                    definitionExamples.definition = PearsonAnswer.DEFAULT_NO_DEFINITION;
                                                }

                                                // get example
                                                if (translations.getJSONObject(0).has("example") && !translations.getJSONObject(0).isNull("example")) {
                                                    JSONArray examples = translations.getJSONObject(0).getJSONArray("example");
                                                    if (examples.length() > 0) {
                                                        if (examples.getJSONObject(0).has("text") && !examples.getJSONObject(0).isNull("text")) {
                                                            definitionExamples.examples.add(examples.getJSONObject(0).getString("text").replace(CustomUVStringAdapter.BETWEEN_BREAK, ""));
                                                        } else {
                                                            definitionExamples.examples.add(PearsonAnswer.DEFAULT_NO_EXAMPLE);
                                                        }
                                                    } else {
                                                        definitionExamples.examples.add(PearsonAnswer.DEFAULT_NO_EXAMPLE);
                                                    }
                                                } else {
                                                    definitionExamples.examples.add(PearsonAnswer.DEFAULT_NO_EXAMPLE);
                                                }


                                            } else {
                                                definitionExamples.definition = PearsonAnswer.DEFAULT_NO_DEFINITION;
                                                definitionExamples.examples.add(PearsonAnswer.DEFAULT_NO_EXAMPLE);
                                            }
                                        } else {
                                            definitionExamples.definition = PearsonAnswer.DEFAULT_NO_DEFINITION;
                                            definitionExamples.examples.add(PearsonAnswer.DEFAULT_NO_EXAMPLE);
                                        }

                                    }


                                } else {
                                    definitionExamples.wordForm = PearsonAnswer.DEFAULT_NO_DEFINITION;
                                    definitionExamples.definition = PearsonAnswer.DEFAULT_NO_DEFINITION;
                                    definitionExamples.examples.add(PearsonAnswer.DEFAULT_NO_EXAMPLE);
                                }

                                pearsonAnswer.definitionExamplesList.add(definitionExamples);
                            } else {
                                definitionExamples.wordForm = PearsonAnswer.DEFAULT_NO_DEFINITION;
                                definitionExamples.definition = PearsonAnswer.DEFAULT_NO_DEFINITION;
                                definitionExamples.examples.add(PearsonAnswer.DEFAULT_NO_EXAMPLE);
                                definitionExamples.partOfSpeech = "---";

                                pearsonAnswer.definitionExamplesList.add(definitionExamples);
                            }

                        }

                        return pearsonAnswer; // not needed

                    } else {
                        PearsonAnswer.DefinitionExamples definitionExamples = new PearsonAnswer.DefinitionExamples();
                        definitionExamples.wordForm = PearsonAnswer.DEFAULT_NO_DEFINITION;
                        definitionExamples.definition = PearsonAnswer.DEFAULT_NO_DEFINITION;
                        definitionExamples.examples.add(PearsonAnswer.DEFAULT_NO_EXAMPLE);
                        definitionExamples.partOfSpeech = "---";

                        pearsonAnswer.definitionExamplesList.add(definitionExamples);
                    }
                } else {
                    PearsonAnswer.DefinitionExamples definitionExamples = new PearsonAnswer.DefinitionExamples();
                    definitionExamples.wordForm = PearsonAnswer.DEFAULT_NO_DEFINITION;
                    definitionExamples.definition = PearsonAnswer.DEFAULT_NO_DEFINITION;
                    definitionExamples.examples.add(PearsonAnswer.DEFAULT_NO_EXAMPLE);
                    definitionExamples.partOfSpeech = "---";

                    pearsonAnswer.definitionExamplesList.add(definitionExamples); //same
                }


            } else {
                StringBuilder sb = new StringBuilder();
                reader = new BufferedReader(new InputStreamReader(con.getErrorStream()));

                String line;
                while ((line = reader.readLine()) != null) {
//                    Log.d("lol", "error line read: " +line);
                    sb.append(line).append("\n");
                }

                Log.d("pearson", "errorstream: \n" + sb.toString());


                PearsonAnswer.DefinitionExamples definitionExamples = new PearsonAnswer.DefinitionExamples();
                definitionExamples.wordForm = PearsonAnswer.DEFAULT_NO_DEFINITION;
                definitionExamples.definition = PearsonAnswer.DEFAULT_NO_DEFINITION;
                definitionExamples.examples.add(PearsonAnswer.DEFAULT_NO_EXAMPLE);
                definitionExamples.partOfSpeech = "---";

                pearsonAnswer.definitionExamplesList.add(definitionExamples); //same
            }


        } catch (SocketTimeoutException e) {
            PearsonAnswer.DefinitionExamples definitionExamples = new PearsonAnswer.DefinitionExamples();
            definitionExamples.wordForm = PearsonAnswer.DEFAULT_NO_DEFINITION;
            definitionExamples.definition = PearsonAnswer.DEFAULT_NO_DEFINITION;
            definitionExamples.examples.add(PearsonAnswer.DEFAULT_NO_EXAMPLE);
            definitionExamples.partOfSpeech = "---";

            pearsonAnswer.definitionExamplesList.add(definitionExamples); //same

            Log.d("pearson","sockettimeout");
        } catch (Exception e) {
            PearsonAnswer.DefinitionExamples definitionExamples = new PearsonAnswer.DefinitionExamples();
            definitionExamples.wordForm = PearsonAnswer.DEFAULT_NO_DEFINITION;
            definitionExamples.definition = PearsonAnswer.DEFAULT_NO_DEFINITION;
            definitionExamples.examples.add(PearsonAnswer.DEFAULT_NO_EXAMPLE);
//            definitionExamples.partOfSpeech = "---";

            pearsonAnswer.definitionExamplesList.add(definitionExamples); //same

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

        return pearsonAnswer;
    }

}
