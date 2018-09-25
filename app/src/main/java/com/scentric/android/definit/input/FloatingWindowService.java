package com.scentric.android.definit.input;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.scentric.android.definit.R;
import com.scentric.android.definit.show.SearchAndShowActivity;
import com.scentric.android.definit.utility.ViewUtility;

/**
 * Created by Steven on 8/7/2016.
 *
 * Defines the Messenger-like poup window
 */
public class FloatingWindowService extends Service {
    public static final String LOG_FLOATINGWINDOW = "floating";

    public static final String KEY_WORD = "keyWord";

    private WindowManager windowManager;
    private LinearLayout linearLayout;

    boolean isKilled = false; // prevent this service from killing its windows when the windows are already killed

    String word; // word receive from ClipboardService

    @Override
    public IBinder onBind(Intent intent) {

        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        if (intent != null) {
            word = intent.getStringExtra(KEY_WORD);
        } else {
//            Toast.makeText(this, "Error...", Toast.LENGTH_SHORT).show();
        }
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onCreate() {
        super.onCreate();


        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        Display display = windowManager.getDefaultDisplay();
        Point size = new Point(); // for positioning
        display.getSize(size);
        int screenWidth = size.x;
        int screenHeight = size.y;

        Log.e(LOG_FLOATINGWINDOW, "width: " + screenWidth + ", height: " + screenHeight);


        linearLayout = new LinearLayout(this);

        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        linearLayout.setBackgroundColor(Color.argb(0, 200, 200, 200));
        linearLayout.setLayoutParams(layoutParams);

        //display the app icon
        LayoutInflater layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//        ImageView icon = (ImageView) layoutInflater.inflate(R.layout.popup_icon, null);
        ImageView icon = new ImageView(this);
        icon.setImageResource(R.drawable.definit_icon);

        icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isKilled = true;
//                super.onClick(view);
                Intent displayDefIntent = new Intent(getApplicationContext(), SearchAndShowActivity.class);
                displayDefIntent.putExtra(SearchAndShowActivity.SENT_WORD, word);
                displayDefIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(displayDefIntent);

                final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        windowManager.removeView(linearLayout);
                        stopSelf();
                    }
                }, 200);

            }
        });
        icon.setVisibility(View.INVISIBLE);
        linearLayout.addView(icon);

        final ImageView fIcon = icon;
        icon.post(new Runnable() {
            @Override
            public void run() {
//                ViewUtility.circleReveal(fIcon);
                ViewUtility.zoomIntoView(fIcon);
            }
        });


        final WindowManager.LayoutParams windowParams = new WindowManager.LayoutParams(150, 150, WindowManager.LayoutParams.TYPE_PHONE, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, PixelFormat.TRANSLUCENT);
        windowParams.x = 0;
        windowParams.y = /*900*/Math.round(ViewUtility.convertDpToPixel(320f, getApplicationContext()));
        windowParams.gravity = Gravity.END | Gravity.BOTTOM;

        windowManager.addView(linearLayout, windowParams);

        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!isKilled) {
                    ViewUtility.circleExit(linearLayout);
                    stopSelf();
                }
            }
        }, 8000);
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!isKilled) {
                    windowManager.removeView(linearLayout);
                    stopSelf();
                }
            }
        }, 8000 + 100);

        try {
            Drawable drawable = getPackageManager().getApplicationIcon("com.steven.android.vocabkeepernew");
            icon.setImageDrawable(drawable);
        } catch (Exception e) {
            Log.e("floating", e.toString());
        }
    }
}
