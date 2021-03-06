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

//import android.support.v7.widget.PopupMenu;

/**
 *
 * Fragment that displays all user vocabulary
 * Created by Steven on 8/30/2016.
 *
 */
public class UserVocabMainFrag extends Fragment implements RecyclerViewClickListener, FragmentRefresher, FragmentReselected/*, View.OnLongClickListener*/ {
    public RecyclerView recyclerView;
    DividerItemDecoration dividerItemDecoration;
    VocabSQLHelper helper;
    public UserVocabAdapter adapter;
    LinearLayoutManager linearLayoutManager;

    Context appContext;
    Context activityContext;

    public static ArrayList<UserVocab> dataSet = null;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        appContext = getActivity().getApplicationContext();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_uservocab_main, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        // recycler stuff
        recyclerView = (RecyclerView) getView().findViewById(R.id.user_vocab_recycler);
        linearLayoutManager = new LinearLayoutManager(appContext);
        recyclerView.setLayoutManager(linearLayoutManager);

        activityContext = getActivity();

        helper = VocabSQLHelper.getInstance(appContext);
        final RecyclerViewClickListener listener = this;
        helper.getAllUserVocab(new GetAllWordsAsyncInterface() { // send callback to be called after list is filled with SQL data
                                   @Override
                                   public void setWordsData(ArrayList<UserVocab> userVocabList) {
                                       Log.e("userVocab", "" + userVocabList.size());
                                       adapter = new UserVocabAdapter(userVocabList, listener, (activityContext != null) ? activityContext : appContext, false/*, flong*/);
                                       recyclerView.setAdapter(/*new SlideInLeftAnimationAdapter(*/adapter/*)*/);
                                   }
                               },
                100); // first only get 100

        helper.getAllUserVocab(new GetAllWordsAsyncInterface() {
                                   @Override
                                   public void setWordsData(ArrayList<UserVocab> userVocabList) {
                                       Log.e("userVocab", "" + userVocabList.size());
                                       adapter.replaceData(userVocabList);
                                       Log.e("adapter count", "" + adapter.getItemCount());
                                       dataSet = adapter.sortedDataSet;
                                   }
                               },
                VocabSQLHelper.GET_ALL); // then get ALL of them!


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

    // reselected this tab while on this tab
    @Override
    public void reselect() {
        if (recyclerView != null) {
            recyclerView.smoothScrollToPosition(0);
        }
    }

    @Override
    public void onResume() {
        refreshRecycler();
        super.onResume();
    }

    @Override // note: overrides FragmentRefresher
    public void refreshViews() {
        Log.e("refresh", "main");
        refreshRecycler();
    }

    public void refreshRecycler() {
        helper = VocabSQLHelper.getInstance(appContext);
        helper.getAllUserVocab(new GetAllWordsAsyncInterface() {
                                   @Override
                                   public void setWordsData(ArrayList<UserVocab> userVocabList) {
                                       Log.e("userVocab", "" + userVocabList.size());
                                       adapter.replaceData(userVocabList);
                                       Log.e("adapter count", "" + adapter.getItemCount());
                                       dataSet = adapter.sortedDataSet; // probably not needed due to java references
                                   }
                               },
                VocabSQLHelper.GET_ALL);
    }

    public void recyclerViewListClicked(View v, int position) {
        if (UserDetailsActivity.isActive) {
            return;
        }

        String userVocabString = (new Gson()).toJson(adapter.sortedDataSet.get(position));
        Log.e("userVocab", "clicked " + position + ". " + userVocabString);

        Intent detailIntent = new Intent(appContext, UserDetailsActivity.class);
        dataSet = adapter.sortedDataSet;

        UserDetailsActivity.isActive = true;

        detailIntent.putExtra(UserDetailsActivity.KEY_POS, position);

        startActivity(detailIntent);
        if (getActivity() != null) {
            getActivity().overridePendingTransition(0, 0);
        }
    }
}
