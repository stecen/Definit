package com.steven.android.vocabkeepernew.showuservocab.sheet;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.steven.android.vocabkeepernew.showuservocab.sqlite.HistoryVocab;
import com.steven.android.vocabkeepernew.utility.DateUtility;
import com.steven.android.vocabkeepernew.utility.PearsonAnswer;
import com.steven.android.vocabkeepernew.R;
import com.steven.android.vocabkeepernew.show.RecyclerViewClickListener;
import com.steven.android.vocabkeepernew.showuservocab.sqlite.UserVocab;

import java.util.ArrayList;

/**
 * Created by Steven on 8/13/2016.
 */
public class SheetHistoryAdapter extends RecyclerView.Adapter<SheetHistoryAdapter.ViewHolder> {
    public ArrayList<HistoryVocab> sortedDataSet;
    private RecyclerViewClickListener itemListener;
    private boolean  mySelected[] = new boolean[500]; //todo: increase
    public boolean surpressGray; // when finishing activiting only.
    private Context context;

    // Provide a suitable constructor (depends on the kind of dataset)
    public SheetHistoryAdapter(ArrayList<HistoryVocab> myDataset, RecyclerViewClickListener listener, Context context) {
        sortedDataSet = myDataset;
        itemListener = listener;
        this.context = context;

        Log.e("constructor", sortedDataSet.size() + "  vs " + myDataset.size());

    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView wordText, dateText;
        TextView exampleText;
        View fillerView;
        RelativeLayout colorView;

        public ViewHolder(View v) {
            super(v);
            wordText = (TextView) v.findViewById(R.id.sheet_word_text);
            dateText = (TextView) v.findViewById(R.id.sheet_date_text);

            v.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            itemListener.recyclerViewListClicked(v, this.getLayoutPosition());

        }
    }


//    public void add(UserVocab def) {
//        sortedDataSet.add(def);
//        notifyItemInserted(sortedDataSet.size()-1);
//    }

    @Override
    public SheetHistoryAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                          int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_sheet_history, parent, false);
        // set the view's size, margins, paddings and layout parameters
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final int pos = position;

        Log.e("adapter","called for " + pos);

        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        final HistoryVocab historyVocab = sortedDataSet.get(position);

        holder.wordText.setText(historyVocab.word);

        String date = DateUtility.getHumanifiedDate(historyVocab.date, "MM/dd");
        String prevDate = " ";

        if (position != 0) {
            prevDate = (DateUtility.getHumanifiedDate(sortedDataSet.get(position-1).date, "MM/dd"));
        }


//        Log.e("datecmp", date + " vs " + prevDate + "...    " + date.equals(prevDate));
        if (position != 0 && !(date.equals(prevDate))) {
            holder.dateText.setText(date);
        } else if (position != 0) {
            holder.dateText.setText(" "); // if is the same date as yesterday
        } else {
            holder.dateText.setText(date);
        }

    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return sortedDataSet.size();
    }
}