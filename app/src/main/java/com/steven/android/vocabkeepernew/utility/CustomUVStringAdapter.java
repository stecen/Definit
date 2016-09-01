package com.steven.android.vocabkeepernew.utility;

import android.util.Log;

import com.steven.android.vocabkeepernew.showuservocab.sqlite.UserVocab;

import java.util.ArrayList;

/**
 * Created by Steven on 8/31/2016.
 */
public class CustomUVStringAdapter {
    public static final String EXAMPLE_BREAK = "EXAMPLE_BREAK_DEFINIT_SPLIT_HERE_GIRAFFES";
    public static final String BETWEEN_BREAK = "\n";

    public static String toString(ArrayList<PearsonAnswer.DefinitionExamples> listOfDefEx) {
        StringBuilder sb = new StringBuilder();

        //assumes that there is only one example. todo: support for an actual list of examples
        for (int i = 0; i < listOfDefEx.size(); i++) {
            // assumes that String contains no newlines
            sb.append(listOfDefEx.get(i).definition)
              .append(" ")
              .append(EXAMPLE_BREAK)
              .append(" ");
            if ((listOfDefEx.get(i).examples.get(0)) != null){
                sb.append(listOfDefEx.get(i).examples.get(0));
            } else {
                sb.append(PearsonAnswer.DEFAULT_NO_EXAMPLE);
            }
            sb.append("\n");
        }

        return sb.toString();
    }

    public static ArrayList<PearsonAnswer.DefinitionExamples> fromString(String s) {
        String[] splitted = s.split("\n");
        Log.e("splitted", splitted.length + "");

        ArrayList<PearsonAnswer.DefinitionExamples> list = new ArrayList<>();

        for (int defIdx = 0; defIdx < splitted.length; defIdx++) {
            String[] defExSplitted = splitted[defIdx].split(EXAMPLE_BREAK);
            Log.e("defExSplitted", defExSplitted.length + ""); // should always equal 2
            PearsonAnswer.DefinitionExamples de = new PearsonAnswer.DefinitionExamples();
            de.definition = defExSplitted[0].trim();
            de.examples.add(defExSplitted[1].trim());

            list.add(de);
        }

        return list;

    }
}
