package com.steven.android.vocabkeepernew.showuservocab;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.steven.android.vocabkeepernew.R;

/**
 * Created by Steven on 11/29/2015.
 */
public class WordDisplayCursorAdapter extends CursorAdapter {
    public WordDisplayCursorAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);

    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
        return LayoutInflater.from(context).inflate(R.layout.item_uservocab, viewGroup, false);
    }

    @Override
    public void bindView(View rootView, Context context, Cursor cursor) {
        TextView wordText = (TextView) rootView.findViewById(R.id.word_text);
        TextView def1Text = (TextView) rootView.findViewById(R.id.def1_text);

        String word = cursor.getString(cursor.getColumnIndexOrThrow("word"));
        String def1 = cursor.getString(cursor.getColumnIndexOrThrow("d1"));

        wordText.setText(word);
        def1Text.setText(def1);

    }
}
