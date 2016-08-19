package com.steven.android.vocabkeepernew.show;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
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
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.steven.android.vocabkeepernew.utility.DefinitionPackage;
import com.steven.android.vocabkeepernew.utility.DividerItemDecoration;
import com.steven.android.vocabkeepernew.utility.PearsonAnswer;
import com.steven.android.vocabkeepernew.R;
import com.steven.android.vocabkeepernew.get.AsyncDefineResponseInterface;
import com.steven.android.vocabkeepernew.get.PearsonAsyncTask;
import com.steven.android.vocabkeepernew.get.PearsonResponseInterface;
import com.steven.android.vocabkeepernew.input.TypeWordPopupActivity;
import com.steven.android.vocabkeepernew.utility.PearsonComparator;
import com.steven.android.vocabkeepernew.utility.ViewUtility;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Locale;

import jp.wasabeef.recyclerview.animators.FadeInRightAnimator;

//import jp.wasabeef.recyclerview.animators.FadeInLeftAnimator;

//import jp.wasabeef.recyclerview.animators.ScaleInBottomAnimator;
//import jp.wasabeef.recyclerview.animators.SlideInDownAnimator;
//import jp.wasabeef.recyclerview.animators.SlideInLeftAnimator;
//import jp.wasabeef.recyclerview.animators.SlideInRightAnimator;
//import jp.wasabeef.recyclerview.animators.SlideInUpAnimator;


public class DisplayDefinitionPopupActivity extends AppCompatActivity implements AsyncDefineResponseInterface, PearsonResponseInterface, RecyclerViewClickListener{
    TextView wordText /*defText, defText2*/, toolbarText;
//    ListView defExListView;
    FloatingActionButton fab;
    FrameLayout frame;
    ProgressBar progressBar;
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

    private static final String PEARSON_JSON = "pearson_json";

    public static String lastWord = null;

    PearsonAsyncTask pearsonAsyncTask;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) { // todo: add alert dialog/fragment and make the background opacity stuff
        super.onCreate(savedInstanceState);

        // Make us non-modal, so that others can receive touch events.  ...but notify us that it happened.
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL, WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH, WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH);

        setContentView(R.layout.activity_displaydefinitionpopup);

        wordText = (TextView) findViewById(R.id.word_text);
        toolbarText = (TextView) findViewById(R.id.toolbar_text); //todo: change to include other things like multiple definitions, context, examples, other reminders, gifs
        progressBar = (ProgressBar) findViewById(R.id.progress_bar);
        fab = (FloatingActionButton) findViewById(R.id.fab);
        coordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinator_layout);
        defExRecycler = (RecyclerView) findViewById(R.id.definition_example_recycler);

        defExRecycler.setLayoutManager(new LinearLayoutManager(this));
        dividerItemDecoration = new DividerItemDecoration(this);
        defExRecycler.addItemDecoration(dividerItemDecoration);

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
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                progressBar.setVisibility(View.VISIBLE);
                //Do something after 100ms
            }
        }, 100);

        ActionBar actionBar = getSupportActionBar();
//        actionBar.setDisplayHomeAsUpEnabled(true);

        String sent  = getIntent().getStringExtra(DisplayDefinitionPopupActivity.SENT_WORD);
        toolbarText.setText(sent);


        pearsonAsyncTask = new PearsonAsyncTask(this, sent, this);
        pearsonAsyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
//        definitionAsyncTask = new DefinitionAsyncTask(this, sent, this);
//        definitionAsyncTask.execute(); //todo: add these when the user wants more definitions


        DisplayMetrics metrics = new DisplayMetrics();
        DisplayDefinitionPopupActivity.this.getWindowManager().getDefaultDisplay().getMetrics(metrics);

        for (int i = 0; i < 500; i++) {
            selected[i] = false;
        } //redundant

        selectedCount = 0;

//        fab.requestLayout();
        fab.setVisibility(View.GONE);

