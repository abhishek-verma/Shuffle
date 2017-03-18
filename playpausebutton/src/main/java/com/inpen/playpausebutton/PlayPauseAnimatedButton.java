package com.inpen.playpausebutton;

import android.content.Context;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.support.design.widget.FloatingActionButton;
import android.text.TextPaint;
import android.util.AttributeSet;

/**
 * TODO: document your custom view class.
 */
public class PlayPauseAnimatedButton extends FloatingActionButton {

    private TextPaint mTextPaint;
    private float mTextWidth;
    private float mTextHeight;

    public PlayPauseAnimatedButton(Context context) {
        super(context);
        init(null, 0);
    }

    public PlayPauseAnimatedButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public PlayPauseAnimatedButton(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs, defStyle);
    }

    private void init(AttributeSet attrs, int defStyle) {
//         Load attributes
//        final TypedArray a = getContext().obtainStyledAttributes(
//                attrs, R.styleable.PlayPauseAnimatedButton, defStyle, 0);
//        mExampleString = a.getString(
//                R.styleable.PlayPauseAnimatedButton_exampleString);
//        mExampleColor = a.getColor(
//                R.styleable.PlayPauseAnimatedButton_exampleColor,
//                mExampleColor);
//        // Use getDimensionPixelSize or getDimensionPixelOffset when dealing with
//        // values that should fall on pixel boundaries.
//        mExampleDimension = a.getDimension(
//                R.styleable.PlayPauseAnimatedButton_exampleDimension,
//                mExampleDimension);
//
//        if (a.hasValue(R.styleable.PlayPauseAnimatedButton_exampleDrawable)) {
//            mExampleDrawable = a.getDrawable(
//                    R.styleable.PlayPauseAnimatedButton_exampleDrawable);
//            mExampleDrawable.setCallback(this);
//        }

//        a.recycle();

        setImageDrawable(getResources().getDrawable(R.drawable.play_icon, null));
    }

    public void pauseToPlayAnimation() {
        AnimatedVectorDrawable pauseToPlay =
                (AnimatedVectorDrawable) getResources().getDrawable(R.drawable.pause_to_play_animated_vector, null);
        setImageDrawable(pauseToPlay);
        pauseToPlay.start();
    }

    public void playToPauseAnimation() {
        AnimatedVectorDrawable playToPause =
                (AnimatedVectorDrawable) getResources().getDrawable(R.drawable.play_to_pause_animation_vector, null);
        setImageDrawable(playToPause);
        playToPause.start();
    }

}
