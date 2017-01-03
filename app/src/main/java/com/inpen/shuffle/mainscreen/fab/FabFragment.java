package com.inpen.shuffle.mainscreen.fab;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.inpen.extendedfab.ExtendedFab;
import com.inpen.shuffle.R;
import com.inpen.shuffle.mainscreen.MainActivity;
import com.inpen.shuffle.model.MutableMediaMetadata;
import com.inpen.shuffle.utility.CustomTypes;
import com.inpen.shuffle.utility.LogHelper;
import com.inpen.shuffle.utility.ResourceHelper;

/**
 * Created by Abhishek on 12/23/2016.
 */

public class FabFragment extends Fragment implements FabContract.FabView {
    private static final int MAX_ART_WIDTH_ICON = 128;  // pixels
    private static final int MAX_ART_HEIGHT_ICON = 128;  // pixels
    String TAG = LogHelper.makeLogTag(FabFragment.class);
    @CustomTypes.FabMode
    int mFabMode;
    private FabContract.InteractionsListener mFabInteractionListener;
    private ExtendedFab mExtendedFab;
    private ImageButton mPlayPauseButton;
    private TextView mShuffleAndPlayTextView;

    private MediaMetadataCompat mMetadata;
    private int mPlaybackState = PlaybackStateCompat.STATE_NONE;

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
        mExtendedFab = (ExtendedFab) inflater.inflate(R.layout.fragment_fab, container, false);
        mExtendedFab.setVisibility(View.INVISIBLE);

        mFabMode = CustomTypes.FabMode.DISABLED;

        return mExtendedFab;
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
    public synchronized void showPlayer(final MediaMetadataCompat metadata, final PlaybackStateCompat playbackState) {
        boolean shouldReveal = false;

        mMetadata = metadata;
        mPlaybackState = (playbackState.getState() == PlaybackStateCompat.STATE_PLAYING) ?
                PlaybackStateCompat.STATE_PLAYING : PlaybackStateCompat.STATE_PAUSED;

        if (mFabMode == CustomTypes.FabMode.PLAYER) {
            LogHelper.d(TAG, "Player already shown, returning...");
            return;
        } else if (mFabMode == CustomTypes.FabMode.DISABLED) {
            LogHelper.d(TAG, "Previously disabled! Showing Player...");
            shouldReveal = true;
        } else {
            LogHelper.d(TAG, "Must remove previous views before showing Player");
            mShuffleAndPlayTextView = null;
            mExtendedFab.removeAllViews();
        }


        //noinspection ResourceType
        LogHelper.d(TAG, "SHOW PLAYER");
        //noinspection ResourceType
        LogHelper.d(TAG, "TITLE: " + mMetadata.getString(MutableMediaMetadata.CUSTOM_METADATA_KEY_TRACK_ID));
        LogHelper.d(TAG, "ART URL: " + mMetadata.getString(MediaMetadataCompat.METADATA_KEY_ALBUM_ART_URI));

        if (shouldReveal) {
            addPlayerViews(metadata, playbackState);
            mExtendedFab.post(new ExtendedFabRevealTask(CustomTypes.FabMode.PLAYER));
        } else {
            addPlayerViews(metadata, playbackState);
            mFabMode = CustomTypes.FabMode.PLAYER;
        }
    }


