package com.scentric.android.definit.showuservocab.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.gson.Gson;
import com.scentric.android.definit.R;
import com.scentric.android.definit.showuservocab.UserDetailsActivity;
import com.scentric.android.definit.showuservocab.UserVocabActivity;
import com.scentric.android.definit.showuservocab.UserVocabAdapter;
import com.scentric.android.definit.showuservocab.sqlite.GetAllWordsAsyncInterface;
import com.scentric.android.definit.showuservocab.sqlite.UserVocab;
import com.scentric.android.definit.showuservocab.sqlite.UserVocabHelper;
import com.scentric.android.definit.utility.DividerItemDecoration;
import com.scentric.android.definit.utility.RecyclerViewClickListener;

import java.util.ArrayList;

/**
 * Created by Steven on 8/30/2016.
 */
public class UserVocabFaveFrag extends Fragment implements RecyclerViewClickListener, FragmentRefresher, FragmentReselected {
    RecyclerView recyclerView;
    DividerItemDecoration dividerItemDecoration;
    UserVocabHelper helper;
    UserVocabAdapter adapter;
    LinearLayoutManager linearLayoutManager;

    Context appContext, activityContext;

    public static ArrayList<UserVocab> dataSet = null;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        appContext = getActivity().getApplicationContext();
    }

    //Overriden method onCreateView
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        //Returning the layout file after inflating
        //Change R.layout.tab1 in you classes
        return inflater.inflate(R.layout.fragment_uservocab_fave, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Log.e("favorite", "on activity created");
        // recycler stuff
        recyclerView = (RecyclerView) getView().findViewById(R.id.fave_recycler);
        linearLayoutManager = new LinearLayoutManager(appContext);
        recyclerView.setLayoutManager(linearLayoutManager);

        activityContext = getActivity();

//        dividerItemDecoration = new DividerItemDecoration(appContext);

        helper = UserVocabHelper.getInstance(appContext);
//        ArrayList<UserVocab> userVocabList = helper.getFaveVocabList();
//        Log.e("userVocab", "" + userVocabList.size());
//        adapter = new UserVocabAdapter(userVocabList, this, appContext, true);
////        recyclerView.addItemDecoration(dividerItemDecoration);
//        recyclerView.setAdapter(/*new SlideInLeftAnimationAdapter(*/adapter/*)*/);
////        Log.e("adapter count",""+ adapter.getItemCount());

        final RecyclerViewClickListener listener = this;
        helper.getFaveVocabList(new GetAllWordsAsyncInterface() {
                                    @Override
                                    public void setWordsData(ArrayList<UserVocab> userVocabList) {
                                        Log.e("faveVocab", "" + userVocabList.size());
                                        adapter = new UserVocabAdapter(userVocabList, listener, (activityContext != null) ? activityContext : appContext, true);
                                        recyclerView.setAdapter(adapter);
                                    }
                                },
                100);

        helper.getFaveVocabList(new GetAllWordsAsyncInterface() {
                                    @Override
                                    public void setWordsData(ArrayList<UserVocab> userVocabList) {
                                        Log.e("fave", "" + userVocabList.size());
                                        adapter.replaceData(userVocabList);
                                        Log.e("adapter count", "" + adapter.getItemCount());
                                    }
                                },
                UserVocabHelper.GET_ALL);

        final UserVocabActivity fActivity = (UserVocabActivity) getActivity();
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (fActivity.fab != null) {
                    if (dy > 0)
                        fActivity.fab.hide();
                    else if (dy < 0)
                        fActivity.fab.show();
                }

            }
        });

    }

    @Override
    public void reselect() {
//        if (linearLayoutManager != null) {
//            linearLayoutManager.scrollToPosition(0);
//        }
        if (recyclerView != null) {
            recyclerView.smoothScrollToPosition(0);
        }
    }


    @Override
    public void onResume() {
        refreshRecycler();
        Log.e("favorite", "onresume");
        super.onResume();
    }

    public void refreshViews() {
        Log.e("favorite", "refresh");
        refreshRecycler();
    }

    public void refreshRecycler() {
        helper.getFaveVocabList(new GetAllWordsAsyncInterface() {
                                    @Override
                                    public void setWordsData(ArrayList<UserVocab> userVocabList) {
                                        Log.e("fave", "" + userVocabList.size());
                                        adapter.replaceData(userVocabList);
                                        Log.e("adapter count", "" + adapter.getItemCount());
                                    }
                                },
                UserVocabHelper.GET_ALL);
//        // todo variable to keep track if there are changes so this activity doesnt have to keep reloading the entire sqlite
    }

    public void recyclerViewListClicked(View v, int position) {

        String userVocabString = (new Gson()).toJson(adapter.sortedDataSet.get(position));
        Log.e("userVocab", "clicked " + position + ". " + userVocabString);

        Intent detailIntent = new Intent(appContext, UserDetailsActivity.class);
        dataSet = adapter.sortedDataSet;


//        detailIntent.putExtra(UserDetailsActivity.KEY_JSON, userVocabString);
        detailIntent.putExtra(UserDetailsActivity.KEY_POS, position);
        detailIntent.putExtra(UserDetailsActivity.KEY_FAVE, true);

        startActivity(detailIntent);
        if (getActivity() != null) {
//            getActivity().overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            getActivity().overridePendingTransition(0, 0);
        }
    }
}
