package com.steven.android.vocabkeepernew.showuservocab.sheet;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
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
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.StringBuilderPrinter;
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
import com.steven.android.vocabkeepernew.input.RelaySpeechActivity;
import com.steven.android.vocabkeepernew.input.TypeWordPopupActivity;
import com.steven.android.vocabkeepernew.show.PearsonAdapter;
import com.steven.android.vocabkeepernew.show.RecyclerViewClickListener;
import com.steven.android.vocabkeepernew.show.SearchAndShowActivity;
import com.steven.android.vocabkeepernew.showuservocab.UserDetailsActivity;
import com.steven.android.vocabkeepernew.showuservocab.UserVocabAdapter;
import com.steven.android.vocabkeepernew.showuservocab.sqlite.UserVocab;
import com.steven.android.vocabkeepernew.showuservocab.sqlite.UserVocabHelper;
import com.steven.android.vocabkeepernew.utility.DividerItemDecoration;
import com.steven.android.vocabkeepernew.utility.PearsonAnswer;
import com.steven.android.vocabkeepernew.utility.PearsonComparator;
import com.steven.android.vocabkeepernew.utility.ViewUtility;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Locale;

import jp.wasabeef.recyclerview.animators.FadeInRightAnimator;

/**
 * Created by Steven on 8/22/2016.
 */
public class SheetHistorySavedActivity extends AppCompatActivity implements RecyclerViewClickListener {

    SearchView searchView;
    Intent comingIntent;

    ViewPager viewPager;
    TabLayout tabLayout;

    RecyclerView recyclerView;
    UserVocabHelper helper;
    UserVocabAdapter adapter;
    NestedScrollView nested;

    boolean canFinish = false;//before opening the sheet, don't stop when clicked coord

    //////

    ImageView navImage;
        BottomSheetBehavior bottomSheetBehavior;
    CoordinatorLayout coordinatorLayout;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Make us non-modal, so that others can receive touch events.  ...but notify us that it happened.
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL, WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH, WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH); // don't watch outside

        setContentView(R.layout.activity_sheet_historysaved);


        //todo: change to include other things like multiple definitions, context, examples, other reminders, gifs

        coordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinator_layout);
//        coordinatorLayout.setElevation(10f);
        coordinatorLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.e("history", "coord clicked");
                if (canFinish) {
                    finish();
                }

            }
        });

        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int swidth = size.x;
        int sheight = size.y; // screen width and height to set height of bottomsheet

        Log.e("sheet", String.format("(%d, %d)", swidth, sheight));

        View bottomSheetView = findViewById(R.id.bottom_sheet);
        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheetView);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        bottomSheetBehavior.setPeekHeight(0);

        bottomSheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(View bottomSheet, int newState) {
                Log.e("bottomsheet", "in state of " + newState);
                if (newState == BottomSheetBehavior.STATE_COLLAPSED) {
//                    bottomSheetBehavior.setPeekHeight(100);
                    Log.e("sheet", "finishing due to state update");
                    finish();
                }
            }

            @Override
            public void onSlide(View bottomSheet, float slideOffset) {
            }
        });


        final BottomSheetBehavior finalSheet = bottomSheetBehavior;
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                finalSheet.setState(BottomSheetBehavior.STATE_EXPANDED);
//                finalSheet.setPeekHeight(200);
                canFinish = true;
            }
        },150);

        ///todo: set sheet percentage height

        navImage = (ImageView) findViewById(R.id.nav_image); // testing view gone // todo: replace with history button

        recyclerView = (RecyclerView) findViewById(R.id.inner_recycler);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        linearLayoutManager.setAutoMeasureEnabled(true);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setHasFixedSize(true);
        helper = UserVocabHelper.getInstance(this);

        ArrayList<UserVocab> userVocabList = helper.getAllUserVocab();
        adapter = new UserVocabAdapter(userVocabList, this, getApplicationContext());
        recyclerView.setNestedScrollingEnabled(false);
        recyclerView.setAdapter(adapter);

        nested = (NestedScrollView) findViewById(R.id.bottom_sheet);
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
            nested.setElevation(16f);
        }


//        setScrollHeight();





//        viewPager = (ViewPager) findViewById(R.id.view_pager);
//        viewPager.setAdapter(new SheetPagerAdapter(getSupportFragmentManager(), this));
//        tabLayout = (TabLayout) findViewById(R.id.sliding_tabs);
//        tabLayout.setupWithViewPager(viewPager);






        //scroll test
//        TextView text = (TextView) findViewById(R.id.sheet_text);
//        StringBuilder sb = new StringBuilder();
//        for (int i = 0; i < 50; i++) {
//            sb.append("https://developer.android.com/reference/android/support/design/widget/BottomSheetBehavior.html");
//        }
//        text.setText(sb.toString());



    }

    public void recyclerViewListClicked(View v, int position) {
        String userVocabString = (new Gson()).toJson(adapter.sortedDataSet.get(position));
        Log.e("userVocab", "clicked " + position +". " + userVocabString);

        Intent detailIntent = new Intent(this, UserDetailsActivity.class);
        detailIntent.putExtra(UserDetailsActivity.KEY_JSON, userVocabString);
        startActivity(detailIntent);
    }

    public void setScrollHeight () {

        //set frame layout height
        final WindowManager windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        final NestedScrollView fview = nested;
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
                Log.e("viewcfsheet", String.format("(%d, %d) vs (%f, %f)", width, height, screenWidth*.75, screenHeight *.75));
                int widthdp = Math.round(ViewUtility.convertPixelsToDp(width, getApplicationContext()));
                int heightdp = Math.round(ViewUtility.convertPixelsToDp(height, getApplicationContext()));

                int newHeight = (int)Math.round(screenHeight * .75);
                fview.setLayoutParams(new CoordinatorLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, newHeight));
                Log.e("viewcfsheet", String.format("Settings new height to %d px, or %d dp", newHeight, Math.round(ViewUtility.convertPixelsToDp(newHeight, getApplicationContext()))));

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    fview.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                } else {
                    fview.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                }
            }

        });
    }






}
