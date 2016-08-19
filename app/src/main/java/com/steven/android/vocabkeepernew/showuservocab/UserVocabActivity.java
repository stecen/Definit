package com.steven.android.vocabkeepernew.showuservocab;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.speech.RecognizerIntent;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.NotificationCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.steven.android.vocabkeepernew.R;
import com.steven.android.vocabkeepernew.get.sqlite.DictionaryDatabaseHelper;
import com.steven.android.vocabkeepernew.input.ClipboardInputService;
import com.steven.android.vocabkeepernew.input.ClipboardWatcherService;
import com.steven.android.vocabkeepernew.input.TypeWordPopupActivity;
import com.steven.android.vocabkeepernew.show.RecyclerViewClickListener;
import com.steven.android.vocabkeepernew.showuservocab.sqlite.UserVocab;
import com.steven.android.vocabkeepernew.showuservocab.sqlite.UserVocabHelper;
import com.steven.android.vocabkeepernew.utility.DividerItemDecoration;

import java.io.File;
import java.util.ArrayList;
import java.util.Locale;


public class UserVocabActivity extends AppCompatActivity implements RecyclerViewClickListener {
    TextView serviceText;
//    ListView wordList;
    Button startButton, stopButton;
    TextView animationText;

    RecyclerView recyclerView;
    UserVocabAdapter adapter;

    WordDisplayCursorAdapter wordDisplayCursorAdapter;

    UserVocabHelper helper;

    private DictionaryDatabaseHelper dictionaryDatabase;

    public static final int NOTIF_ID = 101; // for vocab tracking

    public static final String KEY_TASK = "keyTask";
    public static final String IS_BACK = "isBack";

    public static final String PREF_KEY_WINDOW_ASKED = "prefKeyWindow";
    public static final int PREF_YES = 1;
    public static final int PREF_NO = 0;

    DividerItemDecoration dividerItemDecoration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_vocab);

        Intent intent = getIntent();
        if (intent != null) {
            String intentString = intent.getStringExtra(KEY_TASK);
            if (intentString != null) {
                Toast.makeText(this, "Intent received for " + intentString, Toast.LENGTH_SHORT).show();
                if (intentString.equals(IS_BACK)) {
                    moveTaskToBack(true);
                }
            }
        }


        recyclerView = (RecyclerView) findViewById(R.id.user_vocab_recycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));

//        startButton = (Button) findViewById(R.id.startService);
//        stopButton = (Button) findViewById(R.id.stopService);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null) {
            Log.e("tool", "Setting support toolbar...");
            setSupportActionBar(toolbar);
        }

//        serviceText = (TextView) findViewById(R.id.service_text);
//        wordList = (ListView) findViewById(R.id.words_listview);

//        startButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Toast.makeText(getApplicationContext(), "You clicked on start Button", Toast.LENGTH_SHORT).show();
//                startService(new Intent(getBaseContext(), ClipboardWatcherService.class));
//            }
//        });
//
//        stopButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                stopService(new Intent(getBaseContext(), ClipboardWatcherService.class));
//            }
//        });


        dividerItemDecoration = new DividerItemDecoration(this);

        helper = UserVocabHelper.getInstance(this);

//        PearsonAnswer.DefinitionExamples testDef = new PearsonAnswer.DefinitionExamples();
//        testDef.wordForm = "fly";
//        testDef.definition = "when you go up";
//        helper.addWord((new UserVocab(testDef.wordForm, testDef, System.currentTimeMillis(), "August 11th")));

        ArrayList<UserVocab> userVocabList = helper.getAllUserVocab();

        Log.e("userVocab", "" + userVocabList.size());
//        for (int i =0; i < userVocabList.size(); i++) {
//            Log.e("userVocab", userVocabList.get(i).word);
//        }
        adapter = new UserVocabAdapter(userVocabList, this, getApplicationContext());
        recyclerView.addItemDecoration(dividerItemDecoration);
        recyclerView.setAdapter(/*new SlideInLeftAnimationAdapter(*/adapter/*)*/);
//        Log.e("adapter count",""+ adapter.getItemCount());


//        Toast.makeText(this, "onCreate is being called", Toast.LENGTH_SHORT).show();
        createConvenienceNotif();

        askWindowPermission();

