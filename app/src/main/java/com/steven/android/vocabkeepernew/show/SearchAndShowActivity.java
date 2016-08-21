package com.steven.android.vocabkeepernew.show;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
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
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
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

import com.google.gson.Gson;
import com.steven.android.vocabkeepernew.R;
import com.steven.android.vocabkeepernew.get.glosbe.GlosbeAsyncTask;
import com.steven.android.vocabkeepernew.get.glosbe.GlosbeResponseInterface;
import com.steven.android.vocabkeepernew.get.pearson.PearsonAsyncTask;
import com.steven.android.vocabkeepernew.get.pearson.PearsonResponseInterface;
import com.steven.android.vocabkeepernew.input.TypeWordPopupActivity;
import com.steven.android.vocabkeepernew.utility.DividerItemDecoration;
import com.steven.android.vocabkeepernew.utility.PearsonAnswer;
import com.steven.android.vocabkeepernew.utility.PearsonComparator;
import com.steven.android.vocabkeepernew.utility.ViewUtility;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Locale;

import jp.wasabeef.recyclerview.animators.FadeInRightAnimator;

/**
 * Created by Steven on 8/20/2016.
 */
public class SearchAndShowActivity extends AppCompatActivity implements PearsonResponseInterface, GlosbeResponseInterface, RecyclerViewClickListener {

    SearchView searchView;
    Intent comingIntent;

    //////

    //    ListView defExListView;
    FloatingActionButton fab;
    FrameLayout frame;
    ProgressBar progressBar;
    ImageView navImage;
    View makeSpaceView;
    CoordinatorLayout coordinatorLayout;
    int coordHeight;

    RecyclerView defExRecycler;
    PearsonAdapter recyclerAdapter;
    DividerItemDecoration dividerItemDecoration;

    CollapsingToolbarLayout collapsingToolbarLayout = null;

    public static boolean shouldShowPreviousTypeWordPopup = true; // self explanatory. but the only time this activity finishes when this is true is when the user presses the system back button

    public static final String SENT_WORD = "sent_word";
    //    public static final String SENT_DEF = "send_def";
    public static final String SENT_PACKAGE_JSON = "send_package_json";
//    public static final String KEY_RECOG_NOW = "recognow";
    public final static int REQ_CODE_SPEECH_INPUT = 92;

    private static final String PEARSON_JSON = "pearson_json";

    public static String lastWord = null;

    PearsonAsyncTask pearsonAsyncTask;
    GlosbeAsyncTask glosbeAsyncTask;

    PearsonAnswer pA = null;

    public static final String COLOR_NEUTRAL = "#FFFFFF", COLOR_PRESSED = "#DFDFDF";
    public static final int REMOVE_DURATION = 350;
    public static final int SELECT_DURATION = 75;

    public static final int TOUCH_OUTSIDE = 1; // for outsideclick
    public static final int TOUCH_SEND = 2;

    protected boolean selected[] = new boolean[500]; // keeps track of which definitionExamples are clicked in the recyclerview to send to database / show color
    protected int selectedCount = 0; // count to show FAB or not
    boolean endingActivity = false; //disable onclicks when the user wants to send
    boolean finishedGetting = false; // to make sure the activity doesn't end when the user clicks the framelayout before the definitions have loaded


    final float SMALL_FONT = 15f;
    final float BIG_FONT = 16f;
    boolean doChangeFont = true; // for me to change

    // dialog activity's maximum height http://stackoverflow.com/questions/6624480/how-to-customize-the-width-and-height-when-show-an-activity-as-a-dialog
    // expanding toolbar https://github.com/chrisbanes/cheesesquare/tree/master/app/src/main/java/com/support/android/designlibdemo

    public int lastIdx = -1;//for adapter to know where to add the bottom filler

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Make us non-modal, so that others can receive touch events.  ...but notify us that it happened.
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL, WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH, WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH); // don't watch outside

        setContentView(R.layout.activitiy_searchandshow);





        //region search
        searchView = (SearchView) findViewById(R.id.toolbar_text);
        SearchManager searchManager = (SearchManager) getSystemService(SEARCH_SERVICE);
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setIconifiedByDefault(false);
        EditText searchText = (EditText) searchView.findViewById(android.support.v7.appcompat.R.id.search_src_text);
