package id.co.froyo.froyonion.helper;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import java.util.HashMap;

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
                    IS_CHECKIN = "isCheckin";

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

}
