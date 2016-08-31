package com.steven.android.vocabkeepernew.showuservocab;

import android.content.Context;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.steven.android.vocabkeepernew.showuservocab.sqlite.HistoryVocab;
import com.steven.android.vocabkeepernew.showuservocab.sqlite.UserVocabHelper;
import com.steven.android.vocabkeepernew.utility.DateUtility;
import com.steven.android.vocabkeepernew.utility.PearsonAnswer;
import com.steven.android.vocabkeepernew.R;
import com.steven.android.vocabkeepernew.show.RecyclerViewClickListener;
import com.steven.android.vocabkeepernew.showuservocab.sqlite.UserVocab;
import com.steven.android.vocabkeepernew.utility.ViewUtility;

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

    public static int IS_FAVE_DRAWABLE = R.drawable.ic_star_black_24dp;
    public static int NOT_FAVE_DRAWABLE = R.drawable.ic_star_border_black_24dp;

    boolean isFaveList = false;

    // Provide a suitable constructor (depends on the kind of dataset)
    public UserVocabAdapter(ArrayList<UserVocab> myDataset, RecyclerViewClickListener listener, Context context, boolean isFaveList) {
        sortedDataSet = myDataset;
        itemListener = listener;
        this.context = context;
        this.isFaveList = isFaveList; // depends on the source of the caller. If the caller is looking to display a favorites list, display a yellow background

        Log.e("constructor", sortedDataSet.size() + "  vs " + myDataset.size());

    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView wordText, def1Text/*, def2Text, def3Text*/;
        ImageView faveImage;

        RelativeLayout headerRelative, mainRelative; // main , clickable content. exists because if there is a date header, you don't want ripplies showing through that because it's not supposed to be a part of the item
        TextView dateHeaderText;

        TextView exampleText;
        View fillerView;
        TextView faveColorView;
        RelativeLayout colorView;
//            RelativeLayout relativeLayout;

        public ViewHolder(View v) {
            super(v);
            wordText = (TextView) v.findViewById(R.id.word_text);
            def1Text = (TextView) v.findViewById(R.id.def1_text);
            mainRelative = (RelativeLayout) v.findViewById(R.id.item_uservocab_main_relative);
            headerRelative = (RelativeLayout) v.findViewById(R.id.item_user_vocab_recycler_dateheader);
            dateHeaderText = (TextView) v.findViewById(R.id.item_uservocab_main_dateheader_text);
            faveImage = (ImageView) v.findViewById(R.id.fave_image);
            faveColorView = (TextView) v.findViewById(R.id.fave_color_view);
//            def2Text = (TextView) v.findViewById(R.id.def2_text);
//            def3Text = (TextView) v.findViewById(R.id.def3_text);
//            exampleText = (TextView) v.findViewById(R.id.example_text);

//            colorView = (RelativeLayout) v.findViewById(R.id.color_view);
//            fillerView = v.findViewById(R.id.filler);
//                relativeLayout = (RelativeLayout) v;
            mainRelative.setOnClickListener(this);
//            faveImage.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.item_uservocab_main_relative:
                    itemListener.recyclerViewListClicked(v, this.getLayoutPosition());
                    break;
//                case R.id.fave_image: // favorite clicked
//                    if (((ImageView) v).getDrawable().get)
//                    break;
            }


        }
    }

    public void updateSelect(int idx, boolean sel) {
        mySelected[idx] = sel;
    }

    public void replaceData(ArrayList<UserVocab> list) {
        if (sortedDataSet.equals(list)) {
            Log.e("replaceUser", "equals");
        } else {
            Log.e("replaceUser", "not equals");
            sortedDataSet = list;
            notifyDataSetChanged();
        }

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

        final ViewHolder fholder = holder;
        if (isFaveList) {
//            holder.faveColorView.setVisibility(View.VISIBLE);
            final TextView fview = holder.faveColorView;
            final ViewTreeObserver vto = fview.getViewTreeObserver();
            vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    int height = fholder.mainRelative.getHeight();
                    fholder.faveColorView.setVisibility(View.VISIBLE);
                    fholder.faveColorView.setHeight(height);
                    Log.e("isfavelist", "" + height);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                        fview.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    } else {
                        fview.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                    }
                }

            });
        }


        // if this item is the first
        String date = DateUtility.getFullDate(userVocab.date, "MM/dd");
        String prevDate = " ";
        if (position != 0) {
            prevDate = (DateUtility.getFullDate(sortedDataSet.get(position-1).date, "MM/dd"));
        }
//        Log.e("datecmp", date + " vs " + prevDate + "...    " + date.equals(prevDate));
        if (position != 0 && !(date.equals(prevDate))) {
            holder.headerRelative.setVisibility(View.VISIBLE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                holder.mainRelative.setElevation(8);
            }
            holder.dateHeaderText.setText(date);
        } else if (position != 0) {
//            holder.dateHeaderText.setText(" "); // if is the same date as yesterday
            holder.headerRelative.setVisibility(View.GONE);
        } else { // exclusive for pos == 0
            holder.headerRelative.setVisibility(View.VISIBLE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                holder.mainRelative.setElevation(9);
            }
            holder.dateHeaderText.setPadding(0,0,0,0);
            holder.dateHeaderText.setText(date);
        }

        // toggle fave image
        if (sortedDataSet.get(position).fave) {
            Drawable faveDrawable = context.getResources().getDrawable(IS_FAVE_DRAWABLE);
            holder.faveImage.setImageDrawable(faveDrawable);
        } else {
            Drawable notFaveDrawable = context.getResources().getDrawable(NOT_FAVE_DRAWABLE);
            holder.faveImage.setImageDrawable(notFaveDrawable);
        }

        // update ui and send to database
        holder.faveImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View faveImage) {
//                sortedDataSet.get(pos).
                if (userVocab.fave) {
                    // toggle to false, and send to database. regardless of whether the sqlite succeeds, update the ui. responsive :)
                    Drawable notFaveDrawable = context.getResources().getDrawable(NOT_FAVE_DRAWABLE);
                    ((ImageView)faveImage).setImageDrawable(notFaveDrawable);

                    UserVocabHelper helper = UserVocabHelper.getInstance(context.getApplicationContext());
                    helper.toggleFavorite(userVocab);
                    sortedDataSet.get(pos).fave = false; // todo: once the sql becomes async, this can't be here
                } else {
                    Drawable faveDrawable = context.getResources().getDrawable(IS_FAVE_DRAWABLE);
                    ((ImageView)faveImage).setImageDrawable(faveDrawable);

                    UserVocabHelper helper = UserVocabHelper.getInstance(context.getApplicationContext());
                    helper.toggleFavorite(userVocab);
                    sortedDataSet.get(pos).fave = true;
                }
            }
        });








//        Log.e("adapterGson", (new Gson()).toJson(userVocab));
        holder.wordText.setText(userVocab.word);

        Log.e("deftext", userVocab.word + " len: " + userVocab.listOfDefEx.size());
//        Toast.makeText(context, userVocab.word + " len: " + userVocab.listOfDefEx.size(), Toast.LENGTH_SHORT).show();

        if (userVocab.listOfDefEx.size() > 0) {
//            Log.e("going", "into " + userVocab.word + " 0");
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