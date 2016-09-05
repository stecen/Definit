package com.scentric.android.definit.showuservocab.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
//import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.gson.Gson;
import com.scentric.android.definit.R;
import com.scentric.android.definit.utility.RecyclerViewClickListener;
import com.scentric.android.definit.showuservocab.UserDetailsActivity;
import com.scentric.android.definit.showuservocab.UserVocabActivity;
import com.scentric.android.definit.showuservocab.UserVocabAdapter;
import com.scentric.android.definit.showuservocab.sqlite.GetAllWordsAsyncInterface;
import com.scentric.android.definit.showuservocab.sqlite.UserVocab;
import com.scentric.android.definit.showuservocab.sqlite.UserVocabHelper;
import com.scentric.android.definit.utility.DividerItemDecoration;

import java.util.ArrayList;

/**
 * Created by Steven on 8/30/2016.
 */
public class UserVocabMainFrag extends Fragment implements RecyclerViewClickListener, FragmentRefresher, FragmentReselected/*, View.OnLongClickListener*/ {
    public RecyclerView recyclerView;
    DividerItemDecoration dividerItemDecoration;
    UserVocabHelper helper;
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

//        getActivity().getSupportFragmentManager().beginTransaction().add(R.id.user_vocab_recycler, DetailsFrag.newInstance(), "details").commit();




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
//        final View.OnLongClickListener flong = this;
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
                                       dataSet = adapter.sortedDataSet;
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

    //region onlonclick
//    @Override
//    public boolean onLongClick(View view) {
//        Log.e("mainRelative", "onLongClick for popup");
//        PopupMenu popupMenu = new PopupMenu(appContext, recyclerView); // mainRelative
//        popupMenu.getMenuInflater().inflate(R.menu.menu_uservocab_popup, popupMenu.getMenu());
//
//        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
//            @Override
//            public boolean onMenuItemClick(MenuItem item) {
//                Log.e("popup", "you clicked " + item.getTitle() + " " + item.getItemId());
//                switch (item.getItemId()) {
//                    case R.id.popup_menu_delete:
//                        Log.e("popup", "delete");
//                        break;
//                    case R.id.popup_menu_favorite:
//                        Log.e("popup", "favorite");
//                        break;
//                    default:
//                        break;
//                    }
//                return false;
//            }
//        });
//
//        popupMenu.show();
//
//        return false;
//    }
    // endregion

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
                dataSet = adapter.sortedDataSet; // probably not needed due to java references
            }
        },
        UserVocabHelper.GET_ALL);
    }

    public void recyclerViewListClicked(View v, int position) {
        if (UserDetailsActivity.isActive) {
            return;
        }

        String userVocabString = (new Gson()).toJson(adapter.sortedDataSet.get(position));
        Log.e("userVocab", "clicked " + position +". " + userVocabString);

        Intent detailIntent = new Intent(appContext, UserDetailsActivity.class);
        dataSet = adapter.sortedDataSet; //lol SOH CHEAP

        UserDetailsActivity.isActive = true;


//        detailIntent.putExtra(UserDetailsActivity.KEY_JSON, userVocabString);
        detailIntent.putExtra(UserDetailsActivity.KEY_POS, position);

        startActivity(detailIntent);
        if (getActivity() != null) {
//            getActivity().overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            getActivity().overridePendingTransition(0,0);
        }
    }
}
