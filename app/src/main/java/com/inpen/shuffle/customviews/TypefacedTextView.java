package com.inpen.shuffle.customviews;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.TextView;

import com.inpen.shuffle.R;
import com.inpen.shuffle.utility.FontCache;

/**
 * Created by Abhishek on 12/31/2016.
 */

public class TypefacedTextView extends TextView {
    public TypefacedTextView(Context context, String fontPath) {
        super(context);


        try {
            Typeface typeface = FontCache.getInstance().getTypeface(fontPath, context);

            setTypeface(typeface);
        } catch (Exception e) {

        }
    }

    public TypefacedTextView(Context context, AttributeSet attrs) {
        super(context, attrs);

        applyFont(context, attrs);
    }

    public TypefacedTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        applyFont(context, attrs);
    }

    public TypefacedTextView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        applyFont(context, attrs);
    }

    private void applyFont(Context context, AttributeSet attrs) {
        TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.Typeface,
                0, 0);

        try {
            String fontPath = a.getString(R.styleable.Typeface_fontPath);
            Typeface typeface = FontCache.getInstance().getTypeface(fontPath, context);

            setTypeface(typeface);
        } catch (Exception e) {

        }
    }
}
