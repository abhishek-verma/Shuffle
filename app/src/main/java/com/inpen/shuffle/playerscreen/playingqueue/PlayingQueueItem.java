package com.inpen.shuffle.playerscreen.playingqueue;

import android.support.v4.media.MediaMetadataCompat;
import android.text.TextUtils;

import com.inpen.shuffle.mainscreen.items.Item;
import com.inpen.shuffle.model.MutableMediaMetadata;
import com.inpen.shuffle.utility.BaseItem;

/**
 * Created by Abhishek on 3/26/2017.
 */

public class PlayingQueueItem extends BaseItem {

    private int position;

    public PlayingQueueItem(MediaMetadataCompat metadata,
                                   int position) {

        this(metadata.getString(MutableMediaMetadata.CUSTOM_METADATA_KEY_TRACK_ID),
                metadata.getString(MediaMetadataCompat.METADATA_KEY_TITLE),
                metadata.getString(MediaMetadataCompat.METADATA_KEY_ALBUM_ART_URI),
                position);
    }

    public PlayingQueueItem(String id, String title, String imagePath, int position) {
        super(id, title, imagePath);
        this.position = position;
    }

}