package com.scentric.android.definit.utility;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.RemoteInput;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.v7.app.NotificationCompat;

import com.scentric.android.definit.R;
import com.scentric.android.definit.input.RelaySpeechActivity;
import com.scentric.android.definit.showdefinition.SearchAndShowActivity;

/**
 * Created by Steven on 8/29/2016.
 */
public class NotificationUtility {
    public static final int NOTIF_ID = 101; // for vocab tracking

    public static void createConvenienceNotif(Context context) { //centralized notification manager
        NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        // Intents for stopping the notification

        // todo: figure out why this thing still shows up in the backstack
        Intent typeWordIntent = new Intent(context.getApplicationContext(), SearchAndShowActivity.class);
        typeWordIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK /*| Intent.FLAG_ACTIVITY_NO_HISTORY*/);
        PendingIntent typeWordPendingIntent = PendingIntent.getActivity(context, 0, typeWordIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        // quick reply (Nougat+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) { // TODO: CENTRALIZE WITH NOTIFICATION CREATION IN SEARCHANDSHOWACTIVITY
            Notification.Builder builder = new Notification.Builder(context)
                    .setContentTitle("Define a word...")
                    .setSubText("Definit")
                    .setAutoCancel(false)
//                .addAction(pasteAction)
//                .addAction(android.R.drawable.arrow_up_float, "Custom", typeWordPendingIntent) // use stop action
                    .setContentIntent(typeWordPendingIntent) // use add pending intent
//                    .setSmallIcon(R.drawable.definit_icon_bs)
                    .setSmallIcon(R.drawable.definit_icon)
                    .setPriority(Notification.PRIORITY_LOW);


            Intent speechIntent = new Intent(context.getApplicationContext(), RelaySpeechActivity.class);
            speechIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // todo: revise flags
            speechIntent.setFlags(/*Intent.FLAG_ACTIVITY_NEW_TASK | */Intent.FLAG_ACTIVITY_SINGLE_TOP);
            PendingIntent pendingSpeechIntent = PendingIntent.getActivity(context, 2, speechIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            int speechIconInt;
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) { // because the background on kitkat notifications is black, use white icons
                speechIconInt = R.drawable.ic_mic_white_24dp;
            } else {
                speechIconInt = R.drawable.ic_mic_black_24dp;
            }
            Notification.Action speechAction = new Notification.Action.Builder(speechIconInt, "Speech", pendingSpeechIntent)
                    .build();
            builder.addAction(speechAction);


            Intent replyIntent = new Intent(context.getApplicationContext(), SearchAndShowActivity.class);
            replyIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // todo: revise flags
//        speechIntent.putExtra(SearchAndShowActivity.KEY_RECOG_NOW, true);
            replyIntent.setFlags(/*Intent.FLAG_ACTIVITY_NEW_TASK | */Intent.FLAG_ACTIVITY_SINGLE_TOP);
            PendingIntent pendingReplyIntent = PendingIntent.getActivity(context, 0, replyIntent, PendingIntent.FLAG_UPDATE_CURRENT); // reqCode was 2

            String replyLabel = "Define inline";//getResources().getString(R.string.reply_label);

            RemoteInput remoteInput = new RemoteInput.Builder(SearchAndShowActivity.KEY_TEXT_REPLY)
                    .setLabel(replyLabel)
                    .build();

            Notification.Action replyAction =
                    new Notification.Action.Builder(R.drawable.ic_send_white_24dp,
                            "Define inline", pendingReplyIntent)
                            .addRemoteInput(remoteInput)
                            .build();

            builder.addAction(replyAction);

            Notification n = builder.build();

            n.flags = Notification.FLAG_NO_CLEAR | Notification.FLAG_ONGOING_EVENT;
            n.priority = Notification.PRIORITY_MIN;

            nm.notify(NOTIF_ID, n);

        } else {
            android.support.v4.app.NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
                    .setContentTitle("Define a word")
                    .setSubText("Definit")
                    .setAutoCancel(false)
//                .addAction(pasteAction)
//                .addAction(android.R.drawable.arrow_up_float, "Custom", typeWordPendingIntent) // use stop action
                    .setContentIntent(typeWordPendingIntent) // use add pending intent
//                    .setSmallIcon(R.drawable.definit_icon_bs)
                    .setSmallIcon(R.drawable.definit_icon)
                    .setPriority(Notification.PRIORITY_LOW);

            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT) {
                Intent speechIntent = new Intent(context.getApplicationContext(), RelaySpeechActivity.class);
                speechIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); //todo: revise flags
//        speechIntent.putExtra(SearchAndShowActivity.KEY_RECOG_NOW, true);
                speechIntent.setFlags(/*Intent.FLAG_ACTIVITY_NEW_TASK | */Intent.FLAG_ACTIVITY_SINGLE_TOP);
                PendingIntent pendingSpeechIntent = PendingIntent.getActivity(context, 2, speechIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                int speechIconInt;
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) { // because the background on kitkat notifications is black, use white icons
                    speechIconInt = R.drawable.ic_mic_white_24dp;
                } else {
                    speechIconInt = R.drawable.ic_mic_black_24dp;
                }
                NotificationCompat.Action speechAction = new NotificationCompat.Action.Builder(speechIconInt, "Speech", pendingSpeechIntent)
                        .build();
                builder.addAction(speechAction);

            }

            Notification n = builder.build();

            n.flags = Notification.FLAG_NO_CLEAR | Notification.FLAG_ONGOING_EVENT;
            n.priority = Notification.PRIORITY_MIN;

            nm.notify(NOTIF_ID, n);
        }

        // close notification bar
        Intent it = new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
        context.sendBroadcast(it);
    }

    public static void cancelConvenienceNotif(Context context) {
        NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        nm.cancel(NOTIF_ID);
    }
}
