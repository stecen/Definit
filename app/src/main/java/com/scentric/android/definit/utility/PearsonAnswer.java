package com.scentric.android.definit.utility;

import com.scentric.android.definit.get.glosbe.GlosbeAsyncTask;

import java.util.ArrayList;

/**
 * Created by Steven on 8/3/2016.
 */
public class PearsonAnswer {

    public final static String DEFAULT_NO_DEFINITION = "No definition found";
    public final static String DEFAULT_NO_EXAMPLE = "No example found";

    public ArrayList<DefinitionExamples> definitionExamplesList;

    public String word;

    public PearsonAnswer() {
        definitionExamplesList = new ArrayList<>();
    }

    public static class DefinitionExamples {
        public String partOfSpeech;
        public String definition;
        public String wordForm; // ie. doubtful, doubt, doubting.
        public ArrayList<String> examples;
        public ArrayList<String> ipa;

        public DefinitionExamples() {
            partOfSpeech = "---";
            definition = GlosbeAsyncTask.DEFAULT_NO_DEFINITION;
            wordForm = "";
            examples = new ArrayList<>();
            ipa = new ArrayList<>();
        }


    }


}
