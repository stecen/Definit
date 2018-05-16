package com.scentric.android.definit.export;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.scentric.android.definit.R;
import com.scentric.android.definit.showuservocab.sqlite.UserVocab;
import com.scentric.android.definit.showuservocab.sqlite.UserVocabHelper;

import java.util.ArrayList;

/**
 * Created by Steven on 9/3/2016.
 */
public class ImportActivity extends AppCompatActivity {
    EditText importEdit;
    FloatingActionButton fab;
    AppBarLayout appBarLayout;

    @Override
    public void onCreate(Bundle onSavedInstanceState) {
        super.onCreate(onSavedInstanceState);
        setContentView(R.layout.activity_import);

        final Context ctx = this;

        appBarLayout = (AppBarLayout) findViewById(R.id.import_appbar);
        final Toolbar toolbar = (Toolbar) findViewById(R.id.import_toolbar);
        if (toolbar != null) {
            toolbar.setTitle("Import");
            Log.e("tool", "Setting support toolbar...");
            setSupportActionBar(toolbar);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                if (appBarLayout != null) {
                    appBarLayout.setElevation(8);
                }
            }
        }

        importEdit = (EditText) findViewById(R.id.import_edit);
        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                UserVocabHelper helper = UserVocabHelper.getInstance(ctx);
                ArrayList<UserVocab> userVocabList;
                try {
                    userVocabList = (new Gson()).fromJson(importEdit.getText().toString().trim(), new TypeToken<ArrayList<UserVocab>>() {
                    }.getType());
                } catch (Exception e) {
                    Toast.makeText(ctx, "Invalid import format", Toast.LENGTH_SHORT).show();
                    userVocabList = null;
                }
                if (userVocabList != null) {
                    helper.importNative(userVocabList);
                    Toast.makeText(ctx, "Successfully imported words", Toast.LENGTH_SHORT).show();
                    finish();
                }


            }
        });
    }
}
