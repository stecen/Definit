package com.steven.android.vocabkeepernew.showuservocab.fragment;

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

import com.steven.android.vocabkeepernew.R;
import com.steven.android.vocabkeepernew.showuservocab.sheet.HistoryAdapter;
import com.steven.android.vocabkeepernew.showuservocab.sqlite.GetHistoryInterface;
import com.steven.android.vocabkeepernew.utility.RecyclerViewClickListener;
import com.steven.android.vocabkeepernew.show.SearchAndShowActivity;
import com.steven.android.vocabkeepernew.showuservocab.UserVocabActivity;
import com.steven.android.vocabkeepernew.showuservocab.sqlite.HistoryVocab;
import com.steven.android.vocabkeepernew.showuservocab.sqlite.UserVocabHelper;

import java.util.ArrayList;

/**
 * Created by Steven on 8/30/2016.
 */
public class UserVocabHistFrag extends Fragment implements RecyclerViewClickListener, FragmentRefresher, FragmentReselected {
    RecyclerView recyclerView;
    Context appContext, activityContext;
    UserVocabHelper helper;
    HistoryAdapter adapter;
    LinearLayoutManager linearLayoutManager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        appContext = getActivity().getApplicationContext();
    }

    //Overriden method onCreateView
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        //Returning the layout file after inflating
        //Change R.layout.tab1 in you classes
        return inflater.inflate(R.layout.fragment_uservocab_history, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        recyclerView = (RecyclerView) getView().findViewById(R.id.inner_recycler);
        linearLayoutManager = new LinearLayoutManager(appContext, LinearLayoutManager.VERTICAL, false);
        linearLayoutManager.setAutoMeasureEnabled(true);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setHasFixedSize(true);
        activityContext = getActivity();
        helper = UserVocabHelper.getInstance(appContext);

//        ArrayList<HistoryVocab> historyVocabs = helper.getHistory50();
//        adapter = new HistoryAdapter(historyVocabs, this, appContext);
////        recyclerView.setNestedScrollingEnabled(false);
////        recyclerView.addItemDecoration(new DividerItemDecoration(appContext));
//        recyclerView.setAdapter(adapter);

        final RecyclerViewClickListener fClick = this;
        helper.getHistory50(new GetHistoryInterface() {
            @Override
            public void setHistoryData (ArrayList<HistoryVocab> historyVocabList) {
                adapter = new HistoryAdapter(historyVocabList, fClick, (activityContext != null) ? activityContext : appContext);
                recyclerView.setAdapter(adapter);
            }
        }, UserVocabHelper.GET_ALL);



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
////        if (linearLayoutManager != null) {
////            linearLayoutManager.scrollToPosition(0);
////        }
//        View firstView = recyclerView.getChildAt(0);
//        int toY = firstView.getTop();
//        int firstPosition = recyclerView.getChildAdapterPosition(firstView);
//        int toScrollTo = 0;
//        View thisView = recyclerView.getChildAt(toScrollTo - firstPosition);
//        int fromY = thisView.getTop();
//
//        recyclerView.smoothScrollBy(0, fromY - toY);
        if (recyclerView != null) {
            recyclerView.smoothScrollToPosition(0);
        }
    }

    @Override
    public void onResume() {
        refreshRecycler(); // refresh the data

        super.onResume();
    }

    public void refreshViews() {
        Log.e("refresh", "history");
        refreshRecycler();
    }

    public void refreshRecycler () {
        // todo variable to keep track if there are changes so this activity doesnt have to keep reloading the entire sqlite
        helper = UserVocabHelper.getInstance(appContext);
//        ArrayList<HistoryVocab> historyVocabs = helper.getHistory50();
//        Log.e("historyVocab", "" + historyVocabs.size());
//        Log.e("hist adapter count",""+ adapter.getItemCount());
//        adapter.replaceData(historyVocabs);
        helper.getHistory50(new GetHistoryInterface() {
            @Override
            public void setHistoryData(ArrayList<HistoryVocab> historyVocabList) {
                Log.e("historyVocab", "" + historyVocabList.size());
                Log.e("hist adapter count",""+ adapter.getItemCount());
                adapter.replaceData(historyVocabList);
            }
        }, UserVocabHelper.GET_ALL);
    }


    public void recyclerViewListClicked(View v, int position) {
        String query = adapter.sortedDataSet.get(position).word;
        Log.e("sheet", "clicked " + position +". " + query);

        Intent defineIntent = new Intent(appContext, SearchAndShowActivity.class);
        defineIntent.putExtra(SearchAndShowActivity.SENT_WORD, query);
        defineIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(defineIntent);
    }
}
