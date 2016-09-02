package com.steven.android.vocabkeepernew.showuservocab;

import android.content.Intent;
import android.graphics.drawable.LayerDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.google.gson.Gson;
import com.steven.android.vocabkeepernew.R;
import com.steven.android.vocabkeepernew.showuservocab.sqlite.UserVocab;
import com.steven.android.vocabkeepernew.utility.ViewUtility;

/**
 * Created by Steven on 8/15/2016.
 */
public class UserDetailsActivity extends AppCompatActivity {
    LinearLayout linearLayout;

    public static final String KEY_WORD = "keyForWord";
    public static final String KEY_JSON = "keyJson";

    UserVocab userVocab;

    @Override
    protected void onCreate(Bundle onSavedInstance) {
        super.onCreate(onSavedInstance);

        setContentView(R.layout.activity_userdetails);

        Intent jsonIntent = getIntent();
        if (jsonIntent != null) {
            userVocab = (new Gson()).fromJson(jsonIntent.getStringExtra(KEY_JSON), UserVocab.class);
            Log.e("details", "detailsreceived: " + jsonIntent.getStringExtra(KEY_JSON));
        }

//        final View view = findViewById(R.id.view);
//
//        ViewTreeObserver vto = view.getViewTreeObserver();
//        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
//
//            @Override
//            public void onGlobalLayout() {
////                LayerDrawable ld = (LayerDrawable)tv.getBackground();
////                ld.setLayerInset(1, 0, tv.getHeight() / 2, 0, 0);
////                ViewTreeObserver obs = tv.getViewTreeObserver();
//
//                int width = view.getMeasuredWidth();
//                int height = view.getHeight();
//                Log.e("viewc", String.format("(%d, %d)", width, height));
//                int widthdp = Math.round(ViewUtility.convertPixelsToDp(width, getApplicationContext()));
//                int heightdp = Math.round(ViewUtility.convertPixelsToDp(height, getApplicationContext()));
//                Log.e("viewc", String.format("dp (%d, %d)", widthdp, heightdp));
//
////                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
////                    obs.removeOnGlobalLayoutListener(this);
////                } else {
////                    obs.removeGlobalOnLayoutListener(this);
////                }
//            }
//
//        });
//
//        view.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(Math.round(ViewUtility.convertDpToPixel(100, getApplicationContext())),
//                        Math.round(ViewUtility.convertDpToPixel(100, getApplicationContext())));
//                view.setLayoutParams(layoutParams);
//
//                int width = view.getMeasuredWidth();
//                int height = view.getHeight();
//                Log.e("viewc", String.format("(%d, %d)", width, height));
//                int widthdp = Math.round(ViewUtility.convertPixelsToDp(width, getApplicationContext()));
//                int heightdp = Math.round(ViewUtility.convertPixelsToDp(height, getApplicationContext()));
//                Log.e("viewc", String.format("dp (%d, %d)", widthdp, heightdp));
//            }
//        });



//        linearLayout = (LinearLayout) findViewById(R.id.details_linear);
//        ImageView icon = (ImageView) getLayoutInflater().inflate(R.layout.popup_icon, null);
//
//        icon.setImageResource(R.drawable.definit_icon_bs);
//        linearLayout.addView(icon);
    }
}