//        animationText = (TextView) findViewById(R.id.animation_text);
    }

    public void refreshRecycler () {
        // todo variable to keep track if there are changes so this activity doesnt have to keep reloading the entire sqlite
        helper = UserVocabHelper.getInstance(getApplicationContext());
        ArrayList<UserVocab> userVocabList = helper.getAllUserVocab();
        Log.e("userVocab", "" + userVocabList.size());
                Log.e("adapter count",""+ adapter.getItemCount());
        adapter.replaceData(userVocabList);
        adapter.notifyDataSetChanged();
    }

    public void recyclerViewListClicked(View v, int position) {
        String userVocabString = (new Gson()).toJson(adapter.sortedDataSet.get(position));
        Log.e("userVocab", "clicked " + position +". " + userVocabString);

        Intent detailIntent = new Intent(this, UserDetailsActivity.class);
        detailIntent.putExtra(UserDetailsActivity.KEY_JSON, userVocabString);
        startActivity(detailIntent);
    }

    @Override
    protected void onResume() {
        refreshRecycler();
        super.onResume();
//        StringBuilder sb = new StringBuilder();
//        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
//        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
//            sb.append(service.service.getClassName()).append("\n");
////            if (service.service.getClassName().contains("VocabService")) {
////                serviceText.setText(service.service.getClassName());
////                break;
////            } else {
////                serviceText.setText("Nope");
////            }
//        }
//        serviceText.setText(sb.toString());
    }

    public void askWindowPermission() {
        SharedPreferences sharedPreferences = getPreferences(MODE_PRIVATE);
        int userWindowPermission = sharedPreferences.getInt(PREF_KEY_WINDOW_ASKED, PREF_NO);
        if(userWindowPermission == PREF_NO  && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) { // ask permission only the first time
            // Show alert dialog to the user saying a separate permission is needed
            // Launch the settings activity if the user prefers
            Intent myIntent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
            startActivity(myIntent);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putInt(PREF_KEY_WINDOW_ASKED, PREF_YES);
            editor.apply();
        }
    }

    public void createConvenienceNotif() {
        NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        //// Intents for stopping the notification
//        Intent stopIntent = new Intent();
//        stopIntent.putExtra("Pls", "stahp");
//        PendingIntent stopPendingIntent = PendingIntent.getService(this, 1, stopIntent, PendingIntent.FLAG_ONE_SHOT);
//        Notification.Action stopAction =new Notification.Action(android.R.drawable.ic_media_pause, "Stop", stopPendingIntent);

        //todo: figure out why this thing still shows up in the backstack
        Intent typeWordIntent = new Intent(getApplicationContext(), TypeWordPopupActivity.class);
//        typeWordIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK /*| Intent.FLAG_ACTIVITY_NO_HISTORY*/);
        typeWordIntent.putExtra(TypeWordPopupActivity.KEY_RECOG_NOW, TypeWordPopupActivity.NO);
        PendingIntent typeWordPendingIntent = PendingIntent.getActivity(this, 0, typeWordIntent, PendingIntent.FLAG_ONE_SHOT);
//        Notification.Action stopAction =new Notification.Action(android.R.drawable.arrow_up_float, "Custom", stopPendingIntent);

        Intent pasteTypeWordIntent = new Intent(getApplicationContext(), ClipboardInputService.class);
        pasteTypeWordIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP); //todo: revise flags
        PendingIntent pendingPasteInt = PendingIntent.getService(this, 1, pasteTypeWordIntent, PendingIntent.FLAG_ONE_SHOT);
        int pasteIconInt;
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) { // because the background on kitkat notifications is black, use white icons
            pasteIconInt = R.drawable.ic_content_paste_white_24dp;
        } else {
            pasteIconInt = R.drawable.ic_content_paste_black_24dp;
        }
        NotificationCompat.Action pasteAction = new NotificationCompat.Action.Builder(pasteIconInt, "Pasteboard", pendingPasteInt)
                .build();

        Intent speechIntent = new Intent(getApplicationContext(), TypeWordPopupActivity.class);
