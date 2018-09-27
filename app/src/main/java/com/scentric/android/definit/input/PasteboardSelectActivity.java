package com.scentric.android.definit.input;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;

import com.scentric.android.definit.R;

import java.util.Locale;

/**
 * Created by stevecen on 9/27/18.
 *
 * Called when user copies something to the pasteboard. Allows user to save this group of words
 * as context/tag, and pick words to define
 */

public class PasteboardSelectActivity extends AppCompatActivity {
    public static final String SENT_WORD = "sent_word";

    FrameLayout frame;

    public static final int TOUCH_OUTSIDE = 1; // for out-of-window clicks
    public static final int TOUCH_SEND = 2;
    public static final int TOUCH_FRAME = 3;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_pasteboardselect);

        frame = (FrameLayout) findViewById(R.id.frame);
        if (frame != null) {
//            frame.setBackgroundColor(Color.TRANSPARENT);
            frame.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Log.e("frame", "clicked");
                    touchHandler(TOUCH_FRAME);
                }
            });
        }

//        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
//        if (toolbar != null) {
//            Log.e("tool", "Setting support toolbar...");
//            setSupportActionBar(toolbar);
//            if (getSupportActionBar() != null) {
//                getSupportActionBar().setDisplayShowTitleEnabled(false);
//                getSupportActionBar().setDisplayHomeAsUpEnabled(false);
//            }
//            toolbar.setTitle(null);
//        }
    }

    // deals with logic relate to user touches in different areas of the screen, including within the frame
    // and outside, in attempt in quit
    public void touchHandler(int source) {
        if (source == TOUCH_SEND) {
//            Log.e("touch", "touched sent " + String.format(Locale.US, "%d, %d", fab.getWidth(), fab.getHeight()));
//
//            recyclerAdapter.animateSlidesAndInsertUserVocab();
//
//            endingActivity = true; // disable clicks
//            final Handler handler = new Handler();
//            handler.postDelayed(new Runnable() {
//                @Override
//                public void run() {
//                    finish();
//                }
//            }, REMOVE_DURATION + 50);


        } else if (source == TOUCH_OUTSIDE) {
            Log.e("touch", "2 touching outside");
            finish();
        } else if (source == TOUCH_FRAME) {
            Log.e("touch", "2 frame");
            finish();
        }

    }
}
