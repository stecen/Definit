package com.scentric.android.definit.showdefinition;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.app.RemoteInput;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Point;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.scentric.android.definit.R;
import com.scentric.android.definit.get.glosbe.GlosbeAsyncTask;
import com.scentric.android.definit.get.glosbe.GlosbeResponseInterface;
import com.scentric.android.definit.get.pearson.PearsonAsyncTask;
import com.scentric.android.definit.get.pearson.PearsonResponseInterface;
import com.scentric.android.definit.showuservocab.sheet.SheetHistorySavedActivity;
import com.scentric.android.definit.utility.HistoryVocab;
import com.scentric.android.definit.sqlite.VocabSQLHelper;
import com.scentric.android.definit.utility.DividerItemDecoration;
import com.scentric.android.definit.utility.NotificationUtility;
import com.scentric.android.definit.utility.PearsonAnswer;
import com.scentric.android.definit.utility.PearsonComparator;
import com.scentric.android.definit.utility.RecyclerViewClickListener;
import com.scentric.android.definit.utility.UserVocab;
import com.scentric.android.definit.utility.ViewUtility;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Locale;

import jp.wasabeef.recyclerview.animators.FadeInRightAnimator;

/**
 * Created by Steven on 8/20/2016.
 *
 * Activity which lets users define words but also coordinates logic relating to pulling data from the internet,
 * displaying definitions, and inserting this information into databases. Much of the logic is processed through an incoming new intent.
 *
 */
public class SearchAndShowActivity extends AppCompatActivity implements PearsonResponseInterface, GlosbeResponseInterface, RecyclerViewClickListener {

    SearchView searchView;
    Intent comingIntent;

    String tag; // for incoming intents to save context -- supply to adapter later to send to uservocab database
                // TODO: attach to each instance of ssactivity like now? or attach to adapter
    int wordIdx = -1; // where the tag should appear in context

    //    ListView defExListView;
    FloatingActionButton fab;
    FrameLayout frame;
    ProgressBar progressBar;
    ImageView histImage;
    //    BottomSheetBehavior bottomSheetBehavior;
    View makeSpaceView;
    CoordinatorLayout coordinatorLayout;
    int coordHeight;

    RecyclerView defExRecycler;
    PearsonAdapter recyclerAdapter;
    DividerItemDecoration dividerItemDecoration;

    CollapsingToolbarLayout collapsingToolbarLayout = null;

    boolean iAlreadyExist = false; // solely for the sake of not displaying the circular reveal animation when it's onNewIntent

    public static boolean shouldShowPreviousTypeWordPopup = true; // self explanatory. but the only time this activity finishes when this is true is when the user presses the system back button

    public static final String SENT_TEXT = "sent_word";
    public static final String SENT_TAG = "sent_tag";
    public static final String SENT_WORD_IDX = "word+idx";
    //    public static final String SENT_DEF = "send_def";
    public static final String SENT_PACKAGE_JSON = "send_package_json";
    //    public static final String KEY_RECOG_NOW = "recognow";
    public static final String KEY_TEXT_REPLY = "KEYTEXTreply";
    public final static int REQ_CODE_SPEECH_INPUT = 92;

    private static final String PEARSON_JSON = "pearson_json";

    public static String lastWord = " "; // not null, please :)

    PearsonAsyncTask pearsonAsyncTask;
    GlosbeAsyncTask glosbeAsyncTask;

    PearsonAnswer pA = null;

    public static final String COLOR_NEUTRAL = "#FFFFFF", COLOR_PRESSED = "#DFDFDF";
    public static final int REMOVE_DURATION = 350;
    public static final int SELECT_DURATION = 75;

    public static final int TOUCH_OUTSIDE = 1; // for out-of-window clicks
    public static final int TOUCH_SEND = 2;
    public static final int TOUCH_FRAME = 3;

    protected boolean selected[] = new boolean[500]; // keeps track of which definitionExamples are clicked in the recyclerview to send to database / show color
    protected int selectedCount = 0; // count to show FAB or not
    boolean endingActivity = false; //disable onclicks when the user wants to send
    boolean finishedGetting = false; // to make sure the activity doesn't end when the user clicks the framelayout before the definitions have loaded

