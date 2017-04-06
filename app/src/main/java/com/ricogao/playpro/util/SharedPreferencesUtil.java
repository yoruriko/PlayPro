package com.ricogao.playpro.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;

/**
 * Created by ricogao on 2017/4/5.
 */

public class SharedPreferencesUtil {

    private Context context;
    private String spName = "PlayProSharedPreferences";


    private SharedPreferences sp;

    private String tokenName = "user_name";
    private String uriProfileImg = "user_profile_img";
    private String userWeight = "user_weight";
    private String userPosition = "user_position";

    public SharedPreferencesUtil(Context context) {
        this.context = context;
        sp = context.getSharedPreferences(spName, Context.MODE_PRIVATE);
    }

    public void saveUserName(String name) {
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(tokenName, name);
        editor.apply();
    }

    public String getUsername() {
        return sp.getString(tokenName, "User");
    }

    public void saveProfileImageUri(Uri uri) {
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(uriProfileImg, uri.toString());
        editor.apply();
    }

    public Uri getProfileImageUri() {
        return Uri.parse(sp.getString(uriProfileImg, ""));
    }

    public void saveUserWeight(float weight) {
        SharedPreferences.Editor editor = sp.edit();
        editor.putFloat(userWeight, weight);
        editor.apply();
    }

    public float getUserWeight() {
        return sp.getFloat(userWeight, 50f);
    }

    public void saveUserPosition(int positionCode) {
        SharedPreferences.Editor editor = sp.edit();
        editor.putInt(userPosition, positionCode);
        editor.apply();
    }

    public Position getPosition() {
        return Position.getPositionFromCode(sp.getInt(userPosition, 0));
    }
}
