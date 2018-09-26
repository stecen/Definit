package com.scentric.android.definit.showuservocab.sheet;

import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.ImageView;

import com.scentric.android.definit.R;
import com.scentric.android.definit.show.SearchAndShowActivity;
import com.scentric.android.definit.showuservocab.sqlite.GetHistoryInterface;
import com.scentric.android.definit.showuservocab.sqlite.HistoryVocab;
import com.scentric.android.definit.showuservocab.sqlite.UserVocabSQLHelper;
import com.scentric.android.definit.utility.RecyclerViewClickListener;
import com.scentric.android.definit.utility.ViewUtility;

import java.util.ArrayList;

/**
 * Created by Steven on 8/22/2016.
 */
public class SheetHistorySavedActivity extends AppCompatActivity implements RecyclerViewClickListener {

    SearchView searchView;
    Intent comingIntent;

    ViewPager viewPager;
    TabLayout tabLayout;

    RecyclerView recyclerView;
    UserVocabSQLHelper helper;
    HistoryAdapter adapter;
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
        }, 150);

        ///todo: set sheet percentage height

//        navImage = (ImageView) findViewById(R.id.nav_image); // testing view gone // todo: replace with history button

        recyclerView = (RecyclerView) findViewById(R.id.inner_recycler);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        linearLayoutManager.setAutoMeasureEnabled(true);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setHasFixedSize(true);
        helper = UserVocabSQLHelper.getInstance(this);

        final RecyclerViewClickListener fclick = this;
        final Context activityCtx = this;
        helper.getHistory50(new GetHistoryInterface() {
                                @Override
                                public void setHistoryData(ArrayList<HistoryVocab> historyVocabList) {
                                    adapter = new HistoryAdapter(historyVocabList, fclick, activityCtx);
                                    recyclerView.setNestedScrollingEnabled(false);
                                    recyclerView.setAdapter(adapter);
                                }
                            },
                UserVocabSQLHelper.GET_ALL);


        nested = (NestedScrollView) findViewById(R.id.bottom_sheet);
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
            nested.setElevation(16f);
        }

    }


    public void recyclerViewListClicked(View v, int position) {
        String query = adapter.sortedDataSet.get(position).word;
        Log.e("sheet", "clicked " + position + ". " + query);

        Intent defineIntent = new Intent(this, SearchAndShowActivity.class);
        defineIntent.putExtra(SearchAndShowActivity.SENT_WORD, query);
        defineIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(defineIntent);
    }

    public void setScrollHeight() {

        //set frame layout height
        final WindowManager windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        final NestedScrollView fview = nested;
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
                Log.e("viewcfsheet", String.format("(%d, %d) vs (%f, %f)", width, height, screenWidth * .75, screenHeight * .75));
                int widthdp = Math.round(ViewUtility.convertPixelsToDp(width, getApplicationContext()));
                int heightdp = Math.round(ViewUtility.convertPixelsToDp(height, getApplicationContext()));

                int newHeight = (int) Math.round(screenHeight * .75);
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