    final float SMALL_FONT = 15f;
    final float BIG_FONT = 16f;
    final float SMALL_HEAD_FONT = 16f;
    final float BIG_HEAD_FONT = 17f;
    boolean doChangeFont = true; // for me to change

    boolean didUserCancel = false; // when the user presses the searchview while the results are still loading

    // dialog activity's maximum height http://stackoverflow.com/questions/6624480/how-to-customize-the-width-and-height-when-show-an-activity-as-a-dialog
    // expanding toolbar https://github.com/chrisbanes/cheesesquare/tree/master/app/src/main/java/com/support/android/designlibdemo

    public int lastIdx = -1; // for adapter to know where to add the bottom filler

    TextView searchInnerText; // for getting the text in it

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_searchandshow);

        adjustSearchViews();

        initLayout();

        comingIntent = getIntent();
        Log.e("coming", "" + (comingIntent != null));


        // TODO: clean repeated code up
        // TODO: for now, only second case can deal with context (ie. the only case where pasteboard context naturally occurs)
        // comes from system search
        if (comingIntent != null && comingIntent.getAction() != null && comingIntent.getAction().equals(Intent.ACTION_SEARCH)) {

            // define word!
            String query = comingIntent.getStringExtra(SearchManager.QUERY);
            getDefinition(query);

            lastWord = query;
        }
        // comes from pasteboard/searchbox
        else if (comingIntent != null && comingIntent.hasExtra(SENT_TEXT)) { //  manually sent from places

            String query = comingIntent.getStringExtra(SENT_TEXT).trim();
            getDefinition(query);

            searchView.clearFocus();

            lastWord = query;

            this.tag = getTag(); // for us to remember when sending to adapter
            this.wordIdx = getWordIdx();

        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && comingIntent != null) { // quick reply
            Bundle remoteInput = RemoteInput.getResultsFromIntent(comingIntent);
            if (remoteInput != null) {
                String replyQuery = ((String) remoteInput.getCharSequence(KEY_TEXT_REPLY));
                Log.e("quickreply", "received for " + replyQuery);
                if (replyQuery != null) {
                    getDefinition(replyQuery.toLowerCase());
                    lastWord = replyQuery;
                }

                View view = this.getCurrentFocus();
                if (view != null) {
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                    imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
                }
                // HIDE 2.0
            }
        } else { // no one sent anything, so show the keyboard and allow the user to type
            ((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE)).
                    toggleSoftInput(InputMethodManager.SHOW_FORCED,
                            InputMethodManager.HIDE_IMPLICIT_ONLY);
        }

        ////// Hide keyboard
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    // TODO: deal with repeated code like this for query too
    private String getTag() {
//        Log.e("tag", "getting TAG from intent" + (comingIntent.hasExtra(SENT_TAG)) + " " + comingIntent.getStringExtra(SENT_TAG).trim());
        return (comingIntent.hasExtra(SENT_TAG)) ? comingIntent.getStringExtra(SENT_TAG).trim() : UserVocab.TAG_FOR_NOW;
    }
    private int getWordIdx() { // extra bug proof
        return (comingIntent.hasExtra(SENT_WORD_IDX)) ? comingIntent.getIntExtra(SENT_WORD_IDX, -1) : -1;
    }

    @Override
    public void onNewIntent(Intent intent) {
        Log.e("coming", "onNewIntent");

        if (intent.getAction() != null && intent.getAction().equals(Intent.ACTION_SEARCH)) {
            //hide keyboard

            String query = intent.getStringExtra(SearchManager.QUERY);

            if (true/*!lastWord.equals(query)*/) { // if not defining the same word

                progressBar.setVisibility(View.VISIBLE); // clear the progress bar

                recyclerAdapter.clearAll(); // clear the list
                for (int i = 0; i < 500; i++) { // clear selections
                    selected[i] = false;
                    truthSelect(i, false);
                    selectedCount = 0;
                }

                getDefinition(query);

                // TODO: context here

                lastWord = query;
            }

            // hide keyboard
            View view = this.getCurrentFocus();
            if (view != null) {
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }

        } else if (intent.hasExtra(SENT_TEXT)) {

            String word = intent.getStringExtra(SENT_TEXT).trim();

            Log.e("searchandshow", "wow u sent " + word);

            if (lastWord == null || !lastWord.equals(word)) { // if not defining the same word

                progressBar.setVisibility(View.VISIBLE); // clear the progress bar

                recyclerAdapter.clearAll(); // clear the list
                for (int i = 0; i < selected.length; i++) { // clear selections. 500.
                    selected[i] = false;
                    truthSelect(i, false);
                    selectedCount = 0;
                }

                getDefinition(word);

                this.tag = getTag(); // todo: modularize
                this.wordIdx = getWordIdx();

                final Handler handler1 = new Handler();
                handler1.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        View view = getCurrentFocus();
                        if (view != null) {
                            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                        }
                    }
                }, 100);

                lastWord = word;
            }

            // hide keyboard
            View view = this.getCurrentFocus();
            if (view != null) {
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
            searchView.clearFocus();

        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) { //quick reply
            progressBar.setVisibility(View.VISIBLE); // clear the progress bar
            recyclerAdapter.clearAll(); // clear the list
            for (int i = 0; i < 500; i++) { // clear selections
                selected[i] = false;
                truthSelect(i, false);
                selectedCount = 0;
            }


            Bundle remoteInput = RemoteInput.getResultsFromIntent(intent);
            if (remoteInput != null) {
                String replyQuery = ((String) remoteInput.getCharSequence(KEY_TEXT_REPLY));
                Log.e("quickreply", "onnew received for " + replyQuery);
                getDefinition(replyQuery);

                lastWord = replyQuery;
            }

            View view = this.getCurrentFocus();
            if (view != null) {
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }

        } else { // show keyboard, when no one sent anything
            ((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE)).
                    toggleSoftInput(InputMethodManager.SHOW_FORCED,
                            InputMethodManager.HIDE_IMPLICIT_ONLY);
        }
    }

    // provide visual adjustment to Android views
    private void adjustSearchViews() {
        searchView = (SearchView) findViewById(R.id.toolbar_text);
        SearchManager searchManager = (SearchManager) getSystemService(SEARCH_SERVICE);
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setIconifiedByDefault(false);
        EditText searchText = (EditText) searchView.findViewById(android.support.v7.appcompat.R.id.search_src_text);
        searchText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 21f);

        LinearLayout searchLinear = (LinearLayout) searchView.findViewById(android.support.v7.appcompat.R.id.search_bar);
        int childcount = searchLinear.getChildCount();
        for (int i = 0; i < childcount; i++) {
            View v = searchLinear.getChildAt(i);
            Log.e("viewll", "i" + v.toString());
        }

        LinearLayout frameLinear = (LinearLayout) searchLinear.findViewById(android.support.v7.appcompat.R.id.search_edit_frame);
        frameLinear.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        ViewUtility.setMarginsLinear(0f, 0f, 0f, 0f, frameLinear, getApplicationContext());
        frameLinear.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        int childcount2 = frameLinear.getChildCount();
        Log.e("viewll2", "" + childcount2);
        for (int i = 0; i < childcount2; i++) {
            View v = frameLinear.getChildAt(i);
            Log.e("viewll2", "i" + v.toString());
        }

        LinearLayout plateLinear = (LinearLayout) frameLinear.findViewById(android.support.v7.appcompat.R.id.search_plate);
        int childcount3 = plateLinear.getChildCount();
        Log.e("viewll3", "" + childcount3);
        for (int i = 0; i < childcount3; i++) {
            View v = plateLinear.getChildAt(i);
            Log.e("viewll3", "i" + v.toString());
        }
        AppCompatImageView closeButton = (AppCompatImageView) plateLinear.findViewById(android.support.v7.appcompat.R.id.search_close_btn);
        if (closeButton != null) { // stop inching close button to the left!!

            ViewUtility.setMarginsLinear(0f, 0f, 0f, 0f, closeButton, this);
            closeButton.setPadding(0, 16, 16, 0);
        }
        searchInnerText = (TextView) plateLinear.findViewById(android.support.v7.appcompat.R.id.search_src_text);


        LinearLayout submitLinear = (LinearLayout) frameLinear.findViewById(android.support.v7.appcompat.R.id.submit_area);
        int childcount4 = submitLinear.getChildCount();
        Log.e("viewll4", "" + childcount4);
        for (int i = 0; i < childcount4; i++) {
            View v = submitLinear.getChildAt(i);
            Log.e("viewll4", "i" + v.toString());
        }
        AppCompatImageView voiceButton = (AppCompatImageView) submitLinear.findViewById(android.support.v7.appcompat.R.id.search_voice_btn);
        if (voiceButton != null) { // stop inching record button to the left!!

            ViewUtility.setMarginsLinear(0f, 0f, 0f, 0f, voiceButton, this);
            voiceButton.setPadding(0, 16, 16, 0);
        }
    }

    private void initLayout() {
        // adjustviews()

        // todo: change to include other things like multiple definitions, context, examples, other reminders, gifs
        progressBar = (ProgressBar) findViewById(R.id.progress_bar);
        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setVisibility(View.GONE);
        coordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinator_layout);
        defExRecycler = (RecyclerView) findViewById(R.id.definition_example_recycler);
        makeSpaceView = (View) findViewById(R.id.make_space_view);

        histImage = (ImageView) findViewById(R.id.history_image); // testing view gone // todo: replace with history button
        histImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                View fview = getCurrentFocus();
                if (fview != null) {
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(fview.getWindowToken(), 0);
                }

                Intent sheetIntent = new Intent(getApplicationContext(), SheetHistorySavedActivity.class);
                startActivity(sheetIntent);
                overridePendingTransition(0, 0);
            }
        });

        defExRecycler.setLayoutManager(new LinearLayoutManager(this));
        dividerItemDecoration = new DividerItemDecoration(this);

        // have definitions swoop in
        defExRecycler.setItemAnimator(new FadeInRightAnimator());
        defExRecycler.getItemAnimator().setAddDuration(150);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null) {
            Log.e("tool", "Setting support toolbar...");
            setSupportActionBar(toolbar);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayShowTitleEnabled(false);
                getSupportActionBar().setDisplayHomeAsUpEnabled(false);
            }
            toolbar.setTitle(null);
        }

        frame = (FrameLayout) findViewById(R.id.frame);
        if (frame != null) {
            frame.setBackgroundColor(Color.TRANSPARENT);
            frame.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Log.e("frame", "clicked");
                    touchHandler(TOUCH_FRAME);
                }
            });
        }

        progressBar.setVisibility(View.INVISIBLE);
        progressBar.setVisibility(View.INVISIBLE);
        makeSpaceView.setVisibility(View.GONE);
        frame.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        // todo: remove this, and instead in the future just update the adapter
        recyclerAdapter = new PearsonAdapter(this, new ArrayList<PearsonAnswer.DefinitionExamples>(), this, "Word");
        defExRecycler.setAdapter(recyclerAdapter);

        DisplayMetrics metrics = new DisplayMetrics();
        SearchAndShowActivity.this.getWindowManager().getDefaultDisplay().getMetrics(metrics);

        for (int i = 0; i < 500; i++) {
            selected[i] = false;
        } //redundant

        selectedCount = 0;

    }

    // set frame layout's height, according to device dimensions
    public void setFrameHeight() {
        final WindowManager windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        final FrameLayout fview = frame;
        final ViewTreeObserver vto = fview.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                Display display = windowManager.getDefaultDisplay();
                Point size = new Point(); // for positioning
                display.getSize(size);
                int screenWidth = size.x;
                int screenHeight = size.y;

                int width = fview.getMeasuredWidth();
                int height = fview.getHeight();
                Log.e("viewcf", String.format("(%d, %d) vs (%f, %f)", width, height, screenWidth * .6, screenHeight * .6));
//                int widthdp = Math.round(ViewUtility.convertPixelsToDp(width, getApplicationContext()));
//                int heightdp = Math.round(ViewUtility.convertPixelsToDp(height, getApplicationContext()));

                int newHeight = (int) Math.round(screenHeight * .64);
                frame.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, newHeight));
                Log.e("viewcf", String.format("Settings new height to %d px, or %d dp", newHeight, Math.round(ViewUtility.convertPixelsToDp(newHeight, getApplicationContext()))));

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    fview.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                } else {
                    fview.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                }
            }

        });
    }

    // visually output the disturbance
    public void interruptGetDefinition() {
        progressBar.setVisibility(View.INVISIBLE);
        didUserCancel = true;
    }

    public void getDefinition(String query) {
        Log.e("searchomg", query);

        query = query.replaceAll("\"", ""); // First-aid protection against SQL
        query = query.replace("\\", "/");
        searchView.setQuery(query, false);

        searchView.clearFocus();
        progressBar.setVisibility(View.VISIBLE); // redundant sometimes
        fab.setVisibility(View.GONE);

        didUserCancel = false;

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) { // if the user changes the text after sending this definition request, cancel the request
                Log.e("search", "textchanged");
                interruptGetDefinition();
                return false;
            }
        });

        query = query.replace("\n", " ");
        if (query.length() > 50) {
            query = query.substring(0, 50);
        }

        // define the word, and provide .this (PearsonResponseInterface) as a way to respond when the words have been defined
        pearsonAsyncTask = new PearsonAsyncTask(this, query, this);
        pearsonAsyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

        final SearchAndShowActivity searchAndShowActivity = this;

        View view = searchAndShowActivity.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }

        // hide keyboard, once again
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                View view = searchAndShowActivity.getCurrentFocus();
                if (view != null) {
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                }
            }
        }, 25);

        addToHistory(query); // save into history database -- this doesn't need definition, just the queried word and date

    }

    // add to history table for quick viewing :)
    public void addToHistory(String word) {
        Log.e("sql", "adding " + word + " to database");
        VocabSQLHelper sqlHelper = VocabSQLHelper.getInstance(getApplicationContext());
        sqlHelper.addHistory(new HistoryVocab(word, System.currentTimeMillis()));
    }


    public void saveToUserVocabDatabase(View v) { // FAB action
        if (!endingActivity) { // prevent encouragement of user-indecision
            touchHandler(TOUCH_SEND);
        }
    }

    // animate definition entry into UI
    public void addPearsonList(ArrayList<PearsonAnswer.DefinitionExamples> finalDataSet, boolean showExtraElement) {
        progressBar.setVisibility(View.INVISIBLE);
        if (makeSpaceView != null) {
//            frame.removeView(makeSpaceView);
            makeSpaceView.setVisibility(View.GONE);
        }

        final ArrayList<PearsonAnswer.DefinitionExamples> finalSorted = finalDataSet;

        if (finalSorted.size() > 0) { // add the definitionExamples gradually for the animation
            lastIdx = finalSorted.size() - 1; // communicate to the adapter, which one is the last one?

            recyclerAdapter.add(finalSorted.get(0));

            for (int i = 1; i < finalDataSet.size() /*+ ((showExtraElement)?1:0)*/; i++) { // stagger the additions.. and add a filler if doesn't come from glosbe.
                final int idx = i;

                final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        recyclerAdapter.add(finalSorted.get(idx));
                        Log.e("callback", idx + " " + finalSorted.size());
                        if (idx == finalSorted.size() - 1) {
                            readjustCoordHeight();
//                            defExRecycler.addItemDecoration(dividerItemDecoration); // add the lines
                        }
                    }
                }, 40 * idx);
            }
            // if only one element, still have to readjust
            if (finalDataSet.size() == 1) {
                readjustCoordHeight();
            }
        }
        finishedGetting = true;
    }

    public void addExtraElementContext(int idx) {
        PearsonAnswer.DefinitionExamples de = new PearsonAnswer.DefinitionExamples();
        de.definition = PearsonAdapter.EXTRA_CONTEXT;
        recyclerAdapter.add(de); // filler
    }

    @Override
    protected void onDestroy() {
        Log.e("display", "onDestroy");

        iAlreadyExist = false; // should not be necessary?
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.e("sheet", "options menu called");
        return true;
    }


    // deals with logic relate to user touches in different areas of the screen, including within the frame
    // and outside, in attempt in quit
    // TODO: only TOUCH_SEND is relatively useful here
    public void touchHandler(int source) {
        if (source == TOUCH_SEND) {
//            Log.e("touch", "touched sent " + String.format(Locale.US, "%d, %d", fab.getWidth(), fab.getHeight()));
            Log.e("wordIdx", "  " + String.valueOf(wordIdx));

            recyclerAdapter.animateSlidesAndInsertUserVocab(this.tag, this.wordIdx);

            endingActivity = true; // disable clicks
            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    finish();
                }
            }, REMOVE_DURATION + 50);


        } else if (source == TOUCH_OUTSIDE) {
            Log.e("touch", "2 touching outside");
            finish();
        } else if (source == TOUCH_FRAME && finishedGetting) {
            Log.e("touch", "2 frame");
            finish();
        }

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (MotionEvent.ACTION_OUTSIDE == event.getAction()) {
            Log.e("touch", "touching outside");
            touchHandler(TOUCH_OUTSIDE);
            return true;
        }

        return super.onTouchEvent(event); // Delegate everything else to Activity
    }

    @Override
    public void onPause() {
        super.onPause();

        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }


    @Override
    public void onResume() {
        super.onResume();

        try {
            if (searchInnerText != null &&
                    (searchInnerText.getText().toString().equals("") || searchInnerText.getText().toString().trim().equals(""))) {
                searchInnerText.requestFocus();

                // hide keyboard again
                ((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE)).
                        toggleSoftInput(InputMethodManager.SHOW_FORCED,
                                InputMethodManager.HIDE_IMPLICIT_ONLY);

                final InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                searchInnerText.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        searchInnerText.requestFocus();
                        imm.showSoftInput(searchInnerText, 0);
                    }
                }, 100);

            } else {
                Log.e("searchInnerText", "NULL +" + searchInnerText.getText().toString() + ".");
            }
        } catch (NullPointerException e) {
            Log.e("searchInnerText", "null pointer exception at " + e.toString());
        }


        if (!iAlreadyExist) { // make sure it's not jut a onNewIntent

            iAlreadyExist = true;

            final WindowManager windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
            final CoordinatorLayout fview = coordinatorLayout;
            final ViewTreeObserver vto = fview.getViewTreeObserver();
            vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
//                LayerDrawable ld = (LayerDrawable)tv.getBackground();
//                ld.setLayerInset(1, 0, tv.getHeight() / 2, 0, 0);
//                ViewTreeObserver obs = tv.getViewTreeObserver();

                    ViewUtility.circleRevealExtra(coordinatorLayout, 500);
//                    ViewUtility.zoomIntoView(coordinatorLayout);
                    Log.e("vto", "circle revealing");
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                        fview.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    } else {
                        fview.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                    }
                }

            });
        }