//        speechIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); //todo: revise flags
        speechIntent.putExtra(TypeWordPopupActivity.KEY_RECOG_NOW, TypeWordPopupActivity.YES);
        speechIntent.setFlags(/*Intent.FLAG_ACTIVITY_NEW_TASK | */Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingSpeechIntent = PendingIntent.getActivity(this, 2, speechIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        int speechIconInt;
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) { // because the background on kitkat notifications is black, use white icons
            speechIconInt = R.drawable.ic_mic_white_24dp;
        } else {
            speechIconInt = R.drawable.ic_mic_black_24dp;
        }
        NotificationCompat.Action speechAction = new NotificationCompat.Action.Builder(speechIconInt, "Speech", pendingSpeechIntent)
                .build();



        Notification n = new NotificationCompat.Builder(this)
                .setContentTitle("Define a word")
                .setSubText("Definit")
                .setAutoCancel(false)
                .addAction(pasteAction)
                .addAction(speechAction)
//                .addAction(android.R.drawable.arrow_up_float, "Custom", typeWordPendingIntent) // use stop action
                .setContentIntent(typeWordPendingIntent) // use add pending intent
                .setSmallIcon(R.drawable.definit_icon_bs)
                .setPriority(Notification.PRIORITY_LOW)
                .build();
//        n.flags |= Notification.FLAG_NO_CLEAR; // sticky todo: un sticky this

        nm.notify(NOTIF_ID, n);
    }

    public void startTrackingFromButton(View view) {
//        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){ //permission //todo: only first time
//            // Show alert dialog to the user saying a separate permission is needed
//            // Launch the settings activity if the user prefers
//            Intent myIntent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
//            startActivity(myIntent);
//        }
//        try {
//            Thread.sleep(5000);
//        }catch(Exception e){
//
//        }

//        Calendar c = Calendar.getInstance();
//        Log.e("time","Current time =&gt; "+c.getTime());
//
//        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss a", Locale.US);
//        String formattedDate = df.format(c.getTime());
//            // Now formattedDate have current date/time
//        Toast.makeText(this, formattedDate, Toast.LENGTH_SHORT).show();




        if (true) { // copy paste service
            Toast.makeText(getApplicationContext(), "You clicked on start Button", Toast.LENGTH_SHORT).show();
                startService(new Intent(getBaseContext(), ClipboardWatcherService.class));

        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_word_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
           // region animation test
//            Log.e("build", Build.VERSION.SDK_INT +  " ? " + Build.VERSION_CODES.LOLLIPOP);
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//                // set transitions...
//                Intent intent = new Intent(this, UserDetailsActivity.class);
////                intent.putExtra(UserDetailsActivity.KEY_WORD, )
//                ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(this, (View) animationText, "word");
//                startActivity(intent, options.toBundle());
//            }

            return true;
        } else if (id == R.id.drop_table) {
            try {

                UserVocabHelper helper = UserVocabHelper.getInstance(getApplicationContext());
                helper.deleteAllUserVocab();

                adapter.replaceData(helper.getAllUserVocab());
                adapter.notifyDataSetChanged();
//                SQLiteDatabase vocabDB = openOrCreateDatabase("vocab.db", MODE_PRIVATE, null);
//                Toast.makeText(this, "Resetting table...", Toast.LENGTH_SHORT).show();
//                vocabDB.execSQL("DELETE FROM words;");
//
//                String query = "SELECT * FROM words;"; // get an empty cursor
//                Cursor emptyCursor= vocabDB.rawQuery(query, null);
//                emptyCursor.moveToFirst();
//
//                wordDisplayCursorAdapter.swapCursor(emptyCursor);
            }catch (Exception e) {
                Log.d("lol", e.toString());
            }
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

    }

    private static boolean doesDatabaseExist(Context context, String dbName) {
        File dbFile = context.getDatabasePath(dbName);
        return dbFile.exists();
    }




    //////////////////////

    public final static int REQ_CODE_SPEECH_INPUT = 92;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Log.e("lspeech", "onActivityResult...");

        switch (requestCode) {
            case REQ_CODE_SPEECH_INPUT: {
                if (resultCode == RESULT_OK && null != data) {

                    ArrayList<String> result = data
                            .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    Log.e("lspeech", result.get(0));
                }
                break;
            }

        }
    }

    public void recognizeSpeech(View v) {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Say something!");
        try {
            startActivityForResult(intent, REQ_CODE_SPEECH_INPUT);
        } catch (ActivityNotFoundException a) {
            Toast.makeText(getApplicationContext(), "Oops. Speech recognition is not supported on this device.",
                    Toast.LENGTH_SHORT).show();
        }

    }

}
