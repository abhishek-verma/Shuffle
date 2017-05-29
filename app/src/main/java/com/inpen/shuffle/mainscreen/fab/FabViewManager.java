package com.inpen.shuffle.mainscreen.fab;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.support.v4.media.MediaMetadataCompat;
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
import com.inpen.shuffle.utility.CustomTypes;
import com.inpen.shuffle.utility.LogHelper;
import com.inpen.shuffle.utility.ResourceHelper;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static android.view.View.GONE;

/**
 * Used to manipulate fab
 * this class has no logic (any view is displayed, visible etc)
 * this class just follows and executes instructions blindly
 * logic is handled by {@link FabFragment}
 * <p>
 * keeps a mode variable which stores the current state of FAB like shuffle, player etc
 */

public class FabViewManager {

    private static final String TAG = LogHelper.makeLogTag(FabViewManager.class);

    private static final int MAX_ART_WIDTH_ICON = 128;  // pixels
    private static final int MAX_ART_HEIGHT_ICON = 128;  // pixels

    private final FabViewManagerListener mFabManagerListener;
    TextView mSelectedItemCountTextView;
    private ExecutorService mExecutorService;
    @CustomTypes.FabMode
    private int mFabMode = CustomTypes.FabMode.DISABLED;
    private ExtendedFab mExtendedFab;
    private ImageButton mShuffleDeselectButton;
    private TextView mShuffleTextView;
    private View mBarView;
    private ImageButton mPlayPauseButton;
    private ImageButton mAddButton;
    private ImageButton mClosePlayerButton;


    FabViewManager(FabViewManagerListener listener) {
        mFabManagerListener = listener;
        mExecutorService = Executors.newSingleThreadExecutor();
    }

    void resume() {
        if (mExecutorService.isShutdown()) {
            LogHelper.d(TAG, "ExecutorService is shutdown, restarting service.");
            mExecutorService = Executors.newSingleThreadExecutor();
        }
    }