//        DisplayDefinitionPopupActivity.shouldShowPreviousTypeWordPopup = true; // reset to true, in case
    }

    // pass this into adapter to use. TODO: why is this separate from the adapter
    @Override
    public void recyclerViewListClicked(View v, int position) {
        if (!endingActivity) { // if not already animating the finishing of activity
            final View outerView = v;
            final TextView defText = (TextView) v.findViewById(R.id.definition_text);
            final TextView exText = (TextView) v.findViewById(R.id.de_example_text); // doesn't necessarily exist
            final TextView headText = (TextView) v.findViewById(R.id.wordform_header_text);
            final RelativeLayout colorView = (RelativeLayout) v.findViewById(R.id.color_view);
            final TextView posText = (TextView) v.findViewById(R.id.pos_text);

            View view = getCurrentFocus();
            if (view != null) {
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }

            if (!selected[position]) { // that's for adding a context !!!
//            selected[position] = true;
                if (!(recyclerAdapter.sortedPearsonDataSet.get(position).definition.trim().equals(PearsonAnswer.DEFAULT_NO_DEFINITION))) { // make sure it's selectable
                    truthSelect(position, true);
                    selectedCount++;

                    //enable fab
                    fab.requestLayout();
//                fab.setVisibility(View.VISIBLE);
                    Log.e("selected", "" + selectedCount);
                    if (selectedCount == 1) {
//                        ViewUtility.circleReveal(fab);
                        fab.show();
                    }

                    Log.e("click", "click @ " + Integer.toString(position));


                    //animate shading
                    int colorFrom = Color.parseColor(COLOR_NEUTRAL);
                    int colorTo = Color.parseColor(COLOR_PRESSED);
                    ValueAnimator colorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), colorFrom, colorTo);
                    colorAnimation.setDuration(SELECT_DURATION); // milliseconds
                    colorAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                        @Override
                        public void onAnimationUpdate(ValueAnimator animator) {
                            colorView.setBackgroundColor((int) animator.getAnimatedValue());
                        }
                    });
                    colorAnimation.start();

                    //animate font size
                    if (doChangeFont) {
                        ValueAnimator defAnimator = ValueAnimator.ofFloat(BIG_FONT, SMALL_FONT);
                        defAnimator.setDuration(SELECT_DURATION);
                        defAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                            @Override
                            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                                float animatedValue = (float) valueAnimator.getAnimatedValue();
                                defText.setTextSize(animatedValue);
                            }
                        });
                        defAnimator.start();

                        if (exText != null) {
                            ValueAnimator exAnimator = ValueAnimator.ofFloat(BIG_FONT, SMALL_FONT);
                            exAnimator.setDuration(SELECT_DURATION);
                            exAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                                @Override
                                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                                    float animatedValue = (float) valueAnimator.getAnimatedValue();
                                    exText.setTextSize(animatedValue);
                                }
                            });
                            exAnimator.start();
                        }

                        if (headText != null && headText.getVisibility() == View.VISIBLE) {
                            ValueAnimator headAnimator = ValueAnimator.ofFloat(BIG_HEAD_FONT, SMALL_HEAD_FONT);
                            headAnimator.setDuration(SELECT_DURATION);
                            headAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                                @Override
                                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                                    float animatedValue = (float) valueAnimator.getAnimatedValue();
                                    headText.setTextSize(animatedValue);
                                }
                            });
                            headAnimator.start();
                        }

                        if (posText != null && posText.getVisibility() == View.VISIBLE) {
                            ValueAnimator posAnimator = ValueAnimator.ofFloat(BIG_HEAD_FONT, SMALL_HEAD_FONT);
                            posAnimator.setDuration(SELECT_DURATION);
                            posAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                                @Override
                                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                                    float animatedValue = (float) valueAnimator.getAnimatedValue();
                                    posText.setTextSize(animatedValue);
                                }
                            });
                            posAnimator.start();
                        }
                    }
                }

            } else {
                Log.e("select", "converting " + Integer.toString(position) + " back to normal");
//            selected[position] = false;
                truthSelect(position, false);
                if (--selectedCount <= 0) {
                    selectedCount = 0;

                    fab.requestLayout();
//                    fab.setVisibility(View.GONE);
//                    ViewUtility.circleExit(fab);
                    fab.hide();
                }

                int colorFrom = Color.parseColor(COLOR_PRESSED);
                int colorTo = Color.parseColor(COLOR_NEUTRAL);
                ValueAnimator colorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), colorFrom, colorTo);
                colorAnimation.setDuration(SELECT_DURATION); // milliseconds
                colorAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animator) {
                        colorView.setBackgroundColor((int) animator.getAnimatedValue());
                    }
                });
                colorAnimation.start();

                if (doChangeFont) {
                    //animate font size
                    ValueAnimator defAnimator = ValueAnimator.ofFloat(SMALL_FONT, BIG_FONT);
                    defAnimator.setDuration(SELECT_DURATION);
                    defAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                        @Override
                        public void onAnimationUpdate(ValueAnimator valueAnimator) {
                            float animatedValue = (float) valueAnimator.getAnimatedValue();
                            defText.setTextSize(animatedValue);
                        }
                    });
                    defAnimator.start();

                    if (exText != null) {
                        ValueAnimator exAnimator = ValueAnimator.ofFloat(SMALL_FONT, BIG_FONT);
                        exAnimator.setDuration(SELECT_DURATION);
                        exAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                            @Override
                            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                                float animatedValue = (float) valueAnimator.getAnimatedValue();
                                exText.setTextSize(animatedValue);
                            }
                        });
                        exAnimator.start();
                    }

                    if (headText != null && headText.getVisibility() == View.VISIBLE) {
                        ValueAnimator headAnimator = ValueAnimator.ofFloat(SMALL_HEAD_FONT, BIG_HEAD_FONT);
                        headAnimator.setDuration(SELECT_DURATION);
                        headAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                            @Override
                            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                                float animatedValue = (float) valueAnimator.getAnimatedValue();
                                headText.setTextSize(animatedValue);
                            }
                        });
                        headAnimator.start();
                    }

                    if (posText != null && posText.getVisibility() == View.VISIBLE) { // should never be invisible tho...
                        ValueAnimator posAnimator = ValueAnimator.ofFloat(SMALL_HEAD_FONT, BIG_HEAD_FONT);
                        posAnimator.setDuration(SELECT_DURATION);
                        posAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                            @Override
                            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                                float animatedValue = (float) valueAnimator.getAnimatedValue();
                                posText.setTextSize(animatedValue);
                            }
                        });
                        posAnimator.start();
                    }
                }
            }
        }

    }

    @Override
    public void afterGlosbeDefine(PearsonAnswer pearsonAnswer) {
        finishReplyInputNotif(); //
        if (didUserCancel) { // they changed searchview content aka searched for something new
            didUserCancel = false;
            searchView.setOnQueryTextListener(null);
            return;
        }

        View view = getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }

        ArrayList<PearsonAnswer> list = new ArrayList<>();
        list.add(pearsonAnswer);
        addPearsonList(list.get(0).definitionExamplesList, true); // set false when there is no definition
    }

    // Set the pearson definitions through the listviews
    @Override
    public void afterPearsonDefine(PearsonAnswer pearsonAnswer) {
        if (didUserCancel) { // they changed searchview content aka searched for something new
            didUserCancel = false;
            searchView.setOnQueryTextListener(null);
            return;
        }

        // keyboard surpression
        View view = getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }

        if (searchView != null) {
            searchView.clearFocus();
        }

        pA = pearsonAnswer;

        setFrameHeight();

        recyclerAdapter = new PearsonAdapter(this, pearsonAnswer.definitionExamplesList, this, pearsonAnswer.word); //todo: update adapter, not new one
        defExRecycler.setAdapter(recyclerAdapter); // set a new adapter.. but don't do this

        recyclerAdapter.updateMainWord(pearsonAnswer.word);

        final ArrayList<PearsonAnswer.DefinitionExamples> finalDataSet = new ArrayList<>();
        for (int i = 0; i < pearsonAnswer.definitionExamplesList.size(); i++) {
            finalDataSet.add(pearsonAnswer.definitionExamplesList.get(i)); // duplicate the list
        }

        ArrayList<Integer> removeIdx = new ArrayList<>();
        for (int i = 0; i < finalDataSet.size(); i++) {  // remove blanks
            Log.e("removeIdx", "testing " + "(" + finalDataSet.get(i).definition.trim() + ")" + " " + PearsonAnswer.DEFAULT_NO_DEFINITION + i);
            if (finalDataSet.get(i).definition.trim().equals(PearsonAnswer.DEFAULT_NO_DEFINITION)) {
                Log.e("removeIdx", "removed");
                removeIdx.add(i);
            }
        }
        Collections.sort(removeIdx);
        Collections.reverse(removeIdx);
        for (int i = 0; i < removeIdx.size(); i++) {
            Log.e("removeIdx", "removing " + removeIdx.get(i));
            finalDataSet.remove((int) removeIdx.get(i));
        }

        // if the data set has definitions to display
        if (!(finalDataSet.isEmpty())) {
            finishReplyInputNotif(); // android N only

//            final SearchAndShowActivity searchAndShowActivity = this;

            progressBar.setVisibility(View.INVISIBLE);

            Collections.sort(finalDataSet, new PearsonComparator(pearsonAnswer.word.trim()));
            addPearsonList(finalDataSet, true);

        } else { // get the glosbe package if the Pearson dictionary fails to return anything
            glosbeAsyncTask = new GlosbeAsyncTask(this, pearsonAnswer.word, this); // fui
            glosbeAsyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR); // todo: change to normal .execute()?
        }

    }

    public void readjustCoordHeight() {
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {

                int fw = frame.getWidth();
                int fh = frame.getMeasuredHeight();
                int cw = coordinatorLayout.getWidth();
                int ch = coordinatorLayout.getMeasuredHeight();

                fh = Math.round(ViewUtility.convertPixelsToDp((float) fh, getApplicationContext()));

                Log.e("newheight", String.format(Locale.US, " (%d), (%d)", fh, ch));

                if (ch < fh) { // move dialog the the center
                    frame.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                }
            }
        }, 5000);
    }


    // should only happen after onCreate is called, so recyclerview should not be null
    private void truthSelect(int idx, boolean sel) {
        selected[idx] = sel;
        recyclerAdapter.updateSelect(idx, sel);
    }


    // android N exclusive
    public void finishReplyInputNotif() { // todo: make centralized with the notification creation in UserVocabActivity
        NotificationUtility.createConvenienceNotif(getApplicationContext());
    }

}
