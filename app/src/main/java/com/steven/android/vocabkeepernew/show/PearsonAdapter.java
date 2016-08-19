package com.steven.android.vocabkeepernew.show;

import android.content.Intent;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.steven.android.vocabkeepernew.R;
import com.steven.android.vocabkeepernew.get.PearsonAsyncTask;
import com.steven.android.vocabkeepernew.input.UserVocabInsertService;
import com.steven.android.vocabkeepernew.showuservocab.sqlite.UserVocab;
import com.steven.android.vocabkeepernew.utility.PearsonAnswer;
import com.steven.android.vocabkeepernew.utility.ViewUtility;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Steven on 8/17/2016.
 */ //todo: animation for adding elements
//todo: swirling loading button
public class PearsonAdapter extends RecyclerView.Adapter<PearsonAdapter.ViewHolder> {
    private DisplayDefinitionPopupActivity displayDefinitionPopupActivity;
    private ArrayList<PearsonAnswer.DefinitionExamples> unsortedDataSet, sortedPearsonDataSet;
    private RecyclerViewClickListener itemListener;
    private boolean mySelected[] = new boolean[500];
    private String mainWord; // main word, without stem changes
    public HashMap<String, String> abbr;
    public boolean surpressGray; // when finishing activiting only.

    // Provide a suitable constructor (depends on the kind of dataset)
    public PearsonAdapter(DisplayDefinitionPopupActivity displayDefinitionPopupActivity, ArrayList<PearsonAnswer.DefinitionExamples> myDataset, RecyclerViewClickListener listener, String word) {
        this.displayDefinitionPopupActivity = displayDefinitionPopupActivity;
        unsortedDataSet = myDataset;

        Log.e("pearson constructor", unsortedDataSet.size() + "  vs " + myDataset.size());

        itemListener = listener;
        mainWord = word.trim();

        sortedPearsonDataSet = new ArrayList<>();

        abbr = new HashMap<>();
        abbr.put("adjective", "adj.");
        abbr.put("verb", "v.");
        abbr.put("noun", "n.");
        abbr.put("adverb", "adv.");
//            abbr.put("preposition", "prep.");
//            abbr.put("conjunction", "conj.");
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView definitionText;
        TextView exampleText;
        View fillerView;
        RelativeLayout colorView;
//            RelativeLayout relativeLayout;

        public ViewHolder(View v) {
            super(v);
            definitionText = (TextView) v.findViewById(R.id.definition_text);
            exampleText = (TextView) v.findViewById(R.id.example_text);
            colorView = (RelativeLayout) v.findViewById(R.id.color_view);
            fillerView = v.findViewById(R.id.filler);
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

//    region add remove
// public void add(int position, PearsonAnswer.DefinitionExamples item) {
//            dataSet.add(position, item);
//            notifyItemInserted(position);
//        }

    public void remove(PearsonAnswer.DefinitionExamples item) {
        int position = sortedPearsonDataSet.indexOf(item);
        sortedPearsonDataSet.remove(position);
        notifyItemRemoved(position);
    }

    public void removeByIdx(int idx) {
        Log.e("idxr", "removingByIdx " + idx);
        sortedPearsonDataSet.remove(idx);
        notifyItemRemoved(idx);
    }

    public void add(PearsonAnswer.DefinitionExamples def) {
        sortedPearsonDataSet.add(def);
        notifyItemInserted(sortedPearsonDataSet.size() - 1);
    }

    public void removeTemp() {
        int removedSoFar = 0;
        for (int i = 0; i < 500; i++) {
            if (mySelected[i]) {
                Log.e("selected", i + "");
//                    remove(sortedPearsonDataSet.get(i));
                removeByIdx(i - (removedSoFar++));
            }
            mySelected[i] = false; //surpress trning gray
        }
    }

    public void animateSlidesAndInsertUserVocab() {//important!!! main purpose of the app
        ArrayList<UserVocab> listToSend = new ArrayList<>();

        for (int i = 0; i < sortedPearsonDataSet.size(); i++) {
            if (mySelected[i]) {
                Log.e("sliding", i + "");
//                    remove(sortedPearsonDataSet.get(i));
                slideByIdx(i);

                ArrayList<PearsonAnswer.DefinitionExamples> defExSend = new ArrayList<>();
                defExSend.add(sortedPearsonDataSet.get(i));
                listToSend.add(new UserVocab(/*mainWord*/sortedPearsonDataSet.get(i).wordForm, defExSend, System.currentTimeMillis(), "August 13th"));

//                    // if they equal the main word, they must all equal eachother:)
//                    if (sortedPearsonDataSet.get(i).wordForm.trim().toLowerCase().equals(mainWord.toLowerCase())) {
//                        Log.e("insertingSQLite", sortedPearsonDataSet.get(i).definition);
//
//                        UserVocab userVocab = new UserVocab(mainWord)
//                    }
            }
            mySelected[i] = false; //surpress turning gray
        }


        //this list should never be empty but just in case it is...
        if (listToSend.isEmpty()) {
            Log.e("wat", "listToSend is empty");
        } else {
            Intent insertIntent = new Intent(displayDefinitionPopupActivity.getApplicationContext(), UserVocabInsertService.class);
            Log.e("listToSend", (new Gson()).toJson(listToSend));
            insertIntent.putExtra(UserVocabInsertService.JSON_KEY, (new Gson()).toJson(listToSend));
            displayDefinitionPopupActivity.startService(insertIntent);
        }

    }

    public void slideByIdx(int idx) {
        TranslateAnimation translateAnimation = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, 20.0f,
                Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, 0.0f);
        translateAnimation.setDuration(DisplayDefinitionPopupActivity.REMOVE_DURATION + 50);

        View rootView = (displayDefinitionPopupActivity.defExRecycler.getLayoutManager().findViewByPosition(idx));
        if (rootView != null) {
            TextView defText = (TextView) rootView.findViewById(R.id.definition_text);
            defText.startAnimation(translateAnimation);
            defText.setVisibility(View.INVISIBLE);

            TextView exText = (TextView) rootView.findViewById(R.id.example_text);
            if (exText != null) {
                exText.startAnimation(translateAnimation);
                exText.setVisibility(View.INVISIBLE);
            }
        }

    }


    // Create new views (invoked by the layout manager)
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent,
                                                        int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_definition_example, parent, false);
        // set the view's size, margins, paddings and layout parameters
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final int pos = position;

