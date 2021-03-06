package com.scentric.android.definit.showuservocab.fragment;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

/**
 * For tracking the tabs on the main page of the app
 *
 * Created by Steven on 8/30/2016.
 */

//Extending FragmentStatePagerAdapter
public class Pager extends FragmentStatePagerAdapter {

    //integer to count number of tabs
    int tabCount;

    //Constructor to the class
    public Pager(FragmentManager fm, int tabCount) {
        super(fm);
        //Initializing tab count
        this.tabCount = tabCount;
    }

    //Overriding method getItem
    @Override
    public Fragment getItem(int position) {
        //Returning the current tabs
        switch (position) {
            case 0:
                UserVocabMainFrag tab1 = new UserVocabMainFrag();
                return tab1;
            case 1:
                UserVocabFaveFrag tab3 = new UserVocabFaveFrag();
                return tab3;
            case 2:
                UserVocabHistFrag tab2 = new UserVocabHistFrag();
                return tab2;
            case 3:
                UserVocabProfileFrag tab4 = new UserVocabProfileFrag();
                return tab4;
            default:
                return null;
        }
    }

    //Overriden method getCount to get the number of tabs
    @Override
    public int getCount() {
        return tabCount;
    }
}