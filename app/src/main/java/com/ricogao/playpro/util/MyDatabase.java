package com.ricogao.playpro.util;

import com.raizlabs.android.dbflow.annotation.Database;

/**
 * Created by ricogao on 2017/2/28.
 */
@Database(name = MyDatabase.NAME, version = MyDatabase.VERSION)
public class MyDatabase {
    public static final String NAME = "PlayProDB";
    public static final int VERSION = 1;
}
