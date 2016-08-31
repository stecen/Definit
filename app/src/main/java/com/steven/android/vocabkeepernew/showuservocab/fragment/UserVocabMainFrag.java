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

import com.google.gson.Gson;
import com.steven.android.vocabkeepernew.R;
import com.steven.android.vocabkeepernew.show.RecyclerViewClickListener;
import com.steven.android.vocabkeepernew.showuservocab.UserDetailsActivity;
import com.steven.android.vocabkeepernew.showuservocab.UserVocabAdapter;
import com.steven.android.vocabkeepernew.showuservocab.sqlite.UserVocab;
import com.steven.android.vocabkeepernew.showuservocab.sqlite.UserVocabHelper;
import com.steven.android.vocabkeepernew.utility.DividerItemDecoration;

import java.util.ArrayList;

/**
 * Created by Steven on 8/30/2016.
 */
public class UserVocabMainFrag extends Fragment implements RecyclerViewClickListener, FragmentRefresher {
    RecyclerView recyclerView;
    DividerItemDecoration dividerItemDecoration;
    UserVocabHelper helper;
    UserVocabAdapter adapter;

    Context appContext;


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
        return inflater.inflate(R.layout.fragment_uservocab_main, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        // recycler stuff
        recyclerView = (RecyclerView) getView().findViewById(R.id.user_vocab_recycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(appContext));

        dividerItemDecoration = new DividerItemDecoration(appContext);

        helper = UserVocabHelper.getInstance(appContext);
        ArrayList<UserVocab> userVocabList = helper.getAllUserVocab();
        Log.e("userVocab", "" + userVocabList.size());
        adapter = new UserVocabAdapter(userVocabList, this, appContext, false);
//        recyclerView.addItemDecoration(dividerItemDecoration);
        recyclerView.setAdapter(/*new SlideInLeftAnimationAdapter(*/adapter/*)*/);
//        Log.e("adapter count",""+ adapter.getItemCount());

    }

    @Override
    public void onResume() {
        refreshRecycler();
        super.onResume();
    }

    public void refreshViews() {
        Log.e("refresh", "main");
        refreshRecycler();
    }

    public void refreshRecycler () {
        // todo variable to keep track if there are changes so this activity doesnt have to keep reloading the entire sqlite
        helper = UserVocabHelper.getInstance(appContext);
        ArrayList<UserVocab> userVocabList = helper.getAllUserVocab();
        Log.e("userVocab", "" + userVocabList.size());
        Log.e("adapter count",""+ adapter.getItemCount());
        adapter.replaceData(userVocabList);
    }

    public void recyclerViewListClicked(View v, int position) {
        String userVocabString = (new Gson()).toJson(adapter.sortedDataSet.get(position));
        Log.e("userVocab", "clicked " + position +". " + userVocabString);

        Intent detailIntent = new Intent(appContext, UserDetailsActivity.class);
        detailIntent.putExtra(UserDetailsActivity.KEY_JSON, userVocabString);
        startActivity(detailIntent);
    }
}