////        // Hide keyboard
//        InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
//        imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0); // hide
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }


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

    public static void sendViewToBack(final View child) {
        final ViewGroup parent = (ViewGroup)child.getParent();
        if (null != parent) {
            parent.removeView(child);
            parent.addView(child, -100);
        }
    }

    public void sendToDatabase(View v) { // FAB action

        //region with old and async and all tha work -_-
////        int coordHeight = coordinatorLayout.getLayoutParams().height;
////        int coordWith = coordinatorLayout.getLayoutParams().width;
//        int coordHeight = coordinatorLayout.getHeight();
//        int coordWith = coordinatorLayout.getWidth();
//
//        Log.e("width", "ch: " + coordHeight + " cw: " + coordWith);
//
////        Log.e("heightssend", ""+ defExRecycler.getHeight() /*+ " " + convertPixelsToDp((float)screenHeight, getApplicationContext())*/);
//        recyclerAdapter.removeTemp();
//        defExRecycler.removeItemDecoration(dividerItemDecoration);
//        coordinatorLayout.setLayoutParams(new FrameLayout.LayoutParams(coordWith, coordHeight)); // maintain height
//
//
//
//        CallbackAsyncTask callbackAsyncTask = new CallbackAsyncTask(REMOVE_DURATION + 50, this); // wait 300 milliseconds for moving animations to finish
//        callbackAsyncTask.execute();



////        toolbarText.animate().translationX(100);
//        TranslateAnimation translateAnimation = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, 20.0f,
//                Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, 0.0f);
//        translateAnimation.setDuration(REMOVE_DURATION);
//        toolbarText.startAnimation(translateAnimation);
//
//        AlphaAnimation alphaAnimation = new AlphaAnimation(1.0f, 0.0f);
//        alphaAnimation.setDuration(REMOVE_DURATION);
////        toolbarText.startAnimation(alphaAnimation);
//
//        AnimationSet animationSet = new AnimationSet(true);
//        animationSet.addAnimation(translateAnimation);
////        animationSet.addAnimation(translateAnimation);
//        animationSet.start();

        //endregion

        if (!endingActivity) {
            touchHandler(TOUCH_SEND);
        }
    }


    @Override
    public void onNewIntent(Intent intent) {
        Log.e( "onNewIntent", "wat");
        String sent = intent.getStringExtra(DisplayDefinitionPopupActivity.SENT_WORD);
        if (lastWord == null || !lastWord.equals(sent)) { // if defining the same word
            pearsonAsyncTask = new PearsonAsyncTask(this, sent, this);
            pearsonAsyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            lastWord = sent;
        }
        // else, the same word is being defined so don't define anything
    }


