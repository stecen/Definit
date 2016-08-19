package com.steven.android.vocabkeepernew.utility;

import android.util.Log;

import com.steven.android.vocabkeepernew.get.PearsonAsyncTask;

import java.util.Comparator;

/**
 * Created by Steven on 8/15/2016.
 */
public class PearsonComparator implements Comparator<PearsonAnswer.DefinitionExamples> {
    String mainWord;

    public PearsonComparator(String mainWord) {
        this.mainWord = mainWord;
    }

    @Override
    public int compare(PearsonAnswer.DefinitionExamples de1, PearsonAnswer.DefinitionExamples de2) {

        // sort by distance, but for the ones with the same text as mainWord, prioritize the ones with examples
        boolean e1 = de1.wordForm.trim().toLowerCase().equals(mainWord);
        boolean e2 = de2.wordForm.trim().toLowerCase().equals(mainWord);
        if (e1 && !e2) {
            return -1;
        }
        if (!e1 && e2) {
            return 1;
        }
        if (e1 && e2) {
            boolean h1 = (de1.examples.get(0).equals(PearsonAsyncTask.DEFAULT_NO_EXAMPLE));
            boolean h2 = (de2.examples.get(0).equals(PearsonAsyncTask.DEFAULT_NO_EXAMPLE));

            if (h1 && !h2) {
                return -1;
            }
            if (h2 && !h1) {
                return 1;
            }

            return 0;
        }

        // if both words are different from the main words

        int dis1 = computeLevenshteinDistance(de1.wordForm, mainWord);
        int dis2 = computeLevenshteinDistance(de2.wordForm, mainWord);

//        Log.e("lev", de1.wordForm + " to " + mainWord + " = " + dis1);
//        Log.e("lev", de2.wordForm + " to " + mainWord + " = " + dis2);

        if (dis1 < dis2) {
            return -1;
        } else if (dis1 < dis2) {
            return 1;
        } else {
            return 0;
        }
    }

    private static int minimum(int a, int b, int c) {
        return Math.min(Math.min(a, b), c);
    }

    public static int computeLevenshteinDistance(CharSequence lhs, CharSequence rhs) {
        int[][] distance = new int[lhs.length() + 1][rhs.length() + 1];

        for (int i = 0; i <= lhs.length(); i++)
            distance[i][0] = i;
        for (int j = 1; j <= rhs.length(); j++)
            distance[0][j] = j;

        for (int i = 1; i <= lhs.length(); i++)
            for (int j = 1; j <= rhs.length(); j++)
                distance[i][j] = minimum(
                        distance[i - 1][j] + 1,
                        distance[i][j - 1] + 1,
                        distance[i - 1][j - 1] + ((lhs.charAt(i - 1) == rhs.charAt(j - 1)) ? 0 : 1));

        return distance[lhs.length()][rhs.length()];
    }
}