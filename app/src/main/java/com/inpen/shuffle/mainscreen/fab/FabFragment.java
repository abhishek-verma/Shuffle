package com.inpen.shuffle.mainscreen.fab;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.inpen.extendedfab.ExtendedFab;
import com.inpen.shuffle.R;
import com.inpen.shuffle.mainscreen.MainActivity;
import com.inpen.shuffle.utility.CustomTypes;
import com.inpen.shuffle.utility.LogHelper;

/**
 * Created by Abhishek on 12/23/2016.
 */

public class FabFragment extends Fragment implements FabContract.FabView {
    private static final int MAX_ART_WIDTH_ICON = 128;  // pixels
    private static final int MAX_ART_HEIGHT_ICON = 128;  // pixels
    String TAG = LogHelper.makeLogTag(FabFragment.class);
    private ExtendedFab mExtendedFab;
    private boolean expandedState;
    private CustomTypes.FabMode mFabMode;
    private FabContract.InteractionsListener mFabInteractionListener;

    public static FabFragment newInstance() {
        return new FabFragment();
    }

    @Override
    public void onStart() {
        super.onStart();

        mFabInteractionListener = new FabPresenter();
        mFabInteractionListener.init(this, getContext());
    }

    @Override
    public void onStop() {
        super.onStop();

        mFabInteractionListener.stop();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        FrameLayout parentView = (FrameLayout) inflater.inflate(R.layout.fragment_fab, container, false);

        mExtendedFab = new ExtendedFab(getContext());
        mExtendedFab.setVisibility(View.INVISIBLE);

        mFabMode = CustomTypes.FabMode.DISABLED;

        parentView.addView(mExtendedFab);

        return parentView;
    }

    @Override
    public void connectToMediaController() {
        MediaControllerCompat controller = getActivity()
                .getSupportMediaController();
        LogHelper.d(TAG, "onConnected, mediaController==null? ", controller == null);
        if (controller != null) {
            mFabInteractionListener.metadataChanged(controller.getMetadata());
            mFabInteractionListener.playbackStateChanged(controller.getPlaybackState());
            controller.registerCallback(mFabInteractionListener.getControllerCallback());
            mFabInteractionListener.setTransportControls(controller.getTransportControls());
        }
    }


    @Override
    public synchronized void showPlayer(MediaMetadataCompat metadata) {
        boolean shouldReveal = false;

        if (mFabMode.equals(CustomTypes.FabMode.PLAYER) || metadata == null) {
            LogHelper.d(TAG, "Player already shown, returning...");
            return;
        } else if (mFabMode.equals(CustomTypes.FabMode.DISABLED)) {
            LogHelper.d(TAG, "Previously disabled! Showing Player...");
            shouldReveal = true;
        } else {
            LogHelper.d(TAG, "Must remove previous views before showing Player");
            mExtendedFab.removeAllViews();
        }

        mFabMode = CustomTypes.FabMode.ANIMATING;

        if (shouldReveal) {
            addPlayerViews(metadata, false);
            mExtendedFab.post(new ExtendedFabRevealTask(CustomTypes.FabMode.PLAYER));
        } else {
            addPlayerViews(metadata, true);
            mFabMode = CustomTypes.FabMode.PLAYER;
        }
    }

    @Override
    public synchronized void showShuffle() {
        boolean shouldReveal = false;

        if (mFabMode.equals(CustomTypes.FabMode.SHUFFLE)) {
            LogHelper.d(TAG, "Shuffle already shown, returning...");
            return;
        } else if (mFabMode.equals(CustomTypes.FabMode.DISABLED)) {
            LogHelper.d(TAG, "Previously disabled! Showing shuffle...");
            shouldReveal = true;
        } else {
            LogHelper.d(TAG, "Must remove previous views before showing shuffle");
            mExtendedFab.removeAllViews();
        }

        mFabMode = CustomTypes.FabMode.ANIMATING;

        if (shouldReveal) {
            addShuffleViews(false);
            mExtendedFab.post(new ExtendedFabRevealTask(CustomTypes.FabMode.SHUFFLE));
        } else {
            addShuffleViews(true);
            mFabMode = CustomTypes.FabMode.SHUFFLE;
        }
    }

    @Override
    public synchronized void showLoading() {
        Toast.makeText(getContext(), "Loading Queue!", Toast.LENGTH_SHORT).show();
    }

