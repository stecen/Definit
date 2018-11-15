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
import com.scentric.android.definit.sqlite.GetAllWordsAsyncInterface;
import com.scentric.android.definit.utility.UserVocab;
import com.scentric.android.definit.sqlite.VocabSQLHelper;
import com.scentric.android.definit.utility.DividerItemDecoration;
import com.scentric.android.definit.utility.RecyclerViewClickListener;

import java.util.ArrayList;

/**
 * Secondary tab displaying only favorited words
 * Created by Steven on 8/30/2016.
 */
public class UserVocabFaveFrag extends Fragment implements RecyclerViewClickListener, FragmentRefresher, FragmentReselected {
    RecyclerView recyclerView;
    DividerItemDecoration dividerItemDecoration;
    VocabSQLHelper helper;
    UserVocabAdapter adapter;
    LinearLayoutManager linearLayoutManager;

    Context appContext, activityContext;

    public static ArrayList<UserVocab> dataSet = null;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        appContext = getActivity().getApplicationContext();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        // Returning the layout file after inflating
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

        helper = VocabSQLHelper.getInstance(appContext);

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
                VocabSQLHelper.GET_ALL);

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
                VocabSQLHelper.GET_ALL);
//        // todo variable to keep track if there are changes so this activity doesnt have to keep reloading the entire sqlite
    }

    public void recyclerViewListClicked(View v, int position) {

        String userVocabString = (new Gson()).toJson(adapter.sortedDataSet.get(position));
        Log.e("userVocab", "clicked " + position + ". " + userVocabString);

        Intent detailIntent = new Intent(appContext, UserDetailsActivity.class);
        dataSet = adapter.sortedDataSet;

        detailIntent.putExtra(UserDetailsActivity.KEY_POS, position);
        detailIntent.putExtra(UserDetailsActivity.KEY_FAVE, true);

        startActivity(detailIntent);
        if (getActivity() != null) {
            getActivity().overridePendingTransition(0, 0);
        }
    }
}
