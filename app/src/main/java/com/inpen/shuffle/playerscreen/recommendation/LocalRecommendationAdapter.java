package com.inpen.shuffle.playerscreen.recommendation;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import com.inpen.shuffle.customviews.LocalRecommendationItemView;
import com.inpen.shuffle.playerscreen.playingqueue.PlayingQueueAdapter;
import com.inpen.shuffle.playerscreen.playingqueue.PlayingQueueItem;
import com.inpen.shuffle.utility.CustomTypes;
import com.inpen.shuffle.utility.LogHelper;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

import static com.google.gson.internal.$Gson$Preconditions.checkNotNull;

/**
 * Created by Abhishek on 3/26/2017.
 */

public class LocalRecommendationAdapter
        extends RecyclerView.Adapter<LocalRecommendationAdapter.LocalRecommendationItemViewHolder> {

    private static final String LOG_TAG = LogHelper.makeLogTag(LocalRecommendationAdapter.class);

    private List<LocalRecommendationItem> mItemList;
    private View.OnClickListener mOnClickListener;

    public LocalRecommendationAdapter(@NonNull List<LocalRecommendationItem> itemList, View.OnClickListener localRecommendationClickListener) {
        mItemList = checkNotNull(itemList);
        mOnClickListener = localRecommendationClickListener;
        setHasStableIds(true);
    }

    public void updateData(List<LocalRecommendationItem> tasks) {
        setList(tasks);
        notifyDataSetChanged();
    }

    private void setList(List<LocalRecommendationItem> tasks) {
        mItemList = checkNotNull(tasks);
    }

    @Override
    public long getItemId(int position) {
        return mItemList.get(position).id.hashCode();
    }

    @Override
    public LocalRecommendationItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LocalRecommendationItemView view = new LocalRecommendationItemView(parent.getContext());

        return new LocalRecommendationItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(LocalRecommendationItemViewHolder holder, int position) {
        holder.mItemView.setItem(mItemList.get(position));
        holder.mItemView.setOnClickListener(mOnClickListener);
    }

    @Override
    public int getItemCount() {
        return mItemList.size();
    }

    class LocalRecommendationItemViewHolder
            extends RecyclerView.ViewHolder {

        LocalRecommendationItemView mItemView;

        LocalRecommendationItemViewHolder(View itemView) {
            super(itemView);

            mItemView = (LocalRecommendationItemView) itemView;
        }

    }

}
