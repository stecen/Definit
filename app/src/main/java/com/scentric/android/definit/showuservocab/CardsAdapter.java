package com.scentric.android.definit.showuservocab;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.scentric.android.definit.R;
import com.scentric.android.definit.utility.UserVocab;
import com.scentric.android.definit.sqlite.VocabSQLHelper;
import com.scentric.android.definit.utility.DateUtility;
import com.scentric.android.definit.utility.PearsonAnswer;
import com.scentric.android.definit.utility.RecyclerViewClickListener;
import com.scentric.android.definit.utility.ViewUtility;

import java.util.ArrayList;

/**
 * Created by Steven on 9/2/2016.
 *
 * Flashcards in main frag and fave frag.
 * Each flashcard also contains a list of definition of examples.
 *
 */
public class CardsAdapter extends RecyclerView.Adapter<CardsAdapter.ViewHolder> {
    public ArrayList<UserVocab> sortedDataSet;
    private RecyclerViewClickListener itemListener;
    private Context context;
    UserDetailsActivity userDetailsActivity;

    boolean isFave = false;

    CardDefExAdapter cardDefExAdapter;

    public static int IS_FAVE_DRAWABLE = R.drawable.ic_star_black_24dp;
    public static int NOT_FAVE_DRAWABLE = R.drawable.ic_star_border_black_24dp;


    // Provide a suitable constructor (depends on the kind of dataset)
    public CardsAdapter(ArrayList<UserVocab> myDataset, Context context, UserDetailsActivity userDetailsActivity) {
        sortedDataSet = myDataset;
        this.context = context;
        this.userDetailsActivity = userDetailsActivity;

        Log.e("constructor", sortedDataSet.size() + "  vs " + myDataset.size());

    }

    public CardsAdapter(ArrayList<UserVocab> myDataset, Context context, UserDetailsActivity userDetailsActivity, boolean isFave) {
        sortedDataSet = myDataset;
        this.context = context;
        this.userDetailsActivity = userDetailsActivity;

        Log.e("constructor", sortedDataSet.size() + "  vs " + myDataset.size());

        this.isFave = isFave;

    }


    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView wordText, def1Text/*, def2Text, def3Text*/, toolbarText, tagText;
        ImageView faveImage;
        CardView cardView;
        RecyclerView recyclerView;
        View toolbarView;
        TextView dateText;
        RelativeLayout cardRelative;

        public ViewHolder(View v) {
            super(v);
            wordText = (TextView) v.findViewById(R.id.word_text);
            cardView = (CardView) v.findViewById(R.id.card_view);
            recyclerView = (RecyclerView) v.findViewById(R.id.definition_example_recycler);
            cardRelative = (RelativeLayout) v.findViewById(R.id.card_relative);
            faveImage = (ImageView) v.findViewById(R.id.card_fave_image);
            toolbarView = (View) v.findViewById(R.id.toolbar_view);
            tagText = (TextView) v.findViewById(R.id.tag_text);
            dateText = (TextView) v.findViewById(R.id.card_date_text);

            cardRelative.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Log.e("card", "clicked relative");
                }
            });
            cardView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Log.e("card", "clicked");
                }
            });
            recyclerView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Log.e("card", "clicked");
                }
            });

        }

    }


    // Create new views (invoked by the layout manager)
    @Override
    public CardsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                      int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_cards_snap, parent, false);
        // set the view's size, margins, paddings and layout parameters
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    private int getNextWhitespace(String s, int idx) {
        while (idx < s.length()) {
            if (Character.isWhitespace(s.charAt(idx))) {
                return idx+ 1;
            }
            idx += 1;
        }
        return s.length() - 1;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final int pos = position;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            holder.toolbarView.setElevation(8);
            holder.faveImage.setElevation(9);
            holder.wordText.setElevation(8);
        }

        holder.wordText.setText(sortedDataSet.get(pos).word);

        holder.recyclerView.setLayoutManager(new LinearLayoutManager(context));
        cardDefExAdapter = new CardDefExAdapter(sortedDataSet.get(pos).listOfDefEx);
        holder.recyclerView.setAdapter(cardDefExAdapter);

