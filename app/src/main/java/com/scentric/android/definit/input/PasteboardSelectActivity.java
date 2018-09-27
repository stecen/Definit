package com.scentric.android.definit.input;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.scentric.android.definit.R;
import com.scentric.android.definit.showdefinition.SearchAndShowActivity;

/**
 * Created by stevecen on 9/27/18.
 *
 * Called when user copies something to the pasteboard. Allows user to save this group of words
 * as context/tag, and pick words to define
 */

public class PasteboardSelectActivity extends AppCompatActivity {

    private FrameLayout frame;
    private TextView pasteText;

    private Intent comingIntent;

    public static final int TOUCH_OUTSIDE = 1; // for out-of-window clicks
    public static final int TOUCH_SEND = 2;
    public static final int TOUCH_FRAME = 3;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_pasteboardselect);

        frame = (FrameLayout) findViewById(R.id.frame);
        if (frame != null) {
            frame.setBackgroundColor(Color.TRANSPARENT);
            frame.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Log.e("frame", "clicked");
                    touchHandler(TOUCH_FRAME);
                }
            });
        }

        pasteText = (TextView) findViewById(R.id.paste_text);

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

        // deal with copied texts

        comingIntent = getIntent();

        Log.e("coming", "" + (comingIntent != null));
        if (comingIntent != null && comingIntent.hasExtra(SearchAndShowActivity.SENT_TEXT)) { //  manually sent from places
            final String copiedText = comingIntent.getStringExtra(SearchAndShowActivity.SENT_TEXT).trim();

            pasteText.setText(copiedText);

            // TODO RN: after viewing the text that the user copied, let them touch individual words to define
            // todo: redefine singletask, singletop -- connect the two activities together

            pasteText.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent displayDefIntent = new Intent(getApplicationContext(), SearchAndShowActivity.class);
                    displayDefIntent.putExtra(SearchAndShowActivity.SENT_TEXT, copiedText);
//                    displayDefIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(displayDefIntent);
                }
            });
        }


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


        }

    }
}
