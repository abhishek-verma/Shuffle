package com.inpen.extendedfab;

import android.animation.LayoutTransition;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
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
    private Mode mMode = Mode.UNILATERAL;
    private boolean mIsExpanded = true;

    public ExtendedFab(Context context) {
        super(context);

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
        setClipToOutline(false);
        setElevation(getResources().getDimension(R.dimen.main_elevation));
        setGravity(Gravity.CENTER_VERTICAL);

        setLayoutTransition(new LayoutTransition());
        LayoutTransition layoutTransition = getLayoutTransition();

        //Changing enter animation
        PropertyValuesHolder pvAlphaIn =
                PropertyValuesHolder.ofFloat("alpha", 1f, 0f, 1f); // Todo use ("xxx", 0f, 1f); only as params if dosent work
        PropertyValuesHolder pvhScaleXIn =
                PropertyValuesHolder.ofFloat("scaleX", 1f, 0f, 1f);
        PropertyValuesHolder pvhScaleYIn =
                PropertyValuesHolder.ofFloat("scaleY", 1f, 0f, 1f);
        final ObjectAnimator changeIn = ObjectAnimator.ofPropertyValuesHolder(
                this, pvAlphaIn, pvhScaleXIn, pvhScaleYIn).
                setDuration(layoutTransition.getDuration(LayoutTransition.CHANGE_APPEARING));
        layoutTransition.setAnimator(LayoutTransition.APPEARING, changeIn);
        layoutTransition.setInterpolator(LayoutTransition.APPEARING, new DecelerateInterpolator());

        //Changing enter animation
        PropertyValuesHolder pvAlphaOut =
                PropertyValuesHolder.ofFloat("alpha", 1f, 0f, 1f); // Todo use ("xxx", 1f, 0f); only as params if dosent work
        PropertyValuesHolder pvhScaleXOut =
                PropertyValuesHolder.ofFloat("scaleX", 1f, 0f, 1f);
        PropertyValuesHolder pvhScaleYOut =
                PropertyValuesHolder.ofFloat("scaleY", 1f, 0f, 1f);
        final ObjectAnimator changeOut = ObjectAnimator.ofPropertyValuesHolder(
                this, pvAlphaOut, pvhScaleXOut, pvhScaleYOut).
                setDuration(layoutTransition.getDuration(LayoutTransition.CHANGE_DISAPPEARING));
        layoutTransition.setAnimator(LayoutTransition.DISAPPEARING, changeOut);
        layoutTransition.setInterpolator(LayoutTransition.DISAPPEARING, new AccelerateInterpolator());

    }

    private void setupImageSwitcher() {
        mMainFabImageSwitcher = new ImageSwitcher(getContext());

        int fabSize = (int) getResources().getDimension(R.dimen.fab_size);
        LinearLayout.LayoutParams layoutParams = new LayoutParams(fabSize, fabSize);
        layoutParams.gravity = Gravity.CENTER;
        mMainFabImageSwitcher.setLayoutParams(layoutParams);

        mMainFabImageSwitcher.setFactory(new ViewSwitcher.ViewFactory() {
            @Override
            public View makeView() {
                CircularImageView circularImageView = new CircularImageView(getContext());

                // Set Border
//                circularImageView.setBorderColor(getResources().getColor(R.color.GrayLight));
//                circularImageView.setBorderWidth(1);

                // Add Shadow with default param
                circularImageView.addShadow();
                circularImageView.setShadowRadius(1);
                circularImageView.setShadowColor(getResources().getColor(R.color.GrayLight));

                circularImageView.setLayoutParams(new ImageSwitcher.LayoutParams(
                        LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
                return circularImageView;
            }
        });
    }

    ///////////////////////////////////////////////////////////////////////////
    // public methods to be called
    ///////////////////////////////////////////////////////////////////////////
    public void setMode(Mode mode) {
        mMode = mode;
    }

    public void setMainView(Drawable iconDrawable, OnClickListener listener) {
        mMainFabImageSwitcher.setImageDrawable(iconDrawable);
        mMainFabImageSwitcher.setOnClickListener(listener);
    }

    public void addLeftView(View view) {
        if (mMode == Mode.UNILATERAL && mRightViewList.size() > 0) {
            // emove all right views from layout
            for (View v : mRightViewList) {
                removeView(v);
            }
            mRightViewList.clear();
        }

//        view.setScaleX(0);
//        view.setScaleY(0);
//        view.setAlpha(0);


        // fix for IllegalStateException: The specified child already has a parent. You must call removeView() on the child's parent first.
//        if (view.getParent() != null)
//            ((ViewGroup) view.getParent()).removeView(view);

        addView(view, indexOfChild(mMainFabImageSwitcher));
//
//        view.animate()
//                .alpha(1)
//                .scaleX(1)
//                .scaleY(1)
//                .setDuration(100)
//                .withLayer()
//                .setInterpolator(new AccelerateInterpolator());

        mLeftViewList.add(view);
    }

    public void addRightView(final View view) {
        if (mMode == Mode.UNILATERAL && mLeftViewList.size() > 0) {
            // remove all left views from layout
            for (View v : mLeftViewList) {
                removeView(v);
            }
            mLeftViewList.clear();
        }

        // add the view
//        view.setScaleX(0);
//        view.setScaleY(0);
//        view.setAlpha(0);

        // fix for IllegalStateException: The specified child already has a parent. You must call removeView() on the child's parent first.
//        if (view.getParent() != null)
//            ((ViewGroup) view.getParent()).removeView(view);

        addView(view);

//        view.animate()
//                .alpha(1)
//                .scaleX(1)
//                .scaleY(1)
//                .setDuration(100)
//                .setInterpolator(new AccelerateInterpolator())
//                .setListener(null);

        mRightViewList.add(view);
    }

    public void collapse() {
        if (!mIsExpanded)
            return;

        // hide all left and right views
        // remove all left views from layout
        for (final View v : mLeftViewList) {
            v.animate()
                    .alpha(0)
                    .scaleX(0)
                    .scaleY(0)
                    .setDuration(100)
                    .withLayer()
                    .setInterpolator(new AccelerateInterpolator())
                    .withEndAction(new Runnable() {
                        @Override
                        public void run() {
                            v.setVisibility(GONE);
                        }
                    });
        }

        for (final View v : mRightViewList) {
            v.animate()
                    .alpha(0)
                    .scaleX(0)
                    .scaleY(0)
                    .setDuration(100)
                    .withLayer()
                    .setInterpolator(new AccelerateInterpolator())
                    .withEndAction(new Runnable() {
                        @Override
                        public void run() {
                            v.setVisibility(GONE);
                        }
                    });
        }

        // TODO animate padding to 0 and margin to default


        mIsExpanded = false;
    }

    public void expand() {
        if (mIsExpanded)
            return;

        // hide all left and right views
        // remove all left views from layout
        for (final View v : mLeftViewList) {
            v.animate()
                    .alpha(1)
                    .scaleX(1)
                    .scaleY(1)
                    .setDuration(100)
                    .withLayer()
                    .setInterpolator(new AccelerateInterpolator())
                    .withEndAction(new Runnable() {
                        @Override
                        public void run() {
                            v.setVisibility(VISIBLE);
                        }
                    });
        }

        for (final View v : mRightViewList) {
            v.animate()
                    .alpha(1)
                    .scaleX(1)
                    .scaleY(1)
                    .setDuration(100)
                    .withLayer()
                    .setInterpolator(new AccelerateInterpolator())
                    .withEndAction(new Runnable() {
                        @Override
                        public void run() {
                            v.setVisibility(VISIBLE);
                        }
                    });
        }

        // TODO animate padding to default and margin to 0


        mIsExpanded = true;
    }


    ///////////////////////////////////////////////////////////////////////////
    // Overridden methods
    ///////////////////////////////////////////////////////////////////////////
    @Override
    public boolean shouldDelayChildPressedState() {
        return false;
    }


    public enum Mode {
        UNILATERAL, BILATERAL
    }
}
