package com.scentric.android.definit.utility;

/**
 * Created by Steven on 8/9/2016.
 */

import android.util.Log;

import java.util.ArrayList;

/**
 * Created by Steven on 7/16/2016.
 *
 * Glosbe-returned definition and example representation
 *
 */
public class GlosbePackage {
    public String word;
    public ArrayList<String> localDef, onlineDef, examples;

    public GlosbePackage() {
        word = "";
        localDef = new ArrayList<>();
        onlineDef = new ArrayList<>();
        examples = new ArrayList<>();
    }
}