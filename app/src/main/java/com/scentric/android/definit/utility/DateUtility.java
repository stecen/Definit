package com.scentric.android.definit.utility;

import android.text.format.DateFormat;
import android.util.Log;

import java.util.Calendar;

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
        Log.e("splitted", "" + splitted.length);
        if (splitted.length == 2) { // make sure
            int monthInt = Integer.parseInt(splitted[0]);
            Log.e("splitted", "month: " + monthInt + ", of " + monthAbbr[monthInt]);
            return (monthAbbr[monthInt] + " " + splitted[1]);
        } else {
            return formatted;
        }
    }

    public static String getFullDate(long millis, String dateFormat) { // todo : supply my own dateformat because i dont want to depend on the caller's format...
        String formatted = DateFormat.format(dateFormat, millis).toString();

        String todaysDate = DateFormat.format(dateFormat, System.currentTimeMillis()).toString();

        if (formatted.equals(todaysDate)) {
            return "Today";
        }

        String[] splitted = formatted.split("/");
        Log.e("splitted", "" + splitted.length);
        if (splitted.length == 2) { // make sure
            int monthInt = Integer.parseInt(splitted[0]);
            int dayInt = Integer.parseInt(splitted[1]);
            Log.e("splitted", "month: " + monthInt + ", of " + month[monthInt]);

            if (dayInt < 10) {
                splitted[1] = Integer.toString(dayInt);
            }

            if (dayInt < 10) {
                splitted[1] = Integer.toString(dayInt);
            }


            String superscript = "";
            if (dayInt != 11) {

                switch (dayInt % 10) { // first, second third, ..
                    case 1:
                        superscript = "st";
                        break;
                    case 2:
                        superscript = "nd";
                        break;
                    case 3:
                        superscript = "rd";
                        break;
                    default:
                        superscript = "th";
                        break;
                }
            } else {
                superscript = "th";
            }
            return (month[monthInt] + " " + splitted[1] + superscript);
        } else {
            return formatted;
        }
    }

    public static String getTime(long milliseconds) {

        String time = "";
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(milliseconds);

        int minute = cal.get(Calendar.MINUTE);
        String minuteStr = "" + minute;
        if (minute <= 9) {
            minuteStr = "0" + minuteStr;
        }

        int hour = cal.get(Calendar.HOUR);
        if (hour == 0) {
            hour = 12;
        }
        String hourStr = "" + hour;

        time = hourStr + ":" + minuteStr;

        if (cal.get(Calendar.AM_PM) == 0)
            time = time + " am";
        else
            time = time + " pm";

        return time;

    }
}