    @Override
    public synchronized void updatePlayer(MediaMetadataCompat metadata, PlaybackStateCompat playbackStateCompat) {

        if (metadata == null || playbackStateCompat == null) {
            return;
        }

        if (mFabMode != CustomTypes.FabMode.PLAYER) {
            showPlayer(metadata, playbackStateCompat);
            return;
        }

        LogHelper.d(TAG, "previous ID: " + metadata.getString(MutableMediaMetadata.CUSTOM_METADATA_KEY_TRACK_ID));
        //noinspection ResourceType
        if (mMetadata == null ||
                !mMetadata.getString(MutableMediaMetadata.CUSTOM_METADATA_KEY_TRACK_ID)
                        .equals(metadata.getString(MutableMediaMetadata.CUSTOM_METADATA_KEY_TRACK_ID))) {

            mMetadata = metadata;

            //noinspection ResourceType
            LogHelper.d(TAG, "TITLE: " + metadata.getString(MutableMediaMetadata.CUSTOM_METADATA_KEY_TRACK_ID));
            LogHelper.d(TAG, "ART URL: " + metadata.getString(MediaMetadataCompat.METADATA_KEY_ALBUM_ART_URI));

            // getting album art and setting
            displayAlbumArt(metadata.getString(MediaMetadataCompat.METADATA_KEY_ALBUM_ART_URI));
        }


        int state = playbackStateCompat.getState() == PlaybackStateCompat.STATE_PLAYING ?
                PlaybackStateCompat.STATE_PLAYING : PlaybackStateCompat.STATE_PAUSED;

        if (mPlaybackState != state) {
            mPlaybackState = state;

            Drawable playPauseDrawable = getResources()
                    .getDrawable(
                            playbackStateCompat.getState() == PlaybackStateCompat.STATE_PLAYING ?
                                    R.drawable.ic_pause_black_24dp :
                                    R.drawable.ic_play_arrow_black_24dp
                    );

            //adding pause button
            mPlayPauseButton.setImageDrawable(playPauseDrawable);
        }
    }

    @Override
    public void showLoading() {
        mPlayPauseButton = null;
        mShuffleAndPlayTextView = null;
        mExtendedFab.removeAllViews();
        mExtendedFab.setMainView(getResources().getDrawable(R.mipmap.ic_shuffle_btn), null);
    }

    @Override
    public synchronized void showShuffle() {
        boolean shouldReveal = false;

        if (mFabMode == CustomTypes.FabMode.SHUFFLE) {
            LogHelper.d(TAG, "Shuffle already shown, returning...");
            return;
        } else if (mFabMode == CustomTypes.FabMode.DISABLED) {
            LogHelper.d(TAG, "Previously disabled! Showing shuffle...");
            shouldReveal = true;
        } else {
            LogHelper.d(TAG, "Must remove previous views before showing shuffle");
            mPlayPauseButton = null;
            mExtendedFab.removeAllViews();
        }

        if (shouldReveal) {
            addShuffleViews();
            mExtendedFab.post(new ExtendedFabRevealTask(CustomTypes.FabMode.SHUFFLE));
        } else {
            addShuffleViews();
            mFabMode = CustomTypes.FabMode.SHUFFLE;
        }
    }


    @Override
    public synchronized void disable(boolean animate) {

        mPlaybackState = PlaybackStateCompat.STATE_NONE;

        mPlayPauseButton = null;
        mShuffleAndPlayTextView = null;

        if (!animate) {
            mFabMode = CustomTypes.FabMode.DISABLED;
            mExtendedFab.setVisibility(View.INVISIBLE);
            mExtendedFab.removeAllViews();
            return;
        }

        if (mFabMode == CustomTypes.FabMode.DISABLED) {
            return;
        }

        LogHelper.d(TAG, "Disabled, removing fab!");
//        mFabMode = CustomTypes.FabMode.ANIMATING;

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
        return getActivity();
    }

