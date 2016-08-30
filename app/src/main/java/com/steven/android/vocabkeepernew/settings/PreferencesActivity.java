package com.steven.android.vocabkeepernew.settings;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;

import com.steven.android.vocabkeepernew.R;
import com.steven.android.vocabkeepernew.input.ClipboardWatcherService;
import com.steven.android.vocabkeepernew.utility.NotificationUtility;

/**
 * Created by Steven on 8/29/2016.
 */
public class PreferencesActivity extends AppCompatActivity {
    public static int ENGLISH_KEY = 1;
    public static int CHINESE_KEY = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        getFragmentManager().beginTransaction().replace(android.R.id.content, new MyPreferenceFragment()).commit();
    }

    @Override
    public void onPause() { // commit settings changes
        super.onPause();

        // commit changes made in settings...
        SharedPreferences SP = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
//        String strUserName = SP.getString("username", "NA");
        boolean doPaste = SP.getBoolean("paste",false);
        boolean doShortcut = SP.getBoolean("shortcut", false);
//        String downloadType = SP.getString("downloadType","1");

        if (doPaste) {
            startService(new Intent(getBaseContext(), ClipboardWatcherService.class));
        } else {
            stopService(new Intent(getBaseContext(), ClipboardWatcherService.class));
        }

        if (doShortcut) {
            NotificationUtility.createConvenienceNotif(this);
        } else {
            NotificationUtility.cancelConvenienceNotif(this);
        }
    }

    public static class MyPreferenceFragment extends PreferenceFragment
    {
        @Override
        public void onCreate(final Bundle savedInstanceState)
        {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.preferences);
        }
    }

}