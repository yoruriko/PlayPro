package com.ricogao.playpro.util;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by ricogao on 2017/4/5.
 */

public class SharedPreferencesUtil {

    private Context context;
    private String spName = "PlayProSharedPreferences";


    private SharedPreferences sp;

    private String tokenName = "user_name";

    public SharedPreferencesUtil(Context context) {
        this.context = context;
        sp = context.getSharedPreferences(spName, Context.MODE_PRIVATE);
    }

    private void saveUserName(String name) {
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(tokenName, name);
        editor.apply();
    }

    public String getUsername() {
        return sp.getString(tokenName, "");
    }
}
