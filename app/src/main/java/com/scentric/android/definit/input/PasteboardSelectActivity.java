package com.scentric.android.definit.input;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AppCompatActivity;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.StyleSpan;
import android.text.style.UnderlineSpan;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.scentric.android.definit.R;
import com.scentric.android.definit.showdefinition.SearchAndShowActivity;

import java.text.BreakIterator;
import java.util.Locale;

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


    private void initializeText(TextView pasteText, String pasteStr) {
        pasteText.setMovementMethod(LinkMovementMethod.getInstance());
        pasteText.setText(pasteStr,
                TextView.BufferType.SPANNABLE);

        SpannableString pasteSpan = (SpannableString) pasteText.getText();
        BreakIterator tokenIterator = BreakIterator.getWordInstance(Locale.US); // finds word blocks in the textbox
        tokenIterator.setText(pasteStr);

//        int startIdx = tokenIterator.first();
        for (int beginIdx = tokenIterator.first(), endIdx = tokenIterator.next();
                endIdx != pasteStr.length();
                beginIdx = endIdx, endIdx = tokenIterator.next()) {

            if (isWordStart(pasteStr.charAt(beginIdx))) {
                Log.e("paste", String.format("%d, %d -- %d", beginIdx, endIdx, pasteStr.length()));
                Log.e("paste", String.format("%c, %c\n", pasteStr.charAt(beginIdx), pasteStr.charAt(endIdx), pasteStr.length()));
                String clickedWord = pasteStr.substring(beginIdx, endIdx);
                pasteSpan.setSpan(getClicktokenSpan(clickedWord), beginIdx, endIdx, SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE);
                pasteSpan.setSpan(new UnderlineSpan(), beginIdx, endIdx, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
        }



    }

    private boolean isWordStart(char c) {
        return Character.isLetterOrDigit(c);
    }

    private ClickableSpan getClicktokenSpan(final String pasteToken) {
        return new ClickableSpan() {
            private String token = pasteToken;

            @Override
            public void onClick(View view) {
                    Intent displayDefIntent = new Intent(getApplicationContext(), SearchAndShowActivity.class);
                    displayDefIntent.putExtra(SearchAndShowActivity.SENT_TEXT, token);
                    startActivity(displayDefIntent);
            }

            @Override
            public void updateDrawState(TextPaint ds) {
                super.updateDrawState(ds);
                ds.setUnderlineText(false);
                ds.setColor(Color.BLACK);
            }
        };
    }

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

        initializeText(pasteText, "My serendipity depends on the ephemeral disillusionment of the set of floral arrangements.");

        //region modify reference (try 2)

//        pasteText.setText("My serendipity depends on the ephemeral disillusionment of the set of floral arrangements.");
////        pasteText.setMovementMethod(LinkMovementMethod.getInstance());
//        Spannable spannable = (Spannable) pasteText.getText();
////        SpannableString ss = new SpannableString("My serendipity depends on the ephemeral disillusionment of the set of floral arrangements.");
//        ClickableSpan clickableSpan = new ClickableSpan() {
//            @Override
//            public void onClick(View textView) {
//                Log.e("spannable", "clicked:");
//                Intent displayDefIntent = new Intent(getApplicationContext(), SearchAndShowActivity.class);
//                displayDefIntent.putExtra(SearchAndShowActivity.SENT_TEXT, "My awesome serendipity depends on the ephemeral disillusionment of the set of floral arrangements.");
//                startActivity(displayDefIntent);
//            }
//            @Override
//            public void updateDrawState(TextPaint ds) {
//                super.updateDrawState(ds);
//                ds.setUnderlineText(false);
//            }
//        };
//        spannable.setSpan(clickableSpan, 0, 28, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        // endregion

        //region set text (try 1)

//        SpannableString ss = new SpannableString("My serendipity depends on the ephemeral disillusionment of the set of floral arrangements.");
//        ClickableSpan clickableSpan = new ClickableSpan() {
//            @Override
//            public void onClick(View textView) {
//                Log.e("spannable", "clicked:");
//                Intent displayDefIntent = new Intent(getApplicationContext(), SearchAndShowActivity.class);
//                displayDefIntent.putExtra(SearchAndShowActivity.SENT_TEXT, "My serendipity depends on the ephemeral disillusionment of the set of floral arrangements.");
//                startActivity(displayDefIntent);
//            }
//            @Override
//            public void updateDrawState(TextPaint ds) {
//                super.updateDrawState(ds);
//                ds.setUnderlineText(false);
//            }
//        };
//        ss.setSpan(clickableSpan, 2, 5, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
//
//        pasteText.setText(ss);
//        pasteText.setMovementMethod(LinkMovementMethod.getInstance());
//        pasteText.setHighlightColor(Color.TRANSPARENT);

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

        // endregion

        // deal with copied texts

        comingIntent = getIntent();

//        Log.e("coming", "" + (comingIntent != null));
//        if (comingIntent != null && comingIntent.hasExtra(SearchAndShowActivity.SENT_TEXT)) { //  manually sent from places
//            final String copiedText = comingIntent.getStringExtra(SearchAndShowActivity.SENT_TEXT).trim();
//
//            pasteText.setText(copiedText);
//
//            Spannable spannable = (Spannable) pasteText.getText();
//            StyleSpan boldSpan = new StyleSpan( Typeface.BOLD );
//            spannable.setSpan( boldSpan, 0, 2, Spannable.SPAN_INCLUSIVE_INCLUSIVE );
//
//            // TODO RN: after viewing the text that the user copied, let them touch individual words to define
//            // todo: redefine singletask, singletop -- connect the two activities together
//
//            pasteText.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View view) {
//                    Intent displayDefIntent = new Intent(getApplicationContext(), SearchAndShowActivity.class);
//                    displayDefIntent.putExtra(SearchAndShowActivity.SENT_TEXT, copiedText);
////                    displayDefIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
//                    startActivity(displayDefIntent);
//                }
//            });
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


        } else {
//            else if (source == TOUCH_OUTSIDE) {
                Log.e("touch", "2 touching outside");
                finish();
//            }
        }

    }
}
