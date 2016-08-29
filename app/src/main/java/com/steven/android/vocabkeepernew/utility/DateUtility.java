package com.steven.android.vocabkeepernew.utility;

import android.text.format.DateFormat;
import android.util.Log;

/**
 * Created by Steven on 8/23/2016.
 */
public class DateUtility {
    public static String[]
        monthAbbr = {"",
            "Jan",
            "Feb",
            "Mar",
            "Apr",
            "May",
            "Jun",
            "Jul",
            "Aug",
            "Sep",
            "Oct",
            "Nov",
            "Dec"};

    public static String[]
            month = {"",
            "January",
            "February",
            "March",
            "April",
            "May",
            "June",
            "July",
            "August",
            "September",
            "October",
            "November",
            "December"};

    public static String getHumanifiedDate(long millis, String dateFormat) {
        String formatted = DateFormat.format(dateFormat, millis).toString();
        String[] splitted = formatted.split("/");
        Log.e("splitted", ""+splitted.length);
        if (splitted.length == 2) { // make sure
            int monthInt = Integer.parseInt(splitted[0]);
            Log.e("splitted", "month: " + monthInt + ", of " + monthAbbr[monthInt]);
            return (monthAbbr[monthInt] + " " + splitted[1]);
        } else {
            return formatted;
        }
    }

    public static String getFullDate(long millis, String dateFormat) {
        String formatted = DateFormat.format(dateFormat, millis).toString();
        String[] splitted = formatted.split("/");
        Log.e("splitted", ""+splitted.length);
        if (splitted.length == 2) { // make sure
            int monthInt = Integer.parseInt(splitted[0]);
            Log.e("splitted", "month: " + monthInt + ", of " + month[monthInt]);
            return (month[monthInt] + " " + splitted[1]);
        } else {
            return formatted;
        }
    }
}
