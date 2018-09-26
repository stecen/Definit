package com.scentric.android.definit.show;

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
import com.scentric.android.definit.R;
import com.scentric.android.definit.input.UserVocabInsertService;
import com.scentric.android.definit.showuservocab.sqlite.UserVocab;
import com.scentric.android.definit.useless.DisplayDefinitionPopupActivity;
import com.scentric.android.definit.utility.PearsonAnswer;
import com.scentric.android.definit.utility.PearsonComparator;
import com.scentric.android.definit.utility.RecyclerViewClickListener;
import com.scentric.android.definit.utility.ViewUtility;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Steven on 8/17/2016.
 */

public class PearsonAdapter extends RecyclerView.Adapter<PearsonAdapter.ViewHolder> {
    private SearchAndShowActivity searchAndShowActivity; // Adapter is inextricably connected to searchAndShowActivity
    public ArrayList<PearsonAnswer.DefinitionExamples> unsortedDataSet, sortedPearsonDataSet;
    private RecyclerViewClickListener itemListener;
    private boolean mySelected[] = new boolean[500], disBig[] = new boolean[500]; // myselected is for saving to database, disbig is for remembering which ones to make big
    private int disLen[] = new int[500];
    private String mainWord; // main word, without stem changes
    public HashMap<String, String> abbr;
    public boolean surpressGray; // when finishing activity only.
    public static String EXTRA_CONTEXT = "PutExtraContextHerePleaseLmao";
    public int contextIdx = -1;

    // Provide a suitable constructor (depends on the kind of dataset)
    public PearsonAdapter(SearchAndShowActivity searchAndShowActivity, ArrayList<PearsonAnswer.DefinitionExamples> myDataset, RecyclerViewClickListener listener, String word) {
        this.searchAndShowActivity = searchAndShowActivity;
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

        for (int i = 0; i < 500; i++) {
            disLen[i] = -1; // unvisited
        }
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView wordformText;
        TextView definitionText;
        TextView exampleText;
        TextView posText;
        View defFillerView, exFillerView;
        RelativeLayout colorView;
//            RelativeLayout relativeLayout;

        public ViewHolder(View v) {
            super(v);
            definitionText = (TextView) v.findViewById(R.id.definition_text);
            exampleText = (TextView) v.findViewById(R.id.de_example_text);
            colorView = (RelativeLayout) v.findViewById(R.id.color_view);
            defFillerView = v.findViewById(R.id.def_bottom_filler);
            exFillerView = v.findViewById(R.id.ex_bottom_filler);
            wordformText = (TextView) v.findViewById(R.id.wordform_header_text);
            posText = (TextView) v.findViewById(R.id.pos_text);
//                relativeLayout = (RelativeLayout) v;
            v.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            itemListener.recyclerViewListClicked(v, this.getLayoutPosition());

        }
    }


    public void updateMainWord (String word) {
        mainWord = word;
    }


    public void updateSelect(int idx, boolean sel) {
        mySelected[idx] = sel;
    }

//    region add remove
// public void add(int position, PearsonAnswer.DefinitionExamples item) {
//            dataSet.add(position, item);
//            notifyItemInserted(position);
//        }

