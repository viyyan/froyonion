package id.co.froyo.froyonion.helper;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.TimeZone;

import id.co.froyo.froyonion.LoginActivity;

/**
 * Created by Fian on 6/13/16.
 */

public class SessionManager {
    private Context context;
    public SharedPreferences pref;
    public SharedPreferences.Editor editor;
    public String PREF_NAME = "FroyonionApp";
    public int PRIVATE_MODE = 0;
    public String   IS_LOGIN = "isLoggedIn",
                    KEY_USERID = "userId",
                    KEY_NAME = "name",
                    KEY_EMAIL = "email",
                    KEY_TOKEN = "loginToken",
                    IS_CHECKIN = "isCheckin",
                    KEY_TIME = "time";

    public SessionManager(Context mContext) {
        this.context = mContext;
        this.pref = context.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
        this.editor = pref.edit();
    }

    /** Save new Session */
    public void saveSession(String userId, String name, String email, String token) {
        editor.putBoolean(IS_LOGIN, true);
        editor.putString(KEY_USERID, userId);
        editor.putString(KEY_NAME, name);
        editor.putString(KEY_EMAIL, email);
        editor.putString(KEY_TOKEN, token);
        editor.commit();
    }

    /** Check user is Logged in*/
    public boolean isLoggedIn() {
        return pref.getBoolean(IS_LOGIN, false);
    }

    /** Get user data */
    public HashMap<String, String> getUserData() {
        HashMap<String, String> user = new HashMap<String, String>();
        user.put(KEY_USERID, pref.getString(KEY_USERID, null));
        user.put(KEY_NAME, pref.getString(KEY_NAME, null));
        user.put(KEY_EMAIL, pref.getString(KEY_EMAIL, null));
        user.put(KEY_TOKEN, pref.getString(KEY_TOKEN, null));
        return user;
    }

    /** If Not Login will be redirected */
    public void checkLogin() {
        if(!this.isLoggedIn()){
            Intent i = new Intent(context, LoginActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            context.startActivity(i);
        }
    }

    /** Clear Session */
    public void clearSession(){
        editor.clear();
        editor.commit();
        Intent i = new Intent(context, LoginActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        context.startActivity(i);
    }

    public void checkedIn(String time){
        editor.putBoolean(IS_CHECKIN, true);
        editor.putString(KEY_TIME, time);
        editor.commit();
    }

    public boolean isCheckedIn() {
        String str_date =  pref.getString(KEY_TIME, null);
        Date currentDate = new Date();
        Calendar c = Calendar.getInstance();
        if(str_date != null) {

            SimpleDateFormat inputFormat = new SimpleDateFormat(
                    "yyyy-MM-dd HH:mm:ss");
            inputFormat.setTimeZone(TimeZone.getTimeZone("Etc/UTC"));


            Date dateCheck = null;
            try {
                dateCheck = inputFormat.parse(str_date);
            } catch (ParseException e) {
                e.printStackTrace();
            }


            c.setTime(dateCheck);
            int year = c.get(Calendar.YEAR), month = c.get(Calendar.MONTH), day = c.get(Calendar.DAY_OF_MONTH);
            String limitDate = year + "-" + (month + 1) + "-" + day + " 22:00:00";//UTC
            Date limitTime = null;
            try {
                limitTime = inputFormat.parse(limitDate);
            } catch (ParseException e) {
                e.printStackTrace();
            }

            Log.i("difference", currentDate.toString() + " - " + limitTime.toString());
            if (pref.getBoolean(IS_CHECKIN, false) && (currentDate.getTime() > limitTime.getTime())) {
                editor.putBoolean(IS_CHECKIN, false);
                editor.putString(KEY_TIME, null);
                editor.commit();
            }
        }
        return pref.getBoolean(IS_CHECKIN, false);
    }

    public void checkedOut(String createdAt) {
        editor.putBoolean(IS_CHECKIN, false);
        editor.putString(KEY_TIME, createdAt);
        editor.commit();
    }

    public String getTimeChecked() {
        String str_date =  pref.getString(KEY_TIME, null);
        String timestamp;
        if(str_date != null) {

            SimpleDateFormat inputFormat = new SimpleDateFormat(
                    "yyyy-MM-dd HH:mm:ss");
            inputFormat.setTimeZone(TimeZone.getTimeZone("Etc/UTC"));

            SimpleDateFormat outputFormat = new SimpleDateFormat("HH:mm:ss");
// Adjust locale and zone appropriately

            Date dateCheck = null;
            try {
                dateCheck = inputFormat.parse(str_date);
            } catch (ParseException e) {
                e.printStackTrace();
            }

            timestamp = outputFormat.format(dateCheck);
        } else {
            timestamp = "00:00:00";
        }
        return timestamp;
    }
}
