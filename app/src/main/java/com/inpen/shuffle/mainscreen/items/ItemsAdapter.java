package com.inpen.shuffle.mainscreen.items;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;

import com.inpen.shuffle.customviews.ItemView;
import com.inpen.shuffle.model.repositories.SelectedItemsRepository;
import com.inpen.shuffle.utility.BaseItem;
import com.inpen.shuffle.utility.CustomTypes;
import com.inpen.shuffle.utility.LogHelper;

import java.util.ArrayList;
import java.util.List;

import static com.google.gson.internal.$Gson$Preconditions.checkNotNull;

/**
 * Created by Abhishek on 11/1/2016.
 */
public class ItemsAdapter extends RecyclerView.Adapter<ItemsAdapter.ItemViewHolder> implements Filterable {

    private static final String LOG_TAG = LogHelper.makeLogTag(ItemsAdapter.class);

    private final SelectedItemsRepository mSelectedItemsRepository;
    private List<Item> mItemList;
    private List<Item> mFilteredList;
    private CustomTypes.ItemType mItemType;

    public ItemsAdapter(@NonNull List<Item> itemList,
                        @NonNull SelectedItemsRepository selectedItemsRepository,
                        @NonNull CustomTypes.ItemType itemType) {
        mItemList = checkNotNull(itemList);
        mSelectedItemsRepository = checkNotNull(selectedItemsRepository);
        mItemType = checkNotNull(itemType);
        mFilteredList = mItemList;

        setHasStableIds(true);
    }

    public void replaceData(List<BaseItem> tasks) {
        setList(tasks);
        notifyDataSetChanged();
    }

    public void clearSelection() {
        notifyDataSetChanged();
    }

    private void selectItem(Item item) {
        // select item using selctedItemRepo
        mSelectedItemsRepository.addItem(item.id, mItemType);
    }

    private void deselectItem(Item item) {
        mSelectedItemsRepository.removeItem(item.id, mItemType);
    }

    @SuppressWarnings("unchecked")
    private void setList(List<BaseItem> tasks) {
        mItemList = (List<Item>) (Object) tasks;
        getFilter().filter("");
    }

    @Override
    public long getItemId(int position) {
        return mFilteredList.get(position).id.hashCode();
    }

    @Override
    public ItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        ItemView v = new ItemView(parent.getContext());

        return new ItemViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ItemViewHolder holder, int position) {
        holder.bind(mFilteredList.get(position));
    }

    @Override
    public void onViewRecycled(ItemViewHolder holder) {
        super.onViewRecycled(holder);
    }

    private boolean toggleSelectionForItem(Item item) {
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
        return mFilteredList.size();
    }

    @Override
    public Filter getFilter() {

        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {

                if (charSequence.length() == 0) {
                    mFilteredList = mItemList;
                } else {

                    ArrayList<Item> filteredList = new ArrayList<>();

                    for (Item item : mItemList) {

                        if (item.title.toLowerCase().contains(charSequence)) {
                            filteredList.add(item);
                        }
                    }

                    mFilteredList = filteredList;
                }

                FilterResults filterResults = new FilterResults();
                filterResults.values = mFilteredList;
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                mFilteredList = (ArrayList<Item>) filterResults.values;
                notifyDataSetChanged();
            }
        };
    }

    class ItemViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        ItemView mItemView;

        ItemViewHolder(View itemView) {
            super(itemView);
            mItemView = (ItemView) itemView;
        }

        @Override
        public void onClick(View view) {

            ItemView itemView = (ItemView) view;

//            LogHelper.v(LOG_TAG, "view with itemId: " + itemView.getItem().id + " clicked! ");

            boolean select = toggleSelectionForItem(itemView.getItem());
            itemView.setSelected(select);
        }

        public void bind(Item item) {
            mItemView.setItem(item, mSelectedItemsRepository.hasItem(item.id, mItemType));
            mItemView.setOnClickListener(this);
        }
    }
}
