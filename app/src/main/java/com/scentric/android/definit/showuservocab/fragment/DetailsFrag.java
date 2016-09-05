package com.scentric.android.definit.showuservocab.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.scentric.android.definit.R;

/**
 * Created by Steven on 9/2/2016.
 */
public class DetailsFrag extends Fragment {
    public static DetailsFrag newInstance() {
        return new DetailsFrag();
    }

    public DetailsFrag() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_details, container, false);
    }
}
