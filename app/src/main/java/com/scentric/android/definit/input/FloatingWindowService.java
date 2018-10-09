package com.scentric.android.definit.input;

import android.app.Service;
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
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.scentric.android.definit.R;
import com.scentric.android.definit.showdefinition.SearchAndShowActivity;
import com.scentric.android.definit.utility.ViewUtility;

/**
 * Created by Steven on 8/7/2016.
 *
 * Defines the Messenger-like popup window
 */
public class FloatingWindowService extends Service {
    public static final String LOG_FLOATINGWINDOW = "floating";

    public static final String KEY_WORD = "keyWord";

    private WindowManager windowManager;
    private LinearLayout linearLayout;

    private boolean isKilled = false; // prevent this service from killing its windows when the windows are already killed

    private String copiedText; // copiedText receive from ClipboardService

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        if (intent != null) {
            copiedText = intent.getStringExtra(KEY_WORD);
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
        final Display display = windowManager.getDefaultDisplay();
        Point size = new Point(); // for positioning
        display.getSize(size);
        int screenWidth = size.x;
        int screenHeight = size.y;

        Log.e(LOG_FLOATINGWINDOW, "width: " + screenWidth + ", height: " + screenHeight);


        linearLayout = new LinearLayout(this);

        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        linearLayout.setBackgroundColor(Color.argb(0, 200, 200, 200));
        linearLayout.setLayoutParams(layoutParams);

        // display the app icon
        ImageView icon = new ImageView(this);
        icon.setImageResource(R.drawable.definit_icon);

        // direct user to definition if clicked on
        icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isKilled = true;

//                Intent displayDefIntent = new Intent(getApplicationContext(), SearchAndShowActivity.class);
//                displayDefIntent.putExtra(SearchAndShowActivity.SENT_TEXT, copiedText);
//                displayDefIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
//                startActivity(displayDefIntent);

//                // todo: if single copiedText, send to normal track
                // determine if the copied text is a word (no context selected) or a word in context
                if (copiedText.trim().split("\\s+").length > 1) {
                    Intent displayDefIntent = new Intent(getApplicationContext(), PasteboardSelectActivity.class);
                    displayDefIntent.putExtra(SearchAndShowActivity.SENT_TEXT, copiedText.toLowerCase());
//                displayDefIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(displayDefIntent);
                } else { // go straight to defining the word, rather than letting the user choose a word to define
                    Intent displayDefIntent = new Intent(getApplicationContext(), SearchAndShowActivity.class);
                    displayDefIntent.putExtra(SearchAndShowActivity.SENT_TEXT, copiedText.toLowerCase());
//                    displayDefIntent.s
//                    displayDefIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(displayDefIntent);
                }

                // disappear!
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

        // creation animation
        final ImageView fIcon = icon;
        icon.post(new Runnable() {
            @Override
            public void run() {
                ViewUtility.zoomIntoView(fIcon);
            }
        });


        final WindowManager.LayoutParams windowParams = new WindowManager.LayoutParams(200, 200, WindowManager.LayoutParams.TYPE_PHONE, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, PixelFormat.TRANSLUCENT);
        windowParams.x = 0;
        windowParams.y = Math.round(ViewUtility.convertDpToPixel(320f, getApplicationContext()));
        windowParams.gravity = Gravity.END | Gravity.BOTTOM;

        windowManager.addView(linearLayout, windowParams);

        // remove self after 8 seconds
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
