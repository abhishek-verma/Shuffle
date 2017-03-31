package com.inpen.shuffle.model.repositories;

import com.inpen.shuffle.utility.CustomTypes;
import com.inpen.shuffle.utility.LogHelper;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Abhishek on 12/16/2016.
 */

public class SelectedItemsRepository {
    private static final String TAG = LogHelper.makeLogTag(SelectedItemsRepository.class);

    public static SelectedItemsRepository mSelectedItemsRepositoryInstance;

    Map<CustomTypes.ItemType, List<String>> mSelectedItemsListMap = new HashMap<>();

    private SelectedItemsRepository() {
        mSelectedItemsListMap.put(CustomTypes.ItemType.SONG, new ArrayList<String>());
        mSelectedItemsListMap.put(CustomTypes.ItemType.ALBUM_KEY, new ArrayList<String>());
        mSelectedItemsListMap.put(CustomTypes.ItemType.ARTIST_KEY, new ArrayList<String>());
        mSelectedItemsListMap.put(CustomTypes.ItemType.FOLDER, new ArrayList<String>());
        mSelectedItemsListMap.put(CustomTypes.ItemType.PLAYLIST, new ArrayList<String>());
    }

    public static SelectedItemsRepository getInstance() {
        if (mSelectedItemsRepositoryInstance == null)
            mSelectedItemsRepositoryInstance = new SelectedItemsRepository();

        return mSelectedItemsRepositoryInstance;
    }

    public List<String> getSelectedItemList(CustomTypes.ItemType itemType) {
        return mSelectedItemsListMap.get(itemType);
    }

    public void addItem(String id, CustomTypes.ItemType itemType) {
        if (isEmpty())
            EventBus.getDefault().post(new RepositoryEmptyStateChangedEvent(false)); // repo is GOING TO BE non empty

        mSelectedItemsListMap.get(itemType).add(id);

        LogHelper.d(TAG, "selected item size: " + getSelectedItemCount());

        EventBus.getDefault().post(new SelectedItemCountChanged());
    }

    public void removeItem(String id, CustomTypes.ItemType itemType) {
        boolean wasEmpty = isEmpty();

        mSelectedItemsListMap.get(itemType).remove(id);

        if (!wasEmpty && isEmpty())
            EventBus.getDefault().post(new RepositoryEmptyStateChangedEvent(true)); // repo is emptied

        EventBus.getDefault().post(new SelectedItemCountChanged());
    }

    public void removeItems(List<String> idList, CustomTypes.ItemType itemType) {
        boolean wasEmpty = isEmpty();

        mSelectedItemsListMap.get(itemType).removeAll(idList);

        if (!wasEmpty && isEmpty())
            EventBus.getDefault().post(new RepositoryEmptyStateChangedEvent(true)); // repo is emptied

        EventBus.getDefault().post(new SelectedItemCountChanged());
    }

    public void clearItems() {
        if (!isEmpty())
            EventBus.getDefault().post(new RepositoryEmptyStateChangedEvent(true)); // repo is emptied

        for (List<String> itemList : mSelectedItemsListMap.values())
            itemList.clear();

        EventBus.getDefault().post(new SelectedItemCountChanged());
    }

    public boolean hasItem(String id, CustomTypes.ItemType itemType) {
        return mSelectedItemsListMap.get(itemType).contains(id);
    }

    public boolean isEmpty() {
        if (mSelectedItemsListMap == null) {
            return true;
        }

        for (List<String> itemList : mSelectedItemsListMap.values())
            if (!itemList.isEmpty())
                return false;

        return true;
    }

    public int getSelectedItemCount() {
        int size = 0;

        for (List<String> itemList : mSelectedItemsListMap.values())
            size += itemList.size();

        return size;
    }

    public static class RepositoryEmptyStateChangedEvent {

        public final boolean isEmpty;

        public RepositoryEmptyStateChangedEvent(boolean empty) {
            isEmpty = empty;
        }
    }

    public static class SelectedItemCountChanged {
    }
}
