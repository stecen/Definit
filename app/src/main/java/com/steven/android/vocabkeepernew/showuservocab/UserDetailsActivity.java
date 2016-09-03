package com.steven.android.vocabkeepernew.showuservocab;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.widget.LinearLayoutManager;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.steven.android.vocabkeepernew.R;
import com.steven.android.vocabkeepernew.showuservocab.fragment.UserVocabMainFrag;
import com.steven.android.vocabkeepernew.showuservocab.sqlite.UserVocab;
import com.steven.android.vocabkeepernew.showuservocab.sqlite.UserVocabHelper;
import com.steven.android.vocabkeepernew.utility.RecyclerViewClickListener;
import com.steven.android.vocabkeepernew.utility.SnappyRecyclerView;
import com.steven.android.vocabkeepernew.utility.ViewUtility;

/**
 * Created by Steven on 8/15/2016.
 */
public class UserDetailsActivity extends Activity implements RecyclerViewClickListener {
    LinearLayout linearLayout;
    UserVocabHelper helper;
    CardsAdapter adapter;
//    UserVocabAdapter adapter;
//    RecyclerView recyclerView;
    SnappyRecyclerView recyclerView;
    RelativeLayout relativeLayout;
    LinearLayoutManager manager;

    public static final String KEY_WORD = "keyForWord";
    public static final String KEY_JSON = "keyJson";
    public static final String KEY_POS = "keyPos";

    UserVocab userVocab;

    @Override
    protected void onCreate(Bundle onSavedInstance) {
        super.onCreate(onSavedInstance);

        setContentView(R.layout.activity_userdetails);

        recyclerView = (SnappyRecyclerView) findViewById(R.id.user_details_recycler);
        relativeLayout = (RelativeLayout) findViewById(R.id.details_linear);
        recyclerView.post(new Runnable() {
            @Override
            public void run() {
//                ViewUtility.circleRevealExtraFast(recyclerView);
                ViewUtility.zoomIntoView(recyclerView);
            }
        });

        manager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);

        recyclerView.setLayoutManager(manager);
//        recyclerView.setLayoutParams(new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        helper = UserVocabHelper.getInstance(this);
        final RecyclerViewClickListener listener = this;
        final Context ctx = this;

//        adapter = new UserVocabAdapter(UserVocabMainFrag.dataSet,
//                this, this, false);
        adapter = new CardsAdapter(UserVocabMainFrag.dataSet, this.getApplicationContext());
        recyclerView.setAdapter(/*new SlideInLeftAnimationAdapter(*/adapter/*)*/);
//        SnapHelper snapHelper = new LinearSnapHelper();
//        snapHelper.attachToRecyclerView(recyclerView);
//        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
//        snappyRecyclerView.setLayoutManager(layoutManager);

        relativeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bye();
            }
        });
        recyclerView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bye();
            }
        });

        Intent intent = getIntent();
        if (intent != null) {
            int position = intent.getIntExtra(KEY_POS, 0);
            Log.e("details", "position " + position);
            manager.scrollToPosition(position);
        }
    }

    public void bye() {
        ViewUtility.zoomOut(recyclerView);
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                finish();
            }
        }, 50);
    }

    @Override
    public void onBackPressed() {
        bye();
    }

    @Override
    public void recyclerViewListClicked(View v, int position) {

    }

}
