package com.inpen.shuffle.customviews;

import android.content.Context;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.inpen.shuffle.R;
import com.inpen.shuffle.mainscreen.items.Item;
import com.inpen.shuffle.utility.LogHelper;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Abhishek on 11/2/2016.
 */

public class ItemView extends FrameLayout {
    private static final String TAG = LogHelper.makeLogTag(ItemView.class);
    private static final int ANIMATION_DURATION = 275;

    @BindView(R.id.albumArt)
    public FixedRatioImageView mAlbumArtView;
    @BindView(R.id.mask)
    public FixedRatioImageView mMaskView;
    @BindView(R.id.itemTitle)
    public TextView mTitleTextView;
    @BindView(R.id.itemSongCount)
    public TextView mCountView;
    private Item mItem;
    private boolean mIsSelected = false;

    public ItemView(Context context) {
        super(context);

        initView(context);
    }

    private void initView(Context context) {
        View.inflate(context, R.layout.item_view_layout, this);

        ButterKnife.bind(this);
    }

    public Item getItem() {
        return mItem;
    }

    public void setItem(Item item, boolean selected) {
        mItem = item;

        if (mTitleTextView != null)
            mTitleTextView.setText(mItem.title);

        if (mCountView != null) {
            mCountView.setText(getResources().getQuantityString(R.plurals.song_count, mItem.count, mItem.count));
        }

        if (mAlbumArtView != null) {
            Glide.with(getContext())
                    .load(mItem.imagePath)
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

//        if (mIsSelected) {
//            mAlbumArtView.setAlpha(0.6f);
//            mMaskView.setAlpha(1f);
//        } else {
//            mAlbumArtView.setAlpha(1f);
//            mMaskView.setAlpha(0f);
//        }

        setSelected(selected);
    }

    public void setSelected(boolean select) {
        if (mIsSelected == select) {
            return;
        }

        mIsSelected = select;

        if (mIsSelected) {
            mMaskView.animate()
                    .alpha(1f)
                    .withLayer()
                    .setDuration(ANIMATION_DURATION)
                    .setInterpolator(new AccelerateInterpolator())
                    .start();

            mAlbumArtView.animate()
                    .alpha(0.6f)
                    .withLayer()
                    .setDuration(ANIMATION_DURATION)
                    .setInterpolator(new AccelerateInterpolator())
                    .start();

        } else {
            mMaskView.animate()
                    .alpha(0f)
                    .setDuration(ANIMATION_DURATION)
                    .setInterpolator(new AccelerateInterpolator())
                    .withLayer()
                    .start();

            mAlbumArtView.animate()
                    .alpha(1f)
                    .setDuration(ANIMATION_DURATION)
                    .setInterpolator(new AccelerateInterpolator())
                    .withLayer()
                    .start();

        }
    }
}
