package com.inpen.shuffle.customviews;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.inpen.shuffle.R;
import com.inpen.shuffle.playerscreen.playingqueue.PlayingQueueItem;
import com.inpen.shuffle.utility.CustomTypes;
import com.inpen.shuffle.utility.LogHelper;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Abhishek on 3/26/2017.
 */

public class QueueItemView extends FrameLayout {
    private static final String TAG = LogHelper.makeLogTag(QueueItemView.class);
    @BindView(R.id.albumArt)
    public FixedRatioImageView mAlbumArtView;
    @BindView(R.id.mask)
    public FixedRatioImageView mMaskView;
    @BindView(R.id.itemTitle)
    public TextView mTitleTextView;
    private PlayingQueueItem mQueueItem;

    public QueueItemView(@NonNull Context context) {
        super(context);

        initView(context);
    }

    private void initView(Context context) {
        View.inflate(context, R.layout.queue_item_view_layout, this);

        ButterKnife.bind(this);
    }

    public PlayingQueueItem getItem() {
        return mQueueItem;
    }

    public void setItem(PlayingQueueItem item, @CustomTypes.PlayingQueueItemPlayingState int playingState) {

        mQueueItem = item;


        if (mTitleTextView != null)
            mTitleTextView.setText(mQueueItem.title);

        if (mAlbumArtView != null) {
            Glide.with(getContext())
                    .load(mQueueItem.imagePath)
                    .listener(new RequestListener<String, GlideDrawable>() {
                        @Override
                        public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {
                            mAlbumArtView.setImageDrawable(getResources().getDrawable(R.drawable.ic_loading_circle));
                            return true;
                        }

                        @Override
                        public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                            return false;
                        }
                    })
                    .into(mAlbumArtView);
        }

        setPlayedState(playingState);
    }

    public void setPlayedState(@CustomTypes.PlayingQueueItemPlayingState int playingState) {

    }
}

