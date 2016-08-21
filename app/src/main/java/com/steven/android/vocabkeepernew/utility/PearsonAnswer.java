package com.steven.android.vocabkeepernew.utility;

import com.steven.android.vocabkeepernew.get.glosbe.GlosbeAsyncTask;

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
        definitionExamplesList = new ArrayList<>(); //todo. while displaying, prioritize those with matching headword forms. doubt=doubt. doubt<doubtfulness
    }

//    public PearsonAnswer(String def) { // definition but no examples -- guaranteed
//        partOfSpeech = "---";
//        definitionExamplesList = new ArrayList<>();
//        definitionExamplesList.add
//    }

    public static class DefinitionExamples {
        public String partOfSpeech;
        public String definition;
        public String wordForm; // ie. doubtful, doubt, doubting.
        public ArrayList<String> examples;
        public ArrayList<String> ipa;

        public DefinitionExamples () {
            partOfSpeech = "---";
            definition = GlosbeAsyncTask.DEFAULT_NO_DEFINITION;
            wordForm = "";
            examples = new ArrayList<>();
            ipa = new ArrayList<>();
        }


    }


}
