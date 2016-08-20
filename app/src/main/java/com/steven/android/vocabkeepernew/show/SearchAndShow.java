package com.steven.android.vocabkeepernew.show;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import com.steven.android.vocabkeepernew.R;

/**
 * Created by Steven on 8/20/2016.
 */
public class SearchAndShow extends AppCompatActivity {
    SearchView searchView;
    Intent comingIntent;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activitiy_searchandshow);

        searchView = (SearchView) findViewById(R.id.toolbar_text);
        SearchManager searchManager = (SearchManager) getSystemService(SEARCH_SERVICE);
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setIconifiedByDefault(false);

        comingIntent = getIntent();
        Log.e("coming", "" + (comingIntent != null));
        if (comingIntent != null && comingIntent.getAction().equals(Intent.ACTION_SEARCH)) {
            String query = comingIntent.getStringExtra(SearchManager.QUERY);

            getDefinition(query);

            // hide keyboard
        }

    }

    public void onNewIntent(Intent intent) {
        Log.e("coming", "onNewIntent");
        if (intent.getAction().equals(Intent.ACTION_SEARCH)) {
            //hide keyboard

            String query = intent.getStringExtra(SearchManager.QUERY);
            getDefinition(query);

            //hide keyboard
            View view = this.getCurrentFocus();
            if (view != null) {
                InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }

        }
    }

    public void getDefinition(String query) {
        Log.e("searchomg", query);
        searchView.setQuery(query,false);
    }
}