//    // Save
//    @Override
//    public void onSaveInstanceState(Bundle savedInstanceState) {
//        super.onSaveInstanceState(savedInstanceState);
//
//        if (pA != null) {
//            Gson gson = new Gson();
//            savedInstanceState.putString(PEARSON_JSON, gson.toJson(pA));
//        }
//
//    }

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
            if (TypeWordPopupActivity.typeWordPopupActivity != null) {
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


        } else  if (source == TOUCH_OUTSIDE && finishedGetting == true) {
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
        DisplayDefinitionPopupActivity.shouldShowPreviousTypeWordPopup = true; // reset to true, in case
    }

    @Override
    public void recyclerViewListClicked(View v, int position){
//        int fw = frame.getMeasuredWidth();
//        int fh = frame.getMeasuredHeight();
//        int cw = coordinatorLayout.getMeasuredWidth();
//        int ch =coordinatorLayout.getMeasuredHeight();

//        int fw = frame.getWidth();
//        int fh = frame.getHeight();
//        int cw = coordinatorLayout.getWidth();
//        int ch =coordinatorLayout.getHeight();
//
//        Log.e("newheight", String.format(Locale.US, "(%d, %d), (%d, %d)", fw, fh, cw, ch));

        if (!endingActivity) { // if not already animating the finishing of activity
            final View outerView = v;
            final TextView defText = (TextView) v.findViewById(R.id.definition_text);
            final TextView exText = (TextView) v.findViewById(R.id.example_text); // doesn't necessarily exist
            final RelativeLayout colorView = (RelativeLayout) v.findViewById(R.id.color_view);

            if (!selected[position]) {
//            selected[position] = true;
                truthSelect(position, true);
                selectedCount++;

                //enable fab
//                fab.requestLayout();
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

            } else {
                Log.e("select", "converting " + Integer.toString(position) + " back to normal");
//            selected[position] = false;
                truthSelect(position, false);
                if (--selectedCount <= 0) {
                    selectedCount = 0;

//                    fab.requestLayout();
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

    }

    @Override
    public void afterDefine(DefinitionPackage defPackage) {
        wordText.setText(defPackage.word);

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < defPackage.localDef.size(); i++) {
            sb.append(defPackage.localDef.get(i)).append("\n\n");
        }
        for (int i = 0; i < defPackage.onlineDef.size(); i++) {
            sb.append(defPackage.onlineDef.get(i)).append("\n\n");
        }

//        Toast.makeText(this, "Displaying...", Toast.LENGTH_SHORT).show();
//        defText.setText(sb.toString());
//        defText.setText("wat ru doing steeven dont use this");
    }

    // Set the pearson definitions through the listviews
    @Override
    public void afterPearsonDefine(PearsonAnswer pearsonAnswer) {
        pA = pearsonAnswer;
//        ___PearsonArrayAdapter___LIST pearsonArrayAdapter = new ___PearsonArrayAdapter___LIST(this, pearsonAnswer.definitionExamplesList);
//        defExListView.setAdapter(pearsonArrayAdapter);

//        PearsonAnswer.DefinitionExamples filler = new PearsonAnswer.DefinitionExamples();
//        filler.definition = " ";
//        pearsonAnswer.definitionExamplesList.add(filler);
//        pearsonAnswer.definitionExamplesList.get(0).examples.set(0, " ");




//        AlphaAnimation alphaAnimation = new AlphaAnimation(1.0f, 0.0f); // remove progress bar
//        alphaAnimation.setDuration(120);
//        progressBar.startAnimation(alphaAnimation);
//        (new CallbackAsyncTask(120, new CallbackAsyncInterface() {
//            @Override
//            public void waitCallback() {
//                Log.e("callback", "GRASS MUD HORSE");
                progressBar.setVisibility(View.INVISIBLE);
//            }
//        })).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);


        recyclerAdapter = new PearsonAdapter(this, pearsonAnswer.definitionExamplesList, this, pearsonAnswer.word);
        defExRecycler.setAdapter(recyclerAdapter);

        //region idk
        final ArrayList<PearsonAnswer.DefinitionExamples> lolSortedDataSet = new ArrayList<>();
        for (int i = 0; i < pearsonAnswer.definitionExamplesList.size(); i++) {
            lolSortedDataSet.add(pearsonAnswer.definitionExamplesList.get(i)); // duplicate the list
        }

        ArrayList<Integer> removeIdx = new ArrayList<>();
        for (int i = 0; i < lolSortedDataSet.size(); i++) {  // remove blanks
                Log.e("removeIdx", "testing " + lolSortedDataSet.get(i).definition.trim() + " " + Integer.toString(i));
            if (lolSortedDataSet.get(i).wordForm.trim().equals(PearsonAsyncTask.DEFAULT_NO_DEFINITION)) {
                    Log.e("removeIdx", "yBYEEEEEEEEEEEEEEEEEE");
                removeIdx.add(i);
            }
        }
        Collections.sort(removeIdx);
        Collections.reverse(removeIdx);
        for (int i = 0; i < removeIdx.size(); i++) {
            Log.e("removeIdx", "aaa removing " + removeIdx.get(i));
            lolSortedDataSet.remove((int) removeIdx.get(i));
        }

        Collections.sort(lolSortedDataSet, new PearsonComparator(pearsonAnswer.word.trim()));
//        ArrayList<PearsonAnswer.DefinitionExamples> sameHeadWord = new ArrayList<>();
////            boolean sameBool[] = new boolean[100]; // default is false
//        for (int i = 0; i < pearsonAnswer.definitionExamplesList.size(); i++) { // check for same headwords
//            if (pearsonAnswer.definitionExamplesList.get(i).wordForm.trim().toLowerCase().equals(pearsonAnswer.word.toLowerCase())) {
//                sameHeadWord.add(pearsonAnswer.definitionExamplesList.get(i));
////                    sameBool[i] = true;
//            }
//        }
//        for (int i = 0; i < sameHeadWord.size(); i++) { //
//            if (!(sameHeadWord.get(i).examples.get(0).equals(PearsonAsyncTask.DEFAULT_NO_EXAMPLE))) { // put in the sameheadwords with an example first
//                lolSortedDataSet.add(sameHeadWord.get(i));
//            }
//        }
//        for (int i = 0; i < sameHeadWord.size(); i++) { //
//            if (sameHeadWord.get(i).examples.get(0).equals(PearsonAsyncTask.DEFAULT_NO_EXAMPLE)) { // put the rest of sameheadword in
//                lolSortedDataSet.add(sameHeadWord.get(i));
//            }
//        }
//        for (int i = 0; i < pearsonAnswer.definitionExamplesList.size(); i++) {
//            if (!(pearsonAnswer.definitionExamplesList.get(i).wordForm.trim().toLowerCase().equals(pearsonAnswer.word.toLowerCase()))) {
//                lolSortedDataSet.add(pearsonAnswer.definitionExamplesList.get(i));
//            }
//        }
        Log.e("lolsorted", (new Gson()).toJson(lolSortedDataSet));
        Log.e("countlmao", "unsorted: " +Integer.toString(pearsonAnswer.definitionExamplesList.size()));
        Log.e("countlmao", "sorted: " +Integer.toString(lolSortedDataSet.size()));

        final ArrayList<PearsonAnswer.DefinitionExamples> finalSorted = lolSortedDataSet;
        /// guess I have to use recursion...
        if (finalSorted.size() > 0) {
            recyclerAdapter.add(finalSorted.get(0));

//            for (int i = 1; i < lolSortedDataSet.size(); i++) { // stagger the additions
//                final int idx = i;
//                (new CallbackAsyncTask(40, new CallbackAsyncInterface() {
//                    @Override
//                    public void waitCallback() {
//                        recyclerAdapter.add(finalSorted.get(idx));
//                        Log.e("callbackfk", idx + " " + lolSortedDataSet.size());
//                        if (idx == lolSortedDataSet.size()-1) {
////                            Toast.makeText(getApplicationContext(), "wtf", Toast.LENGTH_SHORT).show();
//                            readjustCoordHeight();
//                        }
//                    }
//                })).executeOnExecutor(AsyncTask.SERIAL_EXECUTOR);
//
//            }




            for (int i = 1; i < lolSortedDataSet.size(); i++) { // stagger the additions
                final int idx = i;

                final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        recyclerAdapter.add(finalSorted.get(idx));
                        Log.e("callbackfk", idx + " " + lolSortedDataSet.size());
                        if (idx == lolSortedDataSet.size()-1) {
//                            Toast.makeText(getApplicationContext(), "wtf", Toast.LENGTH_SHORT).show();
                            readjustCoordHeight();
                        }
                    }
                }, 40 * idx);


//                (new CallbackAsyncTask(40, new CallbackAsyncInterface() {
//                    @Override
//                    public void waitCallback() {
//                        recyclerAdapter.add(finalSorted.get(idx));
//                        Log.e("callbackfk", idx + " " + lolSortedDataSet.size());
//                        if (idx == lolSortedDataSet.size()-1) {
////                            Toast.makeText(getApplicationContext(), "wtf", Toast.LENGTH_SHORT).show();
//                            readjustCoordHeight();
//                        }
//                    }
//                })).executeOnExecutor(AsyncTask.SERIAL_EXECUTOR);

            }
            // if only one element, still have to readjust
            if (lolSortedDataSet.size()== 1) {
                readjustCoordHeight();
            }
        }
        finishedGetting = true;

        //endregion idk

        //region heights fail
