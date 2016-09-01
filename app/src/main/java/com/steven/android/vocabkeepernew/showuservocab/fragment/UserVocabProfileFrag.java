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
public class UserVocabProfileFrag extends Fragment implements FragmentRefresher, FragmentReselected {
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
        return inflater.inflate(R.layout.fragment_uservocab_profile, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
//        // recycler stuff
//        recyclerView = (RecyclerView) getView().findViewById(R.id.user_vocab_recycler);

    }

    @Override
    public void reselect() {
//        if (linearLayoutManager != null) {
//            linearLayoutManager.scrollToPosition(0);
//        }
    }

    @Override
    public void onResume() {

        super.onResume();
    }

    public void refreshViews() {
        // yooo
    }


}
