package com.scentric.android.definit.showuservocab;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.speech.RecognizerIntent;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.scentric.android.definit.R;
import com.scentric.android.definit.get.sqlite.DictionaryDatabaseHelper;
import com.scentric.android.definit.input.ClipboardWatcherService;
import com.scentric.android.definit.settings.PreferencesActivity;
import com.scentric.android.definit.show.SearchAndShowActivity;
import com.scentric.android.definit.showuservocab.fragment.FragmentRefresher;
import com.scentric.android.definit.showuservocab.fragment.FragmentReselected;
import com.scentric.android.definit.showuservocab.fragment.Pager;
import com.scentric.android.definit.showuservocab.sqlite.UserVocabHelper;
import com.scentric.android.definit.useless.WordDisplayCursorAdapter;
import com.scentric.android.definit.utility.DividerItemDecoration;
import com.scentric.android.definit.utility.NotificationUtility;

import java.io.File;
import java.util.ArrayList;
import java.util.Locale;


public class UserVocabActivity extends AppCompatActivity implements /*RecyclerViewClickListener,*/ TabLayout.OnTabSelectedListener {
    TextView serviceText;
    //    ListView wordList;
    Button startButton, stopButton;
    TextView animationText;

    AppBarLayout appBarLayout;

    TextView toolbarText;

    TabLayout tabLayout;
    ViewPager viewPager;

    RecyclerView recyclerView;
    UserVocabAdapter adapter;

    public FloatingActionButton fab;

    WordDisplayCursorAdapter wordDisplayCursorAdapter;

    UserVocabHelper helper;

    private DictionaryDatabaseHelper dictionaryDatabase;

    public static final String KEY_TASK = "keyTask";
    public static final String IS_BACK = "isBack";

    public static final String PREF_KEY_WINDOW_ASKED = "prefKeyWindow";
    public static final int PREF_YES = 1;
    public static final int PREF_NO = 0;

    public static final int UNSEL_TAB_ALPHA = 90;

    DividerItemDecoration dividerItemDecoration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_vocab);

        appBarLayout = (AppBarLayout) findViewById(R.id.word_list_appbar);
        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null) {
            toolbar.setTitle("Definit");
            Log.e("tool", "Setting support toolbar...");
            setSupportActionBar(toolbar);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                if (appBarLayout != null) {
                    appBarLayout.setElevation(8);
//                    toolbar.setElevation(0);
                }
            }
        }

        fab = (FloatingActionButton) findViewById(R.id.fab);