    private void addPlayerViews(MediaMetadataCompat metadataCompat, PlaybackStateCompat playbackState) {

        LogHelper.d(TAG, "Adding player views");

        // getting album art and setting
        displayAlbumArt(metadataCompat.getString(MediaMetadataCompat.METADATA_KEY_ALBUM_ART_URI));

        boolean isPlaying = playbackState.getState() == PlaybackStateCompat.STATE_PLAYING;

        Drawable playPauseDrawable = getResources()
                .getDrawable(isPlaying ?
                        R.drawable.ic_pause_black_24dp :
                        R.drawable.ic_play_arrow_black_24dp
                );

        //adding pause button
        if (mPlayPauseButton == null) {
            mPlayPauseButton = getImageButton(playPauseDrawable,
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            mFabInteractionListener.playPausedClicked();
                        }
                    });
        }

        mExtendedFab.addRightView(mPlayPauseButton);

    }

    private synchronized void displayAlbumArt(String artUrl) {

        LogHelper.d(TAG, "Displaying album art!");

        Glide
                .with(getContext())
                .load(artUrl)
                .asBitmap()
                .dontAnimate()
                .error(getResources().getDrawable(R.drawable.ic_loading_circle, null))
                .into(new SimpleTarget<Bitmap>(MAX_ART_WIDTH_ICON, MAX_ART_HEIGHT_ICON) {
                    boolean handled = false;

                    @Override
                    public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {

                        if (handled) {
                            return;
                        }

                        mExtendedFab
                                .setMainView(
                                        new BitmapDrawable(getResources(), resource),
                                        new Runnable() {
                                            @Override
                                            public void run() {
                                                mFabInteractionListener
                                                        .playerIconClicked((AppCompatActivity) getActivity());
                                            }
                                        });

                        handled = true;
                    }

                    @Override
                    public void onLoadFailed(Exception e, Drawable errorDrawable) {
                        super.onLoadFailed(e, errorDrawable);
                        LogHelper.e(TAG, e);

                        if (handled)
                            return;

                        mExtendedFab
                                .setMainView(
                                        getResources().getDrawable(R.drawable.ic_loading_circle),
                                        new Runnable() {
                                            @Override
                                            public void run() {
                                                mFabInteractionListener
                                                        .playerIconClicked((AppCompatActivity) getActivity());
                                            }
                                        });

                        handled = true;
                    }
                });
    }

    private ImageButton getImageButton(Drawable icon, View.OnClickListener listener) {
        ImageButton btn = new ImageButton(getContext());

        int padding = getResources().getDimensionPixelSize(R.dimen.exfab_borderless_btn_padding);
        btn.setPadding(padding, padding, padding, padding);

        btn.setImageDrawable(icon);
        btn.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        btn.setBackgroundResource(ResourceHelper.getBorderlessButtonBackground(getActivity()));

        btn.setOnClickListener(listener);

        return btn;
    }

    private synchronized void addShuffleViews() {

        // adding icon
        mExtendedFab.setMainView(getResources().getDrawable(R.mipmap.ic_shuffle_btn),
                new Runnable() {
                    @Override
                    public void run() {
                        mFabInteractionListener.shuffleClicked((MainActivity) getActivity());
                    }
                });

        // adding tap to shuffle text
        if (mShuffleAndPlayTextView == null) {
            mShuffleAndPlayTextView = new TextView(getContext());
            mShuffleAndPlayTextView.setText(getString(R.string.tap_to_shuffle));
            mShuffleAndPlayTextView.setMaxLines(2);
            mShuffleAndPlayTextView.setAlpha(0.7f);
            int padding = getResources().getDimensionPixelSize(R.dimen.fab_child_padding);
            mShuffleAndPlayTextView.setPadding(padding, 0, padding, 0);
            mShuffleAndPlayTextView.setGravity(Gravity.RIGHT);
            mShuffleAndPlayTextView.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));

            mExtendedFab.addLeftView(mShuffleAndPlayTextView);
        }
    }

    private class ExtendedFabRevealTask implements Runnable {

        private final
        @CustomTypes.FabMode
        int mFinalFabMode;

        ExtendedFabRevealTask(@CustomTypes.FabMode int finalMode) {
            mFinalFabMode = finalMode;
        }

        @Override
        public void run() {
            if (mExtendedFab == null)
                return;

            mFabMode = mFinalFabMode;

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

                }
            });

            mExtendedFab.setVisibility(View.VISIBLE);
            animator.start();
        }
    }
}

