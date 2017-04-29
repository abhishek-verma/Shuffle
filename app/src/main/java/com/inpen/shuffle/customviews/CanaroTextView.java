package com.inpen.shuffle.customviews;

import android.content.Context;
import android.util.AttributeSet;

/**
 * Created by Dmytro Denysenko on 5/6/15.
 */
public class CanaroTextView extends TypefacedTextView {
    public CanaroTextView(Context context) {
        this(context, null);
        init();
    }

    public CanaroTextView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
        init();
    }

    public CanaroTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        applyTypeface("fonts/canaro_extra_bold.otf");
    }

}
