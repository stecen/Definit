package com.scentric.android.definit.showuservocab;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.scentric.android.definit.R;
import com.scentric.android.definit.showuservocab.sqlite.UserVocab;
import com.scentric.android.definit.showuservocab.sqlite.UserVocabHelper;
import com.scentric.android.definit.utility.DateUtility;
import com.scentric.android.definit.utility.PearsonAnswer;
import com.scentric.android.definit.utility.RecyclerViewClickListener;
import com.scentric.android.definit.utility.ViewUtility;

import java.util.ArrayList;

/**
 * Created by Steven on 9/2/2016.
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
        TextView wordText, def1Text/*, def2Text, def3Text*/, toolbarText;
        ImageView faveImage;
        CardView cardView;
        RecyclerView recyclerView;
        View toolbarView;
        TextView dateText;

        RelativeLayout headerRelative, mainRelative; // main , clickable content. exists because if there is a date header, you don't want ripplies showing through that because it's not supposed to be a part of the item
        TextView dateHeaderText;

        //        TextView exampleText;
//        View fillerView, upShaView, loShaView;
        TextView faveColorView;
        RelativeLayout cardRelative;
//        RelativeLayout colorView;
//            RelativeLayout relativeLayout;

        public ViewHolder(View v) {
            super(v);
            wordText = (TextView) v.findViewById(R.id.word_text);
            cardView = (CardView) v.findViewById(R.id.card_view);
            recyclerView = (RecyclerView) v.findViewById(R.id.definition_example_recycler);
            cardRelative = (RelativeLayout) v.findViewById(R.id.card_relative);
            faveImage = (ImageView) v.findViewById(R.id.card_fave_image);
            toolbarView = (View) v.findViewById(R.id.toolbar_view);
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


//            def1Text = (TextView) v.findViewById(R.id.def1_text);
//            mainRelative = (RelativeLayout) v.findViewById(R.id.item_uservocab_main_relative);
//            headerRelative = (RelativeLayout) v.findViewById(R.id.item_user_vocab_recycler_dateheader);
//            dateHeaderText = (TextView) v.findViewById(R.id.item_uservocab_main_dateheader_text);
//            faveImage = (ImageView) v.findViewById(R.id.fave_image);
//            faveColorView = (TextView) v.findViewById(R.id.fave_color_view);


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

        // toggle fave image
        if (sortedDataSet.get(position).fave) {
            Drawable faveDrawable = context.getResources().getDrawable(IS_FAVE_DRAWABLE);
            holder.faveImage.setImageDrawable(faveDrawable);
        } else {
            Drawable notFaveDrawable = context.getResources().getDrawable(NOT_FAVE_DRAWABLE);
            holder.faveImage.setImageDrawable(notFaveDrawable);
        }

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
                    ViewUtility.bOiiiNNnNNnnNGGGgggg(fholder.faveImage);

                    UserVocabHelper helper = UserVocabHelper.getInstance(context.getApplicationContext());
                    helper.toggleFavorite(userVocab);
                    sortedDataSet.get(pos).fave = false; // todo: once the sql becomes async, this can't be here
                } else {
                    Drawable faveDrawable = context.getResources().getDrawable(IS_FAVE_DRAWABLE);
                    ((ImageView) fholder.faveImage).setImageDrawable(faveDrawable);
                    ViewUtility.bOiiiNNnNNnnNGGGgggg(fholder.faveImage);

                    UserVocabHelper helper = UserVocabHelper.getInstance(context.getApplicationContext());
                    helper.toggleFavorite(userVocab);
                    sortedDataSet.get(pos).fave = true;
                }
            }
        });

        if (isFave) { // set color to yellow
            holder.cardView.setCardBackgroundColor(Color.parseColor("#ffffca"));
        }

//         region adjust width of  card
//        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
//        Display display = wm.getDefaultDisplay();
//        Point size = new Point();
//        display.getSize(size);
//        final int width = size.x;
//        final int height = size.y;
//        Log.e("cards", width + " " + height);

//        final WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
//        final CardView fview = holder.cardView;
//        final ViewTreeObserver vto = fview.getViewTreeObserver();
//        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
//            @Override
//            public void onGlobalLayout() {
////                LayerDrawable ld = (LayerDrawable)tv.getBackground();
////                ld.setLayerInset(1, 0, tv.getHeight() / 2, 0, 0);
////                ViewTreeObserver obs = tv.getViewTreeObserver();
//
////                ViewUtility.circleRevealExtra(coordinatorLayout); // lmfao
////                    ViewUtility.zoomIntoView(coordinatorLayout);
//
////                fview.setLayoutParams(new RelativeLayout.LayoutParams(Math.round((float) width * .66f), height));
////                fview.setLayoutParams(new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
//
//
//
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
//                    fview.getViewTreeObserver().removeOnGlobalLayoutListener(this);
//                } else {
//                    fview.getViewTreeObserver().removeGlobalOnLayoutListener(this);
//                }
//            }
//
//        });
        //endregion
    }


    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return sortedDataSet.size();
    }


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
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_card_recycler_uservocab, parent, false);
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