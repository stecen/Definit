package com.steven.android.vocabkeepernew.utility;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.content.res.Resources;
import android.os.Build;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;
import android.view.animation.OvershootInterpolator;
import android.view.animation.ScaleAnimation;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.steven.android.vocabkeepernew.R;

/**
 * Created by Steven on 8/15/2016.
 */
public class ViewUtility {
    public static final int CIRCLE_ANIM_DURATION = 100;

    public static void circleReveal(View view) {
        // previously invisible view
        final View myView = view;

//        myView.setVisibility(View.VISIBLE);

        Log.e("fab", "showing");

        // create the animator for this view (the start radius is zero)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

            myView.setVisibility(View.VISIBLE);

            // get the center for the clipping circle
            int cx = myView.getMeasuredWidth() / 2;
            int cy = myView.getMeasuredHeight() / 2;

            // get the final radius for the clipping circle
            int finalRadius = Math.max(myView.getWidth(), myView.getHeight()) / 2;
            Animator anim =
                    ViewAnimationUtils.createCircularReveal(myView, cx, cy, 0, finalRadius);

            anim.setDuration(CIRCLE_ANIM_DURATION);


            anim.start();
        } else {
        //    // make the view visible and start the animation
            myView.setVisibility(View.VISIBLE);
        }
    }

    public static void zoomIntoView(View view) {
        ScaleAnimation anim = new ScaleAnimation(0,1,0,1/*,    50, 50*/);
//        new ScaleAnimation()
        anim.setFillBefore(true);
        anim.setFillAfter(true);
        anim.setFillEnabled(true);
        anim.setDuration(300);
        anim.setInterpolator(new OvershootInterpolator());
        view.startAnimation(anim);
    }

    public static void zoomOut(View view) {
        ScaleAnimation anim = new ScaleAnimation(1,0,1,0/*,    50, 50*/);
//        new ScaleAnimation()
        anim.setFillBefore(true);
        anim.setFillAfter(true);
        anim.setFillEnabled(true);
        anim.setDuration(100);
        anim.setInterpolator(new OvershootInterpolator());
        view.startAnimation(anim);
    }



    public static void circleExit(View view) {
        circleExit(view, CIRCLE_ANIM_DURATION);
    }

    public static void circleExit(View view, int duration) {
        // previously visible view
        final View myView = view;

        Log.e("fab", "hiding");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

            // get the center for the clipping circle
            int cx = myView.getMeasuredWidth() / 2;
            int cy = myView.getMeasuredHeight() / 2;

            // get the initial radius for the clipping circle
            int initialRadius = myView.getWidth() / 2;

            // create the animation (the final radius is zero)
            Animator anim =
                    ViewAnimationUtils.createCircularReveal(myView, cx, cy, initialRadius, 0);

            anim.setDuration(duration);

            // make the view invisible when the animation is done
            anim.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    myView.setVisibility(View.GONE);
                }
            });

            // start the animation
            anim.start();
        } else {
            myView.setVisibility(View.GONE);
        }
    }

    public static void circleRevealExtra(View view) {
        // previously invisible view
        final View myView = view;

//        myView.setVisibility(View.VISIBLE);

        Log.e("fab", "showing");

        // create the animator for this view (the start radius is zero)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

            myView.setVisibility(View.VISIBLE);

            // get the center for the clipping circle
            int cx = myView.getMeasuredWidth() / 2;
            int cy = myView.getMeasuredHeight() / 2;

            // get the final radius for the clipping circle
            int finalRadius = Math.max(myView.getWidth(), myView.getHeight()) / 2;
            Animator anim =
                    ViewAnimationUtils.createCircularReveal(myView, cx, cy, 0, finalRadius + 500);


            anim.setDuration(CIRCLE_ANIM_DURATION + 500);


            anim.start();
        } else {
            //    // make the view visible and start the animation
            myView.setVisibility(View.VISIBLE);
        }
    }

    public static void circleRevealExtraFast(View view) {
        // previously invisible view
        final View myView = view;

//        myView.setVisibility(View.VISIBLE);

        Log.e("fab", "showing");

        // create the animator for this view (the start radius is zero)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

            myView.setVisibility(View.VISIBLE);

            // get the center for the clipping circle
            int cx = myView.getMeasuredWidth() / 2;
            int cy = myView.getMeasuredHeight() / 2;

            // get the final radius for the clipping circle
            int finalRadius = Math.max(myView.getWidth(), myView.getHeight()) / 2;
            Animator anim =
                    ViewAnimationUtils.createCircularReveal(myView, cx, cy / 2, 0, finalRadius + 500);


            anim.setDuration(CIRCLE_ANIM_DURATION + 200);


            anim.start();
        } else {
            //    // make the view visible and start the animation
            myView.setVisibility(View.VISIBLE);
        }
    }


    public static View setMarginsRelative(float l, float t, float r, float b, View textView, Context context) {
        RelativeLayout.LayoutParams llp = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        llp.setMargins(Math.round(convertDpToPixel(l, context)),
                Math.round(convertDpToPixel(t, context)),
                Math.round(convertDpToPixel(r, context)),
                Math.round(convertDpToPixel(b, context)));
        textView.setLayoutParams(llp);

        return textView;
    }

    public static View setMarginsLinear(float l, float t, float r, float b, View textView, Context context) {
        LinearLayout.LayoutParams llp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
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
