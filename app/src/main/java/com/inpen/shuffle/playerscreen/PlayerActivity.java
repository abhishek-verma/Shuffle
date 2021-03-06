package com.inpen.shuffle.playerscreen;

import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.RemoteException;
import android.os.SystemClock;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.RatingCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatSeekBar;
import android.support.v7.widget.Toolbar;
import android.text.format.DateUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;

import com.inpen.playpausebutton.PlayPauseAnimatedButton;
import com.inpen.shuffle.R;
import com.inpen.shuffle.model.repositories.QueueRepository;
import com.inpen.shuffle.playerscreen.player.PlayerViewPagerAdapter;
import com.inpen.shuffle.playerscreen.playingqueue.PlayingQueueFragment;
import com.inpen.shuffle.playerscreen.recommendation.RecommendationFragment;
import com.inpen.shuffle.utility.LogHelper;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class PlayerActivity extends AppCompatActivity
        implements PlayerActivityContract.PlayerActivityView {

    private static final String LOG_TAG = LogHelper.makeLogTag(PlayerActivity.class);

    private static final long PROGRESS_UPDATE_INTERNAL = 1000;
    private static final long PROGRESS_UPDATE_INITIAL_INTERVAL = 1000; //WAS 100, changed it to 1000
    private final ScheduledExecutorService mExecutorService =
            Executors.newSingleThreadScheduledExecutor();
    private final Handler mHandler = new Handler();
    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @BindView(R.id.playerViewpager)
    ViewPager mPlayerViewPager;
    @BindView(R.id.seekbar)
    AppCompatSeekBar mSeekbar;
    @BindView(R.id.totalDuration)
    TextView mTotalDurationView;
    @BindView(R.id.currentDuration)
    TextView mCurrentDurationView;
    @BindView(R.id.playPauseBtn)
    PlayPauseAnimatedButton mPlayPauseBtn;
    @BindView(R.id.prevButton)
    ImageButton mPrevButton;
    @BindView(R.id.nextButton)
    ImageButton mNextButton;
    @BindView(R.id.likeButton)
    ImageButton mLikeButton;
    @BindView(R.id.dislikeButton)
    ImageButton mDislikeButton;
    private PlayerActivityContract.PlayerActivityListener mPlayerActivityPresenter;
    private PlayerViewPagerAdapter mPlayerAdapter;
    private ScheduledFuture<?> mScheduleFuture;
    private PlaybackStateCompat mLastPlaybackState;
    private final Runnable mUpdateProgressTask = new Runnable() {
        @Override
        public void run() {
            updateProgress();
        }
    };
    private Fragment mPlayingQueueFragment;
    private RecommendationFragment mRecommendationsFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        ButterKnife.bind(this);

        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mPlayerActivityPresenter = new PlayerActivityPresenter(this);
        mPlayerActivityPresenter.init(this);

        mSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mCurrentDurationView.setText(DateUtils.formatElapsedTime(progress / 1000));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                stopSeekbarUpdate();
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                getSupportMediaController().getTransportControls().seekTo(seekBar.getProgress());
                scheduleSeekbarUpdate();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        setupFragments();
        setupAdapterAndViewPager();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mPlayerActivityPresenter.stop(this);
        mPlayerViewPager.removeOnPageChangeListener(mPlayerActivityPresenter.getPageChangeListener());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.player_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_playlist) {
            mPlayerActivityPresenter.showPlaylistClicked();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            LogHelper.d(LOG_TAG, "x: " + event.getX() + ", y: " + event.getY());
            if (!mPlayingQueueFragment.isHidden()) {

                Rect rect = new Rect();
                View fragmentView = mPlayingQueueFragment.getView();
                fragmentView.getHitRect(rect);

                rect.left = 0;
                rect.top = 0;
                rect.right = rect.left + fragmentView.getWidth();
                rect.bottom = (int) (rect.top + fragmentView.getHeight() * 1.5f);

//                LogHelper.d(LOG_TAG, "left: " + rect.left + ", right: " + rect.right + ", top: " + rect.top + ", bottom: " + rect.bottom);
//                LogHelper.d(LOG_TAG, "is inside fragment: " + rect.contains((int) event.getX(), (int) event.getY()));

                if (!rect.contains((int) event.getX(), (int) event.getY())) {
                    togglePlaylistVisibility();
                }
            }
        }
        return super.dispatchTouchEvent(event);
    }

    private void setupFragments() {
        mPlayingQueueFragment = new PlayingQueueFragment();
        mRecommendationsFragment = new RecommendationFragment();

        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();

        ft.add(R.id.playingQueueContainer, mPlayingQueueFragment);
        ft.hide(mPlayingQueueFragment);
        ft.add(R.id.recommendationsContainer, mRecommendationsFragment);
        ft.commit();

    }

    private void setupAdapterAndViewPager() {
        mPlayerAdapter = new PlayerViewPagerAdapter(getSupportFragmentManager());
        mPlayerViewPager.setOffscreenPageLimit(10);
        mPlayerViewPager.setAdapter(mPlayerAdapter);
        mPlayerViewPager.addOnPageChangeListener(mPlayerActivityPresenter.getPageChangeListener());
        mPlayerViewPager.setCurrentItem(QueueRepository.getInstance().getCurrentIndex(), false);
    }

    @OnClick(R.id.playPauseBtn)
    public void onPlayPauseButtonAction() {
        mPlayerActivityPresenter.playPauseButtonClicked();
    }

    @OnClick(R.id.likeButton)
    public void onLikeButtonAction() {
        mPlayerActivityPresenter.likeButtonClicked();
    }

    @OnClick(R.id.dislikeButton)
    public void onDislikeButtonAction() {
        mPlayerActivityPresenter.dislikeButtonClicked();
    }

    @OnClick(R.id.nextButton)
    public void onNextButtonClicked() {
        mPlayerActivityPresenter.nextButtonClicked();
    }

    @OnClick(R.id.prevButton)
    public void onPrevButtonClicked() {
        mPlayerActivityPresenter.prevButtonClicked();
    }

    ///////////////////////////////////////////////////////////////////////////
    // Methods from PlayerActivityView interface
    ///////////////////////////////////////////////////////////////////////////

    @Override
    public void connectToSession(MediaSessionCompat.Token token) throws RemoteException {
        MediaControllerCompat mediaController = new MediaControllerCompat(this, token);
        setSupportMediaController(mediaController);

        mPlayerActivityPresenter.metadataChanged(mediaController.getMetadata());
        mPlayerActivityPresenter.playbackStateChanged(mediaController.getPlaybackState());
        mediaController.registerCallback(mPlayerActivityPresenter.getControllerCallback());
        mPlayerActivityPresenter.setTransportControls(mediaController.getTransportControls());

        updateProgress();
    }

    @Override
    public void updateMetadataViews(final MediaMetadataCompat metadata) {

        if (metadata == null) {
            return;
        }

        new Runnable() {

            @Override
            public void run() {

                int position = QueueRepository.getInstance().getCurrentIndex();
                LogHelper.d(LOG_TAG, " QueueIndexChangedEvent; QueuePosition: " + position +
                        " \nViewAdapterPosition: " + mPlayerViewPager.getCurrentItem());
                mPlayerViewPager.setCurrentItem(position, false);

                long duration = metadata.getLong(MediaMetadataCompat.METADATA_KEY_DURATION);
                mSeekbar.setMax((int) duration);

                mTotalDurationView.setText(DateUtils.formatElapsedTime(duration / 1000)); // insecs

                RatingCompat rating = metadata.getRating(MediaMetadataCompat.METADATA_KEY_USER_RATING);

                if (rating != null && rating.isRated()) {
                    if (rating.isThumbUp()) {
                        mLikeButton.setAlpha(1f);
                        mDislikeButton.setAlpha(0.4f);
                    } else {
                        mDislikeButton.setAlpha(1f);
                        mLikeButton.setAlpha(0.4f);
                    }
                } else {
                    mLikeButton.setAlpha(0.4f);
                    mDislikeButton.setAlpha(0.4f);
                }
            }
        }.run();
    }

    @Override
    public void updatePlaybackStateViews(final PlaybackStateCompat playbackState, boolean changeViews) {

        mLastPlaybackState = playbackState;

        if (!changeViews)
            return;

        new Runnable() {
            @Override
            public void run() {

                boolean isPlaying = (playbackState.getState() == PlaybackStateCompat.STATE_PLAYING);

                if (isPlaying) {
                    mPlayPauseBtn.playToPauseAnimation();
                    scheduleSeekbarUpdate();

                } else {
                    mPlayPauseBtn.pauseToPlayAnimation();
                    stopSeekbarUpdate();
                }
            }
        }.run();
    }

    @Override
    public void togglePlaylistVisibility() {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();

        ft.setCustomAnimations(android.R.anim.fade_in,
                android.R.anim.fade_out);

        if (mPlayingQueueFragment.isHidden()) {
            ft.show(mPlayingQueueFragment);

//            getSupportActionBar().setBackgroundDrawable(getDrawable(R.drawable.appbar_black_bg));
        } else {
            ft.hide(mPlayingQueueFragment);

//            getSupportActionBar().setBackgroundDrawable(getDrawable(R.drawable.appbar_trans_bg));
        }

        ft.commit();
    }

    private void updateProgress() {
        if (mLastPlaybackState == null) {
            return;
        }
        long currentPosition = mLastPlaybackState.getPosition();
        if (mLastPlaybackState.getState() != PlaybackStateCompat.STATE_PAUSED) {
            // Calculate the elapsed time between the last position update and now and unless
            // paused, we can assume (delta * speed) + current position is approximately the
            // latest position. This ensure that we do not repeatedly call the getPlaybackState()
            // on MediaControllerCompat.
            long timeDelta = SystemClock.elapsedRealtime() -
                    mLastPlaybackState.getLastPositionUpdateTime();
            currentPosition += (int) timeDelta * mLastPlaybackState.getPlaybackSpeed();
        }
        mSeekbar.setProgress((int) currentPosition);
    }

    private void scheduleSeekbarUpdate() {
        stopSeekbarUpdate();
        if (!mExecutorService.isShutdown()) {
            mScheduleFuture = mExecutorService.scheduleAtFixedRate(
                    new Runnable() {
                        @Override
                        public void run() {
                            mHandler.post(mUpdateProgressTask);
                        }
                    }, PROGRESS_UPDATE_INITIAL_INTERVAL,
                    PROGRESS_UPDATE_INTERNAL, TimeUnit.MILLISECONDS);
        }
    }

    private void stopSeekbarUpdate() {
        if (mScheduleFuture != null) {
            mScheduleFuture.cancel(false);
        }
    }
}
