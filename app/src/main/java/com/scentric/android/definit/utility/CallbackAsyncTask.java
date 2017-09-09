package com.scentric.android.definit.utility;

import android.os.AsyncTask;
import android.util.Log;

import com.scentric.android.definit.get.CallbackAsyncInterface;

/**
 * Created by Steven on 8/14/2016.
 */
public class CallbackAsyncTask extends AsyncTask<Void, Void, Void> {
    int time;
    CallbackAsyncInterface cbi;

    public CallbackAsyncTask(int millis, CallbackAsyncInterface cb) {
        super();
        time = millis - 50;
        if (time < 0) {
            time += 50; //loloops
        }
        cbi = cb;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        Log.e("callback", "onPostExecute");
        cbi.waitCallback();
    }

    @Override
    protected Void doInBackground(Void... voids) {
        try {
            Thread.sleep(time);
        } catch (Exception e) {
            Log.e("eeeeeeeeeeeeeee", e.toString());
        }
        return null;
    }
}
