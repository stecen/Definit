package com.scentric.android.definit.export;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.scentric.android.definit.R;
import com.scentric.android.definit.sqlite.GetAllWordsAsyncInterface;
import com.scentric.android.definit.sqlite.UserVocab;
import com.scentric.android.definit.sqlite.VocabSQLHelper;
import com.scentric.android.definit.utility.PearsonAnswer;

import java.util.ArrayList;

/**
 * Created by Steven on 9/3/2016.
 */
public class QuizletExportActivity extends AppCompatActivity /*implements GetAllWordsAsyncInterface*/ {
    VocabSQLHelper helper;

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.activity_quizletexport);

        final TextView exportText = (TextView) findViewById(R.id.export_text);

        helper = VocabSQLHelper.getInstance(getApplicationContext());
        helper.getAllUserVocab(new GetAllWordsAsyncInterface() {
            @Override
            public void setWordsData(ArrayList<UserVocab> userVocabList) {
                StringBuilder sb = new StringBuilder();

                for (int i = 0; i < userVocabList.size(); i++) {
                    UserVocab userVocab = userVocabList.get(i);
                    sb.append(userVocab.word.trim())
                            .append("\t");

                    for (int j = 0; j < userVocab.listOfDefEx.size(); j++) { // todo: add a list of examples
                        StringBuilder sbDefEx = new StringBuilder();
                        String defText = userVocab.listOfDefEx.get(j).definition.trim().replaceAll("\t", " ");
                        if (defText.charAt(defText.length() - 1) == '.' ||
                                defText.charAt(defText.length() - 1) == '!' ||
                                defText.charAt(defText.length() - 1) == '?') {

                        } else {
                            defText = defText + ".";
                        }
                        sbDefEx.append(defText.trim());

                        if (userVocab.listOfDefEx.isEmpty() || (userVocab.listOfDefEx.get(j).examples.get(0).trim().equals(PearsonAnswer.DEFAULT_NO_EXAMPLE))) {

                        } else {
                            sbDefEx.append(" \"");

                            String exText = userVocab.listOfDefEx.get(j).examples.get(0).trim().replaceAll("\t", "");
                            if (exText.charAt(exText.length() - 1) == '.' ||
                                    exText.charAt(exText.length() - 1) == '!' ||
                                    exText.charAt(exText.length() - 1) == '?') {

                            } else {
                                exText = exText + ".";
                            }

                            sbDefEx.append(exText);
                            sbDefEx.append("\" ");
                        }
                        sb.append(sbDefEx.toString()).append(" ");
                    }
                    sb.append("\n");
                }
                exportText.setText(sb.toString());
            }
        }, VocabSQLHelper.GET_ALL);

    }
}

