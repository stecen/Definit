package com.scentric.android.definit.utility;

import java.util.ArrayList;

/**
 * Created by Steven on 8/31/2016.
 */
public class CustomUVStringAdapter {
    public static final String EXAMPLE_BREAK = "EXAMPLE_BREAK_DEFINIT_SPLIT_HERE_GIRAFFES";
    public static final String DEFINE_BREAK = "DEFINE_BREAK_GIRAFFE_SPLIT_HERE_GIRAFFES";
    public static final String BETWEEN_BREAK = "\n";

    public static String toString(ArrayList<PearsonAnswer.DefinitionExamples> listOfDefEx) {
        StringBuilder sb = new StringBuilder();

//        //assumes that there is only one example. todo: support for an actual list of examples
//        for (int i = 0; i < listOfDefEx.size(); i++) {
//            // assumes that String contains no newlines
//            sb.append(listOfDefEx.get(i).definition)
//              .append(" ")
//              .append(EXAMPLE_BREAK)
//              .append(" ");
//            if ((listOfDefEx.get(i).examples.get(0)) != null){
//                sb.append(listOfDefEx.get(i).examples.get(0));
//            } else {
//                sb.append(PearsonAnswer.DEFAULT_NO_EXAMPLE);
//            }
//            sb.append("\n");
//        }
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


//
//        for (int defIdx = 0; defIdx < splitted.length; defIdx++) {
//            String[] defExSplitted = splitted[defIdx].split(EXAMPLE_BREAK);
//            Log.e("defExSplitted", defExSplitted.length + ""); // should always equal 2
//            PearsonAnswer.DefinitionExamples de = new PearsonAnswer.DefinitionExamples();
//            de.definition = defExSplitted[0].trim();
//            de.examples.add(defExSplitted[1].trim());
//
//            list.add(de);
//        }

        return list;

    }
}
