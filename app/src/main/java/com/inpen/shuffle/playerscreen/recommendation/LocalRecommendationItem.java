package com.inpen.shuffle.playerscreen.recommendation;

import android.support.v4.media.MediaMetadataCompat;
import android.text.TextUtils;

import com.inpen.shuffle.mainscreen.items.Item;
import com.inpen.shuffle.model.MutableMediaMetadata;
import com.inpen.shuffle.utility.BaseItem;

/**
 * Created by Abhishek on 3/26/2017.
 */

public class LocalRecommendationItem extends BaseItem {

    public String artist;

    public LocalRecommendationItem(MediaMetadataCompat metadata) {

        this(metadata.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID), // here we save the device provided id
                metadata.getString(MediaMetadataCompat.METADATA_KEY_TITLE),
                metadata.getString(MediaMetadataCompat.METADATA_KEY_ALBUM_ART_URI),
                metadata.getString(MediaMetadataCompat.METADATA_KEY_ARTIST));
    }

    public LocalRecommendationItem(String id, String title, String imagePath, String artist) {
        super(id, title, imagePath);
        this.artist = artist;
    }

}
