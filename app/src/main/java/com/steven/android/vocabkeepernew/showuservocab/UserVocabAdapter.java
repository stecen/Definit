package com.steven.android.vocabkeepernew.showuservocab;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.steven.android.vocabkeepernew.utility.PearsonAnswer;
import com.steven.android.vocabkeepernew.R;
import com.steven.android.vocabkeepernew.show.RecyclerViewClickListener;
import com.steven.android.vocabkeepernew.showuservocab.sqlite.UserVocab;

import java.util.ArrayList;

/**
 * Created by Steven on 8/13/2016.
 */
public class UserVocabAdapter extends RecyclerView.Adapter<UserVocabAdapter.ViewHolder> {
    public ArrayList<UserVocab> sortedDataSet;
    private RecyclerViewClickListener itemListener;
    private boolean  mySelected[] = new boolean[500]; //todo: increase
    public boolean surpressGray; // when finishing activiting only.
    private Context context;

    // Provide a suitable constructor (depends on the kind of dataset)
    public UserVocabAdapter(ArrayList<UserVocab> myDataset, RecyclerViewClickListener listener, Context context) {
        sortedDataSet = myDataset;
        itemListener = listener;
        this.context = context;

        Log.e("constructor", sortedDataSet.size() + "  vs " + myDataset.size());

    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView wordText, def1Text/*, def2Text, def3Text*/;
        TextView exampleText;
        View fillerView;
        RelativeLayout colorView;
//            RelativeLayout relativeLayout;

        public ViewHolder(View v) {
            super(v);
            wordText = (TextView) v.findViewById(R.id.word_text);
            def1Text = (TextView) v.findViewById(R.id.def1_text);
//            def2Text = (TextView) v.findViewById(R.id.def2_text);
//            def3Text = (TextView) v.findViewById(R.id.def3_text);
//            exampleText = (TextView) v.findViewById(R.id.example_text);

//            colorView = (RelativeLayout) v.findViewById(R.id.color_view);
//            fillerView = v.findViewById(R.id.filler);
//                relativeLayout = (RelativeLayout) v;
            v.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            itemListener.recyclerViewListClicked(v, this.getLayoutPosition());

        }
    }

    public void updateSelect(int idx, boolean sel) {
        mySelected[idx] = sel;
    }

    public void replaceData(ArrayList<UserVocab> userVocabArrayList) {
        sortedDataSet = userVocabArrayList;
    }


    public void remove(PearsonAnswer.DefinitionExamples item) {
        int position = sortedDataSet.indexOf(item);
        sortedDataSet.remove(position);
        notifyItemRemoved(position);
    }

    public void removeByIdx(int idx) {
        Log.e("idxr", "removingByIdx " + idx);
        sortedDataSet.remove(idx);
        notifyItemRemoved(idx);
    }

    public void add(UserVocab def) {
        sortedDataSet.add(def);
        notifyItemInserted(sortedDataSet.size()-1);
    }

    public void removeTemp() {
        int removedSoFar = 0;
        for (int i = 0; i < 500; i++) {
            if (mySelected[i]) {
                Log.e("selected", i+"");
//                    remove(sortedDataSet.get(i));
                removeByIdx(i - (removedSoFar++));
            }
            mySelected[i] = false; //surpress trning gray
        }
    }

    public void animateSlide() { ///todo: animate one by one
        for (int i = 0; i < 500; i++) {
            if (mySelected[i]) {
                Log.e("sliding", i+"");
//                    remove(sortedDataSet.get(i));
                slideByIdx(i);
            }
            mySelected[i] = false; //surpress trning gray
        }
    }

    public void slideByIdx(int idx) {
//        TranslateAnimation translateAnimation = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, 20.0f,
//                Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, 0.0f);
//        translateAnimation.setDuration(REMOVE_DURATION + 50);
//
//        View rootView = (defExRecycler.getLayoutManager().findViewByPosition(idx));
//        if (rootView != null) {
//            TextView defText = (TextView) rootView.findViewById(R.id.definition_text);
//            defText.startAnimation(translateAnimation);
//            defText.setVisibility(View.INVISIBLE);
//
//            TextView exText = (TextView) rootView.findViewById(R.id.example_text);
//            if (exText != null) {
//                exText.startAnimation(translateAnimation);
//                exText.setVisibility(View.INVISIBLE);
//            }
//        }
    }

    // Create new views (invoked by the layout manager)
    @Override
    public UserVocabAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                        int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_uservocab, parent, false);
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
        final UserVocab userVocab = sortedDataSet.get(position);

        Log.e("adapterGson", (new Gson()).toJson(userVocab));
        holder.wordText.setText(userVocab.word);

        Log.e("deftext", userVocab.word + " len: " + userVocab.listOfDefEx.size());
//        Toast.makeText(context, userVocab.word + " len: " + userVocab.listOfDefEx.size(), Toast.LENGTH_SHORT).show();

        if (userVocab.listOfDefEx.size() > 0) {
            Log.e("going", "into " + userVocab.word + " 0");
            if (userVocab.listOfDefEx.size() > 1) {
                String text = userVocab.listOfDefEx.get(0).definition + "\n...";
                holder.def1Text.setText(text);
            } else {
                //otherwise no number
                String text = userVocab.listOfDefEx.get(0).definition;
                holder.def1Text.setText(text);
            }
        }
        //region not working list of definitions >:(
//        if (userVocab.listOfDefEx.size() > 1) {
//            Log.e("going", "into " + userVocab.word + " 1");
//            String text ="2. " + userVocab.listOfDefEx.get(1).definition;
//            holder.def2Text.setText(text);
//        }
//
//        if (userVocab.listOfDefEx.size() > 2) {
//            Log.e("going", "into " + userVocab.word + " 2");
//            if (userVocab.listOfDefEx.size() >= 4) {
//                String text = "3. " +userVocab.listOfDefEx.get(2).definition + "\n\n...";
//                holder.def3Text.setText(text);
//            } else {
//                String text = "3. " + userVocab.listOfDefEx.get(2).definition;
//                holder.def3Text.setText(text);
//            }
//        }




    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return sortedDataSet.size();
    }
}