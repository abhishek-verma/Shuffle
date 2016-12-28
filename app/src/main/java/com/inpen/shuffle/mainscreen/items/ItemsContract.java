package com.inpen.shuffle.mainscreen.items;

import android.support.annotation.Nullable;

import com.inpen.shuffle.model.database.MediaContract;

import java.util.List;

/**
 * Created by Abhishek on 12/21/2016.
 */

class ItemsContract {


    interface ItemsView {

        void setProgressIndicator(boolean active);

        void showItems(@Nullable List<Item> itemList);

        void clearSelection();

    }

    interface ItemsFragmentListener {

        String[] ALBUMS_QUERY_CURSOR_COLUMNS = {
                MediaContract.MediaEntry.TABLE_NAME + "." + MediaContract.MediaEntry.COLUMN_ALBUM_KEY,
                MediaContract.MediaEntry.TABLE_NAME + "." + MediaContract.MediaEntry.COLUMN_ALBUM,
                MediaContract.MediaEntry.TABLE_NAME + "." + MediaContract.MediaEntry.COLUMN_ALBUM_ART,
                "COUNT(*)"
        };

        String[] ARTISTS_QUERY_CURSOR_COLUMNS = {
                MediaContract.MediaEntry.TABLE_NAME + "." + MediaContract.MediaEntry.COLUMN_ARTIST_KEY,
                MediaContract.MediaEntry.TABLE_NAME + "." + MediaContract.MediaEntry.COLUMN_ARTIST,
                MediaContract.MediaEntry.TABLE_NAME + "." + MediaContract.MediaEntry.COLUMN_ALBUM_ART,
                "COUNT(*)"
        };

        String[] FOLDERS_QUERY_CURSOR_COLUMNS = {
                MediaContract.MediaEntry.TABLE_NAME + "." + MediaContract.MediaEntry.COLUMN_FOLDER_PATH,
                MediaContract.MediaEntry.TABLE_NAME + "." + MediaContract.MediaEntry.COLUMN_ALBUM_ART,
                "COUNT(*)"
        };

        String[] PLAYLISTS_QUERY_CURSOR_COLUMNS = {
                MediaContract.PlaylistsEntry.TABLE_NAME + "." + MediaContract.PlaylistsEntry.COLUMN_PLAYLIST_NAME,
                MediaContract.MediaEntry.TABLE_NAME + "." + MediaContract.MediaEntry.COLUMN_ALBUM_ART,
                "COUNT(*)"
        };

        void initialize();

        void itemLongPressed(String itemId);

        void itemClicked(Item item);

        void stop();
    }

}
