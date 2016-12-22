package com.inpen.shuffle.utility;

import android.app.Application;

import com.facebook.stetho.Stetho;

/**
 * Created by Abhishek on 12/15/2016.
 */

public class StethoUtil {

    public static void init(Application application) {

        Stetho.initializeWithDefaults(application);
    }
}