//        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
//            int coordHeight = coordinatorLayout.getHeight();
//
//            DisplayMetrics metrics = new DisplayMetrics();
//            getWindowManager().getDefaultDisplay().getMetrics(metrics);
//
//            int screenHeight = metrics.heightPixels;
//            int width = metrics.widthPixels;
//
//            Log.e("heightslol", coordHeight + " aa ");
//        } else {
//            Log.e("heightslol", "landscape mode");
//        }

        //endregion

    }

    public void readjustCoordHeight() {
                int fw = frame.getWidth();
        int fh = frame.getHeight();
        int cw = coordinatorLayout.getWidth();
        int ch =coordinatorLayout.getHeight();

        fh = Math.round(ViewUtility.convertPixelsToDp((float)fh, this));

        Log.e("newheight", String.format(Locale.US, wordText + " (%d, %d), (%d, %d)", fw, fh, cw, ch));

        if (ch < fh) { // move dialog the the center
            frame.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        }
    }



    //should only happen after onCreate is called, so recyclerview should not be null
    private void truthSelect(int idx, boolean sel) {
        selected[idx] = sel;
        recyclerAdapter.updateSelect(idx, sel);
    }

    //    https://github.com/codepath/android_guides/wiki/Using-an-ArrayAdapter-with-ListView
    public class ___PearsonArrayAdapter___LIST extends ArrayAdapter<PearsonAnswer.DefinitionExamples> {

        public ___PearsonArrayAdapter___LIST(Context context, ArrayList<PearsonAnswer.DefinitionExamples> definitionExamples) {
            super(context, 0, definitionExamples);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            PearsonAnswer.DefinitionExamples defEx = getItem(position);
            // Check if an existing view is being reused, otherwise inflate the view
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_definition_example, parent, false);
            }
            // Lookup view for data population
            TextView definitionText = (TextView) convertView.findViewById(R.id.definition_text);
            TextView exampleText = (TextView) convertView.findViewById(R.id.example_text);
            // Populate the data into the template view using the data object
            definitionText.setText(defEx.definition);
            exampleText.setText((defEx.examples.isEmpty())
                ? PearsonAsyncTask.DEFAULT_NO_EXAMPLE :
                defEx.examples.get(0));
            return convertView;
        }

    }



}
