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
    private static final String TAG = LogHelper.makeLogTag(SelectedItemsRepository.class);

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

    private void checkAndSetItemType(CustomTypes.ItemType itemType) {
        if (mItemType == null || !itemType.equals(mItemType)) {
            clearItems(false);
            mItemType = itemType;
            EventBus.getDefault().post(new ItemTypeChangedEvent());
        }
    }

    public void addItem(String id, CustomTypes.ItemType itemType) {
        checkAndSetItemType(itemType);

        if (isEmpty())
            EventBus.getDefault().post(new RepositoryEmptyStateChangedEvent(false)); // repo is GOING TO BE non empty

        mSelectedItemIdList.add(id);

        LogHelper.d(TAG, "selected item size: " + mSelectedItemIdList.size());
    }

    public void addItems(List<String> idList, CustomTypes.ItemType mItemType) {
        checkAndSetItemType(mItemType);

        if (isEmpty())
            EventBus.getDefault().post(new RepositoryEmptyStateChangedEvent(false)); // repo is GOING TO BE non empty

        mSelectedItemIdList.addAll(idList);
    }

    public void removeItem(String id) {
        boolean wasEmpty = isEmpty();

        mSelectedItemIdList.remove(id);


        if (!wasEmpty && isEmpty())
            EventBus.getDefault().post(new RepositoryEmptyStateChangedEvent(true)); // repo is emptied
    }

    public void removeItems(List<String> idList) {
        boolean wasEmpty = isEmpty();

        mSelectedItemIdList.removeAll(idList);

        if (!wasEmpty && isEmpty())
            EventBus.getDefault().post(new RepositoryEmptyStateChangedEvent(true)); // repo is emptied
    }

    /**
     * @param notify whether to notify the empty listeners and itemtypechanged listeners,
     *               usually false when called from ,
     *               coz they are called by add functions and checkandchangetype which notify themselves
     */
    public void clearItems(boolean notify) {
        if (notify && !isEmpty())
            EventBus.getDefault().post(new RepositoryEmptyStateChangedEvent(true)); // repo is emptied

        mSelectedItemIdList.clear();

        mItemType = null;

        if (notify) {
            EventBus.getDefault().post(new ItemTypeChangedEvent());
        }
    }

    public boolean hasItem(String id) {
        return mSelectedItemIdList.contains(id);
    }

    public boolean isEmpty() {
        return mSelectedItemIdList == null || mSelectedItemIdList.size() == 0;
    }


    public static class ItemTypeChangedEvent {
    }

    public static class RepositoryEmptyStateChangedEvent {

        public final boolean isEmpty;

        public RepositoryEmptyStateChangedEvent(boolean empty) {
            isEmpty = empty;
        }
    }
}
