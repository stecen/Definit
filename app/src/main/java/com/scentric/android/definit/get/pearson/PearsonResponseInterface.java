package com.scentric.android.definit.get.pearson;

import com.scentric.android.definit.utility.PearsonAnswer;

/**
 * Callback for PearsonAsyncTask for main thread calls
 *
 * Created by Steven on 8/9/2016.
 */
public interface PearsonResponseInterface {
    void afterPearsonDefine(PearsonAnswer pearsonAnswer);
}
