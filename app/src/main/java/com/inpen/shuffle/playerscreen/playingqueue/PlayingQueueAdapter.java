package com.inpen.shuffle.playerscreen.playingqueue;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import com.inpen.shuffle.customviews.QueueItemView;
import com.inpen.shuffle.utility.CustomTypes;
import com.inpen.shuffle.utility.LogHelper;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

import static com.google.gson.internal.$Gson$Preconditions.checkNotNull;

/**
 * Created by Abhishek on 3/26/2017.
 */

public class PlayingQueueAdapter
        extends RecyclerView.Adapter<PlayingQueueAdapter.QueueItemViewHolder>
        implements SimpleItemTouchHelperCallback.ItemTouchHelperAdapter {

    private static final String LOG_TAG = LogHelper.makeLogTag(PlayingQueueAdapter.class);

    private List<PlayingQueueItem> mPlayingQueueItemList;
    private int mCurrentIndex;

    public PlayingQueueAdapter(@NonNull List<PlayingQueueItem> itemList) {
        mPlayingQueueItemList = checkNotNull(itemList);

        setHasStableIds(true);
    }

    public void updatePlayingIndex(int index) {
        mCurrentIndex = index;
        notifyDataSetChanged();
    }

    public void updateData(List<PlayingQueueItem> tasks) {
        setList(tasks);
        notifyDataSetChanged();
    }

    private void setList(List<PlayingQueueItem> tasks) {
        mPlayingQueueItemList = checkNotNull(tasks);
    }

    @Override
    public long getItemId(int position) {
        return mPlayingQueueItemList.get(position).id.hashCode();
    }

    @Override
    public QueueItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        QueueItemView view = new QueueItemView(parent.getContext());

        return new QueueItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final QueueItemViewHolder holder, int position) {
        @CustomTypes.PlayingQueueItemPlayingState int playingState;

        playingState = (mCurrentIndex <= position) ?
                (mCurrentIndex == position) ? CustomTypes.PlayingQueueItemPlayingState.PLAYING
                        : CustomTypes.PlayingQueueItemPlayingState.PLAYED
                : CustomTypes.PlayingQueueItemPlayingState.UNPLAYED;

        holder.mQueueItemView.setItem(mPlayingQueueItemList.get(position), playingState);

        holder.mQueueItemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EventBus.getDefault()
                        .post(new PlayingQueueEvent(CustomTypes.PlayingQueueEventType.CLICKED,
                                holder.getAdapterPosition()));
            }
        });
    }

    @Override
    public int getItemCount() {
        return mPlayingQueueItemList.size();
    }

    @Override
    public void onItemMove(int fromPosition, int toPosition) {

    }

    @Override
    public void onItemDismiss(int position) {
        EventBus.getDefault().post(new PlayingQueueEvent(CustomTypes.PlayingQueueEventType.SWIPED, position));
        mPlayingQueueItemList.remove(position);
        notifyItemRemoved(position);
    }

    class QueueItemViewHolder
            extends RecyclerView.ViewHolder {

        QueueItemView mQueueItemView;

        QueueItemViewHolder(View itemView) {
            super(itemView);

            mQueueItemView = (QueueItemView) itemView;
        }

    }

    public class PlayingQueueEvent {

        public
        @CustomTypes.PlayingQueueEventType
        int eventType;
        // position array to store the initial and final position when moved
        // in other cases like clicked, swiped position has a single int, the item position
        //when item moved positions[0] = initialPos, positions[1] = finalPos
        public int[] positions;

        public PlayingQueueEvent(@CustomTypes.PlayingQueueEventType int eventType, int... positions) {
            this.eventType = eventType;
            this.positions = positions;
        }
    }
}
