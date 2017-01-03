package com.inpen.shuffle.model.repositories;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.RatingCompat;

import com.inpen.shuffle.model.MutableMediaMetadata;
import com.inpen.shuffle.model.database.MediaContract;
import com.inpen.shuffle.utility.CustomTypes;
import com.inpen.shuffle.utility.StaticStrings;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Abhishek on 12/14/2016.
 */

public class SongsRepository {

    ///////////////////////////////////////////////////////////////////////////
    // Regular fields and methods
    ///////////////////////////////////////////////////////////////////////////

    private final static int COL_INDEX_ID = 0;
    private final static int COL_INDEX_SONG_ID = 1;
    private final static int COL_INDEX_PATH = 2;
    private final static int COL_INDEX_TITLE = 3;
    private final static int COL_INDEX_ALBUM = 4;
    private final static int COL_INDEX_ALBUM_KEY = 5;
    private final static int COL_INDEX_ARTIST = 6;
    private final static int COL_INDEX_ARTIST_KEY = 7;
    private final static int COL_INDEX_DURATION = 8;
    private final static int COL_INDEX_FOLDER_PATH = 9;
    private final static int COL_INDEX_ALBUM_ART = 10;

    private static final String[] projection = {
            MediaContract.MediaEntry.TABLE_NAME + "." + MediaContract.MediaEntry._ID,
            MediaContract.MediaEntry.COLUMN_SONG_ID,
            MediaContract.MediaEntry.COLUMN_PATH,
            MediaContract.MediaEntry.COLUMN_TITLE,
            MediaContract.MediaEntry.COLUMN_ALBUM,
            MediaContract.MediaEntry.COLUMN_ALBUM_KEY,
            MediaContract.MediaEntry.COLUMN_ARTIST,
            MediaContract.MediaEntry.COLUMN_ARTIST_KEY,
            MediaContract.MediaEntry.COLUMN_DURATION,
            MediaContract.MediaEntry.COLUMN_FOLDER_PATH,
            MediaContract.MediaEntry.COLUMN_ALBUM_ART
    };
    private Context mContext;


    public SongsRepository(Context context) {
        mContext = context;
    }

    ///////////////////////////////////////////////////////////////////////////
    // public methods
    ///////////////////////////////////////////////////////////////////////////

    private static String getStringFromSelectorItems(List<String> selectors) {
        StringBuffer s = new StringBuffer();


        for (int i = 0; i < selectors.size(); i++) {
            if (i != 0) s.append(",");

            s.append('"').append(selectors.get(i)).append('"');
        }

        return s.toString();
    }

    public void removeAllSongsFromDatabase() {
    }

    public void removeSong(MediaMetadataCompat mediaMetadata) {

    }

    public List<String> getSongsIds() {

        Cursor songsDataCursor = mContext.getContentResolver().query(MediaContract.MediaEntry.CONTENT_URI,
                new String[]{MediaContract.MediaEntry.COLUMN_SONG_ID},
                null, null, null);

        List<String> songIdList = new ArrayList<>();

        if (songsDataCursor != null && songsDataCursor.moveToFirst()) {
            //noinspection ResourceType
            songIdList.add(songsDataCursor.getString(0));
            songsDataCursor.close();
        }

        return songIdList;
    }

    public List<String> getSongsIdsByFilter(List<String> filterItems, CustomTypes.ItemType itemType) {

        Cursor songsDataCursor = null;

        switch (itemType) {
            case ALBUM_KEY:
                songsDataCursor = mContext.getContentResolver()
                        .query(MediaContract.MediaEntry.CONTENT_URI,
                                new String[]{MediaContract.MediaEntry.COLUMN_SONG_ID},
                                MediaContract.MediaEntry.COLUMN_ALBUM_ART
                                        + " IN ("
                                        + getStringFromSelectorItems(filterItems)
                                        + ")",
                                null, null);

                break;
            case ARTIST_KEY:
                songsDataCursor = mContext.getContentResolver()
                        .query(MediaContract.MediaEntry.CONTENT_URI,
                                new String[]{MediaContract.MediaEntry.COLUMN_SONG_ID},
                                MediaContract.MediaEntry.COLUMN_ARTIST_KEY
                                        + " IN ("
                                        + getStringFromSelectorItems(filterItems)
                                        + ")",
                                null, null);
                break;
            case FOLDER:
                songsDataCursor = mContext.getContentResolver()
                        .query(MediaContract.MediaEntry.CONTENT_URI,
                                new String[]{MediaContract.MediaEntry.COLUMN_SONG_ID},
                                MediaContract.MediaEntry.COLUMN_PATH
                                        + " IN ("
                                        + getStringFromSelectorItems(filterItems)
                                        + ")",
                                null, null);
                break;
            case PLAYLIST:
                songsDataCursor = mContext.getContentResolver()
                        .query(
                                MediaContract.PlaylistsEntry.CONTENT_URI,
                                new String[]{MediaContract.MediaEntry.COLUMN_SONG_ID},
                                MediaContract.PlaylistsEntry.COLUMN_PLAYLIST_NAME
                                        + " IN ("
                                        + getStringFromSelectorItems(filterItems)
                                        + ")",
                                null, null);
                break;
        }

        List<String> songIdList = new ArrayList<>();

        if (songsDataCursor != null && songsDataCursor.moveToFirst()) {
            do {
                //noinspection ResourceType
                songIdList.add(songsDataCursor.getString(0));
                songsDataCursor.close();
            } while (songsDataCursor.moveToFirst());
        }

        return songIdList;
    }

