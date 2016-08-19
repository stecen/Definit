package com.steven.android.vocabkeepernew.utility;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.content.res.Resources;
import android.os.Build;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.widget.RelativeLayout;

/**
 * Created by Steven on 8/15/2016.
 */
public class ViewUtility {
    public static final int CIRCLE_ANIM_DURATION = 100;

    public static void circleReveal(View view) {
        // previously invisible view
        final View myView = view;


        // create the animator for this view (the start radius is zero)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

            // get the center for the clipping circle
            int cx = myView.getMeasuredWidth() / 2;
            int cy = myView.getMeasuredHeight() / 2;

            // get the final radius for the clipping circle
            int finalRadius = Math.max(myView.getWidth(), myView.getHeight()) / 2;
            Animator anim =
                    ViewAnimationUtils.createCircularReveal(myView, cx, cy, 0, finalRadius);

            anim.setDuration(CIRCLE_ANIM_DURATION);

            myView.setVisibility(View.VISIBLE);
            anim.start();
        } else {
            // make the view visible and start the animation
            myView.setVisibility(View.VISIBLE);
        }
    }

    public static void circleExit(View view) {
        // previously visible view
        final View myView = view;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

            // get the center for the clipping circle
            int cx = myView.getMeasuredWidth() / 2;
            int cy = myView.getMeasuredHeight() / 2;

            // get the initial radius for the clipping circle
            int initialRadius = myView.getWidth() / 2;

            // create the animation (the final radius is zero)
            Animator anim =
                    ViewAnimationUtils.createCircularReveal(myView, cx, cy, initialRadius, 0);

            anim.setDuration(CIRCLE_ANIM_DURATION);

            // make the view invisible when the animation is done
            anim.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    myView.setVisibility(View.INVISIBLE);
                }
            });

            // start the animation
            anim.start();
        } else {
            myView.setVisibility(View.INVISIBLE);
        }
    }

    public static View setMargins(float l, float t, float r, float b, View textView, Context context) {
        RelativeLayout.LayoutParams llp = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        llp.setMargins(Math.round(convertDpToPixel(l, context)),
                Math.round(convertDpToPixel(t, context)),
                Math.round(convertDpToPixel(r, context)),
                Math.round(convertDpToPixel(b, context)));
        textView.setLayoutParams(llp);

        return textView;
    }

    public static float convertDpToPixel(float dp, Context context){
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        float px = dp * ((float)metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT);
        return px;
    }

    public static float convertPixelsToDp(float px, Context context){
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        float dp = px / ((float)metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT);
        return dp;
    }
}