//        searchText.setBackgroundColor(Color.parseColor("#ffffff"));
//        ViewUtility.setMarginsLinear(0f, 0f, 0f, 0f, (View) searchText, getApplicationContext());
        searchText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 21f);

//
        LinearLayout searchLinear = (LinearLayout) searchView.findViewById(android.support.v7.appcompat.R.id.search_bar);
        int childcount = searchLinear.getChildCount();
        for (int i=0; i < childcount; i++){
            View v = searchLinear.getChildAt(i);
            Log.e("viewll", "i" + v.toString());
        }
        LinearLayout frameLinear = (LinearLayout) searchLinear.findViewById(android.support.v7.appcompat.R.id.search_edit_frame);
        frameLinear.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        ViewUtility.setMarginsLinear(0f, 0f, 0f, 0f, frameLinear, getApplicationContext());
        frameLinear.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));


        //todo: change to include other things like multiple definitions, context, examples, other reminders, gifs
        progressBar = (ProgressBar) findViewById(R.id.progress_bar);
        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setVisibility(View.GONE);
        coordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinator_layout);
        defExRecycler = (RecyclerView) findViewById(R.id.definition_example_recycler);
        makeSpaceView = (View) findViewById(R.id.make_space_view);
//        navImage = (ImageView) findViewById(R.id.nav_image); // testing view gone

        defExRecycler.setLayoutManager(new LinearLayoutManager(this));
        dividerItemDecoration = new DividerItemDecoration(this);

        //region oldanim
        defExRecycler.setItemAnimator(new FadeInRightAnimator());
//        defExRecycler.getItemAnimator().setRemoveDuration(REMOVE_DURATION);
        //endregion

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
                    touchHandler(TOUCH_OUTSIDE);
                }
            });
        }

        progressBar.setVisibility(View.INVISIBLE);
        final Handler handler = new Handler();
//        handler.postDelayed(new Runnable() { // todo: apply this handler everywhere
//            @Override
//            public void run() {
//                progressBar.setVisibility(View.INVISIBLE);
//                //Do something after 100ms
//            }
//        }, 100);

        ActionBar actionBar = getSupportActionBar();
//        actionBar.setDisplayHomeAsUpEnabled(true);

//        String sent  = getIntent().getStringExtra(DisplayDefinitionPopupActivity.SENT_WORD);


//        getDefinition("brighter"); // todo: make more obvious
        progressBar.setVisibility(View.INVISIBLE);
        makeSpaceView.setVisibility(View.GONE);
        frame.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));


        //todo : remove this, and instead in the future just update the adapter
        recyclerAdapter = new PearsonAdapter(this ,new ArrayList<PearsonAnswer.DefinitionExamples>(), this, "Word");
        defExRecycler.setAdapter(recyclerAdapter);


        DisplayMetrics metrics = new DisplayMetrics();
        SearchAndShowActivity.this.getWindowManager().getDefaultDisplay().getMetrics(metrics);

        for (int i = 0; i < 500; i++) {
            selected[i] = false;
        } //redundant

        selectedCount = 0;






        comingIntent = getIntent();
        Log.e("coming", "" + (comingIntent != null));
        if (comingIntent != null && comingIntent.getAction() != null && comingIntent.getAction().equals(Intent.ACTION_SEARCH)) {
            String query = comingIntent.getStringExtra(SearchManager.QUERY);

            getDefinition(query);

            // hide keyboard
        } else if (comingIntent != null && comingIntent.hasExtra(SENT_WORD)) { //  manually sent from places
            String query = comingIntent.getStringExtra(SENT_WORD).trim();
            getDefinition(query);
        } /*else if (comingIntent != null && comingIntent.hasExtra(KEY_RECOG_NOW)) {
            recognizeSpeech();
            // recognize speech
        }*/
        //endregion search




////        // Hide keyboard
//        InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
//        imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0); // hide
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }


