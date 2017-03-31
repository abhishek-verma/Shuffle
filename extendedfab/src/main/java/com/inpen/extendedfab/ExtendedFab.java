package com.inpen.extendedfab;

import android.animation.LayoutTransition;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageSwitcher;
import android.widget.LinearLayout;
import android.widget.ViewSwitcher;

import com.mikhaellopez.circularimageview.CircularImageView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Abhishek on 12/12/2016.
 */

public class ExtendedFab extends LinearLayout {

    ImageSwitcher mMainFabImageSwitcher;

    List<View> mLeftViewList = new ArrayList<>();
    List<View> mRightViewList = new ArrayList<>();

    private boolean mIsExpanded = true;

    public ExtendedFab(Context context) {
        super(context);
        init();
    }

    public ExtendedFab(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ExtendedFab(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        setLayoutProperties();
        setupImageSwitcher();
        addView(mMainFabImageSwitcher);
    }

    private void setLayoutProperties() {
        setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        //set padding
        int padding = getResources().getDimensionPixelSize(R.dimen.main_padding);
        setPadding(padding, padding, padding, padding);

        setBackground(getResources().getDrawable(R.drawable.extended_fab_bg, null));
        setElevation(getResources().getDimension(R.dimen.main_elevation));
        setGravity(Gravity.CENTER_VERTICAL);

        applyLayoutChangesAnimation();
    }


    private void applyLayoutChangesAnimation() {

        setLayoutTransition(new LayoutTransition());
        LayoutTransition layoutTransition = getLayoutTransition();
        layoutTransition.enableTransitionType(LayoutTransition.CHANGING);
    }


    private void setupImageSwitcher() {
        mMainFabImageSwitcher = new ImageSwitcher(getContext());

        int fabSize = (int) getResources().getDimension(R.dimen.fab_size);
        LinearLayout.LayoutParams layoutParams = new LayoutParams(fabSize, fabSize);
        layoutParams.gravity = Gravity.CENTER_VERTICAL;
        mMainFabImageSwitcher.setLayoutParams(layoutParams);

        mMainFabImageSwitcher.setInAnimation(getContext(), android.R.anim.fade_in);
        mMainFabImageSwitcher.setOutAnimation(getContext(), android.R.anim.fade_out);

        mMainFabImageSwitcher.setFactory(new ViewSwitcher.ViewFactory() {
            @Override
            public View makeView() {
                CircularImageView circularImageView = new CircularImageView(getContext());

                // Set Border
//                circularImageView.setBorderColor(getResources().getColor(R.color.icon_black));
                circularImageView.setBorderWidth(0);

                circularImageView.setLayoutParams(new ImageSwitcher.LayoutParams(
                        LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
                return circularImageView;
            }
        });
    }


    ///////////////////////////////////////////////////////////////////////////
    // public methods to be called
    ///////////////////////////////////////////////////////////////////////////

    public synchronized void setMainView(Drawable iconDrawable, @Nullable final Runnable touchAction) {
        mMainFabImageSwitcher.setImageDrawable(iconDrawable);
        mMainFabImageSwitcher.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_UP && touchAction != null) {
                    touchAction.run();
                } else if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                    // todo animate button
                }
                return true;
            }
        });
    }

    public synchronized void addLeftView(View view) {
        addView(view, indexOfChild(mMainFabImageSwitcher));
        mLeftViewList.add(view);
    }

    public synchronized void addRightView(View view) {
        addView(view);
        mRightViewList.add(view);
    }

    public synchronized void removeLeftViews() {
        // remove all left views from layout
        for (final View v : mLeftViewList) {
            removeView(v);
            mLeftViewList.remove(v);
        }
    }

    public synchronized void removeRightViews() {
        // remove all right view from layout
        for (final View v : mRightViewList) {
            removeView(v);
            mRightViewList.remove(v);
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // Overridden methods
    ///////////////////////////////////////////////////////////////////////////
    @Override
    public boolean shouldDelayChildPressedState() {
        return false;
    }


    @Override
    public synchronized void removeAllViews() {
        removeLeftViews();
        removeRightViews();
    }

}
