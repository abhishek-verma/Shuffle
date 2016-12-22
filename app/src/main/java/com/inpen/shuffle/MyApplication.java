package com.inpen.shuffle;

import android.app.Application;

import com.inpen.shuffle.utility.StethoUtil;

/**
 * Created by Abhishek on 12/15/2016.
 */

public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        StethoUtil.init(this);
    }
}
