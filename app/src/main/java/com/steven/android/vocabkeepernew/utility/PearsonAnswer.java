package com.steven.android.vocabkeepernew.utility;

import com.steven.android.vocabkeepernew.get.DefinitionAsyncTask;

import java.util.ArrayList;
import java.util.Comparator;

/**
 * Created by Steven on 8/3/2016.
 */
public class PearsonAnswer {
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
            definition = DefinitionAsyncTask.DEFAULT_NO_DEFINITION;
            examples = new ArrayList<>();
            ipa = new ArrayList<>();
        }


    }


}
