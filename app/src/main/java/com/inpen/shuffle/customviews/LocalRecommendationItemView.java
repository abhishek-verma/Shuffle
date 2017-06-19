package com.inpen.shuffle.customviews;

import android.content.Context;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.inpen.shuffle.R;
import com.inpen.shuffle.mainscreen.items.Item;
import com.inpen.shuffle.playerscreen.recommendation.LocalRecommendationItem;
import com.inpen.shuffle.utility.LogHelper;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Abhishek on 11/2/2016.
 */

public class LocalRecommendationItemView extends FrameLayout {
    private static final String TAG = LogHelper.makeLogTag(LocalRecommendationItemView.class);

    @BindView(R.id.albumArt)
    public FixedRatioImageView mAlbumArtView;
    @BindView(R.id.itemTitle)
    public TextView mTitleTextView;
    @BindView(R.id.songArtist)
    public TextView mArtistTextView;
    private LocalRecommendationItem mItem;

    public LocalRecommendationItemView(Context context) {
        super(context);

        initView(context);
    }

    private void initView(Context context) {
        View.inflate(context, R.layout.local_recommendation_item_view_layout, this);

        ButterKnife.bind(this);
    }

    public LocalRecommendationItem getItem() {
        return mItem;
    }

    public void setItem(LocalRecommendationItem item) {
        mItem = item;

        if (mTitleTextView != null)
            mTitleTextView.setText(mItem.title);

        if (mArtistTextView != null) {
            mArtistTextView.setText(mItem.artist);
        }

        if (mAlbumArtView != null) {
            Glide.with(getContext())
                    .load(mItem.imagePath)
                    .error(getResources().getDrawable(R.drawable.shuffle_bg))
                    .into(mAlbumArtView);
        }

    }
}