    public List<MutableMediaMetadata> getSongsMetadataByFilter(List<String> filterItems, CustomTypes.ItemType itemType) {

        Cursor songsDataCursor = null;

        switch (itemType) {
            case ALBUM_KEY:
                songsDataCursor = mContext.getContentResolver()
                        .query(MediaContract.MediaEntry.CONTENT_URI,
                                projection,
                                MediaContract.MediaEntry.COLUMN_ALBUM_KEY
                                        + " IN ("
                                        + getStringFromSelectorItems(filterItems)
                                        + ")",
                                null, null);

                break;
            case ARTIST_KEY:
                songsDataCursor = mContext.getContentResolver()
                        .query(MediaContract.MediaEntry.CONTENT_URI,
                                projection,
                                MediaContract.MediaEntry.COLUMN_ARTIST_KEY
                                        + " IN ("
                                        + getStringFromSelectorItems(filterItems)
                                        + ")",
                                null, null);
                break;
            case FOLDER:
                songsDataCursor = mContext.getContentResolver()
                        .query(MediaContract.MediaEntry.CONTENT_URI,
                                projection,
                                MediaContract.MediaEntry.COLUMN_FOLDER_PATH
                                        + " IN ("
                                        + getStringFromSelectorItems(filterItems)
                                        + ")",
                                null, null);
                break;
            case PLAYLIST:
                songsDataCursor = mContext.getContentResolver()
                        .query(
                                MediaContract.PlaylistsEntry.CONTENT_URI,
                                projection,
                                MediaContract.PlaylistsEntry.COLUMN_PLAYLIST_NAME
                                        + " IN ("
                                        + getStringFromSelectorItems(filterItems)
                                        + ")",
                                null, null);
                break;
        }

        List<MutableMediaMetadata> songMetadataList = new ArrayList<>();

        if (songsDataCursor != null && songsDataCursor.moveToFirst()) {
            do {
                //noinspection ResourceType
                MediaMetadataCompat mediaMetadataCompat =
                        new MediaMetadataCompat
                                .Builder()
                                .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID, songsDataCursor.getString(COL_INDEX_ID))
                                .putString(MutableMediaMetadata.CUSTOM_METADATA_KEY_TRACK_ID, songsDataCursor.getString(COL_INDEX_SONG_ID))
                                .putString(MutableMediaMetadata.CUSTOM_METADATA_KEY_TRACK_SOURCE, songsDataCursor.getString(COL_INDEX_PATH))
                                .putString(MediaMetadataCompat.METADATA_KEY_TITLE, songsDataCursor.getString(COL_INDEX_TITLE))
                                .putString(MediaMetadataCompat.METADATA_KEY_ALBUM, songsDataCursor.getString(COL_INDEX_ALBUM))
                                .putString(MutableMediaMetadata.CUSTOM_METADATA_KEY_ALBUM_KEY, songsDataCursor.getString(COL_INDEX_ALBUM_KEY))
                                .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, songsDataCursor.getString(COL_INDEX_ARTIST))
                                .putString(MutableMediaMetadata.CUSTOM_METADATA_KEY_ARTIST_KEY, songsDataCursor.getString(COL_INDEX_ARTIST_KEY))
                                .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, songsDataCursor.getLong(COL_INDEX_DURATION))
                                .putString(MutableMediaMetadata.CUSTOM_METADATA_KEY_FOLDER_PATH, songsDataCursor.getString(COL_INDEX_FOLDER_PATH))
                                .putString(MediaMetadataCompat.METADATA_KEY_ALBUM_ART_URI, songsDataCursor.getString(COL_INDEX_ALBUM_ART))
                                .build();