    public void clearAll() { // for when the user requests and new definition
        for (int i = sortedPearsonDataSet.size()-1; i>=0; i--) {
            sortedPearsonDataSet.remove(i);
            notifyItemRemoved(i);
            Log.e("clearall", "removing " + i);
        }

//        sortedPearsonDataSet = new ArrayList<>();
//        notifyDataSetChanged(); // just in case

        contextIdx = -1; // index of the context input
        for (int i = 0; i < 500; i ++) {
            disBig[i] = false; // unused
            disLen[i] = -1; // unvisited

        }
    }

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
            Intent insertIntent = new Intent(searchAndShowActivity.getApplicationContext(), UserVocabInsertService.class);
//            Log.e("listToSend", (new Gson()).toJson(listToSend));
            insertIntent.putExtra(UserVocabInsertService.JSON_KEY, (new Gson()).toJson(listToSend));
            searchAndShowActivity.startService(insertIntent);
        }

    }

    public void slideByIdx(int idx) {
        TranslateAnimation translateAnimation = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, 20.0f,
                Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, 0.0f);
        translateAnimation.setDuration(DisplayDefinitionPopupActivity.REMOVE_DURATION + 50);

        View rootView = (searchAndShowActivity.defExRecycler.getLayoutManager().findViewByPosition(idx));
        if (rootView != null) {
            TextView defText = (TextView) rootView.findViewById(R.id.definition_text);
            defText.startAnimation(translateAnimation);
            defText.setVisibility(View.INVISIBLE);

            TextView exText = (TextView) rootView.findViewById(R.id.de_example_text);
            if (exText != null) {
                exText.startAnimation(translateAnimation);
                exText.setVisibility(View.INVISIBLE);
            }

            TextView posText = (TextView) rootView.findViewById(R.id.pos_text);
            if (posText != null) {
                posText.startAnimation(translateAnimation);
                posText.setVisibility(View.INVISIBLE);
            }

            TextView headText = (TextView) rootView.findViewById(R.id.wordform_header_text);
            if (headText != null && headText.getVisibility() == View.VISIBLE) {
                headText.startAnimation(translateAnimation);
                headText.setVisibility(View.INVISIBLE);
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
        String form = ((sortedPearsonDataSet.get(position).wordForm.trim().toLowerCase().equals(mainWord.toLowerCase()) || sortedPearsonDataSet.get(position).wordForm.trim().equals("")) ?
                ""
                : (" (" + sortedPearsonDataSet.get(position).wordForm.trim() + ") "));

//        String abbrev = ((abbr.containsKey(sortedPearsonDataSet.get(position).partOfSpeech)) ?
//                (" (" + abbr.get(sortedPearsonDataSet.get(position).partOfSpeech.trim()) + ")")
//                : (" (" + sortedPearsonDataSet.get(position).partOfSpeech + ")"));
        String abbrev = sortedPearsonDataSet.get(position).partOfSpeech.trim(); // lol for now
        String part = ((!(sortedPearsonDataSet.get(position).partOfSpeech.equals("---"))) ? (abbrev) : (""));


        String defText = number + form + sortedPearsonDataSet.get(position).definition + part;
        boolean isValid = sortedPearsonDataSet.get(position).definition.trim().equals(PearsonAnswer.DEFAULT_NO_DEFINITION);

        String htmlDefText = "<strong> " + ((!isValid) ? number : "") + /*form +*/ "</strong>" + sortedPearsonDataSet.get(position).definition /*+ "<i> " + part + "</i>"*/;
        holder.definitionText.setText(Html.fromHtml(htmlDefText));

        // new wordformtext
        // if equals mainword, don't show anything
        if (form.equals("")) {
//            holder.wordformText.setText("wtf");
            holder.wordformText.setVisibility(View.GONE);
        } else {
            holder.wordformText.setVisibility(View.VISIBLE);
            holder.wordformText.setText(Html.fromHtml("<strong>" + sortedPearsonDataSet.get(position).wordForm.trim()+ "</strong>"));
        }

        // part of speech text
        if (part.equals("")) {
            RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, 0);
            layoutParams.addRule(RelativeLayout.BELOW, R.id.wordform_header_text);

            holder.posText.setLayoutParams(layoutParams);
        } else {
            RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            layoutParams.addRule(RelativeLayout.BELOW, R.id.wordform_header_text);
            int px16 = Math.round(ViewUtility.convertDpToPixel(16, searchAndShowActivity));
            int px8 = Math.round(ViewUtility.convertDpToPixel(8, searchAndShowActivity));
//            int px12 = Math.round(ViewUtility.convertDpToPixel(12, searchAndShowActivity));
            layoutParams.setMargins(px16, px16, px16, 0);
            holder.posText.setLayoutParams(layoutParams); // reprogram the margins
            holder.posText.setText(Html.fromHtml("<i>" + part + "</i>"));
        }


        // if there is a context
        boolean hasExample = false;
        if (sortedPearsonDataSet.get(position).examples.size() == 0 ||
                (sortedPearsonDataSet.get(position).examples.get(0).equals(PearsonAnswer.DEFAULT_NO_EXAMPLE))) { // if no example
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

        //todo: fix this viewholder mess  http://androidshenanigans.blogspot.com/2015/02/viewholder-pattern-common-mistakes.html
//            region holder.exampleText.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
////                    remove(definitionExamples);
//                    Toast.makeText(getApplicationContext(), "niceclick " + Integer.toString(pos), Toast.LENGTH_SHORT).show();
//                }
//            });

        if (sortedPearsonDataSet.get(position).wordForm.trim().equals(mainWord)) { // if they are perfect matches, add more botttom margin
//                setMarginsRelative(0f, 0f, 0f, 200f, holder.defFillerView);

            ///fail

            Log.e("equals", "yes "/* + fillerHeight*/);
        } else {
            Log.e("equals", "no");
        }

        if (mySelected[position] && !surpressGray) {
            Log.e("color", "setting " + Integer.toString(pos) + " pressed");
            holder.colorView.setBackgroundColor(Color.parseColor(DisplayDefinitionPopupActivity.COLOR_PRESSED));

            if (searchAndShowActivity.doChangeFont) {
                holder.definitionText.setTextSize(searchAndShowActivity.SMALL_FONT);
                if (holder.exampleText != null) {
                    holder.exampleText.setTextSize(searchAndShowActivity.SMALL_FONT);
                }
                if (holder.wordformText != null && holder.wordformText.getVisibility() == View.VISIBLE) {
                    holder.wordformText.setTextSize(searchAndShowActivity.SMALL_HEAD_FONT);
                }
                if (holder.posText != null) {
                    holder.posText.setTextSize(searchAndShowActivity.SMALL_FONT);
                }
            }

        } else {
            holder.colorView.setBackgroundColor(Color.parseColor(DisplayDefinitionPopupActivity.COLOR_NEUTRAL));
            Log.e("color", "setting " + Integer.toString(pos) + " neutral");

            if (searchAndShowActivity.doChangeFont) {
                holder.definitionText.setTextSize(searchAndShowActivity.BIG_FONT);
                if (holder.exampleText != null) {
                    holder.exampleText.setTextSize(searchAndShowActivity.BIG_FONT);
                }
                if (holder.wordformText != null && holder.wordformText.getVisibility() == View.VISIBLE) {
                    holder.wordformText.setTextSize(searchAndShowActivity.BIG_HEAD_FONT);
                }
                if (holder.posText != null) {
                    holder.posText.setTextSize(searchAndShowActivity.BIG_FONT);
                }
            }
        }
            int dis = PearsonComparator.computeLevenshteinDistance(sortedPearsonDataSet.get(position).wordForm, mainWord);
            int alphaInt = dis - 5; // 6 is an arbitrary number
            if (alphaInt < 0) { // close enough to the actual wor
                 // do nothing
                if (holder.definitionText != null) {
//                Log.e("levDis", "setting def alpha " + (.87f) + " " + sortedPearsonDataSet.get(position).wordForm + " vs " + mainWord + "... " + dis);
                holder.definitionText.setAlpha(.87f);
                }
                if (holder.exampleText != null) {
                    holder.exampleText.setAlpha(.54f);
                }
                if (holder.wordformText != null && holder.wordformText.getVisibility() == View.VISIBLE) {
                    holder.wordformText.setAlpha(.87f);
                }
                if (holder.posText != null && holder.posText.getHeight() > 0) {
                    holder.posText.setAlpha(.70f);
                }
            } else { // alpha distance greater than 6... set alpha to tell user that the app is aware that this word is pretty far off
                float alpha = 1f - ((alphaInt > 8) ? 8f : (float)alphaInt) / 13f;// 16 is also arbitrary
                if (holder.definitionText != null) {
                    Log.e("levDis", "setting def alpha " + (alpha*.87f) + " " + sortedPearsonDataSet.get(position).wordForm + " vs " +mainWord + "... " + dis);
                    holder.definitionText.setAlpha(alpha * .87f);
                }
                if (holder.exampleText != null) {
                    holder.exampleText.setAlpha(alpha * .54f);
                }
                if (holder.wordformText != null && holder.wordformText.getVisibility() == View.VISIBLE) {
                    holder.wordformText.setAlpha(alpha * .87f);
                }
                if (holder.posText != null && holder.posText.getHeight() > 0) {
                    holder.posText.setAlpha(alpha * .70f);
                }
            }
        if (position != searchAndShowActivity.lastIdx) { // reset margins
            RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0);
            layoutParams.addRule(RelativeLayout.BELOW, R.id.de_example_text);
            holder.exFillerView.setLayoutParams(layoutParams);
            holder.defFillerView.setLayoutParams(layoutParams);
        } else if (position == searchAndShowActivity.lastIdx) { // if it's the last one, add filler below to allow FAB to not obscure any text
            Log.e("position", position + " last one vs " + searchAndShowActivity.lastIdx); //todo : why doesn't this work for "cool"?
            if (!(sortedPearsonDataSet.get(position).examples.size() == 0 ||
                    (sortedPearsonDataSet.get(position).examples.get(0).equals(PearsonAnswer.DEFAULT_NO_EXAMPLE)))) { // if no example
                Log.e("position", "setting margins of example filler for " + position);
                RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, Math.round(ViewUtility.convertDpToPixel(54f, searchAndShowActivity.getApplicationContext())));
                layoutParams.addRule(RelativeLayout.BELOW, R.id.de_example_text);
                holder.exFillerView.setLayoutParams(layoutParams);
            } else { // is example
                Log.e("position", "setting margins of def filler " + position);
                RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, Math.round(ViewUtility.convertDpToPixel(54f, searchAndShowActivity.getApplicationContext())));
                layoutParams.addRule(RelativeLayout.BELOW, R.id.definition_text);
                holder.defFillerView.setLayoutParams(layoutParams);
            }

        }

    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return sortedPearsonDataSet.size();
    }
}
