package com.inpen.shuffle.utility;

import android.content.Context;
import android.graphics.Typeface;

import java.util.HashMap;

/**
 * Created by Abhishek on 12/31/2016.
 */

public class FontCache {

    private static HashMap<String, Typeface> fontCache = new HashMap<>();
    private static FontCache instance = null;

    public synchronized static FontCache getInstance() {

        if (instance == null) {
            instance = new FontCache();
        }
        return instance;
    }

    public Typeface getTypeface(String fontName, Context context) {
        Typeface typeface = fontCache.get(fontName);

        if (typeface == null) {
            try {
                typeface = Typeface.createFromAsset(context.getAssets(), fontName);
            } catch (Exception e) {
                return null;
            }

            fontCache.put(fontName, typeface);
        }

        return typeface;
    }
}
