package com.steven.android.vocabkeepernew.utility;

/**
 * Created by Steven on 8/9/2016.
 */

import android.util.Log;

import java.util.ArrayList;

/**
 * Created by Steven on 7/16/2016.
 */
public class DefinitionPackage {
    public String word;
    public ArrayList<String> localDef, onlineDef, examples;

    public DefinitionPackage() {
        Log.e("y tho", "constructor called");
        // make sure the strings and array lists are nwewed..

        word = "";
        localDef = new ArrayList<>();
        onlineDef = new ArrayList<>();
        examples = new ArrayList<>();
    }
}