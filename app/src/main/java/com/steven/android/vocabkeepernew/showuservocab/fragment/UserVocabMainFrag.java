package com.steven.android.vocabkeepernew.showuservocab.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
//import android.support.v7.widget.PopupMenu;
import android.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.google.gson.Gson;
import com.steven.android.vocabkeepernew.R;
import com.steven.android.vocabkeepernew.utility.RecyclerViewClickListener;
import com.steven.android.vocabkeepernew.showuservocab.UserDetailsActivity;
import com.steven.android.vocabkeepernew.showuservocab.UserVocabActivity;
import com.steven.android.vocabkeepernew.showuservocab.UserVocabAdapter;
import com.steven.android.vocabkeepernew.showuservocab.sqlite.GetAllWordsAsyncInterface;
import com.steven.android.vocabkeepernew.showuservocab.sqlite.UserVocab;
import com.steven.android.vocabkeepernew.showuservocab.sqlite.UserVocabHelper;
import com.steven.android.vocabkeepernew.utility.DividerItemDecoration;

import java.util.ArrayList;

/**
 * Created by Steven on 8/30/2016.
 */
public class UserVocabMainFrag extends Fragment implements RecyclerViewClickListener, FragmentRefresher, FragmentReselected, View.OnLongClickListener {
    RecyclerView recyclerView;
    DividerItemDecoration dividerItemDecoration;
    UserVocabHelper helper;
    UserVocabAdapter adapter;
    LinearLayoutManager linearLayoutManager;

    Context appContext;
    Context activityContext;


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
        linearLayoutManager = new LinearLayoutManager(appContext);
        recyclerView.setLayoutManager(linearLayoutManager);

        activityContext = getActivity();

//        dividerItemDecoration = new DividerItemDecoration(appContext);

//        helper = UserVocabHelper.getInstance(appContext);
//        ArrayList<UserVocab> userVocabList = helper.getAllUserVocab();
//        Log.e("userVocab", "" + userVocabList.size());
//        adapter = new UserVocabAdapter(userVocabList, this, appContext, false);
////        recyclerView.addItemDecoration(dividerItemDecoration);
//        recyclerView.setAdapter(/*new SlideInLeftAnimationAdapter(*/adapter/*)*/);
////        Log.e("adapter count",""+ adapter.getItemCount());
        helper = UserVocabHelper.getInstance(appContext);
        final RecyclerViewClickListener listener = this;
        final View.OnLongClickListener flong = this;
        helper.getAllUserVocab(new GetAllWordsAsyncInterface() {
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
                                       Log.e("adapter count",""+ adapter.getItemCount());
                                   }
                               },
                UserVocabHelper.GET_ALL); // then get ALL of them!


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
    public boolean onLongClick(View view) {
        Log.e("mainRelative", "onLongClick for popup");
        PopupMenu popupMenu = new PopupMenu(appContext, recyclerView); // mainRelative
        popupMenu.getMenuInflater().inflate(R.menu.menu_uservocab_popup, popupMenu.getMenu());

        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                Log.e("popup", "you clicked " + item.getTitle() + " " + item.getItemId());
                switch (item.getItemId()) {
                    case R.id.popup_menu_delete:
                        Log.e("popup", "delete");
                        break;
                    case R.id.popup_menu_favorite:
                        Log.e("popup", "favorite");
                        break;
                    default:
                        break;
                    }
                return false;
            }
        });

        popupMenu.show();

        return false;
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
        super.onResume();
    }

    public void refreshViews() {
        Log.e("refresh", "main");
        refreshRecycler();
    }

    public void refreshRecycler () {
        // todo variable to keep track if there are changes so this activity doesnt have to keep reloading the entire sqlite
//        helper = UserVocabHelper.getInstance(appContext);
//        ArrayList<UserVocab> userVocabList = helper.getAllUserVocab();
//        Log.e("userVocab", "" + userVocabList.size());
//        Log.e("adapter count",""+ adapter.getItemCount());
//        adapter.replaceData(userVocabList);

        helper = UserVocabHelper.getInstance(appContext);
        helper.getAllUserVocab(new GetAllWordsAsyncInterface() {
            @Override
            public void setWordsData(ArrayList<UserVocab> userVocabList) {
                Log.e("userVocab", "" + userVocabList.size());
                adapter.replaceData(userVocabList);
                Log.e("adapter count",""+ adapter.getItemCount());
            }
        },
        UserVocabHelper.GET_ALL);
    }

    public void recyclerViewListClicked(View v, int position) {
        String userVocabString = (new Gson()).toJson(adapter.sortedDataSet.get(position));
        Log.e("userVocab", "clicked " + position +". " + userVocabString);

        Intent detailIntent = new Intent(appContext, UserDetailsActivity.class);
        detailIntent.putExtra(UserDetailsActivity.KEY_JSON, userVocabString);
        startActivity(detailIntent);
    }
}
