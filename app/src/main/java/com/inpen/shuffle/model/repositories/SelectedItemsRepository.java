package com.inpen.shuffle.model.repositories;

import com.inpen.shuffle.utility.CustomTypes;
import com.inpen.shuffle.utility.LogHelper;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Abhishek on 12/16/2016.
 */

public class SelectedItemsRepository {

    public static SelectedItemsRepository mSelectedItemsRepositoryInstance;
    private CustomTypes.ItemType mItemType;
    private List<String> mSelectedItemIdList = new ArrayList<>();

    public static SelectedItemsRepository getInstance() {
        if (mSelectedItemsRepositoryInstance == null)
            mSelectedItemsRepositoryInstance = new SelectedItemsRepository();

        return mSelectedItemsRepositoryInstance;
    }

    public CustomTypes.ItemType getItemType() {
        return mItemType;
    }

    public List<String> getSelectedItemIdList() {
        return mSelectedItemIdList;
    }

    public void checkAndSetItemType(CustomTypes.ItemType itemType) {
        if (mItemType == null || !itemType.equals(mItemType)) {
            clearItems();
            mItemType = itemType;
            EventBus.getDefault().post(new ItemTypeChangedEvent());
        }
    }

    public void addItem(String id, CustomTypes.ItemType itemType) {
        checkAndSetItemType(itemType);
        mSelectedItemIdList.add(id);

        LogHelper.d("temp", "selectedItemCount: " + mSelectedItemIdList.size() + " contents: " + mSelectedItemIdList.toString());
    }

    public void addItems(List<String> idList, CustomTypes.ItemType mItemType) {
        checkAndSetItemType(mItemType);
        mSelectedItemIdList.addAll(idList);
    }

    public void removeItem(String id) {
        mSelectedItemIdList.remove(id);
    }

    public void removeItems(List<String> idList) {
        mSelectedItemIdList.removeAll(idList);
    }

    public void clearItems() {
        mSelectedItemIdList.clear();
        mItemType = null;
    }

    public boolean hasItem(String id) {
        return mSelectedItemIdList.contains(id);
    }


    public static class ItemTypeChangedEvent {
    }
}