                MutableMediaMetadata mutableMediaMetadata =
                        new MutableMediaMetadata(
                                mediaMetadataCompat.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID),
                                mediaMetadataCompat
                        );

                songMetadataList.add(mutableMediaMetadata);
            } while (songsDataCursor.moveToNext());
        }

        return songMetadataList;
    }

    public MutableMediaMetadata getSongMetadataForId(String id) {

        Cursor songDataCursor = mContext.getContentResolver().query(MediaContract.MediaEntry.CONTENT_URI,
                projection,
                MediaContract.MediaEntry.COLUMN_SONG_ID + "=?",
                new String[]{id},
                null);

        if (songDataCursor == null || !songDataCursor.moveToFirst()) {
            return null;
        }

        //noinspection ResourceType
        MediaMetadataCompat mediaMetadataCompat =
                new MediaMetadataCompat
                        .Builder()
                        .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID, songDataCursor.getString(COL_INDEX_ID))
                        .putString(MutableMediaMetadata.CUSTOM_METADATA_KEY_TRACK_ID, songDataCursor.getString(COL_INDEX_SONG_ID))
                        .putString(MutableMediaMetadata.CUSTOM_METADATA_KEY_TRACK_SOURCE, songDataCursor.getString(COL_INDEX_PATH))
                        .putString(MediaMetadataCompat.METADATA_KEY_TITLE, songDataCursor.getString(COL_INDEX_TITLE))
                        .putString(MediaMetadataCompat.METADATA_KEY_ALBUM, songDataCursor.getString(COL_INDEX_ALBUM))
                        .putString(MutableMediaMetadata.CUSTOM_METADATA_KEY_ALBUM_KEY, songDataCursor.getString(COL_INDEX_ALBUM_KEY))
                        .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, songDataCursor.getString(COL_INDEX_ARTIST))
                        .putString(MutableMediaMetadata.CUSTOM_METADATA_KEY_ARTIST_KEY, songDataCursor.getString(COL_INDEX_ARTIST_KEY))
                        .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, songDataCursor.getLong(COL_INDEX_DURATION))
                        .putString(MutableMediaMetadata.CUSTOM_METADATA_KEY_FOLDER_PATH, songDataCursor.getString(COL_INDEX_FOLDER_PATH))
                        .putString(MediaMetadataCompat.METADATA_KEY_ALBUM_ART_URI, songDataCursor.getString(COL_INDEX_ALBUM_ART))
                        .build();

        MutableMediaMetadata mutableMediaMetadata =
                new MutableMediaMetadata(
                        mediaMetadataCompat.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID),
                        mediaMetadataCompat
                );

        return mutableMediaMetadata;
    }

    public void storeSongRating(String songId, RatingCompat ratingCompat) {
        ContentValues cv = new ContentValues();
        cv.put(MediaContract.PlaylistsEntry.COLUMN_SONG_ID, songId);


        // removing from both liked and disliked
        mContext.getContentResolver().delete(MediaContract.PlaylistsEntry.CONTENT_URI,
                MediaContract.PlaylistsEntry.COLUMN_SONG_ID + " = ? " +
                        "AND " + MediaContract.PlaylistsEntry.COLUMN_PLAYLIST_NAME +
                        " IN (?, ?) ",
                new String[]{songId, StaticStrings.PlAYLIST_NAME_LIKED, StaticStrings.PlAYLIST_NAME_DISLIKED});

        // adding only if rated (liked or disliked)
        if (ratingCompat.isRated()) {
            if (ratingCompat.isThumbUp()) {
                cv.put(MediaContract.PlaylistsEntry.COLUMN_PLAYLIST_NAME, StaticStrings.PlAYLIST_NAME_LIKED);
            } else {
                cv.put(MediaContract.PlaylistsEntry.COLUMN_PLAYLIST_NAME, StaticStrings.PlAYLIST_NAME_DISLIKED);
            }
            mContext.getContentResolver().insert(MediaContract.PlaylistsEntry.CONTENT_URI, cv);
        }
    }
}