    @Override
    public synchronized void disable(boolean animate) {
        if (!animate) {
            mFabMode = CustomTypes.FabMode.DISABLED;
            mExtendedFab.setVisibility(View.INVISIBLE);
            mExtendedFab.removeAllViews();
        }

        if (mFabMode.equals(CustomTypes.FabMode.DISABLED) || mFabMode.equals(CustomTypes.FabMode.ANIMATING)) {
            return;
        }

        LogHelper.d(TAG, "Disabled, removing fab!");
        mFabMode = CustomTypes.FabMode.ANIMATING;

        mExtendedFab.post(new Runnable() {
            @Override
            public void run() {
                // get the center for the clipping circle

                int cx = mExtendedFab.getMeasuredWidth() / 2;
                int cy = mExtendedFab.getMeasuredHeight() / 2;

                double hypt = Math.hypot(mExtendedFab.getMeasuredHeight(), mExtendedFab.getMeasuredWidth());

                LogHelper.i(TAG, "height: " + mExtendedFab.getHeight() + ", width: " + mExtendedFab.getWidth());
                LogHelper.i(TAG, "measured height: " + mExtendedFab.getMeasuredHeight() + ", measured width: " + mExtendedFab.getMeasuredWidth());
                LogHelper.i(TAG, "hypt: " + hypt);

                // get the initial radius for the clipping circle
                int initialRadius = (int) (hypt / 2);

                // create the animation (the final radius is zero)
                Animator anim =
                        ViewAnimationUtils.createCircularReveal(mExtendedFab, cx, cy, initialRadius, 0);
                anim.setDuration(500);

                // make the view invisible when the animation is done
                anim.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        mExtendedFab.setVisibility(View.INVISIBLE);
                        mExtendedFab.removeAllViews();
                        mFabMode = CustomTypes.FabMode.DISABLED;
                    }
                });

                // start the animation
                anim.start();
            }
        });
    }

    @Override
    public FragmentActivity getFragmentActivity() {
        return null;
    }

    private void addPlayerViews(MediaMetadataCompat metadataCompat, boolean animate) {

        // getting album art and setting
        Glide
                .with(getContext())
                .load(metadataCompat.getString(MediaMetadataCompat.METADATA_KEY_ALBUM_ART_URI))
                .asBitmap()
                .into(new SimpleTarget<Bitmap>(MAX_ART_WIDTH_ICON, MAX_ART_HEIGHT_ICON) {
                    @Override
                    public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                        mExtendedFab
                                .setMainView(
                                        new BitmapDrawable(getResources(), resource),
                                        new View.OnClickListener() {
                                            @Override
                                            public void onClick(View view) {
                                                mFabInteractionListener
                                                        .playerIconClicked((AppCompatActivity) getActivity());
                                            }
                                        });
                    }
                });

    }

    private void addShuffleViews(boolean animate) {

        // adding icon
        mExtendedFab.setMainView(getResources().getDrawable(R.mipmap.ic_shuffle_btn), new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mFabInteractionListener.shuffleClicked((MainActivity) getActivity());

            }
        });

        // adding tap to shuffle text
        final TextView tv = new TextView(getContext());
        tv.setText(getString(R.string.tap_to_shuffle));
        tv.setMaxLines(2);
        tv.setAlpha(0.7f);
        int padding = getResources().getDimensionPixelSize(R.dimen.fab_child_padding);
        tv.setPadding(padding, 0, padding, 0);
        tv.setGravity(Gravity.RIGHT);
        tv.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        mExtendedFab.addLeftView(tv, animate);
    }


    private class ExtendedFabRevealTask implements Runnable {

        private final CustomTypes.FabMode mFinalFabMode;

        public ExtendedFabRevealTask(CustomTypes.FabMode finalMode) {
            mFinalFabMode = finalMode;
        }

        @Override
        public void run() {
            if (mExtendedFab == null)
                return;

            int cx = mExtendedFab.getMeasuredWidth() / 2;
            int cy = mExtendedFab.getMeasuredHeight() / 2;

            double hypt = Math.hypot(mExtendedFab.getMeasuredHeight(), mExtendedFab.getMeasuredWidth());
            int finalRadius = (int) (hypt / 2);

            LogHelper.i(TAG, "height: " + mExtendedFab.getHeight() + ", width: " + mExtendedFab.getWidth());
            LogHelper.i(TAG, "measured height: " + mExtendedFab.getMeasuredHeight() + ", measured width: " + mExtendedFab.getMeasuredWidth());
            LogHelper.i(TAG, "hypt: " + hypt);

            Animator animator = ViewAnimationUtils.createCircularReveal(mExtendedFab, cx, cy, 0, finalRadius);
            animator.setDuration(500);

            animator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);

                    mFabMode = mFinalFabMode;
                }
            });

            mExtendedFab.setVisibility(View.VISIBLE);
            animator.start();
        }
    }
}