//        holder.tagText.setText(String.valueOf(sortedDataSet.get(pos).wordIdx) + sortedDataSet.get(pos).tag);

        if (sortedDataSet.get(pos).hasContext()) { // if we should even display the context/tag
            String intro = "\"...";
            String tag = intro + sortedDataSet.get(pos).tag + "\""; // todo: make StringBuilder/Buffer
            SpannableStringBuilder boldSpan = new SpannableStringBuilder(tag);
            int startBold = sortedDataSet.get(pos).wordIdx + intro.length(); // include the quotation marks
            if (startBold > 0) {
                int endBold = getNextWhitespace(tag, startBold);
                boldSpan.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), startBold, endBold, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
            holder.tagText.setText(boldSpan);
            holder.tagText.setVisibility(View.VISIBLE);
        } else { // no context so don't display
            holder.tagText.setVisibility(View.INVISIBLE);
        }

        // toggle fave image
        if (sortedDataSet.get(position).fave) {
            Drawable faveDrawable = context.getResources().getDrawable(IS_FAVE_DRAWABLE);
            holder.faveImage.setImageDrawable(faveDrawable);
        } else {
            Drawable notFaveDrawable = context.getResources().getDrawable(NOT_FAVE_DRAWABLE);
            holder.faveImage.setImageDrawable(notFaveDrawable);
        }

        // TODO: what is this warning?
        holder.dateText.setText(DateUtility.getFullDate(sortedDataSet.get(pos).date, "MM/dd") + ", " + DateUtility.getTime(sortedDataSet.get(pos).date));

        final UserVocab userVocab = sortedDataSet.get(pos);
        final ViewHolder fholder = holder;
        holder.faveImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (userVocab.fave) {
                    // toggle to false, and send to database. regardless of whether the sqlite succeeds, update the ui. responsive :)
                    Drawable notFaveDrawable = context.getResources().getDrawable(NOT_FAVE_DRAWABLE);
                    ((ImageView) fholder.faveImage).setImageDrawable(notFaveDrawable);
                    ViewUtility.boing(fholder.faveImage);

                    VocabSQLHelper helper = VocabSQLHelper.getInstance(context.getApplicationContext());
                    helper.toggleFavorite(userVocab);
                    sortedDataSet.get(pos).fave = false; // todo: once the sql becomes async, this can't be here
                } else {
                    Drawable faveDrawable = context.getResources().getDrawable(IS_FAVE_DRAWABLE);
                    ((ImageView) fholder.faveImage).setImageDrawable(faveDrawable);
                    ViewUtility.boing(fholder.faveImage);

                    VocabSQLHelper helper = VocabSQLHelper.getInstance(context.getApplicationContext());
                    helper.toggleFavorite(userVocab);
                    sortedDataSet.get(pos).fave = true;
                }
            }
        });

        if (isFave) { // set color to yellow
            holder.cardView.setCardBackgroundColor(Color.parseColor("#ffffca"));
        }
    }


    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return sortedDataSet.size();
    }


    // A RecyclerView inside of a RecyclerView
    // This one is just for definition + examples
    public class CardDefExAdapter extends RecyclerView.Adapter<CardDefExAdapter.ViewHolder> {
        public ArrayList<PearsonAnswer.DefinitionExamples> defExDataSet;


        // Provide a suitable constructor (depends on the kind of dataset)
        public CardDefExAdapter(ArrayList<PearsonAnswer.DefinitionExamples> myDataset) {
            defExDataSet = myDataset;

        }

        public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
            TextView definitionText;
            TextView exampleText;


            public ViewHolder(View v) {
                super(v);
                definitionText = (TextView) v.findViewById(R.id.card_definition_text);
                exampleText = (TextView) v.findViewById(R.id.card_example_text);


                v.setOnClickListener(this);
            }

            @Override
            public void onClick(View v) {
//            itemListener.recyclerViewListClicked(v, this.getLayoutPosition());

            }
        }


        // Create new views (invoked by the layout manager)
        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent,
                                             int viewType) {
            // create a new view
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_card_recycler_defex, parent, false);
            // set the view's size, margins, paddings and layout parameters
            ViewHolder vh = new ViewHolder(v);
            return vh;
        }

        // Replace the contents of a view (invoked by the layout manager)
        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            final int pos = position;

            holder.definitionText.setText(/*Html.fromHtml(*/ Integer.toString(pos + 1) + ". " + defExDataSet.get(pos).definition/*)*/);
            Log.e("cardadapter", "setting definition text");

            if (defExDataSet.get(pos).examples.isEmpty() || defExDataSet.get(pos).examples.get(0).trim().equals(PearsonAnswer.DEFAULT_NO_EXAMPLE)) {
                holder.exampleText.setVisibility(View.GONE);
            } else {
                String example = '"' + defExDataSet.get(pos).examples.get(0).trim() + '"';
                holder.exampleText.setText(example);
            }
        }
        @Override
        public int getItemCount() {
            return defExDataSet.size();
        }


    }


}