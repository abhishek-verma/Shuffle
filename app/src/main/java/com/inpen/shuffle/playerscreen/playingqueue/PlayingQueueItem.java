package com.inpen.shuffle.playerscreen.playingqueue;

import android.support.v4.media.MediaMetadataCompat;
import android.text.TextUtils;

import com.inpen.shuffle.mainscreen.items.Item;
import com.inpen.shuffle.model.MutableMediaMetadata;

/**
 * Created by Abhishek on 3/26/2017.
 */

public class PlayingQueueItem implements Comparable<PlayingQueueItem> {

    public String id;
    public String imagePath;
    public String title;
    private int position;

    public PlayingQueueItem(MediaMetadataCompat metadata,
                            int position) {

        this(metadata.getString(MutableMediaMetadata.CUSTOM_METADATA_KEY_TRACK_ID),
                metadata.getString(MediaMetadataCompat.METADATA_KEY_TITLE),
                metadata.getString(MediaMetadataCompat.METADATA_KEY_ALBUM_ART_URI),
                position);
    }

    public PlayingQueueItem(String id, String title, String imagePath, int position) {
        this.id = id;
        this.title = title;
        this.imagePath = imagePath;
        this.position = position;
    }

    @Override
    public String toString() {
        return "id:" + id + " title:" + title;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || o.getClass() != MutableMediaMetadata.class) {
            return false;
        }

        Item that = (Item) o;

        return TextUtils.equals(id, that.id);
    }

    @Override
    public int compareTo(PlayingQueueItem item) {
        return title.compareTo(item.title);
    }

}
