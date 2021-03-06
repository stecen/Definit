package com.scentric.android.definit.input;

import android.animation.ValueAnimator;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.UnderlineSpan;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.scentric.android.definit.R;
import com.scentric.android.definit.showdefinition.SearchAndShowActivity;
import com.scentric.android.definit.utility.ViewUtility;

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

    private static final int BEFORE_DRAWN = -1;
    private int origFrameHeight = BEFORE_DRAWN; // value to be overwritten by actual value in onCreate
    private int frameHeightDelta = 500; // 500 is a delta -- different from the top value. Ideally it would be set my screenheight/2

    private void initializeText(TextView pasteText, String origPasteStr) {
        String pasteStr = origPasteStr;//origPasteStr.replace(" ", "  "); // pad to make lines more clickable
        pasteText.setMovementMethod(LinkMovementMethod.getInstance());
        pasteText.setText(pasteStr,
                TextView.BufferType.SPANNABLE);

        SpannableString pasteSpan = (SpannableString) pasteText.getText();
        BreakIterator tokenIterator = BreakIterator.getWordInstance(Locale.US); // finds word blocks in the textbox
        tokenIterator.setText(pasteStr);

        for (int beginIdx = tokenIterator.first(), endIdx = tokenIterator.next();
                beginIdx != pasteStr.length();
                beginIdx = endIdx, endIdx = tokenIterator.next()) {

            if (isWordStart(pasteStr.charAt(beginIdx))) {
                Log.e("paste", String.format("%d, %d -- %d", beginIdx, endIdx, pasteStr.length()));
                String clickedWord = pasteStr.substring(beginIdx, endIdx);
                pasteSpan.setSpan(getClickTokenSpan(clickedWord, origPasteStr, beginIdx), // todo: include endIdx?
                        beginIdx, endIdx, SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE);
                pasteSpan.setSpan(new UnderlineSpan(), beginIdx, endIdx, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
        }
    }

    private boolean isWordStart(char c) {
        return Character.isLetterOrDigit(c);
    }

    private ClickableSpan getClickTokenSpan(final String pasteToken, final String originalPasteStr, final int wordIdx) {
        return new ClickableSpan() {
            private String token = pasteToken; // for clarity
            private String tag = originalPasteStr;

            // Called when a user clicks on a word in the pasteboard text that is displayed
            @Override
            public void onClick(View view) {

                moveDialog(0, frameHeightDelta); // animate dialog upwards

                // Display the definition!
                Intent displayDefIntent = new Intent(getApplicationContext(), SearchAndShowActivity.class);
                displayDefIntent.putExtra(SearchAndShowActivity.SENT_TEXT, token);
                displayDefIntent.putExtra(SearchAndShowActivity.SENT_TAG, tag); // TODO: include original formatting, and include indexing to allow highlighting of tag
                displayDefIntent.putExtra(SearchAndShowActivity.SENT_WORD_IDX, wordIdx);
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

        Log.e("measuredHeight", "ONCREATE CALLED");

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null) {
            Log.e("pasteboard", "Setting support toolbar...");
            setSupportActionBar(toolbar);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayShowTitleEnabled(false);
                getSupportActionBar().setDisplayHomeAsUpEnabled(false);
            }
            toolbar.setTitle("Select word");
        }

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

            Log.e("measuredHeightStatic", "" + this.origFrameHeight);

            // set original frame height for animation reference
            frame.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            final FrameLayout finalFrame = frame;
            ViewTreeObserver vto = finalFrame.getViewTreeObserver();
            vto.addOnGlobalLayoutListener (new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    finalFrame.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    origFrameHeight = finalFrame.getMeasuredHeight();
                    Log.e("measuredHeightVto", "" + origFrameHeight);
                }
            });

            DisplayMetrics displayMetrics = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
            frameHeightDelta = 2 * displayMetrics.heightPixels / 5; // move the frame half of the screen upwards
        }

        pasteText = (TextView) findViewById(R.id.paste_text);

        initializeText(pasteText, "My serendipity depends on the ephemeral disillusionment of the set of floral arrangements.");

        // deal with copied texts

        comingIntent = getIntent();

        if (comingIntent != null && comingIntent.hasExtra(SearchAndShowActivity.SENT_TEXT)) { //  manually sent from places
            final String copiedText = comingIntent.getStringExtra(SearchAndShowActivity.SENT_TEXT).trim();

//            pasteText.setText(copiedText);

            initializeText(pasteText, copiedText);

            // todo: after viewing the text that the user copied, let them touch individual words to define
            // todo: redefine singletask, singletop -- connect the two activities together
        }



//        circle animation, to be consistent
        final ScrollView fview = (ScrollView) findViewById(R.id.pasteboard_scroll);
        final ViewTreeObserver vto = fview.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
                public void onGlobalLayout() {
                    ViewUtility.circleRevealExtra(fview, 300);
                    Log.e("vto", "circle revealing");
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                        fview.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    } else {
                        fview.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                    }
                }
        });
    }

    @Override
    protected void onNewIntent(Intent comingIntent) {
        Log.e("measuredHeight", "ONEWINTENT");
    }

    @Override
    protected void onResume() {
        Log.e("measuredHeight", "onResume called " + this.origFrameHeight);
        super.onResume(); // if not, this dialog will not move, which is okay
        frame.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        // NOTE: instead of moveDialog downwords, I just set to wrap content. viola!
//        if (this.origFrameHeight != BEFORE_DRAWN) {
//            moveDialog(frameHeightDelta, 0); // animate dialog back into place (assuming this is resuming from the showdefinition activity)
//        }
    }

    // deals with logic relate to user touches in different areas of the screen, including within the frame
    // and outside, in attempt in quit
    public void touchHandler(int source) {
        if (source == TOUCH_SEND) {
            // TODO: special activity for send touch
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
            // TODO: Beyond outside touch
//            else if (source == TOUCH_OUTSIDE) {
                Log.e("touch", "2 touching outside");
                finish();
//            }
        }

    }

    // Adjusting the height of the frame moves the dialog vertically
    // note: I do not need this to reset the frame's height, as I just reset it to wrap_content for now
    private void moveDialog(int from, int to) {
        final View finalFrame = frame;
        int testHeight = finalFrame.getMeasuredHeight();
        Log.e("measuredHeightMove", "" + testHeight);
        Log.e("measuredHeightMove", "moving from " + (this.origFrameHeight + from) + " to " + (this.origFrameHeight + to));

        ValueAnimator heightAnim = ValueAnimator.ofInt(this.origFrameHeight + from, this.origFrameHeight + to); // todo: make sure singleTop and singleTask make sense with this static variable
        heightAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                int val = (Integer) valueAnimator.getAnimatedValue();
                ViewGroup.LayoutParams layoutParams = finalFrame.getLayoutParams();
                layoutParams.height = val;
                finalFrame.setLayoutParams(layoutParams);
            }
        });
        heightAnim.setDuration(150);
        heightAnim.start();

//        ValueAnimator widthAnim = ValueAnimator.ofInt(finalFrame.getMeasuredWidth(), finalFrame.getMeasuredWidth() - 40); // todo: make sure singleTop and singleTask make sense with this static variable
//        widthAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
//            @Override
//            public void onAnimationUpdate(ValueAnimator valueAnimator) {
//                int val = (Integer) valueAnimator.getAnimatedValue();
//                ViewGroup.LayoutParams layoutParams = finalFrame.getLayoutParams();
//                layoutParams.width = val;
//                finalFrame.setLayoutParams(layoutParams);
//            }
//        });
//        widthAnim.setDuration(150);
//        widthAnim.start();
    }
}
