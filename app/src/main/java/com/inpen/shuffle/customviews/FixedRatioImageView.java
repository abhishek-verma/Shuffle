package com.inpen.shuffle.customviews;

import android.content.Context;
import android.support.v7.widget.AppCompatImageView;
import android.util.AttributeSet;

/**
 * Created by Abhishek on 12/20/2016.
 */

public class FixedRatioImageView extends AppCompatImageView {
    private static float HEIGHT_TO_WIDTH_RATIO = 1; // height/width

    public FixedRatioImageView(Context context) {
        super(context);
    }

    public FixedRatioImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public FixedRatioImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setClipToOutline(true);

        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = (int) (width * HEIGHT_TO_WIDTH_RATIO);

        super.onMeasure(
                MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY));
    }
}
