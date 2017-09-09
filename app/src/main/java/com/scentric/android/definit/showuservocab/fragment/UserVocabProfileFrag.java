package com.scentric.android.definit.showuservocab.fragment;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.Toast;

import com.google.gson.Gson;
import com.scentric.android.definit.R;
import com.scentric.android.definit.export.ImportActivity;
import com.scentric.android.definit.settings.PreferencesActivity;
import com.scentric.android.definit.showuservocab.sqlite.GetAllWordsAsyncInterface;
import com.scentric.android.definit.showuservocab.sqlite.UserVocab;
import com.scentric.android.definit.showuservocab.sqlite.UserVocabHelper;
import com.scentric.android.definit.utility.PearsonAnswer;

import java.util.ArrayList;

/**
 * Created by Steven on 8/30/2016.
 */
public class UserVocabProfileFrag extends Fragment implements FragmentRefresher, FragmentReselected {
    Context appContext, actContext;
    LinearLayout settingsLinear, accountLinear, ixLinear, fdbkLinear;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        appContext = getActivity().getApplicationContext();

    }

    //Overriden method onCreateView
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        //Returning the layout file after inflating
        //Change R.layout.tab1 in you classes
        return inflater.inflate(R.layout.fragment_uservocab_profile, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        actContext = getActivity();
//        // recycler stuff
//        recyclerView = (RecyclerView) getView().findViewById(R.id.user_vocab_recycler);
        if (getView() != null) {
            accountLinear = (LinearLayout) getView().findViewById(R.id.account_linear);
            settingsLinear = (LinearLayout) getView().findViewById(R.id.settings_linear);
            ixLinear = (LinearLayout) getView().findViewById(R.id.import_export_linear);
            fdbkLinear = (LinearLayout) getView().findViewById(R.id.import_feedback_linear);

            accountLinear.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
//                    Intent intent = new Intent(appContext, LoginActivity.class);
//                    startActivity(intent);
                    Toast.makeText(appContext, "Coming soon!", Toast.LENGTH_SHORT).show();
                }
            });
            settingsLinear.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(appContext, PreferencesActivity.class);
                    startActivity(intent);
                }
            });
            fdbkLinear.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
                    emailIntent.setData(Uri.parse("mailto: centricapps@gmail.com"));
                    startActivity(Intent.createChooser(emailIntent, "Send feedback, suggestions, and bug reports"));
                }
            });
            ixLinear.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
//                    Intent intent = new Intent(appContext, QuizletExportActivity.class);
//                    startActivity(intent);
//                    sendNativeExport();
                    PopupMenu popupMenu = new PopupMenu((actContext != null) ? actContext : appContext, ixLinear);
                    popupMenu.getMenuInflater().inflate(R.menu.menu_export_popup, popupMenu.getMenu());

                    popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            Log.e("popup", "you clicked " + item.getTitle() + " " + item.getItemId());
                            switch (item.getItemId()) {
                                case R.id.popup_menu_export_native:
                                    Log.e("popup", "export native");
                                    sendNativeExport();

                                    break;
                                case R.id.popup_menu_import_native:
                                    Log.e("popup", "import native");

                                    Intent intent = new Intent(appContext, ImportActivity.class);
                                    startActivity(intent);



                                    break;
                                case R.id.popup_menu_quizlet:
                                    Log.e("popup", "export quizlet");
                                    sendQuizExport();

                                    break;
                                default:
                                    break;
                            }
                            return false;
                        }
                    });

                    popupMenu.show();

                    //todo : popupmenu
                }
            });
        }

    }

    public void sendNativeExport() {
        if (UserVocabMainFrag.dataSet != null) { // if the data set is already retrieved by the main fragment, don't do another sqlite query
            String shareStr = (new Gson()).toJson(UserVocabMainFrag.dataSet);

            Intent sharingIntent = new Intent(Intent.ACTION_SEND);
            sharingIntent.setType("text/plain");
            sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareStr);
            startActivity(sharingIntent);
        } else {
            UserVocabHelper helper = UserVocabHelper.getInstance(appContext);
            helper.getAllUserVocab(new GetAllWordsAsyncInterface() {
                @Override
                public void setWordsData(ArrayList<UserVocab> userVocabList) {
                    String shareStr = (new Gson()).toJson(UserVocabMainFrag.dataSet);

                    Intent sharingIntent = new Intent(Intent.ACTION_SEND);
                    sharingIntent.setType("text/plain");
                    sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareStr);
                    startActivity(sharingIntent);
                }
            }, UserVocabHelper.GET_ALL);
        }
    }

    public String quizletHelper(ArrayList<UserVocab> userVocabList) {
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < userVocabList.size(); i++) {
            UserVocab userVocab = userVocabList.get(i);
            sb.append(userVocab.word.trim())
                    .append("\t");


            for (int j = 0; j < userVocab.listOfDefEx.size(); j++) { // todo: add a list of examples
                StringBuilder sbDefEx = new StringBuilder();
                String defText = userVocab.listOfDefEx.get(j).definition.trim().replaceAll("\t", " ");
                if (defText.charAt(defText.length() - 1) == '.' ||
                        defText.charAt(defText.length() - 1) == '!' ||
                        defText.charAt(defText.length() - 1) == '?') {

                } else {
                    defText = defText + ".";
                }
                sbDefEx.append(defText.trim());


                if (userVocab.listOfDefEx.isEmpty() || (userVocab.listOfDefEx.get(j).examples.get(0).trim().equals(PearsonAnswer.DEFAULT_NO_EXAMPLE))) {

                } else {
                    sbDefEx.append(" \"");

                    String exText = userVocab.listOfDefEx.get(j).examples.get(0).trim().replaceAll("\t", "");
                    if (exText.charAt(exText.length() - 1) == '.' ||
                            exText.charAt(exText.length() - 1) == '!' ||
                            exText.charAt(exText.length() - 1) == '?') {

                    } else {
                        exText = exText + ".";
                    }

                    sbDefEx.append(exText);
                    sbDefEx.append("\" ");
                }

                sb.append(sbDefEx.toString()).append(" ");


            }

            sb.append("\n");


        }

        return sb.toString();
    }

    public void sendQuizExport() {
        if (UserVocabMainFrag.dataSet != null) { // if the data set is already retrieved by the main fragment, don't do another sqlite query
            String shareStr = quizletHelper(UserVocabMainFrag.dataSet);

            Intent sharingIntent = new Intent(Intent.ACTION_SEND);
            sharingIntent.setType("text/plain");
            sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareStr);
            startActivity(sharingIntent);
        } else {
            UserVocabHelper helper = UserVocabHelper.getInstance(appContext);
            helper.getAllUserVocab(new GetAllWordsAsyncInterface() {
                @Override
                public void setWordsData(ArrayList<UserVocab> userVocabList) {
                    String shareStr = quizletHelper(userVocabList);

                    Intent sharingIntent = new Intent(Intent.ACTION_SEND);
                    sharingIntent.setType("text/plain");
                    sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareStr);
                    startActivity(sharingIntent);
                }
            }, UserVocabHelper.GET_ALL);
        }
    }

    @Override
    public void reselect() {
//        if (linearLayoutManager != null) {
//            linearLayoutManager.scrollToPosition(0);
//        }
    }

    @Override
    public void onResume() {

        super.onResume();
    }

    public void refreshViews() {
        // yooo
    }


}