    void initViews(LayoutInflater inflater, @Nullable ViewGroup container, Context context) {
        mExtendedFab = (ExtendedFab) inflater.inflate(R.layout.fragment_fab, container, false);
        mExtendedFab.setVisibility(View.INVISIBLE);

        mPlayPauseButton = getImageButton(context.getDrawable(R.drawable.ic_play_arrow_black_24dp),
                context,
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        mFabManagerListener.playPauseClicked();
                    }
                });
        mAddButton = getImageButton(context.getDrawable(R.drawable.ic_playlist_add_black_24dp),
                context,
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        mFabManagerListener.plusButtonClicked();
                    }
                });
        mClosePlayerButton = getImageButton(context.getDrawable(R.drawable.ic_clear_black_24dp),
                context,
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        mFabManagerListener.closePlayerClicked();
                    }
                });
        mShuffleDeselectButton = getImageButton(context.getDrawable(R.drawable.ic_clear_black_24dp),
                context,
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        mFabManagerListener.deselectButtonClicked();
                    }
                });

        mShuffleTextView = new TextView(context);
        mShuffleTextView.setText(context.getString(R.string.tap_to_shuffle));
        mShuffleTextView.setMaxLines(2);
        mShuffleTextView.setAlpha(0.7f);
        mShuffleTextView.setVisibility(GONE);
        int padding = context.getResources().getDimensionPixelSize(R.dimen.fab_child_padding);
        mShuffleTextView.setPadding(padding, 0, padding, 0);
        mShuffleTextView.setGravity(Gravity.RIGHT);
        mShuffleTextView.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        mShuffleTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mFabManagerListener.shuffleClicked();
            }
        });

        mSelectedItemCountTextView = new TextView(context);
        mSelectedItemCountTextView
                .setTextSize(context
                        .getResources()
                        .getDimension(R.dimen.selected_count_text_size));
        mSelectedItemCountTextView.setText("0");
        mSelectedItemCountTextView.setTextColor(context
                .getResources()
                .getColor(R.color.black));
        mSelectedItemCountTextView.setSingleLine();
        mSelectedItemCountTextView.setVisibility(GONE);
        mSelectedItemCountTextView.setPadding(padding, 0, padding, 0);
        mSelectedItemCountTextView.setGravity(Gravity.CENTER);
        mSelectedItemCountTextView.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        mSelectedItemCountTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mFabManagerListener.deselectButtonClicked();
            }
        });

        mBarView = new View(context);
        mBarView.setLayoutParams(new FrameLayout.LayoutParams(
                (int) context.getResources().getDimension(R.dimen.h_bar_width),
                (int) context.getResources().getDimension(R.dimen.h_bar_height)));
        mBarView.setBackgroundColor(context.getResources().getColor(R.color.h_bar_black));
        mBarView.setVisibility(GONE);

        mExtendedFab.addLeftView(mShuffleDeselectButton);
        mExtendedFab.addLeftView(mSelectedItemCountTextView);
        mExtendedFab.addLeftView(mAddButton);
        mExtendedFab.addLeftView(mBarView);
        mExtendedFab.addLeftView(mShuffleTextView);

        mExtendedFab.addRightView(mPlayPauseButton);
        mExtendedFab.addRightView(mClosePlayerButton);
    }

    private ImageButton getImageButton(Drawable icon, Context context, View.OnClickListener listener) {
        ImageButton btn = new ImageButton(context);

        int padding = context.getResources().getDimensionPixelSize(R.dimen.exfab_borderless_btn_padding);
        btn.setPadding(padding, padding, padding, padding);

        btn.setImageDrawable(icon);
        btn.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        btn.setBackgroundResource(ResourceHelper.getBorderlessButtonBackground(context));

        btn.setVisibility(GONE);

        btn.setOnClickListener(listener);

        return btn;
    }

    ExtendedFab getFabView() {
        return mExtendedFab;
    }

    synchronized void showShuffleView(final FragmentActivity activity, final boolean showPlus) {

        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {

                mExtendedFab.setMainView(activity.getDrawable(R.mipmap.ic_shuffle_btn),
                        new Runnable() {
                            @Override
                            public void run() {
                                mFabManagerListener.shuffleClicked();
                            }
                        });
            }
        });

        final Runnable addViewsTask = new Runnable() {
            @Override
            public void run() {

                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        if (mFabMode != CustomTypes.FabMode.SHUFFLE
                                && mFabMode != CustomTypes.FabMode.DISABLED) {
                            hideAllViews();
                        }

                        // adding text view
                        mSelectedItemCountTextView.setVisibility(View.VISIBLE);
                        mShuffleDeselectButton.setVisibility(View.VISIBLE);
                        mBarView.setVisibility(View.VISIBLE);
                        mShuffleTextView.setVisibility(View.VISIBLE);
                        if (showPlus)
                            mAddButton.setVisibility(View.VISIBLE);

                        mFabMode = CustomTypes.FabMode.SHUFFLE;
                    }
                });
            }
        };

        Runnable revealTask = new Runnable() {
            @Override
            public void run() {
                if (mFabMode == CustomTypes.FabMode.DISABLED) {
                    enterReveal(addViewsTask);
                } else {
                    mExecutorService.execute(addViewsTask);
                }
            }
        };

        mExecutorService.execute(revealTask);
    }

    void showPlayerView(final MediaMetadataCompat metadata, final FragmentActivity activity) {

        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mExtendedFab.removeMainViewListener();
            }
        });

        // setting icon
        displayAlbumArt(metadata.getString(MediaMetadataCompat.METADATA_KEY_ALBUM_ART_URI), activity);

        Runnable addViewsRunnable = new Runnable() {
            @Override
            public void run() {

                // adding buttons
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        if (!isPlayerMode()
                                && mFabMode != CustomTypes.FabMode.DISABLED) {
                            hideAllViews();
                        }

                        mPlayPauseButton.setVisibility(View.VISIBLE);
                        mClosePlayerButton.setVisibility(View.VISIBLE);

                        mFabMode = CustomTypes.FabMode.PLAYER;
                    }
                });
            }
        };
        if (mFabMode == CustomTypes.FabMode.DISABLED) {
            enterReveal(addViewsRunnable);
        } else {
            mExecutorService.execute(addViewsRunnable);
        }
    }

    void hideAllViews() {
        mSelectedItemCountTextView.setVisibility(GONE);
        mShuffleDeselectButton.setVisibility(GONE);
        mBarView.setVisibility(GONE);
        mShuffleTextView.setVisibility(GONE);
        mPlayPauseButton.setVisibility(GONE);
        mAddButton.setVisibility(GONE);
        mClosePlayerButton.setVisibility(GONE);
    }

    void updatePlayerMetadata(MediaMetadataCompat metadata, Context context) {
        if (!isPlayerMode())
            return;

        // setting icon
        displayAlbumArt(metadata.getString(MediaMetadataCompat.METADATA_KEY_ALBUM_ART_URI), context);
    }

    void showPlayButton() {
        mExecutorService.execute(new Runnable() {
            @Override
            public void run() {
                mExtendedFab.post(new Runnable() {
                    @Override
                    public void run() {
                        mPlayPauseButton
                                .setImageDrawable(mPlayPauseButton.getResources().getDrawable(R.drawable.ic_play_arrow_black_24dp, null));
                    }
                });
            }
        });
    }

    void showPauseButton() {
        mExecutorService.execute(new Runnable() {
            @Override
            public void run() {
                mExtendedFab.post(new Runnable() {
                    @Override
                    public void run() {
                        mPlayPauseButton
                                .setImageDrawable(mPlayPauseButton.getResources().getDrawable(R.drawable.ic_pause_black_24dp, null));
                    }
                });
            }
        });
    }

    void showPlusButton() {
        mExecutorService.execute(new Runnable() {
            @Override
            public void run() {
                mExtendedFab.post(new Runnable() {
                    @Override
                    public void run() {
                        mAddButton.setVisibility(View.VISIBLE);
                    }
                });
            }
        });
    }

    void hidePlusButton() {
        mExecutorService.execute(new Runnable() {
            @Override
            public void run() {
                mExtendedFab.post(new Runnable() {
                    @Override
                    public void run() {
                        mAddButton.setVisibility(GONE);
                    }
                });
            }
        });
    }

    void disableFab(final FragmentActivity activity) {
        final Runnable exitRevealTask = new Runnable() {
            @Override
            public void run() {
                // get the center for the clipping circle

                int cx = mExtendedFab.getMeasuredWidth() / 2;
                int cy = mExtendedFab.getMeasuredHeight() / 2;

                double hypt = Math.hypot(mExtendedFab.getMeasuredHeight(), mExtendedFab.getMeasuredWidth());

                // get the initial radius for the clipping circle
                int initialRadius = (int) (hypt / 2);

                // create the animation (the final radius is zero)
                final Animator anim =
                        ViewAnimationUtils.createCircularReveal(mExtendedFab, cx, cy, initialRadius, 0);

                // make the view invisible when the animation is done
                anim.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        mExtendedFab.setVisibility(View.INVISIBLE);
                        hideAllViews();
                        mFabMode = CustomTypes.FabMode.DISABLED;
                    }
                });

                // start the animation
                activity.runOnUiThread(new Runnable() {
                    public void run() {
                        anim.start();
                    }
                });
            }
        };

        mExecutorService.execute(exitRevealTask);
    }

    public void stop() {
        shutdownAndAwaitTermination(mExecutorService);
    }

    void shutdownAndAwaitTermination(ExecutorService pool) {
        pool.shutdown(); // Disable new tasks from being submitted
        try {
            // Wait a while for existing tasks to terminate
            if (!pool.awaitTermination(60, TimeUnit.SECONDS)) {
                pool.shutdownNow(); // Cancel currently executing tasks
                // Wait a while for tasks to respond to being cancelled
                if (!pool.awaitTermination(60, TimeUnit.SECONDS))
                    System.err.println("Pool did not terminate");
            }
        } catch (InterruptedException ie) {
            // (Re-)Cancel if current thread also interrupted
            pool.shutdownNow();
            // Preserve interrupt status
            Thread.currentThread().interrupt();
        }
    }

    private synchronized void enterReveal(final Runnable postAnimationRunnable) {
        if (mExtendedFab == null)
            return;

        mExtendedFab.post(new Runnable() {
            @Override
            public void run() {
                //doing reveal animation
                int cx = mExtendedFab.getMeasuredWidth() / 2;
                int cy = mExtendedFab.getMeasuredHeight() / 2;

                double hypt = Math.hypot(mExtendedFab.getMeasuredHeight(), mExtendedFab.getMeasuredWidth());
                int finalRadius = (int) (hypt / 2);

                LogHelper.i(TAG, "height: " + mExtendedFab.getHeight() + ", width: " + mExtendedFab.getWidth());
                LogHelper.i(TAG, "measured height: " + mExtendedFab.getMeasuredHeight() + ", measured width: " + mExtendedFab.getMeasuredWidth());
                LogHelper.i(TAG, "hypt: " + hypt);

                final Animator animator = ViewAnimationUtils.createCircularReveal(mExtendedFab, cx, cy, 0, finalRadius);

                animator.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        mExecutorService.execute(postAnimationRunnable);
                    }
                });
                mExtendedFab.setVisibility(View.VISIBLE);
                animator.start();
            }
        });
    }

    private synchronized void displayAlbumArt(String artUrl, final Context context) {

        LogHelper.e("temp", "artUrl: " + artUrl);

        Glide
                .with(context)
                .load(artUrl)
                .asBitmap()
                .dontAnimate()
                .error(context.getDrawable(R.drawable.shuffle_bg))
                .into(new SimpleTarget<Bitmap>(MAX_ART_WIDTH_ICON, MAX_ART_HEIGHT_ICON) {
                    boolean handled = false;

                    @Override
                    public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {

                        if (handled) {
                            return;
                        }

                        mExtendedFab
                                .setMainView(
                                        new BitmapDrawable(context.getResources(), resource),
                                        new Runnable() {
                                            @Override
                                            public void run() {
                                                mFabManagerListener.playerIconClicked();
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
                                        context.getDrawable(R.drawable.shuffle_bg),
                                        new Runnable() {
                                            @Override
                                            public void run() {
                                                mFabManagerListener.playerIconClicked();
                                            }
                                        });

                        handled = true;
                    }
                });
    }

    boolean isPlayerMode() {
        return mFabMode == CustomTypes.FabMode.PLAYER || mFabMode == CustomTypes.FabMode.PLAYER_WITH_ADD;
    }

    interface FabViewManagerListener {

        void shuffleClicked();

        void playerIconClicked();

        void plusButtonClicked();

        void playPauseClicked();

        void closePlayerClicked();

        void deselectButtonClicked();
    }

}
