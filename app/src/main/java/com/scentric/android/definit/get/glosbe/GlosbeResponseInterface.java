package com.scentric.android.definit.get.glosbe;

import com.scentric.android.definit.utility.PearsonAnswer;

/**
 * Callback interface for GlosbeAsyncTask main thread post processing
 *
 * Created by Steven on 8/1/2016.
 */
public interface GlosbeResponseInterface {
    void afterGlosbeDefine(PearsonAnswer pearsonAnswer);

}
