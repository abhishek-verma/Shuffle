package com.inpen.shuffle.syncmedia;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.net.Uri;
import android.provider.MediaStore;

import com.inpen.shuffle.model.MutableMediaMetadata;
import com.inpen.shuffle.model.database.MediaContract;
import com.inpen.shuffle.utility.LogHelper;

import java.util.Vector;

/**
 * Created by Abhishek on 10/25/2016.
 */

public class LocalMediaEndpoint implements MediaEndpoint {

    private static final String TAG = LogHelper.makeLogTag(LocalMediaEndpoint.class);
    Context mContext;

    public LocalMediaEndpoint(Context context) {
        mContext = context;
    }

    @Override
    public void syncMedia(Callback callback) {

        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String selection = MediaStore.Audio.Media.IS_MUSIC + "!=0";
        String sortOrder = MediaStore.Audio.Media.TITLE + " ASC";

        final int COL_INDEX_ID = 0;
        final int COL_INDEX_DATA = 1;
        final int COL_INDEX_TITLE = 2;
        final int COL_INDEX_ALBUM = 3;
        final int COL_INDEX_ALBUM_KEY = 4;
        final int COL_INDEX_ARTIST = 5;
        final int COL_INDEX_ARTIST_KEY = 6;
        final int COL_INDEX_DURATION = 7;
        final int COL_INDEX_ALBUM_ID = 8;
        String[] projection = {
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.DATA,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.ALBUM,
                MediaStore.Audio.Media.ALBUM_KEY,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.ARTIST_KEY,
                MediaStore.Audio.Media.DURATION,
                MediaStore.Audio.Media.ALBUM_ID
        };

        Cursor cur = mContext.getContentResolver()
                .query(uri, projection, selection, null, sortOrder);

        Vector<ContentValues> cvVector = new Vector<>();
        ContentValues cv;
        if (cur != null && cur.moveToFirst()) {
            do {
                String id = cur.getString(COL_INDEX_ID);
                String path = cur.getString(COL_INDEX_DATA);
                String title = cur.getString(COL_INDEX_TITLE);
                String album = cur.getString(COL_INDEX_ALBUM);
                String artist = cur.getString(COL_INDEX_ARTIST);
                long duration = cur.getLong(COL_INDEX_DURATION);
                String albumKey = cur.getString(COL_INDEX_ALBUM_KEY).replaceAll("[^\\w\\s\\-_]", "");
                String artistKey = cur.getString(COL_INDEX_ARTIST_KEY).replaceAll("[^\\w\\s\\-_]", "");
                String albumArt = getAlbumArtForAlbum(cur.getInt(COL_INDEX_ALBUM_ID));

                cv = new ContentValues();
                cv.put(MediaContract.MediaEntry._ID, id);
                cv.put(MediaContract.MediaEntry.COLUMN_SONG_ID, MutableMediaMetadata.generateTrackID(title, artist, duration));
                cv.put(MediaContract.MediaEntry.COLUMN_PATH, path);
                cv.put(MediaContract.MediaEntry.COLUMN_TITLE, title);
                cv.put(MediaContract.MediaEntry.COLUMN_ALBUM, album);
                cv.put(MediaContract.MediaEntry.COLUMN_ALBUM_KEY, albumKey);
                cv.put(MediaContract.MediaEntry.COLUMN_ARTIST, artist);
                cv.put(MediaContract.MediaEntry.COLUMN_ARTIST_KEY, artistKey);
                cv.put(MediaContract.MediaEntry.COLUMN_DURATION, Long.toString(duration));
                cv.put(MediaContract.MediaEntry.COLUMN_FOLDER_PATH, MediaContract.MediaEntry.getFolderPathFromFullPath(path));
                cv.put(MediaContract.MediaEntry.COLUMN_ALBUM_ART, albumArt);
                cvVector.add(cv);
            } while (cur.moveToNext());
        }

        if (cur != null)
            cur.close();

        int inserted = 0;

        // removing all previous items
//        mContext.getContentResolver().delete(MediaContract.MediaEntry.CONTENT_URI, null, null);
        // adding to db
        if (cvVector.size() > 0) {
            ContentValues[] cvArray = new ContentValues[cvVector.size()];
            cvVector.toArray(cvArray);
            try {
                inserted = mContext.getContentResolver().bulkInsert(MediaContract.MediaEntry.CONTENT_URI, cvArray);
            } catch (SQLiteConstraintException exception) {
                // FIXME since not all songs are being inserted [IMPORTANT}
                // try removing this try-catch to find the error
                LogHelper.e(TAG, "Cannot insert into database! ERROR: " + exception);
            }
        }

        callback.onDataSynced(inserted);
    }


    private String getAlbumArtForAlbum(int albumId) {


        final Uri artworkUri = Uri.parse("content://media/external/audio/albumart");
        Uri uri = ContentUris.withAppendedId(artworkUri, albumId);

        return uri.toString();
        // If above doesn't work, use this instead
//
//        Cursor cursor = mContext.getContentResolver().query(MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI,
//                new String[]{MediaStore.Audio.Albums.ALBUM_KEY, MediaStore.Audio.Albums.ALBUM_ART},
//                MediaStore.Audio.Albums.ALBUM_KEY + "=?",
//                new String[]{String.valueOf(albumId)},
//                null);
//        if (cursor.moveToFirst()) {
//            String path = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.AlbumColumns.ALBUM_ART));
//            return path;
//        }
//        return "";

    }
}