//        setFrameHeight();

    }

    public void onNewIntent(Intent intent) {
        Log.e("coming", "onNewIntent");
        if (intent.getAction() != null && intent.getAction().equals(Intent.ACTION_SEARCH)) {
            //hide keyboard

            String query = intent.getStringExtra(SearchManager.QUERY);

            if (lastWord == null || !lastWord.equals(query)) { // if not defining the same word

                progressBar.setVisibility(View.VISIBLE); // clear the progress bar

                recyclerAdapter.clearAll(); // clear the list
                for (int i = 0; i < 500; i++) { // clear selections
                    selected[i] = false;
                    truthSelect(i, false);
                    selectedCount = 0;
                }


                getDefinition(query);

                lastWord = query;
            }

            //hide keyboard
            View view = this.getCurrentFocus();
            if (view != null) {
                InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }

        } else if (intent.hasExtra(SENT_WORD)) {
            // wow
            String word = intent.getStringExtra(SENT_WORD).trim();

            Log.e("searchandshow", "wow u sent " + word);

            if (lastWord == null || !lastWord.equals(word)) { // if not defining the same word

                progressBar.setVisibility(View.VISIBLE); // clear the progress bar

                recyclerAdapter.clearAll(); // clear the list
                for (int i = 0; i < 500; i++) { // clear selections
                    selected[i] = false;
                    truthSelect(i, false);
                    selectedCount = 0;
                }


                getDefinition(word);

                lastWord = word;
            }

            //hide keyboard
            View view = this.getCurrentFocus();
            if (view != null) {
                InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
        } /*else if (intent.hasExtra(KEY_RECOG_NOW)) {
            recognizeSpeech();
        }*/
    }




    public void setFrameHeight() {

        //set frame layout height
        final WindowManager windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        final FrameLayout fview = frame;
        final ViewTreeObserver vto = fview.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
//                LayerDrawable ld = (LayerDrawable)tv.getBackground();
//                ld.setLayerInset(1, 0, tv.getHeight() / 2, 0, 0);
//                ViewTreeObserver obs = tv.getViewTreeObserver();

                Display display = windowManager.getDefaultDisplay();
                Point size = new Point(); // for positioning
                display.getSize(size);
                int screenWidth = size.x;
                int screenHeight = size.y;

                int width = fview.getMeasuredWidth();
                int height = fview.getHeight();
                Log.e("viewcf", String.format("(%d, %d) vs (%f, %f)", width, height, screenWidth*.6, screenHeight *.6));
                int widthdp = Math.round(ViewUtility.convertPixelsToDp(width, getApplicationContext()));
                int heightdp = Math.round(ViewUtility.convertPixelsToDp(height, getApplicationContext()));

                int newHeight = (int)Math.round(screenHeight * .64);
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

    public void getDefinition(String query) {
        Log.e("searchomg", query);
        searchView.setQuery(query,false);

//        defExRecycler.invalidateItemDecorations(); //todo why is this null when oncreate is already called

//        final Handler handler1 = new Handler();
//        handler1.postDelayed(new Runnable() {
//            @Override
//            public void run() {
                progressBar.setVisibility(View.VISIBLE); // redundant sometimes
                fab.setVisibility(View.GONE);
//            }
//        }, 100);
//        ViewUtility.circleExit(fab);

//        makeSpaceView.setVisibility(View.VISIBLE);

        //clear Adapter and set progress bar

        pearsonAsyncTask = new PearsonAsyncTask(this, query, this);
        pearsonAsyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

        final SearchAndShowActivity searchAndShowActivity = this;

        //hide keyboard again///////////// well thats annoying
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                View view = searchAndShowActivity.getCurrentFocus();
                if (view != null) {
                    InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                }
            }
        }, 50);

    }


    public void sendToDatabase(View v) { // FAB action

        if (!endingActivity) {
            touchHandler(TOUCH_SEND);
        }
    }



    public void addPearsonList(ArrayList<PearsonAnswer.DefinitionExamples> finalDataSet, boolean showExtraElement) {
        progressBar.setVisibility(View.INVISIBLE);
        if (makeSpaceView != null) {
//            frame.removeView(makeSpaceView);
            makeSpaceView.setVisibility(View.GONE);
        }


        final ArrayList<PearsonAnswer.DefinitionExamples> finalSorted = finalDataSet;
        /// guess I have to use recursion...
        if (finalSorted.size() > 0) { // add the definitionExamples gradually for the animation
            lastIdx = finalSorted.size()-1;

            recyclerAdapter.add(finalSorted.get(0));

            for (int i = 1; i < finalDataSet.size() /*+ ((showExtraElement)?1:0)*/; i++) { // stagger the additions.. PLUS ADD ONE FILLER if not come from glosbe.
                final int idx = i;

                final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
//                        if (idx == finalSorted.size()) {
//                            addExtraElementContext(idx);
//                            return;
//                        }


                        recyclerAdapter.add(finalSorted.get(idx));
                        Log.e("callbackfk", idx + " " + finalSorted.size());
                        if (idx == finalSorted.size()-1) {
//                            Toast.makeText(getApplicationContext(), "wtf", Toast.LENGTH_SHORT).show();
                            readjustCoordHeight();
//                            defExRecycler.addItemDecoration(dividerItemDecoration); // add the lines
                        }
                    }
                }, 40 * idx);


            }
            // if only one element, still have to readjust
            if (finalDataSet.size()== 1) {
//                if (showExtraElement) {
//                    //add another
//                    addExtraElementContext(1);
//                } else {
//
////                defExRecycler.addItemDecoration(dividerItemDecoration);
//                }
                readjustCoordHeight();
            }
        }
        finishedGetting = true;
    }

    public void addExtraElementContext(int idx) {
        PearsonAnswer.DefinitionExamples de=  new PearsonAnswer.DefinitionExamples();
        de.definition = PearsonAdapter.EXTRA_CONTEXT;
        recyclerAdapter.add(de); //filler
    }

    @Override
    protected void onDestroy(){
        Log.e("display", "onDestroy");

        // :(( according to this stack overflow guy http://stackoverflow.com/questions/3282204/android-open-dialogue-activity-without-opening-main-activity-behind-it

//        Intent mainIntent = new Intent(this, DisplayDefinitionPopupActivity.class);
//        mainIntent.putExtra(UserVocabActivity.KEY_TASK, UserVocabActivity.IS_BACK);
//        startActivity(mainIntent);
        super.onDestroy();
    }

    public void touchHandler(int source) { //called when touching framelayout/outside. note: is not called when the user sends something, because of the async needed. so if u change this chaneg that:)
//        DisplayDefinitionPopupActivity.shouldShowPreviousTypeWordPopup = false;

        if (source == TOUCH_SEND) {
            Log.e("touch", "touched sent " + String.format(Locale.US, "%d, %d", fab.getWidth(), fab.getHeight()));

            recyclerAdapter.animateSlidesAndInsertUserVocab(); // removetemp lol


//            DisplayDefinitionPopupActivity.shouldShowPreviousTypeWordPopup = false;
            if (TypeWordPopupActivity.typeWordPopupActivity != null) { // todo: delete this
                TypeWordPopupActivity.typeWordPopupActivity.finishMe();
            }

            endingActivity = true; // disable clicks
//            CallbackAsyncTask callbackAsyncTask = new CallbackAsyncTask(REMOVE_DURATION + 50, this); // wait 300 milliseconds for moving animations to finish
//            callbackAsyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    finish();
                }
            }, REMOVE_DURATION+50);


        } else  if (source == TOUCH_OUTSIDE && finishedGetting) {
            if (TypeWordPopupActivity.typeWordPopupActivity != null) {
                TypeWordPopupActivity.typeWordPopupActivity.finishMe();
            }
            Log.e("touch", "touching outside");
            finish();
        }

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (MotionEvent.ACTION_OUTSIDE == event.getAction()) {
            touchHandler(TOUCH_OUTSIDE);
            return true;
        }

        return super.onTouchEvent(event); // Delegate everything else to Activity.
    }


    @Override
    public void onResume() {
        super.onResume();
//        DisplayDefinitionPopupActivity.shouldShowPreviousTypeWordPopup = true; // reset to true, in case
    }

    @Override
    public void recyclerViewListClicked(View v, int position){
//        if (position == recyclerAdapter.contextIdx) { // can't select this!
//            return;
//        }

        if (!endingActivity) { // if not already animating the finishing of activity
            final View outerView = v;
            final TextView defText = (TextView) v.findViewById(R.id.definition_text);
            final TextView exText = (TextView) v.findViewById(R.id.de_example_text); // doesn't necessarily exist
            final RelativeLayout colorView = (RelativeLayout) v.findViewById(R.id.color_view);

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
                        ViewUtility.circleReveal(fab);
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
                    ViewUtility.circleExit(fab);
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
                }
            }
        }

        Log.e("hi", "hi");

    }

    @Override
    public void afterGlosbeDefine(PearsonAnswer pearsonAnswer) {
        ArrayList<PearsonAnswer> list = new ArrayList<>();
        list.add(pearsonAnswer);
        addPearsonList(list.get(0).definitionExamplesList, true); // set false when there is no definition

        final SearchAndShowActivity searchAndShowActivity = this; // hide keyboard -_-
//        final Handler handler = new Handler();
//        handler.postDelayed(new Runnable() {
//            @Override
//            public void run() {
                View view = searchAndShowActivity.getCurrentFocus();
                if (view != null) {
                    InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                }
//            }
//        }, 50);
    }

    // Set the pearson definitions through the listviews
    @Override
    public void afterPearsonDefine(PearsonAnswer pearsonAnswer) {
        pA = pearsonAnswer;

        setFrameHeight();

        recyclerAdapter = new PearsonAdapter(this, pearsonAnswer.definitionExamplesList, this, pearsonAnswer.word); //todo: update adapter, not new one
        defExRecycler.setAdapter(recyclerAdapter);

        //region idk
        final ArrayList<PearsonAnswer.DefinitionExamples> finalDataSet = new ArrayList<>();
        for (int i = 0; i < pearsonAnswer.definitionExamplesList.size(); i++) {
            finalDataSet.add(pearsonAnswer.definitionExamplesList.get(i)); // duplicate the list
        }

        ArrayList<Integer> removeIdx = new ArrayList<>();
        for (int i = 0; i < finalDataSet.size(); i++) {  // remove blanks
            Log.e("removeIdx", "testing " + "(" + finalDataSet.get(i).definition.trim() + ")" + " "+ PearsonAnswer.DEFAULT_NO_DEFINITION + i);
            if (finalDataSet.get(i).definition.trim().equals(PearsonAnswer.DEFAULT_NO_DEFINITION)) {
                Log.e("removeIdx", "yBYEEEEEEEEEEEEEEEEEE");
                removeIdx.add(i);
            }
        }
        Collections.sort(removeIdx);
        Collections.reverse(removeIdx);
        for (int i = 0; i < removeIdx.size(); i++) {
            Log.e("removeIdx", "aaa removing " + removeIdx.get(i));
            finalDataSet.remove((int) removeIdx.get(i));
        }

        // if the data set has definitions to display
        if (!(finalDataSet.isEmpty())) { // do glosbe asynctask
//        AlphaAnimation alphaAnimation = new AlphaAnimation(1.0f, 0.0f); // remove progress bar
//        alphaAnimation.setDuration(120);
//        progressBar.startAnimation(alphaAnimation);
//        (new CallbackAsyncTask(120, new CallbackAsyncInterface() {
//            @Override
//            public void waitCallback() {
//                Log.e("callback", "GRASS MUD HORSE");

            //hide keyboard again///////////// well thats annoying
            final SearchAndShowActivity searchAndShowActivity = this;
//            final Handler handler = new Handler();
//            handler.postDelayed(new Runnable() {
//                @Override
//                public void run() {
                    View view = searchAndShowActivity.getCurrentFocus();
                    if (view != null) {
                        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                    }
//                }
//            }, 50);


            progressBar.setVisibility(View.INVISIBLE);
            //            }
            //        })).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

            Collections.sort(finalDataSet, new PearsonComparator(pearsonAnswer.word.trim()));

            Log.e("lolsorted", (new Gson()).toJson(finalDataSet));
            Log.e("countlmao", "unsorted: " +Integer.toString(pearsonAnswer.definitionExamplesList.size()));
            Log.e("countlmao", "sorted: " +Integer.toString(finalDataSet.size()));

            addPearsonList(finalDataSet, true);

        } else { // get the glosbe package

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

                fh = Math.round(ViewUtility.convertPixelsToDp((float)fh, getApplicationContext()));

                Log.e("newheight", String.format(Locale.US, " (%d), (%d)", fh, ch));

                if (ch < fh) { // move dialog the the center
                    frame.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                }
            }
        }, 5000);


    }



    //should only happen after onCreate is called, so recyclerview should not be null
    private void truthSelect(int idx, boolean sel) {
//        if (!(recyclerAdapter.sortedPearsonDataSet.get(idx).definition.trim().equals(PearsonAnswer.DEFAULT_NO_DEFINITION))) { // make sure it's selectable
        selected[idx] = sel;
        recyclerAdapter.updateSelect(idx, sel);
//        }
    }

}
