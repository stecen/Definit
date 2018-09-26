package com.scentric.android.definit.x;

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
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.scentric.android.definit.R;
import com.scentric.android.definit.get.glosbe.GlosbeAsyncTask;
import com.scentric.android.definit.get.glosbe.GlosbeResponseInterface;
import com.scentric.android.definit.get.pearson.PearsonAsyncTask;
import com.scentric.android.definit.get.pearson.PearsonResponseInterface;
import com.scentric.android.definit.showdefinition.PearsonAdapter;
import com.scentric.android.definit.utility.DividerItemDecoration;
import com.scentric.android.definit.utility.PearsonAnswer;
import com.scentric.android.definit.utility.PearsonComparator;
import com.scentric.android.definit.utility.RecyclerViewClickListener;
import com.scentric.android.definit.utility.ViewUtility;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Locale;

import jp.wasabeef.recyclerview.animators.FadeInRightAnimator;


public class DisplayDefinitionPopupActivity extends AppCompatActivity implements PearsonResponseInterface, GlosbeResponseInterface, RecyclerViewClickListener {
    TextView wordText /*defText, defText2*/, toolbarText;
    //    ListView defExListView;
    FloatingActionButton fab;
    FrameLayout frame;
    ProgressBar progressBar;
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
        makeSpaceView = (View) findViewById(R.id.make_space_view);

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

        String sent = getIntent().getStringExtra(DisplayDefinitionPopupActivity.SENT_WORD);
        toolbarText.setText(sent);


        pearsonAsyncTask = new PearsonAsyncTask(this, sent, this);
        pearsonAsyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR); // todo: change to normal .execute()?
//        definitionAsyncTask = new GlosbeAsyncTask(this, sent, this);
//        definitionAsyncTask.execute();


        DisplayMetrics metrics = new DisplayMetrics();
        DisplayDefinitionPopupActivity.this.getWindowManager().getDefaultDisplay().getMetrics(metrics);

        for (int i = 0; i < 500; i++) {
            selected[i] = false;
        } //redundant

        selectedCount = 0;

//        fab.requestLayout();
        fab.setVisibility(View.INVISIBLE);

////        // Hide keyboard
//        InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
//        imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0); // hide
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
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
                Log.e("viewcf", String.format("(%d, %d) vs (%f, %f)", width, height, screenWidth * .6, screenHeight * .6));
                int widthdp = Math.round(ViewUtility.convertPixelsToDp(width, getApplicationContext()));
                int heightdp = Math.round(ViewUtility.convertPixelsToDp(height, getApplicationContext()));

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

    public void sendToDatabase(View v) { // FAB action
        if (!endingActivity) {
            touchHandler(TOUCH_SEND);
        }
    }


    @Override
    public void onNewIntent(Intent intent) {
        Log.e("onNewIntent", "wat");

        String sent = intent.getStringExtra(DisplayDefinitionPopupActivity.SENT_WORD);
        if (lastWord == null || !lastWord.equals(sent)) { // if defining the same word
            pearsonAsyncTask = new PearsonAsyncTask(this, sent, this);
            pearsonAsyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            lastWord = sent;
        }
        // else, the same word is being defined so don't define anything
    }

    public void addPearsonList(ArrayList<PearsonAnswer.DefinitionExamples> finalDataSet) {
        progressBar.setVisibility(View.INVISIBLE);
        if (makeSpaceView != null) {
            frame.removeView(makeSpaceView);
        }


        final ArrayList<PearsonAnswer.DefinitionExamples> finalSorted = finalDataSet;
        if (finalSorted.size() > 0) { // add the definitionExamples gradually for the animation
            recyclerAdapter.add(finalSorted.get(0));

            for (int i = 1; i < finalDataSet.size(); i++) { // stagger the additions
                final int idx = i;

                final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        recyclerAdapter.add(finalSorted.get(idx));
                        Log.e("callbackfk", idx + " " + finalSorted.size());
                        if (idx == finalSorted.size() - 1) {
//                            Toast.makeText(getApplicationContext(), "wtf", Toast.LENGTH_SHORT).show();
                            readjustCoordHeight();
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

    @Override
    protected void onDestroy() {
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
//            if (TypeWordPopupActivity.typeWordPopupActivity != null) {
//                TypeWordPopupActivity.typeWordPopupActivity.finishMe();
//            }

            endingActivity = true; // disable clicks
//            CallbackAsyncTask callbackAsyncTask = new CallbackAsyncTask(REMOVE_DURATION + 50, this); // wait 300 milliseconds for moving animations to finish
//            callbackAsyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    finish();
                }
            }, REMOVE_DURATION + 50);


        } else if (source == TOUCH_OUTSIDE && finishedGetting == true) {
//            if (TypeWordPopupActivity.typeWordPopupActivity != null) {
//                TypeWordPopupActivity.typeWordPopupActivity.finishMe();
//            }
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
    public void recyclerViewListClicked(View v, int position) {
        if (!endingActivity) { // if not already animating the finishing of activity
            final View outerView = v;
            final TextView defText = (TextView) v.findViewById(R.id.definition_text);
            final TextView exText = (TextView) v.findViewById(R.id.de_example_text); // doesn't necessarily exist
            final RelativeLayout colorView = (RelativeLayout) v.findViewById(R.id.color_view);

            if (!selected[position]) {
                if (!(recyclerAdapter.sortedPearsonDataSet.get(position).definition.trim().equals(PearsonAnswer.DEFAULT_NO_DEFINITION))) { // make sure it's selectable
                    truthSelect(position, true);
                    selectedCount++;

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
    public void afterGlosbeDefine(PearsonAnswer pearsonAnswer) {
        ArrayList<PearsonAnswer> list = new ArrayList<>();
        list.add(pearsonAnswer);
        addPearsonList(list.get(0).definitionExamplesList);
    }

    // Set the pearson definitions through the listviews
    @Override
    public void afterPearsonDefine(PearsonAnswer pearsonAnswer) {
        pA = pearsonAnswer;


        //todo FUCK


//        recyclerAdapter = new PearsonAdapter(this, pearsonAnswer.definitionExamplesList, this, pearsonAnswer.word);
        defExRecycler.setAdapter(recyclerAdapter);

        //region idk
        final ArrayList<PearsonAnswer.DefinitionExamples> finalDataSet = new ArrayList<>();
        for (int i = 0; i < pearsonAnswer.definitionExamplesList.size(); i++) {
            finalDataSet.add(pearsonAnswer.definitionExamplesList.get(i)); // duplicate the list
        }

        ArrayList<Integer> removeIdx = new ArrayList<>();
        for (int i = 0; i < finalDataSet.size(); i++) {  // remove blanks
            Log.e("removeIdx", "testing " + "(" + finalDataSet.get(i).definition.trim() + ")" + " " + PearsonAnswer.DEFAULT_NO_DEFINITION + i);
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
            progressBar.setVisibility(View.INVISIBLE);
            Collections.sort(finalDataSet, new PearsonComparator(pearsonAnswer.word.trim()));

            addPearsonList(finalDataSet);

        } else { // get the glosbe package

            glosbeAsyncTask = new GlosbeAsyncTask(this, pearsonAnswer.word, this); // fui
            glosbeAsyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR); // todo: change to normal .execute()?
        }

    }

    public void readjustCoordHeight() {
        int fw = frame.getWidth();
        int fh = frame.getHeight();
        int cw = coordinatorLayout.getWidth();
        int ch = coordinatorLayout.getHeight();

        fh = Math.round(ViewUtility.convertPixelsToDp((float) fh, this));

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

}