//        toolbarText = (TextView) findViewById(R.id.uservocab_toolbar_text);

        tabLayout = (TabLayout) findViewById(R.id.tabLayout);
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//            tabLayout.setElevation(0);
//        }
        tabLayout.addTab(tabLayout.newTab()/*.setText("Saved")*/.setIcon(R.drawable.ic_home_white_24dp));
        tabLayout.addTab(tabLayout.newTab()/*.setText("Favorited")*/.setIcon(R.drawable.ic_star_white_24dp));
        tabLayout.addTab(tabLayout.newTab()/*.setText("History")*/.setIcon(R.drawable.ic_history_white_24dp));
        tabLayout.addTab(tabLayout.newTab().setIcon(R.drawable.ic_person_white_24dp));
        tabLayout.getTabAt(0).getIcon().setAlpha(255);
        tabLayout.getTabAt(1).getIcon().setAlpha(UNSEL_TAB_ALPHA);
        tabLayout.getTabAt(2).getIcon().setAlpha(UNSEL_TAB_ALPHA);
        tabLayout.getTabAt(3).getIcon().setAlpha(UNSEL_TAB_ALPHA);
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);


        viewPager = (ViewPager) findViewById(R.id.pager);
        final Pager adapter = new Pager(getSupportFragmentManager(), tabLayout.getTabCount());
        final Pager finalAdapter = adapter;
        viewPager.setAdapter(adapter);
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        viewPager.setOffscreenPageLimit(4);
//        final TextView ftext = toolbarText;
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
//                Log.e("viewpager", "scrolled to" + position);
            }

            @Override
            public void onPageSelected(int position) {
                Log.e("viewpager", "selected " + position);

                FragmentRefresher fragment = (FragmentRefresher) finalAdapter.instantiateItem(viewPager, position);
                fragment.refreshViews();

                appBarLayout.setExpanded(true, true);
                try {
                    switch (position) {
                        case 0:
//                            ftext.setText("Definit");
                            toolbar.setTitle("Definit");

                            Drawable icon0 = tabLayout.getTabAt(0).getIcon();
                            if (icon0 != null) icon0.setAlpha(255);

                            Drawable icon1 = tabLayout.getTabAt(1).getIcon();
                            if (icon1 != null) icon1.setAlpha(UNSEL_TAB_ALPHA);

                            Drawable icon2 = tabLayout.getTabAt(2).getIcon();
                            if (icon2 != null) icon2.setAlpha(UNSEL_TAB_ALPHA);

                            tabLayout.getTabAt(3).getIcon().setAlpha(UNSEL_TAB_ALPHA);

                            fab.show();
                            break;
                        case 1:
//                            ftext.setText("Starred");
                            toolbar.setTitle("Starred");

                            tabLayout.getTabAt(0).getIcon().setAlpha(UNSEL_TAB_ALPHA);
                            tabLayout.getTabAt(1).getIcon().setAlpha(255);
                            tabLayout.getTabAt(2).getIcon().setAlpha(UNSEL_TAB_ALPHA);
                            tabLayout.getTabAt(3).getIcon().setAlpha(UNSEL_TAB_ALPHA);

                            fab.show();
                            break;
                        case 2:
//                            ftext.setText("History");
                            toolbar.setTitle("History");

                            tabLayout.getTabAt(0).getIcon().setAlpha(UNSEL_TAB_ALPHA);
                            tabLayout.getTabAt(1).getIcon().setAlpha(UNSEL_TAB_ALPHA);
                            tabLayout.getTabAt(2).getIcon().setAlpha(255);
                            tabLayout.getTabAt(3).getIcon().setAlpha(UNSEL_TAB_ALPHA);

                            fab.show();
                            break;
                        case 3:
//                            ftext.setText("User");
                            toolbar.setTitle("User");

                            tabLayout.getTabAt(0).getIcon().setAlpha(UNSEL_TAB_ALPHA);
                            tabLayout.getTabAt(1).getIcon().setAlpha(UNSEL_TAB_ALPHA);
                            tabLayout.getTabAt(2).getIcon().setAlpha(UNSEL_TAB_ALPHA);
                            tabLayout.getTabAt(3).getIcon().setAlpha(255);

                            fab.hide();
                            break;
                    }
                } catch (NullPointerException e) {
                    Log.e("viewpager", e + "\n\n\n\n");
                }

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        tabLayout.addOnTabSelectedListener(this); // for view swiping

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
//                super.onTabSelected(tab);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                int position = tab.getPosition();
                Log.e("tab", "reselected  " + position);

                FragmentReselected fragment = (FragmentReselected) finalAdapter.instantiateItem(viewPager, position);
                if (fragment != null) {
                    fragment.reselect();
                }

                appBarLayout.setExpanded(true, true);
            }
        });

        NotificationUtility.createConvenienceNotif(this);
        //start clipboard watcher service
        startService(new Intent(getBaseContext(), ClipboardWatcherService.class));

        doFirstTimeIntro();

        // set default preferences
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);


        final Context ctx = this;
        fab.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                Intent defineIntent = new Intent(ctx, SearchAndShowActivity.class);
                defineIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(defineIntent);
            }
        });
    }

    @Override
    public void onTabSelected(TabLayout.Tab tab) {
        viewPager.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onTabUnselected(TabLayout.Tab tab) {

    }

    @Override
    public void onTabReselected(TabLayout.Tab tab) {

    }

//    public void refreshRecycler () {
//        // todo variable to keep track if there are changes so this activity doesnt have to keep reloading the entire sqlite
//        helper = UserVocabHelper.getInstance(getApplicationContext());
//        ArrayList<UserVocab> userVocabList = helper.getAllUserVocab();
//        Log.e("userVocab", "" + userVocabList.size());
//                Log.e("adapter count",""+ adapter.getItemCount());
//        adapter.replaceData(userVocabList);
//        adapter.notifyDataSetChanged();
//    }
//
//    public void recyclerViewListClicked(View v, int position) {
//        String userVocabString = (new Gson()).toJson(adapter.sortedDataSet.get(position));
//        Log.e("userVocab", "clicked " + position +". " + userVocabString);
//
//        Intent detailIntent = new Intent(this, UserDetailsActivity.class);
//        detailIntent.putExtra(UserDetailsActivity.KEY_JSON, userVocabString);
//        startActivity(detailIntent);
////        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
////        overridePendingTransition(0,0);
//    }

    @Override
    public void onPause() {
        fab.hide();
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        fab.show();

//        StringBuilder sb = new StringBuilder();
//        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
//        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
//            sb.append(service.service.getClassName()).append("\n");
////            if (service.service.getClassName().contains("VocabService")) {
////                serviceText.setText(service.service.getClassName());
////                break;
////            } else {
////                serviceText.setText("Nope");
////            }
//        }
//        serviceText.setText(sb.toString());
    }

    public void doFirstTimeIntro() { // todo: move shevang into the service so no faceplanting
        SharedPreferences sharedPreferences = getPreferences(MODE_PRIVATE);
        int userWindowPermission = sharedPreferences.getInt(PREF_KEY_WINDOW_ASKED, PREF_NO);
        if (userWindowPermission == PREF_NO && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) { // ask permission only the first time
            // Show alert dialog to the user saying a separate permission is needed
            // Launch the settings activity if the user prefers
            Intent myIntent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
            startActivity(myIntent);
        }

        // not first time anymore!!!
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(PREF_KEY_WINDOW_ASKED, PREF_YES);

        editor.apply();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_word_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handles action bar item clicks
        // The action bar will
        // automatically handle clicks on the Home/Up button
        // since I specified a parent activity in AndroidManifest.xml
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement?
        if (id == R.id.action_settings) {
            // region animation test
//            Log.e("build", Build.VERSION.SDK_INT +  " ? " + Build.VERSION_CODES.LOLLIPOP);
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//                // set transitions...
//                Intent intent = new Intent(this, UserDetailsActivity.class);
////                intent.putExtra(UserDetailsActivity.KEY_WORD, )
//                ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(this, (View) animationText, "word");
//                startActivity(intent, options.toBundle());
//            }

            Intent intent = new Intent(this, PreferencesActivity.class);
            startActivity(intent);

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

    }

    private static boolean doesDatabaseExist(Context context, String dbName) {
        File dbFile = context.getDatabasePath(dbName);
        return dbFile.exists();
    }


    public final static int REQ_CODE_SPEECH_INPUT = 92;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Log.e("lspeech", "onActivityResult...");

        switch (requestCode) {
            case REQ_CODE_SPEECH_INPUT: {
                if (resultCode == RESULT_OK && null != data) {

                    ArrayList<String> result = data
                            .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    Log.e("lspeech", result.get(0));
                }
                break;
            }

        }
    }

    public void recognizeSpeech(View v) {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Say something!");
        try {
            startActivityForResult(intent, REQ_CODE_SPEECH_INPUT);
        } catch (ActivityNotFoundException a) {
            Toast.makeText(getApplicationContext(), "Oops. Speech recognition is not supported on this device.",
                    Toast.LENGTH_SHORT).show();
        }

    }

}
