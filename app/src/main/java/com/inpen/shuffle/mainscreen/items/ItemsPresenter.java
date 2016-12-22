package com.inpen.shuffle.mainscreen.items;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;

import com.inpen.shuffle.model.database.MediaContract;
import com.inpen.shuffle.model.repositories.SelectedItemsRepository;
import com.inpen.shuffle.utility.CustomTypes;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.google.gson.internal.$Gson$Preconditions.checkNotNull;

/**
 * Created by Abhishek on 12/20/2016.
 */

public class ItemsPresenter
        implements ItemsContract.ItemsFragmentListener,
        LoaderManager.LoaderCallbacks<Cursor> {

    private LoaderManager mLoaderManager;
    private ItemsContract.ItemsView mItemsView;
    private Context mContext;
    private CustomTypes.ItemType mItemType;
    private SelectedItemsRepository mSelectedItemsRepository;

    private boolean active = false;

    public ItemsPresenter(@NonNull LoaderManager loaderManager,
                          @NonNull Context context,
                          @NonNull ItemsContract.ItemsView itemsView,
                          @NonNull CustomTypes.ItemType itemType,
                          @NonNull SelectedItemsRepository selectedItemsRepository) {
        mLoaderManager = checkNotNull(loaderManager);
        mItemsView = checkNotNull(itemsView);
        mContext = checkNotNull(context);
        mItemType = checkNotNull(itemType);
        mSelectedItemsRepository = checkNotNull(selectedItemsRepository);

        EventBus.getDefault().register(this);
    }

    @Override
    public void initialize() {
        mSelectedItemsRepository = SelectedItemsRepository.getInstance();


        // Prepare the loader.  Either re-connect with an existing one,
        // or start a new one.
        mLoaderManager.initLoader(0, null, this);
    }

    @Override
    public void itemLongPressed(String itemId) {

    }

    @Override
    public void itemClicked(Item item) {

    }

    @Override
    public void stop() {

    }

    /**
     * Called when {@link SelectedItemsRepository#mItemType} from {@link SelectedItemsRepository} changes.
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onItemTypeChangedEvent(SelectedItemsRepository.ItemTypeChangedEvent obs) {
        if (mSelectedItemsRepository.getItemType().equals(mItemType)) {
            active = true;
        } else if (active && !mSelectedItemsRepository.getItemType().equals(mItemType)) {
            mItemsView.clearSelection();
            active = false;
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        mItemsView.setProgressIndicator(true);

        String selection;
        switch (mItemType) {
            case ALBUM_KEY:
                selection = MediaContract.MediaEntry.COLUMN_ALBUM_KEY + " NOT NULL GROUP BY " + MediaContract.MediaEntry.COLUMN_ALBUM_KEY;
                return new CursorLoader(mContext,
                        MediaContract.MediaEntry.CONTENT_URI,
                        ALBUMS_QUERY_CURSOR_COLUMNS,
                        selection,
                        null, null);
            case ARTIST_KEY:
                selection = MediaContract.MediaEntry.COLUMN_ARTIST_KEY + " NOT NULL GROUP BY " + MediaContract.MediaEntry.COLUMN_ARTIST_KEY;
                return new CursorLoader(mContext,
                        MediaContract.MediaEntry.CONTENT_URI,
                        ARTISTS_QUERY_CURSOR_COLUMNS,
                        selection,
                        null, null);
            case FOLDER:
                selection = MediaContract.MediaEntry.COLUMN_FOLDER_PATH + " NOT NULL GROUP BY " + MediaContract.MediaEntry.COLUMN_FOLDER_PATH;
                return new CursorLoader(mContext,
                        MediaContract.MediaEntry.CONTENT_URI,
                        FOLDERS_QUERY_CURSOR_COLUMNS,
                        selection,
                        null, null);
            case PLAYLIST:
                selection = MediaContract.PlaylistsEntry.COLUMN_PLAYLIST_NAME + " NOT NULL) GROUP BY (" + MediaContract.PlaylistsEntry.COLUMN_PLAYLIST_NAME;
                return new CursorLoader(mContext,
                        MediaContract.PlaylistsEntry.CONTENT_URI,
                        PLAYLISTS_QUERY_CURSOR_COLUMNS,
                        selection,
                        null, null);

        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

        if (data == null || !data.moveToFirst()) {
            mItemsView.showItems(new ArrayList<Item>());
            return;
        }

        data.moveToFirst();

        List<Item> itemList = new ArrayList<>();

        switch (mItemType) {
            case ALBUM_KEY:
                do {
                    Item item = new Item(
                            data.getString(0),//album id
                            data.getString(1),//album title
                            data.getString(2),//Album art
                            data.getInt(3));
                    itemList.add(item);
                } while (data.moveToNext());
                break;
            case ARTIST_KEY:
                do {
                    Item item = new Item(
                            data.getString(0),//artist id
                            data.getString(1),//artist title
                            data.getString(2),//Album art
                            data.getInt(3));
                    itemList.add(item);
                } while (data.moveToNext());
                break;
            case FOLDER:
                do {
                    Item item = new Item(
                            data.getString(0),//folder path
                            MediaContract.MediaEntry.getSongFolderFromFolderPath(data.getString(0)),//folder name from folder path
                            data.getString(1),//Album art
                            data.getInt(2));
                    itemList.add(item);
                } while (data.moveToNext());
                break;
            case PLAYLIST:
                do {
                    Item item = new Item(
                            data.getString(0),//playlist name
                            data.getString(0),//playlist name
                            data.getString(1),//Album art
                            data.getInt(2));
                    itemList.add(item);
                } while (data.moveToNext());
                break;
        }

        Collections.sort(itemList);

        mItemsView.showItems(itemList);

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }
}
