package com.inpen.shuffle.mainscreen.items;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import com.inpen.shuffle.customviews.SongItemView;
import com.inpen.shuffle.model.repositories.SelectedItemsRepository;
import com.inpen.shuffle.utility.CustomTypes;
import com.inpen.shuffle.utility.LogHelper;

import java.util.List;

import static com.google.gson.internal.$Gson$Preconditions.checkNotNull;

/**
 * Created by Abhishek on 11/1/2016.
 */
public class SongItemsAdapter extends RecyclerView.Adapter<SongItemsAdapter.ItemViewHolder> {

    private static final String LOG_TAG = LogHelper.makeLogTag(SongItemsAdapter.class);

    private final SelectedItemsRepository mSelectedItemsRepository;
    private List<SongItem> mItemList;
    private CustomTypes.ItemType mItemType;

    public SongItemsAdapter(@NonNull List<SongItem> itemList,
                            @NonNull SelectedItemsRepository selectedItemsRepository,
                            @NonNull CustomTypes.ItemType itemType) {
        mItemList = checkNotNull(itemList);
        mSelectedItemsRepository = checkNotNull(selectedItemsRepository);
        mItemType = checkNotNull(itemType);

        setHasStableIds(true);
    }

    public void replaceData(List<BaseItem> tasks) {
        setList(tasks);
        notifyDataSetChanged();
    }

    public void clearSelection() {
        notifyDataSetChanged();
    }

    private void selectItem(SongItem item) {
        // select item using selctedItemRepo
        mSelectedItemsRepository.addItem(item.id, mItemType);
    }

    private void deselectItem(SongItem item) {
        mSelectedItemsRepository.removeItem(item.id, mItemType);
    }

    @SuppressWarnings("unchecked")
    private void setList(List<BaseItem> tasks) {
        mItemList = (List<SongItem>) (Object) tasks;

    }

    @Override
    public long getItemId(int position) {
        return mItemList.get(position).id.hashCode();
    }

    @Override
    public ItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        SongItemView v = new SongItemView(parent.getContext());

        return new ItemViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ItemViewHolder holder, int position) {
        holder.bind(mItemList.get(position));
    }

    @Override
    public void onViewRecycled(ItemViewHolder holder) {
        super.onViewRecycled(holder);
    }

    private boolean toggleSelectionForItem(SongItem item) {
//        LogHelper.v(LOG_TAG, "toggleSelectionForItem- title: " + item.title);

        boolean selected = mSelectedItemsRepository.hasItem(item.id, mItemType);

        if (selected) {
            deselectItem(item);
        } else {
            selectItem(item);
        }

        return !selected; // new selected
    }

    @Override
    public int getItemCount() {
        return mItemList.size();
    }


    public class ItemViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        SongItemView mItemView;

        public ItemViewHolder(View itemView) {
            super(itemView);
            mItemView = (SongItemView) itemView;
        }

        @Override
        public void onClick(View view) {

            SongItemView itemView = (SongItemView) view;

//            LogHelper.v(LOG_TAG, "view with itemId: " + itemView.getItem().id + " clicked! ");

            boolean select = toggleSelectionForItem(itemView.getItem());
            itemView.setSelected(select);
        }

        public void bind(SongItem item) {
            mItemView.setItem(item, mSelectedItemsRepository.hasItem(item.id, mItemType));
            mItemView.setOnClickListener(this);
        }
    }
}
