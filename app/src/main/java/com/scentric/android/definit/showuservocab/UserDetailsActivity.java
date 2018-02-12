package com.scentric.android.definit.showuservocab;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.scentric.android.definit.R;
import com.scentric.android.definit.showuservocab.fragment.UserVocabFaveFrag;
import com.scentric.android.definit.showuservocab.fragment.UserVocabMainFrag;
import com.scentric.android.definit.showuservocab.sqlite.UserVocab;
import com.scentric.android.definit.showuservocab.sqlite.UserVocabHelper;
import com.scentric.android.definit.utility.RecyclerViewClickListener;
import com.scentric.android.definit.utility.SnappyRecyclerView;
import com.scentric.android.definit.utility.ViewUtility;

/**
 * Created by Steven on 8/15/2016.
 */
public class UserDetailsActivity extends AppCompatActivity implements RecyclerViewClickListener {
    LinearLayout linearLayout;
    UserVocabHelper helper;
    CardsAdapter adapter;
    SnappyRecyclerView recyclerView;
    RelativeLayout relativeLayout;
    LinearLayoutManager manager;


    public static boolean isActive = false;

    public static final String KEY_WORD = "keyForWord";
    public static final String KEY_JSON = "keyJson";
    public static final String KEY_POS = "keyPos";
    public static final String KEY_FAVE = "keyFave";

    boolean isFave; // is favorite ; display only favorites and use the favefrag static data

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

        Intent intent = getIntent();
        if (intent != null) {
            final int position = intent.getIntExtra(KEY_POS, 0);
            Log.e("details", "position " + position);
            manager.scrollToPosition(position);

            isFave = intent.getBooleanExtra(KEY_FAVE, false);
            Log.e("details", "fave? " + isFave);


            // todo: show the user that you can scroll
        }


        helper = UserVocabHelper.getInstance(this);
        final RecyclerViewClickListener listener = this;
        final Context ctx = this;

        if (!isFave) {
            adapter = new CardsAdapter(UserVocabMainFrag.dataSet, this.getApplicationContext(), this);
        } else {
            adapter = new CardsAdapter(UserVocabFaveFrag.dataSet, this.getApplicationContext(), this, true);
        }
        recyclerView.setAdapter(adapter);

        relativeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bye();
            }
        });
    }

    public void bye() {
        ViewUtility.zoomOut(recyclerView);
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                finish();
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            }
        }, 50);
    }

    @Override
    public void onBackPressed() {
        bye();
    }

    @Override
    public void onPause() {
        isActive = false;
        super.onPause();
    }

    @Override
    public void recyclerViewListClicked(View v, int position) {

    }

}
