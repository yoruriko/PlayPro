package com.ricogao.playpro.Util;

import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v4.content.ContextCompat;

/**
 * Created by ricogao on 2017/2/25.
 */

public abstract class PermissionUtil {

    public static boolean checkPermission(Context context, String[] permissions) {

        if (permissions.length < 1) {
            return false;
        }

        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }

        return true;
    }

    public static boolean verifyPermission(int[] result) {
        if (result.length < 1) {
            return false;
        }
        for (int r : result) {
            if (r != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }

        return true;
    }
}