        Log.e("adapter", "called for " + pos);

        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        final PearsonAnswer.DefinitionExamples definitionExamples = sortedPearsonDataSet.get(position);
        String number = String.format("%d", position + 1) + ". ";
        String form = ((sortedPearsonDataSet.get(position).wordForm.trim().toLowerCase().equals(mainWord.toLowerCase())) ?
                ""
                : (" (" + sortedPearsonDataSet.get(position).wordForm.trim() + ") "));
        String abbrev = ((abbr.containsKey(sortedPearsonDataSet.get(position).partOfSpeech)) ?
                (" (" + abbr.get(sortedPearsonDataSet.get(position).partOfSpeech.trim()) + ")")
                : (" (" + sortedPearsonDataSet.get(position).partOfSpeech + ")"));
        String part = ((!(sortedPearsonDataSet.get(position).partOfSpeech.equals("---"))) ? (abbrev) : (""));


        String defText = number + form + sortedPearsonDataSet.get(position).definition + part;
        String htmlDefText = "<strong> " + number + form + "</strong>" + sortedPearsonDataSet.get(position).definition + "<i> " + part + "</i>";
//            Log.e("html", htmlDefText);

        holder.definitionText.setText(Html.fromHtml(htmlDefText));
        boolean hasExample = false;
        if ((sortedPearsonDataSet.get(position).examples.get(0).equals(PearsonAsyncTask.DEFAULT_NO_EXAMPLE))) { // if no example
            ViewGroup parent = ((ViewGroup) (holder.exampleText.getParent()));
            if (parent != null) {
                parent.removeView(holder.exampleText); // remove example text view
            } else {
                String set = ('"' + sortedPearsonDataSet.get(position).examples.get(0).trim() + '"');
                holder.exampleText.setText(set);
                hasExample = true;
            }
        } else {
            String set = ('"' + sortedPearsonDataSet.get(position).examples.get(0).trim() + '"');
            holder.exampleText.setText(set);
            hasExample = true;
        }

//            region holder.exampleText.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
////                    remove(definitionExamples);
//                    Toast.makeText(getApplicationContext(), "niceclick " + Integer.toString(pos), Toast.LENGTH_SHORT).show();
//                }
//            });

        if (position == 0) { // add space above because of send button
            ViewUtility.setMargins(16f, 36f, 16f, (hasExample) ? 16f : 16f, holder.definitionText, displayDefinitionPopupActivity.getApplicationContext());
        }

        if (sortedPearsonDataSet.get(position).wordForm.trim().equals(mainWord)) { // if they are perfect matches, add more botttom margin
//                setMargins(0f, 0f, 0f, 200f, holder.fillerView);

            ///fail

            Log.e("equals", "yes "/* + fillerHeight*/);
        } else {
            Log.e("equals", "no");
        }

        if (mySelected[position] && !surpressGray) {
            Log.e("color", "setting " + Integer.toString(pos) + " pressed");
            holder.colorView.setBackgroundColor(Color.parseColor(DisplayDefinitionPopupActivity.COLOR_PRESSED));

            if (displayDefinitionPopupActivity.doChangeFont) {
                holder.definitionText.setTextSize(displayDefinitionPopupActivity.SMALL_FONT);
                if (holder.exampleText != null) {
                    holder.exampleText.setTextSize(displayDefinitionPopupActivity.SMALL_FONT);
                }
            }

        } else {
            holder.colorView.setBackgroundColor(Color.parseColor(DisplayDefinitionPopupActivity.COLOR_NEUTRAL));
            Log.e("color", "setting " + Integer.toString(pos) + " neutral");

            if (displayDefinitionPopupActivity.doChangeFont) {
                holder.definitionText.setTextSize(displayDefinitionPopupActivity.BIG_FONT);
                if (holder.exampleText != null) {
                    holder.exampleText.setTextSize(displayDefinitionPopupActivity.BIG_FONT);
                }
            }
        }

    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return sortedPearsonDataSet.size();
    }
}
