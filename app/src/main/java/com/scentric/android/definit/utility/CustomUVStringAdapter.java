package com.scentric.android.definit.utility;

import java.util.ArrayList;

/**
 * Created by Steven on 8/31/2016.
 *
 * Representing UserVocab in a custom way in SQL
 *
 */
public class CustomUVStringAdapter {
    public static final String EXAMPLE_BREAK = "EXAMPLE_BREAK_DEFINIT_SPLIT_HERE_GIRAFFES";
    public static final String DEFINE_BREAK = "DEFINE_BREAK_GIRAFFE_SPLIT_HERE_GIRAFFES";
    public static final String BETWEEN_BREAK = "\n";

    public static String toString(ArrayList<PearsonAnswer.DefinitionExamples> listOfDefEx) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < listOfDefEx.size(); i++) {
            sb.append(DEFINE_BREAK)
                    .append(" ")
                    .append(listOfDefEx.get(i).definition)
                    .append("\n");

            if (listOfDefEx.get(i).examples.size() > 0) {
                for (int j = 0; j < listOfDefEx.get(i).examples.size(); j++) {
                    sb.append(EXAMPLE_BREAK)
                            .append(" ")
                            .append(listOfDefEx.get(i).examples.get(j).trim())
                            .append("\n");
                }
            } else {
                sb.append(EXAMPLE_BREAK)
                        .append(" ")
                        .append(PearsonAnswer.DEFAULT_NO_EXAMPLE)
                        .append("\n");
            }
        }

        return sb.toString();
    }

    public static ArrayList<PearsonAnswer.DefinitionExamples> fromString(String s) {
        String[] splitted = s.split("\n");
//        Log.e("splitted", splitted.length + "");

        ArrayList<PearsonAnswer.DefinitionExamples> list = new ArrayList<>();

        for (int line = 0; line < splitted.length; ) {
            if (splitted[line].substring(0, DEFINE_BREAK.length()).equals(DEFINE_BREAK.trim())) {
                PearsonAnswer.DefinitionExamples de = new PearsonAnswer.DefinitionExamples();
                de.definition = splitted[line].substring(DEFINE_BREAK.length() + 1).trim();

                line++;

                while (line < splitted.length) {
                    if (splitted[line].substring(0, EXAMPLE_BREAK.length()).equals(EXAMPLE_BREAK.trim())) {
                        de.examples.add(splitted[line].substring(EXAMPLE_BREAK.length() + 1).trim());
                        line++;
                    } else {
//                        line++;
                        break;
                    }

                }

                list.add(de);
            }
        }

        return list;

    }
}
